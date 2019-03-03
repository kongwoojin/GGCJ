package com.kongjak.ggcj.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kongjak.ggcj.R;

import org.hyunjun.school.School;
import org.hyunjun.school.SchoolException;
import org.hyunjun.school.SchoolMenu;
import org.hyunjun.school.SchoolSchedule;

import java.util.List;

public class DateReadActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private Integer dayOfMonth, month, year;
    private Thread th;
    private String curDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_read);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dayOfMonth = getIntent().getIntExtra("dayOfMonth", 0);
        month = getIntent().getIntExtra("month", 0);
        year = getIntent().getIntExtra("year", 0);
        curDate = year + "-" + month + "-" + dayOfMonth;

        setView();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    public void setView() {
        SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
        String lunch_str = lunch_sp.getString(curDate, "");

        SharedPreferences dinner_sp = getSharedPreferences("dinner", MODE_PRIVATE);
        String dinner_str = dinner_sp.getString(curDate, "");

        SharedPreferences schedule_sp = getSharedPreferences("schedule", MODE_PRIVATE);
        String schedule_str = schedule_sp.getString(curDate, "");

        String today = String.format(getResources().getString(R.string.date),year,month,dayOfMonth);

        TextView date_v = (TextView) findViewById(R.id.item_date);
        date_v.setText(today);

        TextView lunch_v = (TextView) findViewById(R.id.item_lunch);
        lunch_v.setVisibility(View.VISIBLE);

        TextView dinner_v = (TextView) findViewById(R.id.item_dinner);
        dinner_v.setVisibility(View.VISIBLE);

        TextView schedule_v = (TextView) findViewById(R.id.item_schedule);
        schedule_v.setVisibility(View.VISIBLE);

        if (!lunch_str.isEmpty()) {
            lunch_v.setText(lunch_str);
            dinner_v.setText(dinner_str);
        } else {
            getMeal();
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setView();
        }

        if (!schedule_str.isEmpty()) {
            schedule_v.setText(schedule_str);
        } else {
            getSchedule();
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setView();
        }
    }

    public void getMeal() {
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                String school_code = getString(R.string.school_code);
                School api = new School(School.Type.HIGH, School.Region.GYEONGGI, school_code);

                String lunch, dinner;
                try {
                    List<SchoolMenu> menu = api.getMonthlyMenu(year, month);

                    for (int i = 0; i < menu.size(); i++) { // loop

                        String Date = year + "-" + month + "-" + (i + 1);

                        if (menu.get(i).lunch.equals("급식이 없습니다")) {
                            lunch = getString(R.string.no_lunch);
                        } else {
                            lunch = menu.get(i).lunch;
                            lunch = lunch.replace("&amp;", "&"); // Replace &amp; to &
                        }

                        if (menu.get(i).dinner.equals("급식이 없습니다")) {
                            dinner = getString(R.string.no_dinner);
                        } else {
                            dinner = menu.get(i).dinner;
                            dinner = dinner.replace("&amp;", "&"); // Replace &amp; to &
                        }

                        SharedPreferences lunch_sp = getSharedPreferences("lunch", MODE_PRIVATE);
                        SharedPreferences.Editor lunch_editor = lunch_sp.edit();
                        lunch_editor.putString(Date, lunch);
                        lunch_editor.apply();

                        SharedPreferences dinner_sp = getSharedPreferences("dinner", MODE_PRIVATE);
                        SharedPreferences.Editor dinner_editor = dinner_sp.edit();
                        dinner_editor.putString(Date, dinner);
                        dinner_editor.apply();
                    }
                } catch (SchoolException e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();
    }

    public void getSchedule() {
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                String school_code = getString(R.string.school_code);
                School api = new School(School.Type.HIGH, School.Region.GYEONGGI, school_code);

                String schedule;
                try {
                    List<SchoolSchedule> scheduleList = api.getMonthlySchedule(year, month);

                    for (int i = 0; i < scheduleList.size(); i++) {
                        System.out.println((i + 1) + "일 학사일정");
                        System.out.println(scheduleList.get(i));

                        String Date = year + "-" + month + "-" + (i + 1);

                        Log.d("Parse", Date);

                        if (scheduleList.get(i).schedule.isEmpty()) {
                            schedule = getString(R.string.no_schedule);
                        } else {
                            schedule = scheduleList.get(i).schedule;
                        }

                        SharedPreferences schedule_sp = getSharedPreferences("schedule", MODE_PRIVATE);
                        SharedPreferences.Editor schedule_editor = schedule_sp.edit();
                        schedule_editor.putString(Date, schedule);
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
    public void onRefresh() {
        getMeal();
        getSchedule();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
