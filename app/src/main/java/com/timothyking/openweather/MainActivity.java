package com.timothyking.openweather;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    Context context;
    public static final String EXTRA_MESSAGE = "com.timothyking.openweather.MESSAGE";
    private static final int GETGPS_REQUEST_CODE = 0;
    public  static final String TAG  = "OpenWeather";
    ImageView iconWeather;
    TextView textWeather;
    TextView textCity;
    TextView textSunrise;
    TextView textSunset;
    Button buttonSearch;
    String placeName;

    public String getFormattedDate(long unixDate) {
        // ToDo, remove hard-coded timezone
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        Date date = new java.util.Date(unixDate*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("h:mm:ss a");
        sdf.setTimeZone(tz);
        return sdf.format(date);
    }

    public void findWeatherGeo(String lat, String lon) {
        DownloadTask task = new DownloadTask();

        // Using string resource
        String myURL = getString(R.string.urlSearch) + "lat=" + lat + "&lon=" + lon + getString(R.string.urlEnd);
        task.execute(myURL);
    }

    public void findWeatherCity(View view) {
        // Also takes zip code
        String city = textCity.getText().toString();

        DownloadTask task = new DownloadTask();

        // Using string resource
        String myURL = getString(R.string.urlSearch) + "q=" + city + ",US" + getString(R.string.urlEnd);
        task.execute(myURL);
    }

    public void getForecast(View view) {
        // Call new activity
        Intent intent = new Intent(this, ForecastActivity.class);
        intent.putExtra(EXTRA_MESSAGE, placeName);
        startActivity(intent);
    }

    public void enableSubmitIfReady() {
        boolean isReady = textCity.getText().toString().length() > 2;
        buttonSearch.setEnabled(isReady);
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

            } catch (MalformedURLException e) {
                Log.e(TAG, "Error while fetching weather info (this should not happen really!)", e);
            } catch (IOException e) {
                Log.e(TAG, "Error while fetching weather info", e);
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return result;
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

                // Save as global variable to  use with Forecast
                placeName = place;

                long sunset = jsonObject.getJSONObject("sys").getLong("sunset");
                long sunrise = jsonObject.getJSONObject("sys").getLong("sunrise");

                // Display sunrise/sunset
                String formatSunrise = getFormattedDate(sunrise);
                textSunrise.setText(formatSunrise);
                String formatSunset = getFormattedDate(sunset);
                textSunset.setText(formatSunset);

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
                Toast.makeText(MainActivity.this, "Not found, try another city or zip", Toast.LENGTH_LONG).show();
                Log.e(TAG, "error while fetching weather info", e);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        iconWeather = findViewById(R.id.iconWeather);
        textWeather = findViewById(R.id.textWeather);
        textCity = findViewById(R.id.textCity);
        textSunrise = findViewById(R.id.textSunrise);
        textSunset = findViewById(R.id.textSunset);
        buttonSearch = findViewById(R.id.buttonSearch);
        enableSubmitIfReady();

        textCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                enableSubmitIfReady();
            }
        });

        Intent intent = new Intent(this, GetGPS.class);
        startActivityForResult(intent, GETGPS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == GETGPS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                String returnString = data.getStringExtra("keyGPS");

                // Log.i("***** GPS *****", returnString);

                String[] geoArray = returnString.split(",");
                findWeatherGeo(geoArray[0], geoArray[1]);
            }
        }
    }
}
