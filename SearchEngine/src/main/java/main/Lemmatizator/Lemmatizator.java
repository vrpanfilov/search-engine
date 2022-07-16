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
        String[] words = text.split(
                "\\s*(\\s|,|;|\\?|-|–|—|\\[|]|\\{|}|!|\\.|\\(|\\))\\s*");

        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) {
                continue;
            }
            words[i] = words[i].toLowerCase(Locale.ROOT);
            try {
                List<String> infos = morphology.getMorphInfo(words[i]);
                for (String info : infos) {
                    int pos = info.indexOf('|');
                    if (pos < 0) {
                        continue;
                    }
                    String wordType = String.valueOf(info.charAt(pos + 1));
                    if (wordType.matches("[nfoklp]")) {
                        continue;
                    }
                    String lemmaKey = info.substring(0, pos);
                    result.add(lemmaKey);
                }
            } catch (WrongCharaterException e) {
                continue;
            }
        }
        return result;
    }
}
