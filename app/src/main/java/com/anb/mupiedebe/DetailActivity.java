package com.anb.mupiedebe;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anb.mupiedebe.adapter.ReviewAdapter;
import com.anb.mupiedebe.adapter.VideoAdapter;
import com.anb.mupiedebe.controller.RestManager;
import com.anb.mupiedebe.models.BaseReview;
import com.anb.mupiedebe.models.BaseVideo;
import com.anb.mupiedebe.models.Movie;
import com.anb.mupiedebe.models.Review;
import com.anb.mupiedebe.models.Video;
import com.anb.mupiedebe.utils.Constant;
import com.anb.mupiedebe.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity implements VideoAdapter.ClickListener {

    String posterPath;
    @BindView(R.id.imgPoster)
    ImageView imgPoster;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtRelease)
    TextView txtRelease;
    @BindView(R.id.txtOverview)
    TextView txtOverview;
    @BindView(R.id.txtRate)
    TextView txtRate;
    @BindView(R.id.llMain)
    LinearLayout llMain;
    @BindView(R.id.llProgressBar)
    LinearLayout llProgressBar;
    @BindView(R.id.rvVideo)
    RecyclerView rvVideo;
    @BindView(R.id.rvReview)
    RecyclerView rvReview;
    private RestManager mManager;
    private Movie selectedMovie;
    private VideoAdapter videoAdapter;
    private ReviewAdapter reviewAdapter;
    private Intent curIntent;
    private boolean showMenu = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        llMain.setVisibility(View.INVISIBLE);

        mManager = new RestManager();

        videoAdapter = new VideoAdapter(this);
        reviewAdapter = new ReviewAdapter();

        curIntent = getIntent();

        if (curIntent.hasExtra(Intent.EXTRA_TEXT)) {
            fetchMovie(curIntent.getStringExtra(Intent.EXTRA_TEXT));
        }
        LinearLayoutManager videoLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager reviewLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvVideo.setLayoutManager(videoLayoutManager);
        rvVideo.setAdapter(videoAdapter);
        rvReview.setLayoutManager(reviewLayoutManager);
        rvReview.setAdapter(reviewAdapter);


    }

    private void fetchMovie(final String movieId) {

        if (getNetworkAvailability()) {

            llMain.setVisibility(View.INVISIBLE);
            llProgressBar.setVisibility(View.VISIBLE);

            Call<Movie> listCall = mManager.getDataService().movieDetail(movieId, Constant.MOVIE_API_KEY);

            listCall.enqueue(new Callback<Movie>() {
                @Override
                public void onResponse(Call<Movie> call, Response<Movie> response) {

                    if (response.isSuccessful()) {
                        selectedMovie = response.body();
                        setMovieDetail();
                        fetchVideos(movieId);
                    } else {

                        int sc = response.code();
                        Toast.makeText(getApplicationContext(), "Error code: " + sc, Toast.LENGTH_LONG)
                                .show();

                    }

                    llMain.setVisibility(View.VISIBLE);
                    llProgressBar.setVisibility(View.INVISIBLE);

                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    llMain.setVisibility(View.VISIBLE);
                    llProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                }


            });

        } else {

            llMain.setVisibility(View.VISIBLE);
            llProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Please check your internet connection.", Toast.LENGTH_LONG).show();
            setMovieDetail();

        }

    }


    private void fetchVideos(final String movieId) {

        Call<BaseVideo> listCall = mManager.getDataService().movieVideos(movieId, Constant.MOVIE_API_KEY);

        listCall.enqueue(new Callback<BaseVideo>() {
            @Override
            public void onResponse(Call<BaseVideo> call, Response<BaseVideo> response) {

                if (response.isSuccessful()) {
                    BaseVideo baseVideo = response.body();
                    if (baseVideo != null) {
                        List<Video> videoList = baseVideo.getResults();

                        for (int i = 0; i < videoList.size(); i++) {
                            Video video = videoList.get(i);
                            videoAdapter.addItem(video);
                        }
                        videoAdapter.notifyDataSetChanged();

                        if (videoList.size() < 1) {
                            hideShare();
                        }
                    }
                    fetchReviews(movieId);
                } else {

                    int sc = response.code();
                    Toast.makeText(getApplicationContext(), "Error code: " + sc, Toast.LENGTH_LONG)
                            .show();

                }

                llMain.setVisibility(View.VISIBLE);
                llProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onFailure(Call<BaseVideo> call, Throwable t) {
                llMain.setVisibility(View.VISIBLE);
                llProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }


        });

    }

    private void fetchReviews(final String movieId) {

        Call<BaseReview> listCall = mManager.getDataService().movieReviews(movieId, Constant.MOVIE_API_KEY);

        listCall.enqueue(new Callback<BaseReview>() {
            @Override
            public void onResponse(Call<BaseReview> call, Response<BaseReview> response) {

                if (response.isSuccessful()) {
                    BaseReview baseReview = response.body();
                    if (baseReview != null) {
                        List<Review> reviewList = baseReview.getResults();
                        for (int i = 0; i < reviewList.size(); i++) {
                            Review review = reviewList.get(i);
                            reviewAdapter.addItem(review);
                        }
                        reviewAdapter.notifyDataSetChanged();
                    }
                } else {

                    int sc = response.code();
                    Toast.makeText(getApplicationContext(), "Error code: " + sc, Toast.LENGTH_LONG)
                            .show();

                }


            }

            @Override
            public void onFailure(Call<BaseReview> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }


        });

    }

    private void setMovieDetail() {

        NumberFormat numberFormat = new DecimalFormat("#,###.0");

        posterPath = Constant.MOVIE_IMAGE_BASE_URL + selectedMovie.getPosterPath();
        Picasso.with(DetailActivity.this).load(posterPath).into(imgPoster);

        txtTitle.setText(selectedMovie.getTitle());

        String releaseText = "Release Date: " + selectedMovie.getReleaseDate();
        txtRelease.setText(releaseText);

        String rateText = numberFormat.format(selectedMovie.getVoteAverage()) + " / 10";
        txtRate.setText(rateText);

        txtOverview.setText(selectedMovie.getOverview());

    }

    public boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(getApplicationContext());
    }

    @Override
    public void onVideoClick(int position) {
        try {
            String videoUrl = Constant.YOUTUBE_WATCH + videoAdapter.getSelectedItem(position).getKey();
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(myIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No application can handle this request."
                    + " Please install a webbrowser", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);

        if (!showMenu) {
            MenuItem item = menu.findItem(R.id.action_share);
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareTrailer();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareTrailer() {
        if (videoAdapter.getItemCount() > 0) {
            Video video = videoAdapter.getSelectedItem(0);
            String videoKey = video.getKey();
            if (videoKey != null) {
                String shareUrl = Constant.YOUTUBE_WATCH + videoKey;

                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareUrl);
                    startActivity(Intent.createChooser(shareIntent, "Share Trailer"));
                } catch (Exception e) {
                    Toast.makeText(this, "No application can handle this request.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "Cant find trailer in this movie.", Toast.LENGTH_LONG).show();
        }
    }

    private void hideShare() {
        showMenu = false;
        invalidateOptionsMenu();
    }
}
