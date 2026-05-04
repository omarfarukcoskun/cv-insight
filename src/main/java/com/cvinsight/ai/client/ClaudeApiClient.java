package com.cvinsight.ai.client;

import com.cvinsight.ai.CVAnalysisException;
import com.cvinsight.config.AppConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Sends prompts to the Anthropic Messages API and returns Claude's raw text response.
 *
 * API reference: https://docs.anthropic.com/en/api/messages
 *
 * This class handles only the HTTP concern — it knows nothing about CV models
 * or how to parse the JSON feedback. That is ResponseParser's job.
 */
public class ClaudeApiClient {

    private static final String API_URL      = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION  = "2023-06-01";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;

    public ClaudeApiClient() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)   // AI responses can be slow
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    /**
     * Sends the given prompt to Claude and returns its text response.
     *
     * @param prompt  The full prompt string built by PromptBuilder
     * @return        Claude's raw text reply (the JSON feedback string)
     * @throws CVAnalysisException on network error, API error, or unexpected response shape
     */
    public String send(String prompt) throws CVAnalysisException {
        String requestBody = buildRequestBody(prompt);

        Request request = new Request.Builder()
            .url(API_URL)
            .addHeader("x-api-key",         AppConfig.getAnthropicApiKey())
            .addHeader("anthropic-version", API_VERSION)
            .addHeader("content-type",      "application/json")
            .post(RequestBody.create(requestBody, JSON_TYPE))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new CVAnalysisException(
                    "Claude API returned error " + response.code() + ": " + responseBody
                );
            }

            return extractText(responseBody);

        } catch (IOException e) {
            throw new CVAnalysisException("Network error while calling Claude API: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the Anthropic Messages API request body.
     *
     * Shape:
     * {
     *   "model": "claude-sonnet-4-6",
     *   "max_tokens": 1024,
     *   "messages": [{"role": "user", "content": "<prompt>"}]
     * }
     */
    private String buildRequestBody(String prompt) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject body = new JsonObject();
        body.addProperty("model",      AppConfig.getClaudeModel());
        body.addProperty("max_tokens", AppConfig.getMaxTokens());
        body.add("messages", messages);

        return body.toString();
    }

    /**
     * Extracts the text from Claude's response envelope.
     *
     * Response shape:
     * {
     *   "content": [{"type": "text", "text": "<the feedback JSON>"}],
     *   ...
     * }
     */
    private String extractText(String responseBody) throws CVAnalysisException {
        try {
            JsonObject root    = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray  content = root.getAsJsonArray("content");

            if (content == null || content.isEmpty()) {
                throw new CVAnalysisException("Claude returned an empty response.");
            }

            return content.get(0).getAsJsonObject().get("text").getAsString();

        } catch (Exception e) {
            throw new CVAnalysisException("Failed to parse Claude response envelope: " + e.getMessage(), e);
        }
    }
}
