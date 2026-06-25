// Sethares spectral dissonance (Sethares 1993).
// Identical formula to DissonanceLib's Dissonance.dissmeasure — same constants,
// same inner loop — re-implemented here so CamelotMix has no hard dependency
// on the DissonanceLib quark. Returns a non-negative roughness scalar; the
// caller normalises (spectralCompatibility wraps the standard normalisation).

Sethares {
    classvar dStar = 0.24, s1 = 0.0207, s2 = 18.96,
             c1 = 5.0, c2 = -5.0,
             a1 = -3.51, a2 = -5.75;

    // Raw roughness between two spectra. Each spectrum is [freqs, amps] —
    // typically the top-N FFT peaks of an audio source. Zero-frequency
    // partials are skipped (Sethares is undefined there).
    *dissmeasure { |freqsA, ampsA, freqsB, ampsB|
        var d = 0;
        if (freqsA.isNil || freqsB.isNil) { ^0 };
        if (freqsA.size == 0 || freqsB.size == 0) { ^0 };
        freqsA.do({ |f, i|
            var a = ampsA[i];
            freqsB.do({ |g, j|
                var b = ampsB[j];
                var s, fdif, arg1, arg2, exp1, exp2;
                if ((f > 0) && (g > 0)) {
                    s = dStar / ((s1 * min(f, g)) + s2);
                    fdif = absdif(g, f);
                    arg1 = a1 * s * fdif;
                    arg2 = a2 * s * fdif;
                    exp1 = if (arg1 < -88) { 0 } { exp(arg1) };
                    exp2 = if (arg2 < -88) { 0 } { exp(arg2) };
                    d = d + (min(a, b) * ((c1 * exp1) + (c2 * exp2)));
                };
            });
        });
        ^d
    }

    // 0..1 compatibility score: 1 = no roughness, 0 = saturated. The
    // baseline is calibrated against typical self-dissonance of pop spectra
    // so amplitude differences don't dominate.
    *spectralCompatibility { |freqsA, ampsA, freqsB, ampsB, baselineRoughness = 6|
        var raw, compat;
        if (freqsA.isNil || freqsB.isNil) { ^0.5 };
        if (freqsA.size == 0 || freqsB.size == 0) { ^0.5 };
        raw = this.dissmeasure(freqsA, ampsA, freqsB, ampsB);
        if (raw <= 0) { ^1.0 };
        compat = 1 - (raw / baselineRoughness);
        ^compat.clip(0, 1)
    }
}
