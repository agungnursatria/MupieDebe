package com.anb.mupiedebe;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.anb.mupiedebe.adapter.MovieAdapter;
import com.anb.mupiedebe.controller.RestManager;
import com.anb.mupiedebe.models.Base;
import com.anb.mupiedebe.models.Result;
import com.anb.mupiedebe.utils.Constant;
import com.anb.mupiedebe.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EXTRA_SORT_BY = "EXTRA_SORT_BY";
    private static final String EXTRA_DATA = "EXTRA_DATA";
    private static final String EXTRA_POSITION = "EXTRA_POSITION";
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private Parcelable mListState = null;

    private RecyclerView rv;
    private String sortBy = "popular";
    private RestManager mManager;
    private MovieAdapter mAdapter;
    private LinearLayout llProgressBar;
    private GridLayoutManager gridLayoutManager;

    private ArrayList<Result> movies = new ArrayList<>();
    private Parcelable layoutManagerSavedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView) findViewById(R.id.rv);
        llProgressBar = (LinearLayout) findViewById(R.id.llProgressBar);

        //rv.setVisibility(View.INVISIBLE);
        llProgressBar.setVisibility(View.INVISIBLE);
        mManager = new RestManager();

        mAdapter = new MovieAdapter(this);
        gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        rv.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            sortBy = savedInstanceState.getString(EXTRA_SORT_BY);
            if (savedInstanceState.containsKey(EXTRA_DATA)) {
                List<Result> movieList = savedInstanceState.getParcelableArrayList(EXTRA_DATA);
                mAdapter.setData(movieList);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            fetchMovie(sortBy);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        mBundleRecyclerViewState = new Bundle();
        mListState = rv.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, mListState);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        GridLayoutManager layoutManager = ((GridLayoutManager) rv.getLayoutManager());
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

        outState.putInt(EXTRA_POSITION, firstVisiblePosition);
        outState.putString(EXTRA_SORT_BY, sortBy);
        outState.putParcelableArrayList(EXTRA_DATA, movies);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        movies = savedInstanceState.getParcelableArrayList(EXTRA_DATA);
        sortBy = savedInstanceState.getString(EXTRA_SORT_BY);
        int pos = savedInstanceState.getInt(EXTRA_POSITION);
        mAdapter.clearItem();
        mAdapter.setData(movies);
        mAdapter.notifyDataSetChanged();
        setLayoutManager();
        rv.setLayoutManager(gridLayoutManager);
        rv.setAdapter(mAdapter);
        rv.scrollToPosition(pos);

    }

    public void setLayoutManager() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(this, 2);
        } else {
            gridLayoutManager = new GridLayoutManager(this, 4);
        }
    }

    private void fetchMovie(String sortBy) {
        loadMovie(sortBy);
        rv.setLayoutManager(gridLayoutManager);
    }

    private void loadMovie(String sortBy) {

        if (getNetworkAvailability()) {

            rv.setVisibility(View.INVISIBLE);
            llProgressBar.setVisibility(View.VISIBLE);

            Call<Base> listCall = null;

            if (sortBy.equals("popular")) {
                listCall = mManager.getDataService().moviePopular(Constant.MOVIE_API_KEY);
            } else {
                listCall = mManager.getDataService().movieTopRated(Constant.MOVIE_API_KEY);
            }


            listCall.enqueue(new Callback<Base>() {
                @Override
                public void onResponse(Call<Base> call, Response<Base> response) {

                    if (response.isSuccessful()) {
                        Base resp = response.body();
                        List<Result> results = resp.getResults();
                        mAdapter.clearItem();
                        movies.clear();
                        for (int i = 0; i < results.size(); i++) {
                            mAdapter.addItem(results.get(i));
                            movies.add(results.get(i));
                        }
                        mAdapter.notifyDataSetChanged();
                        rv.scrollToPosition(0);

                    } else {

                        int sc = response.code();
                        Toast.makeText(getApplicationContext(), "Error code: " + sc, Toast.LENGTH_LONG)
                                .show();

                    }


                    rv.setVisibility(View.VISIBLE);
                    llProgressBar.setVisibility(View.INVISIBLE);

                }

                @Override
                public void onFailure(Call<Base> call, Throwable t) {

                    rv.setVisibility(View.VISIBLE);
                    llProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                }


            });

        } else {

            rv.setVisibility(View.VISIBLE);
            llProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Please check your internet connection.", Toast.LENGTH_LONG).show();
        }

    }

    public boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(getApplicationContext());
    }

    @Override
    public void onClick(int position) {
        Result result = mAdapter.getSelectedItem(position);
        Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
        detailIntent.putExtra(Intent.EXTRA_TEXT, String.valueOf(result.getId()));
        startActivity(detailIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_popular) {
            sortBy = "popular";
            fetchMovie(sortBy);
        } else if (id == R.id.action_rate) {
            sortBy = "rate";
            fetchMovie(sortBy);
        }
        return super.onOptionsItemSelected(item);
    }


}