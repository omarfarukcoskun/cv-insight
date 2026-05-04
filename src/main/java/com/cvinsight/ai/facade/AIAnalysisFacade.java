package com.cvinsight.ai.facade;

import com.cvinsight.ai.AnalysisResult;
import com.cvinsight.ai.CVAnalysisException;
import com.cvinsight.ai.client.ClaudeApiClient;
import com.cvinsight.ai.parser.ResponseParser;
import com.cvinsight.ai.prompt.PromptBuilder;
import com.cvinsight.model.CV;

/**
 * PATTERN: Facade
 *
 * Hides the full complexity of the AI pipeline behind a single method call.
 *
 * Without this facade, every caller would need to know about PromptBuilder,
 * ClaudeApiClient, and ResponseParser, their construction order, and how
 * to thread a CV through all three. With it, the entire pipeline is:
 *
 *   AnalysisResult result = facade.analyze(cv);
 *
 * AnalysisService (and through it, Dev B's UI code) calls only this class.
 * The three inner collaborators are invisible to the rest of the application.
 *
 * Internal pipeline:
 *   CV  →  PromptBuilder.buildPrompt()
 *       →  ClaudeApiClient.send()       (HTTP call to Anthropic)
 *       →  ResponseParser.parse()       (JSON → Score + Feedback)
 *       →  AnalysisResult
 */
public class AIAnalysisFacade {

    private final PromptBuilder   promptBuilder;
    private final ClaudeApiClient apiClient;
    private final ResponseParser  responseParser;

    public AIAnalysisFacade() {
        this.promptBuilder  = new PromptBuilder();
        this.apiClient      = new ClaudeApiClient();
        this.responseParser = new ResponseParser();
    }

    /**
     * Runs the full AI analysis pipeline for the given CV.
     *
     * @throws CVAnalysisException if the API call fails or the response cannot be parsed
     */
    public AnalysisResult analyze(CV cv) throws CVAnalysisException {
        String prompt      = promptBuilder.buildPrompt(cv);
        String rawResponse = apiClient.send(prompt);
        return responseParser.parse(rawResponse, cv.getId());
    }
}
