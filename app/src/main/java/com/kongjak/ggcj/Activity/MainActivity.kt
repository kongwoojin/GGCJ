package com.kongjak.ggcj.Activity

import android.app.ProgressDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.CheckDigit.check
import com.kongjak.ggcj.Tools.ParseMeal.WeekMealTask
import com.kongjak.ggcj.Tools.ParseSchedule.ScheduleTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_date_read.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.item_dinner
import kotlinx.android.synthetic.main.content_main.item_lunch
import kotlinx.android.synthetic.main.content_main.item_schedule
import kotlinx.android.synthetic.main.content_main.loadingProgress
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var getYear: String? = null
    private var getMonth: String? = null
    private var getDay: String? = null
    private var getDate: String? = null
    private var alertDialog: AlertDialog? = null
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            loadingProgress.visibility = View.GONE
            if (intent.action == "MealParseEnd") {
                setMealView()
            } else if (intent.action == "ScheduleParseEnd") {
                setScheduleView()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.setCheckedItem(R.id.nav_home)
        val filter = IntentFilter()
        filter.addAction("MealParseEnd")
        filter.addAction("ScheduleParseEnd")
        registerReceiver(mBroadcastReceiver, filter)
        val now = System.currentTimeMillis()
        val date = Date(now)
        val year = SimpleDateFormat("yyyy", Locale.KOREA)
        getYear = year.format(date)
        val month = SimpleDateFormat("MM", Locale.KOREA)
        getMonth = check(month.format(date))
        val day = SimpleDateFormat("dd", Locale.KOREA)
        getDay = check(day.format(date))
        setView()
    }

    private fun setView() {
        getDate = "$getYear-$getMonth-$getDay"
        checkMeal()
        checkSchedule()
    }

    private fun checkMeal() {
        val lunch_sp = getSharedPreferences("lunch", Context.MODE_PRIVATE)
        val lunch_str = lunch_sp.getString(getDate, "")
        if (!lunch_str!!.isEmpty()) {
            setMealView()
        } else {
            EmptyDialog(getString(R.string.meal))
        }
    }

    private fun setMealView() {
        val lunch_sp = getSharedPreferences("lunch", Context.MODE_PRIVATE)
        val lunch_str = lunch_sp.getString(getDate, "")
        val dinner_sp = getSharedPreferences("dinner", Context.MODE_PRIVATE)
        val dinner_str = dinner_sp.getString(getDate, "")
        item_lunch.visibility = View.VISIBLE
        item_dinner.visibility = View.VISIBLE
        item_lunch.text = lunch_str
        item_dinner.text = dinner_str
    }

    private fun checkSchedule() {
        val schedule_sp = getSharedPreferences("schedule", Context.MODE_PRIVATE)
        val schedule_str = schedule_sp.getString(getDate, "")
        item_schedule.visibility = View.VISIBLE
        if (!schedule_str!!.isEmpty()) {
            setScheduleView()
        } else {
            EmptyDialog(getString(R.string.schedule))
        }
    }

    private fun setScheduleView() {
        val schedule_sp = getSharedPreferences("schedule", Context.MODE_PRIVATE)
        val schedule_str = schedule_sp.getString(getDate, "")
        item_schedule.visibility = View.VISIBLE
        if (schedule_str == getString(R.string.no_schedule)) {
            nextSchedule
        } else {
            item_schedule.text = schedule_str
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            ActivityCompat.finishAffinity(this)
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.nav_notice) {
            val intent = Intent(baseContext, NoticeActivity::class.java)
            intent.putExtra("url", getString(R.string.notice_url))
            intent.putExtra("type", 0)
            startActivity(intent)
        } else if (id == R.id.nav_notice_parents) {
            val intent = Intent(baseContext, NoticeActivity::class.java)
            intent.putExtra("url", getString(R.string.notice_parents_url))
            intent.putExtra("type", 1)
            startActivity(intent)
        } else if (id == R.id.nav_date) {
            val intent = Intent(baseContext, DateActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.nav_gallery) {
            val intent = Intent(baseContext, GalleryActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.nav_timetable) {
            val url = "http://comci.kr/st"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this@MainActivity, Uri.parse(url))
        } else if (id == R.id.nav_send) {
            val email = Intent(Intent.ACTION_SENDTO)
            email.data = Uri.parse("mailto:")
            val address = arrayOf("ggcj@kongjak.com")
            email.putExtra(Intent.EXTRA_EMAIL, address)
            val title = resources.getString(R.string.nav_send_intent)
            val chooser = Intent.createChooser(email, title)
            if (email.resolveActivity(packageManager) != null) {
                startActivity(chooser)
            }
        } else if (id == R.id.nav_info) {
            val intent = Intent(baseContext, InfoActivity::class.java)
            startActivity(intent)
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_web) {
            val url = "http://ggcj.hs.kr"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this@MainActivity, Uri.parse(url))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun EmptyDialog(dataType: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(String.format(getString(R.string.data_empty_dialog), dataType))
        builder.setPositiveButton(getString(R.string.ok)) { dialog: DialogInterface?, id: Int ->
            if (dataType == getString(R.string.meal)) {
                getDatas(0)
            } else if (dataType == getString(R.string.schedule)) {
                getDatas(1)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, id: Int -> dialog.dismiss() }
        alertDialog = builder.create()
        alertDialog!!.show()
    }

    private fun getDatas(type: Int) {
        loadingProgress.visibility = View.VISIBLE
        if (type == 0) {
            val lunchTask = WeekMealTask(this@MainActivity)
            lunchTask.execute("2", getYear, getMonth)
            val dinnerTask = WeekMealTask(this@MainActivity)
            dinnerTask.execute("3", getYear, getMonth)
        } else if (type == 1) {
            val asyncTask = ScheduleTask(this@MainActivity)
            asyncTask.execute(getYear, getMonth)
        }
    }

    // show dialog
    private val nextSchedule: Unit
        private get() {
            val today = Calendar.getInstance(TimeZone.getDefault())[Calendar.DAY_OF_MONTH]
            val last_day = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val asyncDialog = ProgressDialog(
                    this@MainActivity)
            val loading = getString(R.string.loading)
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            asyncDialog.setMessage(loading)

            // show dialog
            asyncDialog.show()
            Thread(Runnable {
                var nextSchedule: String? = null
                var nextScheduleDay: String? = null
                for (i in today + 1..last_day) {
                    val getFullDate = getYear + "-" + getMonth + "-" + check(i.toString())
                    val getDate = getMonth + "/" + check(i.toString())
                    val schedule_sp = getSharedPreferences("schedule", Context.MODE_PRIVATE)
                    val schedule_str = schedule_sp.getString(getFullDate, "")
                    if (schedule_str != getString(R.string.no_schedule)) {
                        nextSchedule = schedule_str
                        nextScheduleDay = getDate
                        break
                    }
                }
                val finalNextSchedule = nextSchedule
                val finalNextScheduleDay = nextScheduleDay
                runOnUiThread {
                    if (finalNextSchedule == null || finalNextSchedule.isEmpty()) {
                        item_schedule.text = getString(R.string.no_next_schedule)
                    } else {
                        item_schedule.text = String.format(getString(R.string.next_schedule), finalNextSchedule, finalNextScheduleDay)
                    }
                }
            }).start()
            asyncDialog.dismiss()
        }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
        nav_view.setCheckedItem(R.id.nav_home)
        Log.d("GGCJ", "Restart")
        checkMeal()
        checkSchedule()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver)
    }
}