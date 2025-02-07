package movie_recommender;

import java.util.List;

public class QueryHandler {

    private final Neo4jMovieQuery neo4jQuery;
    private String userQuestion;

    public QueryHandler(Neo4jMovieQuery neo4jQuery , String userQuestion) {
        this.neo4jQuery = neo4jQuery;
        this.userQuestion=userQuestion;
    }

    public List<String> handleIntent(EntityParser parser) {
        List<String> resultList = null;

        String intent = parser.getIntent();

        switch (intent) {
            case "find_movies_by_actor":
                if (parser.getActor() != null) {
                    resultList = neo4jQuery.findMoviesByActor(parser.getActor());
                } else {
                    System.out.println("No actor specified!");
                }
                break;

            case "find_castmates":
                if (parser.getActor() != null) {
                    resultList = neo4jQuery.findCastmates(parser.getActor());
                } else {
                    System.out.println("No actor specified!");
                }
                break;

            case "recommend_movies_by_genre":
                if (parser.getGenre() != null) {
                    resultList = neo4jQuery.recommendMoviesByGenre(parser.getGenre());
                } else {
                    System.out.println("No genre specified!");
                }
                break;

            case "find_director_of_movie":
                if (parser.getMovieTitle() != null) {
                    resultList = neo4jQuery.findMovieDirector(parser.getMovieTitle());
                } else {
                    System.out.println("No movie title specified!");
                }
                break;

            case "recommend_movies_by_same_director":
                if (parser.getMovieTitle() != null) {
                    resultList = neo4jQuery.recommendMoviesBySameDirector(parser.getMovieTitle());
                } else {
                    System.out.println("No movie title specified!");
                }
                break;

            case "recommend_movies_by_genre_and_rating":
                if (parser.getGenre() != null && parser.getMinRating() > 0) {
                    resultList = neo4jQuery.recommendMoviesByGenreAndMinRating(parser.getGenre(),
                            parser.getMinRating());
                } else {
                    System.out.println("No valid genre or minRating!");
                }
                break;

            case "find_movie_cast":
                if (parser.getMovieTitle() != null) {
                    resultList = neo4jQuery.findMovieCast(parser.getMovieTitle());
                } else {
                    System.out.println("No movie title specified!");
                }
                break;

            case "find_most_expensive_movies":
                resultList = neo4jQuery.findMostExpensiveMovies(5);
                break;

            case "find_top_rated_movies":
                resultList = neo4jQuery.findTopRatedMovies(parser.getMinRating());
                break;

            case "find_highest_revenue_movies_by_genre":
                if (parser.getGenre() != null) {
                    resultList = neo4jQuery.findHighestRevenueMoviesByGenre(parser.getGenre());
                } else {
                    System.out.println("No genre specified!");
                }
                break;

            case "recommend_similar_movies":
                if (parser.getMovieTitle() != null) {
                    resultList = neo4jQuery.recommendSimilarMovies(parser.getMovieTitle());
                }
                break;

            case "find_movie_overview":
                if (parser.getMovieTitle() != null) {
                    resultList = neo4jQuery.findMovieOverview(parser.getMovieTitle());
                }
                break;

            case "recommend_movies_by_director":
                if (parser.getDirector() != null) {
                    resultList = neo4jQuery.recommendMoviesByDirector(parser.getDirector());
                } else {
                    System.out.println("No director specified!");
                }
                break;

            case "find_movies_by_year_range":
                if (parser.getEndYear() >= parser.getStartYear()) {
                    resultList = neo4jQuery.findMoviesByYearRange(parser.getStartYear(), parser.getEndYear());
                } else {
                    System.out.println("Invalid year range!");
                }
                break;
            case "find_director_with_most_collaborations":
                if (parser.getActor() != null) {
                    resultList = neo4jQuery.findDirectorWithMostCollaborations(parser.getActor());
                } else {
                    System.out.println("No actor specified!");
                }
                break;
            case "recommend_similar_story" :
            VectorSearch vectorSearch = new VectorSearch(
                "movies_with_embeddings.json",
                "http://localhost:11434/api/embeddings"
            );
            String vectorResult = vectorSearch.searchSimilarMovie(userQuestion);
            System.out.println("----- Vector-based Similarity Result -----");
            System.out.println(vectorResult);
            System.out.println("--------------------------------------------");
            break;

            /*
             * case "find_movies_by_budget_range":
             * long minBudget = 1;
             * long maxBudget = 10;
             * resultList = neo4jQuery.findMoviesByBudgetRange(minBudget, maxBudget);
             * break;
             */

            default:
                System.out.println("Unknown or unsupported intent: " + intent);
                break;
        }

        return resultList;
    }
}
