package main.search;

import main.Lemmatizator.Lemmatizator;
import main.apiResponses.ErrorResponse;
import main.apiResponses.Response;
import main.apiResponses.SearchResponse;
import main.application_properties.Props;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.repository.Repos;

import java.util.ArrayList;
import java.util.List;

public class SearchRequest {
    private List<String> queryWords;
    private final List<String> siteUrls;
    private final int offset;
    private final int limit;
    List<QueryLemma> queryLemmas = new ArrayList<>();

    public SearchRequest(List<String> queryWords, List<String> siteUrls, Integer offset, Integer limit) {
        this.queryWords = queryWords;
        this.siteUrls = siteUrls;
        this.offset = offset;
        this.limit = limit;
    }

    private Response processRequest() {
        Long begin = System.currentTimeMillis();
        SearchResponse response = new SearchResponse();
//        response.createSample();

        for (String queryWord : queryWords) {
            for (String siteUrl : siteUrls) {
                Site site = Repos.siteRepo.findByUrl(siteUrl).get();
                Lemma lemma = Repos.lemmaRepo.findByLemmaAndSite(queryWord, site);
                List<Index> indexes = Repos.indexRepo.findAllByLemma(lemma);
                for (Index index : indexes) {
                    QueryLemma queryLemma = new QueryLemma(lemma, index.getPage());
                    queryLemmas.add(queryLemma);
                }
            }
        }
        queryLemmas.sort((ql1, ql2) ->
                Float.compare(ql1.getLemma().getFrequency(), ql2.getLemma().getFrequency()));

        System.out.println(System.currentTimeMillis() - begin);
        return response;
    }

    public static Response search(String query, String site, Integer offset, Integer limit) {

        List<String> queryWords = Lemmatizator.decomposeTextToLemmas(query);
        if (queryWords.isEmpty()) {
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

        System.out.println("query: " + queryWords +
                "; siteUrls: " + siteUrls + "; offset:  " + offset + "; limit: " + limit);

        SearchRequest searchRequest = new SearchRequest(queryWords, siteUrls, offset, limit);

        return searchRequest.processRequest();
    }

}
