package com.animallogic.markovchain.fsm;

import com.animallogic.markovchain.fsm.types.Prefix;
import com.animallogic.markovchain.fsm.types.PrefixSize;
import com.animallogic.markovchain.fsm.types.State;
import com.animallogic.markovchain.fsm.types.Suffix;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TextFiniteStateMachine {
    private Multimap<Prefix, Suffix> finiteStateMachine;
    private PrefixSize fixedPrefixSize;

    TextFiniteStateMachine(PrefixSize fixedPrefixSize) {
        this.fixedPrefixSize = fixedPrefixSize;
        finiteStateMachine = ArrayListMultimap.create();
    }

    boolean addState(State state) {
        if (state.prefix().size().value() != fixedPrefixSize.value()) {
            throw new IllegalArgumentException(
                    String.format("Prefixes for this state machine are fixed to %d, the given prefix has size %d", fixedPrefixSize.value(), state.prefix().size().value())
            );
        }

        State s = Objects.requireNonNull(state);
        return finiteStateMachine.put(s.prefix(), s.suffix());
    }

    public List<Suffix> sufficesFor(Prefix prefix) {
        if (prefix == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(finiteStateMachine.get(prefix));
        }
    }

    public int statesCount() {
        return finiteStateMachine.size();
    }

    public List<Prefix> prefixes() {
        return new ArrayList<>(finiteStateMachine.keySet());
    }

    public PrefixSize getFixedPrefixSize() {
        return fixedPrefixSize;
    }
}
