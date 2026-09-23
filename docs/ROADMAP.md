# Roadmap

This is a direction-of-travel document, not a claim that planned capabilities exist.

## Current snapshot

- BLE advertisement observations through Android APIs
- 15-second scan lifecycle and periodic UI refresh
- Aurora OpenGL ES background and basic Studio toggles
- Local interface-address summary
- Small JNI/AArch64 animation helper

## Exploration goals

- [ ] DNS-SD/mDNS service browsing with best-effort endpoint resolution
- [ ] Service metadata views with provenance and uncertainty
- [ ] Correlation across observations without asserting identity from weak signals
- [ ] Optional, user-controlled local history and export
- [ ] Meaningful renderer controls: glow, opacity, palette, density, motion
- [ ] Better scan-state handling across lifecycle transitions and OEM variants

## Engineering milestones

- [ ] Reproducible build from a clean Termux environment
- [ ] Install/runtime verification on target Android versions
- [ ] Automated static checks where practical
- [ ] Documented permission matrix and test matrix

## Non-goals

VITRA is not intended to bypass Android permission boundaries, intercept cellular communications, decode arbitrary RF, or imply authorization based on discoverability. Any future network checks should be explicitly user-initiated and limited to authorized environments.
