package main.apiResponses;

import lombok.Data;
import main.model.Site;
import main.repository.Repos;

import java.util.ArrayList;
import java.util.List;

@Data
public class Statistics {
    private TotalStatistics total = new TotalStatistics();
    private List<DetailedStatistics> detailed = new ArrayList<>();

    public Statistics() {
        List<Site> sites = Repos.siteRepo.findAll().stream()
                .filter(site -> site.getType().equals(Site.INDEXED)).toList();
        for (Site site : sites) {
            DetailedStatistics detailedStatistics = new DetailedStatistics(site);
            detailed.add(detailedStatistics);
        }
    }

}

