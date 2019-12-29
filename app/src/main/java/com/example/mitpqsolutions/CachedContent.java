package com.example.mitpqsolutions;

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
import java.util.Random;

import java.net.URI;
import java.util.HashMap;

public class CachedContent extends ContentProvider
{
  static final String PROVIDER_NAME = "com.example.mitpqsolutions.CachedContent";
  static final String URL = "content://"+PROVIDER_NAME + "/solutions";
  static final String installInfoURL = "content://"+PROVIDER_NAME + "/installInfo";
  static final Uri CONTENT_URI = Uri.parse(URL);
  static final Uri INSTALLINFO_URI = Uri.parse(installInfoURL);
  //astatic final double install_key = new Random().nextInt(10000);

  //defining field names
    static final String QID = "qid";
    static final String COURSE = "courseID";
    static final String QDESC = "qdesc";
    static final String QANS = "qans";
    static final String INSTALLID = "INSID";
    static final String IMAGEURL = "imageurl";

    // declaring field aliases
    static HashMap<String,String> projection_map = new HashMap<>();

    static final UriMatcher urimatcher;
    static
    {
     urimatcher = new UriMatcher(UriMatcher.NO_MATCH);
     urimatcher.addURI(PROVIDER_NAME,"solutions",1);
     urimatcher.addURI(PROVIDER_NAME,"solutions/#",2);
     urimatcher.addURI(PROVIDER_NAME,"installInfo",3);
    }
    //initializing database parameters
    private SQLiteDatabase dbase;
    static final String DATABASE_NAME = "cached";
    static final String TABLE_NAME = "pqsolutions";
    static final String TABLE2 = "installData";
    static final String creationQuery = "create table "+TABLE_NAME+" ( qid varchar(10) primary key ,courseID varchar(20),qdesc text,qans text,imageurl text)";
    static final String createInstallDataTable = "create table "+TABLE2+" (INSID varchar(50))";
    static final String insertKey = "insert into "+TABLE2+" values ('"+String.valueOf(new Random().nextInt(10000))+"')";
    //Defining the database helper class
    static class DatabaseHelper extends SQLiteOpenHelper
    {
      DatabaseHelper(Context context)
      {
          super(context,DATABASE_NAME,null,1);
      }
      @Override
        public void onCreate(SQLiteDatabase db)
      {
       db.execSQL(creationQuery);
       db.execSQL(createInstallDataTable);
       db.execSQL(insertKey);
      }
      @Override
        public void onUpgrade(SQLiteDatabase db,int oldver, int newver)
      {
         db.execSQL("drop table if exists "+ TABLE_NAME);
          db.execSQL("drop table if exists "+ TABLE2);
         onCreate(db);
      }

    }
    @Override
    public boolean onCreate()
    {
        try {
            Context context = getContext();
            DatabaseHelper dbHelper = new DatabaseHelper(context);

            /**
             * Create a write able database which will trigger its
             * creation if it doesn't already exist.
             */

            dbase = dbHelper.getWritableDatabase();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return (dbase == null)? false:true;
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) throws SQLException
    {
        long rowID = dbase.insert(TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);

    }
    @Override
    public Cursor query(Uri content_uri, String[] projection, String selection, String[] selArgs, String sortOrder)
    {
        boolean solutions = true;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        //qb.setTables(TABLE_NAME);

        switch (urimatcher.match(content_uri)) {
            case 1:
                qb.setTables(TABLE_NAME);
                //qb.setProjectionMap(projection_map);
                break;

            case 2:
                qb.setTables(TABLE_NAME);
                qb.appendWhere(  QID + "=" + content_uri.getPathSegments().get(1));
                break;
            case 3:
                qb.setTables(TABLE2);
                solutions = false;
                break;

            default:
        }

        if (sortOrder == null || sortOrder == ""){

            if(solutions)
            sortOrder = QID;
            else sortOrder = INSTALLID;
        }

        Cursor c = qb.query(dbase,projection,selection,
                selArgs,null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), content_uri);
        return c;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) throws IllegalArgumentException {
        int count = 0;
        switch (urimatcher.match(uri)){
            case 1:
                count = dbase.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case 2:
                String id = uri.getPathSegments().get(1);
                count = dbase.delete( TABLE_NAME, QID +  " = " + id +
                                (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) throws IllegalArgumentException {
        int count = 0;
        switch (urimatcher.match(uri)) {
            case 1:
                count = dbase.update(TABLE_NAME, values, selection, selectionArgs);
                break;

            case 2:
                count = dbase.update(TABLE_NAME, values, QID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    @Override
    public String getType(Uri uri) {
        switch (urimatcher.match(uri)){
            /**
             * Get all student records
             */
            case 1:
                return "vnd.android.cursor.dir/vnd.example.solutions";
            /**
             * Get a particular student
             */
            case 2:
                return "vnd.android.cursor.item/vnd.example.solutions";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
