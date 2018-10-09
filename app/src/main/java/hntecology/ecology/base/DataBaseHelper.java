package hntecology.ecology.base;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import hntecology.ecology.model.Biotope_attribute;
import hntecology.ecology.model.GpsSet;

public class DataBaseHelper extends SQLiteOpenHelper {

    private final Context myContext;

    // The Android's default system path of your application database.
//    private static String DB_PATH = "/hntecology/ecology/database/";
    private static String DB_PATH = "/data/data/hntecology.ecology/databases/";

    private static String DB_NAME = "biotopeTest.db";

    private SQLiteDatabase myDataBase;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     *
     * @param context
     */
    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public SQLiteDatabase createDataBase() throws IOException {
        boolean dbExist = checkDataBase();

        if (dbExist) {
            // do nothing - database already exist
        } else {

            // By calling this method and empty database will be created into the default system path
            // of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

        try {
            String myPath = DB_PATH + DB_NAME;
            return SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            return false;
        }

        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        File f = new File(DB_PATH);
        if (!f.exists()) {
            f.mkdir();
        }

        // Open your local db as the input stream
//        InputStream myInput = myContext.getAssets().open(DB_NAME);
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void deleteDataBase() {
        File f = new File(DB_PATH + DB_NAME);
        if (f.exists()) {
            f.delete();
        }
    }

    public SQLiteDatabase openDataBase() throws SQLException {

        // Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        return myDataBase;

    }

    @Override
    public synchronized void close() {

        if (myDataBase != null) {
            myDataBase.close();
        }

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "create table if not exists ";
        query += "biotopeattribute ( id String PRIMARY KEY";
        query += ",PRJ_NAME	   String";
        query += ",INV_REGION	String";
        query += ",INV_PERSON	String";
        query += ",INV_DT	    String";
        query += ",INV_TM	    String";
        query += ",INV_INDEX	INTEGER";
        query += ",LU_GR_NUM	String";
        query += ",LU_TY_RATE	Float";
        query += ",STAND_H	    Float";
        query += ",LC_GR_NUM	String";
        query += ",LC_TY	    String";
        query += ",TY_MARK	    String";
        query += ",GV_RATE	    Float";
        query += ",GV_STRUCT	String";
        query += ",DIS_RET	    String";
        query += ",RESTOR_POT	String";
        query += ",COMP_INTA	String";
        query += ",VP_INTA	    String";
        query += ",IMP_FORM	String";
        query += ",BREA_DIA	String";
        query += ",FIN_EST	    String";
        query += ",TRE_SPEC	String";
        query += ",TRE_FAMI	String";
        query += ",TRE_SCIEN	String";
        query += ",TRE_H	    Float";
        query += ",TRE_BREA	Float";
        query += ",TRE_COVE	Float";
        query += ",STRE_SPEC	String";
        query += ",STRE_FAMI	String";
        query += ",STRE_SCIEN	String";
        query += ",STRE_H	    Float";
        query += ",STRE_BREA	Float";
        query += ",STRE_COVE	Float";
        query += ",SHR_SPEC	String";
        query += ",SHR_FAMI	String";
        query += ",SHR_SCIEN	String";
        query += ",SHR_H	    Float";
        query += ",STR_COVE	Float";
        query += ",HER_SPEC	String";
        query += ",HER_FAMI	String";
        query += ",HER_SCIEN	String";
        query += ",HER_H	    Float";
        query += ",HER_COVE	Float";
        query += ",PIC_FOLDER	String";
        query += ",WILD_ANI	String";
        query += ",BIOTOP_POT	String";
        query += ",UNUS_NOTE	String";
        query += ",GPS_LAT	    Float";
        query += ",GPS_LON	    Float";
        query += ",NEED_CONF	String";
        query += ",CONF_MOD	String";
        query += ");";
        db.execSQL(query);

//        query = "create table if not exists ";
//        query += "birds ( no String";
//        query += ", taxon String";
//        query += ", zoological String";
//        query += ", name_kr String";
//        query += ", author String";
//        query += ", year String";
//        query += ", Phylum_name String";
//        query += ", Phylum_name_kr String";
//        query += ", Class_name String";
//        query += ", Class_name_kr String";
//        query += ", Order_name String";
//        query += ", Order_name_kr String";
//        query += ", Family_name String";
//        query += ", Family_name_kr String";
//        query += ", Genus_name String";
//        query += ", Genus_name_kr String";
//        query += ", Species_name String";
//        query += ", Species_name String";
//        query += ");";
//        db.execSQL(query);
//
//        query = "create table if not exists ";
//        query += "member_likes ( _id INTEGER PRIMARY KEY AUTOINCREMENT";
//        query += ", board_id INTEGER";
//        query += ");";
//        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

//        db.execSQL("drop table if exists members");
//
//        String query = "create table if not exists ";
//        query += "members ( _id INTEGER PRIMARY KEY AUTOINCREMENT";
//        query += ", member_id String";
//        query += ", reading INTEGER";
//        query += ");";
//        db.execSQL(query);
//
//        db.execSQL("drop table if exists member_likes");
//
//        query = "create table if not exists ";
//        query += "member_likes ( _id INTEGER PRIMARY KEY AUTOINCREMENT";
//        query += ", board_id INTEGER";
//        query += ");";
//        db.execSQL(query);
    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.
    public void insertbiotope_attribute(Biotope_attribute biotope_attribute) {
        //37 column
        String query = "INSERT INTO biotopeAttribute";
        query += "(id,PRJ_NAME,INV_REGION,INV_PERSON,INV_DT,INV_TM,INV_INDEX,LU_GR_NUM,LU_TY_RATE,STAND_H,LC_GR_NUM,LC_TY,TY_MARK,GV_RATE,GV_STRUCT,DIS_RET,RESTOR_POT,COMP_INTA";
        query += ",VP_INTA,IMP_FORM,BREA_DIA,FIN_EST,TRE_SPEC,TRE_FAMI,TRE_SCIEN,TRE_H,TRE_BREA,TRE_COVE,STRE_SPEC,STRE_FAMI,STRE_SCIEN,STRE_H,STRE_BREA,STRE_COVE,SHR_SPEC,SHR_FAMI";
        query += ",SHR_SCIEN,SHR_H,STR_COVE,HER_SPEC,HER_FAMI,HER_SCIEN,HER_H,HER_COVE,PIC_FOLDER,WILD_ANI,BIOTOP_POT,UNUS_NOTE,GPS_LAT,GPS_LON,NEED_CONF,CONF_MOD)";


        query += " values (";
        query += " '" + biotope_attribute.getId() + "'";
        query += ", '" + biotope_attribute.getPRJ_NAME() + "'";
        query += ", '" + biotope_attribute.getINV_REGION() + "'";
        query += ", '" + biotope_attribute.getINV_PERSON() + "'";
        query += ", '" + biotope_attribute.getINV_DT() + "'";
        query += ", '" + biotope_attribute.getINV_TM() + "'";
        query += ", (SELECT   strftime(\"%Y%m%d\",'now','localtime') || substr('00000' || cast(IFNULL(MAX(substr(INV_INDEX ,9,15)),0)+1 as text), -5, 5) FROM biotopeAttribute)";
        query += ", '" + biotope_attribute.getLU_GR_NUM() + "'";
        query += ", " + biotope_attribute.getLU_TY_RATE() + "";
        query += ", " + biotope_attribute.getSTAND_H() + "";
        query += ", '" + biotope_attribute.getLC_GR_NUM() + "'";
        query += ", '" + biotope_attribute.getLC_TY() + "'";
        query += ", '" + biotope_attribute.getTY_MARK() + "'";
        query += ", " + biotope_attribute.getGV_RATE() + "";
        query += ", '" + biotope_attribute.getGV_STRUCT() + "'";
        query += ", '" + biotope_attribute.getDIS_RET() + "'";
        query += ", '" + biotope_attribute.getRESTOR_POT() + "'";
        query += ", '" + biotope_attribute.getCOMP_INTA() + "'";
        query += ", '" + biotope_attribute.getVP_INTA() + "'";
        query += ", '" + biotope_attribute.getIMP_FORM() + "'";
        query += ", '" + biotope_attribute.getBREA_DIA() + "'";
        query += ", '" + biotope_attribute.getFIN_EST() + "'";
        query += ", '" + biotope_attribute.getTRE_SPEC() + "'";
        query += ", '" + biotope_attribute.getTRE_FAMI() + "'";
        query += ", '" + biotope_attribute.getTRE_SCIEN() + "'";
        query += ", " + biotope_attribute.getTRE_H() + "";
        query += ", " + biotope_attribute.getTRE_BREA() + "";
        query += ", " + biotope_attribute.getTRE_COVE() + "";
        query += ", '" + biotope_attribute.getSTRE_SPEC() + "'";
        query += ", '" + biotope_attribute.getSTRE_FAMI() + "'";
        query += ", '" + biotope_attribute.getSTRE_SCIEN() + "'";
        query += ", " + biotope_attribute.getSTRE_H() + "";
        query += ", " + biotope_attribute.getSTRE_BREA() + "";
        query += ", " + biotope_attribute.getSTRE_COVE() + "";
        query += ", '" + biotope_attribute.getSHR_SPEC() + "'";
        query += ", '" + biotope_attribute.getSHR_FAMI() + "'";
        query += ", '" + biotope_attribute.getSHR_SCIEN() + "'";
        query += ", " + biotope_attribute.getSHR_H() + "";
        query += ", " + biotope_attribute.getSTR_COVE() + "";
        query += ", '" + biotope_attribute.getHER_SPEC() + "'";
        query += ", '" + biotope_attribute.getHER_FAMI() + "'";
        query += ", '" + biotope_attribute.getHER_SCIEN() + "'";
        query += ", " + biotope_attribute.getHER_H() + "";
        query += ", " + biotope_attribute.getHER_COVE() + "";
        query += ", '" + biotope_attribute.getPIC_FOLDER() + "'";
        query += ", '" + biotope_attribute.getWILD_ANI() + "'";
        query += ", '" + biotope_attribute.getBIOTOP_POT() + "'";
        query += ", '" + biotope_attribute.getUNUS_NOTE() + "'";
        query += ", " + biotope_attribute.getGPS_LAT() + "";
        query += ", " + biotope_attribute.getGPS_LON() + "";
        query += ", '" + biotope_attribute.getNEED_CONF() + "'";
        query += ", '" + biotope_attribute.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }
//
//    public void backupMember(Member member) {
//        String query = "INSERT INTO members ( member_id, reading ) ";
//        query += " values (";
//        query += "'" + member.getMember_id() + "'";
//        query += ", " + member.getReading();
//        query += " ); ";
//
//        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL(query);
//        db.close();
//    }

    public void deletebiotope_attribute(Biotope_attribute biotope_attribute) {
        String query = "DELETE FROM biotopeAttribute WHERE id = '" + biotope_attribute.getId() + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void updatebiotope_attribute(Biotope_attribute biotope_attribute) {

        String query = "UPDATE biotopeAttribute SET  " +
                "INV_REGION='" + biotope_attribute.getINV_REGION() + "'"
                + ",INV_PERSON='" + biotope_attribute.getINV_PERSON() + "'"
                + ",INV_DT='" + biotope_attribute.getINV_DT() + "'"
                + ",INV_TM='" + biotope_attribute.getINV_TM() + "'"
                + ",LU_GR_NUM='" + biotope_attribute.getLU_GR_NUM() + "'"
                + ",LU_TY_RATE=" + biotope_attribute.getLU_TY_RATE() + ""
                + ",STAND_H=" + biotope_attribute.getSTAND_H() + ""
                + ",LC_GR_NUM='" + biotope_attribute.getLC_GR_NUM() + "'"
                + ",LC_TY='" + biotope_attribute.getLC_TY() + "'"
                + ",TY_MARK='" + biotope_attribute.getTY_MARK() + "'"
                + ",GV_RATE=" + biotope_attribute.getGV_RATE() + ""
                + ",GV_STRUCT='" + biotope_attribute.getGV_STRUCT() + "'"
                + ",DIS_RET='" + biotope_attribute.getDIS_RET() + "'"
                + ",RESTOR_POT='" + biotope_attribute.getRESTOR_POT() + "'"
                + ",COMP_INTA='" + biotope_attribute.getCOMP_INTA() + "'"
                + ",VP_INTA='" + biotope_attribute.getVP_INTA() + "'"
                + ",IMP_FORM='" + biotope_attribute.getIMP_FORM() + "'"
                + ",BREA_DIA='" + biotope_attribute.getBREA_DIA() + "'"
                + ",FIN_EST='" + biotope_attribute.getFIN_EST() + "'"
                + ",TRE_SPEC='" + biotope_attribute.getTRE_SPEC() + "'"
                + ",TRE_FAMI='" + biotope_attribute.getTRE_FAMI() + "'"
                + ",TRE_SCIEN='" + biotope_attribute.getTRE_SCIEN() + "'"
                + ",TRE_H=" + biotope_attribute.getTRE_H() + ""
                + ",TRE_BREA=" + biotope_attribute.getTRE_BREA() + ""
                + ",TRE_COVE=" + biotope_attribute.getTRE_COVE() + ""
                + ",STRE_SPEC='" + biotope_attribute.getSTRE_SPEC() + "'"
                + ",STRE_FAMI='" + biotope_attribute.getSTRE_FAMI() + "'"
                + ",STRE_SCIEN='" + biotope_attribute.getSTRE_SCIEN() + "'"
                + ",STRE_H=" + biotope_attribute.getSTRE_H() + ""
                + ",STRE_BREA=" + biotope_attribute.getSTRE_BREA() + ""
                + ",STRE_COVE=" + biotope_attribute.getSTRE_COVE() + ""
                + ",SHR_SPEC='" + biotope_attribute.getSHR_SPEC() + "'"
                + ",SHR_FAMI='" + biotope_attribute.getSHR_FAMI() + "'"
                + ",SHR_SCIEN='" + biotope_attribute.getSHR_SCIEN() + "'"
                + ",SHR_H=" + biotope_attribute.getSHR_H() + ""
                + ",STR_COVE=" + biotope_attribute.getSTR_COVE() + ""
                + ",HER_SPEC='" + biotope_attribute.getHER_SPEC() + "'"
                + ",HER_FAMI='" + biotope_attribute.getHER_FAMI() + "'"
                + ",HER_SCIEN='" + biotope_attribute.getHER_SCIEN() + "'"
                + ",HER_H=" + biotope_attribute.getHER_H() + ""
                + ",HER_COVE=" + biotope_attribute.getHER_COVE() + ""
                + ",PIC_FOLDER='" + biotope_attribute.getPIC_FOLDER() + "'"
                + ",WILD_ANI='" + biotope_attribute.getWILD_ANI() + "'"
                + ",BIOTOP_POT='" + biotope_attribute.getBIOTOP_POT() + "'"
                + ",UNUS_NOTE='" + biotope_attribute.getUNUS_NOTE() + "'"
                + ",GPS_LAT=" + biotope_attribute.getGPS_LAT() + ""
                + ",GPS_LON=" + biotope_attribute.getGPS_LON() + ""
                + ",NEED_CONF='" + biotope_attribute.getNEED_CONF() + "'"
                + ",CONF_MOD='" + biotope_attribute.getCONF_MOD() + "'" +
                "where id = '" + biotope_attribute.getId() + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void insertGpsSet(GpsSet gpsset) {
        //37 column
        String query = "INSERT INTO gps_set";
        query += " values (";
        query += " "+gpsset.getId()+"";
        query += ", '"+gpsset.getLatitude()+"'";
        query += ", '"+gpsset.getLongitude()+"'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public int birdsNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM ,9,15)),0)+1 ,-15, 15) FROM birdsAttribute";

        int num = 0;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                num = cursor.getInt(0);
            }
        }
        cursor.close();
        return num;
    }


}
