package com.kongjak.ggcj.Fragment

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.kongjak.ggcj.Activity.NoticeReadActivity
import com.kongjak.ggcj.R
import com.kongjak.ggcj.Tools.NoticeAdapter
import com.kongjak.ggcj.Tools.Notices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import kotlin.math.ceil

class NoticeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var myAdapter: NoticeAdapter
    private lateinit var noticeArrayList: ArrayList<Notices>
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    var page = 1
    var last_page = 0
    var prev: FloatingActionButton? = null
    var next: FloatingActionButton? = null
    var parse_url: String = ""
    var searchValue: String = ""
    var notice_type = 0

    private var listener: OnNoticePageChangeListener? = null

    private lateinit var recycleView: RecyclerView
    lateinit var loadingProgress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_notice, container, false)

        recycleView = rootView.findViewById(R.id.recycleView) as RecyclerView
        loadingProgress = rootView.findViewById(R.id.loadingProgress) as ProgressBar

        arguments?.let {
            Log.d("GGCJ", "TST")
            parse_url = it.getString("url").toString()
            notice_type = it.getInt("type")
            searchValue = it.getString("searchValue").toString()
        }

        if (savedInstanceState != null) {
            page = savedInstanceState.getInt("nowPage")
        }

        prev = rootView.findViewById<View>(R.id.prev) as FloatingActionButton
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
        next = rootView.findViewById<View>(R.id.next) as FloatingActionButton
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

        recycleView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(context)
        recycleView.layoutManager = mLayoutManager
        recycleView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        getList()
        recycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && prev!!.visibility == View.VISIBLE) {
                    prev!!.hide()
                    Log.d("GGCJ", "HIDE")
                } else if (dy < 0 && prev!!.visibility != View.VISIBLE) {
                    if (page != 1) {
                        prev!!.show()
                        Log.d("GGCJ", "SHOW")
                    }
                }
                if (dy > 0 && next!!.visibility == View.VISIBLE) {
                    next!!.hide()
                    Log.d("GGCJ", "HIDE")
                } else if (dy < 0 && next!!.visibility != View.VISIBLE) {
                    if (page != last_page) {
                        next!!.show()
                        Log.d("GGCJ", "SHOW")
                    }
                }
            }
        })

        noticeArrayList = ArrayList<Notices>()

        myAdapter = NoticeAdapter(noticeArrayList) { Notices ->
            if (resources.getBoolean(R.bool.isTablet) && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {

                val fragment = NoticeReadFragment()

                val fragmentManager = activity?.supportFragmentManager

                val bundle = Bundle().apply {
                    putString("url", Notices.url)
                }

                fragment.arguments = bundle

                fragmentManager!!.beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .commit()

                listener?.onNoticePageChange(Notices.url)

            } else {
                Log.d("Fragment", "isPortrait")
                val intent = Intent(context, NoticeReadActivity::class.java)
                intent.putExtra("url", Notices.url)
                context?.startActivity(intent)
            }
        }

        recycleView.adapter = myAdapter
        mSwipeRefreshLayout = rootView.findViewById<View>(R.id.swipe_layout) as SwipeRefreshLayout
        mSwipeRefreshLayout!!.setOnRefreshListener(this)
        prev!!.hide()
        next!!.hide()

        return rootView
    }

    private fun checkFabHide() {
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

    private fun getList() {
        // First, Send Now page to activity
        listener?.onNoticePageListChange(page)
        var count: Int
        Log.d("Page", page.toString())
        val notice_url = String.format(parse_url, page, searchValue)
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                noticeArrayList.clear()
                loadingProgress.visibility = View.VISIBLE
            }
            try {
                Log.d("Parse", notice_url)
                val doc = Jsoup.connect(notice_url).get()
                val root =
                    doc.select("#bbsWrap > form > div.bbsContent > table > tbody") // Get root view
                val list =
                    doc.select("#bbsWrap > form > div.bbsContent > table > tbody > tr") // Get notice list
                val last_page_url =
                    doc.select("#bbsWrap > form > div.bbsPage > li:nth-child(14) > a")
                Log.d("TST", page.toString())
                //if (page == 1) last_page = last_page_url.attr("abs:href").replace("(.*)Page=".toRegex(), "").toInt()
                count = list.size // Count notice!
                Log.d("Parse", "GGCJ$last_page")
                Log.d("Parse", "Count$count")
                for (i in 1..count) { // loop
                    val title = root.select("tr:nth-child($i) > td.tit") // Get title
                    val numoflist =
                        root.select("tr:nth-child($i) > td:nth-child(1)") // Get number of notice
                    val writer = root.select("tr:nth-child($i) > td:nth-child(4)") // Get writer
                    val date = root.select("tr:nth-child($i) > td:nth-child(5)") // Get date
                    val url = root.select("tr:nth-child($i) > td.tit > a") // Get url (Elements)
                    val notice_href = url.attr("abs:href") // Parse REAL url(href)
                    withContext(Dispatchers.Main) {
                        var isImportant = false
                        if (numoflist.text() == "공지") {
                            isImportant = true
                        }
                        noticeArrayList.add(
                            Notices(
                                title.text(),
                                writer.text(),
                                date.text(),
                                notice_href,
                                isImportant
                            )
                        )
                        myAdapter.notifyDataSetChanged()
                    }
                    if (page == 1 && !numoflist.text().equals("공지")) {
                        var lastNoticeNum = numoflist.text().toDouble()
                        last_page =
                            ceil((lastNoticeNum + 14) / 15).toInt() // Get last page number
                        Log.d("GGCJ", last_page.toString())
                    }
                    Log.d("Parse", title.text())
                    Log.d("Parse", "Count: $i")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                loadingProgress.visibility = View.GONE
                checkFabHide()
            }
        }
    }


    fun reload() {
        getList()
        mSwipeRefreshLayout!!.isRefreshing = false
    }

    override fun onRefresh() {
        reload()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("nowPage", page)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNoticePageChangeListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    interface OnNoticePageChangeListener {
        fun onNoticePageListChange(nowPage: Int)
        fun onNoticePageChange(nowUrl: String)
    }
}