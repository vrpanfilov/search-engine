package main.search;

import main.apiResponses.PageData;
import main.apiResponses.SearchResponse;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.repository.Repos;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

@Component
public class SearchResponseBuilder implements Runnable {
    private static final String SITE_NOT_FOUND = "Указанный сайт не найден";

    private static final Map<SearchRequest, SearchResponse> responses = new HashMap<>();
    private static SynchronousQueue<SearchRequest> requestQueue = new SynchronousQueue();
    private static SynchronousQueue<SearchResponse> responseQueue = new SynchronousQueue();
    private static final Map<SearchRequest, Long> lastTimes = new HashMap<>();
    private static ExecutorService executor = Executors.newCachedThreadPool();

    private SearchRequest request;
    private SearchResponse response;
    private Set<PageRelevance> relevanceSet;
    private final Map<Integer /*page.id*/, PageRelevance> relevanceMap = new HashMap<>();
    private List<PageRelevance> relevanceList;

    public static SynchronousQueue<SearchRequest> getRequestQueue() {
        return requestQueue;
    }

    public static SynchronousQueue<SearchResponse> getResponseQueue() {
        return responseQueue;
    }

    public SearchResponseBuilder() {
    }

    public SearchResponseBuilder(SearchRequest request) {
        this.request = request;
        synchronized (responses) {
            response = responses.get(request);
        }
    }

    @Override
    public void run() {
        if (request == null) {
            for (; ; ) {
                communicationWithSearchController();
            }
        }
        processRequest();
    }

    private void communicationWithSearchController() {
        try {
            request = requestQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        defineResponse();
        formResultingResponse();
        try {
            responseQueue.put(response);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void defineResponse() {
        boolean toCreateNewBuider = false;
        synchronized (responses) {
            response = responses.get(request);
            if (response == null) {
                response = responses.put(request, new SearchResponse());
                toCreateNewBuider = true;
            }
        }
        if (toCreateNewBuider) {
            Runnable builder = new SearchResponseBuilder(request);
            executor.execute(builder);
        }
    }

    private void formResultingResponse() {
        for (; ; ) {
            boolean cond;
            synchronized (responses) {
                if (response.getCount() > 0) {
                    if (request.getOffset() + request.getLimit() > response.getCount()) {
                        request.setLimit(response.getCount() - request.getOffset());
                    }
                }
                cond = request.getOffset() + request.getLimit() <= response.getPageDataList().size();
            }
            if (cond) {
                response = formPartialResponse(request, response);
                return;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized (responses) {
                response = responses.get(request);
            }
        }
    }

    private SearchResponse formPartialResponse(SearchRequest request, SearchResponse response) {
        SearchResponse partialResponse = new SearchResponse();
        synchronized (responses) {
            partialResponse.setCount(response.getCount());
            for (int index = request.getOffset();
                 index < request.getOffset() + request.getLimit(); index++) {
                partialResponse.getPageDataList().add(response.getPageDataList().get(index));
            }
        }
        return partialResponse;
    }

    private void processRequest() {
        List<Lemma> lemmaList = defineRequestLemmaList();
        List<PageRelevance> pageRelevances = definePageRelevances(lemmaList);
        calculateRelevances();
        prepareResponse();
    }

    private List<Lemma> defineRequestLemmaList() {
        List<Lemma> lemmaList = new ArrayList<>();
        for (String siteUrl : request.getSiteUrls()) {
            Site site = Repos.siteRepo.findByUrl(siteUrl).get();
            for (String queryWord : request.getQueryWords()) {
                List<Lemma> lemmas = Repos.lemmaRepo.findAllByLemmaAndSite(queryWord, site);
                lemmaList.addAll(lemmas);
            }
        }
        lemmaList.sort((l1, l2) -> Float.compare(l1.getFrequency(), l2.getFrequency()));
        return lemmaList;
    }

    private List<PageRelevance> definePageRelevances(List<Lemma> lemmaList) {
        boolean isFirstLemma = true;
        for (Lemma lemma : lemmaList) {
            if (isFirstLemma) {
                defineRelevancesForFirstLemma(lemma);
                isFirstLemma = false;
            } else {
                defineRelevancesForNextLemmas(lemma);
            }
        }
        return new ArrayList<>(relevanceSet);
    }

    private void defineRelevancesForFirstLemma(Lemma lemma) {
        List<Index> indices = Repos.indexRepo.findAllByLemma(lemma);
        for (Index index : indices) {
            PageRelevance relevance = relevanceMap.get(index.getPage().getId());
            if (relevance == null) {
                relevance = new PageRelevance();
                relevance.setPage(index.getPage());
                relevanceMap.put(index.getPage().getId(), relevance);
            }
            LemmaRank lemmaRank = new LemmaRank(lemma, index.getRank());
            relevance.getLemmaRanks().add(lemmaRank);
        }
        relevanceSet = new HashSet<>(relevanceMap.values());
    }

    private void defineRelevancesForNextLemmas(Lemma lemma) {
        Set<PageRelevance> lemmaRelevances = new HashSet<>();
        List<Index> indices = Repos.indexRepo.findAllByLemma(lemma);
        for (Index index : indices) {
            PageRelevance relevance = relevanceMap.get(index.getPage().getId());
            if (!relevanceSet.contains(relevance)) {
                continue;
            }
            LemmaRank lemmaRank = new LemmaRank(lemma, index.getRank());
            relevance.getLemmaRanks().add(lemmaRank);
            lemmaRelevances.add(relevance);
        }
        relevanceSet.retainAll(lemmaRelevances);
    }

    private void calculateRelevances() {
        float maxRelevance = 0;
        for (PageRelevance relevance : relevanceSet) {
            float absRelevance = 0;
            for (LemmaRank lemmaRank : relevance.getLemmaRanks()) {
                absRelevance += lemmaRank.getRank();
            }
            relevance.setAbsoluteRelevance(absRelevance);
            if (absRelevance > maxRelevance) {
                maxRelevance = absRelevance;
            }
        }

        for (PageRelevance relevance : relevanceSet) {
            relevance.setRelativeRelevance(
                    relevance.getAbsoluteRelevance() / maxRelevance);
        }

        relevanceList = new ArrayList<>(relevanceSet);
        relevanceList.sort((r1, r2) -> -Float.compare(
                r1.getRelativeRelevance(), r2.getRelativeRelevance()));
    }

    private void prepareResponse() {
        int count = relevanceList.size();
        response.setCount(count);
        for (int pageDateNumber = 0; pageDateNumber < count; pageDateNumber++) {
            PageData data = new PageData();
            PageRelevance relevance = relevanceList.get(pageDateNumber);
            Page page = relevance.getPage();
            Site site = page.getSite();
            data.setSite(site.getUrl());
            data.setSiteName(site.getName());
            data.setUri(page.getPath());
            String snippet = new Snippet(page, request.getQueryWords()).formSnippet();
            data.setSnippet(snippet);
            data.setRelevance(relevance.getRelativeRelevance());
        }
    }
}
