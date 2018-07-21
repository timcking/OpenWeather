package com.timothyking.openweather;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ForecastActivity extends AppCompatActivity {

    public  static final String TAG  = "OpenWeather";
    // Temporary
    int[] intArrImages = {R.drawable.d01, R.drawable.n01, R.drawable.d02, R.drawable.n02, R.drawable.d03, R.drawable.d04, R.drawable.d01, R.drawable.d02, R.drawable.d03, R.drawable.n02, R.drawable.d03, R.drawable.d04, R.drawable.n01, R.drawable.n02};

    // ToDo, remove hardcoding, don't know if count will always be 40
    // ImageView[] intArrImages = new ImageView[40];
    String[] strArrCondTemp =  new String[40];
    String[] strArrDateTime = new String[40];

    ListView lView;
    ListAdapter lAdapter;
    Context context;

    public String getFormattedDate(long unixDate) {
        // ToDo, remove hard-coded timezone
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        Date date = new java.util.Date(unixDate*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("E MM/dd h:mm a");

        sdf.setTimeZone(tz);
        return sdf.format(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        context = getApplicationContext();

        Intent intent = getIntent();
        String placeName = (String) intent.getSerializableExtra(MainActivity.EXTRA_MESSAGE);
        String myURL = getString(R.string.urlForecast) + "q=" + placeName + ",US" + getString(R.string.urlEnd);

        DownloadTask task = new DownloadTask();
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

            } catch (MalformedURLException e) {
                Log.e(TAG, "error while fetching weather info (this should not happen really!)", e);
            } catch (IOException e) {
                Log.e(TAG, "error while fetching weather info", e);
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return result;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            int resultCount;

            try {
                JSONObject jsonObject = new JSONObject(result);
                resultCount = jsonObject.getInt("cnt");

                String forecastInfo = jsonObject.getString("list");

                JSONArray arr = new JSONArray(forecastInfo);

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);
                    int dateTime = jsonPart.getInt("dt");
                    String formatDate = getFormattedDate(dateTime);

                    JSONObject mainInfo = jsonPart.getJSONObject("main");
                    String temp = mainInfo.getString("temp");
                    // Format XX.XX to XX
                    temp = temp.replaceAll("\\..*$", "");

                    JSONArray weatherInfo = jsonPart.getJSONArray("weather");
                    // No need to loop through array, only one item
                    JSONObject jsonObj = weatherInfo.getJSONObject(0);
                    String iconName = jsonObj.getString("icon");
                    String condition = jsonObj.getString("main");

                    // Show temp on first line, condition + weather on second
                    strArrDateTime[i] = formatDate;
                    strArrCondTemp[i] = (condition + " " + temp + "\u00b0 F");

                    String url = ("http://openweathermap.org/img/w/" + iconName + ".png");

                    // Picasso.get().load(url).into(intArrImages[i]);
                }
            } catch (JSONException e) {
                Toast.makeText(ForecastActivity.this, "Not found, try another city or zip",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "error while fetching weather info", e);
            }

            // Setup list
            lView = (ListView) findViewById(R.id.listForecast);
            lAdapter = new ListAdapter(ForecastActivity.this, strArrDateTime, strArrCondTemp, intArrImages);
            lView.setAdapter(lAdapter);
        }
    }
}
