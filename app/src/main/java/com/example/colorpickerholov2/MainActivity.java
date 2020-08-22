package com.example.colorpickerholov2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
//import androidx.appcompat.widget.TextView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    RingColorPicker rcp;
    TextView red, green, blue;
    ValueLinearColorPicker value;
    SaturationLinearColorPicker sat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rcp = (RingColorPicker) findViewById(R.id.rcp);
        value = (ValueLinearColorPicker) findViewById(R.id.value);
        sat = (SaturationLinearColorPicker) findViewById(R.id.sat);
        red = (TextView) findViewById(R.id.red);
        green = (TextView) findViewById(R.id.green);
        blue = (TextView) findViewById(R.id.blue);

        rcp.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                String red_int = Integer.toString(Color.red(color));
                String green_int = Integer.toString(Color.green(color));
                String blue_int = Integer.toString(Color.blue(color));
                red.setText(red_int);
                green.setText(green_int);
                blue.setText(blue_int);

            }

        });
        rcp.setValueLinearColorPicker(value);
        rcp.setSaturationLinearColorPicker(sat);

    }
}
