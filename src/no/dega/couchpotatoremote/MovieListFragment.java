package no.dega.couchpotatoremote;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

//Displays a list of Movies
public class MovieListFragment extends ListFragment {

	public MovieListFragment() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        String query = "movie.list?status=";

        query = getArguments().getBoolean("isWanted") ? query + "active" : query + "done";

        String request = APIUtilities.formatRequest(query, getActivity());
        new DownloadMovieListTask().execute(request);

		return rootView;
	}
	//If a user clicks on a movie, take them to the appropriate movie display
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Movie movie = (Movie) getListAdapter().getItem(position);
		Log.d("MovieListFragment:onListItemClick", movie.toString());
		
		Intent intent = new Intent(getActivity(), MovieViewActivity.class);
		intent.putExtra("no.dega.couchpotatoremote.Movie", movie);
		startActivity(intent);
	}
    /*
     *   Status: active = true = on Wanted list
     *   					false = on Manage list
     *   Returns a SparseArray of Movie's built from the list (which can be empty if there are no movies)
     */
    //TODO: what if movie list is empty
    private SparseArray<Movie> parseMovieList(String resp) {
        if((resp == null) || (resp.length() <= 0)) {
            return new SparseArray<Movie>();
        }
        //TODO: init this based on number of objects in movie list?
        SparseArray<Movie> movies = new SparseArray<Movie>();

        try {
            JSONObject response = new JSONObject(resp);
            //	Log.d(this.toString(), "String Contents: " + response.toString());
            //Make sure our request is okay and there's movies to process
            if(!response.getBoolean("success")) {
                return null;
            }
            if(response.getBoolean("empty")) {
                return movies;
            }
            JSONArray jsonMoviesList = response.getJSONArray("movies");
            //Create a movie object from every movie in the list
            for(int i = 0; i < jsonMoviesList.length(); i++) {

                int libraryId = jsonMoviesList.getJSONObject(i).getInt("library_id");
                JSONObject info = jsonMoviesList.getJSONObject(i).getJSONObject("library").
                        getJSONObject("info");
                //	Log.d(this.toString(), "Movie JSON String: " + info.toString());

                //All of these JSON fields can be null, so we have to make sure we check for that
    /*            String title;
                if(!info.isNull("titles")) {
                    title = info.getJSONArray("titles").getString(0);
                } else {
                    title = "No title";
                }*/
                String title = !info.isNull("titles") ? info.getJSONArray("titles").getString(0)
                        : "No title";
                String tagline = !info.isNull("tagline") ? info.getString("tagline") : "No tagline";
                String year = !info.isNull("year") ? info.getString("year") : "";
                String plot = !info.isNull("plot") ? info.getString("plot") : "No plot";
/*
                String tagline;
                if(!info.isNull("tagline")) {
                    tagline = info.getString("tagline");
                } else {
                    tagline = "No tagline";
                }
*/

                /*
                String plot;
                if(!info.isNull("plot")) {
                    plot = info.getString("plot");
                } else {
                    plot = "No plot";
                }*/
                String posterUri;
                if(!info.isNull("images") && !info.getJSONObject("images").isNull("poster")) {
                    posterUri = info.getJSONObject("images").getJSONArray("poster").getString(0);
                } else {
                    posterUri = "";
                }
                /*
                String year;
                if(!info.isNull("year")) {
                    year = info.getString("year");
                } else {
                    year = "";
                }*/

                String[] actors;
                if(!info.isNull("actors")) {
                    JSONArray jsonActors = info.getJSONArray("actors");
                    actors = new String[jsonActors.length()];
                    for(int j = 0; j < jsonActors.length(); j++) {
                        actors[j] = jsonActors.get(j).toString();
                    }
                } else {
                    actors = new String[0];
                }

                String[] directors;
                if(!info.isNull("directors")) {
                    JSONArray jsonDirectors = info.getJSONArray("directors");
                    directors = new String[jsonDirectors.length()];
                    if(jsonDirectors != null) {
                        for(int j = 0; j < jsonDirectors.length(); j++) {
                            directors[j] = jsonDirectors.get(j).toString();
                        }
                    }
                } else {
                    directors = new String[0];
                }

                Movie newMovie = new Movie(libraryId, title, tagline, posterUri, plot, year, actors,
                        directors);
                movies.put(libraryId, newMovie);
            }
            return movies;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SparseArray<Movie>();
    }


    private class DownloadMovieListTask extends APIRequestAsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String result) {
            SparseArray<Movie> movies;
            //TODO: put them in alphabetical order
            if((movies = parseMovieList(result)) == null) {
                movies = new SparseArray<Movie>();
            }
            ArrayList<Movie> movieList = new ArrayList<Movie>();
            for(int i = 0; i < movies.size(); i++) {
                //int key = this.movies.keyAt(i);
                movieList.add(movies.get(movies.keyAt(i)));
            }

            //Use custom movielist adapter to create the list
            final MovieListAdapter<Movie> adapter = new MovieListAdapter<Movie>(
                    getActivity(), R.layout.adapter_movielist, movieList);
            setListAdapter(adapter);
        }
    }
}