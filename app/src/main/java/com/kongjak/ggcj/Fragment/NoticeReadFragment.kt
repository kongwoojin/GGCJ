package com.kongjak.ggcj.Fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.FileAdapter
import com.kongjak.ggcj.Tools.Files
import kotlinx.android.synthetic.main.fragment_notice_read.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class NoticeReadFragment : Fragment() {
    private lateinit var fileArray: ArrayList<Files>
    private lateinit var myAdapter: FileAdapter
    var parse_url: String? = null

    private lateinit var recycleView: RecyclerView
    lateinit var loadingProgress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_notice_read, container, false)

        recycleView = rootView.findViewById(R.id.recycleView) as RecyclerView
        loadingProgress = rootView.findViewById(R.id.loadingProgress) as ProgressBar

        arguments?.let {
            parse_url = it.getString("url").toString()
        }

        recycleView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        recycleView.layoutManager = mLayoutManager
        fileArray = ArrayList<Files>()
        myAdapter = FileAdapter(fileArray)
        recycleView.adapter = myAdapter
        readContents()
        getFile()

        return rootView
    }

    private fun readContents() {
        var count: Int
        var contentsValue = ""

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
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
                for (i in 1..count) { // Get contents line by line
                    val contents =
                        root.select("tr:nth-child(5) > td > p:nth-child($i)") // Get contents
                    contentsValue =
                        if (TextUtils.isEmpty(contentsValue)) contents.text() else "$contentsValue \n ${contents.text()}"
                    Log.d("GGCJ", contentsValue)
                }
                withContext(Dispatchers.Main) {
                    item_title.text = title.text()
                    item_writer.text = writer.text()
                    item_date.text = date.text()
                    if (TextUtils.isEmpty(contentsValue)) item_contents.text =
                        "내용이 없습니다." else item_contents.text = contentsValue
                    loadingProgress.visibility = View.GONE
                    if (tables.size.toString() != "0") {
                        val builder = AlertDialog.Builder(context)
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
            withContext(Dispatchers.Main) {
                loadingProgress.visibility = View.GONE
            }
        }
    }

    private fun getFile() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
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
                    withContext(Dispatchers.Main) {
                        fileArray.add(Files(dl.text(), dl_href))
                        myAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
                Log.e("Parse", e.toString())
            }
            withContext(Dispatchers.Main) {
                loadingProgress.visibility = View.GONE
            }
        }
    }
}