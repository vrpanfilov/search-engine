package main.builders;

import main.Lemmatizator.Lemmatizator;
import main.model.*;
import main.repository.Repos;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class IndexBuilder {
    private final Site site;
    private final Page page;
    private Map<String, Lemma> lemmas;
    private Map<Integer, Index> indices;
    private Set<String> lemmasInPage;

    private static List<Field> fields;

    public static List<Field> getFields() {
        synchronized (Field.class) {
            if (fields == null) {
                fields = Repos.fieldRepo.findAll();
            }
            return fields;
        }
    }

    public IndexBuilder(Site site, Page page, Map<String, Lemma> lemmas, Map<Integer, Index> indices) {
        this.site = site;
        this.page = page;
        this.lemmas = lemmas;
        this.indices = indices;
        lemmasInPage = new HashSet<>();
    }

    public static void build(Site site) {
        synchronized (Lemma.class) {
            Repos.lemmaRepo.deleteAllInBatchBySite(site);
        }
        IndexBuilder indexBuilder = new IndexBuilder(site, null, null, null);
        indexBuilder.buildIndex();
        indexBuilder.saveLemmasAndIndices();
    }

    private void buildIndex() {
        lemmas = new HashMap<>();
        indices = new HashMap<>();

        List<Page> pages = site.getPages().stream()
                .filter(p1 -> p1.getCode() == Node.OK)
                .sorted((o1, o2) -> o1.getId() - o2.getId()).toList();
        for (Page page : pages) {
            if (SiteBuilder.isStopping()) {
                return;
            }
            Page pag;
            pag = Repos.pageRepo.findById(page.getId()).get();
            IndexBuilder indexBuilder = new IndexBuilder(
                    site, pag, lemmas, indices);
            indexBuilder.fillLemmasAndIndices();
            pag.setContent(null);
            pag.setPath(null);
        }
    }

    public void fillLemmasAndIndices() {
        Document doc = Jsoup.parse(page.getContent());
        for (Field field : getFields()) {
            Elements elements = doc.getElementsByTag(field.getSelector());
            for (Element element : elements) {
                String text = element.text();
                List<String> lemmaNames = Lemmatizator.decomposeTextToLemmas(text);
                for (String lemmaName : lemmaNames) {
                    insertIntoLemmasAndIndices(lemmaName, field.getWeight());
                }
            }
        }
    }

    private void insertIntoLemmasAndIndices(String lemmaName, float weight) {
        Lemma lemma = lemmas.get(lemmaName);
        if (lemma == null) {
            lemma = new Lemma();
            lemma.setLemma(lemmaName);
            lemma.setFrequency(1);
            lemma.setSite(site);
            lemma.setWeight(weight);
            lemmas.put(lemmaName, lemma);

            Index index = new Index(page, lemma, weight);
            indices.put(index.hashCode(), index);

            lemmasInPage.add(lemmaName);
            return;
        }
        if (lemmasInPage.contains(lemmaName)) {
            Index auxIndex = new Index(page, lemma, 0);
            Index index = indices.get(auxIndex.hashCode());
            index.setRank(index.getRank() + weight);
        } else {
            lemmasInPage.add(lemmaName);
            lemma.setFrequency(lemma.getFrequency() + 1);
            Index index = new Index(page, lemma, weight);
            indices.put(index.hashCode(), index);
        }
    }

    public static final String TABS = "\t\t\t\t\t\t\t\t\t\t\t\t";

    public void saveLemmasAndIndices() {
        System.out.println(TABS + "Сайт \"" + site.getName() + "\": cохраняем леммы");
        if (SiteBuilder.isStopping()) {
            return;
        }
        var lemmaCollection = lemmas.values();
        synchronized (Lemma.class) {
            Repos.lemmaRepo.saveAllAndFlush(lemmaCollection);
        }

        System.out.println(TABS + "Сайт \"" + site.getName() + "\": cохраняем индексы");
        int ind = 1;
        for (Page page : site.getPages()) {
            if (SiteBuilder.isStopping()) {
                return;
            }
            List<Index> pageIndices = indices.values().stream()
                    .filter(index -> index.getPage().getId() == page.getId()
                            && index.getPage().getCode() == Node.OK)
                    .toList();
            synchronized (Index.class) {
                Repos.indexRepo.saveAllAndFlush(pageIndices);
            }
            ind++;
            if (ind % 100 == 0) {
                System.out.println(TABS + "Сайт " + site.getName() + ": сохранено страниц - " + ind);
            }
        }
        System.out.println(TABS + "Сайт \"" + site.getName() + "\": " +
                "всего сохранено страниц - " + site.getPages().size());
    }
}
