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

public class ParseMeal {

    public static class WeekMealTask extends AsyncTask<String, String, Void> {

        Context context;
        ProgressDialog asyncDialog;

        public WeekMealTask(Context mContext) {
            context = mContext;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent intent = new Intent("MealParseEnd");
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
            String meal_type = null;
            String year = params[1];
            String month = params[2];
            String no_meal = null;

            if (params[0].equals("2")) {
                meal_type = "lunch";
                no_meal = context.getString(R.string.no_lunch);
            } else if (params[0].equals("3")) {
                meal_type = "dinner";
                no_meal = context.getString(R.string.no_dinner);
            }

            try {
                String date = year + month;
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("open.neis.go.kr")
                        .appendPath("hub")
                        .appendPath("mealServiceDietInfo")
                        .appendQueryParameter("KEY", context.getString(R.string.api_key))
                        .appendQueryParameter("ATPT_OFCDC_SC_CODE", context.getString(R.string.api_region_code))
                        .appendQueryParameter("SD_SCHUL_CODE", context.getString(R.string.api_school_code))
                        .appendQueryParameter("MMEAL_SC_CODE", params[0])
                        .appendQueryParameter("MLSV_YMD", date);
                String meal_url = builder.build().toString();

                Document doc = Jsoup.connect(meal_url).get();

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

                    SharedPreferences sp = context.getSharedPreferences(meal_type, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(date_pref, no_meal);
                    editor.apply();
                }

                if (isClassExits) {
                    Elements element = doc.select("mealServiceDietInfo");
                    for (Element e : element) {
                        int count = Integer.parseInt(e.select("list_total_count").get(0).text());
                        for (int i = 0; i < count; i++) {
                            String meals = e.select("DDISH_NM").get(i).text();
                            String date_parsed = e.select("MLSV_YMD").get(i).text();
                            String year_parsed = date_parsed.substring(0, 4);
                            String month_parsed = date_parsed.substring(4, 6);
                            String day_parsed = date_parsed.substring(6, 8);

                            String date_pref1 = year_parsed + "-" + month_parsed + "-" + day_parsed;

                            meals = meals.replace("<br/>", "\n");

                            SharedPreferences sp = context.getSharedPreferences(meal_type, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(date_pref1, meals);
                            editor.apply();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
