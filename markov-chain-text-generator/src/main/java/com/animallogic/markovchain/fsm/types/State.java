package com.animallogic.markovchain.fsm.types;

public class State {
    private Prefix prefix;
    private Suffix suffix;

    private State(Prefix prefix, Suffix suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public Prefix prefix() {
        return prefix;
    }

    public Suffix suffix() {
        return suffix;
    }

    public static State of(Prefix prefix, Suffix suffix) {
        return new State(prefix, suffix);
    }
}
