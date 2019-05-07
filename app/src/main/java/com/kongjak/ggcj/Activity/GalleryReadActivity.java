package com.kongjak.ggcj.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.kongjak.ggcj.R;
import com.kongjak.ggcj.Tools.ImageFileAdapter;
import com.kongjak.ggcj.Tools.ImageFiles;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class GalleryReadActivity extends AppCompatActivity {

    String titleTxt;
    String writertxt;
    String dateTxt;
    String contentsTxt;
    String parse_url;
    String table_count;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_read);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        parse_url = getIntent().getStringExtra("url");

        mRecyclerView = findViewById(R.id.dl_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        MainPageTask asyncTask = new MainPageTask();
        asyncTask.execute();

        ImageTask imageTask = new ImageTask();
        imageTask.execute();
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
            customTabsIntent.launchUrl(this, Uri.parse(parse_url));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class MainPageTask extends AsyncTask<String, String, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(
                GalleryReadActivity.this);
        private Elements root;
        private Elements title;
        private Elements writer;
        private Elements date;
        private Elements contents;
        private Elements contentsroot;
        private Elements tables;
        private int count;
        private String contentsValue;

        @Override
        protected void onPostExecute(Void result) {
            TextView title = (TextView) findViewById(R.id.item_title);
            TextView writer = (TextView) findViewById(R.id.item_writer);
            TextView date = (TextView) findViewById(R.id.item_date);
            TextView contents = (TextView) findViewById(R.id.item_contents);

            title.setText(titleTxt);
            writer.setText(writertxt);
            date.setText(dateTxt);
            if (TextUtils.isEmpty(contentsTxt))
                contents.setText("내용이 없습니다.");
            else
                contents.setText(contentsTxt);

            asyncDialog.dismiss();

            if (!table_count.equals("0")){
                AlertDialog.Builder builder = new AlertDialog.Builder(GalleryReadActivity.this);
                builder.setTitle(getString(R.string.warning));
                builder.setMessage(getString(R.string.table_warning));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            //백그라운드 작업이 진행되는 곳.
            try {
                Document doc = Jsoup.connect(parse_url).get();
                root = doc.select("#bbsWrap > div.bbsContent > table > tbody"); // Get root view
                title = doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(1) > td"); // Get title
                writer = doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(2) > td"); // Get writer
                date = doc.select("#bbsWrap > div.bbsContent > table > tbody > tr:nth-child(4) > td"); // Get date
                contentsroot = root.select("tr:nth-child(5) > td > p"); // Get contents root
                tables = root.select("tr:nth-child(5) > td > table");

                count = contentsroot.size(); // Count!

                Log.d("Parse", String.valueOf(count));

                for (int i = 1; i <= count; i++) { // loop
                    contents = root.select("tr:nth-child(5) > td > p:nth-child(" + i + ")"); // Get contents
                    if (TextUtils.isEmpty(contentsValue))
                        contentsValue = contents.text();
                    else
                        contentsValue = contentsValue + "\n" + contents.text();
                }

                publishProgress(title.text(), writer.text(), date.text(), contentsValue, String.valueOf(tables.size())); // Send it!
                Log.d("Parse", title.text());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
                Log.e("Parse", e.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... params) { // Receive from doInBackground

            titleTxt = params[0];
            writertxt = params[1];
            dateTxt = params[2];
            contentsTxt = params[3];
            table_count = params[4];
        }
    }

    private class ImageTask extends AsyncTask<String, Object, Void> {
        ArrayList<ImageFiles> file_parsed = new ArrayList<>();
        ProgressDialog asyncDialog = new ProgressDialog(
                GalleryReadActivity.this);
        private Elements root;
        private Elements dl;
        private String dl_href;
        private Elements dl_root;
        private Elements img;

        @Override
        protected void onPostExecute(Void result) {
            ArrayList<ImageFiles> ImageFileAdapter = new ArrayList<>();
            ImageFileAdapter myAdapter = new ImageFileAdapter(ImageFileAdapter);
            mRecyclerView.setAdapter(myAdapter);
            ImageFileAdapter.addAll(file_parsed); // Add parsed's values to Real array list

            asyncDialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            //백그라운드 작업이 진행되는 곳.
            try {
                Document doc = Jsoup.connect(parse_url).get();
                root = doc.select("#bbsWrap > div.bbsContent > table > tbody"); // Get root view
                dl_root = doc.select("#bbsWrap > div.bbsContent > table > tbody > tr"); // Get root view
                int count_dl = dl_root.size(); // Count!
                Log.d("Parse", "cnt_dl" + count_dl);

                for (int i = 6; i <= count_dl; i++) { // loop
                    dl = root.select("tr:nth-child(" + i + ") > td > a"); // Get dl url
                    dl_href = dl.attr("abs:href"); // Parse REAL url(href)
                    img = root.select("tr:nth-child(" + i + ") > td > img");
                    publishProgress(dl.text(), dl_href, img.size()); // Send it!
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
                Log.e("Parse", e.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... params) { // Receive from doInBackground
            String title = (String) params[0];
            String url = (String) params[1];
            Integer img_tag = (Integer) params[2];
            System.out.println(img_tag);
            boolean isImageAvailable;
            if (img_tag.equals(0))
                isImageAvailable = false;
            else
                isImageAvailable = true;
            file_parsed.add(new ImageFiles(title, url, isImageAvailable));
        }
    }
}