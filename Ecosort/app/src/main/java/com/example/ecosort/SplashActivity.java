package com.example.ecosort;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 3000; // 3 secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView splashText = findViewById(R.id.splashText);

        // Juste pour API >= 26, sinon ignorer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            splashText.setJustificationMode(android.text.Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        // Démarrer animation fade-in après que la vue soit attachée
        splashText.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(splashText, "alpha", 0f, 1f);
                        fadeIn.setDuration(2000); // 2s pour fade-in
                        fadeIn.start();
                        splashText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );

        // Redirection vers MainActivity après SPLASH_DURATION
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}