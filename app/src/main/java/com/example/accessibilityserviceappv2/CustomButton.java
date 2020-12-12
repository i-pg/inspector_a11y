package com.example.accessibilityserviceappv2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class CustomButton extends androidx.appcompat.widget.AppCompatButton /*implements View.OnClickListener*/ {

    /*
    Custom Button
     */

    String contentDescription;
    Context context;
    String logString;
    private static final String LOG_TAG = "ButtonActivity";
    View view;
    LayoutInflater layoutInflater;
    TextView testText;

    String viewText;
    String hintText;
    int btnCounter;


    public CustomButton(Context context) {
        this(context, 0, null, null, null);
    }

    public CustomButton(Context context, int btnCounter, String viewText, String contentDescription, String hintText) {
        super(context);
        init();
        this.btnCounter = btnCounter;
        this.viewText = viewText;
        this.contentDescription = contentDescription;
        this.hintText = hintText;
        this.context = context;
    }

    private void init(){
        if(context != null){
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.floatingwindow, null, false);
            testText = findViewById(R.id.textView2);
        }
        else {
            Log.v(LOG_TAG, "context empty");
        }
        //setOnClickListener(this);

    }

    //@Override
  /*  public void onClick(View v) {
        String stringToastMessage = "Nummer : " + btnCounter + "\nText: " + viewText + "\nInhaltslabel " + contentDescription + "\nHint: " + hintText;
        Toast toast = Toast.makeText(context, stringToastMessage, Toast.LENGTH_LONG);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(30);
        View toastView = toast.getView();


        TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(25);
        toastMessage.setTextColor(Color.RED);
        toastMessage.setGravity(Gravity.CENTER);
        toastMessage.setCompoundDrawablePadding(16);
        toastView.setBackgroundColor(Color.CYAN);
        //toast.show();


        Log.v(LOG_TAG, contentDescription);

        if(testText!=null){
            testText.setText(contentDescription);
        }
        else {
            testText = findViewById(R.id.textView2);
            Log.v(LOG_TAG, "empty text");
        }

    }
*/


    public String showContent(){
        return this.contentDescription;
    }

    public String showInformation(){
        String InformationString = "<b>Element Nummer:</b> " + btnCounter + "<br><b>View Text: </b>" + viewText + "<br><b>Inhaltslabel:  </b>" + contentDescription + "<br><b>Hint: </b>" + hintText;

        return InformationString;
    }

}
