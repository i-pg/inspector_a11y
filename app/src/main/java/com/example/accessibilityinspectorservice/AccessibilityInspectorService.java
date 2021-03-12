package com.example.accessibilityinspectorservice;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ViewParent;
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
import java.util.Map;

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
    String sharedPrefColorsHolder;
    String sharedPrefTextsizeHolder;
    int textSizeNumber = 18;
    final String sharedPrefLabel = "appsToInspect";
    final String sharedPrefColors = "highlight_color_pref_inspector";
    final String sharedPrefTextsize = "text_size_pref_inspector";
    int showButtonCounter = 0;
    boolean elementsHighlighted = true;
    Toast myToast;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        removeWindows();
        logNodeHierarchy(getRootInActiveWindow(), 0);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {

        Log.w("EVENT TYPE", AccessibilityEvent.eventTypeToString(e.getEventType()));


        //get shared Preferences for Inspector Settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPrefsHolder = prefs.getString(sharedPrefLabel, "defaultStringIfNothingFound");
        sharedPrefColorsHolder = prefs.getString(sharedPrefColors, "#000000");
        sharedPrefTextsizeHolder = prefs.getString(sharedPrefTextsize, "20");
        textSizeNumber = Integer.parseInt(sharedPrefTextsizeHolder);

        //Whitelist - Apps to Inspect
        String splitHelperString = sharedPrefsHolder;
        List<String> appsWhitelist = Arrays.asList(splitHelperString.split(";"));

        nodeCounter = 1;
        String currentPackageName = "init text";


        /*
         * On Change of Windows State ("Optisch abgegrenzter Bereich in der UI") - check Whitelist
         * Create Floating Window and Nodes
         * Add Overlay
         * Log.v(LOG_TAG, e.getPackageName().toString());
         */


        if (e.getPackageName()!=null) {
            currentPackageName = e.getPackageName().toString();


            switch (e.getEventType()) {

                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {

                    if(appsWhitelist.contains(currentPackageName)){

                        removeHighlights();
                        showButtonCounter = 1;
                        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

                        nodeButtonsList = new ArrayList();
                        appName =  e.getPackageName().toString();
                        //log tree of all AccessibilityNodes
                        logNodeHierarchy(getRootInActiveWindow(), 0);
                        //Init Floating Window
                        viewElementDataString = "Gefundene Elemente: " + nodeButtonsList.size();
                        showFloatingInfoWindow(viewElementDataString);
                    }

                }

                //Future Work: Reload on Scroll
                case AccessibilityEvent.TYPE_VIEW_SCROLLED: {
                    //Log.w("Scrolled =", " yes");
                    if(appsWhitelist.contains(currentPackageName)||currentPackageName.equals("com.example.emptytestapp")) {
                        displayToast("Scrollen noch nicht implementiert");
                    }
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
        String className;
        String shortAppName;
        String logString = "";

        if (nodeInfo == null) return;

        for (int i = 0; i < depth; ++i) {
            logString += " ";
        }

        //Collect Accessibility Information
        //text
        if (nodeInfo.getText() != null) {
            viewText = nodeInfo.getText().toString();
        } else {
            viewText = "-";
        }
        //content label
        if (nodeInfo.getContentDescription() != null) {
            contentDescription = nodeInfo.getContentDescription().toString();
        } else {
            contentDescription = "-";
        }
        //hint
        if (nodeInfo.getHintText() != null) {
            hintText = nodeInfo.getHintText().toString();
        } else {
            hintText = "-";
        }
        //labeled by
        if (nodeInfo.getLabeledBy() != null) {
            labeledByElement = nodeInfo.getLabeledBy().getText().toString();
        } else {
            labeledByElement = "-";
        }
        //element type
        if (nodeInfo.getClassName() != null) {
            // split by "." to get class name
            String currentString = nodeInfo.getClassName().toString();
            String[] separated = currentString.split("\\.");
            className = separated[separated.length - 1];
        } else {
            className = "-";
        }
        //app name
        if (nodeInfo.getPackageName() != null) {
            // split by "." to get app name
            String currentString = nodeInfo.getPackageName().toString();
            String[] separated = currentString.split("\\.");
            shortAppName = separated[separated.length - 1];
        } else {
            shortAppName = "-";
        }


        //Filter Layout Elments
        String keyword_one = "layout";
        String keyword_two = "scrollview";
        String keyword_three = "ViewGroup";
        String keyword_four = "RecyclerView";

        if (!className.toLowerCase().contains(keyword_one.toLowerCase() ) && !className.toLowerCase().contains(keyword_two.toLowerCase() ) ) {

            //Node coordinates
            nodeInfo.getBoundsInScreen(rect);
            Context context = getApplicationContext();

            //Params for AccessNodeButtons
            WindowManager.LayoutParams nodeLayoutParams = new WindowManager.LayoutParams();

            nodeLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
            nodeLayoutParams.format = PixelFormat.TRANSLUCENT;
            nodeLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            nodeLayoutParams.width = rect.width();
            nodeLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            nodeLayoutParams.gravity = Gravity.TOP | Gravity.START;
            nodeLayoutParams.x = rect.left;
            nodeLayoutParams.y = rect.top - 70;

            //Create AccessNodeButton (Clickable Numbers and Frames for discorverd View Elements
            AccessNodeButton nodeInfoButton = new AccessNodeButton(context, nodeCounter, viewText, contentDescription, hintText, labeledByElement, shortAppName, className, rect);
            nodeInfoButton.setText(String.valueOf(nodeCounter));
            nodeInfoButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            nodeInfoButton.setPadding(20, 30, 20, 20);

            //Highlight Elements
            ShapeDrawable nodeInfoButtonShape = new ShapeDrawable();
            nodeInfoButtonShape.setShape(new RectShape());
            nodeInfoButtonShape.getPaint().setColor(Color.parseColor(sharedPrefColorsHolder));
            nodeInfoButtonShape.getPaint().setStrokeWidth(10f);
            nodeInfoButtonShape.getPaint().setStyle(Paint.Style.STROKE);
            nodeInfoButton.setBackground(nodeInfoButtonShape);

            //On Click: Show Information in Infobox
            nodeInfoButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    viewElementDataString = nodeInfoButton.getInformationString();
                    showFloatingInfoWindow(viewElementDataString);
                }
            });

            wm.addView(nodeInfoButton, nodeLayoutParams);
            nodeButtonsList.add(nodeInfoButton);
            nodeCounter++;
        }

        //repeat for each Element in tree
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            logNodeHierarchy(nodeInfo.getChild(i), depth + 1);
        }
    }



    /*
     * Create Floating Info Box
     * Show Information of Info-Nodes
     * moveable (vertical)
     */
    public void showFloatingInfoWindow(String initText){
        Context context = getApplicationContext();
        lLayout = new LinearLayout(this);

        if(floatingInfoBoxIsSet){
            wm.removeView(floatingInfobox);
        }


        //Infobox Layout Params
        WindowManager.LayoutParams infoWindowParams = new WindowManager.LayoutParams();
        infoWindowParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        infoWindowParams.format = PixelFormat.TRANSLUCENT;
        infoWindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        infoWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        infoWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        infoWindowParams.gravity = Gravity.BOTTOM;
        infoWindowParams.x = 0;
        infoWindowParams.y = 0;


        //Get Buttons and Textview in Infobox
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingInfobox = layoutInflater.inflate(R.layout.floatingwindow, null);

        TextView infoWindowBox = (TextView) floatingInfobox.findViewById(R.id.infoWindowText);
        infoWindowBox.setMovementMethod(new ScrollingMovementMethod());
        ImageButton nextInfoButton = (ImageButton) floatingInfobox.findViewById(R.id.nextInfoButton);
        ImageButton prevInfoButton = (ImageButton) floatingInfobox.findViewById(R.id.prevInfoButton);
        ImageButton shareButton = (ImageButton) floatingInfobox.findViewById(R.id.shareButton);
        ImageButton showHighlightsButton = (ImageButton) floatingInfobox.findViewById(R.id.showElementsButton);
        ImageButton exitButton = (ImageButton) floatingInfobox.findViewById(R.id.exitButton);


        //Set Text
        infoWindowBox.setText(Html.fromHtml(initText));

        // Set TextSize
        infoWindowBox.setTextSize(textSizeNumber);

        //Show Hightlight Button state
        if(elementsHighlighted){
            showHighlightsButton.setImageResource(R.drawable.squares_25);
        }
        else {
            showHighlightsButton.setImageResource(R.drawable.square_25);
        }


        /*
         *  Implement Infobox Buttons
         *
         */

        //right infobox button - show next Element
        nextInfoButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  //show only current Element
                  elementsHighlighted = false;
                  //edge detection
                  if(showButtonCounter<nodeButtonsList.size()){
                      showButtonCounter++;
                  } else {
                      showButtonCounter = 1;
                  }
                  selectCurrentElement();
              }
          }

        );
        //left infobox button - show previous Element
        prevInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show only current Element
                elementsHighlighted = false;
                //edge detection
                if(showButtonCounter>1){
                    showButtonCounter--;
                } else {
                    showButtonCounter=nodeButtonsList.size();
                }
                selectCurrentElement();
            }
        });

        //hightlight all elements or current element
        showHighlightsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(elementsHighlighted){
                    removeHighlights();
                    elementsHighlighted=false;
                    showHighlightsButton.setImageResource(R.drawable.square_25);
                    selectCurrentElement();
                }
                else if(!elementsHighlighted){
                    highlightAllElements();
                    elementsHighlighted=true;
                    showHighlightsButton.setImageResource(R.drawable.squares_25);
                }
            }
        });

        //share report
        //save data in local storage - go to inspector app to open Share Intent
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnNumber = "Nummer " + nodeCounter;
                writeToFile(btnNumber, context);
                removeWindows();

                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        //ToDo: string.xml
                        .setTitle("Bericht erstellt")
                        .setMessage("Dein Fehlerbericht wurde erstellt. Du wirst nun zur Inspector App weitergeleitet. " +
                                "\n\nUm deinen Bericht zu teilen klicke dort auf 'Fehlerbericht teilen'")

                        .setPositiveButton("Verstanden", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goToInspectorApp();
                            }
                        })
                        .create();

                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                alertDialog.show();
            }
        });

        //close Service
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeWindows();
            }
        });

        //make infobox moveable
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


    //show highlights - select current element
    public void selectCurrentElement(){
        int currentElementNumber = showButtonCounter;

        for(AccessNodeButton cB : nodeButtonsList){

            if(cB.getElementInteger() == currentElementNumber){
                viewElementDataString = cB.getInformationString();
                highlightCurrentElement(cB);
                showFloatingInfoWindow(viewElementDataString);
                break;
            }

        }
    }
    public void highlightCurrentElement(AccessNodeButton currentElement){

        removeHighlights();
        Rect coordinates = currentElement.getCoordinates();
        Context context = getApplicationContext();

        //Set the border color of the highlighted Elements
        ShapeDrawable shapedrawable = new ShapeDrawable();
        shapedrawable.setShape(new RectShape());
        shapedrawable.getPaint().setColor(Color.parseColor(sharedPrefColorsHolder));
        shapedrawable.getPaint().setStrokeWidth(20f);
        shapedrawable.getPaint().setStyle(Paint.Style.STROKE);
        currentElement.setBackground(shapedrawable);

        //Layout Params AccessNode Elements
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

    //show highlights - select all elements
    public void highlightAllElements(){

        if(!elementsHighlighted){

            for(AccessNodeButton aB:nodeButtonsList){

                Rect coordinates = aB.getCoordinates();

                //Set the border color of the highlighted Elements
                ShapeDrawable shapedrawable = new ShapeDrawable();
                shapedrawable.setShape(new RectShape());
                shapedrawable.getPaint().setColor(Color.parseColor(sharedPrefColorsHolder));
                shapedrawable.getPaint().setStrokeWidth(20f);
                shapedrawable.getPaint().setStyle(Paint.Style.STROKE);
                aB.setBackground(shapedrawable);

                //Layout Params AccessNode Elements
                WindowManager.LayoutParams nodeLayoutParams = new WindowManager.LayoutParams();
                nodeLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                nodeLayoutParams.format = PixelFormat.TRANSLUCENT;
                nodeLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
                nodeLayoutParams.width = coordinates.width();
                nodeLayoutParams.height = coordinates.height();
                nodeLayoutParams.gravity = Gravity.TOP | Gravity.START;
                nodeLayoutParams.x = coordinates.left;
                nodeLayoutParams.y = coordinates.top - 70;


                if(aB.getWindowToken() == null){
                    wm.addView(aB, nodeLayoutParams);
                }

            }

            elementsHighlighted=true;
            //update text in Infobox
            showFloatingInfoWindow(viewElementDataString);
        }
    }


    /*
     * Helper Functions
     *
     */


    public void displayToast(String message) {
        if(myToast != null)
            myToast.cancel();
        myToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.TOP, 0, 300); //<-- set gravity here
        myToast.show();
    }

    /*
     * Launch Inspector App to Share Report
     * Cannot start share itent in Service - redirect to main activity
     */

    private void goToInspectorApp() {
        Context context = getApplicationContext();
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage("com.example.accessibilityserviceappv2");
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    //remove AccessNode Buttons and Infobox
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

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    //remove AccessNode Buttons
    private void removeHighlights(){

        if(nodeButtonsList!=null){
            for (AccessNodeButton nb : nodeButtonsList) {

                if (ViewCompat.isAttachedToWindow(nb)) {
                    wm.removeView(nb);
                }

            }
        }
        elementsHighlighted = false;
    }


    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("accessibility_report.txt", Context.MODE_PRIVATE));
            String csvData = dataPreparation();
            outputStreamWriter.write(csvData);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //create CSV File for Report
    private String dataPreparation(){
        Date date = new Date();
        long timestamp = date.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm:ss");
        String currentDate = sdf.format(timestamp);
        String csvData;
        StringWriter sw = new StringWriter();
        String csvHeader = "Element-Nr, Beschriftung, Inhalts-Label, Hint, Zugeh. Label, Element-Typ, Datum/ Zeit, Applikation";

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
            sw.append(ab.getClassName());
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