# CamelotMix

A SuperCollider quark for harmonic mixing. Three classes, zero UGen
dependencies — pure sclang.

| Class | Purpose |
|---|---|
| `Camelot` | Camelot wheel key shorthand + adjacency-based compatibility (0..1). |
| `Sethares` | Sethares (1993) spectral roughness over partial arrays. |
| `HarmonicMix` | Weighted facade: combines key + spectral + BPM into a single candidate score; `recommend` ranks N candidates. |

## Why

DJ track sequencing usually picks the next track by BPM alone. CamelotMix
gives you the two extra musical signals (key + timbre) in a form that
plugs straight into any selection / Pbind / live-coding workflow:

```supercollider
HarmonicMix.recommend(
    "A minor", 124, nil,
    library.collect({ |t| (key: t.key, bpm: t.bpm) }),
    5
);
```

## Install

```supercollider
Quarks.install("https://github.com/studio-sesma/CamelotMix");
```

For local development, clone into
`~/AppData/Local/SuperCollider/downloaded-quarks/CamelotMix` (Windows) or
`~/.local/share/SuperCollider/downloaded-quarks/CamelotMix` (Linux/macOS)
and add the path to `sclang_conf.yaml`.

## Test

Run from the repository root:

```powershell
& 'C:\Program Files\SuperCollider-3.14.1\sclang.exe' -D -r -s --include-path 'Classes' --include-path 'tests' 'tests\RunCamelotMix.scd'
```

Or inside sclang after loading the Quark classes:

```supercollider
TestCamelotMix.run;
```

## Authoring notes

The Sethares constants are identical to those used by the
[DissonanceLib](https://github.com/supercollider-quarks/DissonanceLib)
quark; CamelotMix re-implements the formula locally so installs without
DissonanceLib still work.

The Camelot table follows the Mixed In Key convention: major → `<n>B`,
minor → `<n>A`. Adjacent slots and same-number A/B pairs (relative
major/minor) are considered compatible.

License: MIT.
