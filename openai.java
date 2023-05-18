import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class openai {
    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openai80.p.rapidapi.com/chat/completions"))
                .header("content-type", "application/json")
                .header("X-RapidAPI-Key", "c697b5edefmshebd35848f98c33fp12a0a4jsn182976463e06")
                .header("X-RapidAPI-Host", "openai80.p.rapidapi.com")
                .POST(HttpRequest.BodyPublishers.ofString("{\r\n    \"model\": \"gpt-3.5-turbo\",\r\n \"messages\": [{\r\n \"role\": \"user\",\r\n \"content\": prompt}]}"))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
