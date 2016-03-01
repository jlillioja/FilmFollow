package io.github.jlillioja.project1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    protected List<JSONObject> moviesList;

    private final static String LOG_TAG = "MainActivity";

    protected ImageAdapter mAdapter;
    SharedPreferences settings;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Entered MainActivity");
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        setContentView(R.layout.activity_main);
        settings = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
                /*Convert movie whose click is registered to string in the intent. Could consider parcelable.*/
                intent.putExtra(getString(R.string.key_movie), moviesList.get(position).toString());
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            List<String> savedMoviesStringList;
            if ((savedMoviesStringList = savedInstanceState.getStringArrayList(getString(R.string.key_moviesList))) != null) {

                try {
                    moviesList = toJSONList(savedMoviesStringList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else new populateMoviesTask().execute();
        } else new populateMoviesTask().execute();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {

        if (moviesList != null) {
            savedInstanceState.putStringArrayList(getString(R.string.key_moviesList), toStringArrayList(moviesList));
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh) {
            new populateMoviesTask().execute();
            return true;
        }

        if (id == R.id.popularity_sort) {
            settings.edit().putString("sort", "popularity.desc").apply();
            new populateMoviesTask().execute();
            return true;
        }

        if (id == R.id.rating_sort) {
            settings.edit().putString("sort", "vote_average.desc").apply();
            new populateMoviesTask().execute();
            return true;
        }

        if (id == R.id.favorites_view) {
            new populateFavoritesTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class populateMoviesTask extends AsyncTask<Void, Void, JSONObject> {

        private final String LOG_TAG = populateMoviesTask.class.getSimpleName();

        protected JSONObject doInBackground(Void... n) {

            /* Declare urlConnection outside try/catch block so it can be closed in finally. */
            HttpURLConnection urlConnection = null;

            try {

                URL url = new URL(Uri.parse(getString(R.string.tmdb_discover_path)).buildUpon()
                        .appendQueryParameter(getString(R.string.api_key_query), getApplicationContext().getString(R.string.api_key))
                        .appendQueryParameter(getString(R.string.sort_query), settings.getString("sort", "popularity.desc"))
                        .build().toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                StringBuilder inStringBuilder = new StringBuilder();
                String line;

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                while (((line = reader.readLine()) != null)) {
                    inStringBuilder.append(line);
                }

                String inString = inStringBuilder.toString();
                Log.v(LOG_TAG, inString);

                return new JSONObject(inString);

            } catch (MalformedURLException err) {
                err.printStackTrace();
                return null;
            } catch (IOException err) {
                err.printStackTrace();
                return null;
            } catch (JSONException err) {
                err.printStackTrace();
                return null;
            } finally {
                urlConnection.disconnect();
            }
        }

        /* Assign internal moviesList object and create and bind ImageAdapter to grid. */
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                moviesList = new ArrayList<JSONObject>();
                try {
                    JSONArray moviesArray = result.getJSONArray(context.getString(R.string.results_key));
                    for (int i = 0; i < moviesArray.length(); i++) {
                        moviesList.add(moviesArray.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                GridView gridView = (GridView) findViewById(R.id.gridView);
                mAdapter = new ImageAdapter(context, R.layout.grid_item, moviesList);
                gridView.setAdapter(mAdapter);
            }
        }

    }

    private class populateFavoritesTask extends AsyncTask<Void, Void, List<JSONObject>> {

        @Override
        protected List<JSONObject> doInBackground(Void... params) {

            /* Fetch list of favorite IDs */
            Set<String> favoriteIDs = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE).getStringSet(getString(R.string.key_favorites), Collections.EMPTY_SET);
            if (favoriteIDs.isEmpty()) return null;
            Iterator<String> iterator = favoriteIDs.iterator();

            /* Make a list of movieJSONs with those IDs */
            /* Could this be parallelized? */
            List<JSONObject> favorites = new ArrayList<>();
            try {
                while (iterator.hasNext()) {
                    favorites.add(fetchMovie(iterator.next()));
                }
                return favorites;
            }catch (JSONException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(List<JSONObject> result) {
            moviesList = result;
            GridView gridView = (GridView) findViewById(R.id.gridView);
            mAdapter = new ImageAdapter(context, R.layout.grid_item, moviesList);
            gridView.setAdapter(mAdapter);
        }
    }

    private JSONObject fetchMovie(String id) throws IOException, JSONException {
        String LOG_TAG = "fetchMovie";
        HttpURLConnection urlConnection;
        URL url = new URL(Uri.parse(getString(R.string.tmdb_movie_path))
                .buildUpon()
                .appendPath(id)
                .appendQueryParameter(getString(R.string.api_key_query), getString(R.string.api_key))
                .build().toString());

        Log.d(LOG_TAG, url.toString());
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(false);

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        StringBuilder inStringBuilder = new StringBuilder();
        String line;

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (((line = reader.readLine()) != null)) {
            inStringBuilder.append(line);
        }

        String inString = inStringBuilder.toString();

        return new JSONObject(inString);
    }


    /*Static functions to allow (de)serialization of the list of movies */
    public static ArrayList<String> toStringArrayList(List<JSONObject> array) {
        ArrayList<String> stringList = new ArrayList<String>();
        for (int i=0;i<array.size();i++) {
            stringList.add(i, array.get(i).toString());
        }
        return stringList;
    }

    public static List<JSONObject> toJSONList (List<String> stringList) throws JSONException {
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (int i=0;i<stringList.size();i++) {
            jsonList.add(i, new JSONObject(stringList.get(i)));
        }
        return jsonList;
    }
}
