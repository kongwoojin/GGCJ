package com.kongjak.ggcj.Activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kongjak.ggcj.R;
import com.kongjak.ggcj.Tools.CheckDigit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

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
        WeekMealTask lunchTask = new WeekMealTask();
        lunchTask.execute("2");

        WeekMealTask dinnerTask = new WeekMealTask();
        dinnerTask.execute("3");
    }

    public void getSchedule() {
        ScheduleTask asyncTask = new ScheduleTask();
        asyncTask.execute();
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


    public class WeekMealTask extends AsyncTask<String, String, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(
                DateReadActivity.this);

        @Override
        protected void onPostExecute(Void result) {
            setView();
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
            String school_code = getString(R.string.school_code);
            String school_type = getString(R.string.school_type);
            String meal_type = null;
            String no_meal = null;

            if (params[0].equals("2")) {
                meal_type = "lunch";
                no_meal = getString(R.string.no_lunch);
            } else if (params[0].equals("3")) {
                meal_type = "dinner";
                no_meal = getString(R.string.no_dinner);
            }

            Document doc;
            String meal_url = String.format(getString(R.string.neis_meal), "stu", school_code, school_type, params[0], year, month, dayOfMonth);
            try {
                doc = Jsoup.connect(meal_url).get();
                Elements list = doc.select("#contents > div:nth-child(2) > table > thead > tr > th");
                int num = list.size();
                System.out.println(num);
                for (int i = 2; i <= num; i++) {
                    Elements meal = doc.select("#contents > div:nth-child(2) > table > tbody > tr:nth-child(2) > td:nth-child(" + i + ")");
                    Elements date = doc.select("#contents > div:nth-child(2) > table > thead > tr > th:nth-child(" + i + ")");
                    String Date = date.text().replaceAll("\\(.\\)", "").replace(".", "-");
                    String meals = meal.html().replace("<br>", "\n");
                    if (meals.isEmpty()) {
                        meals = no_meal;
                    } else {
                        meals = meals.replace("&amp;", "&").trim(); // Replace &amp; to &
                    }
                    System.out.println(Date);
                    SharedPreferences sp = getSharedPreferences(meal_type, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(Date, meals);
                    editor.apply();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ScheduleTask extends AsyncTask<String, String, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(
                DateReadActivity.this);

        @Override
        protected void onPostExecute(Void result) {
            //doInBackground 작업이 끝나고 난뒤의 작업
            Log.d("Parse", "End");
            setView();
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
            String school_code = getString(R.string.school_code);
            String school_type = getString(R.string.school_type);
            int day = 0;
            Document doc;
            String schedule_url = String.format(getString(R.string.neis_schedule), "stu", school_code, school_type, year, month);
            try {
                doc = Jsoup.connect(schedule_url).get();
                Elements scheduleAll = doc.select("#contents > div:nth-child(2) > table > tbody > tr");
                for (Element schedulesList : scheduleAll) {
                    Elements schedules = schedulesList.select("td > div");
                    for (Element schedule : schedules) {
                        StringBuilder schedule_txt = new StringBuilder();
                        Elements scheduleDate = schedule.select("em");
                        if (scheduleDate.text().isEmpty())
                            continue;
                        Elements scheduleA = schedule.select("a");
                        for (Element scA : scheduleA) {
                            Elements scheduleTxt = scA.select("strong");
                            schedule_txt.append(scheduleTxt.text()).append("\n");
                        }
                        day = day + 1;
                        String Date = year + "-" + month + "-" + CheckDigit.check(String.valueOf(day));
                        SharedPreferences schedule_sp = getSharedPreferences("schedule", MODE_PRIVATE);
                        SharedPreferences.Editor schedule_editor = schedule_sp.edit();
                        if (schedule_txt.toString().isEmpty())
                            schedule_editor.putString(Date, getString(R.string.no_schedule));
                        else
                            schedule_editor.putString(Date, schedule_txt.toString().trim());
                        schedule_editor.apply();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
