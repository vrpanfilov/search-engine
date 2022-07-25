package main.controller;

import main.apiResponses.ErrorResponse;
import main.apiResponses.Response;
import main.application_properties.Props;
import main.builders.PageBuilder;
import main.builders.SiteBuilder;
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
    public Response startIndexing() {
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
        return new ErrorResponse("Индексация не была запущена");
    }

    @PostMapping("/api/indexPage")
    public Response indexPage(@RequestParam(required = false) String url) {
        String result = PageBuilder.indexPage(url);
        if (result.equals(PageBuilder.OK)) {
            return new Response();
        }
        return new ErrorResponse(result);
    }

    @GetMapping("/api/statistics")
    public Response statistics() {
        return SiteBuilder.getStatistics();
    }

}
