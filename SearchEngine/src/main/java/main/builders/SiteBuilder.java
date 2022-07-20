package main.builders;

import main.apiResponses.StatisticsResponse;
import main.application_properties.Props;
import main.model.Site;
import main.repository.Repos;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class SiteBuilder implements Runnable {
    public static final boolean IS_INDEXING = true;
    private static ExecutorService executor;
    final static int forSitesThreadNumber =
            Props.getInst().getForSitesThreadNumber();
    private static ConcurrentHashMap<String, Site>
            indexingSites = new ConcurrentHashMap<>();
    private Site site;
    private final Set<String> viewedPages;
    private final ConcurrentLinkedQueue<String> lastNodes;
    private CopyOnWriteArraySet<String> forbiddenNodes;


    public static ConcurrentHashMap<String, Site> getIndexingSites() {
        return indexingSites;
    }

    public ConcurrentLinkedQueue<String> getLastNodes() {
        return lastNodes;
    }

    public CopyOnWriteArraySet<String> getForbiddenNodes() {
        return forbiddenNodes;
    }

    public Set<String> getViewedPages() {
        return viewedPages;
    }

    private static boolean stopping = false;

    public static boolean isStopping() {
        return stopping;
    }

    public static void setStopping(boolean stopping) {
        SiteBuilder.stopping = stopping;
    }

    public SiteBuilder(String siteUrl) {
        lastNodes = new ConcurrentLinkedQueue<>();
        forbiddenNodes = new CopyOnWriteArraySet<>();
        viewedPages = new HashSet<>();

        site = Repos.siteRepo.findAllByUrl(siteUrl).stream()
                .filter(site -> site.getType().equals(Site.INDEXING))
                .findFirst().orElse(null);

        Boolean isFirstStagePresent = Props.getInst().getIsFirstStagePresent();
        if (isFirstStagePresent == null || isFirstStagePresent) {
            if (site != null) {
                Repos.siteRepo.delete(site);
            }
            site = new Site();
            site.setName(Props.SiteUrlName.getNameByUrl(siteUrl));
            site.setUrl(siteUrl);
            site.setStatusTime(LocalDateTime.now());
            site.setSiteBuilder(this);
        }
        site.setType(Site.INDEXING);

        synchronized (Site.class) {
            Repos.siteRepo.saveAndFlush(site);
        }
    }

    @Override
    public void run() {
        Long begin = System.currentTimeMillis();
        while (true) {
            PagesOfSiteBuilder.build(site);
            if (isStopping()) {
                break;
            }

            IndexBuilder.build(site);
            if (isStopping()) {
                break;
            }

            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\tСайт " + site.getName() + " построен за " +
                    (System.currentTimeMillis() - begin) / 1000 + " сек");

            Site prevSite = Repos.siteRepo.findByNameAndType(site.getName(), Site.INDEXED)
                    .orElse(null);
            if (prevSite != null) {
                prevSite.setType(Site.REMOVING);
                synchronized (Site.class) {
                    Repos.siteRepo.saveAndFlush(prevSite);
                }
            }

            site.setType(Site.INDEXED);
            synchronized (Site.class) {
                Repos.siteRepo.saveAndFlush(site);
            }

            if (prevSite != null) {
                synchronized (Site.class) {
                    Repos.siteRepo.delete(prevSite);
                }
            }
            break;
        }

        if (isStopping()) {
            Site sit = Repos.siteRepo.findByNameAndType(site.getName(), Site.INDEXING)
                    .orElse(null);
            if (sit != null) {
                synchronized (Site.class) {
                    Repos.siteRepo.delete(sit);
                }
            }
        }

        indexingSites.remove(site.getUrl());
        if (indexingSites.isEmpty()) {
            stopping = false;
        }
    }

    public static void buildSite(String siteUrl) {
        if (executor == null) {
            synchronized (Executors.class) {
                if (executor == null) {
                    executor = Executors.newFixedThreadPool(forSitesThreadNumber);
                }
            }
        }

        SiteBuilder siteBuilder = new SiteBuilder(siteUrl);

        Site processingSite = indexingSites.putIfAbsent(siteUrl, siteBuilder.site);
        if (processingSite != null) {
            return;
        }

        executor.execute(siteBuilder);
    }

    public static boolean buildAllSites() {
        if (!indexingSites.isEmpty()) {
            return IS_INDEXING;
        }
        synchronized (PageBuilder.indexingPages) {
            if (!PageBuilder.indexingPages.isEmpty()) {
                return IS_INDEXING;
            }
        }

        List<Props.SiteUrlName> siteUrlNames = Props.getInst().getSites();
        for (var siteUrlName : siteUrlNames) {
            buildSite(siteUrlName.getUrl());
        }
        return !IS_INDEXING;
    }

    public static boolean stopIndexing() {
        if (indexingSites.isEmpty()) {
            return !IS_INDEXING;
        }

        setStopping(true);

        return IS_INDEXING;
    }

    public static StatisticsResponse getStatistics() {
        return new StatisticsResponse();
    }
}
