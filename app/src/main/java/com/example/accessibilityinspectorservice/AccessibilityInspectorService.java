package com.example.accessibilityinspectorservice;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.example.accessibilityserviceappv2.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AccessibilityInspectorService extends AccessibilityService {

    LinearLayout lLayout;
    private static final String LOG_TAG = "Accessibility Inspector Service";
    public static String appname = "not set";
    int nodeCounter;
    View floatingInfobox;
    String viewElementDataString;

    Boolean floatingInfoBoxIsSet = false;
    WindowManager wm;

    // Liste in der alle viewElementInformationButtons gespeichert werden
    List <CustomButton> nodeButtonsList;
    Button shareButton;
    String appName;
    String sharedPrefsHolder;
    final String sharedPrefLabel = "appsToInspect";


    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPrefsHolder = prefs.getString(sharedPrefLabel, "defaultStringIfNothingFound");
        String splitHelperString = sharedPrefsHolder;
        List<String> appsWhitelist = Arrays.asList(splitHelperString.split(";"));


        nodeCounter = 1;
        String currentPackageName = "init text";


        if (e.getPackageName()!=null) {

            currentPackageName = e.getPackageName().toString();


            switch (e.getEventType()) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {

                    if(appsWhitelist.contains(currentPackageName)){
                        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                        showFloatingInfoWindow("Klicke auf eines der Elemente");
                        nodeButtonsList = new ArrayList();
                        appName =  e.getPackageName().toString();
                        logNodeHierarchy(getRootInActiveWindow(), 0);
                        addShareButton();
                    }

                    else if (!e.getPackageName().equals("com.example.accessibilityserviceappv2") && !appsWhitelist.contains(e.getPackageName().toString())) {
                        removeWindows();
                    }

                }

                case AccessibilityEvent.TYPE_VIEW_SCROLLED:{
                    //ToDo: wm.updateViewLayout();
                }
            }

        }

    }


    @Override
    public void onInterrupt() {
        Log.i("Interrupt", "Interrupt");
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i("Service", "Connected");
    }


    public void logNodeHierarchy(AccessibilityNodeInfo nodeInfo, int depth) {

        Rect rect = new Rect();
        String contentDescription;
        String viewText;
        String hintText;
        String labeledByElement;
        String logString = "";

        if (nodeInfo == null) return;

        for (int i = 0; i < depth; ++i) {
            logString += " ";
        }

        //Koordinaten der Node:
        nodeInfo.getBoundsInScreen(rect);
        Context context = getApplicationContext();

        logString += "\nElement: " + nodeCounter + "\n Text: " + nodeInfo.getText() + "\n" + " Content-Description: " + nodeInfo.getContentDescription() + "\n App Name: " + appname + "\n Koordinaten " +rect + "\n Hint " + nodeInfo.getHintText() + "\n Labeled By " + nodeInfo.getLabeledBy();
        Log.v(LOG_TAG, logString);

        WindowManager.LayoutParams nodeLayoutParams = new WindowManager.LayoutParams();

        nodeLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        nodeLayoutParams.format = PixelFormat.TRANSLUCENT;
        nodeLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        nodeLayoutParams.width = rect.width();
        nodeLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        nodeLayoutParams.gravity = Gravity.TOP | Gravity.START;
        nodeLayoutParams.x = rect.left;
        nodeLayoutParams.y = rect.top - 70;

        if(nodeInfo.getText()!=null){
            viewText = nodeInfo.getText().toString();
        }
        else {
            viewText = "keiner";
        }

        if(nodeInfo.getContentDescription()!=null){
            contentDescription = nodeInfo.getContentDescription().toString();
        }
        else {
            contentDescription = "keines";
        }

        if(nodeInfo.getHintText()!=null){
            hintText = nodeInfo.getHintText().toString();
        }

        else {
            hintText = "keiner";
        }

        if(nodeInfo.getLabeledBy()!=null){
            labeledByElement = nodeInfo.getLabeledBy().getText().toString();
        }

        else {
            labeledByElement = "kein Label";
        }

        CustomButton nodeInfoButton = new CustomButton(context, nodeCounter, viewText, contentDescription, hintText, labeledByElement, appName);
        nodeInfoButton.setText(String.valueOf(nodeCounter));
        //ToDo: accessibility Richtlinien auch bei Service:
        //nodeInfoButton.setContentDescription("auto button");
        nodeInfoButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        nodeInfoButton.setPadding(20,30,20,20);

        ShapeDrawable nodeInfoButtonShape = new ShapeDrawable();
        nodeInfoButtonShape.setShape(new RectShape());
        nodeInfoButtonShape.getPaint().setColor(Color.BLACK);
        nodeInfoButtonShape.getPaint().setStrokeWidth(10f);
        nodeInfoButtonShape.getPaint().setStyle(Paint.Style.STROKE);
        nodeInfoButton.setBackground(nodeInfoButtonShape);

        nodeInfoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewElementDataString = nodeInfoButton.showInformation();
                showFloatingInfoWindow(viewElementDataString);
            }
        });

        wm.addView(nodeInfoButton, nodeLayoutParams);
        nodeButtonsList.add(nodeInfoButton);
        nodeCounter++;

        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            logNodeHierarchy(nodeInfo.getChild(i), depth + 1);
        }
    }

    public void showFloatingInfoWindow(String initText){
        Context context = getApplicationContext();
        lLayout = new LinearLayout(this);

        if(floatingInfoBoxIsSet){
            wm.removeView(floatingInfobox);
        }


        WindowManager.LayoutParams infoWindowParams = new WindowManager.LayoutParams();


        infoWindowParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        infoWindowParams.format = PixelFormat.TRANSLUCENT;
        infoWindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        infoWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        infoWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        infoWindowParams.gravity = Gravity.BOTTOM;
        infoWindowParams.x = 0;
        infoWindowParams.y = 0;


        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingInfobox = layoutInflater.inflate(R.layout.floatingwindow, null);

        TextView infoWindowBox = (TextView) floatingInfobox.findViewById(R.id.infoWindowText);
        infoWindowBox.setText(Html.fromHtml(initText));

        floatingInfobox.setOnTouchListener(new View.OnTouchListener() {

            private WindowManager.LayoutParams updateParameters = infoWindowParams;
            int y;
            float touchedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        y = updateParameters.y;
                        touchedY = event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updateParameters.y = (int) (y - (event.getRawY() - touchedY));

                        wm.updateViewLayout(floatingInfobox, updateParameters);

                    default:

                        break;
                }

                return false;
            }
        });

        wm.addView(floatingInfobox,infoWindowParams);

        floatingInfoBoxIsSet = true;

    }


    public void addShareButton(){

        Context context = getApplicationContext();

        shareButton = new Button(this);
        shareButton.setText("Ergebnis teilen");

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        WindowManager.LayoutParams shareBtnParams;

        shareBtnParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        |WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        |WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ,
                PixelFormat.TRANSLUCENT);

        shareBtnParams.gravity = Gravity.TOP | Gravity.END;

/*      //Versuch TYPE_APPLICATION:
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //lp.alpha = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        lp.gravity = Gravity.TOP;
        lp.x = 0;
        lp.y = 0;*/

        wm.addView(shareButton, shareBtnParams);

        shareButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                writeToFile(context);
                removeWindows();
                goToInspectorApp();
            }
        });
    }


    private void goToInspectorApp(){

        Context context = getApplicationContext();
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage("com.example.accessibilityserviceappv2");
        if (intent != null) {
            context.startActivity(intent);
        }
    }


    private void removeWindows() {

        try {
            for (CustomButton nb : nodeButtonsList) {

                if (ViewCompat.isAttachedToWindow(nb)) {
                    wm.removeView(nb);
                }

            }

            if (ViewCompat.isAttachedToWindow(floatingInfobox)) {
                wm.removeView(floatingInfobox);
                floatingInfoBoxIsSet = false;

            }

            if (ViewCompat.isAttachedToWindow(shareButton)) {
                wm.removeView(shareButton);
            }


        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }


    private void writeToFile(Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("app-inspector-results.csv", Context.MODE_PRIVATE));
            String csvData = dataPreparation();
            outputStreamWriter.write(csvData);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String dataPreparation(){

        //ToDo: Überprüfen der Uhrzeit beim Export
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        String csvData;
        String currentDate = df.toString();
        StringWriter sw = new StringWriter();
        String csvHeader = "Element-Nr, Beschriftung, Inhalts-Label, Hint, Zugeh. Label, Datum/ Zeit, Applikation";

        sw.append(csvHeader);

        sw.append("\n\r");

        for (CustomButton ab: nodeButtonsList) {

            sw.append(ab.getElementNumber());
            sw.append(",");
            sw.append(ab.getElementText());
            sw.append(",");
            sw.append(ab.getElementContentDescription());
            sw.append(",");
            sw.append(ab.getElementHint());
            sw.append(",");
            sw.append(ab.getLabeledByElement());
            sw.append(",");
            sw.append(currentDate);
            sw.append(",");
            sw.append(ab.getAppName());
            sw.append("\n");
        }

        csvData = sw.toString();

        return csvData;

    }

}