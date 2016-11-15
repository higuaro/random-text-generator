package com.animallogic.markovchain.fsm.types;

public class PrefixSize {
    int value;
    private PrefixSize(int value) {
        this.value = value;
    }

    public static PrefixSize of(int value) {
        if (value <= 1) {
            throw new IllegalArgumentException(
                    String.format("A prefix size has to be a positive number great than 1 (got %d)", value)
            );
        }

        return new PrefixSize(value);
    }

    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
