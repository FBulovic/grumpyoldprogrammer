package za.org.droidika.tutorial;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ContentResolver cr;
	private CronJobScheduler cron;
	private static final int START_ID = Menu.FIRST + 1;
	private static final int STOP_ID = Menu.FIRST + 2;
	ContentObserver statusObserver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cr = getContentResolver();
        cron = new CronJobScheduler();
        buildList();
    }
	private Cursor createCursor() {
		Cursor cursor = null;
		cursor = cr.query(SearchResultProvider.CONTENT_URI, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	private void buildList() {
		String[] columns = new String[] { DbHelper.DATE, DbHelper.TEXT,
				DbHelper.USER };
		int[] to = new int[] { R.id.textView2, R.id.textView4, R.id.textView6 };
		Cursor cursor = createCursor();
		final SearchableCursorAdapter dataAdapter = new SearchableCursorAdapter(this, R.layout.list_entry,
				cursor, columns, to);
		ListView listView = (ListView) findViewById(R.id.missingList);
		listView.setAdapter(dataAdapter);
		EditText textFilter = (EditText) findViewById(R.id.myFilter);
		textFilter.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (dataAdapter != null) {
						dataAdapter.getFilter().filter(s.toString());
				}
			}
		});
		dataAdapter.setFilterQueryProvider(dataAdapter);
	}
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, START_ID, Menu.NONE, "Start Cron job").setAlphabeticShortcut('g');
		menu.add(Menu.NONE, STOP_ID, Menu.NONE, "Stop Cron job").setAlphabeticShortcut('s');
		return (super.onCreateOptionsMenu(menu));
	}
    private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case START_ID:
			if (isNetworkAvailable()) {
				cron.startJob(getApplicationContext(),180);
			} else {
				Toast.makeText(getBaseContext(),
						"Please enable internet and try again",
						Toast.LENGTH_LONG).show();
			}
			return (true);
		case STOP_ID:
			cron.stopJob(getApplicationContext());
			return (true);
		}
		return (super.onOptionsItemSelected(item));
	}
}
