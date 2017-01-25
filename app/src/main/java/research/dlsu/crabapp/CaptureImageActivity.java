package research.dlsu.crabapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureImageActivity extends Activity implements SurfaceHolder.Callback {

    final String TAG = "CAMERA APP";

    private String mCurrentPhotoPath;
    private String podname;
    private String version;

    static Camera camera = null;
    SurfaceView surfaceView = null;
    Button buttonCapture = null;
    SurfaceHolder surfaceHolder = null;
    ImageView ivTemplate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        getWindow().setFormat(PixelFormat.UNKNOWN);

        surfaceView = (SurfaceView) findViewById(R.id.surface);
        buttonCapture = (Button) findViewById(R.id.capture);
        ivTemplate = (ImageView) findViewById(R.id.ivtemplate);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        Bitmap templateImage = ((BitmapDrawable)ivTemplate.getDrawable()).getBitmap();

        DisplayMetrics display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        Log.i("Touch", "templateImage width and height " + templateImage.getWidth() + ", " + templateImage.getHeight());

        float screenwidthheightratio = display.widthPixels/display.heightPixels;
        float templatewidthheightratio = templateImage.getWidth()/ templateImage.getHeight();

        float ratio = 1f;
        float newHeight = 1f, newWidth = 1f;
        if(templatewidthheightratio > screenwidthheightratio){
            ratio = (float) display.widthPixels / (float) templateImage.getWidth();
            newHeight = templateImage.getHeight() * ratio;
            newWidth = display.widthPixels;
            Log.i("Touch", "Follow width Ratio is " + ratio);
        }else{
            ratio = (float) display.heightPixels / (float) templateImage.getHeight();
            newWidth = templateImage.getWidth() * ratio;
            newHeight = display.heightPixels;
            Log.i("Touch", "Follow height Ratio is " + ratio);
        }

        Bitmap bmp = Bitmap.createScaledBitmap(templateImage, Math.round(newWidth), Math.round(newHeight), false);
//        templateImage = decodeSampledBitmapFromResource(getResources(), R.mipmap.ic_launcher, display.widthPixels, display.heightPixels);

        ivTemplate.setImageBitmap(bmp);
        ivTemplate.setAlpha(0.3f);

//        camera.release();
//        camera = null;

        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                camera.takePicture(null, null, callBack);
                Toast.makeText(getBaseContext(), "Photo saved.", Toast.LENGTH_SHORT).show();
            }
        });

    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        Bitmap templateImage = decodeSampledBitmapFromResource(getResources(), R.mipmap.ic_launcher, ivTemplate.getWidth(), ivTemplate.getHeight());
//        ivTemplate.setImageBitmap(templateImage);
//        ivTemplate.setAlpha(0.3f);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if(camera != null){
            camera.release();
            Log.i(TAG, "Camera was not null, now set to null");
        }

        camera = Camera.open();

        camera.setDisplayOrientation(90);
    }

    Camera.PictureCallback callBack = new Camera.PictureCallback(){

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            // get the file
            File file = null;
            try {
                file = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            String uri = MediaStore.Images.Media.insertImage(
                    getContentResolver(), bitmap,  System.currentTimeMillis() + "-crab", ""
            );
            */

            // save bytes to file
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            // camera.startPreview();

            // TODO: Intent
            Intent intent = new Intent();
            intent.putExtra(ChoosePhotoActivity.EXTRA_PATH, file.getAbsolutePath());
            setResult(RESULT_OK, intent);

            finish();
        }

    };

    @Override
    protected void onPause() {
        super.onPause();
//        if(camera != null){
//            camera.release();
//            camera = null;
//        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File folder = new File(Environment.getExternalStorageDirectory(), "Crab");
        boolean folderExists = true;
        if(!folder.exists()){
            if(!folder.mkdir()){
                Log.i(TAG, "Error in making Cacao directory");
                folderExists = false;
            }
        }

        if(folderExists) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = timeStamp + "_CRAB_" + podname + "_" + version;
//            File storageDir = Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    folder      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }

        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
//		camera = Camera.open();
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    // Bitmap methods
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

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
