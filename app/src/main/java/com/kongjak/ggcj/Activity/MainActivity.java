package com.kongjak.ggcj.Activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kongjak.ggcj.R;
import com.kongjak.ggcj.Tools.CheckDigit;
import com.kongjak.ggcj.Tools.ParseMeal;
import com.kongjak.ggcj.Tools.ParseSchedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String getYear, getMonth, getDay, getDate;
    private String next_schedule_day = "", next_schedule = "";

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkFirstRun();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        IntentFilter filter = new IntentFilter();
        filter.addAction("MealParseEnd");
        filter.addAction("ScheduleParseEnd");
        registerReceiver(mBroadcastReceiver, filter);

        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.KOREA);
        getYear = year.format(date);

        SimpleDateFormat month = new SimpleDateFormat("MM", Locale.KOREA);
        getMonth = CheckDigit.check(month.format(date));

        SimpleDateFormat day = new SimpleDateFormat("dd", Locale.KOREA);
        getDay = CheckDigit.check(day.format(date));

        setView();
    }

    private void checkFirstRun() {
        SharedPreferences sp = getSharedPreferences("AppIntro", MODE_PRIVATE);
        if (!sp.getBoolean("first", false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("first", true);
            editor.apply();
            Intent intent = new Intent(this, IntroActivity.class); // Call the AppIntro java class
            startActivity(intent);
        }
    }

    private void setView() {
        getDate = getYear + "-" + getMonth + "-" + getDay;
        checkMeal();
        checkSchedule();
    }

    private void checkMeal() {
        SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
        String lunch_str = lunch_sp.getString(getDate, "");

        if (!lunch_str.isEmpty()) {
            setMealView();
        } else {
            EmptyDialog(getString(R.string.meal));
        }
    }

    private void setMealView() {
        SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
        String lunch_str = lunch_sp.getString(getDate, "");

        SharedPreferences dinner_sp = getSharedPreferences("dinner", MODE_PRIVATE);
        String dinner_str = dinner_sp.getString(getDate, "");

        TextView lunch_v = findViewById(R.id.item_lunch);
        lunch_v.setVisibility(View.VISIBLE);

        TextView dinner_v = findViewById(R.id.item_dinner);
        dinner_v.setVisibility(View.VISIBLE);

        lunch_v.setText(lunch_str);
        dinner_v.setText(dinner_str);
    }

    private void checkSchedule() {
        SharedPreferences schedule_sp = getSharedPreferences("schedule", MODE_PRIVATE);
        String schedule_str = schedule_sp.getString(getDate, "");

        TextView schedule_v = findViewById(R.id.item_schedule);
        schedule_v.setVisibility(View.VISIBLE);

        if (!schedule_str.isEmpty()) {
            setScheduleView();
        } else {
            EmptyDialog(getString(R.string.schedule));
        }
    }

    private void setScheduleView() {
        SharedPreferences schedule_sp = getSharedPreferences("schedule", MODE_PRIVATE);
        String schedule_str = schedule_sp.getString(getDate, "");

        TextView schedule_v = findViewById(R.id.item_schedule);
        schedule_v.setVisibility(View.VISIBLE);

        if (schedule_str.equals(getString(R.string.no_schedule))) {
            NextSchedule asyncTask = new NextSchedule();
            asyncTask.execute();
        } else {
            schedule_v.setText(schedule_str);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(getBaseContext(), GalleryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_timetable) {
            String url = "http://comci.kr/st";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    private void EmptyDialog(String dataType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format(getString(R.string.data_empty_dialog), dataType));
        builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
            if (dataType.equals(getString(R.string.meal))) {
                getDatas(0);
            } else if (dataType.equals(getString(R.string.schedule))) {
                getDatas(1);
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.dismiss());
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void getDatas(int type) {
        if (type == 0) {
            ParseMeal.WeekMealTask lunchTask = new ParseMeal.WeekMealTask(MainActivity.this);
            lunchTask.execute("2", getYear, getMonth, getDay);

            ParseMeal.WeekMealTask dinnerTask = new ParseMeal.WeekMealTask(MainActivity.this);
            dinnerTask.execute("3", getYear, getMonth, getDay);
        } else if (type == 1) {
            ParseSchedule.ScheduleTask asyncTask = new ParseSchedule.ScheduleTask(MainActivity.this);
            asyncTask.execute(getYear, getMonth);
        }
    }

    private class NextSchedule extends AsyncTask<String, String, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(
                MainActivity.this);

        @Override
        protected void onPostExecute(Void result) {
            //doInBackground 작업이 끝나고 난뒤의 작업
            TextView schedule_v = findViewById(R.id.item_schedule);
            if (next_schedule.isEmpty()) {
                schedule_v.setText(getString(R.string.no_next_schedule));
            } else {
                schedule_v.setText(String.format(getString(R.string.next_schedule), next_schedule, next_schedule_day));
            }
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            String loading = getString(R.string.loading);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage(loading);

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            //백그라운드 작업이 진행되는 곳.
            int today = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_MONTH);
            int last_day = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
            try {
                for (int i = today + 1; i <= last_day; i++) {
                    String getFullDate = getYear + "-" + getMonth + "-" + CheckDigit.check(String.valueOf(i));
                    String getDate = getMonth + "/" + CheckDigit.check(String.valueOf(i));

                    SharedPreferences schedule_sp = getSharedPreferences("schedule", MODE_PRIVATE);
                    String schedule_str = schedule_sp.getString(getFullDate, "");

                    if (!schedule_str.equals(getString(R.string.no_schedule))) {
                        publishProgress(getDate, schedule_str); // Send it!
                        break;
                    }

                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... params) { // Receive from doInBackground
            next_schedule_day = params[0];
            next_schedule = params[1];
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("MealParseEnd")) {
                setMealView();
            } else if (intent.getAction().equals("ScheduleParseEnd")) {
                setScheduleView();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_home);
        Log.d("GGCJ", "Resume");
        setMealView();
        setScheduleView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
