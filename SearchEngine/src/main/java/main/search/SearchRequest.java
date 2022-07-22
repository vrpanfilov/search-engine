package main.search;

import main.Lemmatizator.Lemmatizator;
import main.apiResponses.ErrorResponse;
import main.apiResponses.Response;
import main.apiResponses.SearchResponse;
import main.application_properties.Props;

import java.util.ArrayList;
import java.util.List;

public class SearchRequest {
    private List<String> queryLemmas;
    private String siteUrl;
    private int offset;
    private int limit;

    public SearchRequest(List<String> queryLemmas, String site, Integer offset, Integer limit) {
        this.queryLemmas = queryLemmas;
        siteUrl = site;
        this.offset = offset;
        this.limit = limit;
    }

    private Response processRequest() {
        SearchResponse response = new SearchResponse();
        response.createSample();
        return response;
    }

    public static Response search(String query, String site, Integer offset, Integer limit) {

        List<String> queryLemmas = Lemmatizator.decomposeTextToLemmas(query);
        if (queryLemmas.isEmpty()) {
            return new ErrorResponse("Задан пустой поисковый запрос");
        }

        List<String> siteUrls = null;
        if (site == null) {
            siteUrls = Props.getAllSiteUrls();
        } else {
            siteUrls = new ArrayList<String>();
            siteUrls.add(site);
        }
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 20;
        }

        System.out.println("siteUrls: " + siteUrls + ", offset:  " + offset + ", limit: " + limit);

        SearchRequest searchRequest = new SearchRequest(queryLemmas, site, offset, limit);

        return searchRequest.processRequest();
    }

}
