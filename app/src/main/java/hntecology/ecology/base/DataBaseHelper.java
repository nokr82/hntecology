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
import java.lang.reflect.Member;

import hntecology.ecology.model.Biotope_attribute;

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
     * */
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
     * */
    private void copyDataBase() throws IOException {

        File f = new File(DB_PATH);
        if(!f.exists()) {
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
        query += ", INVES_REGION String";
        query += ", INVESTIGATOR String";
        query += ", INVES_DATETIME String";
        query += ", INVES_INDEX INTEGER";
        query += ", LUM_GROUP_NUM String";
        query += ", LUM_TYPE_RATE Float";
        query += ", STANDARD_HEIGHT Float";
        query += ", LCM_GROUP_NUM String";
        query += ", LCM_TYPE String";
        query += ", TYPE_MARK String";
        query += ", GV_RATE Float";
        query += ", GV_STRUCTURE String";
        query += ", DIST_RETURN String";
        query += ", RESTORE_POT String";
        query += ", COMP_INTACT String";
        query += ", VP_INTACT String";
        query += ", IMP_FORM String";
        query += ", BREAST_DIA String";
        query += ", FINAL_EST String";
        query += ", TREE_SPECIES String";
        query += ", TREE_HEIGHT Float";
        query += ", TREE_BREAST Float";
        query += ", TREE_COVE Float";
        query += ", SUB_TREE_SPEC String";
        query += ", SUB_TREE_HEIGHT Float";
        query += ", SUB_TREE_BREAST Float";
        query += ", SUB_TREE_COVER Float";
        query += ", SHRUB_SPECIES String";
        query += ", SHRUB_HEIGHT Float";
        query += ", SHRUB_COVER Float";
        query += ", HERB_SPECIES String";
        query += ", HERB_HEIGHT String";
        query += ", HERB_COVER String";
        query += ", PICTURE_FOLDER String";
        query += ", WILD_ANI String";
        query += ", REP_BIOTOP_POT String";
        query += ", UNUSUAL_NOTE String";
        query += ", POINT_GPS String";
        query += ");";
        db.execSQL(query);
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
        String query = "INSERT INTO biotopeAttribute (id,INVES_REGION,INVESTIGATOR,INVES_DATETIME,INVES_INDEX,LUM_GROUP_NUM,LUM_TYPE_RATE,STANDARD_HEIGHT,LCM_GROUP_NUM,"
                +"LCM_TYPE,TYPE_MARK,GV_RATE,GV_STRUCTURE,DIST_RETURN,RESTORE_POT,COMP_INTACT,VP_INTACT,IMP_FORM,BREAST_DIA,FINAL_EST,TREE_SPECIES,TREE_HEIGHT,TREE_BREAST,"
                +"TREE_COVE,SUB_TREE_SPEC,SUB_TREE_HEIGHT,SUB_TREE_BREAST,SUB_TREE_COVER,SHRUB_SPECIES,SHRUB_HEIGHT,SHRUB_COVER,HERB_SPECIES,HERB_HEIGHT,HERB_COVER,"
                +"PICTURE_FOLDER,WILD_ANI,REP_BIOTOP_POT,UNUSUAL_NOTE,POINT_GPS)";
        query += " values (";
        query += " '"+biotope_attribute.getId()+"'";
        query += ", '"+biotope_attribute.getINVES_REGION()+"'";
        query += ", '"+biotope_attribute.getINVESTIGATOR()+"'";
        query += ", '"+biotope_attribute.getINVES_DATETIME()+"'";
        query += ", "+biotope_attribute.getINVES_INDEX()+"";
        query += ", '"+biotope_attribute.getLUM_GROUP_NUM()+"'";
        query += ", "+biotope_attribute.getLUM_TYPE_RATE()+"";
        query += ", "+biotope_attribute.getSTANDARD_HEIGHT()+"";
        query += ", '"+biotope_attribute.getLCM_GROUP_NUM()+"'";
        query += ", '"+biotope_attribute.getLCM_TYPE()+"'";
        query += ", '"+biotope_attribute.getTYPE_MARK()+"'";
        query += ", "+biotope_attribute.getGV_RATE()+"";
        query += ", '"+biotope_attribute.getGV_STRUCTURE()+"'";
        query += ", '"+biotope_attribute.getDIST_RETURN()+"'";
        query += ", '"+biotope_attribute.getRESTORE_POT()+"'";
        query += ", '"+biotope_attribute.getCOMP_INTACT()+"'";
        query += ", '"+biotope_attribute.getVP_INTACT()+"'";
        query += ", '"+biotope_attribute.getIMP_FORM()+"'";
        query += ", '"+biotope_attribute.getBREAST_DIA()+"'";
        query += ", '"+biotope_attribute.getFINAL_EST()+"'";
        query += ", '"+biotope_attribute.getTREE_SPECIES()+"'";
        query += ", "+biotope_attribute.getTREE_HEIGHT()+"";
        query += ", "+biotope_attribute.getTREE_BREAST()+"";
        query += ", "+biotope_attribute.getTREE_COVE()+"";
        query += ", '"+biotope_attribute.getSUB_TREE_SPEC()+"'";
        query += ", "+biotope_attribute.getSUB_TREE_HEIGHT()+"";
        query += ", "+biotope_attribute.getSUB_TREE_BREAST()+"";
        query += ", "+biotope_attribute.getSUB_TREE_COVER()+"";
        query += ", '"+biotope_attribute.getSHRUB_SPECIES()+"'";
        query += ", '"+biotope_attribute.getSHRUB_HEIGHT()+"'";
        query += ", '"+biotope_attribute.getSHRUB_COVER()+"'";
        query += ", '"+biotope_attribute.getHERB_SPECIES()+"'";
        query += ", "+biotope_attribute.getHERB_HEIGHT()+"";
        query += ", "+biotope_attribute.getHERB_COVER()+"";
        query += ", '"+biotope_attribute.getPICTURE_FOLDER()+"'";
        query += ", '"+biotope_attribute.getWILD_ANI()+"'";
        query += ", '"+biotope_attribute.getREP_BIOTOP_POT()+"'";
        query += ", '"+biotope_attribute.getUNUSUAL_NOTE()+"'";
        query += ", '"+biotope_attribute.getPoint_gps()+"'";
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
        String query = "DELETE FROM biotopeAttribute WHERE id = '"+biotope_attribute.getId()+"'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void updatebiotope_attribute(Biotope_attribute biotope_attribute) {

        String query = "UPDATE biotopeAttribute SET  " +
                             "INVES_REGION='"+biotope_attribute.getINVES_REGION()+"'"
                            +",INVESTIGATOR='"+biotope_attribute.getINVESTIGATOR()+"'"
                            +",INVES_DATETIME='"+biotope_attribute.getINVES_DATETIME()+"'"
                            +",INVES_INDEX="+biotope_attribute.getINVES_INDEX()+""
                            +",LUM_GROUP_NUM='"+biotope_attribute.getLUM_GROUP_NUM()+"'"
                            +",LUM_TYPE_RATE="+biotope_attribute.getLUM_TYPE_RATE()+""
                            +",STANDARD_HEIGHT="+biotope_attribute.getSTANDARD_HEIGHT()+""
                            +",LCM_GROUP_NUM='"+biotope_attribute.getLCM_GROUP_NUM()+"'"
                            +",LCM_TYPE='"+biotope_attribute.getLCM_TYPE()+"'"
                            +",TYPE_MARK='"+biotope_attribute.getTYPE_MARK()+"'"
                            +",GV_RATE="+biotope_attribute.getGV_RATE()+""
                            +",GV_STRUCTURE='"+biotope_attribute.getGV_STRUCTURE()+"'"
                            +",DIST_RETURN='"+biotope_attribute.getDIST_RETURN()+"'"
                            +",RESTORE_POT='"+biotope_attribute.getRESTORE_POT()+"'"
                            +",COMP_INTACT='"+biotope_attribute.getCOMP_INTACT()+"'"
                            +",VP_INTACT='"+biotope_attribute.getVP_INTACT()+"'"
                            +",IMP_FORM='"+biotope_attribute.getIMP_FORM()+"'"
                            +",BREAST_DIA='"+biotope_attribute.getBREAST_DIA()+"'"
                            +",FINAL_EST='"+biotope_attribute.getFINAL_EST()+"'"
                            +",TREE_SPECIES='"+biotope_attribute.getTREE_SPECIES()+"'"
                            +",TREE_HEIGHT="+biotope_attribute.getTREE_HEIGHT()+""
                            +",TREE_BREAST="+biotope_attribute.getTREE_BREAST()+""
                            +",TREE_COVE="+biotope_attribute.getTREE_COVE()+""
                            +",SUB_TREE_SPEC='"+biotope_attribute.getSUB_TREE_SPEC()+"'"
                            +",SUB_TREE_HEIGHT="+biotope_attribute.getSUB_TREE_HEIGHT()+""
                            +",SUB_TREE_BREAST="+biotope_attribute.getSUB_TREE_BREAST()+""
                            +",SUB_TREE_COVER="+biotope_attribute.getSUB_TREE_COVER()+""
                            +",SHRUB_SPECIES='"+biotope_attribute.getSHRUB_SPECIES()+"'"
                            +",SHRUB_HEIGHT="+biotope_attribute.getSHRUB_HEIGHT()+""
                            +",SHRUB_COVER="+biotope_attribute.getSHRUB_COVER()+""
                            +",HERB_SPECIES='"+biotope_attribute.getHERB_SPECIES()+"'"
                            +",HERB_HEIGHT="+biotope_attribute.getHERB_HEIGHT()+""
                            +",HERB_COVER="+biotope_attribute.getHERB_COVER()+""
                            +",PICTURE_FOLDER='"+biotope_attribute.getPICTURE_FOLDER()+"'"
                            +",WILD_ANI='"+biotope_attribute.getWILD_ANI()+"'"
                            +",REP_BIOTOP_POT='"+biotope_attribute.getREP_BIOTOP_POT()+"'"
                            +",UNUSUAL_NOTE='"+biotope_attribute.getUNUSUAL_NOTE()+"'"+
                            "where id = '" + biotope_attribute.getId() + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

}
