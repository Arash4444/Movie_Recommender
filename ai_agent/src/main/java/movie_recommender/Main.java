package movie_recommender;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Neo4jMovieQuery neo4jQuery = new Neo4jMovieQuery("bolt://localhost:7687", "neo4j", "12345678");
        LlamaInputService llamaInput = new LlamaInputService();
        LlamaOutputService llamaOutput = new LlamaOutputService();


        try {
            System.out.print("Enter your question: ");
            String userQuestion = scanner.nextLine();
            QueryHandler queryHandler = new QueryHandler(neo4jQuery,userQuestion);
            String rawJson = llamaInput.sendPromptToLlama(userQuestion);
            System.out.println("Raw Json: " + rawJson);

            EntityParser parser = new EntityParser(rawJson);

            List<String> resultList = queryHandler.handleIntent(parser);

            if (resultList != null && !resultList.isEmpty()) {
                System.out.println("Found results: " + resultList);

                String dbContext = String.join("\n", resultList);
                try {
                    String finalAnswer = llamaOutput.getAnswerWithContext(userQuestion, dbContext);
                    System.out.println("_____________________Final Answer_____________________");
                    System.out.println(finalAnswer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("No results found or no valid intent.");
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            neo4jQuery.close();
        }
    }
}