package za.org.droidika.tutorial;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

public class SearchableCursorAdapter extends SimpleCursorAdapter implements
		FilterQueryProvider {
	private ContentResolver contentResolver;
	private Cursor c;

	@SuppressWarnings("deprecation")
	public SearchableCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.c = c;
		contentResolver = context.getContentResolver();
	}

	public Cursor runQuery(CharSequence constraint) {
		String searchString = constraint.toString();
		if (searchString == null || searchString.length() == 0)
			c = contentResolver.query(SearchResultProvider.CONTENT_URI, null, null, null, null);
		else {
			c = contentResolver.query(SearchResultProvider.CONTENT_URI, null, DbHelper.TEXT + " like '%"
					+ searchString + "%'", null, null);
		}
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

}
