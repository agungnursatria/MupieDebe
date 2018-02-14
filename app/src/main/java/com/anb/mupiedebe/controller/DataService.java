package com.anb.mupiedebe.controller;

import com.anb.mupiedebe.models.Base;
import com.anb.mupiedebe.models.BaseReview;
import com.anb.mupiedebe.models.BaseVideo;
import com.anb.mupiedebe.models.Movie;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by dharmana on 6/17/17.
 */

public interface DataService {

    @GET("movie/popular")
    Call<Base> moviePopular(
            @Query("api_key") String api_key
    );

    @GET("movie/top_rated")
    Call<Base> movieTopRated(
            @Query("api_key") String api_key
    );

    @GET("movie/{movie_id}")
    Call<Movie> movieDetail(
            @Path("movie_id") String movie_id,
            @Query("api_key") String api_key
    );

    @GET("movie/{movie_id}/videos")
    Call<BaseVideo> movieVideos(
            @Path("movie_id") String movie_id,
            @Query("api_key") String api_key
    );

    @GET("movie/{movie_id}/reviews")
    Call<BaseReview> movieReviews(
            @Path("movie_id") String movie_id,
            @Query("api_key") String api_key
    );


}
