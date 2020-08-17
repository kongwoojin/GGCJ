package com.kongjak.ggcj.Activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.CheckDigit.check
import com.kongjak.ggcj.Tools.ParseMeal.WeekMealTask
import com.kongjak.ggcj.Tools.ParseSchedule.ScheduleTask
import kotlinx.android.synthetic.main.activity_date_read.*
import kotlinx.android.synthetic.main.content_date_read.*

class DateReadActivity : AppCompatActivity(), OnRefreshListener {
    private var curDate: String? = null
    private var dayOfMonth: String = ""
    private var month: String = ""
    private var year: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_read)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val filter = IntentFilter()
        filter.addAction("MealParseEnd")
        filter.addAction("ScheduleParseEnd")
        registerReceiver(mBroadcastReceiver, filter)
        dayOfMonth = check(intent.getStringExtra("dayOfMonth"))
        month = check(intent.getStringExtra("month"))
        year = intent.getStringExtra("year")
        curDate = "$year-$month-$dayOfMonth"
        println(curDate)
        setView()
        swipe_layout!!.setOnRefreshListener(this)
    }

    fun setView() {
        val lunch_sp = getSharedPreferences("lunch", Context.MODE_PRIVATE)
        val lunch_str = lunch_sp.getString(curDate, "")
        val dinner_sp = getSharedPreferences("dinner", Context.MODE_PRIVATE)
        val dinner_str = dinner_sp.getString(curDate, "")
        val schedule_sp = getSharedPreferences("schedule", Context.MODE_PRIVATE)
        val schedule_str = schedule_sp.getString(curDate, "")
        val today = String.format(resources.getString(R.string.date), year, month, dayOfMonth)
        item_date.text = today
        item_lunch.visibility = View.VISIBLE
        item_dinner.visibility = View.VISIBLE
        item_schedule.visibility = View.VISIBLE
        if (!lunch_str!!.isEmpty()) {
            item_lunch.text = lunch_str
            item_dinner.text = dinner_str
        } else {
            meal
        }
        if (!schedule_str!!.isEmpty()) {
            item_schedule.text = schedule_str
        } else {
            schedule
        }
    }

    private val meal: Unit
        get() {
            val lunchTask = WeekMealTask(this)
            lunchTask.execute("2", year, month)
            val dinnerTask = WeekMealTask(this)
            dinnerTask.execute("3", year, month)
        }

    private val schedule: Unit
        get() {
            val asyncTask = ScheduleTask(this)
            asyncTask.execute(year, month)
        }

    override fun onRefresh() {
        meal
        schedule
        swipe_layout!!.isRefreshing = false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("GGCJ", "Received ParseEnd")
            setView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver)
    }
}