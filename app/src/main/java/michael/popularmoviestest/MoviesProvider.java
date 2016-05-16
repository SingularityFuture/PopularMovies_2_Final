package michael.popularmoviestest;

/**
 * Created by Michael on 5/11/2016.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class MoviesProvider extends ContentProvider {

    static final String PROVIDER_NAME = "michael.popularmoviestest.provider";
    static final String URL = "content://" + PROVIDER_NAME + "/movies";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String ID = "id";
    static final String TITLE = "title";
    static final String POSTER_PATH = "poster_path";
    static final String OVERVIEW = "overview";
    static final String VOTER_AVERAGE = "voter_average";
    static final String RELEASE_DATE = "release_date";
    static final String FAVORITE = "favorite";

    private static HashMap<String, String> MOVIES_PROJECTION_MAP;

    static final int MOVIES = 1;
    static final int MOVIES_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "movies", MOVIES);
        uriMatcher.addURI(PROVIDER_NAME, "movies/#", MOVIES_ID);
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "Movies";
    static final String MOVIE_TABLE_NAME = "movie_information_new";
    static final int DATABASE_VERSION = 2;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + MOVIE_TABLE_NAME +
                    " (id STRING PRIMARY KEY, title TEXT, poster_path TEXT, " +
                    "overview TEXT, voter_average TEXT, " +
                    "release_date TEXT, favorite INTEGER DEFAULT 0);";
    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MOVIE_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null) ? false : true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */

        long rowID = db.insert(MOVIE_TABLE_NAME, null, values);

        /**j
         * If record is added successfully
         */

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
             return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(MOVIE_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case MOVIES:
                qb.setProjectionMap(MOVIES_PROJECTION_MAP);
                break;

            case MOVIES_ID:
                qb.appendWhere(ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == "") {
/**
             * By default sort on titles.
             */

            sortOrder = TITLE;
        }
        Cursor c = qb.query(db, columns, selection, selectionArgs, null, null, sortOrder);


         /* register to watch a content URI for changes
         */

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case MOVIES:
                count = db.delete(MOVIE_TABLE_NAME, selection, selectionArgs);
                break;

            case MOVIES_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(MOVIE_TABLE_NAME, ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case MOVIES:
                count = db.update(MOVIE_TABLE_NAME, values, selection, selectionArgs);
                break;

            case MOVIES_ID:
                count = db.update(MOVIE_TABLE_NAME, values, ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
          /*
             * Get all student records*/

            case MOVIES:
                return "vnd.android.cursor.dir/vnd.example.students";

/*            *
             * Get a particular student*/

            case MOVIES_ID:
                return "vnd.android.cursor.item/vnd.example.students";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}