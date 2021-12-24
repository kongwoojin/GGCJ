package com.kongjak.ggcj.Activity

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.ImageFileAdapter
import com.kongjak.ggcj.Tools.ImageFiles
import kotlinx.android.synthetic.main.content_gallery_read.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class GalleryReadActivity : AppCompatActivity() {
    private lateinit var imageFileArray: ArrayList<ImageFiles>
    private lateinit var myAdapter: ImageFileAdapter
    var parse_url: String? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_read)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        parse_url = intent.getStringExtra("url")
        recycleView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = mLayoutManager
        imageFileArray = ArrayList<ImageFiles>()
        myAdapter = ImageFileAdapter(imageFileArray)
        recycleView.adapter = myAdapter
        getContents()
        getImageFile()
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
            customTabsIntent.launchUrl(this, Uri.parse(parse_url))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getContents() {
        var count: Int
        var contentsValue = ""

        CoroutineScope(IO).launch {
            withContext(Main) {
                loadingProgress.visibility = View.VISIBLE
            }
            try {
                val doc = Jsoup.connect(parse_url).get()
                val root = doc.select("#bbsWrap > div.bbsContent > table > tbody") // Get root view
                val title =
                    doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(1) > td") // Get title
                val writer =
                    doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(2) > td") // Get writer
                val date =
                    doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(4) > td") // Get date
                val contentsroot = root.select("tr:nth-child(5) > td > p") // Get contents root
                val tables = root.select("tr:nth-child(5) > td > table")
                count = contentsroot.size // Count!
                Log.d("Parse", count.toString())
                for (i in 1..count) { // loop
                    val contents =
                        root.select("tr:nth-child(5) > td > p:nth-child($i)") // Get contents
                    contentsValue =
                        if (TextUtils.isEmpty(contentsValue)) contents.text() else "$contentsValue \n ${contents.text()}"
                }
                withContext(Main) {
                    item_title.text = title.text()
                    item_writer.text = writer.text()
                    item_date.text = date.text()
                    if (TextUtils.isEmpty(contentsValue)) item_contents.text =
                        "내용이 없습니다." else item_contents.text = contentsValue
                    loadingProgress.visibility = View.GONE
                    if (tables.size.toString() != "0") {
                        val builder = AlertDialog.Builder(this@GalleryReadActivity)
                        builder.setTitle(getString(R.string.warning))
                        builder.setMessage(getString(R.string.table_warning))
                        builder.setPositiveButton(getString(R.string.ok), null)
                        builder.show()
                    }
                }
                Log.d("Parse", title.text())
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
                Log.e("Parse", e.toString())
            }
            withContext(Main) {
                loadingProgress.visibility = View.GONE
            }
        }
    }


    private fun getImageFile() {
        CoroutineScope(IO).launch {
            withContext(Main) {
                loadingProgress.visibility = View.VISIBLE
            }
            try {
                val doc = Jsoup.connect(parse_url).get()
                val root = doc.select("#bbsWrap > div.bbsContent > table > tbody") // Get root view
                val dl_root =
                    doc.select("#bbsWrap > div.bbsContent > table > tbody > tr") // Get root view
                val count_dl = dl_root.size // Count!
                Log.d("Parse", "cnt_dl$count_dl")
                for (i in 6..count_dl) { // loop
                    val dl = root.select("tr:nth-child($i) > td > a") // Get dl url
                    val dl_href = dl.attr("abs:href") // Parse REAL url(href)
                    val img = root.select("tr:nth-child($i) > td > img")
                    withContext(Main) {
                        val isImageAvailable: Boolean = img.size != 0
                        imageFileArray.add(ImageFiles(dl.text(), dl_href, isImageAvailable))
                        myAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
                Log.e("Parse", e.toString())
            }
            withContext(Main) {
                loadingProgress.visibility = View.GONE
            }
        }
    }
}