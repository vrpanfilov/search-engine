package main.apiResponses;

import lombok.Data;

@Data
public class ErrorResponse extends Response {
    private String error;

    public ErrorResponse(String error) {
        setResult(false);
        this.error = error;
    }
}
