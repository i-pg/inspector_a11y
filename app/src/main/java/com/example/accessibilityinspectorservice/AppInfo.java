package com.example.accessibilityinspectorservice;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public Drawable icon;
    public String applicationName;

    public AppInfo(){
        super();
    }

    public AppInfo(Drawable icon, String applicationName){
        super();
        this.icon = icon;
        this.applicationName = applicationName;
    }

}
