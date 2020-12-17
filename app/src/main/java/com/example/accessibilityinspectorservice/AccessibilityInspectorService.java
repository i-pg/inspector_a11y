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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccessibilityInspectorService extends AccessibilityService {

    FrameLayout mLayout;
    LinearLayout lLayout;
    private static final String LOG_TAG = "MyActivity";
    public static String appname = "DummyApp";
    int btnCounter;
    View floatingInfobox;
    String buttonClickTextTest;
    Boolean viewIsSet = false;
    WindowManager wm;
    List <CustomButton> accessButtonList;
    Button exportButton;
    ActivityInfo activityInfo;
    ComponentName componentName;
    String appName;
    String myStrValue;
    final String sharedPrefLabel = "appsToExamine";



    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (e.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (e.getPackageName() != null && e.getClassName() != null) {
                componentName = new ComponentName(
                        e.getPackageName().toString(),
                        e.getClassName().toString()
                );

                activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity)
                    Log.i("CurrentActivity", componentName.flattenToShortString());
            }

            if(e.getPackageName() != null){
                if(!e.getPackageName().equals("com.example.emptytestapp") && !e.getPackageName().equals("com.example.accessibilityserviceappv2")) {
                    System.out.println(" The Package " + e.getPackageName());
                    System.out.println(" Remove Windows ");
                    removeWindows();
                }

            }





        }

        btnCounter = 1;

        if(e.getPackageName()!=null){appname = e.getPackageName().toString();}

        myStrValue = prefs.getString(sharedPrefLabel, "defaultStringIfNothingFound");
        System.out.println("Shared Pref " + myStrValue);


        if (e.getPackageName()!=null && e.getPackageName().toString().equals("com.example.emptytestapp")) {


            switch (e.getEventType()) {
                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {

                    wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                    showFloatingWindow("init text");
                    accessButtonList = new ArrayList();

                    appName =  e.getPackageName().toString();
                    logNodeHierarchy(getRootInActiveWindow(), 0);
                    addExportButton();
                }

                case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                }

                case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED: {
                }

            }

        }

       /*==========  https://stackoverflow.com/questions/35842762/how-to-read-window-content-using-accessibilityservice-and-evoking-ui-using-dra ===========
       AccessibilityNodeInfo source = event.getSource();
        if (source == null) {return;}

        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId = source.findAccessibilityNodeInfosByViewId("YOUR PACKAGE NAME:id/RESOURCE ID FROM WHERE YOU WANT DATA");
        if (findAccessibilityNodeInfosByViewId.size() > 0) {
            AccessibilityNodeInfo parent = (AccessibilityNodeInfo) findAccessibilityNodeInfosByViewId.get(0);
            String requiredText = parent.getText().toString();
            Log.i("Required Text", requiredText);}
        ======================================================*/

    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {

        Log.i("Interrupt", "Interrupt");
        Toast.makeText(getApplicationContext(), "onInterrupt", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();
        Log.i("Service", "Connected");
        Toast.makeText(getApplicationContext(), "onServiceConnected", Toast.LENGTH_SHORT).show();


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

        nodeInfo.getBoundsInScreen(rect);

        Context context = getApplicationContext();

        logString += "\nElement: " + btnCounter + "\n Text: " + nodeInfo.getText() + "\n" + " Content-Description: " + nodeInfo.getContentDescription() + "\n App Name: " + appname + "\n Koordinaten " +rect + "\n Hint " + nodeInfo.getHintText() + "\n Labeled By " + nodeInfo.getLabeledBy();

        Log.v(LOG_TAG, logString);

        mLayout = new FrameLayout(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
       // lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //lp.alpha = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        lp.gravity = Gravity.TOP | Gravity.RIGHT;
        lp.x = rect.centerX();
        lp.y = rect.centerY();



        if(nodeInfo.getText()!=null){
            viewText = nodeInfo.getText().toString();
        }
        else {
            viewText = "empty";
        }

        if(nodeInfo.getContentDescription()!=null){
            contentDescription = nodeInfo.getContentDescription().toString();
        }
        else {
            contentDescription = "empty";
        }

        if(nodeInfo.getHintText()!=null){
            hintText = nodeInfo.getHintText().toString();
        }

        else {
            hintText = "empty";
        }

        if(nodeInfo.getLabeledBy()!=null){
            labeledByElement = nodeInfo.getLabeledBy().getText().toString();
        }

        else {
            labeledByElement = "not labeled";
        }



        CustomButton testBtn = new CustomButton(context, btnCounter, viewText, contentDescription, hintText, labeledByElement, appName);
        Button testBtn2 = new Button(context);
        testBtn.setText(String.valueOf(btnCounter));
        testBtn.setContentDescription("auto button");
        testBtn2.setText(String.valueOf(btnCounter));
        testBtn2.setContentDescription("auto button");
        testBtn.setWidth(150);
        testBtn.setHeight(150);
        testBtn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        testBtn.setPadding(20,30,20,20);

        ShapeDrawable shapedrawable = new ShapeDrawable();
        shapedrawable.setShape(new RectShape());
        shapedrawable.getPaint().setColor(Color.BLACK);
        shapedrawable.getPaint().setStrokeWidth(10f);
        shapedrawable.getPaint().setStyle(Paint.Style.STROKE);
        testBtn.setBackground(shapedrawable);

        testBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClickTextTest = testBtn.showInformation();
                showFloatingWindow(buttonClickTextTest);
            }
        });

        wm.addView(testBtn, lp);

        accessButtonList.add(testBtn);

        btnCounter++;


        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {

            logNodeHierarchy(nodeInfo.getChild(i), depth + 1);

        }


    }

    public void showFloatingWindow(String initText){
        Context context = getApplicationContext();
        lLayout = new LinearLayout(this);

        if(viewIsSet){
            wm.removeView(floatingInfobox);
        }

        Button testButton = new Button(context);

        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);

        WindowManager.LayoutParams lp2 = new WindowManager.LayoutParams();


        lp2.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        lp2.format = PixelFormat.TRANSLUCENT;
        lp2.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        lp2.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp2.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp2.gravity = Gravity.BOTTOM;
        lp2.x = 0;
        lp2.y = 0;


        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingInfobox = layoutInflater.inflate(R.layout.floatingwindow, null);

        TextView item = (TextView) floatingInfobox.findViewById(R.id.textView2);

        item.setText(Html.fromHtml(initText));

        lLayout.setLayoutParams(llParameters);

        floatingInfobox.setOnTouchListener(new View.OnTouchListener() {

            private WindowManager.LayoutParams updateParameters = lp2;
            int x, y;
            float touchedX, touchedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        y = updateParameters.y;


                        Toast.makeText(getApplicationContext(), "onTouch", Toast.LENGTH_SHORT).show();


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


        wm.addView(floatingInfobox,lp2);

        viewIsSet = true;

    }


    public void addExportButton(){

        Context context = getApplicationContext();


        exportButton = new Button(this);
        exportButton.setText("Export Button");



        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        WindowManager.LayoutParams theparams = new WindowManager.LayoutParams();

        theparams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        |WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        |WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ,
                PixelFormat.TRANSLUCENT);


        wm.addView(exportButton, theparams);



        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //lp.alpha = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        lp.gravity = Gravity.TOP;
        lp.x = 0;
        lp.y = 0;

        exportButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                System.out.println("onclicker geklickt");
                String btnNumber = "Test Anzahl " + btnCounter;
                writeToFile(btnNumber, context);
                removeWindows();
                goToMainActivity();
            }
        });

    }


    public void export(){


        //generate data
        StringBuilder data = new StringBuilder();
        data.append("Time, Distance");
        for(int i = 0; i<5; i++ ){
            data.append("\n"+String.valueOf(i)+","+String.valueOf(i*i));
        }

        try{
            //saving the file into device
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

        }
        catch (Exception e){
            e.printStackTrace();
        }


    }



    private void goToMainActivity(){

        //https://stackoverflow.com/questions/30800900/android-launch-another-app-from-activity

        Context context = getApplicationContext();

        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage("com.example.accessibilityserviceappv2");
        if (intent != null) {
            context.startActivity(intent);
        }

    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("mytesttext.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void removeWindows(){

        System.out.println(" windows removed");



        try {


                for (CustomButton ab: accessButtonList) {

                    if( ViewCompat.isAttachedToWindow(ab)){

                        wm.removeView(ab);
                    }

                }

                wm.removeView(floatingInfobox);

                wm.removeView(exportButton);
                viewIsSet=false;


        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }


    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("mytesttext.txt", Context.MODE_PRIVATE));

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
        String csvData;

        String elementNumber;
        String elementText;
        String contentDescription;
        String elementHintText;
        String labeledByElement;
        String currentTime;
        String currentDate = sdf.format(timestamp);
        String appName;


        StringWriter sw = new StringWriter();

        String csvHeader = "Element-Nr, Beschriftung, Inhalts-Label, Hint, Zugeh. Label, Datum/ Zeit, Applikation";

        sw.append(csvHeader);

        sw.append("\n\r");

        for (CustomButton ab: accessButtonList) {

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