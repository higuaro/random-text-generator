package com.animallogic.markovchain.corpus.stream;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InMemoryCorpusStream implements CorpusStream {
    private List<String> words;
    private int currentWordIndex;

    public InMemoryCorpusStream(List<String> words) {
        this.words = words;
        this.currentWordIndex = 0;
    }

    public static InMemoryCorpusStream of(String... words) {
        String[] w = Objects.requireNonNull(words);
        return new InMemoryCorpusStream(ImmutableList.copyOf(w));
    }

    @Override
    public Optional<String> nextWord() {
        if (currentWordIndex < words.size()) {
            int index = currentWordIndex;
            currentWordIndex++;
            return Optional.of(words.get(index));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean consumed() {
        return currentWordIndex >= words.size();
    }

    @Override
    public void close() throws IOException {
        // Honour Closeable contract and forget all previous resources
        words = ImmutableList.of();
    }
}
