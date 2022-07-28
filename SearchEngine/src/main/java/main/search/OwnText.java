package main.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OwnText {
    String text;
    List<Integer> keyWordIndices = new ArrayList<>();

    public OwnText(String text) {
        this.text = text;
    }
}
