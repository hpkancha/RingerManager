package edu.ncsu.soc.rms;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class RingerManagerLocations extends ContentProvider {
	public static final Uri LOCATIONS_URI = Uri.parse("content://edu.ncsu.soc.rms/locations");
	  //Create the constants used to differentiate between the different URI requests.
	  private static final int LOCATIONS = 1;
	  private static final int LOCATION_ID = 2;

	  private static final UriMatcher uriMatcher;
	  // Allocate the UriMatcher object, where a URI ending in 'locations' will
	  // correspond to a request for all locations, and 'locations' with a trailing
	  // '/[locationID]' will represent a single row.
	  static {
	    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	    uriMatcher.addURI("edu.ncsu.soc.rms", "locations", LOCATIONS);
	    uriMatcher.addURI("edu.ncsu.soc.rms", "locations/#", LOCATION_ID);
	  }

	  @Override
	  public boolean onCreate() {
	    Context context = getContext();

	    locationsDatabaseHelper dbHelper;
	    dbHelper = new locationsDatabaseHelper(context, DATABASE_NAME,
	        null, DATABASE_VERSION);
	    locationDB = dbHelper.getWritableDatabase();
	    return (locationDB == null) ? false : true;
	  }
	  
	  @Override
	  public String getType(Uri uri) {
	    switch (uriMatcher.match(uri)) {
	    case LOCATIONS: return "vnd.android.cursor.dir/vnd.ncsu.alert";
	    case LOCATION_ID: return "vnd.android.cursor.item/vnd.ncsu.alert";
	    default: throw new IllegalArgumentException("Unsupported URI: " + uri);
	    }
	  }
	   
	  @Override
	  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
	      String sort) {
	    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	    qb.setTables(LOCATION_TABLE);

	    // If this is a row query, limit the result set to the passed in row.
	    switch (uriMatcher.match(uri)) {
	    case LOCATION_ID:
	      qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
	      break;
	    default:
	      break;
	    }

	    // If no sort order is specified sort by date / time
	    String orderBy;
	    if (TextUtils.isEmpty(sort)) {
	      orderBy = KEY_ID;
	    } else {
	      orderBy = sort;
	    }

	    // Apply the query to the underlying database.
	    Cursor c = qb.query(locationDB, projection, selection, selectionArgs, null, null, orderBy);

	    // Register the contexts ContentResolver to be notified if
	    // the cursor result set changes.
	    c.setNotificationUri(getContext().getContentResolver(), uri);

	    // Return a cursor to the query result.
	    return c;
	  }

	  @Override
	  public Uri insert(Uri _uri, ContentValues _initialValues) {
	    // Insert the new row, will return the row number if successful.
		  System.out.println("In INSERT");
		  System.out.println(_initialValues);
	    long rowID = locationDB.insert(LOCATION_TABLE, "location", _initialValues);

	    // Return a URI to the newly inserted row on success.
	    if (rowID > 0) {
	      Uri uri = ContentUris.withAppendedId(LOCATIONS_URI, rowID);
	      getContext().getContentResolver().notifyChange(uri, null);
	      return uri;
	    }
	    throw new SQLException("Failed to insert row into " + _uri);
	  }

	  @Override
	  public int delete(Uri uri, String where, String[] whereArgs) {
	    int count;

	    switch (uriMatcher.match(uri)) {
	    case LOCATIONS:
	      count = locationDB.delete(LOCATION_TABLE, where, whereArgs);
	      break;

	    case LOCATION_ID:
	      String segment = uri.getPathSegments().get(1);
	      count = locationDB.delete(LOCATION_TABLE, KEY_ID + "="
	          + segment
	          + (!TextUtils.isEmpty(where) ? " AND ("
	              + where + ')' : ""), whereArgs);
	      break;

	    default: throw new IllegalArgumentException("Unsupported URI: " + uri);
	    }

	    getContext().getContentResolver().notifyChange(uri, null);
	    return count;
	  }

	  @Override
	  public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
	    int count;
	    System.out.println("In UPDATE");
		  System.out.println(values);
	    switch (uriMatcher.match(uri)) {
	    case LOCATIONS: count = locationDB.update(LOCATION_TABLE, values, where, whereArgs);
	    break;

	    case LOCATION_ID: String segment = uri.getPathSegments().get(1);
	    count = locationDB.update(LOCATION_TABLE, values, KEY_ID
	        + "=" + segment
	        + (!TextUtils.isEmpty(where) ? " AND ("
	            + where + ')' : ""), whereArgs);
	    break;

	    default: throw new IllegalArgumentException("Unknown URI " + uri);
	    }

	    getContext().getContentResolver().notifyChange(uri, null);
	    System.out.println("UPDATE:" + count);
	    return count;
	  }
	  
	  /** The underlying database */
	  private SQLiteDatabase locationDB;

	  private static final String TAG = "RingerManagerLocations";
	  private static final String DATABASE_NAME = "locations.db";
	  private static final int DATABASE_VERSION = 1;
	  private static final String LOCATION_TABLE = "locations";

	  // Column Names
	  public static final String KEY_ID = "_id";
	  public static final String KEY_PLACE_LAT = "latitude";
	  public static final String KEY_PLACE_LNG = "longitude";
	  public static final String KEY_ISREP = "isrep";
	  public static final String KEY_LOCATION = "location";
	  public static final String KEY_MODE = "mode";

	  // Column indexes
	  public static final int LATITUDE_COLUMN = 1;
	  public static final int LONGITUDE_COLUMN = 2;
	  public static final int ISREP_COLUMN = 3;
	  public static final int LOCATION_COLUMN = 4;
	  public static final int MODE_COLUMN = 5;

	  // Helper class for opening, creating, and managing database version control
	  private static class locationsDatabaseHelper extends SQLiteOpenHelper {
	    private static final String DATABASE_CREATE =
	      "create table " + LOCATION_TABLE + " ("
	      + KEY_ID + " integer primary key autoincrement, "
	      + KEY_PLACE_LAT + " INTEGER, "
	      + KEY_PLACE_LNG + " INTEGER, "
	      + KEY_ISREP + " INTEGER, "
	      + KEY_LOCATION + " TEXT, "
	      + KEY_MODE + " TEXT );";

	    /** Helper class for managing the Earthquake database */
	    public locationsDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
	      super(context, name, factory, version);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	      db.execSQL(DATABASE_CREATE);
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	          + newVersion + ", which will destroy all old data");

	      db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
	      onCreate(db);
	    }
	  }
}
