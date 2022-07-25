package main.Lemmatizator;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Lemmatizator {
    private final static LuceneMorphology morphology;

    static {
        try {
            morphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> decomposeTextToLemmas(String text) {
        List<String> result = new ArrayList<>();
        LemmaAndType lemmaAndType = new LemmaAndType();
        LemmaAndType lemmaAndTypePrev = new LemmaAndType();
        String[] words = text.split(
                "\\s*(\\s|,|;|\\?|-|–|—|\\[|]|\\{|}|!|\\.|\\(|\\))\\s*");

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            word = word.toLowerCase(Locale.ROOT);
            word = word.replaceAll("ё", "е");
            try {
                List<String> infos = morphology.getMorphInfo(word);
                lemmaAndTypePrev.lemma = "";
                for (String info : infos) {
                    infoToLemmaAndType(info, lemmaAndType);
                    if (lemmaAndType.type.isEmpty()) {
                        continue;
                    }
                    if (!lemmaAndTypePrev.lemma.isEmpty()) {
                        if (lemmaAndType.type.matches("[AGKC]")) {
                            if (lemmaAndTypePrev.type.matches("[AGKC]")) {
                                if (lemmaAndType.lemma.length() <= lemmaAndTypePrev.lemma.length()) {
                                    continue;
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                    lemmaAndTypePrev.lemma = lemmaAndType.lemma;
                    lemmaAndTypePrev.type = lemmaAndType.type;
                }
                result.add(lemmaAndTypePrev.lemma);
            } catch (WrongCharaterException e) {
                continue;
            }
        }
        return result;
    }

    private static void infoToLemmaAndType(String info, LemmaAndType lemmaAndType) {
        int pos = info.indexOf('|');
        if (pos < 0) {
            lemmaAndType.type = "";
            return;
        }
        String wordType = String.valueOf(info.charAt(pos + 1));
        if (wordType.matches("[nfoklp]")) {
            lemmaAndType.type = "";
            return;
        }
        lemmaAndType.lemma = info.substring(0, pos);
        lemmaAndType.type = wordType;
    }

    static class LemmaAndType {
        String lemma;
        String type;
    }
}
