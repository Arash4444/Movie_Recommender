package movie_recommender;

import movie_recommender.abstracts.AbstractLlamaService;

import org.json.JSONObject;

public class LlamaOutputService extends AbstractLlamaService {

    public LlamaOutputService() {
        super();
    }

    public LlamaOutputService(String model, String apiUrl, boolean stream) {
        super(model, apiUrl, stream);
    }

    public String getAnswerWithContext(String userPrompt, String dbInfo) throws Exception {
        String finalPrompt = """
                You are a specialized assistant.
                You must act as a simple aggregator of the given data.
                The user asked: "%s"

                Here is the data from our database (each item may be an action movie, or any other movie):
                %s

                Instructions:
                1) If the data list is NOT empty, output them directly, as they are.
                2) If the data list is empty, say "No data available".
                3) Do NOT add any new commentary, genre analysis, or disclaimers.
                4) Do NOT say "I cannot find a matching result" or anything else.
                5) Just return the list or "No data available".

                Produce your final answer below (no extra text):
                """.formatted(userPrompt.replace("\"", "\\\""), dbInfo);

        String rawResponse = callApi(finalPrompt);

        if (!rawResponse.startsWith("{")) {
            throw new Exception("Invalid JSON response from Llama (OutputService): " + rawResponse);
        }

        JSONObject json = new JSONObject(rawResponse);
        return json.getString("response");
    }
}
