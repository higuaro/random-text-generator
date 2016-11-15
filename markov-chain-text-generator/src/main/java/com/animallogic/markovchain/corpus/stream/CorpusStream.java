package com.animallogic.markovchain.corpus.stream;

import java.io.Closeable;
import java.util.Optional;

public interface CorpusStream extends Closeable {
    Optional<String> nextWord();

    boolean consumed();
}
