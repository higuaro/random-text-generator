package com.animallogic.markovchain.fsm.types;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Prefix {
    private List<String> words;

    private Prefix(List<String> words) {
        this.words = words;
    }

    public static Prefix of(List<String> words) {
        return new Prefix(words);
    }

    public PrefixSize size() {
        return PrefixSize.of(words.size());
    }

    public List<String> words() {
        return words;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Prefix prefix = (Prefix) o;

        return Objects.equals(this.words, prefix.words);
    }

    @Override
    public int hashCode() {
        return words != null ? words.hashCode() : 0;
    }

    @Override
    public String toString() {
        return words.stream().collect(Collectors.joining(" "));
    }
}
