package com.knobbers.matriculapas;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Intro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //REFERENCES
        ImageView iv2 = findViewById(R.id.imageView2);
        ImageView iv = findViewById(R.id.imageView);
        TextView tv2 = findViewById(R.id.textView2);
        TextView tv = findViewById(R.id.textView);

        //ANIMATION
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        iv.startAnimation(anim);
        iv2.startAnimation(anim);
        tv.startAnimation(anim);
        tv2.startAnimation(anim);

        //SET FONT
        Typeface font = Typeface.createFromAsset(getAssets(), "lovelo.otf");
        (tv2).setTypeface(font);
        (tv).setTypeface(font);


        //DELAY
        int SPLASH_SCREEN_TIME = 5000;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent act = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(act);
            overridePendingTransition(R.anim.left_in, R.anim.left_out); // (IN, OUT)
            finish();
        }, SPLASH_SCREEN_TIME);
    }

}
