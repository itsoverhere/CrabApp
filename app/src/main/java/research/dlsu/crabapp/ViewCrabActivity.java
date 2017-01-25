package research.dlsu.crabapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewCrabActivity extends AppCompatActivity {

    public final static String EXTRA_ID_CRAB = "id_crab";

    RecyclerView rvUpdates;
    TextView tvFarmCity, tvLocation, tvStatus, tvId, tvWeight; // tvEntries
    ImageButton ibSync;
    UpdatesAdapter updatesAdapter;

    ProgressDialog progressDialog;

    private long id_crab;
    private long serverIdCrab = -1;
    Crab crab;
    CrabUpdate crabUpdate = null;

    boolean isSyncCrab = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_crab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvFarmCity = (TextView) findViewById(R.id.tv_farm_city);
        tvId = (TextView) findViewById(R.id.tv_id);
//        tvCity = (TextView) findViewById(R.id.tv_city);
//        tvEntries = (TextView) findViewById(R.id.tv_entries);
        tvWeight = (TextView) findViewById(R.id.tv_weight);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        tvStatus = (TextView) findViewById(R.id.tv_status);

        ibSync = (ImageButton) findViewById(R.id.button_sync);

        rvUpdates = (RecyclerView) findViewById(R.id.rv_updates);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Add new update here
                Intent intent = new Intent(getBaseContext(), ChoosePhotoActivity.class);
                intent.putExtra(EXTRA_ID_CRAB, id_crab);
                startActivity(intent);
            }
        });

        updatesAdapter = new UpdatesAdapter(getBaseContext(), null);

        id_crab = getIntent().getExtras().getLong(ViewAlbumsActivity.EXTRA_ID);

        updateCrabDetailsDisplay();

        updatesAdapter.setOnSyncListener(new UpdatesAdapter.OnSyncListener() {
            @Override
            public void onSync(CrabUpdate crabUpdate) {

                showProgressDialog();

                ViewCrabActivity.this.crabUpdate = crabUpdate;

                isSyncCrab = false;

//                int serveridcrab = new DatabaseHelper(getBaseContext())
//                        .getServerIdCrabWithIdCrab(crabUpdate.getIdCrab());
//
//                if (serveridcrab < 0) {
//                    // if not submit crab first,
//                    submitCrabToServer(crab);
//
//                    // make sure to submit crabupdate afterwards
////                    syncCrabUpdate(crabUpdate);
//                } else {
//                    // crab already exists submit crab update
//                    syncCrabUpdate(crabUpdate);
//                }
                syncCrabUpdate(crabUpdate);
            }
        });

        ibSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSyncCrab = true;

                boolean canSync = Boolean.parseBoolean(v.getTag(R.id.sync_enabled).toString());
                if (canSync) {
                    submitCrabToServer(crab);
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        rvUpdates.setLayoutManager(linearLayoutManager);
        rvUpdates.setAdapter(updatesAdapter);
    }

    public void syncCrabUpdate(CrabUpdate crabUpdate){
        // check if idcrab already exists in server
//        int serveridcrab = new DatabaseHelper(getBaseContext())
//                .getServerIdCrabWithIdCrab(crabUpdate.getIdCrab());

//        crabUpdate.setServerIdCrab(serveridcrab);

//        if(serveridcrab > 0) {
//            submitCrabUpdateToServer(crabUpdate);
//        }

        submitCrabUpdateToServer(crabUpdate);
    }


    public void submitCrabUpdateToServer(CrabUpdate crabUpdate){
        new SubmitCrabUpdateToServerTask().execute(crabUpdate);
    }

    public class SubmitCrabUpdateToServerTask extends AsyncTask<CrabUpdate, Void, String>{

        CrabUpdate crabUpdate;

        @Override
        protected String doInBackground(CrabUpdate... params) {
            crabUpdate = params[0];

            MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");

            OkHttpClient okHttpClient
                    = new OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .build();

            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeFile(crabUpdate.getPath());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            RequestBody requestBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(DatabaseContract.CrabUpdate.EXTRA_ID, String.valueOf(crabUpdate.getId()))
//                        .addFormDataPart(DatabaseContract.CrabUpdate.EXTRA_SERVERIDCRAB, String.valueOf(crabUpdate.getServerIdCrab()))
//                        .addFormDataPart(DatabaseContract.CrabUpdate.COLUMN_IDCRAB, String.valueOf(id_crab))
                        .addFormDataPart(DatabaseContract.CrabUpdate.COLUMN_DATE, String.valueOf(crabUpdate.getDate().getTime()))
                        .addFormDataPart(DatabaseContract.CrabUpdate.EXTRA_IMAGE, crabUpdate.getPath(),
                                RequestBody.create(MEDIA_TYPE_JPEG, byteArray))
                        .build();

            Request request = new Request.Builder()
                    .url(RemoteServer.buildInsertCrabUpdateUri(getIpAddress()))
                    .post(requestBody)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Long serverIdCrabUpdate = Long.parseLong(s);
                if(new DatabaseHelper(getBaseContext()).updateServerIdCrabUpdate(crabUpdate.getId(), serverIdCrabUpdate)){
                    Snackbar.make(tvFarmCity, "Success! Update " + crabUpdate.getId() + " has been synced.", Snackbar.LENGTH_SHORT);

                    updatesAdapter.notifyItemChanged(crabUpdate.getAdapterPosition());
                    progressDialog.dismiss();

                }else{
                    Snackbar.make(tvFarmCity, "Something went wrong during local database update, please try again.", Snackbar.LENGTH_SHORT);
                }
            }catch(NumberFormatException ex){
                Snackbar.make(tvFarmCity, "Something went wrong. Please try again.", Snackbar.LENGTH_SHORT);
            }
        }
    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    public class SubmitCrabToServerTask extends AsyncTask<Crab, Void, String>{

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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            long serverIdCrab = -1;
//            try{
//                serverIdCrab = Long.parseLong(s);
//                new DatabaseHelper(getBaseContext()).updateServerIdCrab(c.getId(), serverIdCrab);
//                ViewCrabActivity.this.serverIdCrab = serverIdCrab;
//                updateCrabDetailsDisplay();
//                if(!isSyncCrab) {
//                    syncCrabUpdate(crabUpdate);
//                }
//            }catch(NumberFormatException e){
//                e.printStackTrace();
//                Snackbar.make(tvFarmCity, "Error in adding crab to remote database.", Snackbar.LENGTH_SHORT).show();
//            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = getContentResolver().query(
                DatabaseContract.CrabUpdate.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(id_crab)).build(),
                null, null, null, null
        );

        updatesAdapter.changeCursor(cursor);
        updatesAdapter.notifyDataSetChanged();
    }

    public void updateCrabDetailsDisplay(){
        Cursor cursor = getContentResolver().query(
                DatabaseContract.Crab.buildCrabItemUri(id_crab),
                null, null, null, null
        );

        crab = cursorToCrab(cursor);

        tvFarmCity.setText(crab.getFarm() + ", " + crab.getCity());
        // tvCity.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_CITY)));

        tvLocation.setText(crab.getLatitude() + ", " + crab.getLongitude());
        tvWeight.setText("" + crab.getWeight());
        tvStatus.setText(crab.getStatus());
        tvId.setText("" + crab.getId());

        Log.i("tag", crab.getServerIdCrab() + "");

        if(crab.getServerIdCrab() > 0){
            ibSync.setImageResource(R.drawable.sync_64_gray);
            ibSync.setClickable(false);
            ibSync.setTag(R.id.sync_enabled, false);
        }else{
            ibSync.setImageResource(R.drawable.sync_64_green);
            ibSync.setClickable(true);
            ibSync.setTag(R.id.sync_enabled, true);
        }

//        tvEntries.setText(String.valueOf(new DatabaseHelper(getBaseContext()).getNumberOfUpdatesOfCrab(id_crab)));
    }

    public Crab cursorToCrab(Cursor cursor){
        Crab crab = new Crab();

        SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
        String serialNumber = sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, null);
        Log.i("SERIAL NUMBER 2 ITO UN","Serial number is " + sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, "noneasdasd"));

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
        crab.setServerIdCrab(cursor.getInt(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_SERVERIDCRAB)));

        return crab;
    }

    public void submitCrabToServer(Crab crab){
        new SubmitCrabToServerTask().execute(crab);
    }

    public void showProgressDialog(){
        progressDialog = ProgressDialog.show(ViewCrabActivity.this, "Uploading",
                "Sending crab to server. You can sync this later.", true, true);
    }

}
