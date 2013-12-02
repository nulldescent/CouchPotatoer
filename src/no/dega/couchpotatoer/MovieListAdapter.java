package no.dega.couchpotatoer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MovieListAdapter<T> extends ArrayAdapter<Movie> {

	private List<Movie> movies;
	
	public MovieListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}
	
	public MovieListAdapter(Context context, int resource, List<Movie> movies) {
		super(context, resource, movies);
		this.movies = movies;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
	    View v = convertView;
	    if(v == null) {
	        LayoutInflater inflater = LayoutInflater.from(getContext());
	        v = inflater.inflate(R.layout.adapter_movielist, null);
	    }

	    Movie movie = this.movies.get(position);

	    if(movie != null) {
	        TextView title = (TextView) v.findViewById(R.id.adapter_title);
	        TextView year = (TextView) v.findViewById(R.id.adapter_year);
	        ImageView poster = (ImageView) v.findViewById(R.id.adapter_poster);
	        if(title != null) {
	            title.setText(movie.getTitle());
	        }
	        if(year != null) {
	           year.setText(String.valueOf(movie.getYear()));
	        }
	        if(poster != null) {
	        	poster.setImageDrawable(movie.getPoster());
	        }
	    }
	    return v;
	}
}
