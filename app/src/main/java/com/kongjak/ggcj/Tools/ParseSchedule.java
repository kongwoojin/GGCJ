package com.kongjak.ggcj.Tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.kongjak.ggcj.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

public class ParseSchedule {

    public static class ScheduleTask extends AsyncTask<String, String, Void> {

        Context context;
        ProgressDialog asyncDialog;

        public ScheduleTask(Context mContext) {
            context = mContext;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent intent = new Intent("ScheduleParseEnd");
            context.sendBroadcast(intent);

            asyncDialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            asyncDialog = new ProgressDialog(context);
            String loading = context.getString(R.string.loading);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage(loading);

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            //백그라운드 작업이 진행되는 곳.
            String school_code = context.getString(R.string.school_code);
            String school_type = context.getString(R.string.school_type);
            int day = 0;

            String year = params[0];
            String month = params[1];

            Document doc;
            String schedule_url = String.format(context.getString(R.string.neis_schedule), "goe", school_code, school_type, year, month);
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
                        SharedPreferences schedule_sp = context.getSharedPreferences("schedule", MODE_PRIVATE);
                        SharedPreferences.Editor schedule_editor = schedule_sp.edit();
                        if (schedule_txt.toString().isEmpty())
                            schedule_editor.putString(Date, context.getString(R.string.no_schedule));
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
