package com.timothyking.openweather;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    ImageView iconWeather;
    TextView textWeather;
    TextView textZipCode;

    public void findWeatherZip(View view) {

        String zipCode = textZipCode.getText().toString();
        DownloadTask task = new DownloadTask();

        // Using string resources
        String myURL = getString(R.string.myURL1) + "zip=" + zipCode + getString(R.string.myURL2);
        task.execute(myURL);
    }

    public void findWeatherGeo(String lat, String lon) {

        DownloadTask task = new DownloadTask();

        // Using string resource
        String myURL = getString(R.string.myURL1) + "lat=" + lat + "&lon=" + lon + getString(R.string.myURL2);
        task.execute(myURL);
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                String weatherInfo = jsonObject.getString("weather");
                String humidity = jsonObject.getJSONObject("main").getString("humidity");
                String temp = jsonObject.getJSONObject("main").getString("temp");
                // Format XX.XX to XX
                String newTemp = temp.replaceAll("\\..*$", "");
                String place = jsonObject.getString("name");

                JSONArray arr = new JSONArray(weatherInfo);

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);
                    String weather = jsonPart.getString("main");
                    textWeather.setText(place + "\n" + newTemp + " \u00b0 F\n" + weather + "\n" +
                            humidity + "% Humidity");
                    String iconName = jsonPart.getString("icon");
                    Picasso.get().load("http://openweathermap.org/img/w/" + iconName + ".png").into(iconWeather);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iconWeather = findViewById(R.id.iconWeather);
        textWeather = findViewById(R.id.textWeather);
        textZipCode = findViewById(R.id.textZipCode);

        // ToDo replace with GetGPS
        findWeatherGeo("38.564215", "-121.413700");

        // Intent intent = new Intent(this, GetLocation.class);
        // startActivity(intent);
    }
}
