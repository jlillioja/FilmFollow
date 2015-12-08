package io.github.jlillioja.project1;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jlillioja on 11/29/2015.
 */
public class ImageAdapter extends BaseAdapter {

    private Context context;
    private JSONObject moviesJSON = null;
    int layoutResourceID;

    private final String LOG_TAG = ImageAdapter.class.getSimpleName();

    public ImageAdapter(Context context, int layoutResourceID, JSONObject moviesJSON) {
        super();
        this.context = context;
        this.moviesJSON = moviesJSON;
        this.layoutResourceID = layoutResourceID;
    }

    public int getCount() {
        if (moviesJSON == null) return 0;
        try {
            return moviesJSON.getJSONArray("results").length();
        } catch (JSONException err) {
            return 0;
        }
    }

    public JSONObject getItem(int position) {
        if (moviesJSON == null) return null;
        try {
            return (JSONObject) moviesJSON.getJSONArray("results").get(position);
        } catch (JSONException err) {
            return null;
        }
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (moviesJSON == null) {
            Toast.makeText(context, R.string.movies_not_loaded, Toast.LENGTH_LONG).show();
            return null;
        }

        View itemView;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            itemView = inflater.inflate(layoutResourceID, parent, false);
        } else {
            itemView = convertView;
        }

        try {
            JSONObject movie = moviesJSON.getJSONArray(context.getString(R.string.results_key)).getJSONObject(position);

            /* Load poster from movie into itemView's ImageView */
            loadImage((ImageView) itemView.findViewById(R.id.grid_image), movie, context);

            /* Subtitle poster with movie name. */
            ((TextView) itemView.findViewById(R.id.grid_item_title)).setText(movie.getString(context.getString(R.string.title_key)));

            return itemView;
        } catch (JSONException err) {
            err.printStackTrace();
            return null;
        }
    }

    /* Static method to allow loading of image into an ImageView given a JSONObject movie. Used on details screen without the superclass. */
    public static Uri loadImage(ImageView imageView, JSONObject movie, Context context) throws JSONException {
        String imagePath = movie.getString(context.getString(R.string.poster_key));
        Uri imageURL = Uri.parse(context.getString(R.string.tmdb_image_path)).buildUpon()
                .appendPath(context.getString(R.string.imageSize))
                .appendEncodedPath(imagePath)
                .build();
        Picasso.with(context).load(imageURL).into(imageView);
        return imageURL;
    }
}
