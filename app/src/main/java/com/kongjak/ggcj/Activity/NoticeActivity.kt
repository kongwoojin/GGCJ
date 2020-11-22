package com.kongjak.ggcj.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import com.kongjak.ggcj.Fragment.NoticeFragment
import com.kongjak.ggcj.R


class NoticeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, NoticeFragment.OnNoticePageChangeListener {

    lateinit var parseUrl: String // Notice List page url, Pass to Fragment
    private var fragmentManager: FragmentManager? = null
    lateinit var nowParseUrl: String // Open this link when user click action_web menu


    override fun onNoticePageListChange(nowPage: Int) {
        nowParseUrl = String.format(parseUrl, nowPage)
    }

    override fun onNoticePageChange(nowUrl: String) {
        nowParseUrl = nowUrl
    }

    /**
     * notice_type
     * 0 is Notice, 1 is Notice for Parents
     */
    var notice_type = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        if (intent.hasExtra("url")) parseUrl = intent.getStringExtra("url").toString()
        notice_type = intent.getIntExtra("type", 0)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        setNav()


        fragmentManager = supportFragmentManager;

        val fragment: Fragment = NoticeFragment() // Fragment 생성

        val bundle = Bundle().apply {
            putString("url", parseUrl)
            putInt("type", notice_type)
        }

        fragment.arguments = bundle

        if (savedInstanceState == null) {
            fragmentManager!!.beginTransaction()
                    .add(R.id.container, fragment)
                    .commit()
        }
    }

    fun setNav() {
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        if (notice_type == 0) {
            navigationView.setCheckedItem(R.id.nav_notice)
        } else if (notice_type == 1) {
            navigationView.setCheckedItem(R.id.nav_notice_parents)
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
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
        } else if (id == R.id.nav_notice && notice_type != 0) {
            val intent = Intent(baseContext, NoticeActivity::class.java)
            intent.putExtra("url", getString(R.string.notice_url))
            intent.putExtra("type", 0)
            startActivity(intent)
        } else if (id == R.id.nav_notice_parents && notice_type != 1) {
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
            customTabsIntent.launchUrl(this@NoticeActivity, Uri.parse(url))
        } else if (id == R.id.nav_send) {
            val email = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("ggcj@kongjak.com"))
            }
            val chooser = Intent.createChooser(email, resources.getString(R.string.nav_send_intent))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(chooser)
            }
        } else if (id == R.id.nav_info) {
            val intent = Intent(baseContext, InfoActivity::class.java)
            startActivity(intent)
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.notice, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_web) {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, Uri.parse(nowParseUrl))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setNav()
    }
}