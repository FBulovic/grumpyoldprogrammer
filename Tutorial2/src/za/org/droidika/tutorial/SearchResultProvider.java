package za.org.droidika.tutorial;



import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;


public class SearchResultProvider extends ContentProvider {
	private static final String AUTHORITY = "za.org.droidika.tutorial.SearchResultProvider";
	private static final String TWEETS_BASE_PATH = "tweets";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TWEETS_BASE_PATH);
	public static final int TWEETS = 100;
    public static final int TWEET_ID = 110;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "vnd.org.droidika.tutorial/tweets";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "vnd.org.droidika.tutorial/tweets";
    private static UriMatcher sURIMatcher;
	private DbHelper dbInst;
	private static HashMap<String, String> mapper;
	static {
		sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sURIMatcher.addURI(AUTHORITY, TWEETS_BASE_PATH, TWEETS);
        sURIMatcher.addURI(AUTHORITY, TWEETS_BASE_PATH + "/#", TWEET_ID);
        mapper = new HashMap<String, String>();
        mapper.put(DbHelper.ID, DbHelper.ID);
        mapper.put(DbHelper.USER, DbHelper.USER);
        mapper.put(DbHelper.DATE, DbHelper.DATE);
        mapper.put(DbHelper.TEXT, DbHelper.TEXT);
    }
	
	@Override
	public boolean onCreate() {
		dbInst = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DbHelper.TABLE_NAME);
        switch (sURIMatcher.match(uri)) {
            case TWEETS:
                qb.setProjectionMap(mapper);
                break;
            case TWEET_ID:
                String tweetId = uri.getPathSegments().get(1);
                qb.setProjectionMap(mapper);
                qb.appendWhere(DbHelper.ID + "=" + tweetId);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbInst.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case TWEETS:
			return CONTENT_TYPE;
		case TWEET_ID:
			return CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        ContentValues tmpValues;
        if (values != null) {
        	tmpValues = new ContentValues(values);
        } else {
        	tmpValues = new ContentValues();
        }

        SQLiteDatabase db = dbInst.getWritableDatabase();
        long rowId = db.insert(DbHelper.TABLE_NAME, DbHelper.DATE, tmpValues);
        if (rowId > 0) {
            Uri citizenUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(citizenUri, null);
            return citizenUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbInst.getWritableDatabase();
        int count;

        switch (sURIMatcher.match(uri)) {
            case TWEETS:
                count = db.delete(DbHelper.TABLE_NAME, selection, selectionArgs);
                break;
            case TWEET_ID:
                String tweetId = uri.getPathSegments().get(1);
                String finalWhere = DbHelper.ID + " = " + tweetId;
                if (selection != null) {
                    finalWhere = finalWhere + " AND " + selection;
                }

                count = db.delete(DbHelper.TABLE_NAME, finalWhere, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbInst.getWritableDatabase();
        int count;
        switch (sURIMatcher.match(uri)) {
            case TWEETS:
                count = db.update(DbHelper.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TWEET_ID:
                String ssid = uri.getPathSegments().get(1);
                String finalWhere = DbHelper.ID + " = " + ssid;

                if (selection != null) {
                    finalWhere = finalWhere + " AND " + selection;
                }
                count = db.update(DbHelper.TABLE_NAME, values, finalWhere, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		final SQLiteDatabase db = dbInst.getWritableDatabase();
		final int match = sURIMatcher.match(uri);
		switch(match){
		case TWEETS:
	        int numInserted= 0;
			db.beginTransaction();
			try {
				SQLiteStatement insert = 
					db.compileStatement("insert into " + DbHelper.TABLE_NAME
							+ "(" + DbHelper.USER + "," + DbHelper.DATE
							+ "," + DbHelper.TEXT + ")"
							+" values " + "(?,?,?)");
				for (ContentValues value : values){
					insert.bindString(1, value.getAsString(DbHelper.USER));
					insert.bindString(2, value.getAsString(DbHelper.DATE));
					insert.bindString(3, value.getAsString(DbHelper.TEXT));
					insert.execute();
				}
				db.setTransactionSuccessful();
	            numInserted = values.length;
			} finally {
				db.endTransaction();
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return numInserted;
		default:
			throw new UnsupportedOperationException("unsupported uri: " + uri);
		}
	}
	public void sayHello(){
		
	}
}
