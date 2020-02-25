package com.kongjak.ggcj.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kongjak.ggcj.R;
import com.kongjak.ggcj.Tools.CheckDigit;
import com.kongjak.ggcj.Tools.ParseMeal;
import com.kongjak.ggcj.Tools.ParseSchedule;

public class DateReadActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private String curDate, dayOfMonth, month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_read);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction("MealParseEnd");
        filter.addAction("ScheduleParseEnd");
        registerReceiver(mBroadcastReceiver, filter);

        dayOfMonth = CheckDigit.check(getIntent().getStringExtra("dayOfMonth"));
        month = CheckDigit.check(getIntent().getStringExtra("month"));
        year = getIntent().getStringExtra("year");
        curDate = year + "-" + month + "-" + dayOfMonth;
        System.out.println(curDate);

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

        String today = String.format(getResources().getString(R.string.date), year, month, dayOfMonth);

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
        }

        if (!schedule_str.isEmpty()) {
            schedule_v.setText(schedule_str);
        } else {
            getSchedule();
        }
    }

    public void getMeal() {
        ParseMeal.WeekMealTask lunchTask = new ParseMeal.WeekMealTask(DateReadActivity.this);
        lunchTask.execute("2", year, month, dayOfMonth);

        ParseMeal.WeekMealTask dinnerTask = new ParseMeal.WeekMealTask(DateReadActivity.this);
        dinnerTask.execute("3", year, month, dayOfMonth);
    }

    public void getSchedule() {
        ParseSchedule.ScheduleTask asyncTask = new ParseSchedule.ScheduleTask(DateReadActivity.this);
        asyncTask.execute(year, month);
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


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GGCJ", "Received ParseEnd");
            setView();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
