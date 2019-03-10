package com.kongjak.ggcj.Activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.kongjak.ggcj.R;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class InfoPreference extends PreferenceFragment {

    private Preference.OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String getKey = preference.getKey();
            if ("allSource".equals(getKey)) {
                final Notices notices = new Notices();
                notices.addNotice(new Notice("GGCJ", "https://github.com/kongwoojin/ggcj", "Copyright (c) 2019 WooJin Kong", new MITLicense()));
                notices.addNotice(new Notice("NIES API", "https://github.com/agemor/neis-api", "Copyright (c) 2016 HyunJun Kim and other contributers.", new MITLicense()));
                notices.addNotice(new Notice("jsoup", "https://jsoup.org", "Copyright © 2009 - 2017 Jonathan Hedley (jonathan@hedley.net)", new MITLicense()));
                notices.addNotice(new Notice("Material Calendar View", "https://github.com/prolificinteractive/material-calendarview", "Copyright (c) 2018 Prolific Interactive", new MITLicense()));

                new LicensesDialog.Builder(getActivity())
                        .setNotices(notices)
                        .setIncludeOwnLicense(true)
                        .build()
                        .show();
            }
            return true;

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.info);
        setOnPreferenceClick(findPreference("allSource"));
    }

    private void setOnPreferenceClick(Preference mPreference) {
        mPreference.setOnPreferenceClickListener(onPreferenceClickListener);
    }
}
