package research.dlsu.crabapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AddNewCrabActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_LOCATION = 1;

    EditText etFarm;
    EditText etCity;
    EditText etWeight;
    TextView tvLocation;
    Button buttonLocate;
    Button buttonSubmit;

    ProgressBar progressBar;
    ProgressDialog progressDialog;

    private double latitude = 0;
    private double longitude = 0;

    GoogleApiClient googleApiClient;

    Uri uriNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_crab);

        etFarm = (EditText) findViewById(R.id.et_farm);
        etCity = (EditText) findViewById(R.id.et_city);
        etWeight = (EditText) findViewById(R.id.et_weight);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        buttonLocate = (Button) findViewById(R.id.button_locate);
        buttonSubmit = (Button) findViewById(R.id.button_submit);

        buttonLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleApiClient.connect();
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crab crab = submitEntryIntoTheDatabase();
                if(crab!=null) {
                    SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
                    crab.setSerialNumber(sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, ""));

                    if (crab.getSerialNumber() != null) {
                        submitCrabToServer(crab);
                    }

                    //                progressBar.setVisibility(View.VISIBLE);
                    showProgressDialog();
                }
            }
        });

        // setEditTextListeners();
    }

    public void showProgressDialog(){
        progressDialog = ProgressDialog.show(AddNewCrabActivity.this, "Uploading",
                "Sending crab to server. You can sync this later.", true, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient = new GoogleApiClient.Builder(getBaseContext()).
                addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        googleApiClient.connect();
    }

    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setNumUpdates(1);


            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                getLocationPermission();
                googleApiClient.disconnect();

                return;
            }else {;
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        googleApiClient,
                        locationRequest,
                        locationListener
                );
            }
        }

        @Override
        public void onConnectionSuspended(int i){
            Snackbar.make(buttonLocate, "Something went wrong. Please try again.", Snackbar.LENGTH_LONG).show();
        }
    };

    public void getLocationPermission(){
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSION_LOCATION
        );
    }

    GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Snackbar.make(buttonLocate, "Something went wrong. Please make sure location services is enabled.", Snackbar.LENGTH_LONG).show();

        }
    };

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            tvLocation.setText("Location is " + latitude + ", " + longitude);
            googleApiClient.disconnect();
        }
    };

    public Crab submitEntryIntoTheDatabase(){
        Crab crab = null;
        // if(isEntrySufficient()) {
        if(false){
            crab = userInputToCrab();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseContract.Crab.COLUMN_FARM, etFarm.getText().toString());
            contentValues.put(DatabaseContract.Crab.COLUMN_CITY, etCity.getText().toString());
            contentValues.put(DatabaseContract.Crab.COLUMN_LATITUDE, latitude);
            contentValues.put(DatabaseContract.Crab.COLUMN_LONGITUDE, longitude);
            contentValues.put(DatabaseContract.Crab.COLUMN_WEIGHT,
                    Double.parseDouble(etWeight.getText().toString()));

            try{
                uriNew = getContentResolver().insert(
                        DatabaseContract.Crab.CONTENT_URI,
                        contentValues
                );

                long idNewCrab = Long.parseLong(uriNew.getPathSegments().get(1));
                crab.setId(idNewCrab);

                Snackbar.make(buttonSubmit, "Crab successfully added to the local database.", Snackbar.LENGTH_SHORT).show();
            }catch (Exception ex){
                new AlertDialog.Builder(getBaseContext())
                        .setMessage("Something wrong happened. Please try again.")
                        .create()
                        .show();
            }
//            finish();
        }else{
            Snackbar.make(buttonSubmit, "Please see the errors above.", Snackbar.LENGTH_LONG).show();
        }

        return crab;
    }

    public Crab userInputToCrab(){
        Crab crab = new Crab();
        crab.setFarm(etFarm.getText().toString());
        crab.setCity(etCity.getText().toString());
        crab.setLatitude(latitude);
        crab.setLongitude(longitude);
        crab.setWeight(Double.parseDouble(etWeight.getText().toString()));

        return crab;
    }

    public void submitCrabToServer(Crab crab){
        new SubmitCrabToServerTask().execute(crab);
    }

    public class SubmitCrabToServerTask extends AsyncTask<Crab, Void, String> {

        Crab c = null;

        @Override
        protected String doInBackground(Crab... params) {
            OkHttpClient okHttpClient
                    = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .build();

            c = params[0];

            RequestBody requestBody
                    = new FormBody.Builder()
                    .add(DatabaseContract.Crab.COLUMN_CITY, c.getCity())
                    .add(DatabaseContract.Crab.COLUMN_FARM, c.getFarm())
                    .add(DatabaseContract.Crab.COLUMN_LATITUDE, String.valueOf(c.getLatitude()))
                    .add(DatabaseContract.Crab.COLUMN_LONGITUDE, String.valueOf(c.getLongitude()))
                    .add(DatabaseContract.Crab.COLUMN_WEIGHT, String.valueOf(c.getWeight()))
                    .add(DatabaseContract.Crab.EXTRA_ID, String.valueOf(c.getId()))
                    .add(DatabaseContract.OnSiteUser.SERIALNUMBER, c.getSerialNumber())
                    .build();

            Request request
                    = new Request.Builder()
                    .url(RemoteServer.buildInsertCrabUri(getIpAddress()))
                    .post(requestBody)
                    .build();

            Response response = null;
            try {
                response = okHttpClient.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            long serverIdCrab = -1;
//            try{
//                serverIdCrab = Long.parseLong(s);
//                new DatabaseHelper(getBaseContext()).updateServerIdCrab(c.getId(), serverIdCrab);
//                Snackbar.make(buttonSubmit, "Crab successfully added to the remtoe database.", Snackbar.LENGTH_SHORT).show();
//
//                finish();
//
////                progressBar.setVisibility(View.GONE);
//                progressDialog.dismiss();
//
//                long idNewCrab = Long.parseLong(uriNew.getPathSegments().get(1));
//                Intent intent = new Intent(getBaseContext(), ViewCrabActivity.class);
//                intent.putExtra(ViewAlbumsActivity.EXTRA_ID, idNewCrab);
//                startActivity(intent);
//            }catch(NumberFormatException e){
//                e.printStackTrace();
//                Snackbar.make(buttonSubmit, "Error in adding crab to remote database.", Snackbar.LENGTH_SHORT).show();
//            }
//        }
    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    public Crab cursorToCrab(Cursor cursor){
        Crab crab = new Crab();

        SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
        String serialNumber = sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, null);

        long id = cursor.getLong(cursor.getColumnIndex(DatabaseContract.Crab._ID));
        crab.setId(id);
//        crab.setTag(cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_TAG)));
        crab.setStatus(cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_STATUS)));
        crab.setSerialNumber(serialNumber);
        crab.setCity(cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_CITY)));
        crab.setFarm(cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_FARM)));
        crab.setLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_LATITUDE)));
        crab.setLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_LONGITUDE)));
        crab.setWeight(cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_WEIGHT)));

        return crab;
    }

    public boolean isEntrySufficient(){

        boolean result = true;

        if(etFarm.getText().toString().trim().isEmpty()){
            etFarm.setError("Farm may not be empty.");
            result = false;
        }

        if(etCity.getText().toString().trim().isEmpty()){
            etCity.setError("City may not be empty.");
            result = false;
        }

        if(etWeight.getText().toString().trim().isEmpty()){
            etWeight.setError("Weight may not be empty.");
            result = false;
        }

        return result;
    }

    public void setEditTextListeners(){
        etFarm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(etFarm.getText().toString().trim().isEmpty()){
                    etFarm.setError(null);
                }
            }
        });

        etCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(etCity.getText().toString().trim().isEmpty()){
                    etCity.setError(null);
                }
            }
        });

        etWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(etWeight.getText().toString().trim().isEmpty()){
                    etWeight.setError(null);
                }
            }
        });
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == REQUEST_PERMISSION_LOCATION
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//            googleApiClient.connect();
//        }else{
//            Snackbar.make(buttonLocate, "We need your permission to enable location services.", Snackbar.LENGTH_LONG).show();
        }
    }
}
