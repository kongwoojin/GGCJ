package com.kongjak.ggcj.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kongjak.ggcj.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Integer getYear, getMonth, getDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        getYear = Integer.valueOf(year.format(date));

        SimpleDateFormat month = new SimpleDateFormat("MM");
        getMonth = Integer.valueOf(month.format(date));

        SimpleDateFormat day = new SimpleDateFormat("dd");
        getDay = Integer.valueOf(day.format(date));

        setView();
    }

    private void setView() {
        String getDate = getYear + "-" + getMonth + "-" + getDay;

        SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
        String lunch_str = lunch_sp.getString(getDate, "");

        SharedPreferences dinner_sp = getSharedPreferences("dinner", MODE_PRIVATE);
        String dinner_str = dinner_sp.getString(getDate, "");

        SharedPreferences schedule_sp = getSharedPreferences("schedule", MODE_PRIVATE);
        String schedule_str = schedule_sp.getString(getDate, "");

        TextView lunch_v = (TextView) findViewById(R.id.item_lunch);
        lunch_v.setVisibility(View.VISIBLE);

        TextView dinner_v = (TextView) findViewById(R.id.item_dinner);
        dinner_v.setVisibility(View.VISIBLE);

        TextView schedule_v = (TextView) findViewById(R.id.item_schedule);
        schedule_v.setVisibility(View.VISIBLE);

        Log.d("Parse", lunch_str);
        Log.d("Parse", dinner_str);

        if (!lunch_str.isEmpty()) {
            lunch_v.setText(lunch_str);
            dinner_v.setText(dinner_str);
            schedule_v.setText(schedule_str);
        } else {
            lunch_v.setText(getString(R.string.no_lunch_data));
            dinner_v.setText(getString(R.string.no_dinner_data));
            schedule_v.setText(getString(R.string.no_schedule_data));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            ActivityCompat.finishAffinity(this);
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notice) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_url));
            intent.putExtra("type", 0);
            startActivity(intent);
        } else if (id == R.id.nav_notice_parents) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_parents_url));
            intent.putExtra("type", 1);
            startActivity(intent);
        } else if (id == R.id.nav_date) {
            Intent intent = new Intent(getBaseContext(), DateActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("plain/text");
            String[] address = {"ggcj@kongjak.com"};
            email.putExtra(Intent.EXTRA_EMAIL, address);
            startActivity(email);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(getBaseContext(), InfoActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_home);
        setView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_web) {
            String url = "http://ggcj.hs.kr";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
        }

        return super.onOptionsItemSelected(item);
    }
}
