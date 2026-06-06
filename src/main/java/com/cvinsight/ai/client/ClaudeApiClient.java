package com.cvinsight.ai.client;

import com.cvinsight.ai.CVAnalysisException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Sends prompts to the Ollama local API and returns the model's raw text response.
 *
 * API reference: https://github.com/ollama/ollama/blob/main/docs/api.md#generate-a-completion
 *
 * This class handles only the HTTP concern — it knows nothing about CV models
 * or how to parse the JSON feedback. That is ResponseParser's job.
 */
public class ClaudeApiClient {

    private static final String API_URL      = "http://localhost:11434/api/generate";
    private static final String MODEL        = "llama3";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;

    public ClaudeApiClient() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)  // local LLM responses can be slow
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    /**
     * Sends the given prompt to Ollama and returns its text response.
     *
     * @param prompt  The full prompt string built by PromptBuilder
     * @return        The model's raw text reply (the JSON feedback string)
     * @throws CVAnalysisException on network error, API error, or unexpected response shape
     */
    public String send(String prompt) throws CVAnalysisException {
        return send(prompt, 1024);
    }

    /** Same as {@link #send(String)} but with a custom token budget (e.g. 2048 for comparisons). */
    public String send(String prompt, int numPredict) throws CVAnalysisException {
        String requestBody = buildRequestBody(prompt, numPredict);

        Request request = new Request.Builder()
            .url(API_URL)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(requestBody, JSON_TYPE))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new CVAnalysisException(
                    "Ollama API returned error " + response.code() + ": " + responseBody
                );
            }

            return extractText(responseBody);

        } catch (IOException e) {
            throw new CVAnalysisException("Network error while calling Ollama API: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the Ollama /api/generate request body.
     *
     * Shape:
     * {
     *   "model": "llama3",
     *   "prompt": "<prompt>",
     *   "stream": false
     * }
     */
    private String buildRequestBody(String prompt, int numPredict) {
        JsonObject body = new JsonObject();
        body.addProperty("model",       MODEL);
        body.addProperty("prompt",      prompt);
        body.addProperty("stream",      false);
        body.addProperty("num_predict", numPredict);
        return body.toString();
    }

    /**
     * Extracts the text from Ollama's response envelope.
     *
     * Response shape:
     * {
     *   "model": "llama3",
     *   "response": "<the feedback JSON>",
     *   "done": true,
     *   ...
     * }
     */
    private String extractText(String responseBody) throws CVAnalysisException {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

            if (!root.has("response") || root.get("response").isJsonNull()) {
                throw new CVAnalysisException("Ollama returned an empty response.");
            }

            return root.get("response").getAsString();

        } catch (CVAnalysisException e) {
            throw e;
        } catch (Exception e) {
            throw new CVAnalysisException("Failed to parse Ollama response envelope: " + e.getMessage(), e);
        }
    }
}
