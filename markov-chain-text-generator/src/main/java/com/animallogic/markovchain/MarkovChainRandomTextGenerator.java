package com.animallogic.markovchain;

import com.animallogic.markovchain.fsm.TextFiniteStateMachine;
import com.animallogic.markovchain.fsm.types.Prefix;
import com.animallogic.markovchain.fsm.types.Suffix;
import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MarkovChainRandomTextGenerator implements Iterable<String> {
    private final TextFiniteStateMachine textFiniteStateMachine;
    private final Random rng;

    public MarkovChainRandomTextGenerator(TextFiniteStateMachine textFiniteStateMachine, Random rng) {
        this.textFiniteStateMachine = Objects.requireNonNull(textFiniteStateMachine);
        this.rng = Objects.requireNonNull(rng);
    }

    public MarkovChainRandomTextGenerator(TextFiniteStateMachine textFiniteStateMachine) {
        this(textFiniteStateMachine, new Random());
    }

    @Override
    public Iterator<String> iterator() {
        return new RandomTextIterator(textFiniteStateMachine, rng);
    }

    public Stream<String> stream() {
        Spliterator<String> splitIterator = Spliterators.spliteratorUnknownSize(iterator(), 0);
        return StreamSupport.stream(splitIterator, false);
    }

    class RandomTextIterator implements Iterator<String> {
        private TextFiniteStateMachine textFiniteStateMachine;
        private List<Prefix> prefixes;

        private Random rng;

        private Prefix currentPrefix;

        RandomTextIterator(TextFiniteStateMachine textFiniteStateMachine, Random rng) {
            this.textFiniteStateMachine = textFiniteStateMachine;
            this.rng = rng;

            prefixes = textFiniteStateMachine.prefixes();

            int randomStartIndex = rng.nextInt(prefixes.size());
            currentPrefix = prefixes.get(randomStartIndex);
        }

        @Override
        public boolean hasNext() {
            return !textFiniteStateMachine.sufficesFor(currentPrefix).isEmpty();
        }

        @Override
        public String next() {
            List<Suffix> suffices = textFiniteStateMachine.sufficesFor(currentPrefix);
            if (suffices.isEmpty()) {
                throw new NoSuchElementException();
            }

            Suffix randomSuffix = suffices.get(rng.nextInt(suffices.size()));

            Prefix previousPrefix = currentPrefix;
            currentPrefix = rotatePrefixToLeft(currentPrefix, randomSuffix);

            List<String> words = previousPrefix.words();
            return words.subList(0, words.size() - 1).stream().map(s -> s.endsWith("\n") ? s : s + " ").collect(Collectors.joining());
        }

        Prefix rotatePrefixToLeft(Prefix prefix, Suffix suffix) {
            List<String> words = prefix.words();
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            builder.addAll(words.subList(1, words.size()));
            builder.add(suffix.value());
            return Prefix.of(builder.build());
        }
    }
}
