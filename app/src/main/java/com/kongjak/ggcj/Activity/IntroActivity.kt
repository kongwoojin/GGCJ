package com.kongjak.ggcj.Activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.kongjak.ggcj.R

class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_1_title), getString(R.string.intro_1_desc), R.drawable.ggcj_intro_1, Color.parseColor("#5d4037")))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_2_title), getString(R.string.intro_2_desc), R.drawable.ggcj_intro_2, Color.parseColor("#5d4037")))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_3_title), getString(R.string.intro_3_desc), R.drawable.ggcj_intro_3, Color.parseColor("#5d4037")))

        setTransformer(AppIntroPageTransformerType.Parallax())
    }

    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        val sp = getSharedPreferences("AppIntro", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean("first", true)
        editor.apply()
        finish()
    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val sp = getSharedPreferences("AppIntro", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean("first", true)
        editor.apply()
        finish()
    }
}