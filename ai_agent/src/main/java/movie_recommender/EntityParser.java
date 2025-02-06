package movie_recommender;

import org.json.JSONObject;

public class EntityParser {
    private String intent;
    private String actor;
    private String director;
    private String movieTitle;
    private String genre;
    private int startYear;
    private int endYear;
    private double minRating;
    private double maxRating;

    public EntityParser(String rawJson) {
        parse(rawJson);
    }

    private void parse(String rawJson) {
        JSONObject root = new JSONObject(rawJson);

        this.intent = root.optString("intent", "unknown_intent");

        JSONObject entities = root.optJSONObject("entities");
        if (entities != null) {
            this.actor = entities.optString("actor", null);
            this.director = entities.optString("director", null);
            this.movieTitle = entities.optString("movie_title", null);
            this.genre = entities.optString("genre", null);
            this.startYear = entities.optInt("start_year", 0);
            this.endYear = entities.optInt("end_year", 0);
            this.minRating = entities.optDouble("min_rating", 0.0);
            this.maxRating = entities.optDouble("max_rating", 0.0);
        }
    }


    public String getIntent() {
        return intent;
    }

    public String getActor() {
        return actor;
    }

    public String getDirector() {
        return director;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getGenre() {
        return genre;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public double getMinRating() {
        return minRating;
    }

    public double getMaxRating() {
        return maxRating;
    }

    public static EntityParser fromJson(String rawJson) {
        return new EntityParser(rawJson);
    }
}
