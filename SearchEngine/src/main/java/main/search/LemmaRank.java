package main.search;

import lombok.Data;
import main.model.Lemma;

@Data
public class LemmaRank {
    private Lemma lemma;
    private float rank;

    public LemmaRank(Lemma lemma, float rank) {
        this.lemma = lemma;
        this.rank = rank;
    }
}
