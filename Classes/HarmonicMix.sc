// Convenience facade that combines the three independent harmonic signals
// into a single 0..1 candidate score for "should this be the next track?".
//
//   keyCompat       Camelot wheel adjacency       weight 0.5
//   spectralCompat  Sethares roughness over top partials   weight 0.3
//   bpmCompat       relative BPM proximity        weight 0.2
//
// Weights default to DJ-style "key first, timbre second, tempo third" — the
// caller can override via the `weights` argument when calling `score`. Any
// missing signal degrades gracefully to 0.5 (neutral).

HarmonicMix {

    // BPM compatibility: linear falloff. Same BPM → 1.0, ±5% of refBpm → 0.5,
    // ±10% → 0. Returns 0.5 when either BPM is missing so unmetered tracks
    // don't punish the rest.
    *bpmCompatibility { |fromBpm, toBpm, refBpm = 120|
        var diff, compat;
        if (fromBpm.isNil || toBpm.isNil) { ^0.5 };
        if ((fromBpm == 0) || (toBpm == 0)) { ^0.5 };
        diff = absdif(toBpm, fromBpm) / max(1, refBpm);
        compat = 1 - (diff * 10);
        ^compat.clip(0, 1)
    }

    // Returns a Dictionary with: total, keyCompat, spectralCompat, bpmCompat,
    // camelotFrom, camelotTo. `fromPartials`/`toPartials` (when supplied) are
    // each `[freqs, amps]` arrays — typically the top-10 FFT peaks of each
    // track. Pass nil to skip the spectral term.
    *score { |fromKey, toKey, fromBpm, toBpm, fromPartials, toPartials,
             refBpm, weights|
        var refBpmFinal = refBpm ?? fromBpm ?? toBpm ?? 120;
        var w = weights ?? (key: 0.5, spectral: 0.3, bpm: 0.2);
        var keyCompat = Camelot.compatibility(fromKey, toKey);
        var spectralCompat = if (fromPartials.notNil && toPartials.notNil) {
            Sethares.spectralCompatibility(
                fromPartials[0], fromPartials[1],
                toPartials[0], toPartials[1]
            )
        } { 0.5 };
        var bpmCompat = this.bpmCompatibility(fromBpm, toBpm, refBpmFinal);
        var total = (w[\key] * keyCompat)
                  + (w[\spectral] * spectralCompat)
                  + (w[\bpm] * bpmCompat);
        ^(
            total: total,
            keyCompat: keyCompat,
            spectralCompat: spectralCompat,
            bpmCompat: bpmCompat,
            camelotFrom: Camelot.fromKey(fromKey),
            camelotTo: Camelot.fromKey(toKey)
        )
    }

    // Pick the best next track from a list of candidates. Each candidate is
    // a Dictionary: ( key: <str>, bpm: <num>, partials: [freqs, amps] ?).
    // Returns the candidate plus its score Dictionary, sorted by total desc.
    *recommend { |currentKey, currentBpm, currentPartials, candidates, topN = 5|
        var ranked = candidates.collect({ |cand|
            var s = this.score(
                currentKey, cand[\key],
                currentBpm, cand[\bpm],
                currentPartials, cand[\partials]
            );
            (candidate: cand, score: s)
        });
        ranked = ranked.sort({ |a, b| a[\score][\total] > b[\score][\total] });
        ^ranked.copyRange(0, topN.max(1) - 1)
    }
}
