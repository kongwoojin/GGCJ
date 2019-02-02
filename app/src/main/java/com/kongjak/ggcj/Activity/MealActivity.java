package com.kongjak.ggcj.Activity;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.kongjak.ggcj.R;

import kr.go.neis.api.School;
import kr.go.neis.api.SchoolException;
import kr.go.neis.api.SchoolMenu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MealActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String getDate;
    private Integer getYear, getMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                getWeekMeal();
                cardAdd();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        getYear = Integer.valueOf(year.format(date));

        SimpleDateFormat month = new SimpleDateFormat("MM");
        getMonth = Integer.valueOf(month.format(date));
    }

    public void getWeekMeal() {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                String school_code = getString(R.string.school_code);
                School api = new School(School.Type.HIGH, School.Region.GYEONGGI, school_code);

                try {
                    List<SchoolMenu> menu = api.getMonthlyMenu(getYear, getMonth);

                    for (int i = 0; i <= 6; i++) { // loop
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat mdformat = new SimpleDateFormat("dd ");
                        calendar.add(Calendar.DAY_OF_YEAR, i);
                        Integer getDay = Integer.valueOf(mdformat.format(calendar.getTime()).trim());

                        getDate = getYear + "-" + getMonth + "-" + getDay;

                        Log.d("Parse", getDate);

                        SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
                        SharedPreferences.Editor lunch_editor = lunch_sp.edit();
                        lunch_editor.putString(getDate, menu.get(getDay - 1).lunch);
                        lunch_editor.apply();

                        Log.d("Parse", getDay + menu.get(getDay - 1).lunch);

                        SharedPreferences dinner_sp = getSharedPreferences("dinner", MODE_PRIVATE);
                        SharedPreferences.Editor dinner_editor = dinner_sp.edit();
                        dinner_editor.putString(getDate, menu.get(getDay - 1).dinner);
                        dinner_editor.apply();
                    }

                } catch (SchoolException e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();
    }

    public void getMeal(final Integer Year, final Integer Month, final Integer Day) {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                String school_code = getString(R.string.school_code);
                School api = new School(School.Type.HIGH, School.Region.GYEONGGI, school_code);

                try {
                    List<SchoolMenu> menu = api.getMonthlyMenu(Year, Month);

                    String Date = Year + "-" + Month + "-" + Day;

                    SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
                    SharedPreferences.Editor lunch_editor = lunch_sp.edit();
                    lunch_editor.putString(Date, menu.get(Day).lunch);
                    lunch_editor.apply();

                    Log.d("Parse", Day + menu.get(Day).lunch);

                    SharedPreferences dinner_sp = getSharedPreferences("dinner", MODE_PRIVATE);
                    SharedPreferences.Editor dinner_editor = dinner_sp.edit();
                    dinner_editor.putString(Date, menu.get(Day).dinner);
                    dinner_editor.apply();
                } catch (SchoolException e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();
    }

    public void cardAdd() {
        for (int i = 0; i <= 6; i++) { // loop
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("dd ");
            calendar.add(Calendar.DAY_OF_YEAR, i);
            Integer getDay = Integer.valueOf(mdformat.format(calendar.getTime()).trim());

            getDate = getYear + "-" + getMonth + "-" + getDay;

            SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
            String lunch_str = lunch_sp.getString(getDate, "");

            SharedPreferences dinner_sp = getSharedPreferences("dinner", MODE_PRIVATE);
            String dinner_str = dinner_sp.getString(getDate, "");

            realCardAdd(getString(R.string.lunch), lunch_str);
            realCardAdd(getString(R.string.dinner), dinner_str);
        }
    }

    public void realCardAdd(String type, String meal) {
        LinearLayout mLayout = (LinearLayout) findViewById(R.id.meallayout);

        // Initialize a new CardView
        CardView card = new CardView(getApplicationContext());

        // Set the CardView layoutParams
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        card.setLayoutParams(params);

        // Set cardView content padding
        card.setContentPadding(10, 0, 10, 0);

        // Set a background color for CardView
        card.setCardBackgroundColor(Color.parseColor("#ffffff"));

        // Set CardView elevation
        card.setCardElevation(5);

        // Initialize a new CardView
        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(params);

        card.addView(ll);

        TextView tvtype = new TextView(getApplicationContext());
        tvtype.setLayoutParams(params);
        tvtype.setText(type);

        TextView tvmeal = new TextView(getApplicationContext());
        tvmeal.setLayoutParams(params);
        tvmeal.setText(meal);

        ll.addView(tvtype);
        ll.addView(tvmeal);

        mLayout.addView(card);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.meal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            getMeal(year, monthOfYear + 1, dayOfMonth - 1);

                            Log.d("Parse", String.valueOf(year));
                            Log.d("Parse", String.valueOf(monthOfYear + 1));
                            Log.d("Parse", String.valueOf(dayOfMonth));
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
