package com.cvinsight.ai.parser;

import com.cvinsight.ai.CVAnalysisException;
import com.cvinsight.ai.AnalysisResult;
import com.cvinsight.model.Feedback;
import com.cvinsight.model.Score;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Parses Claude's raw text response into an AnalysisResult (Score + Feedback).
 *
 * Expected JSON shape from Claude:
 * {
 *   "score": 78,
 *   "strengths":   ["...", "..."],
 *   "weaknesses":  ["...", "..."],
 *   "suggestions": ["...", "..."]
 * }
 *
 * Claude occasionally wraps JSON in markdown fences (```json ... ```) even when
 * told not to. stripFences() handles that defensively.
 */
public class ResponseParser {

    /**
     * Parses Claude's text reply into a Score and Feedback for the given CV.
     *
     * @param rawText  The string returned by ClaudeApiClient.send()
     * @param cvId     The CV this analysis belongs to
     */
    public AnalysisResult parse(String rawText, String cvId) throws CVAnalysisException {
        String json = stripFences(rawText.trim());

        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            int overall = root.get("score").getAsInt();
            if (overall < 0 || overall > 100) {
                throw new CVAnalysisException("Score out of range: " + overall);
            }

            List<String> strengths   = toStringList(root.getAsJsonArray("strengths"));
            List<String> weaknesses  = toStringList(root.getAsJsonArray("weaknesses"));
            List<String> suggestions = toStringList(root.getAsJsonArray("suggestions"));

            Score score = new Score(UUID.randomUUID().toString(), cvId, overall, LocalDateTime.now());
            Feedback feedback = new Feedback(UUID.randomUUID().toString(), cvId, strengths, weaknesses, suggestions);

            return new AnalysisResult(score, feedback);

        } catch (CVAnalysisException e) {
            throw e;
        } catch (Exception e) {
            throw new CVAnalysisException(
                "Could not parse Claude's response as valid JSON. Raw response was:\n" + rawText, e
            );
        }
    }

    /**
     * Strips markdown code fences if Claude added them despite instructions.
     * "```json\n{...}\n```"  →  "{...}"
     */
    private String stripFences(String text) {
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            int lastFence    = text.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return text.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return text;
    }

    private List<String> toStringList(JsonArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (JsonElement el : array) {
                list.add(el.getAsString());
            }
        }
        return list;
    }
}
