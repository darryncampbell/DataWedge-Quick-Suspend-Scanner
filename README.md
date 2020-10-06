*Please be aware that this application / sample is provided as-is for demonstration purposes without any guarantee of support*
=========================================================

# DataWedge Quick Suspend Scanner

Most applications will need to temporarily disable the barcode scanner during their lifecycle.  There are **two** ways to achieve this with DataWedge, both using the [ScannerInputPlugin API](https://techdocs.zebra.com/datawedge/latest/guide/api/scannerinputplugin/): Enable / Disable or Resume / Suspend:

**Enable / Disable:**

_Enable / Disable can be called at any time_

```java
Intent dwIntent = new Intent();
dwIntent.setAction("com.symbol.datawedge.api.ACTION");
//  Enable
dwIntent.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "ENABLE_PLUGIN");
//  or Disable
dwIntent.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "DISABLE_PLUGIN");
sendBroadcast(dwIntent);
```

**Resume / Suspend:**

_Resume / Suspend is **much quicker** but Suspend can only be called when the scanner is in the `SCANNING` or `WAITING` state_

```java
Intent dwIntent = new Intent();
dwIntent.setAction("com.symbol.datawedge.api.ACTION");
//  Resume
dwIntent.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "RESUME_PLUGIN");
//  or Suspend
if (okToSuspend)
    dwIntent.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "SUSPEND_PLUGIN");
sendBroadcast(dwIntent);
```

You can determine the scanner state using the [GetScannerStatus](https://techdocs.zebra.com/datawedge/latest/guide/api/getscannerstatus/) or [RegisterForNotification](https://techdocs.zebra.com/datawedge/latest/guide/api/registerfornotification/) APIs:

```java
//  RegisterForNotification
@Override
protected void onResume()
{
    super.onResume();
    IntentFilter filter = new IntentFilter();
    filter.addAction("com.symbol.datawedge.api.NOTIFICATION_ACTION");
    filter.addCategory(Intent.CATEGORY_DEFAULT);
    registerReceiver(myBroadcastReceiver, filter);
}

private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("com.symbol.datawedge.api.NOTIFICATION_ACTION"))
        {
            if (intent.hasExtra("com.symbol.datawedge.api.NOTIFICATION"))
            {
                Bundle extras = intent.getBundleExtra("com.symbol.datawedge.api.NOTIFICATION");
                String notificationType = extras.getString("NOTIFICATION_TYPE");
                if (notificationType != null && notificationType.equals("SCANNER_STATUS"))
                {
                    //  We have received a change in Scanner status
                    String scannerStatus = extras.getString("STATUS");
                    if (scannerStatus.equals("WAITING") || scannerStatus.equals("SCANNING"))
                            okToSuspend = true;
                        else
                            okToSuspend = false;
                }
            }
        }
    }
};
```
A simple project to demonstrate these API calls is available from [https://github.com/darryncampbell/DataWedge-Quick-Suspend-Scanner](https://github.com/darryncampbell/DataWedge-Quick-Suspend-Scanner)

As can be seen from the gif below or from [this demo video](https://github.com/darryncampbell/DataWedge-Quick-Suspend-Scanner/blob/master/media/capture.mp4), **it is much quicker _resuming_ the scanner compared to _enabling_ it**.

![Demo gif](https://raw.githubusercontent.com/darryncampbell/DataWedge-Quick-Suspend-Scanner/master/media/capture.gif)
