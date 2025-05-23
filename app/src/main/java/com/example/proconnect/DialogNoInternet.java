package com.example.proconnect;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DialogNoInternet {

    private static AlertDialog dialog;

    public static void showNoInternetDialog(Context context) {
        if (dialog != null && dialog.isShowing()) return;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_no_connection, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false);

        dialog = builder.create();

        Button btnReconnect = dialogView.findViewById(R.id.btnReconnect);
        btnReconnect.setOnClickListener(v -> {
            if (isNetworkAvailable(context)) {
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Still no connection", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                showNoInternetDialog(context); // Try again
            }
        });

        dialog.show();
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
