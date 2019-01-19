package com.kongjak.ggcj.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.kongjak.ggcj.Tools.FileAdapter;
import com.kongjak.ggcj.Tools.Files;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class NoticeReadActivity extends AppCompatActivity {

    String titleTxt;
    String writertxt;
    String dateTxt;
    String contentsTxt;
    String parse_url;
    String dl_url;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_read);

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

        FileTask fileTask = new FileTask();
        fileTask.execute();
    }

    private class MainPageTask extends AsyncTask<String, String, Void> {
        private Elements root;
        private Elements title;
        private Elements writer;
        private Elements date;
        private Elements contents;
        private Elements contentsroot;
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
            super.onPostExecute(result);
        }

        ProgressDialog asyncDialog = new ProgressDialog(
                NoticeReadActivity.this);

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
                title = root.select("tr:nth-child(1) > td"); // Get title
                writer = root.select("tr:nth-child(2) > td"); // Get writer
                date = root.select("tr:nth-child(4) > td"); // Get date
                contentsroot = root.select("tr:nth-child(5) > td > p"); // Get contents root

                count = contentsroot.size(); // Count!

                Log.d("Parse", String.valueOf(count));

                for (int i = 1; i <= count; i++) { // loop
                    contents = root.select("tr:nth-child(5) > td > p:nth-child(" + i + ")"); // Get contents
                    if (TextUtils.isEmpty(contentsValue))
                        contentsValue = contents.text();
                    else
                        contentsValue = contentsValue + "\n" + contents.text();
                }

                publishProgress(title.text(), writer.text(), date.text(), contentsValue); // Send it!
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
        }
    }


    private class FileTask extends AsyncTask<String, String, Void> {
        private Elements root;
        private Elements dl;
        private String dl_href;
        private Elements dl_root;

        ArrayList<Files> file_parsed = new ArrayList<>();

        @Override
        protected void onPostExecute(Void result) {
            ArrayList<Files> FileAdapter = new ArrayList<>();
            com.kongjak.ggcj.Tools.FileAdapter myAdapter = new FileAdapter(FileAdapter);
            mRecyclerView.setAdapter(myAdapter);
            FileAdapter.addAll(file_parsed); // Add parsed's values to Real array list

            asyncDialog.dismiss();
            super.onPostExecute(result);
        }

        ProgressDialog asyncDialog = new ProgressDialog(
                NoticeReadActivity.this);

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

                dl = root.select("tr:nth-child(6) > td > a"); // Get dl url
                dl_href = dl.attr("abs:href"); // Parse REAL url(href)
                dl_root = root.select("tr"); // Get root view
                int count_dl = dl_root.size(); // Count!
                Log.d("Parse", "cnt_dl" + count_dl);

                for (int i = 6; i <= count_dl; i++) { // loop
                    dl = root.select("tr:nth-child(" + i + ") > td > a"); // Get dl url
                    dl_href = dl.attr("abs:href"); // Parse REAL url(href)
                    publishProgress(dl.text(), dl_href); // Send it!
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
        protected void onProgressUpdate(String... params) { // Receive from doInBackground
            file_parsed.add(new Files(params[0], params[1]));
            Log.d("Parse_dl", params[0]);
        }
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
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(parse_url)));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}