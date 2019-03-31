package com.kongjak.ggcj.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.kongjak.ggcj.R;

public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_1_title), getString(R.string.intro_1_desc), R.drawable.ggcj_intro_1, Color.parseColor("#5d4037")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_2_title), getString(R.string.intro_2_desc), R.drawable.ggcj_intro_2, Color.parseColor("#5d4037")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_3_title), getString(R.string.intro_3_desc), R.drawable.ggcj_intro_3, Color.parseColor("#5d4037")));

        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}