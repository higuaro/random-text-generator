package com.animallogic.markovchain.corpus.stream;

import com.animallogic.markovchain.corpus.stream.FileCorpusStream.ExtraSpacesHandling;
import com.animallogic.markovchain.junit5.extensions.mockito.MockitoExtension;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileCorpusStreamTest {

    @Test
    @DisplayName("nextWord() should return empty() and consumed=true on an empty corpus")
    public void nextWordOnEmptyCorpus() throws IOException {
        FileCorpusStream fileCorpusStream = createEmptyStream();

        assertAll("consumed empty corpus", () -> {
            assertThat(fileCorpusStream.nextWord().isPresent(), is(false));
            assertThat(fileCorpusStream.consumed(), is(true));
        });
    }

    @Test
    @DisplayName("nextWord() should return each word from the corpus on each invocation")
    public void nextWordShouldReturnAllCorpusWords() throws IOException {
        String line1 = "This is a test line";
        String line2 = "and this is another line.";

        FileCorpusStream fileCorpusStream = createStreamWithLines(line1, line2);

        List<String> words = ImmutableList.of("This", "is", "a", "test", "line\n", "and", "this", "is", "another", "line.\n");
        int totalWords = words.size();

        AtomicInteger counter = new AtomicInteger(0);

        assertWordsFromStream(fileCorpusStream, words, counter);

        assertEquals(totalWords, counter.intValue());

        assertThat(fileCorpusStream.nextWord().isPresent(), is(false));
        assertThat(fileCorpusStream.consumed(), is(true));
    }

    @Test
    @DisplayName("nextWord() should return each word from the corpus on each invocation ignoring extra spaces")
    public void nextWordShouldReturnAllCorpusWordsIgnoringExtraSpaces() throws IOException {
        String line1 = "1    2    3";
        String line2 = "4";
        FileCorpusStream fileCorpusStream = createStreamWithLines(line1, line2);

        List<String> words = ImmutableList.of("1", "2", "3\n", "4\n")
                .stream()
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toList());
        int totalWords = words.size();

        AtomicInteger counter = new AtomicInteger(0);

        assertWordsFromStream(fileCorpusStream, words, counter);

        assertEquals(totalWords, counter.get());

        assertThat(fileCorpusStream.nextWord().isPresent(), is(false));
        assertThat(fileCorpusStream.consumed(), is(true));
    }

    @Test
    @DisplayName("nextWord() should return each word from the corpus on each invocation including only spaces")
    public void nextWordShouldReturnAllCorpusWordsWithSeveralSpaces() throws IOException {
        String line1 = " 1     3    5";
        String line2 = "6  7";

        FileCorpusStream fileCorpusStream =
                createStreamWithSpaceHandlingAndLines(ExtraSpacesHandling.MORE_THAN_TWO_SPACES_IS_A_WORD, line1, line2);

        List<String> words = ImmutableList.of("1", "   ", "3", "  ", "5\n", "6", "7\n");
        int totalWords = words.size();

        AtomicInteger counter = new AtomicInteger(0);

        assertWordsFromStream(fileCorpusStream, words, counter);

        assertEquals(totalWords, counter.get());

        assertThat(fileCorpusStream.nextWord().isPresent(), is(false));
        assertThat(fileCorpusStream.consumed(), is(true));
    }

    private FileCorpusStream createStreamWithLines(String line1, String... line2) throws IOException {
        return createStreamWithSpaceHandlingAndLines(ExtraSpacesHandling.IGNORE_EXTRA_SPACES, line1, line2);
    }

    private FileCorpusStream createEmptyStream() throws IOException {
        return createStreamWithSpaceHandlingAndLines(ExtraSpacesHandling.IGNORE_EXTRA_SPACES, null);
    }

    private FileCorpusStream createStreamWithSpaceHandlingAndLines(FileCorpusStream.ExtraSpacesHandling extraSpacesHandling, String firstLine, String... otherLines) throws IOException {
        BufferedReader mockBufferedReader = mock(BufferedReader.class);

        FileCorpusStream.BufferedReaderFactory mockBufferedReaderFactory = mock(FileCorpusStream.BufferedReaderFactory.class);
        when(mockBufferedReaderFactory.createBufferedReader()).thenReturn(mockBufferedReader);

        List<String> words = Arrays.asList(otherLines);
        // Creating the array with `words.size() + 1` adds the EOF file indicator ("null") to the BufferedReader
        String linesArray[] = new String[words.size() + 1];
        words.toArray(linesArray);

        when(mockBufferedReader.readLine()).thenReturn(firstLine, linesArray);

        return new FileCorpusStream(mockBufferedReaderFactory, extraSpacesHandling);
    }

    private void assertWordsFromStream(FileCorpusStream fileCorpusStream, List<String> words, AtomicInteger counter) {
        words.stream().forEachOrdered(word -> {
            Optional<String> maybeNextWord = fileCorpusStream.nextWord();
            assertThat(maybeNextWord.isPresent(), is(true));
            assertEquals(word, maybeNextWord.get());
            counter.set(counter.intValue() + 1);
        });
    }
}