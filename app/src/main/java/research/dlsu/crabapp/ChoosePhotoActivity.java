package research.dlsu.crabapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

public class ChoosePhotoActivity extends AppCompatActivity {

    final static int REQUEST_CAPTURE_IMAGE = 0;
    final static int REQUEST_CHOOSE_FROM_GALLERY = 1;
    final static int REQUEST_CHANGE_CRAB = 2;
    final static int REQUEST_PERMISSION_CAMERA_STORAGE = 0;
    public static final String EXTRA_FROM_CHOOSE_PHOTO = "from_choose_photo";
    public static final String EXTRA_PATH = "path";

    Button buttonTakePicture;
    Button buttonChooseGallery;
    Button buttonSubmit;
    Button buttonChangeCrab;
    ImageView ivImage;
    TextView tvIdCrab;

    private long idChosenCrab = -1;
    private String chosenPath ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_photo);

        buttonTakePicture = (Button) findViewById(R.id.button_take_picture);
        buttonChooseGallery = (Button) findViewById(R.id.button_choose_gallery);
        buttonSubmit = (Button) findViewById(R.id.button_submit);
        buttonChangeCrab = (Button) findViewById(R.id.button_change_crab);
        ivImage = (ImageView) findViewById(R.id.iv_image);
        tvIdCrab = (TextView) findViewById(R.id.tv_id_crab);

        if(getIntent().hasExtra(ViewCrabActivity.EXTRA_ID_CRAB)){
            idChosenCrab = getIntent().getExtras().getLong(ViewCrabActivity.EXTRA_ID_CRAB);
            tvIdCrab.setText(String.valueOf(idChosenCrab));
        }

        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCaptureImageActivity();
            }
        });

        buttonChooseGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_FROM_GALLERY);
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEntrySufficient()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseContract.CrabUpdate.COLUMN_DATE, Calendar.getInstance().getTimeInMillis());
                    contentValues.put(DatabaseContract.CrabUpdate.COLUMN_PATH, chosenPath);
//                    contentValues.put(DatabaseContract.CrabUpdate.COLUMN_IDCRAB, idChosenCrab);

                    getContentResolver().insert(
                            DatabaseContract.CrabUpdate.CONTENT_URI,
                            contentValues
                    );

                    // show snackbar
                    Snackbar.make(v, "Image has been saved", Snackbar.LENGTH_LONG)
//                         .setAction("Action", null)
                            .show();

                    finish();
                }
            }
        });

        buttonChangeCrab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ViewAlbumsActivity.class);
                intent.putExtra(EXTRA_FROM_CHOOSE_PHOTO, true);
                startActivityForResult(intent, REQUEST_CHANGE_CRAB);
            }
        });

    }

    public boolean isEntrySufficient(){
        boolean result = true;
        String error = "";

        if(idChosenCrab == -1){
            result = false;
            error += "Please choose a crab to associate this image to. \n";
        }

        if(chosenPath.equals("")){
            result = false;
            error += "Please choose a picture. Either take a picture, or choose one from the gallery.";
        }

        if (!result) {
            Snackbar.make(buttonSubmit, error, Snackbar.LENGTH_LONG).show();
        }

        return result;
    }

    public void openCaptureImageActivity(){
        if(ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CAMERA_STORAGE);
        }else{
            startActivityForResult(new Intent(getBaseContext(), CaptureImageActivity.class), REQUEST_CAPTURE_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_CAMERA_STORAGE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            Snackbar.make(tvIdCrab, "Please try taking a picture again.", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK){
            String path = data.getExtras().getString(EXTRA_PATH);
//            Bitmap bmp = BitmapFactory.decodeFile(path);
//            uriChosen = Uri.parse(path);
            Log.i("TAG", ivImage.getWidth() + " height " + ivImage.getHeight());
            chosenPath = path;
            Bitmap bmp = decodeSampledBitmapFromPath(chosenPath, ivImage.getWidth(), ivImage.getHeight());
            ivImage.setImageBitmap(bmp);
//            ivImage.setImageURI(uriChosen);
        }else if(requestCode == REQUEST_CHOOSE_FROM_GALLERY && resultCode == RESULT_OK){
//            uriChosen = data.getData();
            chosenPath = getRealPathFromURI(data.getData());
            Bitmap bmp = decodeSampledBitmapFromPath(chosenPath, ivImage.getWidth(), ivImage.getHeight());
            ivImage.setImageBitmap(bmp);
        }else if(requestCode == REQUEST_CHANGE_CRAB && resultCode == RESULT_OK){
            idChosenCrab = data.getExtras().getLong(ViewAlbumsActivity.EXTRA_ID);
            tvIdCrab.setText(String.valueOf(idChosenCrab));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapDrawable bd = (BitmapDrawable)ivImage.getDrawable();
        bd.getBitmap().recycle();
        ivImage.setImageBitmap(null);
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
        final BitmapFactory.Options options = new BitmapFactory.Options();
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

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };

        //This method was deprecated in API level 11
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        CursorLoader cursorLoader = new CursorLoader(
                this,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    // TODO Add CrabUpdate to DB
    // TODO Save return id to local DB's SERVERIDCRABUPDATE
}
