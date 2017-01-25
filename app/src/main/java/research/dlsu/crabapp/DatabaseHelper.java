package research.dlsu.crabapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by courtneyngo on 4/21/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public final static String SCHEMA = "crab";
    public static final int VERSION = 3;

    SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();

    public DatabaseHelper(Context context) {
        super(context, SCHEMA, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(DatabaseContract.OnSiteUser.CREATE_TABLE);
//        db.execSQL(DatabaseContract.Scientist.CREATE_TABLE);
//        db.execSQL(DatabaseContract.Crab.CREATE_TABLE);
        db.execSQL(DatabaseContract.CrabUpdate.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL(DatabaseContract.OnSiteUser.DELETE_TABLE);
//        db.execSQL(DatabaseContract.Scientist.DELETE_TABLE);
        //db.execSQL(DatabaseContract.Crab.DELETE_TABLE);
        db.execSQL(DatabaseContract.CrabUpdate.DELETE_TABLE);

        onCreate(db);
    }

    // CUSTOM DB OPERATIONS

//    public int getNumberOfUpdatesOfCrab(long id){
//        Cursor cursor = getReadableDatabase().query(
//                DatabaseContract.CrabUpdate.TABLE_NAME,
//                null,
//                DatabaseContract.CrabUpdate.COLUMN_IDCRAB + "=?",
//                new String[]{String.valueOf(id)},
//                null, null, null
//        );
//
//        if(cursor.moveToFirst()){
//            return cursor.getCount();
//        }else{
//            return 0;
//        }
//    }

//    public Date getLastCrabUpdateOfCrab(long id){
//        Cursor cursor = getReadableDatabase().query(
//                DatabaseContract.CrabUpdate.TABLE_NAME,
//                null,
//                DatabaseContract.CrabUpdate.COLUMN_IDCRAB + "=?",
//                new String[]{String.valueOf(id)},
//                null, null,
//                DatabaseContract.CrabUpdate.COLUMN_DATE + " DESC"
//        );
//
//        if(cursor.moveToFirst()){
//            return new Date(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_DATE)));
//        }else{
//            return new Date(0);
//        }
//    }

//    public int getServerIdCrabWithIdCrabUpdate(int idCrabupdate){
//
//        int serverIdCrab = -1;
//
//        sqliteQueryBuilder.setTables(DatabaseContract.CrabUpdate.TABLE_NAME
//            + " INNER JOIN "
//            + DatabaseContract.Crab.TABLE_NAME
//            + " ON "
//            + DatabaseContract.Crab.TABLE_NAME + "." + DatabaseContract.Crab._ID
//            + " = "
//            + DatabaseContract.CrabUpdate.TABLE_NAME + "." + DatabaseContract.CrabUpdate.COLUMN_IDCRAB
//        );
//
//        String[] projection = new String[]{
//            DatabaseContract.Crab.TABLE_NAME + "." + DatabaseContract.Crab.COLUMN_SERVERIDCRAB
//        };
//
//        String selection =
//            DatabaseContract.CrabUpdate.TABLE_NAME + "." + DatabaseContract.CrabUpdate.COLUMN_IDCRAB
//            + "=?";
//
//        String[] selectionArgs = new String[]{
//            String.valueOf(idCrabupdate)
//        };
//
//        Cursor cursor = sqliteQueryBuilder.query(
//                getReadableDatabase(),
//                projection,
//                selection,
//                selectionArgs,
//                null, null, null
//        );
//
//        if(cursor.moveToFirst()){
//            serverIdCrab = cursor.getInt(0); // column index is zero-based
//        }
//
//        return serverIdCrab;
//    }
//
//    public int getServerIdCrabWithIdCrab(long idCrab){
//
//        int serverIdCrab = -1;
//
//        Cursor cursor = getReadableDatabase().query(
//                DatabaseContract.Crab.TABLE_NAME,
//                new String[]{DatabaseContract.Crab.COLUMN_SERVERIDCRAB},
//                DatabaseContract.Crab._ID + "=? ",
//                new String[]{String.valueOf(idCrab)},
//                null, null, null
//        );
//
//        if(cursor.moveToFirst()){
//            serverIdCrab = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_SERVERIDCRAB)); // column index is zero-based
//        }
//
//        return serverIdCrab;
//    }
//
//    public void updateServerIdCrab(long idCrab, long serverIdCrab){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(DatabaseContract.Crab.COLUMN_SERVERIDCRAB, serverIdCrab);
//
//        int i = getWritableDatabase().update(
//                DatabaseContract.Crab.TABLE_NAME,
//                contentValues,
//                DatabaseContract.Crab._ID + "=?",
//                new String[]{String.valueOf(idCrab)}
//        );
//
//        Log.i("TAG", "update server id idCrab is " + idCrab + " serveridcrab is " + serverIdCrab + " i is " + i);
//    }

    public boolean updateServerIdCrabUpdate(long idCrabUpdate, long serverIdCrabUpdate){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.CrabUpdate.COLUMN_SERVERIDCRABUPDATE, serverIdCrabUpdate);

        int i = getWritableDatabase().update(
                DatabaseContract.CrabUpdate.TABLE_NAME,
                contentValues,
                DatabaseContract.CrabUpdate._ID + "=?",
                new String[]{String.valueOf(idCrabUpdate)}
        );

        Log.i("TAG", "update server idupdate idCrabupdate is " + idCrabUpdate + " serveridcrabupdate is " + serverIdCrabUpdate + " i is " + i);

        if(i != 1){
            return false;
        }else{
            return true;
        }
    }

    public String getCrabUpdatesWithoutResults(){
        String idString = "(";

//        Cursor cursor = getReadableDatabase().query(
//                DatabaseContract.Crab.TABLE_NAME,
//                null,
////                DatabaseContract.Crab.COLUMN_RESULT + " = '' OR "
////                        + DatabaseContract.Crab.COLUMN_RESULT + " = NULL",
//                null,
//                null,
//                null, null, null
//        );

        Cursor cursor = getReadableDatabase().query(
                DatabaseContract.CrabUpdate.TABLE_NAME,
                null,
                DatabaseContract.CrabUpdate.COLUMN_RESULT + " is ''", // OR "
//                        + DatabaseContract.Crab.COLUMN_RESULT + " is NULL",
                null, null, null, null
        );

        if(cursor.moveToFirst()){
            idString += cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab._ID));
            while(cursor.moveToNext()){
                idString += ", " + cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab._ID));
            }
        }else{
            return null;
        }

        return idString +")";
    }

    public void updateCrabUpdateResults(ArrayList<CrabUpdate> crabUpdatesList){
        SQLiteDatabase db = getWritableDatabase();
        //db.beginTransaction();
        int returnCount = 0;
        for(int i = 0; i < crabUpdatesList.size(); i++ ){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseContract.CrabUpdate.COLUMN_RESULT, crabUpdatesList.get(i).getResult());
            int rows = db.update(
                    DatabaseContract.CrabUpdate.TABLE_NAME,
                    contentValues,
                    DatabaseContract.CrabUpdate._ID + "=?",
                    new String[]{String.valueOf(crabUpdatesList.get(i).getId())}
            );

            returnCount+=rows;
        }
        //db.endTransaction();

        Log.i("TAG", "affected rows : " + returnCount);
    }

    public ContentValues[] crabListToContentValues(ArrayList<Crab> crabsList){
        ContentValues[] contentValuesList = new ContentValues[crabsList.size()];

        for(int i = 0; i < crabsList.size(); i++ ){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseContract.Crab.COLUMN_RESULT, crabsList.get(i).getResult());
            contentValuesList[i] = contentValues;
        }

        return contentValuesList;
    }

}
