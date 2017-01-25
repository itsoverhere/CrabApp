package research.dlsu.crabapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DatabaseProvider extends ContentProvider {

    public static final int URIMATCHER_CODE_CRAB = 100;
    public static final int URIMATCHER_CODE_CRAB_ITEM = 101;
    public static final int URIMATCHER_CODE_CRABUPDATE_DIR = 200;
    public static final int URIMATCHER_CODE_CRAB_CRABUPDATE_ITEM = 201;
    public static final int URIMATCHER_CODE_CRABUPDATE = 300;

    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sqliteQueryBuilder;

    static{
        sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(DatabaseContract.CrabUpdate.TABLE_NAME
//                + " " + DatabaseContract.CrabUpdate.TABLE_ALIAS
//                        + " INNER JOIN "
//                        + DatabaseContract.Crab.TABLE_NAME  + " " + DatabaseContract.Crab.TABLE_ALIAS
//                        + " ON "
//                        + DatabaseContract.Crab.TABLE_ALIAS + "." + DatabaseContract.Crab._ID
//                        + " = "
//                        + DatabaseContract.CrabUpdate.TABLE_ALIAS + "." + DatabaseContract.CrabUpdate.COLUMN_IDCRAB
        );
    }

    DatabaseHelper databaseHelper;

    public DatabaseProvider() {
//        databaseHelper = new DatabaseHelper(getContext());
    }

    static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, DatabaseContract.PATH_CRAB, URIMATCHER_CODE_CRAB);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CRAB + "/#", URIMATCHER_CODE_CRAB_ITEM);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CRABUPDATE, URIMATCHER_CODE_CRABUPDATE);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CRABUPDATE + "/#", URIMATCHER_CODE_CRABUPDATE_DIR);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CRABUPDATE + "/#/#", URIMATCHER_CODE_CRAB_CRABUPDATE_ITEM);

        return uriMatcher;
    }

    public String getType(Uri uri){
        final int match = buildUriMatcher().match(uri);

        switch(match){
            case URIMATCHER_CODE_CRAB:
                return DatabaseContract.Crab.CONTENT_DIR_TYPE;
            case URIMATCHER_CODE_CRAB_ITEM:
                return DatabaseContract.Crab.CONTENT_ITEM_TYPE;
            case URIMATCHER_CODE_CRABUPDATE_DIR:
                return DatabaseContract.CrabUpdate.CONTENT_DIR_TYPE;
            case URIMATCHER_CODE_CRAB_CRABUPDATE_ITEM:
                return DatabaseContract.CrabUpdate.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }

    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch(mUriMatcher.match(uri)){
            case URIMATCHER_CODE_CRAB:
                retCursor = databaseHelper.getReadableDatabase().query(
                        DatabaseContract.Crab.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder
                );
                break;
            case URIMATCHER_CODE_CRAB_ITEM:
                retCursor = queryCrabItemDetails(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case URIMATCHER_CODE_CRABUPDATE_DIR:
                retCursor = queryCrabUpdateItem(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case URIMATCHER_CODE_CRAB_CRABUPDATE_ITEM:
                retCursor = queryCrabUpdateItem(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case URIMATCHER_CODE_CRABUPDATE:
                retCursor = databaseHelper.getReadableDatabase().query(
                        DatabaseContract.CrabUpdate.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("No query() operation found for URI : " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }

    public Cursor queryCrabUpdateItem(Uri uri, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder){
        long id_crabupdate = Long.parseLong(uri.getPathSegments().get(1)); // id of crabupdate
        // long id = Long.parseLong(uri.getPathSegments().get(2));
        Log.i("databaseprovider", "idcrabupdate is " + id_crabupdate);
        return sqliteQueryBuilder.query(
                databaseHelper.getReadableDatabase(),
                null,
                //DatabaseContract.CrabUpdate.TABLE_NAME + "." +
                DatabaseContract.CrabUpdate._ID + " =? ",
                new String[]{String.valueOf(id_crabupdate)},
                null,
                null,
                sortOrder
        );
    }

    public Cursor queryCrabItemDetails(Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder){
        long id = Long.parseLong(uri.getPathSegments().get(1));
        Cursor cursor = databaseHelper.getReadableDatabase().query(
                DatabaseContract.Crab.TABLE_NAME,
                projection,
                DatabaseContract.Crab._ID + " = ? ",
                new String[]{String.valueOf(id)},
                null,
                null,
                sortOrder
        );

        cursor.moveToFirst();

        return cursor;
    }

    /*
    public static final String[] queryCrabItemDetailsWithCrabUpdatesProjection = new String[]{
            DatabaseContract.Crab.TABLE_NAME + "." + DatabaseContract.Crab._ID,
            DatabaseContract.Crab.TABLE_NAME + "." + DatabaseContract.Crab.COLUMN_TAG,
            DatabaseContract.Crab.TABLE_NAME + "." + DatabaseContract.Crab.COLUMN_STATUS,
            DatabaseContract.Crab.TABLE_NAME + "." + DatabaseContract.Crab.COLUMN_CITY,
            DatabaseContract.Crab.TABLE_NAME + "." + DatabaseContract.Crab.COLUMN_FARM,

            DatabaseContract.CrabUpdate.TABLE_NAME + "." + DatabaseContract.CrabUpdate._ID

    };

    public Cursor queryCrabItemDetailsWithCrabUpdates(Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder){
        long id = Long.parseLong(uri.getPathSegments().get(1));
        return sqliteQueryBuilder.query(
                databaseHelper.getReadableDatabase(),

        )

        return databaseHelper.getReadableDatabase().query(
                sq,
                projection,
                DatabaseContract.Crab._ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                sortOrder
        );
    }
    */


    public Cursor queryUpdatesOfCrab(Uri uri, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder){
        long id = Long.parseLong(uri.getPathSegments().get(1)); // id of crab
//        return sqliteQueryBuilder.query(
//                databaseHelper.getReadableDatabase(),
//                projection,
//                DatabaseContract.Crab.TABLE_ALIAS + "." + DatabaseContract.Crab._ID + " =? ",
//                new String[]{String.valueOf(id)},
//                null,
//                null,
//                sortOrder
//        );

        String TABLES = DatabaseContract.CrabUpdate.TABLE_NAME;
//                + " " + DatabaseContract.CrabUpdate.TABLE_ALIAS
//                + " INNER JOIN "
//                + DatabaseContract.Crab.TABLE_NAME  + " " + DatabaseContract.Crab.TABLE_ALIAS
//                + " ON "
//                + DatabaseContract.Crab.TABLE_ALIAS + "." + DatabaseContract.Crab._ID
//                + " = "
//                + DatabaseContract.CrabUpdate.TABLE_ALIAS + "." + DatabaseContract.CrabUpdate.COLUMN_IDCRAB;

//        return databaseHelper.getReadableDatabase().query(
//                TABLES,
//                projection,
//                DatabaseContract.Crab.TABLE_ALIAS + "." + DatabaseContract.Crab._ID + " =? ",
//                new String[]{String.valueOf(id)},
//                null, null, null
//        );
        // id, date, path, idcrab, serveridcrabupdate

        String selectionString =
                DatabaseContract.CrabUpdate.TABLE_ALIAS + "." + DatabaseContract.CrabUpdate._ID
                    + " AS " + DatabaseContract.CrabUpdate._ID + ", " +
                DatabaseContract.CrabUpdate.TABLE_ALIAS + "." + DatabaseContract.CrabUpdate.COLUMN_DATE
                    + " AS " + DatabaseContract.CrabUpdate.COLUMN_DATE + ", " +
                DatabaseContract.CrabUpdate.TABLE_ALIAS + "." + DatabaseContract.CrabUpdate.COLUMN_PATH
                    + " AS " + DatabaseContract.CrabUpdate.COLUMN_PATH + ", " +
//                DatabaseContract.CrabUpdate.TABLE_ALIAS + "." + DatabaseContract.CrabUpdate.COLUMN_IDCRAB
//                    + " AS " + DatabaseContract.CrabUpdate.COLUMN_IDCRAB + ", " +
                DatabaseContract.CrabUpdate.TABLE_ALIAS + "." + DatabaseContract.CrabUpdate.COLUMN_SERVERIDCRABUPDATE
                    + " AS " + DatabaseContract.CrabUpdate.COLUMN_SERVERIDCRABUPDATE
                ;

        String sql = "SELECT "
                + selectionString
                + " FROM "
                + TABLES
                + " WHERE " + DatabaseContract.Crab.TABLE_ALIAS + "." + DatabaseContract.Crab._ID + " =?";

        Log.i("TAG", "QUERY : " + sql);
        Log.i("TAG", "QUERY id : " + id);

        return databaseHelper.getReadableDatabase().rawQuery(
            sql, new String[]{String.valueOf(id)}
        );
    }

//    public Cursor queryCrabUpdateItem(Uri uri, String[] projection, String selection,
//                                      String[] selectionArgs, String sortOrder){
//        long ic_crab = Long.parseLong(uri.getPathSegments().get(1)); // id of crab not used
//        long id = Long.parseLong(uri.getPathSegments().get(2)); // id of crabupdate
//        return sqliteQueryBuilder.query(
//                databaseHelper.getReadableDatabase(),
//                projection,
//                DatabaseContract.CrabUpdate.TABLE_NAME + "." + DatabaseContract.CrabUpdate._ID + " =? ",
//                new String[]{String.valueOf(id)},
//                null,
//                null,
//                sortOrder
//        );
//    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        long id;
        int affectedRows;
        switch(mUriMatcher.match(uri)){
            case URIMATCHER_CODE_CRAB_ITEM:
                id = DatabaseContract.Crab.getCrabIdAsSegment(uri, 1);
                affectedRows = databaseHelper.getWritableDatabase().delete(
                        DatabaseContract.Crab.TABLE_NAME,
                        DatabaseContract.Crab._ID + " =? ",
                        new String[]{String.valueOf(id)}
                );
                break;
            case URIMATCHER_CODE_CRAB_CRABUPDATE_ITEM:
                id = DatabaseContract.CrabUpdate.getCrabUpdateIdAsSegment(uri, 2);
                affectedRows = databaseHelper.getWritableDatabase().delete(
                        DatabaseContract.CrabUpdate.TABLE_NAME,
                        DatabaseContract.CrabUpdate._ID + " =? ",
                        new String[]{String.valueOf(id)}
                );
                break;
            default:
                throw new UnsupportedOperationException("No delete() operation found for URI : "+ uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return affectedRows;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        Uri returnUri;
        switch(mUriMatcher.match(uri)){
            case URIMATCHER_CODE_CRAB:
                id = databaseHelper.getWritableDatabase().insert(
                        DatabaseContract.Crab.TABLE_NAME,
                        null,
                        values
                );
                if(id > 0){
                    returnUri = DatabaseContract.Crab.buildCrabItemUri(id);
                }else{
                    throw new android.database.SQLException("Item wasn't inserted into the database properly");
                }
                break;
            case URIMATCHER_CODE_CRABUPDATE:
                id = databaseHelper.getWritableDatabase().insert(
                        DatabaseContract.CrabUpdate.TABLE_NAME,
                        null,
                        values
                );
                if(id > 0){
                    returnUri = DatabaseContract.CrabUpdate.buildCrabUpdateItemUri(id);
                }else{
                    throw new android.database.SQLException("Item wasn't inserted into the database properly");
                }
                break;
            default:
                throw new UnsupportedOperationException("No insert() operation for URI : " + uri);
        }

        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
