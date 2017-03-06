package research.dlsu.crabapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewCrabsActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_crabupdate";
    private RecyclerView rvAlbums;
    private AlbumAdapter albumAdapter;

    SwipeRefreshLayout swipeRefreshLayout;

    private boolean isFromChoosePhotoActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_albums);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), CameraActivity.class);
                finish();
                startActivity(intent);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        rvAlbums = (RecyclerView) findViewById(R.id.rv_albums);
        albumAdapter = new AlbumAdapter(getBaseContext(), null);

        albumAdapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(long id) {
                if (!isFromChoosePhotoActivity) {
                    // normal viewing
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), ViewCrabUpdateActivity.class);
                    intent.putExtra(EXTRA_ID, id);
                    Log.i("ViewCrabsActivity", "extra_id is " + id);
                    startActivity(intent);
                } else {
                    throw new UnsupportedOperationException("Well whaddya know. It was needed.");
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestResults();
            }
        });

        // GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(), 2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, true);
        rvAlbums.setLayoutManager(linearLayoutManager);
        rvAlbums.setAdapter(albumAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = getContentResolver().query(
                DatabaseContract.CrabUpdate.CONTENT_URI, null, null, null, null);
        albumAdapter.changeCursor(cursor);
        albumAdapter.notifyDataSetChanged();
    }

    public void requestResults(){
        String phoneIdString = new DatabaseHelper(getBaseContext()).getCrabUpdatesWithoutResults();
        if (phoneIdString != null) {
            new GetRequestResults().execute(phoneIdString);
        }else{
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(swipeRefreshLayout, "All crabs already have results.", Snackbar.LENGTH_LONG).show();
        }
    }

    public class GetRequestResults extends AsyncTask<String, Void, String> {

        String serialNumber = "";
        String phoneIdString = "";
        boolean hasResultToFetch = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
            serialNumber = sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, "");
            phoneIdString = new DatabaseHelper(getBaseContext()).getCrabUpdatesWithoutResults();
        }

        @Override
        protected String doInBackground(String... params) {
            phoneIdString = params[0]; // get phoneIdString as fetched in requestResults()
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = new FormBody.Builder()
                    .add(DatabaseContract.OnSiteUser.SERIALNUMBER, serialNumber)
                    .add(DatabaseContract.Crab.EXTRA_PHONEIDCRABSTRING, phoneIdString)
                    .build();

            Request request = new Request.Builder()
                    .url(RemoteServer.buildGetResultsUri(getIpAddress()))
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
            ArrayList<CrabUpdate> crabUpdateResults = new ArrayList<>();
            JSONArray crabResultsJson = null;
            if(s != null) {
                try {
                    crabResultsJson = new JSONArray(s);
                    for (int i = 0; crabResultsJson != null && i < crabResultsJson.length(); i++) {
                        CrabUpdate c = new CrabUpdate();
                        JSONObject jsonObject = crabResultsJson.getJSONObject(i);
                        c.setResult(jsonObject.getString(DatabaseContract.CrabUpdate.COLUMN_RESULT));
                        c.setId(jsonObject.getInt(DatabaseContract.CrabUpdate.PHONEIDCRABUPDATE));
                        crabUpdateResults.add(c);
                        //crabResults.add(c);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateCrabResults(crabUpdateResults);
            }else{
                Snackbar.make(rvAlbums, "No updates.", Snackbar.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    public void updateCrabResults(ArrayList<CrabUpdate> crabUpdateResults){
        new DatabaseHelper(getBaseContext()).updateCrabUpdateResults(crabUpdateResults);
        swipeRefreshLayout.setRefreshing(false);

        // Update recyclerview here
        albumAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View v = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog_ipaddress, null);
        final EditText etIpAddress = (EditText) v.findViewById(R.id.et_ipaddress);

        String ipaddress = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);

        if(ipaddress!=null){
            etIpAddress.setText(ipaddress);
        }else{
            etIpAddress.setText("");
        }


        int menuId = item.getItemId();
        switch(menuId){
            case R.id.ip_address:
                AlertDialog alertDialog = new AlertDialog.Builder(ViewCrabsActivity.this)
                        .setView(v)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                                    .edit()
                                    .putString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, etIpAddress.getText().toString())
                                    .commit();

                                // RemoteServer.IPADDRESS = etIpAddress.getText().toString();
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
