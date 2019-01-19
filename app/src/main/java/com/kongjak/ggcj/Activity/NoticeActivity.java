package com.kongjak.ggcj.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.kongjak.ggcj.R;
import com.kongjak.ggcj.Tools.NoticeAdapter;
import com.kongjak.ggcj.Tools.Notices;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class NoticeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    int page = 1;
    int last_page;
    FloatingActionButton prev, next;
    String parse_url;

    /**
     * notice_type
     * 0 is Notice, 1 is Notice for Parents
     **/
    int notice_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parse_url = getIntent().getStringExtra("url");
        notice_type = getIntent().getIntExtra("type",0);
        Log.d("URL",String.format(parse_url, page));

        prev = (FloatingActionButton) findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (page > 1) {
                    page = page - 1;
                    MainPageTask asyncTask = new MainPageTask();
                    asyncTask.execute();
                } else {
                    Snackbar.make(view, getString(R.string.first_page), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        next = (FloatingActionButton) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (page != last_page) {
                    page = page + 1;
                    MainPageTask asyncTask = new MainPageTask();
                    asyncTask.execute();
                } else {
                    Snackbar.make(view, getString(R.string.last_page), Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setNav();

        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        MainPageTask asyncTask = new MainPageTask();
        asyncTask.execute();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && prev.getVisibility() == View.VISIBLE) {
                    prev.hide();
                } else if (dy < 0 && prev.getVisibility() != View.VISIBLE) {
                    if (!(page == 1)) {
                        prev.show();
                    }
                }
                if (dy > 0 && next.getVisibility() == View.VISIBLE) {
                    next.hide();
                } else if (dy < 0 && next.getVisibility() != View.VISIBLE) {
                    if (!(page == last_page)) {
                        next.show();
                    }
                }
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        prev.hide();
        next.hide();
    }

    public void setNav() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (notice_type == 0) {
            navigationView.setCheckedItem(R.id.nav_notice);
        } else if (notice_type == 1) {
            navigationView.setCheckedItem(R.id.nav_notice_parents);
        }
    }

    public void reload() {
        MainPageTask asyncTask = new MainPageTask();
        asyncTask.execute();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        reload();
    }

    private class MainPageTask extends AsyncTask<String, String, Void> {
        private Elements root;
        private Elements title;
        private Elements list;
        private Elements writer;
        private Elements date;
        private Elements url;
        private Elements numoflist;
        private int count;

        ArrayList<Notices> parsed = new ArrayList<>();

        @Override
        protected void onPostExecute(Void result) {
            //doInBackground 작업이 끝나고 난뒤의 작업
            Log.d("Parse", "End");
            ArrayList<Notices> NoticeArrayList = new ArrayList<>();
            NoticeAdapter myAdapter = new NoticeAdapter(NoticeArrayList);
            mRecyclerView.setAdapter(myAdapter);
            NoticeArrayList.addAll(parsed); // Add parsed's values to Real array list
            asyncDialog.dismiss();
            checkFabHide();
            super.onPostExecute(result);
        }

        ProgressDialog asyncDialog = new ProgressDialog(
                NoticeActivity.this);

        @Override
        protected void onPreExecute() {
            String loading = getString(R.string.loading);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage(loading);

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            //백그라운드 작업이 진행되는 곳.
            Resources res = getResources();
            String notice_url = String.format(parse_url, page);
            try {
                Log.d("Parse", notice_url);
                Document doc = Jsoup.connect(notice_url).get();
                root = doc.select("#bbsWrap > form > div.bbsContent > table > tbody"); // Get root view
                list = doc.select("#bbsWrap > form > div.bbsContent > table > tbody > tr"); // Get notice list
                count = list.size(); // Count notice!

                Log.d("Parse", "Count" + String.valueOf(count));

                for (int i = 1; i <= count; i++) { // loop
                    title = root.select("tr:nth-child(" + i + ") > td.tit"); // Get title
                    numoflist = root.select("tr:nth-child(" + i + ") > td:nth-child(1)"); // Get writer
                    writer = root.select("tr:nth-child(" + i + ") > td:nth-child(4)"); // Get writer
                    date = root.select("tr:nth-child(" + i + ") > td:nth-child(5)"); // Get date
                    url = root.select("tr:nth-child(" + i + ") > td.tit > a"); // Get url (Elements)
                    String notice_href = url.attr("abs:href"); // Parse REAL url(href)

                    publishProgress(title.text(), writer.text(), date.text(), notice_href, numoflist.text()); // Send it!
                    Log.d("Parse", title.text());
                    Log.d("Parse", "Count: " + i);
                    Log.d("Parse", url.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... params) { // Receive from doInBackground
            parsed.add(new Notices(params[0], params[1], params[2], params[3])); // Add values to array list
            Log.d("Parse", params[3]);
            if (params[4].equals("1")) {
                last_page = page;
                Log.d("Parse", String.valueOf(last_page));
            }
        }
    }

    public void checkFabHide() {
        if (page == 1) {
            prev.hide();
        } else {
            prev.show();
        }
        if (page == last_page) {
            next.hide();
        } else {
            next.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_notice && !(notice_type ==0)) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_url));
            intent.putExtra("type", 0);
            startActivity(intent);
        } else if (id == R.id.nav_notice_parents && !(notice_type ==1)) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_parents_url));
            intent.putExtra("type", 1);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("plain/text");
            String[] address = {"ggcj@kongjak.com"};
            email.putExtra(Intent.EXTRA_EMAIL, address);
            startActivity(email);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(getBaseContext(), InfoActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNav();
    }
}
