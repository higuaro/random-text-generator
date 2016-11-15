package com.animallogic.markovchain.fsm.types;

import io.atlassian.fugue.Either;

import java.util.Objects;
import java.util.Optional;

public class TextFiniteStateMachineError {
    private final String errorMessage;
    private final Throwable throwable;

    public TextFiniteStateMachineError(String errorMessage) {
        this(errorMessage, null);
    }

    public TextFiniteStateMachineError(String errorMessage, Throwable throwable) {
        this.errorMessage = Objects.requireNonNull(errorMessage);
        this.throwable = throwable;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Optional<Throwable> throwable() {
        return Optional.ofNullable(throwable);
    }

    public static <T> Either<TextFiniteStateMachineError, T> asLeft(String errorMessage) {
        return Either.left(new TextFiniteStateMachineError(errorMessage));
    }

    public static <T> Either<TextFiniteStateMachineError, T> asLeft(String errorMessage, Throwable throwable) {
        return Either.left(new TextFiniteStateMachineError(errorMessage, throwable));
    }
}
