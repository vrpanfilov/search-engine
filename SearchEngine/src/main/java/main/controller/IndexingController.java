package main.controller;

import lombok.Data;
import main.apiResponses.ErrorResponse;
import main.apiResponses.Response;
import main.apiResponses.StatisticsResponse;
import main.application_properties.Props;
import main.builders.IndexBuilder;
import main.builders.SiteBuilder;
import main.repository.LemmaRepository;
import main.repository.Repos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexingController {
    @Autowired
    public Props props;

    @GetMapping("/api/startIndexing")
    public Response startEntity() {
        boolean isIndexing = SiteBuilder.buildAllSites();
        if (isIndexing) {
            return new ErrorResponse("Индексация уже запущена");
        }
        return new Response();
    }

    @GetMapping("/api/stopIndexing")
    public Response stopIndexing() {
        boolean isIndexing = SiteBuilder.stopIndexing();
        if (isIndexing) {
            return new Response();
        }
        return new ErrorResponse("Индексация не запущена");
    }

    @PostMapping("/api/indexPage")
    public Response indexPage(@RequestParam String url) {
        String result = SiteBuilder.indexPage(url);
        if (result.equals("OK")) {
            return new Response();
        }
        return new ErrorResponse(result);
    }

    @GetMapping("/api/statistics")
    public StatisticsResponse statistics() {
        return SiteBuilder.getStatistics();
    }

}
