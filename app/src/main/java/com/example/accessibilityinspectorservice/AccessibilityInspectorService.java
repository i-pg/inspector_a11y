package com.example.accessibilityinspectorservice;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.example.accessibilityserviceappv2.R;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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
    List <AccessNodeButton> nodeButtonsList;
    Button shareButton;
    String appName;
    String sharedPrefsHolder;
    final String sharedPrefLabel = "appsToInspect";
    int showButtonCounter = 1;


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

                /*
                 * Bei einer Änderung in einem optisch abgegrenzten Bereich der Benutzeroberfläche wird die Whitelist verglichen
                 * Ist die aktuelle App in der Whitelist werden FloatingInfo Window, Nodes und Share-Button erstellt und als
                 * Overlay hinzugefügt
                 */
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {

                    Log.v(LOG_TAG, e.getPackageName().toString());
                    //ToDo: OR Teil entfernen - nur für testing
                    if(appsWhitelist.contains(currentPackageName)||currentPackageName.equals("com.example.emptytestapp")){
                        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                        nodeButtonsList = new ArrayList();
                        appName =  e.getPackageName().toString();
                        logNodeHierarchy(getRootInActiveWindow(), 0);
                        showFloatingInfoWindow("Klicke auf eines der Elemente");
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

    /*
     * Alle Nodes im Screen werden gesammelt
     * Koordinaten der einzelnen Nodes werden in einem Rect() gespeichert
     * Die einzelnen Nodes werden als Custom Buttons (AccessNodeButton) in
     * die App gezeichnet
     * Für jede Node wird ein AccessNodeButton-Objekt erstellt in dem die
     * Informationen der Node als Instanzvariablen gespeichert werden     *
     */

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

        AccessNodeButton nodeInfoButton = new AccessNodeButton(context, nodeCounter, viewText, contentDescription, hintText, labeledByElement, appName, rect);
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
                viewElementDataString = nodeInfoButton.getInformationString();
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



    /*
     * Erstellen der Floating Info Box, welche die Informationen der einzelnen Nodes bei einem
     * Klickt anzeigt.
     * Die Box kann vertikal verschoben werden.
     *
     */
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
        infoWindowBox.setMovementMethod(new ScrollingMovementMethod());
        ImageButton nextInfoButton = (ImageButton) floatingInfobox.findViewById(R.id.nextInfoButton);
        ImageButton shareButton = (ImageButton) floatingInfobox.findViewById(R.id.shareButton);
        infoWindowBox.setText(Html.fromHtml(initText));


        nextInfoButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  int currentElementNumber = showButtonCounter;

                  AccessNodeButton currentButton = null;

                  for(AccessNodeButton cB : nodeButtonsList){

                      if(cB.getElementInteger() == currentElementNumber){
                          viewElementDataString = cB.getInformationString();
                          showFloatingInfoWindow(viewElementDataString);
                          highlightCurrentElement(cB);
                          break;
                      }

                  }

                  if(showButtonCounter<nodeButtonsList.size()){
                      showButtonCounter++;
                  }
                  else {
                      showButtonCounter = 1;
                  }


              }
          }

        );

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnNumber = "Nummer " + nodeCounter;
                writeToFile(btnNumber, context);
                removeWindows();
                goToInspectorApp();
            }
        });

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

    public void highlightCurrentElement(AccessNodeButton currentElement){

        removeHighlights();

        Rect coordinates = currentElement.getCoordinates();
        Context context = getApplicationContext();

        //Set the color of the higlighted Elements border
        ShapeDrawable shapedrawable = new ShapeDrawable();
        shapedrawable.setShape(new RectShape());
        shapedrawable.getPaint().setColor(Color.RED);
        shapedrawable.getPaint().setStrokeWidth(20f);
        shapedrawable.getPaint().setStyle(Paint.Style.STROKE);
        currentElement.setBackground(shapedrawable);

        WindowManager.LayoutParams nodeLayoutParams = new WindowManager.LayoutParams();

        nodeLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        nodeLayoutParams.format = PixelFormat.TRANSLUCENT;
        nodeLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        nodeLayoutParams.width = coordinates.width();
        nodeLayoutParams.height = coordinates.height();
        nodeLayoutParams.gravity = Gravity.TOP | Gravity.START;
        nodeLayoutParams.x = coordinates.left;
        nodeLayoutParams.y = coordinates.top - 70;

        wm.addView(currentElement, nodeLayoutParams);

    }

    /*
     * Die Informationen können nicht direkt aus dem Service geteilt werden. Deshalb werden die Daten gespeichert
     * und der Nutzer an die Main Activity weitergeleitet, von wo aus ein Share Intent gestartet werden kann
     */

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
            for (AccessNodeButton nb : nodeButtonsList) {

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

    private void removeHighlights(){
        for (AccessNodeButton nb : nodeButtonsList) {

            if (ViewCompat.isAttachedToWindow(nb)) {
                wm.removeView(nb);
            }

        }
    }


    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("newmytesttext.txt", Context.MODE_PRIVATE));
            String csvData = dataPreparation();
            outputStreamWriter.write(csvData);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String dataPreparation(){

        Date date = new Date();
        long timestamp = date.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm:ss");
        String currentDate = sdf.format(timestamp);


        //ToDo: Überprüfen der Uhrzeit beim Export
        //DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        String csvData;
       // String currentDate = df.toString();
        StringWriter sw = new StringWriter();
        String csvHeader = "Element-Nr, Beschriftung, Inhalts-Label, Hint, Zugeh. Label, Datum/ Zeit, Applikation";

        sw.append(csvHeader);

        sw.append("\n\r");

        for (AccessNodeButton ab: nodeButtonsList) {

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