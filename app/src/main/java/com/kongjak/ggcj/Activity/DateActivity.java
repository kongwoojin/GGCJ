package com.kongjak.ggcj.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.kongjak.ggcj.R;
import com.kongjak.ggcj.Tools.SaturdayDecorator;
import com.kongjak.ggcj.Tools.SundayDecorator;
import com.kongjak.ggcj.Tools.TodayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.threeten.bp.format.DateTimeFormatter;

public class DateActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("d");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private MaterialCalendarView widget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_date);

        widget = (MaterialCalendarView) findViewById(R.id.calendarView);
        widget.setOnDateChangedListener(this);

        widget.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                new TodayDecorator());
    }

    @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay date,
            boolean selected) {
        Intent intent = new Intent(getBaseContext(), DateReadActivity.class);
        intent.putExtra("dayOfMonth", selected ? DAY_FORMATTER.format(date.getDate()) : "0");
        intent.putExtra("month", selected ? MONTH_FORMATTER.format(date.getDate()) : "0");
        intent.putExtra("year", selected ? YEAR_FORMATTER.format(date.getDate()) : "0");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

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
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(getBaseContext(), GalleryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_timetable) {
            String url = "http://comci.kr/st";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(DateActivity.this, Uri.parse(url));
        } else if (id == R.id.nav_send) {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:"));
            String[] address = {"ggcj@kongjak.com"};
            email.putExtra(Intent.EXTRA_EMAIL, address);
            String title = getResources().getString(R.string.nav_send_intent);
            Intent chooser = Intent.createChooser(email, title);
            if (email.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(getBaseContext(), InfoActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_date);
    }
}
