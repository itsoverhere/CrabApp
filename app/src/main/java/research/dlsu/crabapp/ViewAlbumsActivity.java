package research.dlsu.crabapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewAlbumsActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_crab";
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
                Intent intent = new Intent(getBaseContext(), AddNewCrabActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        rvAlbums = (RecyclerView) findViewById(R.id.rv_albums);
        albumAdapter = new AlbumAdapter(getBaseContext(), null);

        if (getIntent().hasExtra(ChoosePhotoActivity.EXTRA_FROM_CHOOSE_PHOTO)){
            isFromChoosePhotoActivity = true;
        }

        albumAdapter.setmOnLoadDataListener(new AlbumAdapter.OnLoadDataListener() {
            @Override
            public int onLoadDataNumUpdates(long id) {
//                return new DatabaseHelper(getBaseContext()).getNumberOfUpdatesOfCrab(id);
                return -1;
            }

            @Override
            public Date onLoadDataLastUpdate(long id) {
                return new Date(-1);
//                return new DatabaseHelper(getBaseContext()).getLastCrabUpdateOfCrab(id);
            }
        });

        albumAdapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(long id) {
                if (!isFromChoosePhotoActivity) {
                    // normal viewing
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), ViewCrabActivity.class);
                    intent.putExtra(EXTRA_ID, id);
                    startActivity(intent);
                } else {
                    // from choose photo activity, return chosen crab to ChoosePhotoActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_ID, id);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestResults();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        rvAlbums.setLayoutManager(linearLayoutManager);
        rvAlbums.setAdapter(albumAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = getContentResolver().query(
                DatabaseContract.Crab.CONTENT_URI, null, null, null, null);
        albumAdapter.swapCursor(cursor);
        albumAdapter.notifyDataSetChanged();
    }

    public void requestResults(){
        String phoneIdString = new DatabaseHelper(getBaseContext()).getCrabUpdatesWithoutResults();
        if (phoneIdString != null) {
            new GetRequestResults().execute();
        }else{
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class GetRequestResults extends AsyncTask<String, Void, String>{

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
            try {
                crabResultsJson = new JSONArray(s);
                for(int i=0; i<crabResultsJson.length(); i++){
                    CrabUpdate c = new CrabUpdate();
                    JSONObject jsonObject = crabResultsJson.getJSONObject(i);
                    c.setResult(jsonObject.getString(DatabaseContract.CrabUpdate.COLUMN_RESULT));
                    c.setId(jsonObject.getInt(DatabaseContract.CrabUpdate.EXTRA_ID));
                    crabUpdateResults.add(c);
                    // crabUpdateResults.add(c);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            updateCrabResults(crabUpdateResults);
        }
    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    public void updateCrabResults(ArrayList<CrabUpdate> crabUpdateResults){
        new DatabaseHelper(getBaseContext()).updateCrabUpdateResults(crabUpdateResults);
        swipeRefreshLayout.setRefreshing(false);
    }

}
