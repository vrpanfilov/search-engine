package main.apiResponses;

import lombok.Data;
import main.model.Site;
import main.repository.Repos;

import java.sql.Timestamp;
import java.time.ZoneOffset;

@Data
public class DetailedStatistics {
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
