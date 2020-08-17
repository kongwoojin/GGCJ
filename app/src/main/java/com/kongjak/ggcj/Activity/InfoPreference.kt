package com.kongjak.ggcj.Activity

import android.os.Bundle
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceFragment
import com.kongjak.ggcj.R
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.BSD3ClauseLicense
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

class InfoPreference : PreferenceFragment() {
    private val onPreferenceClickListener = OnPreferenceClickListener { preference ->
        val getKey = preference.key
        if ("allSource" == getKey) {
            val notices = Notices()
            notices.addNotice(Notice("GGCJ", "https://github.com/kongwoojin/ggcj", "Copyright (c) 2019 WooJin Kong", MITLicense()))
            notices.addNotice(Notice("jsoup", "https://jsoup.org", "Copyright © 2009 - 2020 Jonathan Hedley (jonathan@hedley.net)", MITLicense()))
            notices.addNotice(Notice("Material Calendar View", "https://github.com/prolificinteractive/material-calendarview", "Copyright (c) 2018 Prolific Interactive", MITLicense()))
            notices.addNotice(Notice("AppIntro library", "https://github.com/AppIntro/AppIntro", "Copyright 2015 Paolo Rotolo\nCopyright 2018 APL Devs", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Glide", "https://github.com/bumptech/glide", "Copyright 2014 Google, Inc. All rights reserved.", BSD3ClauseLicense()))
            LicensesDialog.Builder(activity)
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show()
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.info)
        setOnPreferenceClick(findPreference("allSource"))
    }

    private fun setOnPreferenceClick(mPreference: Preference) {
        mPreference.onPreferenceClickListener = onPreferenceClickListener
    }
}