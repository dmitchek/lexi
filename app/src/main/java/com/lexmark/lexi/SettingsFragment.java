package com.lexmark.lexi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by dmitchell on 4/8/2016.
 */
public class SettingsFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final View view = getActivity().getLayoutInflater().inflate(R.layout.settings, null);

        final TextView hostValue = (TextView)view.findViewById(R.id.hostValue);
        final TextView printerValue = (TextView)view.findViewById(R.id.printerValue);

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        hostValue.setText(settings.getString("serviceHost", ""));
        printerValue.setText(settings.getString("printer", ""));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String serviceHost = hostValue.getText().toString();
                        String printer = printerValue.getText().toString();

                        setSettings(serviceHost, printer);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void setSettings(String host, String printer)
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("serviceHost", host);
        editor.putString("printer", printer);

        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Settings saved", Toast.LENGTH_SHORT);
        toast.show();

        editor.commit();
    }
}
