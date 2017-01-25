package research.dlsu.crabapp;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewCrabUpdateActivity extends AppCompatActivity {

    private TextView tvId;
    private TextView tvDate;
    private TextView tvPath;
    private TextView tvType;
    private ImageButton buttonSync;
    private ImageView ivImage;
    ProgressDialog progressDialog;

    private CrabUpdate crabupdate;
    int viewWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_crab_update);

        tvId = (TextView) findViewById(R.id.tv_id);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvPath = (TextView) findViewById(R.id.tv_path);
        tvType = (TextView) findViewById(R.id.tv_type);
        buttonSync = (ImageButton) findViewById(R.id.button_sync);
        ivImage = (ImageView) findViewById(R.id.iv_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        ViewTreeObserver viewTreeObserver = ivImage.getViewTreeObserver();
//        if (viewTreeObserver.isAlive()) {
//            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    ivImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    viewWidth = ivImage.getWidth();
//
//                    getCrabUpdateId();
//                }
//            });
//        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        viewWidth = size.x;

        getCrabUpdateId();
    }

    public void getCrabUpdateId(){
        long idcrabupdate = getIntent().getExtras().getLong(ViewCrabsActivity.EXTRA_ID, -1);
        if(idcrabupdate==-1){
            finish();
        }else {
            populateData(idcrabupdate);
        }
    }

    public void populateData(long idcrabupdate){
        Cursor cursor = getContentResolver().query(
            DatabaseContract.CrabUpdate.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(idcrabupdate)).build(),
            null, null, null, null
        );


        if(cursor.moveToFirst()){
            crabupdate = cursorToCrabUpdate(cursor);

            tvId.setText(String.valueOf(crabupdate.getId()));
            tvPath.setText(crabupdate.getPath());
            tvType.setText(crabupdate.getCrabType().name());

            if(crabupdate.getServerIdCrabUpdate() != -1){
                // has already synced
                setButtonSync(true);
            }else{
                // hasn't synced
                setButtonSync(false);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(crabupdate.getDate());
            tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.get(Calendar.MONTH)+1 + " "
                    + calendar.get(Calendar.YEAR) + " || "
                    + calendar.get(Calendar.HOUR_OF_DAY)+ ":"
                    + calendar.get(Calendar.MINUTE));

            Log.i("tag", "IVIMAGE WIDTH IS  "+ ivImage.getWidth());

            if(viewWidth > 0) {
                Bitmap bitmap = decodeSampledBitmapFromPath(crabupdate.getPath(), viewWidth, -1);
                ivImage.setImageBitmap(bitmap);
            }
        }else{
            finish();
        }

    }

    public CrabUpdate cursorToCrabUpdate(Cursor cursor){

        CrabUpdate crabupdate = new CrabUpdate();

        Log.i("ViewCrabUpdateActivity", "cursor length is " + cursor.getCount());
        Log.i("ViewCrabUpdateActivity", "cursor column count length is " + cursor.getColumnCount());
        long id = cursor.getLong(cursor.getColumnIndex(DatabaseContract.CrabUpdate._ID));
        crabupdate.setId(id);
        crabupdate.setPath(cursor.getString(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_PATH)));
        crabupdate.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_DATE))));
        crabupdate.setServerIdCrabUpdate(cursor.getInt(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_SERVERIDCRABUPDATE)));

        String type = cursor.getString(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_CRABTYPE));
        crabupdate.setCrabType(CrabUpdate.CrabType.valueOf(type));

        return crabupdate;
    }

    // Bitmap related methods
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromPath(String path,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        // return BitmapFactory.decodeResource(res, resId, options);
        return BitmapFactory.decodeFile(path, options);
    }

    public class SubmitCrabUpdateToServerTask extends AsyncTask<CrabUpdate, Void, String>{

        CrabUpdate crabUpdate;

        @Override
        protected String doInBackground(CrabUpdate... params) {
            publishProgress();

            crabUpdate = params[0];

            SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
            String serialNumber = sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, null);

            MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");

            OkHttpClient okHttpClient
                    = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .build();

            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeFile(crabUpdate.getPath());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            if(bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                RequestBody requestBody =
                        new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart(DatabaseContract.CrabUpdate.EXTRA_ID, String.valueOf(crabUpdate.getId()))
                                //                        .addFormDataPart(DatabaseContract.CrabUpdate.EXTRA_SERVERIDCRAB, String.valueOf(crabUpdate.getServerIdCrab()))
                                //                        .addFormDataPart(DatabaseContract.CrabUpdate.COLUMN_IDCRAB, String.valueOf(id_crab))
                                .addFormDataPart(DatabaseContract.CrabUpdate.COLUMN_DATE, String.valueOf(crabUpdate.getDate().getTime()))
                                .addFormDataPart(DatabaseContract.CrabUpdate.COLUMN_CRABTYPE, crabUpdate.getCrabType().name())
                                .addFormDataPart(DatabaseContract.OnSiteUser.SERIALNUMBER, serialNumber)
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
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("crabupdateresult", "results are " + s);
            try {
                Long serverIdCrabUpdate = Long.parseLong(s);
                if(new DatabaseHelper(getBaseContext()).updateServerIdCrabUpdate(crabUpdate.getId(), serverIdCrabUpdate)){
                    Snackbar.make(ivImage, "Success! Update " + crabUpdate.getId() + " has been synced.", Snackbar.LENGTH_SHORT);
                    progressDialog.dismiss();
                    setButtonSync(true);
                }else{
                    Snackbar.make(ivImage, "Something went wrong during local database update, please try again.", Snackbar.LENGTH_SHORT);
                }
            }catch(NumberFormatException ex){
                Snackbar.make(ivImage, "Some" +
                        "}thing went wrong. Please try again.", Snackbar.LENGTH_SHORT);
            }
        }
    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    public void setButtonSync(boolean hasSynced){
        if (hasSynced) {
            buttonSync.setImageResource(R.drawable.sync_64_gray);
            buttonSync.setEnabled(false);
            buttonSync.setTag(R.id.sync_enabled, false);
            buttonSync.setOnClickListener(null);
        }else{
            buttonSync.setImageResource(R.drawable.sync_64_green);
            buttonSync.setEnabled(true);
            buttonSync.setTag(R.id.sync_enabled, true);
            buttonSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("display", "nowsending to server");
                    new SubmitCrabUpdateToServerTask().execute(crabupdate);
                }
            });
        }
    }

    public void showProgressDialog(){
        progressDialog = ProgressDialog.show(ViewCrabUpdateActivity.this, "Uploading",
                "Sending crab update to server. You can sync this later. Tap anywhere to dismiss.", true, true);
    }
}
