package com.animallogic.markovchain.fsm;

import com.animallogic.markovchain.corpus.stream.CorpusStream;
import com.animallogic.markovchain.fsm.types.Prefix;
import com.animallogic.markovchain.fsm.types.PrefixSize;
import com.animallogic.markovchain.fsm.types.State;
import com.animallogic.markovchain.fsm.types.Suffix;
import com.animallogic.markovchain.fsm.types.TextFiniteStateMachineError;
import com.google.common.collect.EvictingQueue;
import io.atlassian.fugue.Either;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class TextFiniteStateMachineFactory {
    public Either<TextFiniteStateMachineError, TextFiniteStateMachine> createTextFiniteStateMachine(CorpusStream corpusStream, PrefixSize prefixSize) {
        TextFiniteStateMachine finiteStateMachine = new TextFiniteStateMachine(prefixSize);

        EvictingQueue<String> slidingWindow = EvictingQueue.create(prefixSize.value());

        try (CorpusStream stream = corpusStream) {
            Optional<String> maybeNextWord = stream.nextWord();

            while (maybeNextWord.isPresent()) {
                String nextWord = maybeNextWord.get();

                createStateIfFullWindow(slidingWindow, Suffix.of(nextWord)).ifPresent(finiteStateMachine::addState);

                slidingWindow.add(nextWord);

                maybeNextWord = stream.nextWord();
            }

            if (!stream.consumed()) {
                return TextFiniteStateMachineError.asLeft("Could not read next word from corpus, the stream is not consumed/closed yet the returned word was an Optional.empty");
            }
        } catch (IOException e) {
            return TextFiniteStateMachineError.asLeft("An IO error occurred while closing the corpus stream source, see attached exception for more details", e);
        }

        // Add the final state to the FSM
        createStateIfFullWindow(slidingWindow, Suffix.EOF).ifPresent(finiteStateMachine::addState);

        return Either.right(finiteStateMachine);
    }

    private Optional<State> createStateIfFullWindow(EvictingQueue<String> slidingWindow, Suffix suffix) {
        if (slidingWindow.remainingCapacity() == 0) {
            Prefix prefix = Prefix.of(new ArrayList<>(slidingWindow));
            return Optional.of(State.of(prefix, suffix));
        } else {
            return Optional.empty();
        }
    }
}
