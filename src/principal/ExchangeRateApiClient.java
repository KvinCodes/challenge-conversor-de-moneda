package principal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ExchangeRateApiClient {
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6";
    private final HttpClient http;
    private final Gson gson;
    private final String apiKey; // cargada desde env o config

    public ExchangeRateApiClient(String apiKey) {
        this.http = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.apiKey = apiKey;
    }

    /**
     * Obtiene la tasa de conversión entre base y target.
     * Si amountOpt está presente, la API también devolverá conversion_result.
     */
    public Optional<Double> fetchPairRate(String base, String target, Double amountOpt) throws IOException, InterruptedException {
        String url = String.format("%s/%s/pair/%s/%s", BASE_URL, apiKey, base, target);
        if (amountOpt != null) {
            url = String.format("%s/%s/pair/%s/%s/%.8f", BASE_URL, apiKey, base, target, amountOpt);
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                // Si prefieres usar header auth en vez de embed en URL:
                // .header("Authorization", "Bearer " + apiKey)
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() / 100 != 2) {
            throw new IOException("HTTP error: " + resp.statusCode() + " - " + resp.body());
        }

        JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
        String result = root.has("result") ? root.get("result").getAsString() : "unknown";

        if ("error".equalsIgnoreCase(result)) {
            String errorType = root.has("error-type") ? root.get("error-type").getAsString() : "unknown-error";
            throw new IOException("API error: " + errorType);
        }

        if (root.has("conversion_rate")) {
            double rate = root.get("conversion_rate").getAsDouble();
            return Optional.of(rate);
        } else {
            return Optional.empty();
        }
    }

    // Ejemplo de uso rápido
    public static void main(String[] args) {
        String key = System.getenv("EXR_API_KEY"); // o lee config.properties
        if (key == null || key.isBlank()) {
            System.err.println("Por favor setea la variable de entorno EXR_API_KEY con tu API key.");
            return;
        }

        ExchangeRateApiClient client = new ExchangeRateApiClient(key);
        try {
            Optional<Double> rate = client.fetchPairRate("USD", "EUR", null);
            rate.ifPresentOrElse(
                    r -> System.out.println("Tasa USD->EUR = " + r),
                    () -> System.out.println("No se encontró conversion_rate en la respuesta")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
