package main.builders;

import main.application_properties.Props;
import main.model.Page;
import main.model.Site;
import main.repository.Repos;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class SiteBuilder implements Runnable {
    private static ExecutorService executor;
    final static int forSitesThreadNumber =
            Props.getInst().getForSitesThreadNumber();
    private static ConcurrentHashMap<String, Site>
            indexingSites = new ConcurrentHashMap<>();

    private Site site;
    private final Set<String> viewedPages;
    private final ConcurrentLinkedQueue<String> lastNodes;
    private CopyOnWriteArraySet<String> forbiddenNodes;

    public ConcurrentLinkedQueue<String> getLastNodes() {
        return lastNodes;
    }

    public CopyOnWriteArraySet<String> getForbiddenNodes() {
        return forbiddenNodes;
    }

    public Set<String> getViewedPages() {
        return viewedPages;
    }

    public SiteBuilder(String siteUrl) {
        lastNodes = new ConcurrentLinkedQueue<>();
        forbiddenNodes = new CopyOnWriteArraySet<>();
        viewedPages = new HashSet<>();

//        site = Repos.siteRepo.findByUrl(siteUrl).orElse(null);
        synchronized (Site.class) {
            site = Repos.siteRepo.findAllByUrl(siteUrl).stream()
                    .filter(site -> site.getType().equals(Site.INDEXING))
                    .findFirst().orElse(null);
        }

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

        PagesOfSiteBuilder.build(site);

        IndexBuilder.build(site);

        indexingSites.remove(site.getUrl());

        System.out.println("\t\t\t\t\t\t\t\t\t\t\t\tСайт " + site.getName() + " построен за " +
                (System.currentTimeMillis() - begin) / 1000 + " сек");

        Site prevSite = Repos.siteRepo.findByNameAndType(site.getName(), Site.INDEXED)
                .orElse(null);
        if (prevSite != null) {
            prevSite.setType(Site.REMOVING);
            Repos.siteRepo.saveAndFlush(prevSite);
        }

        site.setType(Site.INDEXED);
        Repos.siteRepo.saveAndFlush(site);

        if (prevSite != null) {
            Repos.siteRepo.delete(prevSite);
        }
    }


    public static boolean buildSite(String siteUrl) {
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
            return false;
        }

        executor.execute(siteBuilder);

        return true;
    }
}
