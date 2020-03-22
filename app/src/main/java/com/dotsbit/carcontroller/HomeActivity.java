package com.dotsbit.carcontroller;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dotsbit.carcontroller.databinding.ActivityHomeBinding;
import com.dotsbit.carcontroller.databinding.SettingDialogBinding;

import java.io.IOException;
import java.time.chrono.MinguoChronology;
import java.util.UUID;

public class HomeActivity
        extends AppCompatActivity
{

    String address = null;
    TextView lumn;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ActivityHomeBinding mBinding;
    private int currentApiVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initControls();
    }

    private void initControls()
    {
        mBinding.connectToCar.setOnClickListener(view -> showDialog());
        mBinding.left.setOnClickListener(view -> sendSignal("L"));
        mBinding.right.setOnClickListener(view -> sendSignal("R"));
        mBinding.forward.setOnClickListener(view -> sendSignal("F"));
        mBinding.back.setOnClickListener(view -> sendSignal("B"));
        mBinding.carBreak.setOnClickListener(view -> sendSignal("S"));
        mBinding.horn.setOnClickListener(view -> sendSignal("H"));
        mBinding.speedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                mBinding.speed.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        hideNavBar();
    }

    private void showDialog()
    {

        final Dialog dialog = new Dialog(this);
        SettingDialogBinding filterDialogBinding = DataBindingUtil.inflate(
                LayoutInflater.from(this), R.layout.setting_dialog, null, false);
        dialog.setContentView(filterDialogBinding.getRoot());
        filterDialogBinding.connectToCar.setOnClickListener(
                view -> startActivityForResult(new Intent(this, DeviceList.class), 123));
        dialog.show();

    }

    private void hideNavBar()
    {
        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {
            getWindow().getDecorView()
                       .setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(
                            visibility -> {
                                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                                {
                                    decorView.setSystemUiVisibility(flags);
                                }
                            });
        }
    }

    private class ConnectBT
            extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(HomeActivity.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter()
                                    .cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                mBinding.connectionColor.setCardBackgroundColor(
                        getResources().getColor(android.R.color.holo_red_dark));
                mBinding.connectionText.setText("No connected");

                finish();
            }
            else
            {
                msg("Connected");
                isBtConnected = true;
                mBinding.connectionColor.setCardBackgroundColor(
                        getResources().getColor(android.R.color.holo_green_light));
                mBinding.connectionText.setText("Connected");

            }

            progress.dismiss();
        }
    }

    private void sendSignal(String number)
    {
        if (btSocket != null)
        {
            try
            {
                btSocket.getOutputStream()
                        .write(number.toString()
                                     .getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
        else
        {
            msg("Connect bluetooth first");
        }
    }

    private void Disconnect()
    {
        if (btSocket != null)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }

        finish();
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG)
             .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        address = data.getStringExtra(DeviceList.EXTRA_ADDRESS);
        new ConnectBT().execute();
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView()
                       .setSystemUiVisibility(
                               View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                       | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                       | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                       | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                       | View.SYSTEM_UI_FLAG_FULLSCREEN
                                       | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

}