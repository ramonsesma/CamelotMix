// Unit tests for CamelotMix. Run from sclang with:
//   TestCamelotMix.run
//
// Mirrors the bridge's TypeScript test suite (harmonic-compat.test.ts) so the
// two implementations are kept consistent.

TestCamelotMix : UnitTest {

    test_camelot_maps_canonical_keys {
        this.assertEquals(Camelot.fromKey("A minor"), "8A");
        this.assertEquals(Camelot.fromKey("C major"), "8B");
        this.assertEquals(Camelot.fromKey("F# minor"), "11A");
    }

    test_camelot_bare_letter_falls_back_to_minor {
        this.assertEquals(Camelot.fromKey("A"), "8A");
        this.assertEquals(Camelot.fromKey("C#"), "12A");
    }

    test_camelot_unknown_returns_nil {
        this.assert(Camelot.fromKey(nil).isNil, "nil input");
        this.assert(Camelot.fromKey("nonsense").isNil, "garbage input");
    }

    test_camelot_unison_scores_1 {
        this.assertEquals(Camelot.compatibility("A minor", "A minor"), 1.0);
    }

    test_camelot_one_step_better_than_two_steps {
        var one = Camelot.compatibility("A minor", "E minor"); // 8A → 9A
        var two = Camelot.compatibility("A minor", "B minor"); // 8A → 10A
        this.assert(one > two, "±1 step should beat ±2 step");
    }

    test_camelot_relative_pair_compatible {
        // 8A (A minor) ↔ 8B (C major) — relative pair.
        this.assert(Camelot.compatibility("A minor", "C major") >= 0.8,
            "relative major/minor should be highly compatible");
    }

    test_camelot_neutral_when_missing {
        this.assertEquals(Camelot.compatibility(nil, "A minor"), 0.5);
        this.assertEquals(Camelot.compatibility("A minor", nil), 0.5);
    }

    test_sethares_empty_input_is_zero {
        this.assertEquals(Sethares.dissmeasure([], [], [440], [1]), 0);
    }

    test_sethares_unison_below_tritone {
        var unison = Sethares.dissmeasure([440], [1], [440], [1]);
        var tritone = Sethares.dissmeasure([440], [1], [440 * sqrt(2)], [1]);
        this.assert(tritone > unison, "tritone should be rougher than unison");
    }

    test_sethares_octave_below_minor_second {
        var oct = Sethares.dissmeasure([440], [1], [880], [1]);
        var m2 = Sethares.dissmeasure([440], [1], [466], [1]);
        this.assert(m2 > oct, "minor 2nd should be rougher than octave");
    }

    test_sethares_compat_neutral_when_partials_missing {
        this.assertEquals(
            Sethares.spectralCompatibility(nil, nil, [440], [1]),
            0.5
        );
    }

    test_sethares_compat_clamps_to_range {
        var heavy = Sethares.spectralCompatibility(
            [220, 440, 880], [1, 1, 1],
            [225, 445, 885], [1, 1, 1],
            0.1
        );
        this.assert(heavy >= 0, "lower clamp");
        this.assert(heavy <= 1, "upper clamp");
    }

    test_harmonic_mix_prefers_same_key_same_bpm {
        var same = HarmonicMix.score("A minor", "A minor", 124, 124);
        var clash = HarmonicMix.score("A minor", "Eb major", 124, 132);
        this.assert(same[\total] > clash[\total],
            "same key + tempo should beat distant key + tempo");
    }

    test_harmonic_mix_surfaces_camelot_labels {
        var s = HarmonicMix.score("A minor", "E minor", 120, 120);
        this.assertEquals(s[\camelotFrom], "8A");
        this.assertEquals(s[\camelotTo], "9A");
    }

    test_harmonic_mix_recommend_sorts_by_total {
        var ranked = HarmonicMix.recommend(
            "A minor", 124, nil,
            [
                (key: "Eb major", bpm: 132),
                (key: "A minor",  bpm: 124),
                (key: "E minor",  bpm: 122)
            ],
            3
        );
        this.assertEquals(ranked[0][\candidate][\key], "A minor",
            "perfect match goes first");
        this.assert(
            ranked[1][\score][\total] >= ranked[2][\score][\total],
            "results are sorted descending"
        );
    }
}
