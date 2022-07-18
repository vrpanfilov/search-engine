package main.apiResponses;

import lombok.Data;

@Data
public class StatisticsResponse extends Response {
    private Statistics statistics = new Statistics();
}
