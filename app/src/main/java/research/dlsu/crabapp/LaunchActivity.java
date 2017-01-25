package research.dlsu.crabapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;

public class LaunchActivity extends AppCompatActivity {

    private static int REQUEST_PERMISSION_CAMERA = 0;
    private static int REQUEST_PERMISSION_STORAGE = 1;
    private static int REQUEST_PERMISSION_DEVICEID = 2;
    private static final String FRAGMENT_DIALOG = "dialog";

    ImageView ivIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        ivIcon = (ImageView) findViewById(R.id.iv_icon);

        new ConfirmationDialog().show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
        if(sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, null) == null) {
            requestDeviceId();
        }else{
            moveToCameraActivity();
        }

    }

    public void saveDeviceIdToSharedPreferences(){
        SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
        if(sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, null) == null) {
            SharedPreferences.Editor spEditor = sp.edit();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            spEditor.putString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, telephonyManager.getDeviceId());
            spEditor.commit();
            Log.i("SERIAL NUMBER","Serial number is " + sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, "noneasdasd"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_DEVICEID && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            saveDeviceIdToSharedPreferences();
            moveToCameraActivity();
        }else{
            Snackbar.make(ivIcon, "We need this permission to be granted for the app to function.", Snackbar.LENGTH_LONG).show();
        }
    }

    public void moveToCameraActivity(){
        startActivity(new Intent(getBaseContext(), CameraActivity.class));
        finish();
    }

    public void requestDeviceId(){
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSION_DEVICEID
        );
    }

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("We need to acquire the device's serial number for identification.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    REQUEST_PERMISSION_DEVICEID);                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }
}
