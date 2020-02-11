package com.darryncampbell.datawedgequicksuspendscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String ACTION_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
    private static final String EXTRA_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION";
    private static final String EXTRA_RESULT_NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private static final String EXTRA_KEY_VALUE_SCANNER_STATUS = "SCANNER_STATUS";
    private static final String EXTRA_KEY_VALUE_NOTIFICATION_STATUS = "STATUS";
    private static final String EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME = "PROFILE_NAME";
    private static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
    private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";
    private static final String EXTRA_REGISTER_NOTIFICATION = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_KEY_APPLICATION_NAME = "com.symbol.datawedge.api.APPLICATION_NAME";
    private static final String EXTRA_KEY_NOTIFICATION_TYPE = "com.symbol.datawedge.api.NOTIFICATION_TYPE";
    private static final String EXTRA_SCANNER_STATUS_WAITING = "WAITING";
    private static final String EXTRA_SCANNER_STATUS_SCANNING = "SCANNING";
    private static final String EXTRA_SCANNERINPUTPLUGIN = "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN";
    private final String EXTRA_PROFILE_NAME = "DW Quick Suspend Profile";
    private final String EXTRA_INTENT_ACTION = "com.zebra.quicksuspendscanner.ACTION";
    private final String LOG_TAG = "DW Quick Suspend";
    private boolean okToSuspend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CreateDWProfile();
        RegisterToReceiveScannerNotifications();
        Button btnEnablePlugin = findViewById(R.id.btnEnablePlugin);
        Button btnDisablePlugin = findViewById(R.id.btnDisablePlugin);
        Button btnResumePlugin = findViewById(R.id.btnResumePlugin);
        Button btnSuspendPlugin = findViewById(R.id.btnSuspendPlugin);
        btnEnablePlugin.setOnClickListener(this);
        btnDisablePlugin.setOnClickListener(this);
        btnResumePlugin.setOnClickListener(this);
        btnSuspendPlugin.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RESULT_NOTIFICATION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(EXTRA_INTENT_ACTION);
        registerReceiver(myBroadcastReceiver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(myBroadcastReceiver);
    }

    private void CreateDWProfile()
    {
        String profileName = EXTRA_PROFILE_NAME;
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_CREATE_PROFILE, profileName);

        //  Now configure that created profile to apply to our application
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", EXTRA_PROFILE_NAME);
        profileConfig.putString("PROFILE_ENABLED", "true"); //  Seems these are all strings
        profileConfig.putString("CONFIG_MODE", "UPDATE");

        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true"); //  This is the default but never hurts to specify
        Bundle barcodeProps = new Bundle();
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", getPackageName());      //  Associate the profile with this app
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
        //  You can only configure one plugin at a time, we have done the barcode input, now do the intent output
        profileConfig.remove("PLUGIN_CONFIG");
        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT");
        intentConfig.putString("RESET_CONFIG", "true");
        Bundle intentProps = new Bundle();
        intentProps.putString("intent_output_enabled", "true");
        intentProps.putString("intent_action", EXTRA_INTENT_ACTION);
        intentProps.putString("intent_delivery", "2");
        intentConfig.putBundle("PARAM_LIST", intentProps);
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
    }

    private void RegisterToReceiveScannerNotifications() {
        Bundle extras = new Bundle();
        extras.putString(EXTRA_KEY_APPLICATION_NAME, getPackageName()); //  The package name of this application
        extras.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS);  //  Register for changes to scanner status.
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_REGISTER_NOTIFICATION, extras);
    }

    private void sendDataWedgeIntentWithExtra(String action, String extraKey, String extraValue)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        this.sendBroadcast(dwIntent);
    }

    private void sendDataWedgeIntentWithExtra(String action, String extraKey, Bundle extras)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extras);
        this.sendBroadcast(dwIntent);
    }

    private void displayScanResult(Intent scanIntent)
    {
        String decodedSource = scanIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_source));
        String decodedData = scanIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        String decodedLabelType = scanIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));
        final TextView lblScanData = findViewById(R.id.lblScanData);
        lblScanData.setText(decodedData);
    }

    private void displayScanerStatus(String scannerStatus)
    {
        final TextView lblScannerStatus = findViewById(R.id.lblScannerStatus);
        lblScannerStatus.setText(scannerStatus);
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(EXTRA_INTENT_ACTION))
            {
                //  Received a barcode scan
                try {
                    displayScanResult(intent);
                }
                catch (Exception e)
                {
                    //  Catch if the UI does not exist when we receive the broadcast... this is not designed to be a production app
                }
            }
            else if (action.equals(ACTION_RESULT_NOTIFICATION))
            {
                //  6.3 API for RegisterForNotification
                if (intent.hasExtra(EXTRA_RESULT_NOTIFICATION))
                {
                    Bundle extras = intent.getBundleExtra(EXTRA_RESULT_NOTIFICATION);
                    String notificationType = extras.getString(EXTRA_RESULT_NOTIFICATION_TYPE);
                    if (notificationType != null && notificationType.equals(EXTRA_KEY_VALUE_SCANNER_STATUS))
                    {
                        //  We have received a change in Scanner status
                        String associatedProfile = extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME);
                        if (associatedProfile.equals(EXTRA_PROFILE_NAME))
                        {
                            String scannerStatus = extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_STATUS);
                            String userReadableScannerStatus = "Scanner status: " + scannerStatus;
                            if (scannerStatus.equals(EXTRA_SCANNER_STATUS_WAITING) || scannerStatus.equals(EXTRA_SCANNER_STATUS_SCANNING))
                                okToSuspend = true;
                            else
                                okToSuspend = false;
                            displayScanerStatus(scannerStatus);
                            Log.i(LOG_TAG, userReadableScannerStatus);
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDisablePlugin:
                sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SCANNERINPUTPLUGIN, "DISABLE_PLUGIN");
                break;
            case R.id.btnEnablePlugin:
                sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SCANNERINPUTPLUGIN, "ENABLE_PLUGIN");
                break;
            case R.id.btnSuspendPlugin:
                if (okToSuspend)
                    sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SCANNERINPUTPLUGIN, "SUSPEND_PLUGIN");
                break;
            case R.id.btnResumePlugin:
                sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SCANNERINPUTPLUGIN, "RESUME_PLUGIN");
                break;
        }
    }
}
