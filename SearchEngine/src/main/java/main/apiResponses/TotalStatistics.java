package main.apiResponses;

import lombok.Data;
import main.builders.SiteBuilder;
import main.model.Site;
import main.repository.Repos;

import java.util.List;

@Data
public class TotalStatistics {
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
