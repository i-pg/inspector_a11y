package com.example.accessibilityinspectorservice;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.accessibilityserviceappv2.R;

public class AccessNodeButton extends androidx.appcompat.widget.AppCompatButton /*implements View.OnClickListener*/ {

    Context context;
    private static final String LOG_TAG = "ButtonActivity";
    View view;
    LayoutInflater layoutInflater;
    TextView testText;

    int btnCounter;
    String viewText;
    String contentDescription;
    String hintText;
    String labeledByElement;
    String appName;
    Rect coordinates;


    public AccessNodeButton(Context context) {
        this(context, 0, null, null, null, null, null, null);
    }

    public AccessNodeButton(Context context, int btnCounter, String viewText, String contentDescription, String hintText, String labeledByElement, String appName, Rect coordinates) {
        super(context);
        init();
        this.btnCounter = btnCounter;
        this.viewText = viewText;
        this.contentDescription = contentDescription;
        this.hintText = hintText;
        this.labeledByElement = labeledByElement;
        this.context = context;
        this.appName = appName;
        this.coordinates = coordinates;
    }

    private void init(){
        if(context != null){
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.floatingwindow, null, false);
            testText = findViewById(R.id.infoWindowText);
        }
        else {
            Log.v(LOG_TAG, "context empty");
        }
    }

    public String getInformationString(){
        String InformationString = "<b>Element Nummer:</b> " + btnCounter + "<br><b>View Text: </b>" + viewText + "<br><b>Inhaltslabel:  </b>" + contentDescription + "<br><b>Zugeh. Label: </b>"+ labeledByElement + "<br><b>Hint: </b>" + hintText;
        return InformationString;
    }

    public String getElementNumber(){

        return String.valueOf(btnCounter);

    }

    public int getElementInteger(){

        return btnCounter;

    }
    public String getElementText(){

        return viewText;

    }
    public String getElementContentDescription(){

        return contentDescription;
    }
    public String getElementHint(){
        return hintText;

    }

    public Rect getCoordinates(){
        return coordinates;
    }
    public String getLabeledByElement(){
        return labeledByElement;
    }


    public String getAppName(){
        return appName;
    }


}
