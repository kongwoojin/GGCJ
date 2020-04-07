package com.kongjak.ggcj.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
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
        notice_type = getIntent().getIntExtra("type", 0);
        Log.d("URL", String.format(parse_url, page));

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

        prev.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (page != 1) {
                    page = 1;
                    MainPageTask asyncTask = new MainPageTask();
                    asyncTask.execute();
                } else {
                    Snackbar.make(view, getString(R.string.first_page), Snackbar.LENGTH_SHORT).show();
                }
                return true;
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

        next.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (page != last_page) {
                    page = last_page;
                    MainPageTask asyncTask = new MainPageTask();
                    asyncTask.execute();
                } else {
                    Snackbar.make(view, getString(R.string.last_page), Snackbar.LENGTH_SHORT).show();
                }
                return true;
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
        } else if (id == R.id.nav_notice && !(notice_type == 0)) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_url));
            intent.putExtra("type", 0);
            startActivity(intent);
        } else if (id == R.id.nav_notice_parents && !(notice_type == 1)) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_parents_url));
            intent.putExtra("type", 1);
            startActivity(intent);
        } else if (id == R.id.nav_date) {
            Intent intent = new Intent(getBaseContext(), DateActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(getBaseContext(), GalleryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_timetable) {
            String url = "http://comci.kr/st";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(NoticeActivity.this, Uri.parse(url));
        } else if (id == R.id.nav_send) {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:"));
            String[] address = {"ggcj@kongjak.com"};
            email.putExtra(Intent.EXTRA_EMAIL, address);
            String title = getResources().getString(R.string.nav_send_intent);
            Intent chooser = Intent.createChooser(email, title);
            if (email.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(getBaseContext(), InfoActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_web) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(String.format(parse_url, page)));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNav();
    }

    private class MainPageTask extends AsyncTask<String, String, Void> {
        ArrayList<Notices> parsed = new ArrayList<>();
        ProgressDialog asyncDialog = new ProgressDialog(
                NoticeActivity.this);
        private Elements root;
        private Elements title;
        private Elements list;
        private Elements writer;
        private Elements date;
        private Elements url;
        private Elements numoflist;
        private Elements last_page_url;
        private int count;

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
                last_page_url = doc.select("#bbsWrap > form > div.bbsPage > li:nth-child(14) > a");
                if (page == 1)
                    last_page = Integer.parseInt(last_page_url.attr("abs:href").replaceAll("(.*)Page=", ""));
                count = list.size(); // Count notice!

                Log.d("Parse", "GGCJ" + String.valueOf(last_page));
                Log.d("Parse", "Count" + String.valueOf(count));

                for (int i = 1; i <= count; i++) { // loop
                    title = root.select("tr:nth-child(" + i + ") > td.tit"); // Get title
                    numoflist = root.select("tr:nth-child(" + i + ") > td:nth-child(1)"); // Get number of notice
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
            boolean isImportant = false;
            if (params[4].equals("공지")) {
                isImportant = true;
            }

            parsed.add(new Notices(params[0], params[1], params[2], params[3], isImportant)); // Add values to array list
            Log.d("Parse", params[3]);
        }
    }
}
