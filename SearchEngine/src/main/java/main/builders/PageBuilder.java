package main.builders;

import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.repository.Repos;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageBuilder implements Runnable {
    public static final String OK = "OK";
    public static final String NOT_FOUND = "\"Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";
    public static final String RUNNING = "Индексация уже запущена";

    private Site site;
    private Page oldPage;
    private Page page = null;

    public PageBuilder(Site site, String pagePath) {
        this.site = site;
        oldPage = Repos.pageRepo.findBySiteAndPath(site, pagePath).orElse(null);

        Node node = new Node(site, pagePath);
        node.setFromPageBuilder(true);
        Document doc = node.processAndRetunPageDoc();
        if (doc == null) {
            return;
        }
        int id = node.getAddedPageId();
        page = Repos.pageRepo.findById(id).get();
        if (page == null) {
            return;
        }
        page.setContent(doc.outerHtml());
        page.setPath(pagePath);

    }

    @Override
    public void run() {
        List<Lemma> lemmaList = Repos.lemmaRepo.findAllBySite(site);
        Map<String, Lemma> lemmas = new HashMap<>();
        for (Lemma lemma : lemmaList) {
            lemmas.put(lemma.getLemma(), lemma);
        }

        List<Index> indexList = Repos.indexRepo.findAllBySiteId(site.getId());
        Map<Integer, Index> indexes = new HashMap<>();
        for (Index index : indexList) {
            indexes.put(index.hashCode(), index);
        }

        IndexBuilder indexBuilder = new IndexBuilder(site, page, lemmas, indexes);
        indexBuilder.fillLemmasAndIndexes();

        List<Lemma> lemmasToDelete = new ArrayList<>();
        if (oldPage != null) {

            var a = indexes.values().stream()
                    .filter(index -> index.getPage().getId() == oldPage.getId())
                    .toList();

            for (Index index : indexes.values().stream()
                    .filter(index -> index.getPage().getId() == oldPage.getId())
                    .toList()) {
                Lemma lemma = index.getLemma();
                lemma.setFrequency(lemma.getFrequency() - 1);
                if (lemma.getFrequency() == 0) {
                    lemmas.remove(lemma.getLemma());
                    lemmasToDelete.add(lemma);
                }
            }
        }

        synchronized (Lemma.class) {
            Repos.lemmaRepo.deleteAllInBatch(lemmasToDelete);
        }

        List<Index> pageIndexes = new ArrayList<>();
        for (Index index : indexes.values().stream()
                .filter(index -> index.getPage().getId() == page.getId())
                .toList()) {
            pageIndexes.add(index);
        }

        synchronized (Page.class) {
            Repos.pageRepo.saveAndFlush(page);
        }
        synchronized (Lemma.class) {
            Repos.lemmaRepo.deleteAllInBatch(lemmasToDelete);
            Repos.lemmaRepo.saveAllAndFlush(lemmaList);
        }
        synchronized (Index.class) {
            Repos.indexRepo.saveAllAndFlush(pageIndexes);
        }
        synchronized (Page.class) {
            if (oldPage != null) {
                Repos.pageRepo.deleteById(oldPage.getId());
            }
        }

        SiteBuilder.getIndexingSites().remove(site.getUrl());
    }

    public static String indexPage(String stringUrl) {
        if (!SiteBuilder.getIndexingSites().isEmpty()) {
            return RUNNING;
        }

        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            return NOT_FOUND;
        }
        String home = url.getProtocol() + "://" + url.getHost();
        String path = url.getFile();

        Site site;
        site = Repos.siteRepo.findByUrl(home).orElse(null);
        if (site == null) {
            return NOT_FOUND;
        }

        if (SiteBuilder.getIndexingSites().contains(site.getUrl())) {
            return RUNNING;
        }

        SiteBuilder.getIndexingSites().put(site.getUrl(), site);

        PageBuilder pageBuilder = new PageBuilder(site, path);
        if (pageBuilder.page == null) {
            return NOT_FOUND;
        }

        pageBuilder.run();

        return OK;
    }
}