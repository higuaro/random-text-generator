package com.animallogic.markovchain.corpus.stream;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

public class FileCorpusStream implements CorpusStream {
    private final BufferedReader bufferedReader;
    private final ExtraSpacesHandling extraSpacesHandlingOption;

    private boolean consumed;
    private Queue<String> currentLineQueue;

    public enum ExtraSpacesHandling {
        IGNORE_EXTRA_SPACES,
        MORE_THAN_TWO_SPACES_IS_A_WORD
    }

    public FileCorpusStream(InputStream inputStream) throws IOException {
        this(new InputStreamBufferedReaderFactory(inputStream), ExtraSpacesHandling.IGNORE_EXTRA_SPACES);
    }

    public FileCorpusStream(InputStream inputStream, ExtraSpacesHandling extraSpacesHandlingOption) throws IOException {
        this(new InputStreamBufferedReaderFactory(inputStream), extraSpacesHandlingOption);
    }

    public FileCorpusStream(Path path) throws IOException {
        this(new DefaultBufferedReaderFactory(path), ExtraSpacesHandling.IGNORE_EXTRA_SPACES);
    }

    public FileCorpusStream(Path path, ExtraSpacesHandling extraSpacesHandlingOption) throws IOException {
        this(new DefaultBufferedReaderFactory(path), extraSpacesHandlingOption);
    }

    @VisibleForTesting
    FileCorpusStream(BufferedReaderFactory factory, ExtraSpacesHandling extraSpacesHandlingOption) throws IOException {
        this.extraSpacesHandlingOption = extraSpacesHandlingOption;
        BufferedReaderFactory bufferedReaderFactory = Objects.requireNonNull(factory);
        bufferedReader = bufferedReaderFactory.createBufferedReader();

        consumed = false;
        currentLineQueue = new ArrayDeque<>();
    }

    @Override
    public Optional<String> nextWord() {
        if (currentLineQueue.isEmpty()) {
            try {
                String line = bufferedReader.readLine();
                if (line == null) {
                    consumed = true;
                    return Optional.empty();
                }

                currentLineQueue.addAll(extractWords(line, extraSpacesHandlingOption));
            } catch (IOException e) {
                return Optional.empty();
            }
        }

        return Optional.of(currentLineQueue.poll());
    }

    private List<String> extractWords(String line, ExtraSpacesHandling extraSpacesHandling) {
        int len = line.length();
        StringBuilder wordBuffer = new StringBuilder(len);
        List<String> words = new ArrayList<>();

        boolean collectingSpacesMode = false;

        switch (extraSpacesHandling) {
            case IGNORE_EXTRA_SPACES:
                for (int i = 0; i < len; i++) {
                    char c = line.charAt(i);

                    if (Character.isWhitespace(c)) {
                        if (wordBuffer.length() > 0) {
                            words.add(wordBuffer.toString());
                            wordBuffer.delete(0, wordBuffer.length());
                        }
                    } else {
                        wordBuffer.append(c);
                    }
                }
                break;

            case MORE_THAN_TWO_SPACES_IS_A_WORD:
                for (int i = 0; i < len; i++) {
                    char c = line.charAt(i);

                    if (Character.isWhitespace(c)) {
                        if (!collectingSpacesMode) {
                            // Flush the word that was in the buffer before
                            if (wordBuffer.length() > 0) {
                                words.add(wordBuffer.toString());
                                wordBuffer.delete(0, wordBuffer.length());
                            }
                        }
                        collectingSpacesMode = true;
                        wordBuffer.append(c);
                    } else {
                        if (collectingSpacesMode) {
                            if (wordBuffer.length() > 2) {
                                words.add(wordBuffer.substring(1, wordBuffer.length() - 1));
                            }
                            wordBuffer.delete(0, wordBuffer.length());
                        }
                        collectingSpacesMode = false;
                        wordBuffer.append(c);
                    }
                }
                break;
        }

        wordBuffer.append('\n');
        words.add(wordBuffer.toString());

        return words;
    }

    @Override
    public boolean consumed() {
        return consumed;
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
        currentLineQueue.clear();
    }

    interface BufferedReaderFactory {
        BufferedReader createBufferedReader() throws IOException;
    }

    private static class DefaultBufferedReaderFactory implements BufferedReaderFactory {
        Path path;

        DefaultBufferedReaderFactory(Path path) {
            this.path = path;
        }
        public BufferedReader createBufferedReader() throws IOException {
            return Files.newBufferedReader(path);
        }

    }

    private static class InputStreamBufferedReaderFactory implements BufferedReaderFactory {
        InputStream inputStream;

        InputStreamBufferedReaderFactory(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        public BufferedReader createBufferedReader() throws  IOException {
            return new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        }
    }
}
