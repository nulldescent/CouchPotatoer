package no.dega.couchpotatoremote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

//TODO: decide on whether/where to put the tagline
public class MovieViewActivity extends ActionBarActivity {
    Movie movie = null;
    boolean actorExpanded = false;
    boolean directorExpanded = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bun = getIntent().getExtras();
        TextView movieTitle = (TextView) findViewById(R.id.movieview_title);
        TextView moviePlot = (TextView) findViewById(R.id.movieview_plot);
       // TextView movieTagline = (TextView) findViewById(R.id.movieview_tagline);
        TextView movieYear = (TextView) findViewById(R.id.movieview_year);
        ImageView poster = (ImageView) findViewById(R.id.movieview_poster);

        if (bun != null) {
            movie = bun.getParcelable("no.dega.couchpotatoremote.Movie");
            if (movie != null) {
                this.setTitle(movie.getTitle());

                movieTitle.setText(movie.getTitle());
                moviePlot.setText("Plot: " + movie.getPlot());
              //  movieTagline.setText(movie.getTagline());
                movieYear.setText(movie.getYear());

                //Grab from cache, or network if not cached
                ImageLoader.getInstance().displayImage(movie.getPosterUri(), poster);
            } else {
                Log.e("MovieViewActivity", "Movie passed to MovieView is null (parcelling may have failed)");
            }
        } else {
            Log.e("MovieViewActivity", "Null bundle passed to movieview");
        }
    }

    public void onActorButtonPress(View view) {
        Fragment frag = new ActorDirectorFragment();
        Bundle args = new Bundle();
        args.putStringArray("Names", movie.getActors());
        frag.setArguments(args);
        getSupportFragmentManager().beginTransaction().
                replace(R.id.movieview_actors_placeholder, frag).commit();
    }
}
