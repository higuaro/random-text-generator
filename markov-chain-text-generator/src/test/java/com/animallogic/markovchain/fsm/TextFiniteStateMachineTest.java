package com.animallogic.markovchain.fsm;

import com.animallogic.markovchain.fsm.types.Prefix;
import com.animallogic.markovchain.fsm.types.PrefixSize;
import com.animallogic.markovchain.fsm.types.State;
import com.animallogic.markovchain.fsm.types.Suffix;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.expectThrows;

public class TextFiniteStateMachineTest {
    @Test
    @DisplayName("Trying to create a Finite State Machine with fixed prefix size of 1 should fail")
    void throwExceptionWithPrefixSizeOfOne() {
        //noinspection ThrowableResultOfMethodCallIgnored
        expectThrows(IllegalArgumentException.class, () -> new TextFiniteStateMachine(PrefixSize.of(1)));
    }

    @Test
    @DisplayName("Should throw an exception when the prefix size does not match with the fixed prefix size")
    void throwExceptionWithMismatchingPrefixSize() {
        TextFiniteStateMachine textFiniteStateMachine = new TextFiniteStateMachine(PrefixSize.of(2));

        textFiniteStateMachine.addState(createState(ImmutableList.of("This", "is"), "fine"));
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        Throwable throwable =
                expectThrows(IllegalArgumentException.class, () -> textFiniteStateMachine.addState(createState(ImmutableList.of("This", "is", "not"), "fine")));

        assertEquals("Prefixes for this state machine are fixed to 2, the given prefix has size 3", throwable.getMessage());
        assertEquals(1, textFiniteStateMachine.statesCount());
    }

    @Test
    @DisplayName("Should add one state to the Finite State Machine")
    void addOneState() {
        TextFiniteStateMachine textFiniteStateMachine = new TextFiniteStateMachine(PrefixSize.of(2));

        textFiniteStateMachine.addState(createState(ImmutableList.of("A", "simple"), "test"));

        assertEquals(1, textFiniteStateMachine.statesCount());
    }

    @Test
    @DisplayName("Adding the same state twice should count as two states")
    void addDuplicates() {
        TextFiniteStateMachine textFiniteStateMachine = new TextFiniteStateMachine(PrefixSize.of(2));

        State state = createState(ImmutableList.of("A", "simple"), "test");

        textFiniteStateMachine.addState(state);
        textFiniteStateMachine.addState(state);

        assertEquals(2, textFiniteStateMachine.statesCount());
    }

    @Test
    @DisplayName("Should return all suffixes for a given prefix")
    void returnSuffixes() {
        TextFiniteStateMachine textFiniteStateMachine = new TextFiniteStateMachine(PrefixSize.of(2));

        textFiniteStateMachine.addState(createState(ImmutableList.of("A", "simple"), "test"));
        textFiniteStateMachine.addState(createState(ImmutableList.of("is", "not"), "much"));
        textFiniteStateMachine.addState(createState(ImmutableList.of("A", "simple"), "test"));
        textFiniteStateMachine.addState(createState(ImmutableList.of("is", "not"), "enough"));

        assertEquals(2, textFiniteStateMachine.sufficesFor(Prefix.of(ImmutableList.of("A", "simple"))).size());
        assertEquals(2, textFiniteStateMachine.sufficesFor(Prefix.of(ImmutableList.of("is", "not"))).size());
    }

    @Test
    @DisplayName("Should return a list with all prefixes")
    void returnPrefixes() {
        TextFiniteStateMachine textFiniteStateMachine = new TextFiniteStateMachine(PrefixSize.of(3));

        textFiniteStateMachine.addState(createState(ImmutableList.of("A", "simple", "test"), "is"));
        textFiniteStateMachine.addState(createState(ImmutableList.of("is", "not", "enough"), "But"));
        textFiniteStateMachine.addState(createState(ImmutableList.of("A", "simple", "test"), "could"));
        textFiniteStateMachine.addState(createState(ImmutableList.of("be", "useful", "sometimes"), "right?"));

        assertEquals(3, textFiniteStateMachine.prefixes().size());
    }

    private State createState(List<String> words, String suffixWord) {
        Prefix prefix = Prefix.of(words);
        Suffix suffix = Suffix.of(suffixWord);
        return State.of(prefix, suffix);
    }
}