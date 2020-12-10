package com.example.accessibilityserviceappv2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class CustomButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {

    /*
    Custom Button
     */

    String contentDescription;
    Context context;
    String logString;
    private static final String LOG_TAG = "ButtonActivity";


    public CustomButton(Context context) {
        this(context, null);
    }

    public CustomButton(Context context, String contentDescription) {
        super(context);
        init();
        this.contentDescription = contentDescription;
        this.context = context;
    }

    private void init(){
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(context, contentDescription, Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, contentDescription);
    }

}
