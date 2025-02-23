import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherApp extends Application {

    private static final String API_KEY = "1ef463dc592d56caef6b4137db663d74"; // Your API Key

    private Label weatherLabel = new Label("Enter a city and click 'Get Weather'");
    private ImageView weatherIcon = new ImageView();
    private TextField cityInput = new TextField();
    private VBox root = new VBox(10);

    @Override
    public void start(Stage stage) {
        cityInput.setPromptText("Enter city name...");
        Button fetchWeatherButton = new Button("Get Weather");
        fetchWeatherButton.setOnAction(e -> fetchWeatherData(cityInput.getText().trim()));

        weatherIcon.setFitWidth(100);
        weatherIcon.setFitHeight(100);

        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(cityInput, fetchWeatherButton, weatherIcon, weatherLabel);
        root.setStyle("-fx-padding: 20px; -fx-alignment: center; -fx-background-color: #ADD8E6;");

        Scene scene = new Scene(root, 400, 350);
        stage.setTitle("Weather App");
        stage.setScene(scene);
        stage.show();
    }

    private void fetchWeatherData(String city) {
        if (city.isEmpty()) {
            weatherLabel.setText("Please enter a city name!");
            return;
        }

        try {
            String urlString = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_KEY + "&units=metric";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            parseWeatherData(response.toString());
            parseForecastData(response.toString());

        } catch (Exception e) {
            weatherLabel.setText("City not found. Try again!");
        }
    }

    private void parseWeatherData(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        JSONObject currentWeather = json.getJSONArray("list").getJSONObject(0);
        String cityName = json.getJSONObject("city").getString("name");
        double temperature = currentWeather.getJSONObject("main").getDouble("temp");
        String description = currentWeather.getJSONArray("weather").getJSONObject(0).getString("description");
        String iconCode = currentWeather.getJSONArray("weather").getJSONObject(0).getString("icon");

        int humidity = currentWeather.getJSONObject("main").getInt("humidity");
        double windSpeed = currentWeather.getJSONObject("wind").getDouble("speed");
        int pressure = currentWeather.getJSONObject("main").getInt("pressure");

        String imageUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        weatherIcon.setImage(new Image(imageUrl));

        weatherLabel.setText("City: " + cityName + "\nTemperature: " + temperature + "°C\nCondition: " + description
                + "\nHumidity: " + humidity + "%\nWind Speed: " + windSpeed + " m/s\nPressure: " + pressure + " hPa");

        updateBackgroundColor(temperature);
    }

    private void parseForecastData(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        JSONArray forecastList = json.getJSONArray("list");

        StringBuilder forecastText = new StringBuilder("\n5-Day Forecast:\n");

        for (int i = 0; i < forecastList.length(); i += 8) { // Every 24 hours (~8 data points apart)
            JSONObject dayData = forecastList.getJSONObject(i);
            String date = dayData.getString("dt_txt").split(" ")[0];
            double temp = dayData.getJSONObject("main").getDouble("temp");
            String desc = dayData.getJSONArray("weather").getJSONObject(0).getString("description");

            forecastText.append(date).append(": ").append(temp).append("°C, ").append(desc).append("\n");
        }

        weatherLabel.setText(weatherLabel.getText() + "\n" + forecastText);
    }

    private void updateBackgroundColor(double temperature) {
        String color;
        if (temperature > 25) {
            color = "#FF5733"; // Hot (Orange/Red)
        } else if (temperature > 15) {
            color = "#FFD700"; // Warm (Yellow)
        } else if (temperature > 5) {
            color = "#87CEEB"; // Cool (Light Blue)
        } else {
            color = "#1E90FF"; // Cold (Dark Blue)
        }
        root.setStyle("-fx-padding: 20px; -fx-alignment: center; -fx-background-color: " + color + ";");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
