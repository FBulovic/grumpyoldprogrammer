package za.org.droidika.tutorial;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "db";
	public static final String TABLE_NAME = "tweets";
	private static final String createSQL = "CREATE TABLE "+TABLE_NAME+" " +
			"(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, user_name TEXT NOT NULL, " +
			"date_name TEXT NOT NULL, text_name TEXT NOT NULL);";
	public static final String USER = "user_name";
	public static final String DATE = "date_name";
	public static final String TEXT = "text_name";
	public static final String ID = "_id";
	
	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL(createSQL);
	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		arg0.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		onCreate(arg0);
	}	
}
