package movie_recommender;

import movie_recommender.abstracts.AbstractLlamaService;

import org.json.JSONObject;

public class LlamaInputService extends AbstractLlamaService {

  public LlamaInputService() {
    super();
  }

  public LlamaInputService(String model, String apiUrl, boolean stream) {
    super(model, apiUrl, stream);
  }

  public String sendPromptToLlama(String userPrompt) throws Exception {
    String finalPrompt = """
          You are an advanced cinematic query parser. Your job is to read the user's query about movies and return a JSON response with the following structure:

          {
            "intent": "<string>",
            "entities": {
              "actor": "<string or null>",
              "director": "<string or null>",
              "movie_title": "<string or null>",
              "genre": "<string or null>",
              "start_year": "<integer or null>",
              "end_year": "<integer or null>",
              "min_rating": "<double or null>",
              "max_rating": "<double or null>"
            }
          }

          - "intent" MUST be one of:
            [
              "recommend_movie",
              "recommend_similar_movies",
              "recommend_movies_by_genre",
              "recommend_movies_by_same_director",
              "recommend_movies_by_genre_and_rating",
              "find_movies_by_actor",
              "find_director_of_movie",

              "find_top_rated_movies",
              "recommend_movies_by_director",
              "find_highest_revenue_movies_by_genre",
         "find_movies_by_release_date_range",
              "find_movies_by_year_range",
              "find_movie_cast",
              "find_castmates",
        "find_movies_of_director_in_year",
         "find_movie_overview",
              "find_director_with_most_collaborations",
         "find_top_grossing_movies_of_year",
              "find_most_expensive_movies",
              "unknown_intent"
            ]
          -If the user mentions a movie with a year in parentheses (like "Inception (2010)"),
              put that exact string (including the parentheses and the year) in the "movie_title" field.
              Do not split the year into start_year.

          - If the user's query specifically asks for a different kind of information, or is unclear, set "intent" to "unknown_intent".

          - "actor", "director", "movie_title", "genre" are strings if identified, otherwise null.
          - "start_year" and "end_year" are integers if a year or range is explicitly mentioned, otherwise null.
          - "min_rating" and "max_rating" are doubles if a rating or rating range is explicitly mentioned, otherwise null.

          You MUST return ONLY valid JSON. Do NOT include explanations, extra text, or code blocks outside of the JSON. If there's no relevant information, fill fields with null or choose "unknown_intent".

          ### Examples

          1) User says: "What movies has Tom Hanks starred in?"
             Possible Output:
             {
               "intent": "find_movies_by_actor",
               "entities": {
                 "actor": "Tom Hanks",
                 "director": null,
                 "movie_title": null,
                 "genre": null,
                 "start_year": null,
                 "end_year": null,
                 "min_rating": null,
                 "max_rating": null
               }
             }

          2) User says: "I want Sci-Fi films from 2010 to 2015 with rating above 7.5"
             Possible Output:
             {
               "intent": "find_movies_by_genre_and_rating",
               "entities": {
                 "actor": null,
                 "director": null,
                 "movie_title": null,
                 "genre": "Science Fiction",
                 "start_year": 2010,
                 "end_year": 2015,
                 "min_rating": 7.5,
                 "max_rating": null
               }
             }

          Use these instructions carefully.

          Now, analyze the following text and produce the JSON:

          USER QUERY: "%s"

              """
        .formatted(userPrompt.replace("\"", "\\\""));

    String rawResponse = callApi(finalPrompt);

    if (!rawResponse.startsWith("{")) {
      throw new Exception("Invalid JSON response from Llama : " + rawResponse);
    }

    JSONObject json = new JSONObject(rawResponse);
    return json.getString("response");
  }
}
