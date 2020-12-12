package com.example.accessibilityserviceappv2;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static java.net.Proxy.Type.HTTP;

public class MyAccessibilityServiceV2 extends AccessibilityService {

    FrameLayout mLayout;
    LinearLayout lLayout;
    private static final String LOG_TAG = "MyActivity";
    public static String appname = "DummyApp";
    private Paint paint;
    Rect rectArray[];
    List<Rect> rectList = new ArrayList<>();
    int btnCounter;
    View view;
    TextView floatingView;
    String buttonClickTextTest;
    Boolean viewIsSet = false;



    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {

        btnCounter = 1;

        //Log.i("Accessibility", "onAccessibilityEvent");


        if(e.getPackageName()!=null){appname = e.getPackageName().toString();}

        String logString = "";

        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        if (e.getPackageName()!=null && e.getPackageName().toString().equals("com.example.emptytestapp")) {

            showFloatingWindow("init text");




            switch (e.getEventType()) {
                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {

                    logNodeHierarchy(getRootInActiveWindow(), 0);
                    addExportButton();
                }

                case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                    //logString += "Text: " + nodeInfo.getText() + " \n " + " Content-Description: " + nodeInfo.getContentDescription() + "\n App Name: " + appname;
                    //Log.v(LOG_TAG, logString);

                }

                case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED: {
                    //  logString += "Context Text: " + nodeInfo.getText() + " " + " Content-Description: " + nodeInfo.getContentDescription() + " App Name: " + appname;
                    //  Log.v(LOG_TAG, logString);
                }

            }

            //for (int i = 0 ; i < rectList.size() ; i++)
            //  Log.d("value is" , rectList.get(i).toString());

        }

        /*
        String name;

        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            if (event.getPackageName().toString().equals("com.whatsapp")){
                StringBuilder message = new StringBuilder();
                if (!event.getText().isEmpty()) {
                    for (CharSequence subText : event.getText()) {
                        message.append(subText);
                    }
                    if (message.toString().contains("Message from")){
                        name=message.toString().substring(13);
                    }
                }
            }
        }


        if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            if (source.getPackageName().equals("com.whatsapp")) {
                AccessibilityNodeInfo currentNode=getRootInActiveWindow();
                if (currentNode!=null && currentNode.getClassName().equals("android.widget.FrameLayout") && currentNode.getChild(2)!=null && currentNode.getChild(2).getClassName().equals("android.widget.TextView") && currentNode.getChild(2).getContentDescription().equals("Search")) {
                    currentNode.getChild(2).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }

         */



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


/*        System.out.println("onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.eventTypes=AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;
        setServiceInfo(info);*/
    }


    public void logNodeHierarchy(AccessibilityNodeInfo nodeInfo, int depth) {

        Rect rect = new Rect();

        String contentDescription;
        String viewText;
        String hintText;



        if (nodeInfo == null) return;


        String logString = "";

        for (int i = 0; i < depth; ++i) {
            logString += " ";
        }

        nodeInfo.getBoundsInScreen(rect);

        Context context = getApplicationContext();

        logString += "\nElement: " + btnCounter + "\n Text: " + nodeInfo.getText() + "\n" + " Content-Description: " + nodeInfo.getContentDescription() + "\n App Name: " + appname + "\n Koordinaten " +rect + "\n Hint " + nodeInfo.getHintText() + "\n Labeled By " + nodeInfo.getLabeledBy();

        Log.v(LOG_TAG, logString);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
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

        CustomButton testBtn = new CustomButton(context, btnCounter, viewText, contentDescription, hintText);
        Button testBtn2 = new Button(context);
        testBtn.setText(String.valueOf(btnCounter));
        testBtn.setContentDescription("auto button");
        testBtn2.setText(String.valueOf(btnCounter));
        testBtn2.setContentDescription("auto button");
        //testBtn.setBackgroundColor(Color.RED);
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

        btnCounter++;


        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {

            logNodeHierarchy(nodeInfo.getChild(i), depth + 1);

        }


    }

    public void showFloatingWindow(String initText){
        Context context = getApplicationContext();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        lLayout = new LinearLayout(this);

        if(viewIsSet){
            wm.removeView(view);
        }

        Button testButton = new Button(context);

        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);

        WindowManager.LayoutParams lp2 = new WindowManager.LayoutParams();


        lp2.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp2.format = PixelFormat.TRANSLUCENT;
        lp2.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        lp2.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp2.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp2.alpha = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        lp2.gravity = Gravity.BOTTOM;
        lp2.x = 0;
        lp2.y = 0;


        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.floatingwindow, null);

        TextView item = (TextView) view.findViewById(R.id.textView2);

        item.setText(Html.fromHtml(initText));

        lLayout.setLayoutParams(llParameters);

        view.setOnTouchListener(new View.OnTouchListener() {

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

                        wm.updateViewLayout(view, updateParameters);

                    default:

                        break;
                }

                return false;
            }
        });


        wm.addView(view,lp2);

        viewIsSet = true;

    }


    public void addExportButton(){

        Context context = getApplicationContext();


        Button exportButton = new Button(this);
        exportButton.setText("Export Button");

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
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
                //export();
                //emailTest();
                //intentTest();
                String btnNumber = "Test Anzahl " + btnCounter;
                //writeFileOnInternalStorage(context, "testFilename", "testFileBodyText");
                //writeToFile(btnNumber, context);
                String testText = readFromFile(context);
                Log.v(LOG_TAG, testText);

            }
        });


        wm.addView(exportButton, lp);

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

            /*
            //exporting
            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "com.example.accessibilityserviceappv2.fileprovider", filelocation);
            Intent fileIntent = new Intent();
            fileIntent.setAction(Intent.ACTION_SEND);
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            //fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(fileIntent, "Send Mail"));
             */
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    public void emailTest(){
/*        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        //Intent emailIntent = new Intent(App.getContext(), App.class);
        emailIntent.setType("text/html");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"jon@example.com"}); // recipients
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message text");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://path/to/email/attachment"));
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        emailIntent.setComponent(new ComponentName(getApplicationContext().getPackageName(),  App.class.getName()));

        startActivity(Intent.createChooser(emailIntent, "Send Email"));*/
// You can also attach multiple items by passing an ArrayList of Uris

    }

    public void intentTest(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cn = new ComponentName(this, MainActivity.class);
        intent.setComponent(cn);
        startActivity(intent);
    }


    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        File dir = new File(mcoContext.getFilesDir(), "mydir");

/*
        try {
            File myFile = new File("Your File name");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
            myOutWriter.append("testtext");
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getApplicationContext(), "Done writing SD 'mysdfile.txt", Toast.LENGTH_SHORT).show();

        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
        }*/



    }

    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("mytesttext.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
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


}