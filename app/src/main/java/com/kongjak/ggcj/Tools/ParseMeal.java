package com.kongjak.ggcj.Tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.kongjak.ggcj.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

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
            //백그라운드 작업이 진행되는 곳.
            String school_code = context.getString(R.string.school_code);
            String school_type = context.getString(R.string.school_type);
            String meal_type = null;
            String no_meal = null;
            String res_kcal = context.getString(R.string.kcal);

            if (params[0].equals("2")) {
                meal_type = "lunch";
                no_meal = context.getString(R.string.no_lunch);
            } else if (params[0].equals("3")) {
                meal_type = "dinner";
                no_meal = context.getString(R.string.no_dinner);
            }

            String year = params[1];
            String month = params[2];
            String dayOfMonth = params[3];

            Document doc;
            String meal_url = String.format(context.getString(R.string.neis_meal), "stu", school_code, school_type, params[0], year, month, dayOfMonth);
            try {
                doc = Jsoup.connect(meal_url).get();
                Elements list = doc.select("#contents > div:nth-child(2) > table > thead > tr > th");
                int num = list.size();
                System.out.println(num);
                for (int i = 2; i <= num; i++) {
                    Elements meal = doc.select("#contents > div:nth-child(2) > table > tbody > tr:nth-child(2) > td:nth-child(" + i + ")");
                    Elements date = doc.select("#contents > div:nth-child(2) > table > thead > tr > th:nth-child(" + i + ")");
                    Elements kcal = doc.select("#contents > div:nth-child(2) > table > tbody > tr:nth-child(45) > td:nth-child(" + i + ")");
                    String Date = date.text().replaceAll("\\(.\\)", "").replace(".", "-");
                    String meals = meal.html().replace("<br>", "\n");
                    if (meals.isEmpty()) {
                        meals = no_meal;
                    } else {
                        meals = meals.replace("&amp;", "&").trim(); // Replace &amp; to &
                        meals = meals + "\n\n" + String.format(res_kcal, kcal.text());
                    }
                    System.out.println(Date);
                    SharedPreferences sp = context.getSharedPreferences(meal_type, MODE_PRIVATE);
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
}
