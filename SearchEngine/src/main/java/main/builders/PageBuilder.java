package main.builders;

import main.application_properties.Props;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PageBuilder implements Runnable {
    public static final String OK = "OK";
    public static final String NOT_FOUND = "\"Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";
    public static final String RUNNING = "Индексация уже запущена";

    private static ExecutorService executor;
    final static int forSitesThreadNumber =
            Props.getInst().getForSitesThreadNumber();
    public static final Map<String /*siteUrl*/, String/*pagePath*/> indexingPages = new HashMap<>();
    private Site site;
    private Page oldPage;
    private Page page;

    public PageBuilder(Site site, String pagePath) {
        this.site = site;
        oldPage = Repos.pageRepo.findBySiteAndPath(site, pagePath).orElse(null);

        Node node = new Node(site, pagePath);
        Document doc = node.processAndRetunPageDoc();
        int id = node.getAddedPageId();
        page = Repos.pageRepo.findById(id).get();
        page.setContent(doc.outerHtml());
        page.setPath(pagePath);
    }

    @Override
    public void run() {
        List<Lemma> lemmaList;
        lemmaList = Repos.lemmaRepo.findAllBySite(site);
        Map<String, Lemma> lemmas = new HashMap<>();
        for (Lemma lemma : lemmaList) {
            lemmas.put(lemma.getLemma(), lemma);
        }

        List<Index> indexList;
        indexList = Repos.indexRepo.findAllBySiteId(site.getId());
        Map<Integer, Index> indexes = new HashMap<>();
        for (Index index : indexList) {
            indexes.put(index.hashCode(), index);
        }

        IndexBuilder indexBuilder = new IndexBuilder(site, page, lemmas, indexes);
        indexBuilder.fillLemmasAndIndexes();

        for (Index index : indexes.values().stream()
                .filter(index -> index.getPage().getId() == oldPage.getId())
                .toList()) {
            Lemma lemma = index.getLemma();
            lemma.setFrequency(lemma.getFrequency() - 1);
            lemma = lemma;
        }

        saveLemmasAndIndexes(lemmas, indexes);

        synchronized (Page.class) {
            Repos.pageRepo.deleteById(oldPage.getId());
        }

        synchronized (indexingPages) {
            indexingPages.remove(oldPage.getSite().getUrl(), oldPage.getPath());
        }
        oldPage = oldPage;
    }

    public void saveLemmasAndIndexes(Map<String, Lemma> lemmas, Map<Integer, Index> indexes) {
        List<Index> pageIndexes = indexes.values().stream()
                .filter(index -> index.getPage().getId() == page.getId())
                .toList();
        List<Lemma> pageLemmas = new ArrayList<>();
        for (Index index : pageIndexes) {
            pageLemmas.add(index.getLemma());
        }

        synchronized (Lemma.class) {
            Repos.lemmaRepo.saveAllAndFlush(pageLemmas);
        }

        synchronized (Index.class) {
            Repos.indexRepo.saveAllAndFlush(pageIndexes);
        }
    }

    public static String indexPage(String stringUrl) {
        if (!SiteBuilder.getIndexingSites().isEmpty()) {
            return RUNNING;
        }
        if (executor == null) {
            synchronized (Executors.class) {
                if (executor == null) {
                    executor = Executors.newFixedThreadPool(forSitesThreadNumber);
                }
            }
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

        synchronized (indexingPages) {
            if (indexingPages.containsKey(site.getUrl()) &&
                    indexingPages.containsValue(path)) {
                return RUNNING;
            }
            indexingPages.put(site.getUrl(), path);
        }

        PageBuilder pageBuilder = new PageBuilder(site, path);
        executor.execute(pageBuilder);

        return OK;
    }
}
