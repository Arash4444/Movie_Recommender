package movie_recommender.abstracts;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public abstract class AbstractLlamaService {

    private String model = "llama3.2";
    private String apiUrl = "http://localhost:11434/api/generate";
    private boolean stream = false;

    protected AbstractLlamaService() {
    }

    protected AbstractLlamaService(String model, String apiUrl, boolean stream) {
        this.model = model;
        this.apiUrl = apiUrl;
        this.stream = stream;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    protected String callApi(String promptBody) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", this.model);
        requestBody.put("prompt", promptBody);
        requestBody.put("stream", this.stream);

        URL connectionUrl = new URL(this.apiUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connectionUrl.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);

        try (OutputStream os = httpURLConnection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        int statusCode = httpURLConnection.getResponseCode();
        if (statusCode != 200) {
            throw new RuntimeException("Failed to call llama api. code: " + statusCode);
        }

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        StringBuilder responseSb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            responseSb.append(line.trim());
        }

        return responseSb.toString().trim();
    }
}
