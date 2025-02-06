package movie_recommender;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;//AuthTokens;
/*import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;*/

import java.util.ArrayList;
import java.util.List;

public class Neo4jMovieQuery {
    private final Driver driver;

    public Neo4jMovieQuery(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void close() {
        driver.close();
    }

    private List<String> runQueryForStrings(String cypher, Value params, String fieldName) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                List<String> results = new ArrayList<>();
                Result result = tx.run(cypher, params);
                while (result.hasNext()) {
                    Record record = result.next();
                    results.add(record.get(fieldName).asString());
                }
                return results;
            });
        }
    }

    public List<String> findCastmates(String actorName) {
        String query = """
                    MATCH (a:Person {name: $actorName})-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(coActor:Person)
                    WHERE coActor <> a
                    RETURN DISTINCT coActor.name AS castmate
                    LIMIT 10
                """;
        return runQueryForStrings(query, Values.parameters("actorName", actorName), "castmate");
    }

    public List<String> recommendMoviesByGenre(String genre) {
        String cypher = """
                    MATCH (m:Movie)-[:HAS_GENRE]->(g:Genre {name: $genre})
                    RETURN m.title AS title
                     LIMIT 10
                """;
        return runQueryForStrings(cypher, Values.parameters("genre", genre), "title");
    }

    public List<String> findMoviesByActor(String actorName) {
        String cypher = """
                    MATCH (p:Person {name: $actorName})-[:ACTED_IN]->(m:Movie)
                    RETURN m.title AS title
                     LIMIT 10
                """;
        return runQueryForStrings(cypher, Values.parameters("actorName", actorName), "title");
    }

    public List<String> findMostExpensiveMovies(int limit) {
        String cypher = """
                    MATCH (m:Movie)
                    RETURN m.title AS title
                    ORDER BY m.budget DESC
                    LIMIT $lim
                """;
        return runQueryForStrings(cypher, Values.parameters("lim", limit), "title");
    }

    public List<String> recommendMoviesByDirector(String directorName) { // NNEEWW
        String cypher = """
                    MATCH (d:Person {name: $directorName})-[:DIRECTED]->(m:Movie)
                    RETURN m.title AS title
                    ORDER BY m.voteAverage DESC
                    LIMIT 10
                """;
        return runQueryForStrings(cypher, Values.parameters("directorName", directorName), "title");
    }

    public List<String> findMovieCast(String movieTitle) {
        String cypher = """
                    MATCH (p:Person)-[:ACTED_IN]->(m:Movie {title: $title})
                    RETURN p.name AS actorName

                """;
        return runQueryForStrings(cypher, Values.parameters("title", movieTitle), "actorName");
    }

    public List<String> findMovieDirector(String movieTitle) {
        String cypher = """
                    MATCH (p:Person)-[:DIRECTED]->(m:Movie {title: $title})
                    RETURN p.name AS directorName

                """;
        return runQueryForStrings(cypher, Values.parameters("title", movieTitle), "directorName");

    }

    public List<String> findHighestRevenueMoviesByGenre(String genre) {
        String cypher = """
                    MATCH (m:Movie)-[:HAS_GENRE]->(g:Genre {name: $genre})
                    RETURN m.title AS title
                    ORDER BY m.revenue DESC
                    LIMIT 10
                """;
        return runQueryForStrings(cypher, Values.parameters("genre", genre), "title");
    }

    public List<String> findTopRatedMovies(double minRating) {
        String cypher = """
                    MATCH (m:Movie)
                    WHERE m.voteAverage >= $minRating
                    RETURN m.title AS title
                    ORDER BY m.voteAverage DESC
                    LIMIT 10
                """;
        return runQueryForStrings(cypher, Values.parameters("minRating", minRating), "title");
    }

    public List<String> recommendMoviesBySameDirector(String movieTitle) {
        String cypher = """
                    MATCH (d:Person)-[:DIRECTED]->(m:Movie {title: $title})
                    WITH d
                    MATCH (d)-[:DIRECTED]->(other:Movie)
                    RETURN other.title AS title
                     LIMIT 10
                """;
        return runQueryForStrings(cypher, Values.parameters("title", movieTitle), "title");
    }


    public List<String> recommendMoviesByGenreAndMinRating(String genre, double minRating) {
        String cypher = """
                    MATCH (m:Movie)-[:HAS_GENRE]->(g:Genre {name: $genre})
                    WHERE m.voteAverage >= $minRating
                    RETURN m.title AS title
                    ORDER BY m.voteAverage DESC
                    LIMIT 10
                """;
        return runQueryForStrings(cypher, Values.parameters("genre", genre, "minRating", minRating), "title");
    }


    public List<String> findMoviesByYearRange(int startYear, int endYear) {
        String cypher = """
                    MATCH (m:Movie)
                    WHERE toInteger(substring(m.releaseDate,6,10)) >= $startYear
                      AND toInteger(substring(m.releaseDate,6,10)) <= $endYear
                    RETURN m.title AS title
                    ORDER BY m.releaseDate
                     LIMIT 5
                """;
        return runQueryForStrings(cypher, Values.parameters("startYear", startYear, "endYear", endYear), "title");
    }

    public List<String> findMoviesByBudgetRange(long minBudget, long maxBudget) {
        String cypher = """
                    MATCH (m:Movie)
                    WHERE m.budget >= $minBudget AND m.budget <= $maxBudget
                    RETURN m.title AS title
                    ORDER BY m.budget DESC
                     LIMIT 20
                """;
        return runQueryForStrings(cypher, Values.parameters("minBudget", minBudget, "maxBudget", maxBudget), "title");
    }


    public List<String> findDirectorWithMostCollaborations(String actorName) {
        String cypher = """
                    MATCH (a:Person {name: $actorName})-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(d:Person)
                    RETURN d.name AS directorName, COUNT(m) AS collaborations
                    ORDER BY collaborations DESC
                    LIMIT 1
                """;
        return runQueryForStrings(cypher, Values.parameters("actorName", actorName), "directorName");
    }

    public List<String> findMovieOverview(String movieTitle) {

        String cypher = """
                    MATCH (m:Movie {title: $title})
                    RETURN m.overview AS overview
                """;

        return runQueryForStrings(
                cypher,
                Values.parameters("title", movieTitle),
                "overview"
        );
    }

    public List<String> recommendSimilarMovies(String movieTitle) {

        String cypher = """
                MATCH (m:Movie {title: $title})-[:HAS_GENRE]->(g:Genre)
                WITH COLLECT(g) AS genres, SIZE(COLLECT(g)) AS genreCount
                MATCH (other:Movie)
                WHERE other.title <> $title
                AND other.voteAverage IS NOT NULL
                WITH other, genres, genreCount
                MATCH (other)-[:HAS_GENRE]->(og:Genre)
                WITH other, genres, genreCount, COLLECT(og) AS otherGenres, SIZE(COLLECT(og)) AS otherGenreCount
                WHERE otherGenres = genres AND otherGenreCount = genreCount
                RETURN other.title AS recommended
                ORDER BY other.voteAverage DESC
                LIMIT 3

                 """;
        return runQueryForStrings(cypher, Values.parameters("title", movieTitle), "recommended");
    }
}
