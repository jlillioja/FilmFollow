package io.github.jlillioja.project1;

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

public class MainActivity extends AppCompatActivity {

    protected JSONObject moviesJSON;
    protected ImageAdapter mAdapter;
    protected String sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        sort = settings.getString("sort", "popularity.desc");



        new populateMoviesTask().execute();

        GridView gridView = (GridView) findViewById(R.id.gridView);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "Not just yet.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
                try {
                    intent.putExtra(getString(R.string.movie_key), moviesJSON.getJSONArray("results").getJSONObject(position).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
            }
        });


    }

    public boolean onCreateOptionsMenu (Menu menu) {
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
            sort = "popularity.desc";
            new populateMoviesTask().execute();
            return true;
        }

        if (id == R.id.rating_sort) {
            sort = "vote_average.desc";
            new populateMoviesTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class populateMoviesTask extends AsyncTask<Void, Void, JSONObject> {


        private final String LOG_TAG = populateMoviesTask.class.getSimpleName();

        protected JSONObject doInBackground(Void... n) {



            HttpURLConnection urlConnection = null;

            try {
                //URL url = new URL("http://api.themoviedb.org/3/discover/movie?api_key=" + getApplicationContext().getString(R.string.api_key)); //TODO - softcode

                URL url = new URL(Uri.parse("http://api.themoviedb.org/3/discover/movie").buildUpon()
                        .appendQueryParameter("api_key", getApplicationContext().getString(R.string.api_key))
                        .appendQueryParameter("sort_by", sort)
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
                Log.e(LOG_TAG, "MalformedURLException", err);
                return null;
            } catch (IOException err) {
                Log.e(LOG_TAG, "IOException", err);
                return null;
            } catch (JSONException err) {
                Log.e(LOG_TAG, "JSONException", err);
                return null;
            } finally {
                urlConnection.disconnect();
            }
        }

        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                Log.d(LOG_TAG, "entered nontrivial onPostExecute");
                moviesJSON = result;
                GridView gridView = (GridView) findViewById(R.id.gridView);
                mAdapter = new ImageAdapter(getApplicationContext(), R.layout.grid_item, moviesJSON);
                gridView.setAdapter(mAdapter);

            }
        }
    }
}
