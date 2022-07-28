package main.search;

import main.Lemmatizator.Lemmatizator;
import main.model.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Snippet {
    private Page page;
    private List<String> queryWords;
    private List<OwnText> ownTexts = new ArrayList<>();

    public Snippet(Page page, List<String> queryWords) {
        this.page = page;
        this.queryWords = queryWords;
    }

    public String formSnippet() {
        Document doc = Jsoup.parse(page.getContent());
        Element element = doc.getElementsByTag("body").first();
        createOwnTexts(element);
        StringBuilder builder = new StringBuilder();
        for (OwnText ownText : ownTexts) {
            defineKeyWordIndices(ownText);
            insertBoldTags(ownText);
            formCompositionOfFragments(ownText, builder);
        }
        return builder.toString();
    }

    private void defineKeyWordIndices(OwnText ownText) {
        String text = ownText.getText();
        String[] words = text.split(Lemmatizator.WORD_SEPARATORS);
        int startIndex = 0;
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            String w = Lemmatizator.processOneWord(word);
            if (w.isEmpty()) {
                continue;
            }
            if (queryWords.contains(w)) {
                int index = ownText.getText().indexOf(word, startIndex);
                ownText.getKeyWordIndices().add(index);
                startIndex += word.length();
                ownText.getKeyWordIndices().add(startIndex);
            }
        }
    }

    private void insertBoldTags(OwnText ownText) {
        List<Integer> indices = ownText.keyWordIndices;
        StringBuilder builder = new StringBuilder(ownText.getText());
        for (int ind = indices.size() - 1; ind >= 0; ) {
            builder.insert(indices.get(ind--), "</b>");
            builder.insert(indices.get(ind--), "<b>");
        }
        ownText.setText(builder.toString());
    }

    private void createOwnTexts(Element element) {
        String ownText = element.ownText();
        if (!ownText.isEmpty()) {
            ownTexts.add(new OwnText(ownText));
        }
        for (Element child : element.children()) {
            createOwnTexts(child);
        }
    }

    private void formCompositionOfFragments(OwnText ownText, StringBuilder builder) {
    }
}
