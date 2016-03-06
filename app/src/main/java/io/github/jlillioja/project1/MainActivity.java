package io.github.jlillioja.project1;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements DiscoverFragment.OnMovieClickListener, DetailsFragment.DetailsListener {

    Boolean tablet;
    FragmentManager fragmentManager;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        /* We're in tablet view */
        tablet = findViewById(R.id.detail_fragment) != null;

        fragmentManager = getFragmentManager();
        Fragment discoverFragment = new DiscoverFragment();

        fragmentManager.beginTransaction()
                .add(R.id.master_fragment, discoverFragment)
                .commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        DiscoverFragment discover = (DiscoverFragment) fragmentManager.findFragmentById(R.id.fragment_discover);
        Boolean discoverIsPresent = false;
        if (discover != null) {
            discoverIsPresent = true;
        }

        if (id == R.id.refresh) {
            if (discoverIsPresent) discover.populateMovies();
            return true;
        }

        if (id == R.id.popularity_sort) {
            settings.edit().putString("sort", "popularity.desc").apply();
            if (discoverIsPresent) discover.populateMovies();
            return true;
        }

        if (id == R.id.rating_sort) {
            settings.edit().putString("sort", "vote_average.desc").apply();
            if (discoverIsPresent) discover.populateMovies();
            return true;
        }

        if (id == R.id.favorites_view) {
            if (discoverIsPresent) discover.populateFavorites();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /* The following two methods are almost the same, but from different interfaces. How can I DRY it up? */
    @Override
    public void onMovieClick(JSONObject movie) {
        DetailsFragment detailFragment = new DetailsFragment();
        Bundle movieArgument = new Bundle();
        movieArgument.putString(getString(R.string.key_movie), movie.toString());
        detailFragment.setArguments(movieArgument);
        if (tablet) {
            fragmentManager.beginTransaction()
                    .replace(R.id.detail_fragment, detailFragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.master_fragment, detailFragment)
                    .commit();
        }
    }

    @Override
    public void onViewReviews(JSONObject movie) {
        ReviewsFragment reviewsFragment = new ReviewsFragment();
        Bundle movieArgument = new Bundle();
        movieArgument.putString(getString(R.string.key_movie), movie.toString());
        reviewsFragment.setArguments(movieArgument);
        if (tablet) {
            fragmentManager.beginTransaction()
                    .replace(R.id.detail_fragment, reviewsFragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.master_fragment, reviewsFragment)
                    .commit();
        }
    }
}

