package com.example.accessibilityinspectorservice;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.example.accessibilityserviceappv2.R;


/*
 *  AccessNodeButton: Create for every Element in inspected App
 *  custom Button Object
 *  Buttons keeps Information from AccessibilityNode
 *
 */


public class AccessNodeButton extends androidx.appcompat.widget.AppCompatButton {

    Context context;
    View view;
    LayoutInflater layoutInflater;
    TextView infoboxText;

    // AccessNode Infos
    int btnCounter;
    String viewText;
    String contentDescription;
    String hintText;
    String labeledByElement;
    String appName;
    String className;
    Rect coordinates;


    //Default Constructor
    public AccessNodeButton(Context context) {
        this(context, 0, null, null, null, null, null, null, null);
    }

    //Constructor
    public AccessNodeButton(Context context, int btnCounter, String viewText, String contentDescription, String hintText, String labeledByElement, String appName, String className, Rect coordinates) {
        super(context);
        init();
        this.btnCounter = btnCounter;
        this.viewText = viewText;
        this.contentDescription = contentDescription;
        this.hintText = hintText;
        this.labeledByElement = labeledByElement;
        this.context = context;
        this.appName = appName;
        this.className = className;
        this.coordinates = coordinates;
    }

    private void init(){
        //connect to infobox
        if(context != null){
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.floatingwindow, null, false);
            infoboxText = findViewById(R.id.infoWindowText);
        }
    }

    /*
     *  Getter
     */
    public String getInformationString(){
        String InformationString = "<b>Element Nummer:</b> " + btnCounter + "<br><b>View Text: </b>" + viewText + "<br><b>Inhaltslabel: </b> " + contentDescription + "<br><b>Zugeh. Label: </b>"+ labeledByElement +  "<br><b>Type: </b>" + className;
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
    public String getClassName(){
        return className;
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
