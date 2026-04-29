package com.example.ecosort;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialisation Retrofit avec l'URL de strings.xml
        RetrofitClient.init(this);

        TextView splashText = findViewById(R.id.splashText);
        ImageView logo      = findViewById(R.id.logo);

        // Justification texte API >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            splashText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        // Animation logo : scale + fade in
        logo.setAlpha(0f);
        logo.setScaleX(0.5f);
        logo.setScaleY(0.5f);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .start();

        // Animation texte : fade in après que la vue soit attachée
        splashText.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(
                                splashText, "alpha", 0f, 1f);
                        fadeIn.setDuration(2000);
                        fadeIn.start();
                        splashText.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                }
        );

        // Redirection après SPLASH_DURATION
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}