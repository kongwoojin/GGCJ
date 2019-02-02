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

import org.hyunjun.school.School;
import org.hyunjun.school.SchoolException;
import org.hyunjun.school.SchoolMenu;
import org.hyunjun.school.SchoolSchedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String getDate;
    private Integer getYear, getMonth, getDay;
    private Thread th;

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
        getMonthSchedule();
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

        if (!lunch_str.equals("")) {
            lunch_v.setText(lunch_str);
            dinner_v.setText(dinner_str);
        } else {
            lunch_v.setText(getString(R.string.no_lunch));
            dinner_v.setText(getString(R.string.no_dinner));
        }

        if (!schedule_str.equals("")) {
            schedule_v.setText(schedule_str);
        } else {
            schedule_v.setText(getString(R.string.no_schedule_data));
        }
    }

    public void getWeekMeal() {
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                String school_code = getString(R.string.school_code);
                School api = new School(School.Type.HIGH, School.Region.GYEONGGI, school_code);
                try {
                    List<SchoolMenu> menu = api.getMonthlyMenu(getYear, getMonth);

                    for (int i = 0; i <= menu.size(); i++) { // loop
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat mdformat = new SimpleDateFormat("dd ");
                        calendar.add(Calendar.DAY_OF_YEAR, i);
                        Integer getDayP = Integer.valueOf(mdformat.format(calendar.getTime()).trim());

                        getDate = getYear + "-" + getMonth + "-" + getDayP;

                        Log.d("Parse", getDate);

                        SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
                        SharedPreferences.Editor lunch_editor = lunch_sp.edit();
                        lunch_editor.putString(getDate, menu.get(getDayP - 1).lunch);
                        lunch_editor.apply();

                        Log.d("Parse", getDayP + menu.get(getDayP - 1).lunch);

                        SharedPreferences dinner_sp = getSharedPreferences("dinner", MODE_PRIVATE);
                        SharedPreferences.Editor dinner_editor = dinner_sp.edit();
                        dinner_editor.putString(getDate, menu.get(getDayP - 1).dinner);
                        dinner_editor.apply();
                    }
                } catch (SchoolException e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();
    }

    public void getMonthSchedule() {
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                String school_code = getString(R.string.school_code);
                School api = new School(School.Type.HIGH, School.Region.GYEONGGI, school_code);

                String schedule;
                try {
                    List<SchoolSchedule> scheduleList = api.getMonthlySchedule(getYear, getMonth);

                    for (int i = 0; i < scheduleList.size(); i++) {
                        System.out.println((i + 1) + "일 학사일정");
                        System.out.println(scheduleList.get(i));

                        getDate = getYear + "-" + getMonth + "-" + (i + 1);

                        Log.d("Parse", getDate);

                        if (scheduleList.get(i).schedule.isEmpty()) {
                            schedule = getString(R.string.no_schedule);
                        } else {
                            schedule = scheduleList.get(i).schedule;
                        }

                        SharedPreferences schedule_sp = getSharedPreferences("schedule", MODE_PRIVATE);
                        SharedPreferences.Editor schedule_editor = schedule_sp.edit();
                        schedule_editor.putString(getDate, schedule);
                        schedule_editor.apply();
                    }
                } catch (SchoolException e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();
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

    public void getMeal(View view) {
        getWeekMeal();
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setView();
    }

    public void getSchedule(View view) {
        getMonthSchedule();
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
