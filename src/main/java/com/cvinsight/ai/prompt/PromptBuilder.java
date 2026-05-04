package com.cvinsight.ai.prompt;

import com.cvinsight.model.CV;

/**
 * Builds the prompt sent to Claude for CV analysis.
 *
 * The prompt instructs Claude to return ONLY a JSON object so ResponseParser
 * can parse it reliably without stripping markdown or prose.
 *
 * Scoring guide embedded in the prompt keeps scores consistent across calls.
 */
public class PromptBuilder {

    private static final String SYSTEM_INSTRUCTIONS = """
        You are an expert CV and resume reviewer with experience hiring at top tech companies.
        Analyze the provided CV and return ONLY a valid JSON object — no markdown, no prose, no code fences.

        Use this exact JSON structure:
        {
          "score": <integer 0-100>,
          "strengths": [<string>, ...],
          "weaknesses": [<string>, ...],
          "suggestions": [<string>, ...]
        }

        Scoring guide:
          90-100 : Exceptional CV, ready to submit to top companies
          70-89  : Good CV with minor improvements needed
          50-69  : Average CV, significant improvements needed
          Below 50: Needs major revision before submitting

        Rules:
        - "strengths"   : 2-4 specific things the CV does well
        - "weaknesses"  : 2-4 specific issues that hurt the CV
        - "suggestions" : 3-5 concrete, actionable improvements
        - Keep each string under 120 characters
        - Be honest and specific — generic feedback is not helpful
        """;

    /**
     * Builds a prompt that includes the full CV text.
     */
    public String buildPrompt(CV cv) {
        String cvText = cv.getRawText() != null ? cv.getRawText().trim() : "(empty CV)";

        return SYSTEM_INSTRUCTIONS + """

            CV to analyze:
            ---
            """ + cvText + """

            ---
            Return only the JSON object.
            """;
    }
}
