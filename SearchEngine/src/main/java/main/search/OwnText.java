package main.search;

import lombok.Data;

import java.util.*;

@Data
public class OwnText {
    public static final int SEPARATORS_BEFORE_COUNT = 5;
    public static final int SEPARATORS_AFTER_COUNT = 5;
    public static final String BOLD_BEGIN = "<b>";
    public static final String BOLD_END = "</b>";
    public static final String SPACE = " ";
    public static final String ELLIPSIS = "...";

    private String text;
    private List<Integer> keyWordIndices = new ArrayList<>();

    int startIndex;
    private Separator separator;
    private StringBuilder builder;

    public OwnText(String text) {
        this.text = text;
    }

    public boolean containsKeyWords() {
        return keyWordIndices.size() > 0;
    }

    private enum SeparatorType {
        SEPARATOR, ELEMENT_BEGIN, ELEMENT_END, SENTENCE_BEGIN, SENTENCE_END, BOLD_BEGIN, BOLD_END
    }

    private static final Set<Character> sentanceEnds =
            new HashSet<>(Arrays.asList('.', '!', '?'));
    private static final Set<Character> separators =
            new HashSet<>(Arrays.asList(' ', ',', ';', '-', '–', '—'));
    private static final Set<Character> allSeparators = new HashSet<>(
            Arrays.asList('.', '!', '?', ' ', ',', ';', '-', '–', '—'));

    private class Separator {
        SeparatorType type;
        int position;

        public Separator(int position) {
            this.position = position;
            allSeparators.addAll(separators);
        }
    }

    public void formCompositionOfFragments(StringBuilder builder) {
        this.builder = builder;
        startIndex = 0;
        while (startIndex < text.length()) {
            addNextFragment();
        }
    }

    public void addNextFragment() {
        startIndex = text.indexOf(BOLD_BEGIN, startIndex);
        if (startIndex < 0) {
            startIndex = text.length();
            return;
        }
        addLeftPart();
        addHighlightedWord();
        addRightPart();
        startIndex = separator.position;
    }

    private void addHighlightedWord() {
        int pos = text.indexOf(BOLD_END, startIndex);
        builder.append(text.substring(startIndex, pos + BOLD_END.length()));
        startIndex += pos + BOLD_END.length();
    }

    private void addLeftPart() {
        loop:
        for (int i = 0; i < SEPARATORS_BEFORE_COUNT; i++) {
            stepOneWordLeft();
            switch (separator.type) {
                case ELEMENT_BEGIN, SENTENCE_BEGIN, BOLD_END:
                    break loop;
            }
        }
        switch (separator.type) {
            case ELEMENT_BEGIN, SENTENCE_BEGIN:
                if (!builder.isEmpty()) {
                    builder.indexOf(SPACE);
                }
                break;
            case SEPARATOR:
                if (builder.lastIndexOf(ELLIPSIS) == builder.length() - ELLIPSIS.length()) {
                    builder.append(SPACE);
                } else {
                    builder.append(SPACE + ELLIPSIS);
                }
            case BOLD_END:
                break;
        }
        builder.append(text.substring(separator.position, startIndex));
    }

    private void addRightPart() {
        loop:
        for (int i = 0; i < SEPARATORS_AFTER_COUNT; i++) {
            stepOneWordRight();
            switch (separator.type) {
                case SENTENCE_END, ELEMENT_END, BOLD_BEGIN:
                    break loop;
            }
        }
        builder.append(text.substring(startIndex, separator.position));
        if (separator.type == SeparatorType.SEPARATOR) {
            builder.append(ELLIPSIS);
        }
        startIndex = separator.position;
    }

    private void stepOneWordLeft() {
        for (int index = separator.position - 1; ; index--) {
            if (index < 0) {
                findWordRight();
                separator.type = SeparatorType.ELEMENT_BEGIN;
                return;
            }
            if (sentanceEnds.contains(text.charAt(index))) {
                findWordRight();
                separator.type = SeparatorType.SENTENCE_BEGIN;
                return;
            }
            if (allSeparators.contains(text.charAt(separator.position))) {
                findWordLeft();
                if (separator.position < 0) {
                    continue;
                }
                separator.position++;
                separator.type = SeparatorType.SEPARATOR;
                return;
            }
        }
    }

    private void findWordLeft() {
        while (allSeparators.contains(text.charAt(separator.position))) {
            --separator.position;
            if (separator.position < 0) {
                break;
            }
        }
    }

    private void findWordRight() {
        while (allSeparators.contains(text.charAt(separator.position))) {
            ++separator.position;
        }
    }

    private void stepOneWordRight() {
        for (int index = separator.position; ; index++) {
            if (index >= text.length()) {
                separator.type = SeparatorType.ELEMENT_END;
                return;
            }
            if (text.charAt(index) == '<') {
                if (text.indexOf(BOLD_BEGIN, index) == index) {
                    separator.position = index;
                    separator.type = SeparatorType.BOLD_BEGIN;
                    return;
                }
            }
            if (sentanceEnds.contains(text.charAt(index))) {
                separator.type = SeparatorType.SENTENCE_END;
                separator.position = index;
                return;
            }
            if (separators.contains(text.charAt(index))) {
                separator.type = SeparatorType.SEPARATOR;
                separator.position = index;
                return;
            }
        }
    }
}
