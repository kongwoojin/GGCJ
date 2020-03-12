package com.kongjak.ggcj.Tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;

import com.kongjak.ggcj.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
            String year = params[0];
            String month = params[1];

            try {
                String date = year + month;
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("open.neis.go.kr")
                        .appendPath("hub")
                        .appendPath("SchoolSchedule")
                        .appendQueryParameter("KEY", context.getString(R.string.api_key))
                        .appendQueryParameter("ATPT_OFCDC_SC_CODE", context.getString(R.string.api_region_code))
                        .appendQueryParameter("SD_SCHUL_CODE", context.getString(R.string.api_school_code))
                        .appendQueryParameter("AA_YMD", date);
                String schedule_url = builder.build().toString();

                Document doc = Jsoup.connect(schedule_url).get();

                boolean isClassExits = false;

                Elements code = doc.select("* > RESULT >CODE");

                if (code.text().equals("INFO-000")) {
                    isClassExits = true;
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
                Date convertedDate = dateFormat.parse(date);
                Calendar c = Calendar.getInstance();
                c.setTime(convertedDate);

                int last_day = c.getActualMaximum(Calendar.DAY_OF_MONTH);

                for (int i = 1; i <= last_day; i++) {
                    String date_pref = year + "-" + month + "-" + CheckDigit.check(String.valueOf(i));
                    SharedPreferences schedule_sp = context.getSharedPreferences("schedule", MODE_PRIVATE);
                    SharedPreferences.Editor schedule_editor = schedule_sp.edit();
                    schedule_editor.putString(date_pref, context.getString(R.string.no_schedule));
                    schedule_editor.apply();
                }

                if (isClassExits) {
                    Elements element = doc.select("SchoolSchedule");
                    for (Element e : element) {
                        int count = Integer.parseInt(e.select("list_total_count").get(0).text());
                        for (int i = 0; i < count; i++) {
                            String schedule = e.select("EVENT_NM").get(i).text();
                            String date_parsed = e.select("AA_YMD").get(i).text();
                            String year_parsed = date_parsed.substring(0, 4);
                            String month_parsed = date_parsed.substring(4, 6);
                            String day_parsed = date_parsed.substring(6, 8);

                            String date_pref1 = year_parsed + "-" + month_parsed + "-" + day_parsed;

                            schedule = schedule.replace("<br/>", "\n");

                            SharedPreferences schedule_sp = context.getSharedPreferences("schedule", MODE_PRIVATE);
                            SharedPreferences.Editor schedule_editor = schedule_sp.edit();
                            schedule_editor.putString(date_pref1, schedule);
                            schedule_editor.apply();

                        }
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
