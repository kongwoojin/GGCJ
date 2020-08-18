package com.kongjak.ggcj.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.Gallery
import com.kongjak.ggcj.Tools.GalleryAdapter
import kotlinx.android.synthetic.main.content_date_read.*
import kotlinx.android.synthetic.main.content_gallery.*
import kotlinx.android.synthetic.main.content_gallery.loadingProgress
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class GalleryActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnRefreshListener {
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    var page = 1
    var last_page = 0
    var prev: FloatingActionButton? = null
    var next: FloatingActionButton? = null
    var parse_url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        parse_url = resources.getString(R.string.gallery_url)
        prev = findViewById<View>(R.id.prev) as FloatingActionButton
        prev!!.setOnClickListener { view ->
            if (page > 1) {
                page = page - 1
                val asyncTask = MainPageTask()
                asyncTask.execute()
            } else {
                Snackbar.make(view, getString(R.string.first_page), Snackbar.LENGTH_SHORT).show()
            }
        }
        prev!!.setOnLongClickListener { view ->
            if (page != 1) {
                page = 1
                val asyncTask = MainPageTask()
                asyncTask.execute()
            } else {
                Snackbar.make(view, getString(R.string.first_page), Snackbar.LENGTH_SHORT).show()
            }
            true
        }
        next = findViewById<View>(R.id.next) as FloatingActionButton
        next!!.setOnClickListener { view ->
            if (page != last_page) {
                page = page + 1
                val asyncTask = MainPageTask()
                asyncTask.execute()
            } else {
                Snackbar.make(view, getString(R.string.last_page), Snackbar.LENGTH_SHORT).show()
            }
        }
        next!!.setOnLongClickListener { view ->
            if (page != last_page) {
                page = last_page
                val asyncTask = MainPageTask()
                asyncTask.execute()
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
        navigationView.setCheckedItem(R.id.nav_gallery)
        recycleView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = mLayoutManager
        val asyncTask = MainPageTask()
        asyncTask.execute()
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
        mSwipeRefreshLayout = findViewById<View>(R.id.swipe_layout) as SwipeRefreshLayout
        mSwipeRefreshLayout!!.setOnRefreshListener(this)
        prev!!.hide()
        next!!.hide()
    }

    fun reload() {
        val asyncTask = MainPageTask()
        asyncTask.execute()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.gallery, menu)
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
            customTabsIntent.launchUrl(this, Uri.parse(String.format(parse_url!!, page)))
        }
        return super.onOptionsItemSelected(item)
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
        } else if (id == R.id.nav_date) {
            val intent = Intent(baseContext, DateActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.nav_timetable) {
            val url = "http://comci.kr/st"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this@GalleryActivity, Uri.parse(url))
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
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private inner class MainPageTask : AsyncTask<String?, Any?, Void?>() {
        var parsed = ArrayList<Gallery>()
        override fun onPostExecute(result: Void?) {
            Log.d("Parse", "End")
            val GalleryArrayList = ArrayList<Gallery>()
            val myAdapter = GalleryAdapter(GalleryArrayList)
            recycleView.adapter = myAdapter
            GalleryArrayList.addAll(parsed) // Add parsed's values to Real array list
            loadingProgress.visibility = View.GONE
            checkFabHide()
            super.onPostExecute(result)
        }

        override fun onPreExecute() {
            loadingProgress.visibility = View.VISIBLE
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): Void? {
            //백그라운드 작업이 진행되는 곳.
            val notice_url = String.format(parse_url!!, page)
            try {
                Log.d("Parse", notice_url)
                val doc = Jsoup.connect(notice_url).get()
                val root = doc.select("#container > div > div.content.col-md-9 > div.contentBody > div.bbsWrap > form > div.bbsContent.mt10.clearfix > ul") // Get root view
                val list = doc.select("#container > div > div.content.col-md-9 > div.contentBody > div.bbsWrap > form > div.bbsContent.mt10.clearfix > ul > li") // Get notice list
                val last_page_url = doc.select("#container > div > div.content.col-md-9 > div.contentBody > div.bbsWrap > form > div.bbsPage > a:nth-child(14)")
                if (page == 1) last_page = last_page_url.attr("abs:href").replace("(.*)Page=".toRegex(), "").toInt()
                val count = list.size // Count notice!
                Log.d("Parse", "GGCJ$last_page")
                Log.d("Parse", "Count$count")
                for (i in 1..count) { // loop
                    val title = root.select("li:nth-child($i) > a > span")
                    val imageUrl = root.select("li:nth-child($i) > a > img")
                    val url = root.select("li:nth-child($i) > a")
                    val full_image_url = "http://ggcj.hs.kr/main.php" + imageUrl.attr("src")
                    Log.d("Parse", title.text())
                    Log.d("Parse", "http://ggcj.hs.kr/main.php" + imageUrl.attr("src"))
                    Log.d("Parse", url.attr("abs:href"))
                    publishProgress(title.text(), url.attr("abs:href"), full_image_url)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onProgressUpdate(vararg params: Any?) { // Receive from doInBackground
            val title = params[0] as String
            val url = params[1] as String
            val image_url = params[2] as String
            parsed.add(Gallery(title, url, image_url))
        }
    }
}