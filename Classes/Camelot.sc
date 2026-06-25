// Camelot wheel — the Mixed-In-Key shorthand used by working DJs to pick
// harmonically-compatible tracks. Major keys map to *B* slots (8B = C major),
// minor keys to *A* slots (8A = A minor — the relative minor of C major).
// Compatibility rules used here (after Camelot.compatibility):
//
//   exact match (8A → 8A) ............. 1.0
//   ±1 step, same letter (8A → 9A) .... 0.85
//   ±2 steps, same letter ............. 0.55
//   relative pair, same number (8A→8B). 0.8
//   adjacent + mode change (8A → 9B) .. 0.5
//   anything else ..................... 0.25..0.3
//   key unknown on either side ........ 0.5 (neutral, doesn't punish)
//
// The score is a 0..1 number suitable for ranking next-track candidates
// alongside other harmonic metrics (Sethares roughness, BPM proximity).

Camelot {
    classvar <table, <reverseTable;

    *initClass {
        table = IdentityDictionary[
            // Major keys → "<n>B"
            'C major'  -> "8B",
            'G major'  -> "9B",
            'D major'  -> "10B",
            'A major'  -> "11B",
            'E major'  -> "12B",
            'B major'  -> "1B",
            'F# major' -> "2B",  'Gb major' -> "2B",
            'C# major' -> "3B",  'Db major' -> "3B",
            'G# major' -> "4B",  'Ab major' -> "4B",
            'D# major' -> "5B",  'Eb major' -> "5B",
            'A# major' -> "6B",  'Bb major' -> "6B",
            'F major'  -> "7B",
            // Minor keys → "<n>A"
            'A minor'  -> "8A",
            'E minor'  -> "9A",
            'B minor'  -> "10A",
            'F# minor' -> "11A", 'Gb minor' -> "11A",
            'C# minor' -> "12A", 'Db minor' -> "12A",
            'G# minor' -> "1A",  'Ab minor' -> "1A",
            'D# minor' -> "2A",  'Eb minor' -> "2A",
            'A# minor' -> "3A",  'Bb minor' -> "3A",
            'F minor'  -> "4A",
            'C minor'  -> "5A",
            'G minor'  -> "6A",
            'D minor'  -> "7A"
        ];

        reverseTable = IdentityDictionary.new;
        table.keysValuesDo({ |key, label|
            reverseTable[label.asSymbol] = key;
        });
    }

    // Convert a key string ("A minor", "Bb", "F# major") to its Camelot label,
    // or nil if unknown. A bare letter ("A") falls back to its minor form
    // since our analysis pipeline produces chroma-only keys without an
    // explicit mode.
    *fromKey { |key|
        var direct, trimmed, normalised;
        if (key.isNil) { ^nil };
        trimmed = key.asString.stripWhiteSpace;
        if (trimmed.size == 0) { ^nil };
        direct = table[trimmed.asSymbol];
        if (direct.notNil) { ^direct };
        normalised = this.prNormaliseKey(trimmed);
        ^table[normalised.asSymbol];
    }

    *toKey { |camelotLabel|
        ^reverseTable[camelotLabel.asSymbol];
    }

    *prNormaliseKey { |trimmed|
        // Bare letter ("A", "C#") → assume minor.
        var minor = trimmed ++ " minor";
        if (table[minor.asSymbol].notNil) { ^minor };
        ^trimmed
    }

    *parts { |camelotLabel|
        var match = camelotLabel.asString.findRegexp("^(\\d{1,2})([AB])$");
        var n, letter;
        if (match.isEmpty) { ^nil };
        n = match[1][1].asInteger;
        letter = match[2][1].asSymbol;
        if ((n < 1) || (n > 12)) { ^nil };
        ^(number: n, letter: letter)
    }

    *prWheelDistance { |a, b|
        var raw = absdif(a, b);
        ^min(raw, 12 - raw)
    }

    // Compatibility between two key labels — returns 0..1.
    // Neutral 0.5 when either key is missing/unknown so absent metadata
    // doesn't crowd out tracks that do have keys.
    *compatibility { |fromKey, toKey|
        var fromCamelot = this.fromKey(fromKey);
        var toCamelot = this.fromKey(toKey);
        var fromParts, toParts, distance;
        if (fromCamelot.isNil || toCamelot.isNil) { ^0.5 };
        if (fromCamelot == toCamelot) { ^1.0 };
        fromParts = this.parts(fromCamelot);
        toParts = this.parts(toCamelot);
        if (fromParts.isNil || toParts.isNil) { ^0.5 };
        distance = this.prWheelDistance(fromParts[\number], toParts[\number]);
        if (fromParts[\letter] == toParts[\letter]) {
            if (distance == 0) { ^1.0 };
            if (distance == 1) { ^0.85 };
            if (distance == 2) { ^0.55 };
            ^0.3
        };
        // Different mode (A vs B).
        if (distance == 0) { ^0.8 };   // relative major / minor pair
        if (distance == 1) { ^0.5 };
        ^0.25
    }
}
