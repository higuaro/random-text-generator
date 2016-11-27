package com.animallogic.markovchain.fsm.types;

import java.util.Objects;

public class Suffix {
    private String value;

    public static Suffix EOF = new Suffix("\0");

    private Suffix(String suffix) {
        this.value = suffix;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Suffix suffix = (Suffix) o;

        return Objects.equals(this.value, suffix.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    public static Suffix of(String word) {
        return new Suffix(word);
    }

    public boolean isEof() {
        return value.equals(EOF.value);
    }
}
