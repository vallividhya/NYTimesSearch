package com.codepath.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.adapters.ArticlesAdapter;
import com.codepath.nytimessearch.fragments.ChooseFilterDialogFragment;
import com.codepath.nytimessearch.listeners.EndlessRecyclerViewScollListener;
import com.codepath.nytimessearch.model.Article;
import com.codepath.nytimessearch.utils.ArticleSearchURLEncoder;
import com.codepath.nytimessearch.utils.NetworkUtil;
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
    int maxPages = 20;

    ArrayList<Article> articlesList;
    ArticlesAdapter adapter;
    RecyclerView rvArticles;
    EndlessRecyclerViewScollListener scrollListener;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();
    }

    public void setupViews() {
        articlesList = new ArrayList<Article>();
        rvArticles = (RecyclerView) findViewById(R.id.rvArticles);
        adapter = new ArticlesAdapter(this, articlesList);
        rvArticles.setAdapter(adapter);

        StaggeredGridLayoutManager rvLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvArticles.setLayoutManager(rvLayoutManager);
        adapter.setOnItemClickListener(new ArticlesAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View itemView, int position) {
                // Create an intent to display article
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                // Get article to display
                Article article = articlesList.get(position);
                //Pass article into intent
                intent.putExtra("article", article);
                // Launch activity
                startActivity(intent);

            }
        });

        scrollListener = new EndlessRecyclerViewScollListener(rvLayoutManager) {
            @Override
            public void onLoadMore(int pageNum, int totalItemsCount, RecyclerView view) {
                Log.d("DEBUG", "Page num = " + pageNum + " , total items count = " + totalItemsCount);
                // To limit the number of calls made in a day, load pages for a result only upto the maxPages value
                // To make sure the app makes a maximum of 1000 calls per day as X-RateLimit-Limit-day for Article search API is 1000
                 if (pageNum <= maxPages) {
                     loadNextDataFromApi(pageNum);
                 }
            }
        };

        rvArticles.addOnScrollListener(scrollListener);
    }

    private void loadNextDataFromApi(int page) {
        String query = searchView.getQuery().toString();
        RequestParams params = new RequestParams();
        params.put("api-key","66cf7a84e5f7471e94c70b7eb9ebecd4");
        params.put("page", page);
        params.put("q", query.trim());
        params = addQueryParams(params);
        getArticles(params);
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

       MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                onArticleSearch(query);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

       // return true;
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

    public void onArticleSearch(String query) {
        Log.d("DEBUG", "Searching articles.......");
        //page = 0;
        //String query = etQuery.getText().toString();
        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);

        // As this is a new search, clear the articles list off old search results
        resetEndlessScroll();

        RequestParams params = new RequestParams();
        params.put("api-key","66cf7a84e5f7471e94c70b7eb9ebecd4");
        params.put("page", 0);
        params.put("q", query.trim());
        SharedPreferences mSettings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isFilterSet = mSettings.getBoolean("isFilterSet", false);
        if (isFilterSet) {
            params = addQueryParams(params);
        }
        getArticles(params);
    }

    private RequestParams addQueryParams(RequestParams params) {
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

        String newsDeskParam = ArticleSearchURLEncoder.encodeNewsDeskValues(mSettings.getBoolean("isArts", false), mSettings.getBoolean("isFashion", false), mSettings.getBoolean("isSports", false));
        if (newsDeskParam != null && !newsDeskParam.isEmpty()) {
            params.put("fq", newsDeskParam);
        }
        return params;
    }


    @Override
    public void onFilterSave() {
        //Log.d("DEBUG", beginDate + " ... " + sortOrder) ;

        // On saving filter settings, perform a search (call API) only if there was a previous search performed.
        String query = searchView.getQuery().toString();
        resetEndlessScroll();
        RequestParams params = new RequestParams();
        params.put("api-key","66cf7a84e5f7471e94c70b7eb9ebecd4");
        params.put("page", 0);
        params.put("q", query.trim());
        params = addQueryParams(params);
        getArticles(params);

    }

    private void resetEndlessScroll() {
        // Clear list off old search results
        articlesList.clear();

        // Notify adapter of this change.
        rvArticles.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        // Reset State of scroll listener
        scrollListener.resetState();
    }


    private void getArticles(final RequestParams params) {
        // Making the API call in a handler with a delay of 1 second to stagger network call
        // This is done to restrict the number of API calls made in a second to one, which would otherwise give a "API Rate Limit Exceeded" Error
        // X-RateLimit-Limit-second = 1
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                AsyncHttpClient client = new AsyncHttpClient();
                String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
                Log.d("DEBUG", params.toString());

                // Checking if internet is available
                if (NetworkUtil.isNetworkAvailable(getApplicationContext()) && NetworkUtil.isOnline()) {

                    client.get(url, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            JSONArray articlesJson = null;
                            for (int i = 0; i < headers.length; i++) {
                                Log.d("DEBUG", "Headers for article search API = " + headers[i]);
                            }

                            try {
                                articlesJson = response.getJSONObject("response").getJSONArray("docs");
                                // Adapter's notifydatasetChanged will be called automatically
                                articlesList.addAll(Article.fromJSONArray(articlesJson));

                                if (articlesList.isEmpty()) {
                                    // No search results for that query string
                                    // Tell this to the user
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_results_msg) , Toast.LENGTH_SHORT).show();
                                    Log.d("DEBUG", "No articles to show");
                                }
                                adapter.notifyDataSetChanged();
                                Log.d("DEBUG", articlesList.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            if (statusCode != 429) {
                                // API call returned a failure. Tell this to the user
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.failure_msg) , Toast.LENGTH_SHORT).show();
                                Log.d("ERROR", errorResponse.toString());
                            }
                        }
                    });
                } else {
                    // No network connectivity- Tell this to the user
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_msg) , Toast.LENGTH_SHORT).show();
                    Log.d("ERROR", "No network connectivity");
                }

            }
        };
        handler.postDelayed(runnable, 300);
    }
}
