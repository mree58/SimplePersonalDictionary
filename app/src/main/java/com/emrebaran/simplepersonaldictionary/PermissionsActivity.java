package com.emrebaran.simplepersonaldictionary;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by mree on 1/11/17.
 */

public class PermissionsActivity extends AppCompatActivity {


    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private SharedPreferences permissionStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_permissions);
        setTitle(getString(R.string.permissions_management));


        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);


    }



    public void checkPermissions(){
        if(ActivityCompat.checkSelfPermission(PermissionsActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(PermissionsActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(PermissionsActivity.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(PermissionsActivity.this,permissionsRequired[1])){

                AlertDialog.Builder builder = new AlertDialog.Builder(PermissionsActivity.this);
                builder.setTitle(getString(R.string.permissions_title));
                builder.setMessage(getString(R.string.permissions_message));
                builder.setPositiveButton(getString(R.string.permissions_grant), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(PermissionsActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(getString(R.string.permissions_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        proceedAfterPermission();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PermissionsActivity.this);
                builder.setTitle(getString(R.string.permissions_title));
                builder.setMessage(getString(R.string.permissions_message));
                builder.setPositiveButton(getString(R.string.permissions_grant), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), getString(R.string.permissions_goto), Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(getString(R.string.permissions_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        proceedAfterPermission();
                    }
                });
                builder.show();
            }  else {
                ActivityCompat.requestPermissions(PermissionsActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }


        } else {

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();

            proceedAfterPermission();
        }
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;


                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {

                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(permissionsRequired[0], true);
                editor.commit();

                proceedAfterPermission();

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionsActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(PermissionsActivity.this, permissionsRequired[1])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PermissionsActivity.this);
                builder.setTitle(getString(R.string.permissions_title));
                builder.setMessage(getString(R.string.permissions_message));
                builder.setPositiveButton(getString(R.string.permissions_grant), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(PermissionsActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(getString(R.string.permissions_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        proceedAfterPermission();

                    }
                });
                builder.show();
            } else {

                Toast.makeText(getBaseContext(), getString(R.string.permissions_unable), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                Toast.makeText(getBaseContext(), getString(R.string.permissions_goto), Toast.LENGTH_LONG).show();
                proceedAfterPermission();


            }
        }
    }






    private void proceedAfterPermission() {

        finish();

        Log.d("permission after",String.valueOf(permissionStatus.getBoolean(permissionsRequired[0],false)));

    }



    @Override
    public void onResume(){
        super.onResume();
        checkPermissions();
    }

}
