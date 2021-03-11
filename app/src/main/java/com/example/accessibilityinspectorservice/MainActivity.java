package com.example.accessibilityinspectorservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

import com.example.accessibilityserviceappv2.R;

import java.io.File;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);

        Button exportButton = (Button) findViewById(R.id.exportDataButton);
        Button showAppSelectorBtn = (Button) findViewById(R.id.appSelectorScreenBtn);
        Button openSettingsButton = (Button) findViewById(R.id.settingsButton);

        //Prüfe ob der Accessibility Service aktiviert ist
        if (this.checkSelfPermission(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)!= PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(this)
                    .setTitle("Inspector A11y Service aktivieren")
                    .setMessage("Starte in den Bedienungshilfen deines Smartphones den Inspector A11y Service um die App zu verwenden")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent openSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            openSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(openSettings);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        }

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                export();
            }
        });

        showAppSelectorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AppListActivity.class));
            }
        });

        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

    }



    public void export(){

        try{

            //exporting
            Context context = getApplicationContext();

            File filelocation = new File(getFilesDir(), "accessibility_report.txt");
            Uri path = FileProvider.getUriForFile(context, "com.example.accessibilityserviceappv2.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send Mail"));

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}