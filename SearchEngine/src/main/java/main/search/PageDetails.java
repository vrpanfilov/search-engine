package main.search;

import lombok.Data;
import main.model.Page;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageDetails {
    private Page page;
    private List<LemmaRank> lemmaRanks = new ArrayList<>();
    private float absoluteRelevance;
    private float relativeRelevance;

    @Override
    public int hashCode() {
        return page.hashCode();
    }
}
