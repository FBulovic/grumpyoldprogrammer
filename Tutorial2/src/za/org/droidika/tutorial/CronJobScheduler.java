package za.org.droidika.tutorial;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;

public class CronJobScheduler extends BroadcastReceiver {
	private ContentResolver cr;
	public void startJob(Context context, int frequency)
    {
		AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CronJobScheduler.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, 0);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * frequency , pending); 
		
    }
	public void stopJob(Context context)
    {
		Intent intent = new Intent(context, CronJobScheduler.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pending);
    }
	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock lock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HOWZIT:)");
        lock.acquire();
        cr = context.getContentResolver();
        new FetchItemsTask().execute();
        lock.release();
	}
	private class FetchItemsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			ContentValues[] load;
			HttpResponse response;
			HttpClient httpClient = new DefaultHttpClient();
			URI uri = null;
			try {
				uri = new URI("http://search.twitter.com/search.json?q=android");
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				return null;
			}
			HttpGet getClient = new HttpGet();
			getClient.setURI(uri);
			StringBuffer sb = new StringBuffer("");
			try {
				response = httpClient.execute(getClient);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
			} catch (Exception e) {
				return null;
			}
			String page = sb.toString();
			if (page != null && page.length() > 0) {
				try {
					JSONObject parser = new JSONObject(page);
					JSONArray jsonArray = parser.getJSONArray("results");
					load = new ContentValues[jsonArray.length()];
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						ContentValues tmp = new ContentValues();
						tmp.put(DbHelper.USER, jsonObject.getString("from_user_name"));
						tmp.put(DbHelper.DATE, jsonObject.getString("created_at"));
						tmp.put(DbHelper.TEXT, jsonObject.getString("text"));
						load[i] = tmp;
					}
					cr.bulkInsert(SearchResultProvider.CONTENT_URI, load);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}
