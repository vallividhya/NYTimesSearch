package com.codepath.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.adapters.ArticleArrayAdapter;
import com.codepath.nytimessearch.fragments.ChooseFilterDialogFragment;
import com.codepath.nytimessearch.listeners.EndlessScrollListener;
import com.codepath.nytimessearch.model.Article;
import com.codepath.nytimessearch.utils.URLEncoder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements ChooseFilterDialogFragment.OnChooseFilterDialogListener {
    String API_KEY = "66cf7a84e5f7471e94c70b7eb9ebecd4";
    EditText etQuery;
    GridView gvResults;
    Button btnSearch;
    int page = 0;
    int visibleScrollLimit = 12;

    ArrayList<Article> articlesList;
    ArticleArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();
    }

    public void setupViews() {
        etQuery = (EditText) findViewById(R.id.etQuery);
        gvResults = (GridView) findViewById(R.id.gvResults);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        articlesList = new ArrayList<Article>();
        adapter = new ArticleArrayAdapter(this, articlesList);
        gvResults.setAdapter(adapter);

        // ClickListener for grid click
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Create an intent to display article
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                // Get article to display
                Article article = articlesList.get(i);
                //Pass article into intent
                intent.putExtra("article", article);
                // Launch activity
                startActivity(intent);

            }
        });
        gvResults.setOnScrollListener(new EndlessScrollListener(visibleScrollLimit, page) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page);
                return true;
            }
        });
    }

    private void loadNextDataFromApi(int page) {
        this.page = page;
        onFilterSave();

    }

    public void onFilterSearchResults() {
        //Open a dialog fragment showing filter options
        FragmentManager fm = getSupportFragmentManager();
        // Call new Instamce of dialogFrament, passing the title
        ChooseFilterDialogFragment chooseFilterDialogFragment = ChooseFilterDialogFragment.newInstance(getResources().getString(R.string.filterDialogTitle));
        //chooseFilterDialogFragment.setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        // Show dialog
        chooseFilterDialogFragment.show(fm, getString(R.string.ChooseFilterFragmentTag));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.miFilter) {
            onFilterSearchResults();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onArticleSearch(View view) {
        String query = etQuery.getText().toString();
        RequestParams params = new RequestParams();
        params.put("api-key","66cf7a84e5f7471e94c70b7eb9ebecd4");
        params.put("page", page);
        params.put("q", query);
        SharedPreferences mSettings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isFilterSet = mSettings.getBoolean("isFilterSet", false);
        if (isFilterSet) {
            params = addQueryParams(params);
        }
        getArticles(params);
    }

    private RequestParams addQueryParams(RequestParams params) {
        //params.put("api-key","66cf7a84e5f7471e94c70b7eb9ebecd4");
        //params.put("page", 0);
        SharedPreferences mSettings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String beginDate = mSettings.getString("beginDate", null);
        if (beginDate != null && !beginDate.isEmpty()) {
            params.put("begin_date", beginDate);
        }
        String sortOrder = mSettings.getString("sortOrder", null);
        if (sortOrder != null && !sortOrder.isEmpty()) {
            if (!sortOrder.equals("none")) {
                params.put("sort", sortOrder);
            }
        }

        String newsDeskParam = URLEncoder.encodeNewsDeskValues(mSettings.getBoolean("isArts", false), mSettings.getBoolean("isFashion", false), mSettings.getBoolean("isSports", false));
        if (newsDeskParam != null && !newsDeskParam.isEmpty()) {
            params.put("fq", newsDeskParam);
        }
        return params;
    }


    @Override
    public void onFilterSave() {
        //Log.d("DEBUG", beginDate + " ... " + sortOrder) ;
        RequestParams params = new RequestParams();
        params.put("api-key","66cf7a84e5f7471e94c70b7eb9ebecd4");
        params.put("page", page);
        params = addQueryParams(params);
        getArticles(params);
    }

    private void getArticles(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articlesJson = null;

                try {
                    articlesJson = response.getJSONObject("response").getJSONArray("docs");
                    // Adapter's notifydatasetChanged will be called automatically
                    adapter.addAll(Article.fromJSONArray(articlesJson));
                    Log.d("DEBUG", articlesList.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                if (statusCode == 429) {
//
//                }
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.failure_msg) , Toast.LENGTH_LONG).show();
                Log.d("ERROR", errorResponse.toString());
            }
        });
    }
}
