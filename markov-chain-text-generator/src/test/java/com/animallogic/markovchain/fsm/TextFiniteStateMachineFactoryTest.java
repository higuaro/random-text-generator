package com.animallogic.markovchain.fsm;

import com.animallogic.markovchain.corpus.stream.CorpusStream;
import com.animallogic.markovchain.corpus.stream.InMemoryCorpusStream;
import com.animallogic.markovchain.fsm.types.PrefixSize;
import com.animallogic.markovchain.fsm.types.TextFiniteStateMachineError;
import com.animallogic.markovchain.junit5.extensions.mockito.MockitoExtension;
import io.atlassian.fugue.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TextFiniteStateMachineFactoryTest {
    private TextFiniteStateMachineFactory textFiniteStateMachineFactory;

    @BeforeEach
    void setUp() {
        textFiniteStateMachineFactory = new TextFiniteStateMachineFactory();
    }

    @Test
    @DisplayName("Should create an empty state machine from an empty corpus")
    public void emptyFsmFromEmptyCorpus() {
        CorpusStream mockCorpusStream = mock(CorpusStream.class);
        when(mockCorpusStream.nextWord()).thenReturn(Optional.empty());
        when(mockCorpusStream.consumed()).thenReturn(true);

        Either<TextFiniteStateMachineError, TextFiniteStateMachine> result =
                textFiniteStateMachineFactory.createTextFiniteStateMachine(mockCorpusStream, PrefixSize.of(2));

        assertThat(result.isRight(), is(true));
        assertEquals(result.right().get().statesCount(), 0);
    }

    @Test
    @DisplayName("Should create an state machine with 2 states")
    public void fsmWithTwoStates() {
        // The resulting states are:
        //   (this, is, a) -> test
        //   (is, a, test) -> EOF
        CorpusStream corpusStream = createCorpusStreamWithWords("this", "is", "a", "test");

        Either<TextFiniteStateMachineError, TextFiniteStateMachine> result =
                textFiniteStateMachineFactory.createTextFiniteStateMachine(corpusStream, PrefixSize.of(3));

        assertThat(result.isRight(), is(true));
        assertEquals(result.right().get().statesCount(), 2);
    }

    @Test
    @DisplayName("Should create an state machine with 3 states")
    public void fsmWithThreeStates() {
        // The resulting states are:
        //   (this, is) -> a
        //   (is, a) -> test
        //   (a, test) -> EOF
        CorpusStream corpusStream = createCorpusStreamWithWords("this", "is", "a", "test");

        Either<TextFiniteStateMachineError, TextFiniteStateMachine> result =
                textFiniteStateMachineFactory.createTextFiniteStateMachine(corpusStream, PrefixSize.of(2));

        assertThat(result.isRight(), is(true));
        assertEquals(result.right().get().statesCount(), 3);
    }

    @Test
    @DisplayName("Should create an state machine with several states")
    public void fsmWithSeveralStates() {
        // The resulting states:               After compressing them:
        // 1   (A, is) -> father                (A, is) -> father
        // 2  (is, father) -> of                (is, father) -> [of, of]
        // 3  (father, of) -> B.                (father, of) -> [B., C.]
        // 4  (of, B.) -> B                     (of, B.) -> B
        // 5  (B., B) -> is                     (B., B) -> is
        // 6  (B, is) -> father                 (B, is) -> father
        // 7  (is, father) -> of
        // 8  (father, of) -> C.
        // 9  (of, C.) -> EOF                   (of, C.) -> EOF
        CorpusStream corpusStream = createCorpusStreamWithWords("A", "is", "father", "of", "B.",
                                                                "B", "is", "father", "of", "C.");

        int expectedStates = 9;  // See comment above
        Either<TextFiniteStateMachineError, TextFiniteStateMachine> result =
                textFiniteStateMachineFactory.createTextFiniteStateMachine(corpusStream, PrefixSize.of(2));

        assertThat(result.isRight(), is(true));
        assertEquals(expectedStates, result.right().get().statesCount());
    }

    private CorpusStream createCorpusStreamWithWords(String firstWord, String... otherWords) {
        return new InMemoryCorpusStream(Stream.concat(Stream.of(firstWord), Stream.of(otherWords)).collect(Collectors.toList()));
    }
}