package main.search;

import lombok.Data;
import main.model.Lemma;
import main.model.Page;

@Data
public class QueryLemma {
    private Lemma lemma;
    private Page page;

    public QueryLemma(Lemma lemma, Page page) {
        this.lemma = lemma;
        this.page = page;
    }
}
