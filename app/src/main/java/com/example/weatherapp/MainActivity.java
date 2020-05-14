package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText cityName;
    TextView resultText;

    public void checkWeather(View view) {

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        try {
            if (cityName.getText().toString().equals("")) {
                resultText.setVisibility(View.INVISIBLE);
                mgr.hideSoftInputFromWindow(cityName.getWindowToken(), 0);
                throw new Exception();
            }

            //removing accidental space if added by user into city name which causes failure of weather fetching
            String temp = cityName.getText().toString();
            if (temp.charAt(temp.length()-1) == ' ') {

                int index = temp.indexOf(' ');

                if (index != temp.length()-1) {
                    if(temp.charAt(index + 1) == ' ') {
                        temp = temp.substring(0, index);
                    } else {
                        int realIndex = temp.indexOf(' ', index + 1);
                        temp = temp.substring(0, realIndex);
                    }
                } else {
                    temp = temp.substring(0, index);
                }
                cityName.setText(temp);
            }
            int position = cityName.length();
            Editable city = cityName.getText();
            Selection.setSelection(city, position);

            //hide keyboard after entering city name
            mgr.hideSoftInputFromWindow(cityName.getWindowToken(), 0);

            //encoding not required afaik coz it worked without doing it as well
            String encodedCityName = URLEncoder.encode(cityName.getText().toString(), "UTF-8");

            DownloadTask task = new DownloadTask();
            task.execute("https://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&units=metric&APPID=ae989d951e10ce6494c0dacc10903145");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Please enter a valid city", Toast.LENGTH_SHORT).show();
            mgr.hideSoftInputFromWindow(cityName.getWindowToken(), 0);
        }
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

                String tempInfo = jsonObject.getString("main");
                JSONObject jsonTemp = new JSONObject(tempInfo);

                JSONArray jsonWeather = new JSONArray(weatherInfo);

                for(int i = 0; i<jsonWeather.length(); i++) {

                    JSONObject jsonPart = jsonWeather.getJSONObject(i);

                    String main = "", description = "", temp = "", feels_like = "", temp_min = "", temp_max = "";

                    main = jsonPart.getString("main");
                    description = jsonPart.getString("description");
                    temp = jsonTemp.get("temp").toString();
                    feels_like = jsonTemp.get("feels_like").toString();
                    temp_min = jsonTemp.get("temp_min").toString();
                    temp_max = jsonTemp.get("temp_max").toString();

                    if(main != "") {
                        message += main
                                + ": "
                                + description
                                + "\n\nTemperature: " + temp + " \u2103" +
                                "\n\nFeels Like: " + feels_like + " \u2103" +
                                "\n\nMax/Min: " + temp_max + "/" + temp_min + " \u2103";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        resultText = findViewById(R.id.resultText);
    }
}
