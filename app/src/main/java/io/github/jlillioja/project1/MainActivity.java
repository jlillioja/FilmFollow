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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected List<JSONObject> moviesList;
    protected ImageAdapter mAdapter;
    SharedPreferences settings;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        setContentView(R.layout.activity_main);
        settings = getPreferences(MODE_PRIVATE);

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
            Toast.makeText(getApplicationContext(), getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
            new populateFavoritesTask().execute();
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
                JSONObject moviesJSON = result;
                List<JSONObject> moviesList = new ArrayList<JSONObject>();
                try {
                    JSONArray moviesArray = moviesJSON.getJSONArray(context.getString(R.string.results_key));
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

    private class populateFavoritesTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            List<Favorite> favoritesList = Favorite.listAll(Favorite.class);
            return unwrapMovies(favoritesList);
        }

        protected void onPostExecute(List<JSONObject> result) {
            moviesList = result;
            GridView gridView = (GridView) findViewById(R.id.gridView);
            mAdapter = new FavoritesAdapter(context, R.layout.grid_item, moviesList);
            gridView.setAdapter(mAdapter);
        }


        private List<JSONObject> unwrapMovies(List<Favorite> favoritesList) {
            List<JSONObject> moviesJSONList = new ArrayList<JSONObject>();
            for (int i=0;i<favoritesList.size();i++) {
                moviesJSONList.add(i, favoritesList.get(i).movie);
            }
            return moviesJSONList;
        }


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
