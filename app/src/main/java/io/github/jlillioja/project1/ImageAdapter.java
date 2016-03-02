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

import java.util.List;

/**
 * Created by jlillioja on 11/29/2015.
 */
public class ImageAdapter extends BaseAdapter {

    private final String LOG_TAG = ImageAdapter.class.getSimpleName();
    int layoutResourceID;
    private Context context;

    private List<JSONObject> movies = null;
    int layoutResourceID;

    private final String LOG_TAG = ImageAdapter.class.getSimpleName();


    public ImageAdapter(Context context, int layoutResourceID, List<JSONObject> movies) {
        super();
        this.context = context;
        this.movies = movies;
        this.layoutResourceID = layoutResourceID;
    }

    /* Static method to allow loading of image into an ImageView given a JSONObject movie. Used on details screen without the superclass. */
    public static Uri loadImage(ImageView imageView, JSONObject movie, Context context) throws JSONException {
        String imagePath = movie.getString(context.getString(R.string.poster_key));
        Uri imageURL = Uri.parse(context.getString(R.string.tmdb_image_path)).buildUpon()
                .appendPath(context.getString(R.string.imageSize))
                .appendEncodedPath(imagePath)
                .build();

        Picasso.with(context)
                .load(imageURL)
                .into(imageView);

        return imageURL;
    }

    public int getCount() {
        if (movies == null) return 0;
        else return movies.size();
    }

    public JSONObject getItem(int position) {
        if (movies == null) return null;
        else return (JSONObject) movies.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View itemView;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            itemView = inflater.inflate(layoutResourceID, parent, false);
        } else {
            itemView = convertView;
        }

        if (movies == null) {
            Toast.makeText(context, R.string.movies_not_loaded, Toast.LENGTH_LONG).show();
            return null;
        } else {
            try {
                JSONObject movie = movies.get(position);


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
    }
}
