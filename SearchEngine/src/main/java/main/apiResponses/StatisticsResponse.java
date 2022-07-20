package main.apiResponses;

import lombok.Data;
import main.builders.SiteBuilder;
import main.model.Site;
import main.repository.Repos;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Data
public class StatisticsResponse extends Response {
    private Statistics statistics = new Statistics();

    @Data
    static class Statistics {
        private TotalStatistics total;
        private List<DetailedStatistics> detailed;

        public Statistics() {
            total = new TotalStatistics();
            detailed = new ArrayList<>();

            List<Site> sites = Repos.siteRepo.findAll().stream()
                    .filter(site -> site.getType().equals(Site.INDEXED)).toList();
            for (Site site : sites) {
                DetailedStatistics detailedStatistics = new DetailedStatistics(site);
                detailed.add(detailedStatistics);
            }
        }
    }

    @Data
    static class TotalStatistics {
        private int sites;
        private int pages;
        private int lemmas;
        private boolean isIndexing;

        public TotalStatistics() {
            setSites(Repos.siteRepo.findSiteCount());
            setPages(Repos.pageRepo.findPageCount());
            int lemmaCount = Repos.lemmaRepo.findLemmaCount();
            setLemmas(lemmaCount);
            setIndexing(!SiteBuilder.getIndexingSites().isEmpty());
        }
    }

    @Data
    static class DetailedStatistics {
        private String url;
        private String name;
        private String status;
        private long statusTime;
        private String error;
        private int pages;
        private int lemmas;

        public DetailedStatistics(Site site) {
            url = site.getUrl();
            name = site.getName();
            status = site.getType();
            statusTime = site.getStatusTime().toEpochSecond(ZoneOffset.UTC);
            error = site.getLastError();
            pages = Repos.pageRepo.findPageCountInSite(site.getId());
            lemmas = Repos.lemmaRepo.findLemmaCountInSite(site.getId());
        }
    }
}
