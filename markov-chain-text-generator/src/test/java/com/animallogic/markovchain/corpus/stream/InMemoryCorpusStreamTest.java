package com.animallogic.markovchain.corpus.stream;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryCorpusStreamTest {
    @Test
    @DisplayName("nextWord() should return empty() and consumed=true on an empty corpus")
    public void nextWordOnEmptyCorpus() {
        InMemoryCorpusStream inMemoryCorpusStream = new InMemoryCorpusStream(Collections.emptyList());

        assertAll("consumed empty corpus", () -> {
            assertThat(inMemoryCorpusStream.nextWord().isPresent(), is(false));
            assertThat(inMemoryCorpusStream.consumed(), is(true));
        });
    }

    @Test
    @DisplayName("nextWord() should return each word from the corpus on each invocation")
    public void nextWordShouldReturnAllCorpusWords() {
        ImmutableList<String> words = ImmutableList.of("Hello", "World!", "Animal", "Logic");
        InMemoryCorpusStream inMemoryCorpusStream = new InMemoryCorpusStream(words);

        words.stream().forEachOrdered(word -> {
            Optional<String> maybeNextWord = inMemoryCorpusStream.nextWord();
            assertThat(maybeNextWord.isPresent(), is(true));
            assertEquals(word, maybeNextWord.get());
        });
        assertThat(inMemoryCorpusStream.consumed(), is(true));
    }

    @Test
    @DisplayName("After reading the whole corpus, consumed() should return true and nextWord() should return empty()")
    public void readAllCorpus() {
        ImmutableList<String> words = ImmutableList.of("Hello", "World!", "Animal", "Logic");
        InMemoryCorpusStream inMemoryCorpusStream = new InMemoryCorpusStream(words);

        words.stream().forEachOrdered(__ -> inMemoryCorpusStream.nextWord());

        assertAll("consumed corpus", () -> {
            assertThat(inMemoryCorpusStream.consumed(), is(true));
            assertThat(inMemoryCorpusStream.nextWord().isPresent(), is(false));
        });
    }
}
