package com.kongjak.ggcj.Tools

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.CheckDigit.check
import org.jsoup.Jsoup
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ParseMeal {
    class WeekMealTask(var context: Context) : AsyncTask<String?, String?, Void?>() {
        var asyncDialog: ProgressDialog? = null
        override fun onPostExecute(result: Void?) {
            val intent = Intent("MealParseEnd")
            context.sendBroadcast(intent)
            asyncDialog!!.dismiss()
            super.onPostExecute(result)
        }

        override fun onPreExecute() {
            asyncDialog = ProgressDialog(context)
            val loading = context.getString(R.string.loading)
            asyncDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            asyncDialog!!.setMessage(loading)

            // show dialog
            asyncDialog!!.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): Void? {
            var meal_type: String? = null
            val year = params[1]
            val month = params[2]
            var no_meal: String? = null
            if (params[0] == "2") {
                meal_type = "lunch"
                no_meal = context.getString(R.string.no_lunch)
            } else if (params[0] == "3") {
                meal_type = "dinner"
                no_meal = context.getString(R.string.no_dinner)
            }
            try {
                val date = year + month
                val builder = Uri.Builder()
                builder.scheme("https")
                        .authority("open.neis.go.kr")
                        .appendPath("hub")
                        .appendPath("mealServiceDietInfo")
                        .appendQueryParameter("KEY", context.getString(R.string.api_key))
                        .appendQueryParameter("ATPT_OFCDC_SC_CODE", context.getString(R.string.api_region_code))
                        .appendQueryParameter("SD_SCHUL_CODE", context.getString(R.string.api_school_code))
                        .appendQueryParameter("MMEAL_SC_CODE", params[0])
                        .appendQueryParameter("MLSV_YMD", date)
                val meal_url = builder.build().toString()
                val doc = Jsoup.connect(meal_url).get()
                var isClassExits = false
                val code = doc.select("* > RESULT >CODE")
                if (code.text() == "INFO-000") {
                    isClassExits = true
                }
                val dateFormat = SimpleDateFormat("yyyyMM")
                val convertedDate = dateFormat.parse(date)
                val c = Calendar.getInstance()
                c.time = convertedDate
                val last_day = c.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (i in 1..last_day) {
                    val date_pref = year + "-" + month + "-" + check(i.toString())
                    val sp = context.getSharedPreferences(meal_type, Context.MODE_PRIVATE)
                    val editor = sp.edit()
                    editor.putString(date_pref, no_meal)
                    editor.apply()
                }
                if (isClassExits) {
                    val element = doc.select("mealServiceDietInfo")
                    for (e in element) {
                        val count = e.select("list_total_count")[0].text().toInt()
                        for (i in 0 until count) {
                            var meals = e.select("DDISH_NM")[i].text()
                            val date_parsed = e.select("MLSV_YMD")[i].text()
                            val year_parsed = date_parsed.substring(0, 4)
                            val month_parsed = date_parsed.substring(4, 6)
                            val day_parsed = date_parsed.substring(6, 8)
                            val date_pref1 = "$year_parsed-$month_parsed-$day_parsed"
                            meals = meals.replace("<br/>", "\n")
                            val sp = context.getSharedPreferences(meal_type, Context.MODE_PRIVATE)
                            val editor = sp.edit()
                            editor.putString(date_pref1, meals)
                            editor.apply()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return null
        }

    }
}