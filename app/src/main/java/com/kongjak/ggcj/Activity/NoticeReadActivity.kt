package com.kongjak.ggcj.Activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.FileAdapter
import com.kongjak.ggcj.Tools.Files
import kotlinx.android.synthetic.main.content_notice_read.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException
import java.util.*

class NoticeReadActivity : AppCompatActivity() {
    var titleTxt: String? = null
    var writertxt: String? = null
    var dateTxt: String? = null
    var contentsTxt: String? = null
    var parse_url: String? = null
    var table_count: String? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_read)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        parse_url = intent.getStringExtra("url")
        recycleView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        recycleView.setLayoutManager(mLayoutManager)
        val asyncTask = MainPageTask()
        asyncTask.execute()
        val fileTask = FileTask()
        fileTask.execute()
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

    private inner class MainPageTask : AsyncTask<String, String, Void?>() {
        var asyncDialog = ProgressDialog(
                this@NoticeReadActivity)
        private var count = 0
        private var contentsValue: String? = null
        override fun onPostExecute(result: Void?) {
            val title = findViewById<View>(R.id.item_title) as TextView
            val writer = findViewById<View>(R.id.item_writer) as TextView
            val date = findViewById<View>(R.id.item_date) as TextView
            val contents = findViewById<View>(R.id.item_contents) as TextView
            title.text = titleTxt
            writer.text = writertxt
            date.text = dateTxt
            if (TextUtils.isEmpty(contentsTxt)) contents.text = "내용이 없습니다." else contents.text = contentsTxt
            asyncDialog.dismiss()
            if (table_count != "0") {
                val builder = AlertDialog.Builder(this@NoticeReadActivity)
                builder.setTitle(getString(R.string.warning))
                builder.setMessage(getString(R.string.table_warning))
                builder.setPositiveButton(getString(R.string.ok), null)
                builder.show()
            }
            super.onPostExecute(result)
        }

        override fun onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            asyncDialog.setMessage("로딩중입니다..")

            // show dialog
            asyncDialog.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String): Void? {
            //백그라운드 작업이 진행되는 곳.
            try {
                val doc = Jsoup.connect(parse_url).get()
                val root = doc.select("#bbsWrap > div.bbsContent > table > tbody") // Get root view
                val title = doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(1) > td") // Get title
                val writer = doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(2) > td") // Get writer
                val date = doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(4) > td") // Get date
                val contentsroot = root.select("tr:nth-child(5) > td > p") // Get contents root
                val tables = root.select("tr:nth-child(5) > td > table")
                count = contentsroot.size // Count!
                Log.d("Parse", count.toString())
                for (i in 1..count) { // loop
                    val contents = root.select("tr:nth-child(5) > td > p:nth-child($i)") // Get contents
                    contentsValue = if (TextUtils.isEmpty(contentsValue)) contents.text() else "$contentsValue \n ${contents.text()}"
                    Log.d("GGCJ", contentsValue)
                }
                publishProgress(title.text(), writer.text(), date.text(), contentsValue, tables.size.toString()) // Send it!
                Log.d("Parse", title.text())
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
                Log.e("Parse", e.toString())
            }
            return null
        }

        override fun onProgressUpdate(vararg params: String) { // Receive from doInBackground
            titleTxt = params[0]
            writertxt = params[1]
            dateTxt = params[2]
            contentsTxt = params[3]
            table_count = params[4]
        }
    }

    private inner class FileTask : AsyncTask<String, String, Void?>() {
        var file_parsed = ArrayList<Files>()
        var asyncDialog = ProgressDialog(
                this@NoticeReadActivity)
        override fun onPostExecute(result: Void?) {
            val FileAdapter = ArrayList<Files>()
            val myAdapter = FileAdapter(FileAdapter)
            recycleView.adapter = myAdapter
            FileAdapter.addAll(file_parsed) // Add parsed's values to Real array list
            asyncDialog.dismiss()
            super.onPostExecute(result)
        }

        override fun onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            asyncDialog.setMessage("로딩중입니다..")

            // show dialog
            asyncDialog.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String): Void? {
            try {
                val doc = Jsoup.connect(parse_url).get()
                val root = doc.select("#bbsWrap > div.bbsContent > table > tbody") // Get root view
                val dl_root = doc.select("#bbsWrap > div.bbsContent > table > tbody > tr") // Get root view
                val count_dl = dl_root.size // Count!
                Log.d("Parse", "cnt_dl$count_dl")
                for (i in 6..count_dl) { // loop
                    val dl = root.select("tr:nth-child($i) > td > a") // Get dl url
                    val dl_href = dl.attr("abs:href") // Parse REAL url(href)
                    publishProgress(dl.text(), dl_href) // Send it!
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
                Log.e("Parse", e.toString())
            }
            return null
        }

        override fun onProgressUpdate(vararg params: String) { // Receive from doInBackground
            file_parsed.add(Files(params[0], params[1]))
            Log.d("Parse_dl", params[0])
        }
    }
}