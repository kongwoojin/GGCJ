package com.kongjak.ggcj.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.SaturdayDecorator
import com.kongjak.ggcj.Tools.SundayDecorator
import com.kongjak.ggcj.Tools.TodayDecorator
import com.kongjak.ggcj.Tools.WeekDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.android.synthetic.main.activity_date.*
import kotlinx.android.synthetic.main.app_bar_date.*
import kotlinx.android.synthetic.main.content_date.*
import org.threeten.bp.format.DateTimeFormatter

class DateActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.setCheckedItem(R.id.nav_date)
        calendarView.setOnDateChangedListener(this)
        calendarView.addDecorators(
                SundayDecorator(),
                SaturdayDecorator(),
                TodayDecorator()
        )

        calendarView.setLeftArrow(R.drawable.ic_date_left_black)
        calendarView.setRightArrow(R.drawable.ic_date_right_black)
    }

    override fun onDateSelected(
            widget: MaterialCalendarView,
            date: CalendarDay,
            selected: Boolean) {
        val intent = Intent(baseContext, DateReadActivity::class.java)
        intent.putExtra("dayOfMonth", if (selected) DAY_FORMATTER.format(date.date) else "0")
        intent.putExtra("month", if (selected) MONTH_FORMATTER.format(date.date) else "0")
        intent.putExtra("year", if (selected) YEAR_FORMATTER.format(date.date) else "0")
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.nav_home) {
            val intent = Intent(baseContext, MainActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.nav_notice) {
            val intent = Intent(baseContext, NoticeActivity::class.java)
            intent.putExtra("url", getString(R.string.notice_url))
            intent.putExtra("type", 0)
            startActivity(intent)
        } else if (id == R.id.nav_notice_parents) {
            val intent = Intent(baseContext, NoticeActivity::class.java)
            intent.putExtra("url", getString(R.string.notice_parents_url))
            intent.putExtra("type", 1)
            startActivity(intent)
        } else if (id == R.id.nav_gallery) {
            val intent = Intent(baseContext, GalleryActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.nav_timetable) {
            val url = "http://comci.kr/st"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, Uri.parse(url))
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

    public override fun onResume() {
        super.onResume()
        nav_view.setCheckedItem(R.id.nav_date)
    }

    companion object {
        private val DAY_FORMATTER = DateTimeFormatter.ofPattern("d")
        private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM")
        private val YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy")
    }
}