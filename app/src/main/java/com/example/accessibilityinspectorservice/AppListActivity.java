package com.example.accessibilityinspectorservice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import android.content.SharedPreferences.Editor;

import com.example.accessibilityserviceappv2.R;

import java.util.ArrayList;
import java.util.List;

public class AppListActivity extends ListActivity {

    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listadaptor = null;
    final String sharedPrefLabel = "appsToExamine";
    private String selectedPackageName = "NoPackageSelected";
    Editor prefEditor;
    Button sendAppslistButton;

    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        packageManager = getPackageManager();
        sendAppslistButton = (Button) findViewById(R.id.checkButton);

        new LoadApplications().execute();

        sendAppslistButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                writeStringToSharedContent();
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        CheckBox cb = (CheckBox) v.findViewById(R.id.cb_app);
        cb.setChecked(!cb.isChecked());

        ApplicationInfo app = applist.get(position);

        try {
            Intent intent = packageManager
                    .getLaunchIntentForPackage(app.packageName);

            if (null != intent) {
                //startActivity(intent);
                System.out.println("Package: " + app.packageName);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(AppListActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(AppListActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        cb.setChecked(cb.isChecked());

        if(selectedPackageName=="NoPackageSelected"){
            selectedPackageName = app.packageName;
        }
        else {
            selectedPackageName += app.packageName;
        }
        Toast.makeText(AppListActivity.this, app.packageName, Toast.LENGTH_SHORT).show();
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                    applist.add(info);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return applist;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            listadaptor = new ApplicationAdapter(AppListActivity.this,
                    R.layout.row, applist);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listadaptor);
            progress.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(AppListActivity.this, null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public void writeStringToSharedContent(){
        prefEditor.putString(sharedPrefLabel, selectedPackageName);
        prefEditor.apply();
    }

}
