package main.controller;

import main.apiResponses.ErrorResponse;
import main.apiResponses.Response;
import main.apiResponses.SearchResponse;
import main.search.SearchRequest;
import main.search.SearchResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    @Autowired
    private SearchResponseBuilder searchResponseBuilder;

    @GetMapping("/api/search")
    public Response search(@RequestParam(required = false) String query,
                           @RequestParam(required = false) String site,
                           @RequestParam(required = false) Integer offset,
                           @RequestParam(required = false) Integer limit) {
        SearchRequest request = new SearchRequest().buildRequest(query, site, offset, limit);
        if (request == null) {
            return new ErrorResponse("Задан пустой поисковый запрос");
        }
        return receiveResponse(request);
    }

    public Response receiveResponse(SearchRequest request) {
        SearchResponse response;
        try {
            searchResponseBuilder.getRequestQueue().put(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            response = searchResponseBuilder.getResponseQueue().take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
