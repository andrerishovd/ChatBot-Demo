package com.example.demo;

import com.example.demo.Dto.ContentItem;
import com.example.demo.Dto.OpenAiResponse;
import com.example.demo.Dto.OutputItem;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final RestClient restClient;
    private final String model;

    public OpenAiService(
    ) {
        Dotenv dotenv = Dotenv.load();

        String apiKey = dotenv.get("OPENAI_API_KEY");
        String baseUrl = dotenv.get("BASE_URL", "https://api.openai.com");
        this.model = dotenv.get("MODEL", "gpt-4o-mini");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not set.");
        }

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChatResponse chat(String userMessage) {
        Map<String, Object> body = buildRequestBody(userMessage);

        OpenAiResponse resp = restClient.post()
                .uri("/v1/responses")
                .body(body)
                .retrieve()
                .body(OpenAiResponse.class);

        String reply = extractOutputText(resp);
        return new ChatResponse(reply);
    }

    private Map<String, Object> buildRequestBody(String userMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        List<Map<String, Object>> input = new ArrayList<>();
        input.add(Map.of("role", "system", "content", "You are a helpful demo assistant."));
        input.add(Map.of("role", "user", "content", userMessage));
        body.put("input", input);

        body.put("text", Map.of("format", Map.of("type", "text")));

        return body;
    }

    private String extractOutputText(OpenAiResponse resp) {
        if (resp == null || resp.output() == null) return "";

        StringBuilder sb = new StringBuilder();

        for (OutputItem item : resp.output()) {
            if (item == null || item.content() == null) continue;

            for (ContentItem c : item.content()) {
                if (c == null) continue;

                if ("output_text".equals(c.type()) && c.text() != null) {
                    if (!sb.isEmpty()) sb.append("\n");
                    sb.append(c.text());
                }
            }
        }

        return sb.toString();
    }
}
