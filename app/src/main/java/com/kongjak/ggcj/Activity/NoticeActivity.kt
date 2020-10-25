package com.kongjak.ggcj.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.NoticeAdapter
import com.kongjak.ggcj.Tools.Notices
import kotlinx.android.synthetic.main.content_notice.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class NoticeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnRefreshListener {
    private lateinit var myAdapter: NoticeAdapter
    private lateinit var NoticeArrayList: ArrayList<Notices>
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    var page = 1
    var last_page = 0
    var prev: FloatingActionButton? = null
    var next: FloatingActionButton? = null
    lateinit var parse_url: String

    /**
     * notice_type
     * 0 is Notice, 1 is Notice for Parents
     */
    var notice_type = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (intent.hasExtra("url")) parse_url = intent.getStringExtra("url").toString()
        notice_type = intent.getIntExtra("type", 0)
        Log.d("URL", String.format(parse_url, page))
        prev = findViewById<View>(R.id.prev) as FloatingActionButton
        prev!!.setOnClickListener { view ->
            if (page > 1) {
                page -= 1
                getList()
            } else {
                Snackbar.make(view, getString(R.string.first_page), Snackbar.LENGTH_SHORT).show()
            }
        }
        prev!!.setOnLongClickListener { view ->
            if (page != 1) {
                page = 1
                getList()
            } else {
                Snackbar.make(view, getString(R.string.first_page), Snackbar.LENGTH_SHORT).show()
            }
            true
        }
        next = findViewById<View>(R.id.next) as FloatingActionButton
        next!!.setOnClickListener { view ->
            if (page != last_page) {
                page += 1
                getList()
            } else {
                Snackbar.make(view, getString(R.string.last_page), Snackbar.LENGTH_SHORT).show()
            }
        }
        next!!.setOnLongClickListener { view ->
            if (page != last_page) {
                page = last_page
                getList()
            } else {
                Snackbar.make(view, getString(R.string.last_page), Snackbar.LENGTH_SHORT).show()
            }
            true
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        setNav()
        recycleView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = mLayoutManager
        recycleView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        getList()
        recycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && prev!!.visibility == View.VISIBLE) {
                    prev!!.hide()
                } else if (dy < 0 && prev!!.visibility != View.VISIBLE) {
                    if (page != 1) {
                        prev!!.show()
                    }
                }
                if (dy > 0 && next!!.visibility == View.VISIBLE) {
                    next!!.hide()
                } else if (dy < 0 && next!!.visibility != View.VISIBLE) {
                    if (page != last_page) {
                        next!!.show()
                    }
                }
            }
        })
        NoticeArrayList = ArrayList<Notices>()
        myAdapter = NoticeAdapter(NoticeArrayList)
        recycleView.adapter = myAdapter
        mSwipeRefreshLayout = findViewById<View>(R.id.swipe_layout) as SwipeRefreshLayout
        mSwipeRefreshLayout!!.setOnRefreshListener(this)
        prev!!.hide()
        next!!.hide()
    }

    fun setNav() {
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        if (notice_type == 0) {
            navigationView.setCheckedItem(R.id.nav_notice)
        } else if (notice_type == 1) {
            navigationView.setCheckedItem(R.id.nav_notice_parents)
        }
    }

    fun reload() {
        getList()
        mSwipeRefreshLayout!!.isRefreshing = false
    }

    override fun onRefresh() {
        reload()
    }

    fun checkFabHide() {
        if (page == 1) {
            prev!!.hide()
        } else {
            prev!!.show()
        }
        if (page == last_page) {
            next!!.hide()
        } else {
            next!!.show()
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
            customTabsIntent.launchUrl(this, Uri.parse(String.format(parse_url, page)))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setNav()
    }

    private fun getList() {
        var count: Int
        val notice_url = String.format(parse_url, page)
        CoroutineScope(IO).launch {
            withContext(Main) {
                NoticeArrayList.clear()
                loadingProgress.visibility = View.VISIBLE
            }
            try {
                Log.d("Parse", notice_url)
                val doc = Jsoup.connect(notice_url).get()
                val root = doc.select("#bbsWrap > form > div.bbsContent > table > tbody") // Get root view
                val list = doc.select("#bbsWrap > form > div.bbsContent > table > tbody > tr") // Get notice list
                val last_page_url = doc.select("#bbsWrap > form > div.bbsPage > li:nth-child(14) > a")
                if (page == 1) last_page = last_page_url.attr("abs:href").replace("(.*)Page=".toRegex(), "").toInt()
                count = list.size // Count notice!
                Log.d("Parse", "GGCJ$last_page")
                Log.d("Parse", "Count$count")
                for (i in 1..count) { // loop
                    val title = root.select("tr:nth-child($i) > td.tit") // Get title
                    val numoflist = root.select("tr:nth-child($i) > td:nth-child(1)") // Get number of notice
                    val writer = root.select("tr:nth-child($i) > td:nth-child(4)") // Get writer
                    val date = root.select("tr:nth-child($i) > td:nth-child(5)") // Get date
                    val url = root.select("tr:nth-child($i) > td.tit > a") // Get url (Elements)
                    val notice_href = url.attr("abs:href") // Parse REAL url(href)
                    withContext(Main) {
                        var isImportant = false
                        if (numoflist.text() == "공지") {
                            isImportant = true
                        }
                        NoticeArrayList.add(Notices(title.text(), writer.text(), date.text(), notice_href, isImportant))
                        myAdapter.notifyDataSetChanged()
                    }
                    Log.d("Parse", title.text())
                    Log.d("Parse", "Count: $i")
                    Log.d("Parse", url.toString())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
            withContext(Main) {
                loadingProgress.visibility = View.GONE
                checkFabHide()
            }
        }
    }
}