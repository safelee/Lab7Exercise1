package th.ac.tu.siit.its333.lab7exercise1;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
public class MainActivity extends ActionBarActivity {
    //long currentTime = System.currentTimeMillis();
    // long currentTimeMin = TimeUnit.MILLISECONDS.toMinutes(currentTime);
    // long oldTime = 0;//System.currentTimeMillis();
    long oldTimeMinBK = 0;//TimeUnit.MILLISECONDS.toMinutes(oldTime);
    long oldTimeMinNON = 0;
    long oldTimeMinPT = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        // if(currentTimeMin-oldTimeMin>1){
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
        //}
    }
    public void buttonClicked(View v) {
        int id = v.getId();
        WeatherTask w = new WeatherTask();
        long currentTime = System.currentTimeMillis();
        long currentTimeMin = TimeUnit.MILLISECONDS.toMinutes(currentTime);
        switch (id) {
            case R.id.btBangkok:
                if(currentTimeMin-oldTimeMinBK>1) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    oldTimeMinBK = currentTimeMin;
                    oldTimeMinPT =0;
                    oldTimeMinNON =0;
                }
                break;
            case R.id.btNon:
                if(currentTimeMin-oldTimeMinNON>1) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    oldTimeMinNON = currentTimeMin;
                    oldTimeMinBK =0;
                    oldTimeMinPT =0;
                }break;
            case R.id.btPathum:
                if(currentTimeMin-oldTimeMinPT>1) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    oldTimeMinPT = currentTimeMin;
                    oldTimeMinBK =0;
                    oldTimeMinNON =0;
                }break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;
        double windSpeed;
        double temp;
        double temp_min;
        double temp_max;
        double humidity;
        String weather;
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }
        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();
                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    windSpeed = jWind.getDouble("speed");
                    JSONObject jMain = jWeather.getJSONObject("main");
                    temp = jMain.getDouble("temp");
                    temp = temp - 272.15;
                    temp_min = jMain.getDouble("temp_min");
                    temp_min = temp_min - 272.15;
                    temp_max = jMain.getDouble("temp_max");
                    temp_max = temp_max - 272.15;
                    humidity = jMain.getDouble("humidity");
                    JSONArray jweather = jWeather.getJSONArray("weather");
                    weather = jweather.getJSONObject(0).getString("main");
                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind, tvTemp, tvHumid;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvHumid = (TextView)findViewById(R.id.tvHumid);
            tvWind = (TextView)findViewById(R.id.tvWind);
            if (result) {
                tvTitle.setText(title);
                tvWind.setText(String.format("%.1f", windSpeed));
                tvWeather.setText(String.format("%s", weather));
                tvTemp.setText(String.format("%.1f(max=%.1f,min=%.1f)", temp,temp_max,temp_min));
                tvHumid.setText(String.format("%.1f%%", humidity));
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
            }
        }
    }
}