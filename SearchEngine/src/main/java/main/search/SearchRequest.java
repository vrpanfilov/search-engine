package main.search;

import lombok.Data;
import main.Lemmatizator.Lemmatizator;
import main.application_properties.Props;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchRequest {
    private List<String> queryWords;
    private List<String> siteUrls = new ArrayList<>();
    private int offset;
    private int limit;

    @Override
    public int hashCode() {
        return queryWords.hashCode() + siteUrls.hashCode();
    }

    public SearchRequest buildRequest(String query, String siteUrl, Integer offset, Integer limit) {
        queryWords = Lemmatizator.decomposeTextToLemmas(query);
        if (queryWords.isEmpty()) {
            return null;
        }

        if (siteUrl == null) {
            siteUrls = Props.getAllSiteUrls();
        } else {
            siteUrls.add(siteUrl);
        }
        this.offset = offset == null ? 0 : offset;
        this.limit = limit == null ? 20 : limit;
        return this;
    }
}

