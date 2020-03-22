package com.dotsbit.carcontroller;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity
        extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dexter.withActivity(this)
              .withPermissions(
                      Manifest.permission.BLUETOOTH_ADMIN,
                      Manifest.permission.BLUETOOTH)
              .withListener(new MultiplePermissionsListener()
              {
                  @Override
                  public void onPermissionsChecked(MultiplePermissionsReport report)
                  {
                      // check if all permissions are granted
                      if (report.areAllPermissionsGranted())
                      {
                          // do you work now
                          new Handler().postDelayed(() -> {
                              startActivity(new Intent(MainActivity.this, HomeActivity.class));
                          }, 1000);
                      }

                      // check for permanent denial of any permission
                      if (report.isAnyPermissionPermanentlyDenied())
                      {
                          // permission is denied permenantly, navigate user to app settings
                      }
                  }

                  @Override
                  public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token)
                  {
                      token.continuePermissionRequest();
                  }
              })
              .onSameThread()
              .check();
    }

}
