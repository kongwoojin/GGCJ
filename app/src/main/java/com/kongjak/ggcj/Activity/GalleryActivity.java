package com.kongjak.ggcj.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kongjak.ggcj.R;
import com.kongjak.ggcj.Tools.Gallery;
import com.kongjak.ggcj.Tools.GalleryAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    int page = 1;
    int last_page;
    FloatingActionButton prev, next;
    String parse_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parse_url = getResources().getString(R.string.gallery_url);

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

        navigationView.setCheckedItem(R.id.nav_gallery);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gallery, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_notice) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_url));
            intent.putExtra("type", 0);
            startActivity(intent);
        } else if (id == R.id.nav_notice_parents) {
            Intent intent = new Intent(getBaseContext(), NoticeActivity.class);
            intent.putExtra("url", getString(R.string.notice_parents_url));
            intent.putExtra("type", 1);
            startActivity(intent);
        } else if (id == R.id.nav_date) {
            Intent intent = new Intent(getBaseContext(), DateActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_timetable) {
            String url = "http://comci.kr/st";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(GalleryActivity.this, Uri.parse(url));
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

    private class MainPageTask extends AsyncTask<String, Object, Void> {
        ArrayList<Gallery> parsed = new ArrayList<>();
        ProgressDialog asyncDialog = new ProgressDialog(
                GalleryActivity.this);
        private Elements root;
        private Elements title;
        private Elements list;
        private Elements imageUrl;
        private Elements url;
        private Elements last_page_url;
        private int count;

        @Override
        protected void onPostExecute(Void result) {
            //doInBackground 작업이 끝나고 난뒤의 작업
            Log.d("Parse", "End");
            ArrayList<Gallery> GalleryArrayList = new ArrayList<>();
            GalleryAdapter myAdapter = new GalleryAdapter(GalleryArrayList);
            mRecyclerView.setAdapter(myAdapter);
            GalleryArrayList.addAll(parsed); // Add parsed's values to Real array list
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
            String notice_url = String.format(parse_url, page);
            try {
                Log.d("Parse", notice_url);
                Document doc = Jsoup.connect(notice_url).get();
                root = doc.select("#container > div > div.content.col-md-9 > div.contentBody > div.bbsWrap > form > div.bbsContent.mt10.clearfix > ul"); // Get root view
                list = doc.select("#container > div > div.content.col-md-9 > div.contentBody > div.bbsWrap > form > div.bbsContent.mt10.clearfix > ul > li"); // Get notice list
                last_page_url = doc.select("#container > div > div.content.col-md-9 > div.contentBody > div.bbsWrap > form > div.bbsPage > a:nth-child(14)");
                if (page == 1)
                    last_page = Integer.parseInt(last_page_url.attr("abs:href").replace(getResources().getString(R.string.gallery_url_filter), ""));
                count = list.size(); // Count notice!

                Log.d("Parse", "Count" + String.valueOf(count));

                for (int i = 1; i <= count; i++) { // loop
                    title = root.select("li:nth-child(" + i + ") > a > span");
                    imageUrl = root.select("li:nth-child(" + i + ") > a > img");
                    url = root.select("li:nth-child(" + i + ") > a");
                    String full_image_url = "http://ggcj.hs.kr/main.php" + imageUrl.attr("src");
                    InputStream is = (InputStream) new URL(full_image_url).getContent();
                    Drawable d = Drawable.createFromStream(is, "src name");
                    Log.d("Parse", title.text());
                    Log.d("Parse", "http://ggcj.hs.kr/main.php" + imageUrl.attr("src"));
                    Log.d("Parse", url.attr("abs:href"));
                    publishProgress(title.text(), url.attr("abs:href"), d);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... params) { // Receive from doInBackground
            String title = (String) params[0];
            String url = (String) params[1];
            Drawable img = (Drawable) params[2];
            parsed.add(new Gallery(title, url, img));
        }
    }
}
