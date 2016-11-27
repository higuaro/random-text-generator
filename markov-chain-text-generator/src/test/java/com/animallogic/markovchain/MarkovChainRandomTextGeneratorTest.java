package com.animallogic.markovchain;

import com.animallogic.markovchain.corpus.stream.CorpusStream;
import com.animallogic.markovchain.corpus.stream.InMemoryCorpusStream;
import com.animallogic.markovchain.fsm.TextFiniteStateMachine;
import com.animallogic.markovchain.fsm.TextFiniteStateMachineFactory;
import com.animallogic.markovchain.fsm.types.PrefixSize;
import com.animallogic.markovchain.fsm.types.TextFiniteStateMachineError;
import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Random;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MarkovChainRandomTextGeneratorTest {
    @Test
    @Tag("integration")
    @DisplayName("Should generate several 'random' prefixes")
    void generateSeveralRandomPrefixes() {
        // Prepare Corpus
        CorpusStream corpusStream = new InMemoryCorpusStream(
                ImmutableList.of(
                        "This", "is", "a", "simple", "test.\n",
                        "With", "no", "duplicated", "words."
                )
        );

        // Create the Finite State Machine for the algorithm to work with by using the provided factory
        //
        // Using above's corpus the State Machine should be:
        // ---------------------------------------------
        // Prefix index  |   (Prefix) -> [Suffices]
        // --------------+------------------------------
        //      0        |    (This, is) -> a
        //      1        |    (is, a) -> simple
        //      2        |    (a, simple) -> test.\n
        //      3        |    (simple, test\n.) -> With
        //      4        |    (test\.n., With) -> no
        //      5        |    (With, no) -> duplicated
        //      6        |    (no, duplicated) -> words.
        //      7        |    (duplicated, words.) -> EOF
        Either<TextFiniteStateMachineError, TextFiniteStateMachine> result =
                new TextFiniteStateMachineFactory().createTextFiniteStateMachine(corpusStream, PrefixSize.of(2));
        assertThat(result.isRight(), is(true));
        TextFiniteStateMachine textFiniteStateMachine = result.right().get();


        final int SEED = 0x41878291;
        Random rng = new Random(SEED);

        MarkovChainRandomTextGenerator markovChainRandomTextGenerator =
                new MarkovChainRandomTextGenerator(textFiniteStateMachine, rng);

        Iterator<String> iterator = markovChainRandomTextGenerator.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is("test.\n With"));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is("duplicated"));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is("words."));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    @Tag("integration")
    @DisplayName("Should generate random text")
    void generateRandomText() {
        ImmutableList<String> words = ImmutableList.of(
                "A", "is", "father", "of", "B.\n",
                "B", "is", "brother", "of", "C.\n",
                "C", "is", "father", "of", "K.\n"
        );

        // Prepare Corpus
        CorpusStream corpusStream = new InMemoryCorpusStream(words);

        Either<TextFiniteStateMachineError, TextFiniteStateMachine> result =
                new TextFiniteStateMachineFactory().createTextFiniteStateMachine(corpusStream, PrefixSize.of(3));
        assertThat(result.isRight(), is(true));
        TextFiniteStateMachine textFiniteStateMachine = result.right().get();

        final int SEED = 0x12454697;
        MarkovChainRandomTextGenerator randomTextGenerator = new MarkovChainRandomTextGenerator(textFiniteStateMachine, new Random(SEED));

        String randomText = randomTextGenerator.stream().map(s -> s.endsWith("\n") ? s : s + " ").collect(Collectors.joining());
        System.out.println("randomText = " + randomText);

        assertNotEquals(words.toString(), randomText);
    }

    @Test
    @Tag("integration")
    @DisplayName("Should generate empty text when prefix size is great than total number of words in text")
    void prefixTooLongForCorpus() {
        ImmutableList<String> words = ImmutableList.of(
                "A", "is", "father", "of", "B.\n",
                "B", "is", "brother", "of", "C.\n",
                "C", "is", "father", "of", "K.\n"
        );

        // Prepare Corpus
        CorpusStream corpusStream = new InMemoryCorpusStream(words);

        Either<TextFiniteStateMachineError, TextFiniteStateMachine> result =
                new TextFiniteStateMachineFactory().createTextFiniteStateMachine(corpusStream, PrefixSize.of(100));
        assertThat(result.isRight(), is(true));
        TextFiniteStateMachine textFiniteStateMachine = result.right().get();

        final int SEED = 0x12454697;
        MarkovChainRandomTextGenerator randomTextGenerator = new MarkovChainRandomTextGenerator(textFiniteStateMachine, new Random(SEED));

        String randomText = randomTextGenerator.stream().map(s -> s.endsWith("\n") ? s : s + " ").collect(Collectors.joining());

        assertThat(randomText, is(""));
    }
}