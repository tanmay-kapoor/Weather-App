package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText cityName;
    TextView resultText;

    public void checkWeather(View view) {

        //removing accidental space if added by user into city name which causes failure of weather fetching
        String temp = cityName.getText().toString();
        if (temp.charAt(temp.length()-1) == ' ') {

            temp = temp.substring(0, temp.length()-1);
            cityName.setText(temp);
        }

        //hide keyboard after entering city name
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(cityName.getWindowToken(), 0);

        //encoding not required afaik coz it worked without doing it as well
        try {
            String encodedCityName = URLEncoder.encode(cityName.getText().toString(), "UTF-8");

            DownloadTask task = new DownloadTask();
            task.execute("https://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&APPID=ae989d951e10ce6494c0dacc10903145");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Please enter a valid city", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        resultText = findViewById(R.id.resultText);
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        TextView resultText = findViewById(R.id.resultText);

        @Override
        protected String doInBackground(String... urls) {

            URL url;
            HttpURLConnection urlConnection;
            String result = "";

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1) {
                    result = result + (char) data;
                    data = reader.read();
                }
                return result;

            } catch (Exception e) {
                resultText.setVisibility(View.INVISIBLE);
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "Please enter a valid city", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try{
                String message = "";
                JSONObject jsonObject = new JSONObject(result);
                String weatherInfo = jsonObject.getString("weather");

                JSONArray arr = new JSONArray(weatherInfo);

                for(int i = 0; i<arr.length(); i++) {

                    JSONObject jsonPart = arr.getJSONObject(i);

                    String main = "";
                    String description = "";

                    main = jsonPart.getString("main");
                    description = jsonPart.getString("description");

                    if(main != "" && description != "") {
                        message += main + ": " + description + "\n";
                    }
                }

                if(message != "") {
                    resultText.setText(message);
                    resultText.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter a valid city", Toast.LENGTH_SHORT).show();
                }

            } catch(Exception e) {
                Toast.makeText(getApplicationContext(), "Please enter a valid city", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
