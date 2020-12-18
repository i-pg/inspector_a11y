// Nachlesen: https://stackoverflow.com/questions/18162931/get-selected-item-using-checkbox-in-listview
package com.example.accessibilityinspectorservice;

import android.app.ListActivity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.content.SharedPreferences.Editor;
import com.example.accessibilityserviceappv2.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.lang.String.valueOf;

public class AppListActivity extends ListActivity {

    ApplicationAdapter adapter ;
    AppInfo app_info[] ;
    int counter;
    final String sharedPrefLabel = "appsToExamine";
    private String selectedPackageName = "NoPackageSelected";
    Editor prefEditor;
    Button sendAppslistButton;
    String resultString;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        sendAppslistButton = (Button) findViewById(R.id.checkButton);

        final ListView listApplication = (ListView)findViewById(R.id.listView);
        sendAppslistButton= (Button) findViewById(R.id.checkButton);
        sendAppslistButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {

                StringBuilder result = new StringBuilder();
                for(int i=0;i<counter;i++)
                {
                    if(adapter.mCheckStates.get(i))
                    {

                        result.append(app_info[i].applicationName);
                        result.append(" ; ");
                    }

                }

                resultString = result.toString();

                writeStringToSharedContent(resultString);
                Toast.makeText(AppListActivity.this, result, Toast.LENGTH_SHORT).show();
            }

        });


        ApplicationInfo applicationInfo = getApplicationInfo();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> pInfo = new ArrayList<ApplicationInfo>();
        pInfo.addAll(pm.getInstalledApplications(PackageManager.GET_META_DATA));


        //Sortieren der gefundenen Applications
        Collections.sort(pInfo, new ApplicationInfo.DisplayNameComparator(pm));


        app_info = new AppInfo[pInfo.size()];

        counter = 0;
        for(ApplicationInfo item: pInfo){
            try{

                applicationInfo = pm.getApplicationInfo(item.packageName, 0);

                app_info[counter] = new AppInfo(pm.getApplicationIcon(applicationInfo),
                        valueOf(pm.getApplicationLabel(applicationInfo)));

            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }

            counter++;
        }

        adapter = new ApplicationAdapter(this, R.layout.row, app_info);
        listApplication.setAdapter(adapter);

    }

    public void writeStringToSharedContent(String resultString){
        prefEditor.putString(sharedPrefLabel, resultString);
        prefEditor.apply();
    }
}