package michael.popularmoviestest;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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

/**
 * Created by Michael on 4/2/2016.
 */
public class MovieFragment extends Fragment{

    private michael.popularmoviestest.ImageAdapter mAdapter;

    private ArrayList<michael.popularmoviestest.MovieClass> moviesData;

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(ArrayList<String> moviesArray);
    }

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        moviesData = new ArrayList<>();
        mAdapter = new ImageAdapter(getActivity(), R.id.grid_item_movies_imageview, moviesData);
        gridView.setAdapter(mAdapter);

/*        if (moviesData.size() == 0) {
            Toast.makeText(getContext(),
                    "No Favorites to Show", Toast.LENGTH_SHORT).show();
        }*/

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MovieClass moviesStrings = moviesData.get(position);
                ArrayList<String> moviesArray = new ArrayList<>();
                moviesArray.add(moviesStrings.getPoster());
                moviesArray.add(moviesStrings.getTitle());
                moviesArray.add(moviesStrings.getOverview());
                moviesArray.add(moviesStrings.getVoter_average());
                moviesArray.add(moviesStrings.getRelease_date());
                moviesArray.add(moviesStrings.getID());

                ((Callback) getActivity())
                        .onItemSelected(moviesArray);

/*                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putStringArrayListExtra("movies_strings", moviesArray);
                startActivity(intent);*/
            }
        });

        return rootView;
    }

    private void updateMovies() {
        FetchMovieTask movieTask = new FetchMovieTask();
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortSetting = SP.getString("sortSetting", "1");
        movieTask.execute(sortSetting);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMovies();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Integer> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        /**
         * Take the String representing the complete movie data in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         */

        @Override
        protected Integer doInBackground(String... params) {
            // If there's no sorting selection, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            if (params[0].equals("1") || params[0].equals("2")) {
                try {
                    // Construct the URL for the TMDB query
                    final String SCHEME = "https";
                    final String MOVIE_BASE_URL =
                            "api.themoviedb.org";
                    final String SORT_PARAM;
                    if (params[0].equals("1")) {
                        SORT_PARAM = "popular";
                    } else {
                        SORT_PARAM = "top_rated";
                    }
                    final String APPID_PARAM = "api_key";

                    Uri.Builder builder = new Uri.Builder();
                    URL url = new URL(builder.scheme(SCHEME)
                            .authority(MOVIE_BASE_URL)
                            .appendPath("3")
                            .appendPath("movie")
                            .appendPath(SORT_PARAM)
                            .appendQueryParameter(APPID_PARAM, BuildConfig.TMDB_API_KEY)
                            .build().toString());

                    // Create the request to OpenWeatherMap, and open the connection
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
                    movieJsonStr = buffer.toString();
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
                    return getMovieDataFromJson(movieJsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            }
            else {
                moviesData.removeAll(moviesData);
                String selection = "CAST(favorite AS TEXT) = ?";
                String[] selectionArgs = new String[]{"1"};
                Cursor movieCursor = getContext().getContentResolver().query(MoviesProvider.CONTENT_URI, null, selection, selectionArgs, "");

                String title;
                String poster_path;
                String overview;
                String voter_average;
                String release_date;
                String ID;
                while (movieCursor.moveToNext()) {
                    title = movieCursor.getString(movieCursor.getColumnIndex("title"));
                    poster_path = movieCursor.getString(movieCursor.getColumnIndex("poster_path"));
                    overview = movieCursor.getString(movieCursor.getColumnIndex("overview"));
                    voter_average = movieCursor.getString(movieCursor.getColumnIndex("voter_average"));
                    release_date = movieCursor.getString(movieCursor.getColumnIndex("release_date"));
                    ID = movieCursor.getString(movieCursor.getColumnIndex("id"));
                    moviesData.add(new michael.popularmoviestest.MovieClass(title,poster_path,overview,voter_average,release_date,ID));
                }
                return 1;
            }
            // This will only happen if there was an error getting or parsing the movies.
            return null;
        }

        private Integer getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            final String TMDB_Results = "results";
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_Results);

            moviesData.clear();

            for(int i = 0; i < movieArray.length(); i++) {
                String title;
                String poster_path;
                String overview;
                String voter_average;
                String release_date;
                String ID;

                // Get the JSON object representing a movie.
                JSONObject movie = movieArray.getJSONObject(i);
                title = movie.getString("original_title");
                poster_path = movie.getString("poster_path");
                overview = movie.getString("overview");
                voter_average = movie.getString("vote_average");
                release_date = movie.getString("release_date");
                ID = movie.getString("id");

                moviesData.add(new michael.popularmoviestest.MovieClass(title,poster_path,overview,voter_average,release_date,ID));
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(moviesData!=null){
                mAdapter.setGridData(moviesData);
                mAdapter.notifyDataSetChanged();
            }
            else
                Toast.makeText(getActivity(), "Couldn't get data", Toast.LENGTH_SHORT).show();
        }
    }
}
