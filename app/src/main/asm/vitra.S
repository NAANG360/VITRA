.text
.align 2
.global Java_com_vitra_glass_MainActivity_nativePulse
.type Java_com_vitra_glass_MainActivity_nativePulse, %function

// JNI static float nativePulse(float time, float x, float y)
// AArch64 float args: s0, s1, s2. Returns time + x*0.5 + y*0.125.
Java_com_vitra_glass_MainActivity_nativePulse:
    fmov    s3, #0.5
    fmadd   s0, s1, s3, s0
    fmov    s3, #0.125
    fmadd   s0, s2, s3, s0
    ret
.size Java_com_vitra_glass_MainActivity_nativePulse, .-Java_com_vitra_glass_MainActivity_nativePulse