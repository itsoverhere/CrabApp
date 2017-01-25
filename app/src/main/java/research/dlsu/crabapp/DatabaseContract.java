package research.dlsu.crabapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by courtneyngo on 4/21/16.
 */
public class DatabaseContract {

    public static final String CONTENT_AUTHORITY = "research.dlsu.crabapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ONSITEUSER = "onsiteuser";
    public static final String PATH_CRAB = "crab";
    public static final String PATH_CRABUPDATE = "crabupdate";
    public static final String PATH_SCIENTIST = "scientist";

    public static class OnSiteUser implements BaseColumns {
        public static final String TABLE_NAME = "onsiteuser";

        public static final String SERIALNUMBER = "serialnumber";

//        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_FIRSTNAME = "firstname";
        public static final String COLUMN_LASTNAME = "lastname";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_FARM = "farm";
        public static final String COLUMN_CITY = "city";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRSTNAME + " TEXT NOT NULL, "
                + COLUMN_LASTNAME + " TEXT NOT NULL, "
                + COLUMN_USERNAME + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_FARM + " TEXT NOT NULL, "
                + COLUMN_CITY + " TEXT " + " ); ";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ONSITEUSER).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ONSITEUSER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ONSITEUSER;

        public static Uri buildOnSiteUserItemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static class OffSiteUser implements BaseColumns {
        public static final String TABLE_NAME = "offsiteuser";
//        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_FIRSTNAME = "firstname";
        public static final String COLUMN_LASTNAME = "lastname";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRSTNAME + " TEXT NOT NULL, "
                + COLUMN_LASTNAME + " TEXT NOT NULL, "
                + COLUMN_USERNAME + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL" + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCIENTIST).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCIENTIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCIENTIST;

        public static Uri buildScientistItemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static class Crab implements BaseColumns {
        public static final String EXTRA_ID = "phoneidcrab"; // only to be used for asynctask

        public static final String EXTRA_PHONEIDCRABSTRING = "idcrabstring";
        public static final String EXTRA_PHONEIDCRAB = "phoneidcrab";

        public static final String TABLE_NAME = "crab";
        public static final String TABLE_ALIAS = "c";
//        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_IDSCIENTIST = "idoffsiteuser";
        public static final String COLUMN_IDONSITEUSER = "idonsiteuser";

        public static final String COLUMN_TAG = "tag"; // for various remarks?
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_FARM = "farm";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_RESULT = "result";

        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

        public static final String COLUMN_SERVERIDCRAB = "serveridcrab";

        // could be changed to be NOT SENT / PENDING / RESULT
        public static final String STATUS_ONGOING = "ongoing";
        public static final String STATUS_DONE = "done";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

                + COLUMN_IDONSITEUSER + " INTEGER, " // NOT NULL, "
                + COLUMN_IDSCIENTIST + " INTEGER, "

                + COLUMN_SERVERIDCRAB + " INTEGER DEFAULT -1, "

                + COLUMN_TAG + " TEXT, "
                + COLUMN_WEIGHT + " REAL DEFAULT 0.0, "// NOT NULL, "

                + COLUMN_STATUS + " TEXT NOT NULL DEFAULT '" + Crab.STATUS_ONGOING + "', "
                + COLUMN_RESULT + " TEXT DEFAULT '',"

                + COLUMN_LATITUDE + " REAL DEFAULT 0, " // NOT NULL, "
                + COLUMN_LONGITUDE + " REAL DEFAULT 0," // NOT NULL, "

                + COLUMN_FARM + " TEXT NOT NULL, "
                + COLUMN_CITY + " TEXT NOT NULL" + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CRAB).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CRAB;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CRAB;

        public static Uri buildCrabItemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getCrabIdAsSegment(Uri uri, int segmentIndex){
            return Long.parseLong(uri.getPathSegments().get(segmentIndex));
        }
    }

    public static class CrabUpdate implements BaseColumns {
        public static final String EXTRA_ID = "idcrabupdate"; // only to be used for asynctask
        public static final String EXTRA_IMAGE = "image";
        public static final String EXTRA_SERVERIDCRAB = "serveridcrab";

        public static final String PHONEIDCRABUPDATE = "phoneidcrabupdate";

        public static final String TABLE_NAME = "crabupdate";
        public static final String TABLE_ALIAS = "cu";

//        public static final String COLUMN_ID = "_id";
//        public static final String COLUMN_IDCRAB = "idcrab";

        public static final String COLUMN_SERVERIDCRABUPDATE = "serveridcrabupdate";

        public static final String COLUMN_PATH = "path"; // image path

//        public static final String COLUMN_REMARKS = "remarks";
        public static final String COLUMN_RESULT = "result";
        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_CRABTYPE = "crabtype";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

//                + COLUMN_IDONSITEUSER + " INTEGER NOT NULL, "
//                + COLUMN_IDCRAB + " INTEGER NOT NULL, "
//                  + COLUMN_IDSCIENTIST + " INTEGER, "

                + COLUMN_SERVERIDCRABUPDATE + " INTEGER DEFAULT -1, "

                + COLUMN_PATH + " TEXT NOT NULL, "

//                + COLUMN_REMARKS + " REAL, "
//                + COLUMN_RESULT + " REAL, "
                + COLUMN_DATE + " REAL, "

                + COLUMN_RESULT + " TEXT DEFAULT '',"
                + COLUMN_CRABTYPE + " TEXT NOT NULL "

//                + "FOREIGN KEY (" + COLUMN_IDONSITEUSER + ") REFERENCES " + OnSiteUser.TABLE_NAME + " (" + OnSiteUser._ID + "), "
//                + "FOREIGN KEY (" + COLUMN_IDCRAB + ") REFERENCES " + Crab.TABLE_NAME + " (" + Crab._ID + ")"
//                + "FOREIGN KEY (" + COLUMN_IDSCIENTIST + ") REFERENCES " + Scientist.TABLE_NAME + " (" + Scientist._ID + ")"

                + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CRABUPDATE).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CRABUPDATE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CRABUPDATE;

        public static Uri buildCrabUpdateItemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getCrabUpdateIdAsSegment(Uri uri, int segmentIndex){
            return Long.parseLong(uri.getPathSegments().get(segmentIndex));
        }
    }



}
