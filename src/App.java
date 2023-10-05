import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {
    static Dotenv dotenv = null;

    public static void main(String[] args) throws IOException {
        dotenv = Dotenv.configure()
                .directory("assets")
                .filename(".env")
                .load();

        String address;
        Scanner scanner = new Scanner(System.in);
        address = scanner.nextLine();
        while (!address.equals("Q") && !address.equals("q") && !address.equals("Й") && !address.equals("й")) {
            System.out.println(address);
            searchOrgHours(address);
            address = scanner.nextLine();
        }
    }

    private static void searchOrgHours(String address) throws IOException {
        String apikey = "apikey=" + App.dotenv.get("API_KEY");
        String geocode = "&text=" + encodeValue(address);
        String uri = "https://search-maps.yandex.ru/v1?" + apikey + "&type=biz&lang=ru_RU&format=json" + geocode;

        System.out.println(uri);

        String response = getResponse(uri);

        String hoursText = "организация не найдена";
        JSONArray features = new JSONObject(response).getJSONArray("features");
        if (features.length() > 0) {
            JSONObject hours = features.getJSONObject(0)
                    .getJSONObject("properties")
                    .getJSONObject("CompanyMetaData")
                    .getJSONObject("Hours");
            hoursText = hours.getString("text");
        }

        System.out.println(hoursText);
    }

    private static String getResponse(String uri) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.setRequestProperty("Content-Type", "application/json");

        InputStream responseStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);

        return bufferedReader.lines().collect(Collectors.joining(""));
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
