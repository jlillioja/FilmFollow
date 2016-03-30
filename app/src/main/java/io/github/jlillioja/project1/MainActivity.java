package io.github.jlillioja.project1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements DiscoverFragment.OnMovieClickListener, DetailsFragment.DetailsListener {

    static String sortSetting = "sort";
    final String LOG_TAG = "MainActivity";
    Boolean tablet;
    FragmentManager fragmentManager;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        settings = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        /* We're in tablet view */
        View masterFragment = findViewById(R.id.master_fragment);
        View detailFragment = findViewById(R.id.detail_fragment);
        tablet = (detailFragment != null);

        fragmentManager = getSupportFragmentManager();
        Fragment discoverFragment = new DiscoverFragment();

        fragmentManager.beginTransaction()
                .add(R.id.master_fragment, discoverFragment, getString(R.string.fragment_discover))
                .commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        DiscoverFragment discover = (DiscoverFragment) fragmentManager.findFragmentByTag(getString(R.string.fragment_discover));
        Boolean discoverIsPresent = false;

        if (discover != null) {
            discoverIsPresent = true;
        }

        switch (id) {
            case R.id.refresh:
                if (discoverIsPresent) {
                    discover.populateMovies();
                }
            case R.id.popularity_sort:
                settings.edit().putString(sortSetting, getString(R.string.key_popularity_desc)).apply();
                if (discoverIsPresent) {
                    discover.populateMovies();
                }
            case R.id.rating_sort:
                settings.edit().putString(sortSetting, getString(R.string.key_vote_average_desc)).apply();
                if (discoverIsPresent) {
                    discover.populateMovies();
                }
            case R.id.favorites_view:
                if (discoverIsPresent) {
                    discover.populateFavorites();
                } else {
                    DiscoverFragment discoverFragment = new DiscoverFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.master_fragment, discoverFragment)
                            .addToBackStack("Navigate to Favorites")
                            .commit();
                    discoverFragment.populateFavorites();
                }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMovieClick(JSONObject movie) {
        Utils.detailFragmentSwap(new DetailsFragment(), fragmentManager, movie, tablet, "New Movie Click");
    }

    @Override
    public void viewReviews(JSONObject movie) {
        Utils.detailFragmentSwap(new ReviewsFragment(), fragmentManager, movie, tablet, "View Reviews");
    }
}

