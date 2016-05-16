package michael.popularmoviestest;

/**
 * Created by Michael on 4/3/2016.
 */
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DetailActivity extends AppCompatActivity {

    private static String trailerLink;
    private static ArrayList<String> reviewContent;
    private static Button trailer_button;
    private static HorizontalScrollView scrollview;
    private static LinearLayout linscrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putStringArrayList("movie_strings", getIntent().getStringArrayListExtra("movies_strings"));

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {
        private static ArrayList<String> mMovieStr;
        private static String mMovieID;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for movie data.
            //Intent intent = getActivity().getIntent();
            Bundle arguments = getArguments();
            if (arguments != null) {
                mMovieStr = arguments.getStringArrayList("movie_strings");
                mMovieID = mMovieStr.get(5);

            //if (intent != null && intent.hasExtra("movies_strings")) {
                //mMovieStr = intent.getStringArrayListExtra("movies_strings");
               Picasso.with(getActivity()).load(mMovieStr.get(0)).into((ImageView) rootView.findViewById(R.id.detail_imageview));
                ((TextView) rootView.findViewById(R.id.detail_title))
                        .setText(mMovieStr.get(1));
                ((TextView) rootView.findViewById(R.id.detail_overview))
                        .setText(mMovieStr.get(2));
                ((TextView) rootView.findViewById(R.id.detail_voter_average))
                        .setText("Voter Average: " + mMovieStr.get(3));
                ((TextView) rootView.findViewById(R.id.detail_release_date))
                        .setText("Release Date: " + mMovieStr.get(4));
                trailer_button = (Button) rootView.findViewById(R.id.detail_trailer);

                scrollview = (HorizontalScrollView) rootView.findViewById(R.id.detail_reviews);
                linscrollview = new LinearLayout(getContext());

                CheckBox cb = (CheckBox) rootView.findViewById(R.id.add_favorite);

                String selection = "id=?";
                String[] selectionArgs = new String[]{String.valueOf(mMovieID)};
                Cursor cursor1 = getContext().getContentResolver().query(MoviesProvider.CONTENT_URI, new String[]{"favorite"}, selection, selectionArgs, "");
                if (cursor1.moveToFirst() && cursor1.getInt(cursor1.getColumnIndex("favorite")) == 1) {
                    cb.setChecked(true);
                }
                cursor1.close();
            }
            else {
                return null;
            }
            return rootView;
        }
    }

    private void updateTrailerAndReviews() {
        FetchTrailerTask trailerTask = new FetchTrailerTask();
        trailerTask.execute(DetailActivity.DetailFragment.mMovieID);
        FetchReviewTask ReviewTask = new FetchReviewTask();
        ReviewTask.execute(DetailActivity.DetailFragment.mMovieID);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTrailerAndReviews();
    }

    public void onClickAddFavorite(View view) {
        //ArrayList<String> mMovieStr;
        //Intent intent = DetailActivity.this.getIntent();
        //mMovieStr = intent.getStringArrayListExtra("movies_strings");

        // Add a new movie record
        ContentValues values = new ContentValues();
        CheckBox cb = (CheckBox) view.findViewById(R.id.add_favorite);
        values.clear();

        if (cb.isChecked()) {
            values.put(MoviesProvider.FAVORITE, 1);

            String selection = "id=?";
            String[] selectionArgs = new String[]{String.valueOf(DetailActivity.DetailFragment.mMovieStr.get(5))};
            Cursor cursor1 = getContentResolver().query(MoviesProvider.CONTENT_URI, null, selection, selectionArgs, "");
            if (cursor1.getCount() > 0) {
                cursor1.close();
                String where = "id=?";
                String[] whereArgs = new String[]{String.valueOf(DetailActivity.DetailFragment.mMovieStr.get(5))};
                getContentResolver().update(MoviesProvider.CONTENT_URI, values, where, whereArgs);
            } else {
                cursor1.close();
                values.put(MoviesProvider.ID, DetailActivity.DetailFragment.mMovieStr.get(5));
                values.put(MoviesProvider.TITLE, DetailActivity.DetailFragment.mMovieStr.get(1));
                values.put(MoviesProvider.OVERVIEW, DetailActivity.DetailFragment.mMovieStr.get(2));
                values.put(MoviesProvider.VOTER_AVERAGE, DetailActivity.DetailFragment.mMovieStr.get(3));
                values.put(MoviesProvider.RELEASE_DATE, DetailActivity.DetailFragment.mMovieStr.get(4));
                values.put(MoviesProvider.POSTER_PATH, DetailActivity.DetailFragment.mMovieStr.get(0));

                getContentResolver().insert(MoviesProvider.CONTENT_URI, values);
            }

            Toast.makeText(getBaseContext(),
                    DetailActivity.DetailFragment.mMovieStr.get(1) + " Added to Favorites.", Toast.LENGTH_LONG).show();
        } else {
            values.put(MoviesProvider.FAVORITE, 0);
            String where = "id=?";
            String[] whereArgs = new String[]{String.valueOf(DetailActivity.DetailFragment.mMovieStr.get(5))};

            getContentResolver().update(MoviesProvider.CONTENT_URI, values, where, whereArgs);

            Toast.makeText(getBaseContext(),
                    DetailActivity.DetailFragment.mMovieStr.get(1) + " Removed from Favorites.", Toast.LENGTH_LONG).show();
        }
    }

    public class FetchTrailerTask extends AsyncTask<String, Void, Integer> {

        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        /**
         * Take the String representing the complete movie data in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         */

        @Override
        protected Integer doInBackground(String... params) {
            // If there's no movie ID, there no trailer to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailerJsonStr = null;

            try {
                // Construct the URL for the TMDB query
                final String SCHEME = "https";
                final String TRAILER_BASE_URL =
                        "api.themoviedb.org";
                final String ID = params[0];
                final String APPID_PARAM = "api_key";

                Uri.Builder builder = new Uri.Builder();
                URL url = new URL(builder.scheme(SCHEME)
                        .authority(TRAILER_BASE_URL)
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(ID)
                        .appendPath("videos")
                        .appendQueryParameter(APPID_PARAM, BuildConfig.TMDB_API_KEY)
                        .build().toString());

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                trailerJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailerDataFromJson(trailerJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movies.
            return null;
        }

        private Integer getTrailerDataFromJson(String trailerJsonStr)
                throws JSONException {

            final String Trailer_Results = "results";
            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray(Trailer_Results);

            int i = 0;
            String YouTubeKey;

            // Get the JSON object representing a movie.
            JSONObject trailer = trailerArray.getJSONObject(i);
            YouTubeKey = trailer.getString("key");
            trailerLink = "https://www.youtube.com/watch?v=" + YouTubeKey;

            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (trailerLink != null) {
                trailer_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerLink));
                        startActivity(intent);
                    }
                });
            } else {
                //Toast.makeText(getApplicationContext(), "Couldn't get trailer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class FetchReviewTask extends AsyncTask<String, Void, Integer> {

        private final String LOG_TAG = FetchReviewTask.class.getSimpleName();

        /**
         * Take the String representing the complete movie data in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         */

        @Override
        protected Integer doInBackground(String... params) {
            // If there's no movie ID, there no review to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailerJsonStr = null;

            try {
                // Construct the URL for the TMDB query
                final String SCHEME = "https";
                final String TRAILER_BASE_URL =
                        "api.themoviedb.org";
                final String ID = params[0];
                final String APPID_PARAM = "api_key";

                Uri.Builder builder = new Uri.Builder();
                URL url = new URL(builder.scheme(SCHEME)
                        .authority(TRAILER_BASE_URL)
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(ID)
                        .appendPath("reviews")
                        .appendQueryParameter(APPID_PARAM, BuildConfig.TMDB_API_KEY)
                        .build().toString());

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                trailerJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewDataFromJson(trailerJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movies.
            return null;
        }

        private Integer getReviewDataFromJson(String reviewJsonStr)
                throws JSONException {

            final String Review_Results = "results";
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(Review_Results);

            reviewContent = new ArrayList<>();
            // Get the JSON object representing reviews.
            for (int i = 0; i < reviewArray.length(); i++) {
                reviewContent.add(reviewArray.getJSONObject(i).getString("content"));
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (reviewContent != null) {
                //ScrollView[] sv = new ScrollView[reviewContent.size()];
                for (int i = 0; i < reviewContent.size(); i++) {
                    final int j = i;
                    //sv[j] = new ScrollView(getApplicationContext());
                    TextView tv = new TextView(getApplicationContext());
                    tv.setText(reviewContent.get(i));
                    tv.setTextSize(12);
                    tv.setTextColor(Color.BLACK);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(500, 500));
                    params.setMargins(10, 10, 10, 10);
                    tv.setLayoutParams(params);
                    tv.setMaxLines(15);
                    tv.setMovementMethod(new ScrollingMovementMethod());
                    //sv[j].setLayoutParams(params);
                    tv.setId(i);
                    tv.setBackgroundResource(R.drawable.border);
                    //sv[j].addView(tv);
                    linscrollview.addView(tv);

/*                    tv[j].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), ReviewActivity.class)
                                    .putExtra("review", reviewContent.get(j));
                            startActivity(intent);
                        }
                    });*/

                }
                scrollview.addView(linscrollview);
            }
        }
    }
}