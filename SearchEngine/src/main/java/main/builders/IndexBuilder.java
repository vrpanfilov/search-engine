package main.builders;

import main.Lemmatizator.Lemmatizator;
import main.application_properties.Props;
import main.model.*;
import main.repository.Repos;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IndexBuilder {
    private final Site site;
    private final Page page;
    private Map<String, Lemma> lemmas;
    private Map<Integer, Index> indexes;
    private Set<String> lemmasInPage;
    private static List<Field> fields;

    public IndexBuilder(Site site, Page page, Map<String, Lemma> lemmas, Map<Integer, Index> indexes) {
        this.site = site;
        this.page = page;
        this.lemmas = lemmas;
        this.indexes = indexes;
        lemmasInPage = new HashSet<>();
    }

    public static void build(Site site) {
        synchronized (Field.class) {
            fields = Repos.fieldRepo.findAll();
        }
        synchronized (Lemma.class) {
            Repos.lemmaRepo.deleteAllInBatchBySite(site);
        }
        IndexBuilder indexBuilder = new IndexBuilder(site, null, null, null);
        indexBuilder.buildIndex();
        indexBuilder.saveLemmasAndIndexes();
    }

    private void buildIndex() {
        lemmas = new HashMap<>();
        indexes = new HashMap<>();

        List<Page> pages = site.getPages().stream()
                .filter(p1 -> p1.getCode() == Node.OK)
                .sorted((o1, o2) -> o1.getId() - o2.getId()).toList();
        for (Page page : pages) {
            Page pag;
            synchronized (Page.class) {
                pag = Repos.pageRepo.findById(page.getId()).get();
            }
            IndexBuilder indexBuilder = new IndexBuilder(
                    site, pag, lemmas, indexes);
            indexBuilder.fillLemmasAndIndexes();
            pag.setContent(null);
            pag.setPath(null);
        }
    }

    private void fillLemmasAndIndexes() {
        Document doc = Jsoup.parse(page.getContent());
        for (Field field : fields) {
            Element element = doc.getElementsByTag(field.getSelector()).first();
            if (element == null) {
                continue;
            }
            String text = element.text();
            List<String> lemmaNames = Lemmatizator.decomposeTextToLemmas(text);
            for (String lemmaName : lemmaNames) {
                insertIntoLemmasAndIndexes(lemmaName, field.getWeight());
            }
        }
    }

    private void insertIntoLemmasAndIndexes(String lemmaName, float weight) {
        Lemma lemma = lemmas.get(lemmaName);
        if (lemma == null) {
            lemma = new Lemma();
            lemma.setLemma(lemmaName);
            lemma.setFrequency(1);
            lemma.setSite(site);
            lemma.setWeight(weight);
            lemmas.put(lemmaName, lemma);

            Index index = new Index(page, lemma, weight);
            index.setRank(weight);
            indexes.put(index.hashCode(), index);

            lemmasInPage.add(lemmaName);
            return;
        }
        if (lemmasInPage.contains(lemmaName)) {
            Index index = new Index(page, lemma, 0);
            index = indexes.get(index.hashCode());
            index.setRank(index.getRank() + weight);
            index = index;
        } else {
            lemmasInPage.add(lemmaName);
            lemma.setFrequency(lemma.getFrequency() + 1);
            Index index = new Index(page, lemma, weight);
            indexes.put(index.hashCode(), index);
            index = index;
        }
    }

    private void saveLemmasAndIndexes() {
        System.out.println("\t\t\t\t\t\tСайт " + site.getName() + ": cохраняем леммы");
        var lemmaCollection = lemmas.values();
        synchronized (Lemma.class) {
            Repos.lemmaRepo.saveAllAndFlush(lemmaCollection);
        }

        System.out.println("\t\t\t\t\t\tСайт " + site.getName() + ": cохраняем индексы");
        int ind = 1;
        for (Page page : site.getPages()) {
            List<Index> pageIndexes = indexes.values().stream()
                    .filter(index -> index.getPage().getId() == page.getId()
                            && index.getPage().getCode() == Node.OK)
                    .toList();
            synchronized (Index.class) {
                Repos.indexRepo.saveAllAndFlush(pageIndexes);
            }
            ind++;
            if (ind % 100 == 0) {
                System.out.println("\t\t\t\t\t\tСайт " + site.getName() + ": сохранено " + ind + " страниц");
            }
        }
        System.out.println("\t\t\t\t\t\tСайт " + site.getName() + ": " +
                " все " + site.getPages().size() + " страницы сохранены");
    }
}
