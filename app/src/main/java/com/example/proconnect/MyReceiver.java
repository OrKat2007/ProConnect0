package com.example.proconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.TextView;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    private TextView tv;

    // Constructor receives a TextView reference so we can update it.
    public MyReceiver(TextView tv) {
        this.tv = tv;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the current battery level from the Intent
        int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int batteryChargingState = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        // Show some Toast messages (what your teacher suggests)
        Toast.makeText(context, "Battery level: " + batteryLevel + "%", Toast.LENGTH_SHORT).show();
        Toast.makeText(context, "Battery charging state: " + batteryChargingState, Toast.LENGTH_SHORT).show();

        // Optionally update the TextView with the battery level
        tv.setText("Battery Level: " + batteryLevel + "%");
    }
}
