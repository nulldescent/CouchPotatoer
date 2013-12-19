package no.dega.couchpotatoremote;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
    When a user clicks on a movie in the list, take them to this view activity
    User can see poster, title, tagline, year, plot and actors/directors
*/
public class MovieViewActivity extends ActionBarActivity {
    private static final String TAG = MovieViewActivity.class.getName();

    private ArrayList<Movie> movies = null;
    private Movie current = null;
    private int currentPos = 0;
    protected boolean actorsExpanded = false;
    protected boolean directorsExpanded = false;
    protected boolean plotExpanded = false;
    private GestureDetector gestureDetector = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Create the fling listener
        GestureDetector.SimpleOnGestureListener listener =
                new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                View layout = findViewById(R.id.movie_view_layout);
                Animation slideoutMovie;
                int pos;

                if(e1.getAxisValue(MotionEvent.AXIS_X) > e2.getAxisValue(MotionEvent.AXIS_X)) {
                    //Left fling
                    pos = currentPos + 1;
                    slideoutMovie = AnimationUtils.loadAnimation(layout.getContext(),
                            R.anim.movieview_slideout_left);
                } else { //Right fling
                    pos = currentPos - 1;
                    slideoutMovie = AnimationUtils.loadAnimation(layout.getContext(),
                            R.anim.movieview_slideout_right);
                }
                //Wraparound
                if(pos < 0) {
                    pos = movies.size() - 1;
                } else if(pos > movies.size() - 1) {
                    pos = 0;
                }

                final int finalPos = pos;
                slideoutMovie.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        displayMovie(finalPos);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                //Collapse the dropdowns
                hideActors();
                hideDirectors();
                hidePlot();

                layout.startAnimation(slideoutMovie);
                return true;
            }
        };
        gestureDetector = new GestureDetector(this, listener);

        Bundle bun = getIntent().getExtras();
        if (bun != null) {
            movies = bun.getParcelableArrayList("movies");
            currentPos = bun.getInt("position");
            //This will also initialise current
            displayMovie(currentPos);
        } else {
            Log.e(TAG, "Null bundle passed to movieview");
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    //Display the movie at pos on the screen.
    public void displayMovie(int pos) {
        currentPos = pos;
        current = movies.get(currentPos);

        this.setTitle(current.getTitle());

        TextView movieTitle = (TextView) findViewById(R.id.movieview_title);
        TextView movieTagline = (TextView) findViewById(R.id.movieview_tagline);
        TextView movieYear = (TextView) findViewById(R.id.movieview_year);
        ImageView poster = (ImageView) findViewById(R.id.movieview_poster);

        movieTitle.setText(current.getTitle());
        //Hide the tagline if there isn't one, helps compact the view
        if(current.getTagline().length() > 0) {
            movieTagline.setText(current.getTagline());
        } else {
            movieTagline.setVisibility(View.GONE);
        }
        movieYear.setText(current.getYear());

        //Grab from cache, or network if not cached
        ImageLoader.getInstance().displayImage(current.getPosterUri(), poster);
    }

    public void onDeleteButtonPress(View view) {
        new ConfirmMovieDeleteFragment().show(getSupportFragmentManager(), null);
    }

    public void onPlotButtonPress(View view) {
        if(!plotExpanded) {
            showPlot();
        } else {
            hidePlot();
        }
    }

    private void showPlot() {
        TextView plot = (TextView) findViewById(R.id.movieview_plot_text);
        plotExpanded = true;

        plot.setText(current.getPlot());
        plot.setVisibility(View.VISIBLE);
    }

    private void hidePlot() {
        TextView plot = (TextView) findViewById(R.id.movieview_plot_text);
        plotExpanded = false;
        plot.setVisibility(View.GONE);
    }

    //Called when user presses 'Actors' button. Expands list of actors.
    public void onActorButtonPress(View view) {
        //TODO: add some animation for expanding/unexpanding
        if(!actorsExpanded) {
            showActors();
        } else {
            hideActors();
        }
    }

    private void showActors() {
        TextView actors = (TextView) findViewById(R.id.movieview_actors_text);
        actorsExpanded = true;

        StringBuilder display = new StringBuilder();
        if(current.getActors().length > 0) {
            for(String str: current.getActors()) {
                display.append(str).append("\n");
            }
        } else {
            display.append("No actors.");
        }

        actors.setText(display.toString());
        actors.setVisibility(View.VISIBLE);
    }

    //Hide actors list
    private void hideActors() {
        TextView actors = (TextView) findViewById(R.id.movieview_actors_text);
        actorsExpanded = false;
        actors.setVisibility(View.GONE);
    }

    //Called when user presses 'Directors' button. Expands list of directors.
    public void onDirectorButtonPress(View view) {
        //TODO: add some animation for expanding/unexpanding
        if(!directorsExpanded) {
            showDirectors();
        } else {
            hideDirectors();
        }
    }

    private void showDirectors() {
        TextView directors = (TextView) findViewById(R.id.movieview_directors_text);
        directorsExpanded = true;

        StringBuilder display = new StringBuilder();
        if(current.getDirectors().length > 0) {
            for(String str: current.getDirectors()) {
                display.append(str).append("\n");
            }
        } else {
            display.append("No directors.");
        }

        directors.setText(display.toString());
        directors.setVisibility(View.VISIBLE);
    }

    private void hideDirectors() {
        TextView directors = (TextView) findViewById(R.id.movieview_directors_text);
        //Already expanded, and we need to close
        directorsExpanded = false;
        directors.setVisibility(View.GONE);
    }

    public void onQualityButtonPress(View view) {
        new ChangeQualityFragment().show(getSupportFragmentManager(), null);
    }

    //Popup box for user to change qualities
    private class ChangeQualityFragment extends DialogFragment {
        private ArrayList<Quality> qualities = null;
        private Spinner spinner = null;
        private Quality selection = null;
        private AlertDialog dialog = null;
        private TextView loading = null;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setRetainInstance(true);

            View view = getActivity().getLayoutInflater().inflate(R.layout.change_quality_dialog, null);
            spinner = (Spinner) view.findViewById(R.id.changequality_selector);
            spinner.setVisibility(View.GONE);

            loading = (TextView) view.findViewById(R.id.changequality_wait);

            //Get a list of qualities
            new GetQualitiesTask(MovieViewActivity.this).execute(
                    APIUtilities.formatRequest("quality.list", MovieViewActivity.this));

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setView(view)
                    //Accept the quality change
                    .setPositiveButton(R.string.accept_change_quality, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Qualities haven't loaded yet
                            if(selection == null) {
                                return;
                            }
                            StringBuilder builder = new StringBuilder();
                            builder.append("movie.edit?").append("id=").append(current.getLibraryId())
                                    .append("&profile_id=").append(selection.getId());
                            new APIRequestAsyncTask<String, Void, String>(MovieViewActivity.this)
                                    .execute(APIUtilities.formatRequest(builder.toString(), MovieViewActivity.this));

                            Toast.makeText(MovieViewActivity.this, current.getTitle() + "'s quality changed to " + selection.getLabel() + ".",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.reject_change_quality, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
            dialog = builder.create();
            dialog.show();
            dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            return dialog;
        }

        private class GetQualitiesTask extends APIRequestAsyncTask<String, Void, String> {

            public GetQualitiesTask(Context context) {
                super(context);
            }

            @Override
            protected void onPostExecute(String result) {
                parseQualitiesList(result);
                //Change the selected quality
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                            selection = qualities.get(pos);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {}
                    });

                ArrayAdapter<Quality> adapter = new ArrayAdapter<Quality>(MovieViewActivity.this,
                        R.layout.spinner_dropdown_tight, qualities);
                spinner.setAdapter(adapter);

                loading.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
            //Parse the list of qualities
            private void parseQualitiesList(String result) {
                try {
                    JSONArray list = new JSONObject(result).getJSONArray("list");
                    qualities = new ArrayList<Quality>(list.length());
                    for(int i = 0; i < list.length(); i++) {
                        JSONObject quality = list.getJSONObject(i);
                        Quality qual = new Quality(quality.getInt("id"), quality.getString("label"));
                        qualities.add(qual);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //Small object that holds quality name and its profile id
        private class Quality {
            private final int id;
            private final String label;

            public Quality(int id, String label) {
                this.id = id;
                this.label = label;
            }
            public String getLabel() {
                return label;
            }
            public int getId() {
                return id;
            }
            public String toString() {
                return label;
            }
        }
    }

    //Alert dialog to confirm that the user really wants to delete a movie
    private class ConfirmMovieDeleteFragment extends DialogFragment {
        public ConfirmMovieDeleteFragment() {

        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.confirm_delete_movie)
                //Confirm delete
                .setPositiveButton(R.string.accept_delete_movie, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String request = APIUtilities.formatRequest(
                                "movie.delete?id=" + String.valueOf(current.getLibraryId()), getActivity());
                        //Send the request. Don't need to subclass this
                        new APIRequestAsyncTask<String, Void, String>(getActivity()).execute(request);
                        movies.remove(currentPos);
                        finish();
                    }
                })
                //Reject delete
                .setNegativeButton(R.string.reject_delete_movie, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        return builder.create();
        }
    }
}
