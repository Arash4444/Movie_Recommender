package movie_recommender;

import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.util.*;

public class VectorSearch {

    private final String embeddingsFilePath;
    private final String nomicEmbedUrl;
    private List<MovieEmbeddingEntry> allMovies;

    public VectorSearch(String embeddingsFilePath, String nomicEmbedUrl) {
        this.embeddingsFilePath = embeddingsFilePath;
        this.nomicEmbedUrl = nomicEmbedUrl;
        this.allMovies = new ArrayList<>();
        loadEmbeddingsFromJson();
    }

    private void loadEmbeddingsFromJson() {
        try {
            File file = new File(embeddingsFilePath);

            if (!file.exists()) {
                System.err.println("[ERROR] File not found: " + file.getAbsolutePath());
                throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
            }

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();

            JSONArray arr = new JSONArray(stringBuilder.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jsonobj = arr.getJSONObject(i);
                String title = jsonobj.optString("title", "");
                String overview = jsonobj.optString("overview", "");

                JSONArray embeddingArr = jsonobj.optJSONArray("embedding");
                if (embeddingArr == null)
                    continue;

                float[] embedding = new float[embeddingArr.length()];
                for (int j = 0; j < embeddingArr.length(); j++) {
                    embedding[j] = (float) embeddingArr.getDouble(j);
                }
                allMovies.add(new MovieEmbeddingEntry(title, overview, embedding));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float[] getNomicEmbedding(String text) {
        try {
            URL url = new URL(nomicEmbedUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            JSONObject req = new JSONObject();
            req.put("model", "nomic-embed-text");
            req.put("prompt", text);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = req.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int statusCode = con.getResponseCode();
            if (statusCode != 200) {
                System.err.println("Unable to get embedding!");
                return null;
            }

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();

            JSONObject Jsonobj = new JSONObject(stringBuilder.toString());
            JSONArray embeddingArr = Jsonobj.optJSONArray("embedding");
            if (embeddingArr == null) {
                System.err.println("Did'nt recieve any embedding field!");
                return null;
            }

            float[] embedding = new float[embeddingArr.length()];
            for (int i = 0; i < embeddingArr.length(); i++) {
                embedding[i] = (float) embeddingArr.getDouble(i);
            }
            return embedding;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String searchSimilarMovie(String userQuestion) {
        float[] queryEmbedding = getNomicEmbedding(userQuestion);
        if (queryEmbedding == null) {
            return "Failed to get embedding from nomic!";
        }
        // System.out.println("Query embedding vector: " +
        // Arrays.toString(queryEmbedding)); //for debug
        // System.out.println("Query embedding length: " + queryEmbedding.length);
        if (allMovies.isEmpty()) {
            return "No movie embeddings available!";
        }
        MovieEmbeddingEntry bestMatch = null;
        float bestScore = -1.0f;
        for (MovieEmbeddingEntry x : allMovies) {
            float Similarity = cosineSimilarity(queryEmbedding, x.embedding);

            if (Similarity > bestScore) {
                bestScore = Similarity;
                bestMatch = x;
            }
        }
        if (bestMatch == null) {
            return "No matching movie found.";
        } else {
            return String.format(
                    "Best match: %s (score=%f)\n Overview: %s",
                    bestMatch.title,
                    bestScore,
                    bestMatch.overview);
        }
    }

    private float cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            return -1f;
        }
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += (v1[i] * v2[i]);
            norm1 += (v1[i] * v1[i]);
            norm2 += (v2[i] * v2[i]);
        }
        if (norm1 == 0 || norm2 == 0)
            return 0f;
        return (float) (dot / (Math.sqrt(norm1) * Math.sqrt(norm2)));
    }

    private static class MovieEmbeddingEntry {
        String title;
        String overview;
        float[] embedding;

        public MovieEmbeddingEntry(String title, String overview, float[] embedding) {
            this.title = title;
            this.overview = overview;
            this.embedding = embedding;
        }
    }
}
