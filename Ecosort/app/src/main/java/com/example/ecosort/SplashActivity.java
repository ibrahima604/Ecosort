package com.example.ecosort;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 3400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(0xFF0D1F1A);

        setContentView(R.layout.activity_splash);
        RetrofitClient.init(this);

        ImageView    logo          = findViewById(R.id.logo);
        View         logoRing      = findViewById(R.id.logoRing);
        View         ringMid       = findViewById(R.id.ringMid);
        View         ringOuter     = findViewById(R.id.ringOuter);
        View         lineTop       = findViewById(R.id.lineTop);
        View         lineBottom    = findViewById(R.id.lineBottom);
        TextView     labelCorner   = findViewById(R.id.labelCorner);
        TextView     labelVersion  = findViewById(R.id.labelVersion);
        LinearLayout centerBlock   = findViewById(R.id.centerContainer);
        LinearLayout bottomBlock   = findViewById(R.id.bottomContainer);
        View         progressBar   = findViewById(R.id.progressBar);
        TextView     percentText   = findViewById(R.id.percentText);

        // ── Logo : zoom overshoot ──────────────────────────────
        logo.setAlpha(0f);
        logo.setScaleX(0.2f);
        logo.setScaleY(0.2f);
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(
                ObjectAnimator.ofFloat(logo, "alpha",  0f, 1f),
                ObjectAnimator.ofFloat(logo, "scaleX", 0.2f, 1f),
                ObjectAnimator.ofFloat(logo, "scaleY", 0.2f, 1f)
        );
        logoAnim.setDuration(750);
        logoAnim.setInterpolator(new OvershootInterpolator(1.4f));

        // ── Anneaux : rotation continue ───────────────────────
        ObjectAnimator rotInner = ObjectAnimator.ofFloat(logoRing, "rotation", 0f, 360f);
        rotInner.setDuration(6000);
        rotInner.setRepeatCount(ValueAnimator.INFINITE);
        rotInner.setInterpolator(new LinearInterpolator());

        ObjectAnimator rotMid = ObjectAnimator.ofFloat(ringMid, "rotation", 360f, 0f);
        rotMid.setDuration(10000);
        rotMid.setRepeatCount(ValueAnimator.INFINITE);
        rotMid.setInterpolator(new LinearInterpolator());

        ObjectAnimator rotOuter = ObjectAnimator.ofFloat(ringOuter, "rotation", 0f, 360f);
        rotOuter.setDuration(18000);
        rotOuter.setRepeatCount(ValueAnimator.INFINITE);
        rotOuter.setInterpolator(new LinearInterpolator());

        // ── Bloc centre : slide-up fade ────────────────────────
        centerBlock.setAlpha(0f);
        centerBlock.setTranslationY(50f);
        AnimatorSet centerAnim = new AnimatorSet();
        centerAnim.playTogether(
                ObjectAnimator.ofFloat(centerBlock, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(centerBlock, "translationY", 50f, 0f)
        );
        centerAnim.setDuration(700);
        centerAnim.setStartDelay(350);
        centerAnim.setInterpolator(new DecelerateInterpolator(2.5f));

        // ── Lignes décoratives : scale-in ─────────────────────
        lineTop.setScaleX(0f);
        lineBottom.setScaleX(0f);
        ObjectAnimator lineTopAnim    = ObjectAnimator.ofFloat(lineTop,    "scaleX", 0f, 1f);
        ObjectAnimator lineTopAlpha   = ObjectAnimator.ofFloat(lineTop,    "alpha",  0f, 1f);
        ObjectAnimator lineBottomAnim = ObjectAnimator.ofFloat(lineBottom, "scaleX", 0f, 1f);
        ObjectAnimator lineBottomAlpha= ObjectAnimator.ofFloat(lineBottom, "alpha",  0f, 1f);
        for (ObjectAnimator a : new ObjectAnimator[]{lineTopAnim, lineTopAlpha, lineBottomAnim, lineBottomAlpha}) {
            a.setDuration(600);
            a.setStartDelay(600);
            a.setInterpolator(new DecelerateInterpolator());
        }

        // ── Labels coins : fade ────────────────────────────────
        ObjectAnimator cornerFade  = ObjectAnimator.ofFloat(labelCorner,  "alpha", 0f, 1f);
        ObjectAnimator versionFade = ObjectAnimator.ofFloat(labelVersion, "alpha", 0f, 1f);
        cornerFade.setDuration(500);  cornerFade.setStartDelay(700);
        versionFade.setDuration(500); versionFade.setStartDelay(750);

        // ── Bas : fade + progression ───────────────────────────
        bottomBlock.setAlpha(0f);
        ObjectAnimator bottomFade = ObjectAnimator.ofFloat(bottomBlock, "alpha", 0f, 1f);
        bottomFade.setDuration(400);
        bottomFade.setStartDelay(900);

        int trackDp = 140;
        ValueAnimator progressAnim = ValueAnimator.ofInt(0, trackDp);
        progressAnim.setDuration(2200);
        progressAnim.setStartDelay(1000);
        progressAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnim.addUpdateListener(anim -> {
            int val = (int) anim.getAnimatedValue();
            progressBar.getLayoutParams().width = dpToPx(val);
            progressBar.requestLayout();
            int pct = (int) ((val / (float) trackDp) * 100);
            percentText.setText(pct + "%");
        });

        // ── Lancement ──────────────────────────────────────────
        logoAnim.start();
        rotInner.start();
        rotMid.start();
        rotOuter.start();
        centerAnim.start();
        lineTopAnim.start();   lineTopAlpha.start();
        lineBottomAnim.start(); lineBottomAlpha.start();
        cornerFade.start();
        versionFade.start();
        bottomFade.start();
        progressAnim.start();

        // ── Transition vers MainActivity ───────────────────────
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            View root = findViewById(android.R.id.content);
            root.animate()
                    .alpha(0f)
                    .setDuration(450)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    })
                    .start();
        }, SPLASH_DURATION);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}