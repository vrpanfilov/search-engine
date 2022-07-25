package main.search;

import main.Lemmatizator.Lemmatizator;
import main.apiResponses.ErrorResponse;
import main.apiResponses.Response;
import main.apiResponses.SearchResponse;
import main.application_properties.Props;
import main.model.Index;
import main.model.Lemma;
import main.model.Site;
import main.repository.Repos;

import java.util.*;

public class SearchRequest {
    private List<String> queryWords;
    private final List<String> siteUrls;
    private final int offset;
    private final int limit;

    public SearchRequest(List<String> queryWords, List<String> siteUrls, Integer offset, Integer limit) {
        this.queryWords = queryWords;
        this.siteUrls = siteUrls;
        this.offset = offset;
        this.limit = limit;
    }

    private List<Lemma> defineRequestLemmaList() {
        List<Lemma> lemmaList = new ArrayList<>();
        for (String queryWord : queryWords) {
            for (String siteUrl : siteUrls) {
                Site site = Repos.siteRepo.findByUrl(siteUrl).get();
                List<Lemma> lemmas = Repos.lemmaRepo.findAllByLemmaAndSite(queryWord, site);
                for (Lemma lemma : lemmas) {
                    lemmaList.add(lemma);
                }
            }
        }
        lemmaList.sort((l1, l2) -> Float.compare(l1.getFrequency(), l2.getFrequency()));
        return lemmaList;
    }

    private List<PageDetails> definePageDetailsList(List<Lemma> lemmaList) {
        Set<PageDetails> allPages = null;
        Map<Integer, PageDetails> pageDetails = new HashMap<>();

        for (Lemma lemma : lemmaList) {
            List<Index> indices = Repos.indexRepo.findAllByLemma(lemma);
            for (Index index : indices) {
                PageDetails details = pageDetails.get(index.getPage().getId());
                if (details == null) {
                    details = new PageDetails();
                    details.setPage(index.getPage());
                }
                LemmaRank lemmaRank = new LemmaRank(lemma, index.getRank());
                details.getLemmaRanks().add(lemmaRank);
                pageDetails.put(index.getPage().getId(), details);
            }
            Set<PageDetails> detailsSet = new HashSet<>(pageDetails.values());
            if (allPages == null) {
                allPages = detailsSet;
            } else {
                allPages.retainAll(detailsSet);
            }
            if (allPages.isEmpty()) {
                break;
            }
        }

        Set<PageDetails> detailsSet = new HashSet<>(pageDetails.values());
        Set<PageDetails> finalAllPages = allPages;
        detailsSet.removeIf(details -> !finalAllPages.contains(details));

        return new ArrayList<>(detailsSet);
    }

    private void calculateRelevances(List<PageDetails> pageDetailsList) {
        float maxRelevance = 0;
        for (PageDetails details : pageDetailsList) {
            float absRelevance = 0;
            for (LemmaRank lemmaRank : details.getLemmaRanks()) {
                absRelevance += lemmaRank.getRank();
            }
            details.setAbsoluteRelevance(absRelevance);
            if (absRelevance > maxRelevance) {
                maxRelevance = absRelevance;
            }
        }
        for (PageDetails details : pageDetailsList) {
            details.setRelativeRelevance(
                    details.getAbsoluteRelevance() / maxRelevance);
        }
        pageDetailsList.sort((o1, o2) -> -Float.compare(
                o1.getRelativeRelevance(), o2.getRelativeRelevance()));
    }

    private Response processRequest() {
        Long begin = System.currentTimeMillis();
        SearchResponse response = new SearchResponse();
//        response.createSample();

        List<Lemma> lemmaList = defineRequestLemmaList();
        List<PageDetails> pageDetailsList = definePageDetailsList(lemmaList);
        calculateRelevances(pageDetailsList);

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
