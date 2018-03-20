package com.govind.wheather.wheatherapp;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth firebaseAuth;
    private TextView textViewUserEmail;
    private Button buttonLogout;
    private DatabaseReference databaseReference;
    private EditText editTextName;
    private Button buttonAddPeople;
    private Button btnGetWeatherinfo;
    GPSTracker gps;


    TextView txtCity, txtLastUpdate, txtDescription, txtCelsius, txtMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);

        txtCity = (TextView) findViewById(R.id.txtCity);

        txtMain = (TextView) findViewById(R.id.txtMain);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtCelsius = (TextView) findViewById(R.id.txtCelsius);
        txtLastUpdate = (TextView) findViewById(R.id.txtLastUpdate);

        editTextName = (EditText) findViewById(R.id.editTextName);


        textViewUserEmail.setText("Welcome "+user.getEmail());

        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        buttonAddPeople = (Button) findViewById(R.id.buttonAddPeople);
        btnGetWeatherinfo = (Button) findViewById(R.id.btnGetWeatherinfo);


        buttonLogout.setOnClickListener(this);
        buttonAddPeople.setOnClickListener(this);

        btnGetWeatherinfo.setOnClickListener(this);
    }



    class WeatherInfo extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {

            try{
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);

                int data = reader.read();
                String apiDetails = "";
                char current;
                while(data != -1){
                    current = (char) data;
                    apiDetails += current;
                    data = reader.read();
                }
                return apiDetails;

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public void getWeatherInfo(){

        WeatherInfo weatherInfo = new WeatherInfo();
        try{

           /* if (cityName.getText().toString().isEmpty()){
                Toast.makeText(this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
                txtMain.setText("");
                txtDescription.setText("");

            }else {
*/

            gps = new GPSTracker(ProfileActivity.this);

            if (gps.canGetLocation()) {
                double lat = gps.getLatitude();
                double lon = gps.getLongitude();
                //Toast.makeText(getApplicationContext(), "LAT: " + lat + "LONG: " + lon, Toast.LENGTH_SHORT).show();
                String weatherApiDetails = weatherInfo.execute("http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&apikey=811f2257c228a0fbf874280626cbe6a0").get();

                //Log.i("Weather Api Info", weatherApiDetails);

                JSONObject json = new JSONObject(weatherApiDetails);


                txtCity.setText(json.getString("name") + ", " + json.getJSONObject("sys").getString("country"));

                JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                JSONObject main = json.getJSONObject("main");

                txtMain.setText(details.getString("main"));

                txtDescription.setText(
                        details.getString("description") +
                                "\n" + "Humidity: " + main.getString("humidity") + "%" +
                                "\n" + "Pressure: " + main.getString("pressure") + " hPa");

                txtCelsius.setText(
                        String.format("Temperature: "+"%.2f ℃", main.getDouble("temp")/10));

                DateFormat df = DateFormat.getDateTimeInstance();
                String updatedOn = df.format(new Date(json.getLong("dt")*1000));
                txtLastUpdate.setText("Last update: " + updatedOn);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveInformation(){

        String name = editTextName.getText().toString().trim();

        String cityName = txtCity.getText().toString().trim();
        String updateTime = txtLastUpdate.getText().toString().trim();
        String Description = txtDescription.getText().toString().trim();
        String temperature = txtCelsius.getText().toString().trim();
        String main = txtMain.getText().toString().trim();

        Information information = new Information(name,cityName,updateTime,Description,temperature,main);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        databaseReference.child(user.getUid()).setValue(information);

        Toast.makeText(this, "Information Saved...", Toast.LENGTH_LONG).show();

    }


    @Override
    public void onClick(View view){

        if (view == buttonLogout){
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        if (view == buttonAddPeople) {
            if (editTextName.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please Enter Your Name...", Toast.LENGTH_SHORT).show();
            } else {
                saveInformation();
            }
        }

        if (view == btnGetWeatherinfo){
            getWeatherInfo();
        }
    }
}
