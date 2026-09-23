package com.vitra.glass;

import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.opengl.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.nio.*;
import java.util.*;
import java.net.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends Activity {
    static { System.loadLibrary("vitra"); }
    public static native float nativePulse(float time,float x,float y);

    volatile boolean deepMode=false, auroraEnabled=true, motionEnabled=true;
    private static final int REQ_BT=71;
    private BluetoothLeScanner scanner;
    private boolean scanning=false;
    private final ArrayList<Device> devices=new ArrayList<>();
    private TextView scanStatus;
    private long scanStarted;
    private long scanEnded;
    private Handler handler=new Handler(Looper.getMainLooper());
    // Keep the Canvas overlay repainting without requiring a touch event.
    private final Runnable uiRefresh = new Runnable() {
        @Override public void run() {
            if (screen != null) screen.invalidate();
            if (!isFinishing() && !isDestroyed()) handler.postDelayed(this, 500);
        }
    };
    private ScanCallback scanCallback;
    private volatile int callbackCount=0;
    private volatile int batchCount=0;
    private volatile int scanError=0;
    private volatile String diagState="IDLE";
    private volatile long lastResultAt=0;
    private volatile boolean timedOut=false;
    private VitraScreen screen;

    static class Device {
        String name, address, evidence;
        int rssi;
        long seen;
        Device(String n,String a,int s,String e){name=n;address=a;rssi=s;evidence=e;seen=System.currentTimeMillis();}
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.rgb(8,12,28));
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        screen=new VitraScreen(this);
        setContentView(screen);
        handler.removeCallbacks(uiRefresh);
        handler.post(uiRefresh);
    }
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    boolean hasBtPermission(){
        if(Build.VERSION.SDK_INT>=31){
            return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED;
        }
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }

    private final Runnable scanTimeout = () -> {
        if (scanning) {
            timedOut=true;
            diagState="TIMED OUT";
            stopScan();
        }
    };

    void beginScan(){
        if (scanning) stopScan();

        if(!hasBtPermission()){
            if(Build.VERSION.SDK_INT>=31){
                requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                }, REQ_BT);
            } else {
                requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
                }, REQ_BT);
            }
            return;
        }

        BluetoothManager bm=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter ba=bm==null?null:bm.getAdapter();

        if(ba==null || !ba.isEnabled()){
            toast("Enable Bluetooth to scan");
            return;
        }

        try {
            scanner=ba.getBluetoothLeScanner();
            if(scanner==null){
                toast("BLE scanner unavailable");
                return;
            }

            devices.clear();
            callbackCount=0;
            batchCount=0;
            scanError=0;
            lastResultAt=0;
            timedOut=false;
            diagState="STARTING";
            scanStarted=System.currentTimeMillis();
            scanEnded=0L;

            scanCallback=new ScanCallback(){
                @Override public void onScanResult(int type, ScanResult result){
                    callbackCount++;
                    lastResultAt=System.currentTimeMillis();
                    diagState="RECEIVING";
                    accept(result);
                    if(screen!=null) runOnUiThread(()->screen.invalidate());
                }

                @Override public void onBatchScanResults(List<ScanResult> results){
                    batchCount++;
                    callbackCount+=results.size();
                    lastResultAt=System.currentTimeMillis();
                    diagState="BATCH RECEIVED";
                    for(ScanResult r:results) accept(r);
                    if(screen!=null) runOnUiThread(()->screen.invalidate());
                }

                @Override public void onScanFailed(int error){
                    handler.post(()->{
                        handler.removeCallbacks(scanTimeout);
                        if (scanning && scanEnded == 0L) scanEnded = System.currentTimeMillis();
                        scanning=false;
                        scanError=error;
                        diagState="FAILED";
                        refreshStatus("Scan failed · error "+error);
                        if(screen!=null) screen.invalidate();
                        toast("BLE scan failed · "+error);
                    });
                }
            };

            android.bluetooth.le.ScanSettings settings =
                new android.bluetooth.le.ScanSettings.Builder()
                    .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build();

            scanner.startScan(null, settings, scanCallback);
            scanning=true;
            diagState="SCANNING";
            refreshStatus("SCANNING · LIVE BLE");
            if(screen!=null) screen.invalidate();

            handler.removeCallbacks(scanTimeout);
            handler.postDelayed(scanTimeout, 15000);

        } catch(SecurityException e){
            scanning=false;
            refreshStatus("Bluetooth permission denied");
            toast("Bluetooth permission denied");
        } catch(IllegalStateException e){
            scanning=false;
            refreshStatus("Scanner unavailable");
            toast("BLE scanner unavailable");
        }
    }

    void accept(ScanResult r){
        if(r==null || r.getDevice()==null) return;

        BluetoothDevice d=r.getDevice();
        String addr="Unknown";

        try { addr=d.getAddress(); }
        catch(SecurityException ignored){}

        String name=null;
        ScanRecord rec=r.getScanRecord();
        if(rec!=null) name=rec.getDeviceName();

        if(name==null || name.trim().isEmpty()) name="Unnamed device";

        String evidence="BLE advertisement";
        if(rec!=null){
            byte[] data=rec.getBytes();
            if(data!=null && contains(data,new byte[]{(byte)0xF6,(byte)0xFF}))
                evidence="Possible Matter-related service data · unverified";
        }

        final String n=name, a=addr, e=evidence;
        final int signal=r.getRssi();

        runOnUiThread(()->{
            boolean found=false;

            for(Device old:devices){
                if(old.address.equals(a)){
                    old.name=n;
                    old.rssi=signal;
                    old.evidence=e;
                    old.seen=System.currentTimeMillis();
                    found=true;
                    break;
                }
            }

            if(!found) devices.add(0,new Device(n,a,signal,e));

            refreshStatus("LIVE · "+devices.size()+" devices observed");
            if(screen!=null) screen.invalidate();
        });
    }

    static boolean contains(byte[] hay,byte[] needle){
        if(hay==null || needle==null || needle.length==0 || hay.length<needle.length)
            return false;

        outer:for(int i=0;i<=hay.length-needle.length;i++){
            for(int j=0;j<needle.length;j++)
                if(hay[i+j]!=needle[j]) continue outer;
            return true;
        }
        return false;
    }

    void stopScan(){
        handler.removeCallbacks(scanTimeout);
        // Freeze elapsed time at the actual stop, rather than letting the UI
        // counter continue increasing after the scanner has stopped.
        if (scanning && scanEnded == 0L) scanEnded = System.currentTimeMillis();

        try {
            if(scanner!=null && scanCallback!=null && hasBtPermission())
                scanner.stopScan(scanCallback);
        } catch(SecurityException ignored){}

        scanning=false;
        if(!timedOut && scanError==0) diagState="STOPPED";
        refreshStatus("SCAN PAUSED · "+devices.size()+" observed");
        if(screen!=null) screen.invalidate();
    }

    String localNetworkSummary(){
        ArrayList<String> found=new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces();
            while(interfaces!=null && interfaces.hasMoreElements()){
                NetworkInterface ni=interfaces.nextElement();
                if(!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addresses=ni.getInetAddresses();
                while(addresses.hasMoreElements()){
                    InetAddress a=addresses.nextElement();
                    if(a.isLoopbackAddress() || a.isLinkLocalAddress()) continue;
                    found.add(ni.getName()+" · "+a.getHostAddress());
                }
            }
        } catch(Exception ignored){}
        if(found.isEmpty()) return "No active local interface reported";
        StringBuilder out=new StringBuilder();
        for(int i=0;i<Math.min(3,found.size());i++){
            if(i>0) out.append("  |  ");
            out.append(found.get(i));
        }
        return out.toString();
    }

    void refreshStatus(String s){
        if(scanStatus!=null) scanStatus.setText(s);
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_BT){boolean ok=true;for(int x:g)if(x!=PackageManager.PERMISSION_GRANTED)ok=false;if(ok)beginScan();else toast("Bluetooth scan permission is required");}
    }
    @Override protected void onDestroy(){handler.removeCallbacks(uiRefresh);stopScan();super.onDestroy();}

    class VitraScreen extends FrameLayout {
        VitraScreen(Context c){
            super(c);setBackgroundColor(0xff080c1c);
            GLSurfaceView gl=new GLSurfaceView(c);gl.setEGLContextClientVersion(2);
            gl.setRenderer(new AuroraRenderer());gl.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            addView(gl,new FrameLayout.LayoutParams(-1,-1));
            addView(new GlassOverlay(c),new FrameLayout.LayoutParams(-1,-1));
        }
    }
    class AuroraRenderer implements GLSurfaceView.Renderer {
        int program,pos,timeLoc,resLoc,enabledLoc,deepLoc;int sw=1,sh=1;long start;
        final String vertex="attribute vec2 p; void main(){gl_Position=vec4(p,0.,1.);}";
        final String fragment="precision mediump float;uniform float t;uniform vec2 r;uniform float enabled;uniform float deep;void main(){vec2 uv=gl_FragCoord.xy/r;float a=sin(uv.x*4.+t*.55+sin(uv.y*3.+t*.3));float b=cos(uv.y*5.-t*.35+uv.x*2.);float glow=.5+.5*sin(a+b+t*.25);vec3 c=mix(vec3(.025,.07,.20),vec3(.22,.10,.42),glow*.72);c=mix(c,vec3(.08,.62,.68),smoothstep(.65,.98,glow)*.55);float v=1.-smoothstep(.25,1.1,length((uv-.5)*vec2(.8,1.)));c*=.55+.45*v;c=mix(vec3(.012,.02,.055),c,enabled);c=mix(c,c*.45,deep);gl_FragColor=vec4(c,1.);}";
        int shader(int type,String src){int s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,src);GLES20.glCompileShader(s);return s;}
        public void onSurfaceCreated(GL10 g,EGLConfig c){program=GLES20.glCreateProgram();GLES20.glAttachShader(program,shader(GLES20.GL_VERTEX_SHADER,vertex));GLES20.glAttachShader(program,shader(GLES20.GL_FRAGMENT_SHADER,fragment));GLES20.glLinkProgram(program);pos=GLES20.glGetAttribLocation(program,"p");timeLoc=GLES20.glGetUniformLocation(program,"t");resLoc=GLES20.glGetUniformLocation(program,"r");enabledLoc=GLES20.glGetUniformLocation(program,"enabled");deepLoc=GLES20.glGetUniformLocation(program,"deep");start=System.nanoTime();}
        public void onSurfaceChanged(GL10 g,int w,int h){sw=Math.max(1,w);sh=Math.max(1,h);GLES20.glViewport(0,0,w,h);}
        public void onDrawFrame(GL10 g){float t=(System.nanoTime()-start)/1e9f;if(motionEnabled)t=nativePulse(t,.3f,.7f);else t=0;GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);GLES20.glUseProgram(program);FloatBuffer b=ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();b.put(new float[]{-1,-1,1,-1,-1,1,1,1}).position(0);GLES20.glEnableVertexAttribArray(pos);GLES20.glVertexAttribPointer(pos,2,GLES20.GL_FLOAT,false,0,b);GLES20.glUniform1f(timeLoc,t);GLES20.glUniform2f(resLoc,sw,sh);GLES20.glUniform1f(enabledLoc,auroraEnabled?1:0);GLES20.glUniform1f(deepLoc,deepMode?1:0);GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);}
    }
    class GlassOverlay extends View {
        Paint p=new Paint(3);int page=0;float density,W,H,downX,downY;int panel;
        String[] tabs={"Home","RADAR","Studio","Core"},icons={"⌂","◎","✧","◉"};
        GlassOverlay(Context c){super(c);density=getResources().getDisplayMetrics().density;setLayerType(View.LAYER_TYPE_SOFTWARE,null);setClickable(true);}
        void txt(Canvas c,String s,float x,float y,float z,int col,boolean bold){p.setColor(col);p.setTextSize(z);p.setTypeface(Typeface.create("sans-serif",bold?1:0));p.setStyle(Paint.Style.FILL);c.drawText(s,x,y,p);}
        void rr(Canvas c,float l,float t,float r,float b,float rad,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawRoundRect(l,t,r,b,rad,rad,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0x55ffffff);c.drawRoundRect(l,t,r,b,rad,rad,p);p.setStyle(Paint.Style.FILL);}
        void header(Canvas c,String title,String sub){txt(c,"V I T R A",22,38,15,0xffe7faff,true);rr(c,W-91,8,W-20,43,18,0x33ffffff);txt(c,"✦ CORE",W-80,29,10,0xffd9faff,true);txt(c,title,22,101,29,Color.WHITE,true);txt(c,sub,22,126,12,0xffc2d7ed,false);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);W=getWidth()/density;H=getHeight()/density;c.save();c.scale(density,density);panel=deepMode?0x44101b37:0x35ffffff;
            if(page==0){header(c,"Discover your space.","VITRA · ambient intelligence");rr(c,18,151,W-18,277,24,panel);txt(c,"AURORA CORE",34,180,10,0xffa9f4ff,true);txt(c,"A living interface.",34,215,22,Color.WHITE,true);txt(c,"Light, depth, discovery.",34,239,12,0xffc2d7ed,false);txt(c,"OPEN RADAR  →",34,261,11,0xffa9f4ff,true);
                rr(c,18,293,W-18,390,20,panel);txt(c,"LIVE DISCOVERY",34,324,10,0xffa9f4ff,true);txt(c,scanning?"Scanning nearby BLE…":"Bluetooth discovery",34,353,17,Color.WHITE,true);txt(c,devices.size()+" observed · local only",34,376,11,0xffc2d7ed,false);
                rr(c,18,406,W-18,486,20,panel);txt(c,"VISUAL ENGINE",34,437,10,0xffa9f4ff,true);txt(c,deepMode?"Deep mode · enabled":"Aurora · "+(auroraEnabled?"enabled":"disabled"),34,465,15,Color.WHITE,true);
            }else if(page==1){header(c,"RADAR.","Nearby signals · evidence first");rr(c,18,145,W-18,204,18,panel);txt(c,scanning?"● SCANNING":"○ PAUSED",32,171,12,0xffa9f4ff,true);txt(c,devices.size()+" devices · 15 sec scan window",32,190,10,0xffc2d7ed,false);
                rr(c,W-112,211,W-18,251,18,0x553beaff);txt(c,scanning?"STOP SCAN":"SCAN",W-96,236,11,Color.WHITE,true);
                txt(c,"OBSERVED DEVICES",22,280,10,0xffa9f4ff,true);
                txt(c,"State: "+diagState+" · callbacks: "+callbackCount,22,298,9,0xffa9f4ff,false);
                txt(c,"Batches: "+batchCount+" · unique: "+devices.size()+" · error: "+scanError,22,313,9,0xffc2d7ed,false);
                String age=lastResultAt==0?"none":((System.currentTimeMillis()-lastResultAt)/1000)+"s ago";
                txt(c,"Last result: "+age+" · elapsed: "+((scanStarted==0?0:((scanning?System.currentTimeMillis():(scanEnded==0?System.currentTimeMillis():scanEnded))-scanStarted))/1000)+"s",22,328,9,0xffc2d7ed,false);
                if(devices.isEmpty()){txt(c,"No observations yet.",24,365,16,Color.WHITE,true);txt(c,"Check Bluetooth and scan state above.",24,389,11,0xffc2d7ed,false);}
                else {int count=Math.min(4,devices.size());for(int i=0;i<count;i++){Device d=devices.get(i);float y=340+i*66;rr(c,18,y,W-18,y+58,15,panel);txt(c,d.name,30,y+22,13,Color.WHITE,true);txt(c,d.address+" · "+d.rssi+" dBm",30,y+40,9,0xffc2d7ed,false);}}
                txt(c,"Matter clues are unverified; BLE ≠ Matter proof.",22,H-111,9,0xffc2d7ed,false);
            }else if(page==2){header(c,"Studio.","Tune the atmosphere");String[] labels={"Aurora","Deep mode","Motion"};String[] vals={auroraEnabled?"ON":"OFF",deepMode?"ON":"OFF",motionEnabled?"ON":"OFF"};for(int i=0;i<3;i++){float y=160+i*105;rr(c,18,y,W-18,y+82,19,panel);txt(c,labels[i],34,y+32,17,Color.WHITE,true);txt(c,"Tap to toggle · "+vals[i],34,y+57,11,0xffa9f4ff,false);}}
            else{header(c,"Core.","Local status & evidence");rr(c,18,145,W-18,240,20,panel);txt(c,"VITRA ENGINE",32,174,10,0xffa9f4ff,true);txt(c,"ARM64 JNI · loaded",32,201,16,Color.WHITE,true);txt(c,"BLE observations stay on-device.",32,222,11,0xffc2d7ed,false);rr(c,18,252,W-18,348,20,panel);txt(c,"SCAN STATUS",32,280,10,0xffa9f4ff,true);txt(c,diagState+" · "+(scanning?"active":"idle"),32,306,14,Color.WHITE,true);txt(c,devices.size()+" unique · "+callbackCount+" callbacks",32,328,10,0xffc2d7ed,false);rr(c,18,360,W-18,455,20,panel);txt(c,"ACTIVE LOCAL INTERFACES",32,388,10,0xffa9f4ff,true);String net=localNetworkSummary();txt(c,net.length()>54?net.substring(0,51)+"…":net,32,414,9,0xffd7eafa,false);txt(c,"Interface addresses only · no host scan",32,434,9,0xffc2d7ed,false);txt(c,"Observed ≠ identified ≠ certified.",22,H-105,10,0xffa9f4ff,true);}
            float y=H-78,tw=(W-40)/4;rr(c,16,y,W-16,H-9,22,0x660b1630);for(int i=0;i<4;i++){float x=20+i*tw;if(i==page)rr(c,x,y+5,x+tw-3,H-15,15,0x44c3f8ff);txt(c,icons[i],x+tw/2-6,y+27,16,Color.WHITE,true);txt(c,tabs[i],x+tw/2-tabs[i].length()*2.7f,y+49,9,0xffd7eafa,true);}c.restore();}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX()/density,y=e.getY()/density;if(e.getAction()==0){downX=x;downY=y;return true;}if(e.getAction()==1){if(Math.abs(x-downX)>65){page=Math.max(0,Math.min(3,page+(x<downX?1:-1)));invalidate();return true;}if(y>=H-84){int n=(int)((x-20)/((W-40)/4));if(n>=0&&n<4)page=n;}else if(page==0){if(y>145&&y<277){page=1;beginScan();}else if(y>293&&y<390){page=1;beginScan();}else if(y>406&&y<486)deepMode=!deepMode;}else if(page==1&&y>205&&y<255){if(scanning)stopScan();else beginScan();}else if(page==2){if(y>=155&&y<242)auroraEnabled=!auroraEnabled;else if(y<347&&y>=260)deepMode=!deepMode;else if(y>=365&&y<470)motionEnabled=!motionEnabled;}invalidate();return true;}return true;}
    }
}
