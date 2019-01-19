package com.kongjak.ggcj.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.kongjak.ggcj.R;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class InfoActivity extends PreferenceActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.info);
        setOnPreferenceClick(findPreference("allSource"));

        setContentView(R.layout.activity_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setNav();
    }

    public void setNav() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_info);
    }

    private void setOnPreferenceClick(Preference mPreference) {
        mPreference.setOnPreferenceClickListener(onPreferenceClickListener);
    }

    private Preference.OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String getKey = preference.getKey();
            if ("allSource".equals(getKey)) {
                final Notices notices = new Notices();
                notices.addNotice(new Notice("GGCJ", "https://github.com/kongwoojin/ggcj", "Copyright (c) 2019 WooJin Kong", new MITLicense()));
                notices.addNotice(new Notice("School API", "https://github.com/agemor/neis-api", "Copyright (c) 2016 HyunJun Kim and other contributers.", new MITLicense()));

                new LicensesDialog.Builder(InfoActivity.this)
                        .setNotices(notices)
                        .setIncludeOwnLicense(true)
                        .build()
                        .show();
            }
            return true;

        }
    };

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_notice) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_url));
            intent.putExtra("type", 0);
            startActivity(intent);
        } else if (id == R.id.nav_notice_parents) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_parents_url));
            intent.putExtra("type", 1);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("plain/text");
            String[] address = {"ggcj@kongjak.com"};
            email.putExtra(Intent.EXTRA_EMAIL, address);
            startActivity(email);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNav();
    }
}