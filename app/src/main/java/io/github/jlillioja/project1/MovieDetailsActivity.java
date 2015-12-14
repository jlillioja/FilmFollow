package io.github.jlillioja.project1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();
        try {
            JSONObject movie = new JSONObject(intent.getStringExtra(getString(R.string.movie_key)));

            ImageView image = (ImageView) findViewById(R.id.poster_imageView);
            ImageAdapter.loadImage(image, movie, this);

            TextView title = (TextView) findViewById(R.id.title_textView);
            title.setText(getString(R.string.title_title) + movie.getString("original_title"));

            TextView overview = (TextView) findViewById(R.id.overview_textView);
            overview.setText(getString(R.string.overview_title) + movie.getString("overview"));

            TextView release = (TextView) findViewById(R.id.release_textView);
            release.setText(getString(R.string.release_date_title) + movie.getString("release_date"));

            TextView rating = (TextView) findViewById(R.id.rating_textView);
            rating.setText(getString(R.string.rating_title) + movie.getString("vote_average"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}