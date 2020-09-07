package com.kongjak.ggcj.Tools

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

class ParseSchedule {
    class ScheduleTask(var context: Context) : AsyncTask<String?, String?, Void?>() {
        override fun onPostExecute(result: Void?) {
            val intent = Intent("ScheduleParseEnd")
            context.sendBroadcast(intent)
            super.onPostExecute(result)
        }

        override fun doInBackground(vararg params: String?): Void? {
            val year = params[0]
            val month = params[1]
            try {
                val date = year + month
                val builder = Uri.Builder()
                builder.scheme("https")
                        .authority("open.neis.go.kr")
                        .appendPath("hub")
                        .appendPath("SchoolSchedule")
                        .appendQueryParameter("KEY", context.getString(R.string.api_key))
                        .appendQueryParameter("ATPT_OFCDC_SC_CODE", context.getString(R.string.api_region_code))
                        .appendQueryParameter("SD_SCHUL_CODE", context.getString(R.string.api_school_code))
                        .appendQueryParameter("AA_YMD", date)
                val schedule_url = builder.build().toString()
                val doc = Jsoup.connect(schedule_url).get()
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
                    val schedule_sp = context.getSharedPreferences("schedule", Context.MODE_PRIVATE)
                    val schedule_editor = schedule_sp.edit()
                    schedule_editor.putString(date_pref, context.getString(R.string.no_schedule))
                    schedule_editor.apply()
                }
                if (isClassExits) {
                    val element = doc.select("SchoolSchedule")
                    for (e in element) {
                        val count = e.select("list_total_count")[0].text().toInt()
                        for (i in 0 until count) {
                            var schedule = e.select("EVENT_NM")[i].text()
                            val date_parsed = e.select("AA_YMD")[i].text()
                            val year_parsed = date_parsed.substring(0, 4)
                            val month_parsed = date_parsed.substring(4, 6)
                            val day_parsed = date_parsed.substring(6, 8)
                            val date_pref1 = "$year_parsed-$month_parsed-$day_parsed"
                            schedule = schedule.replace("<br/>", "\n")
                            val schedule_sp = context.getSharedPreferences("schedule", Context.MODE_PRIVATE)
                            val schedule_editor = schedule_sp.edit()
                            schedule_editor.putString(date_pref1, schedule)
                            schedule_editor.apply()
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