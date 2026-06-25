// CamelotMix quickstart
// Rank candidate tracks by key, tempo, and optional spectral roughness.
(
~library = [
    (title: "Night Drive", key: "8A", bpm: 124),
    (title: "Solar Lift", key: "9A", bpm: 126),
    (title: "Wrong Door", key: "3B", bpm: 118)
];

HarmonicMix.recommend("8A", 124, nil, ~library, 3).do { |item|
    "% -> %".format(item[\candidate][\title], item[\score][\total]).postln;
};
)
