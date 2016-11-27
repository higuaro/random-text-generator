package com.animallogic.server.rest.v1.resources;

import com.animallogic.markovchain.MarkovChainRandomTextGenerator;
import com.animallogic.markovchain.corpus.stream.FileCorpusStream;
import com.animallogic.markovchain.fsm.TextFiniteStateMachine;
import com.animallogic.markovchain.fsm.TextFiniteStateMachineFactory;
import com.animallogic.markovchain.fsm.types.PrefixSize;
import com.animallogic.markovchain.fsm.types.TextFiniteStateMachineError;
import io.atlassian.fugue.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
class RandomTextGeneratorResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomTextGeneratorResource.class);

    private final TextFiniteStateMachineFactory textFiniteStateMachineFactory;

    RandomTextGeneratorResource(TextFiniteStateMachineFactory textFiniteStateMachineFactory) {
        this.textFiniteStateMachineFactory = textFiniteStateMachineFactory;
    }

    @RequestMapping(
            value = "api/v1/random/generate-from-file",
            method = RequestMethod.POST
    )
    @ResponseBody
    public DeferredResult<ResponseEntity<?>> uploadTextFile(@RequestParam(value = "prefix-size") int prefixSize,
                                                            @RequestParam(value = "extra-spaces-as-words", required = false, defaultValue = "false") boolean treatExtraSpacesAsWords,
                                                            @RequestParam("text-file") MultipartFile file) {
        if (!file.getContentType().startsWith(MediaType.TEXT_PLAIN_VALUE)) {
            LOGGER.warn("Uploaded file type unsupported: '{}'", file.getContentType());
            return badRequest();
        }

        FileCorpusStream.ExtraSpacesHandling extraSpacesHandlingOption;
        if (treatExtraSpacesAsWords) {
            extraSpacesHandlingOption = FileCorpusStream.ExtraSpacesHandling.MORE_THAN_TWO_SPACES_IS_A_WORD;
        } else {
            extraSpacesHandlingOption = FileCorpusStream.ExtraSpacesHandling.IGNORE_EXTRA_SPACES;
        }

        try (FileCorpusStream fileCorpusStream = new FileCorpusStream(file.getInputStream(), extraSpacesHandlingOption)) {

            Either<TextFiniteStateMachineError, TextFiniteStateMachine> errorOrStateMachine =
                    textFiniteStateMachineFactory.createTextFiniteStateMachine(fileCorpusStream, PrefixSize.of(prefixSize));

            if (errorOrStateMachine.isLeft()) {
                TextFiniteStateMachineError error = errorOrStateMachine.left().get();
                LOGGER.error("An error occurred while trying to create the Finite State Machine, the error message is: '{}'", error.errorMessage(), error.throwable().orElse(null));
                return internalServerError();
            }

            TextFiniteStateMachine finiteStateMachine = errorOrStateMachine.right().get();

            LOGGER.debug("Generating random text promise");

            return toDeferredResult(asyncGenerateRandomText(finiteStateMachine).thenApply(ResponseEntity::ok));
        } catch (Exception e) {
            return badRequest();
        }
    }

    private DeferredResult<ResponseEntity<?>> badRequest() {
        return toDeferredResult(CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
    }

    private DeferredResult<ResponseEntity<?>> internalServerError() {
        return toDeferredResult(CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)));
    }

    private <T> DeferredResult<T> toDeferredResult(CompletableFuture<T> completableFuture) {
        DeferredResult<T> deferredResult = new DeferredResult<>();
        completableFuture.thenAccept(deferredResult::setResult);
        return deferredResult;
    }

    private CompletableFuture<String> asyncGenerateRandomText(TextFiniteStateMachine finiteStateMachine) {
        return CompletableFuture.supplyAsync(
                () -> new MarkovChainRandomTextGenerator(finiteStateMachine)
                        .stream()
                        .map(s -> s.endsWith("\n") ? s : s + " ")
                        .collect(Collectors.joining())
        );
    }
}
