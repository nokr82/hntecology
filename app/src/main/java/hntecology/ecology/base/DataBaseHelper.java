package hntecology.ecology.base;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import hntecology.ecology.model.Base;
import hntecology.ecology.model.Biotope_attribute;
import hntecology.ecology.model.Birds_attribute;
import hntecology.ecology.model.Fish_attribute;
import hntecology.ecology.model.Flora_Attribute;
import hntecology.ecology.model.GpsSet;
import hntecology.ecology.model.Insect_attribute;
import hntecology.ecology.model.Mammal_attribute;
import hntecology.ecology.model.ManyFloraAttribute;
import hntecology.ecology.model.Reptilia_attribute;
import hntecology.ecology.model.StockMap;
import hntecology.ecology.model.Tracking;
import hntecology.ecology.model.TreeData1;
import hntecology.ecology.model.Zoobenthos_Attribute;

public class DataBaseHelper extends SQLiteOpenHelper {

    private final Context myContext;

    // The Android's default system path of your application database.
//    private static String DB_PATH = "/hntecology/ecology/database/";
    private static String DB_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data" + File.separator;

    private static String DB_NAME = "ecology.db";

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

                e.printStackTrace();

                throw new Error("Error copying database");

            }
        }

        try {
            String myPath = DB_PATH + DB_NAME;
            return SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
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
        if(f.exists()) {
            boolean deleted = f.delete();

            System.out.println("deleted : " + deleted);
        }

        f = new File(DB_PATH);

        System.out.println("f.exists() : " + f.exists());

        if (!f.exists()) {
            boolean made = f.mkdir();

            System.out.println("made : " + f.exists());

        }

        // Open your local db as the input stream
//        InputStream myInput = myContext.getAssets().open(DB_NAME);
//        InputStream myInput = myContext.getAssets().open(DB_NAME);

        File path = myContext.getDatabasePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data" + File.separator + "ecology.db");
        InputStream myInput = new FileInputStream(path);

        System.out.println("open--------------------------");

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
        query += "biotopeAttribute ( id String PRIMARY KEY";
        query += ",GROP_ID	   String";
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
        query += ",TEMP_YN String";
        query += ");";
        db.execSQL(query);


        String querybrids = "create table if not exists ";
        querybrids += "birdsAttribute ( id String PRIMARY KEY";
        querybrids += ",GROP_ID	   String";
        querybrids += ",PRJ_NAME	   String";
        querybrids += ",INV_REGION	String";
        querybrids += ",INV_DT	    String";
        querybrids += ",INV_PERSON	String";
        querybrids += ",WEATHER	String";
        querybrids += ",WIND 	String";
        querybrids += ",WIND_DIRE	String";
        querybrids += ",TEMPERATUR	    Float";
        querybrids += ",ETC	String";
        querybrids += ",NUM	    Int";
        querybrids += ",INV_TM	    String";
        querybrids += ",SPEC_NM	    String";
        querybrids += ",FAMI_NM	String";
        querybrids += ",SCIEN_NM	    String";
        querybrids += ",ENDANGERED	    String";
        querybrids += ",INDI_CNT	Integer";
        querybrids += ",OBS_STAT	String";
        querybrids += ",OBS_ST_ETC	    String";
        querybrids += ",USE_TAR	String";
        querybrids += ",USE_TAR_SP	String";
        querybrids += ",USE_LAYER	    String";
        querybrids += ",MJ_ACT	String";
        querybrids += ",MJ_ACT_PR	String";
        querybrids += ",GPS_LAT	    Float";
        querybrids += ",GPS_LON	    Float";
        querybrids += ",TEMP_YN 	String";
        querybrids += ");";
        db.execSQL(querybrids);

        String queryreptilia = "create table if not exists ";
        queryreptilia += "reptiliaAttribute ( id String PRIMARY KEY";
        queryreptilia += ",GROP_ID	   String";
        queryreptilia += ",PRJ_NAME	   String";
        queryreptilia += ",INV_REGION	String";
        queryreptilia += ",INV_DT	    String";
        queryreptilia += ",INV_PERSON	String";
        queryreptilia += ",WEATHER	String";
        queryreptilia += ",WIND 	String";
        queryreptilia += ",WIND_DIRE	String";
        queryreptilia += ",TEMPERATUR	    Float";
        queryreptilia += ",ETC	String";
        queryreptilia += ",NUM	    Int";
        queryreptilia += ",INV_TM	    String";
        queryreptilia += ",SPEC_NM	    String";
        queryreptilia += ",FAMI_NM	String";
        queryreptilia += ",SCIEN_NM	    String";
        queryreptilia += ",ENDANGERED	    String";
        queryreptilia += ",IN_CNT_ADU	Integer";
        queryreptilia += ",IN_CNT_LAR	Integer";
        queryreptilia += ",IN_CNT_EGG	Integer";
        queryreptilia += ",HAB_RIVEER	String";
        queryreptilia += ",HAB_EDGE	    String";
        queryreptilia += ",WATER_IN	String";
        queryreptilia += ",WATER_OUT	String";
        queryreptilia += ",WATER_CONT	    String";
        queryreptilia += ",WATER_QUAL	String";
        queryreptilia += ",WATER_DEPT	Integer";
        queryreptilia += ",HAB_AREA_W	Integer";
        queryreptilia += ",HAB_AREA_H	Integer";
        queryreptilia += ",GPS_LAT	    Float";
        queryreptilia += ",GPS_LON	    Float";
        queryreptilia += ",TEMP_YN	String";
        queryreptilia += ");";
        db.execSQL(queryreptilia);

        String querymammal = "create table if not exists ";
        querymammal += "mammalAttribute ( id String PRIMARY KEY";
        querymammal += ",GROP_ID	   String";
        querymammal += ",PRJ_NAME	   String";
        querymammal += ",INV_REGION	String";
        querymammal += ",INV_DT	    String";
        querymammal += ",INV_PERSON	String";
        querymammal += ",WEATHER	String";
        querymammal += ",WIND 	String";
        querymammal += ",WIND_DIRE	String";
        querymammal += ",TEMPERATUR	    Float";
        querymammal += ",ETC	String";
        querymammal += ",NUM	    Int";
        querymammal += ",INV_TM	    String";
        querymammal += ",SPEC_NM	    String";
        querymammal += ",FAMI_NM	String";
        querymammal += ",SCIEN_NM	    String";
        querymammal += ",ENDANGERED	    String";
        querymammal += ",OBS_TY	String";
        querymammal += ",OBS_TY_ETC	String";
        querymammal += ",INDI_CNT	Integer";
        querymammal += ",OB_PT_CHAR	String";
        querymammal += ",UNUS_NOTE	    String";
        querymammal += ",GPS_LAT	Float";
        querymammal += ",GPS_LON	Float";
        querymammal += ",UN_SPEC	    String";
        querymammal += ",UN_SPEC_RE	String";
        querymammal += ",TR_EASY	Integer";
        querymammal += ",TR_EASY_RE	Integer";
        querymammal += ",TEMP_YN	String";
        querymammal += ");";
        db.execSQL(querymammal);

        String queryinsect = "create table if not exists ";
        queryinsect += "insectAttribute ( id String PRIMARY KEY";
        queryinsect += ",GROP_ID	   String";
        queryinsect += ",PRJ_NAME	   String";
        queryinsect += ",INV_REGION	String";
        queryinsect += ",INV_DT	    String";
        queryinsect += ",INV_PERSON	String";
        queryinsect += ",WEATHER	String";
        queryinsect += ",WIND 	String";
        queryinsect += ",WIND_DIRE	String";
        queryinsect += ",TEMPERATUR	    Float";
        queryinsect += ",ETC	String";
        queryinsect += ",NUM	    Int";
        queryinsect += ",INV_TM	    String";
        queryinsect += ",SPEC_NM	    String";
        queryinsect += ",FAMI_NM	String";
        queryinsect += ",SCIEN_NM	    String";
        queryinsect += ",INDI_CNT	Integer";
        queryinsect += ",OBS_STAT	String";
        queryinsect += ",OBS_ST_ETC	String";
        queryinsect += ",USE_TAR	String";
        queryinsect += ",USER_TA_ETC	    String";
        queryinsect += ",MJ_ACT	    String";
        queryinsect += ",MJ_ACT_ETC	    String";
        queryinsect += ",INV_MN_ETC	    String";
        queryinsect += ",UNUS_NOTE	String";
        queryinsect += ",GPS_LAT	Float";
        queryinsect += ",GPS_LON	Float";
        queryinsect += ",TEMP_YN	String";
        queryinsect += ");";
        db.execSQL(queryinsect);

        String layerinsect = "create table if not exists ";
        layerinsect += "layers ( id String PRIMARY KEY";
        layerinsect += ",file_name	   String";
        layerinsect += ",layer_name	   String";
        layerinsect += ",min_scale	String";
        layerinsect += ",max_scale	    String";
        layerinsect += ",type	String";
        layerinsect += ",added	String";
        layerinsect += ",grop_id 	String";
        layerinsect += ");";
        db.execSQL(layerinsect);

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
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_PERSON,INV_DT,INV_TM,INV_INDEX,LU_GR_NUM,LU_TY_RATE,STAND_H,LC_GR_NUM,LC_TY,TY_MARK,GV_RATE,GV_STRUCT,DIS_RET,RESTOR_POT,COMP_INTA";
        query += ",VP_INTA,IMP_FORM,BREA_DIA,FIN_EST,TRE_SPEC,TRE_FAMI,TRE_SCIEN,TRE_H,TRE_BREA,TRE_COVE,STRE_SPEC,STRE_FAMI,STRE_SCIEN,STRE_H,STRE_BREA,STRE_COVE,SHR_SPEC,SHR_FAMI";
        query += ",SHR_SCIEN,SHR_H,STR_COVE,HER_SPEC,HER_FAMI,HER_SCIEN,HER_H,HER_COVE,PIC_FOLDER,WILD_ANI,BIOTOP_POT,UNUS_NOTE,GPS_LAT,GPS_LON,NEED_CONF,CONF_MOD,TEMP_YN)";


        query += " values (";
        query += " '" + biotope_attribute.getGROP_ID() + "'";
        query += ", '" + biotope_attribute.getPRJ_NAME() + "'";
        query += ", '" + biotope_attribute.getINV_REGION() + "'";
        query += ", '" + biotope_attribute.getINV_PERSON() + "'";
        query += ", '" + biotope_attribute.getINV_DT() + "'";
        query += ", '" + biotope_attribute.getINV_TM() + "'";
        query += ", '" + biotope_attribute.getINV_INDEX() + "'";
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
        query += ", '" + biotope_attribute.getTEMP_YN() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }


    public void insertbirds_attribute(Birds_attribute birds_attribute){
        String query = "INSERT INTO birdsAttribute";
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_DT,INV_PERSON,WEATHER,WIND,WIND_DIRE";
        query += ",TEMPERATUR,ETC,NUM,INV_TM,SPEC_NM,FAMI_NM,SCIEN_NM,ENDANGERED,INDI_CNT,OBS_STAT,OBS_ST_ETC";
        query += ",USE_TAR,USE_TAR_SP,USE_LAYER,MJ_ACT,MJ_ACT_PR,GPS_LAT,GPS_LON,TEMP_YN,CONF_MOD)";

        query += " values (";
        query += " '" + birds_attribute.getGROP_ID() + "'";
        query += ", '" + birds_attribute.getPRJ_NAME() + "'";
        query += ", '" + birds_attribute.getINV_REGION() + "'";
        query += ", '" + birds_attribute.getINV_DT() + "'";
        query += ", '" + birds_attribute.getINV_PERSON() + "'";
        query += ", '" + birds_attribute.getWEATHER() + "'";
        query += ", '" + birds_attribute.getWIND() + "'";
        query += ", '" + birds_attribute.getWIND_DIRE() + "'";
        query += ", '" + birds_attribute.getTEMPERATUR() + "'";
        query += ", '" + birds_attribute.getETC() + "'";
        query += ", '" + birds_attribute.getNUM() + "'";
        query += ", '" + birds_attribute.getINV_TM() + "'";
        query += ", '" + birds_attribute.getSPEC_NM() + "'";
        query += ", '" + birds_attribute.getFAMI_NM() + "'";
        query += ", '" + birds_attribute.getSCIEN_NM() + "'";
        query += ", '" + birds_attribute.getENDANGERED() + "'";
        query += ", '" + birds_attribute.getINDI_CNT() + "'";
        query += ", '" + birds_attribute.getOBS_STAT() + "'";
        query += ", '" + birds_attribute.getOBS_ST_ETC() + "'";
        query += ", '" + birds_attribute.getUSE_TAR() + "'";
        query += ", '" + birds_attribute.getUSE_TAR_SP() + "'";
        query += ", '" + birds_attribute.getUSE_LAYER() + "'";
        query += ", '" + birds_attribute.getMJ_ACT() + "'";
        query += ", '" + birds_attribute.getMJ_ACT_PR() + "'";
        query += ", '" + birds_attribute.getGPS_LAT() + "'";
        query += ", '" + birds_attribute.getGPS_LON() + "'";
        query += ", '" + birds_attribute.getTEMP_YN() + "'";
        query += ", '" + birds_attribute.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertreptilia_attribute(Reptilia_attribute reptilia_attribute){
        String query = "INSERT INTO reptiliaAttribute";
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_DT,INV_PERSON,WEATHER,WIND,WIND_DIRE";
        query += ",TEMPERATUR,ETC,NUM,INV_TM,SPEC_NM,FAMI_NM,SCIEN_NM,ENDANGERED,IN_CNT_ADU,IN_CNT_LAR,IN_CNT_EGG";
        query += ",HAB_RIVEER,HAB_EDGE,WATER_IN,WATER_OUT,WATER_CONT,WATER_QUAL,WATER_DEPT";
        query += ",HAB_AREA_W,HAB_AREA_H,GPS_LAT,GPS_LON,TEMP_YN,CONF_MOD)";


        query += " values (";
        query += " '" + reptilia_attribute.getGROP_ID() + "'";
        query += ", '" + reptilia_attribute.getPRJ_NAME() + "'";
        query += ", '" + reptilia_attribute.getINV_REGION() + "'";
        query += ", '" + reptilia_attribute.getINV_DT() + "'";
        query += ", '" + reptilia_attribute.getINV_PERSON() + "'";
        query += ", '" + reptilia_attribute.getWEATHER() + "'";
        query += ", '" + reptilia_attribute.getWIND() + "'";
        query += ", '" + reptilia_attribute.getWIND_DIRE() + "'";
        query += ", '" + reptilia_attribute.getTEMPERATUR() + "'";
        query += ", '" + reptilia_attribute.getETC() + "'";
        query += ",  '" + reptilia_attribute.getNUM() + "'";
        query += ", '" + reptilia_attribute.getINV_TM() + "'";
        query += ", '" + reptilia_attribute.getSPEC_NM() + "'";
        query += ", '" + reptilia_attribute.getFAMI_NM() + "'";
        query += ", '" + reptilia_attribute.getSCIEN_NM() + "'";
        query += ", '" + reptilia_attribute.getENDANGERED() + "'";
        query += ", '" + reptilia_attribute.getIN_CNT_ADU() + "'";
        query += ", '" + reptilia_attribute.getIN_CNT_LAR() + "'";
        query += ", '" + reptilia_attribute.getIN_CNT_EGG() + "'";
        query += ", '" + reptilia_attribute.getHAB_RIVEER() + "'";
        query += ", '" + reptilia_attribute.getHAB_EDGE() + "'";
        query += ", '" + reptilia_attribute.getWATER_IN() + "'";
        query += ", '" + reptilia_attribute.getWATER_OUT() + "'";
        query += ", '" + reptilia_attribute.getWATER_CONT() + "'";
        query += ", '" + reptilia_attribute.getWATER_QUAL() + "'";
        query += ", '" + reptilia_attribute.getWATER_DEPT() + "'";
        query += ", '" + reptilia_attribute.getHAB_AREA_W() + "'";
        query += ", '" + reptilia_attribute.getHAB_AREA_H() + "'";
        query += ", " + reptilia_attribute.getGPS_LAT() + "";
        query += ", " + reptilia_attribute.getGPS_LON() + "";
        query += ", '" + reptilia_attribute.getTEMP_YN() + "'";
        query += ", '" + reptilia_attribute.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertmammal_attribute(Mammal_attribute mammal_attribute){
        String query = "INSERT INTO mammalAttribute";
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_DT,INV_PERSON,WEATHER,WIND,WIND_DIRE";
        query += ",TEMPERATUR,ETC,NUM,INV_TM,SPEC_NM,FAMI_NM,SCIEN_NM,ENDANGERED,OBS_TY,OBS_TY_ETC,INDI_CNT";
        query += ",OB_PT_CHAR,UNUS_NOTE,GPS_LAT,GPS_LON,UN_SPEC,UN_SPEC_RE,TR_EASY,TR_EASY_RE,TEMP_YN,CONF_MOD)";

        query += " values (";
        query += " '" + mammal_attribute.getGROP_ID() + "'";
        query += ", '" + mammal_attribute.getPRJ_NAME() + "'";
        query += ", '" + mammal_attribute.getINV_REGION() + "'";
        query += ", '" + mammal_attribute.getINV_DT() + "'";
        query += ", '" + mammal_attribute.getINV_PERSON() + "'";
        query += ", '" + mammal_attribute.getWEATHER() + "'";
        query += ", '" + mammal_attribute.getWIND() + "'";
        query += ", '" + mammal_attribute.getWIND_DIRE() + "'";
        query += ", '" + mammal_attribute.getTEMPERATUR() + "'";
        query += ", '" + mammal_attribute.getETC() + "'";
        query += ", '" + mammal_attribute.getNUM() + "'";
        query += ", '" + mammal_attribute.getINV_TM() + "'";
        query += ", '" + mammal_attribute.getSPEC_NM() + "'";
        query += ", '" + mammal_attribute.getFAMI_NM() + "'";
        query += ", '" + mammal_attribute.getSCIEN_NM() + "'";
        query += ", '" + mammal_attribute.getENDANGERED() + "'";
        query += ", '" + mammal_attribute.getOBS_TY() + "'";
        query += ", '" + mammal_attribute.getOBS_TY_ETC() + "'";
        query += ", '" + mammal_attribute.getINDI_CNT() + "'";
        query += ", '" + mammal_attribute.getOB_PT_CHAR() + "'";
        query += ", '" + mammal_attribute.getUNUS_NOTE() + "'";
        query += ", '" + mammal_attribute.getGPS_LAT() + "'";
        query += ", '" + mammal_attribute.getGPS_LON() + "'";
        query += ", '" + mammal_attribute.getUN_SPEC() + "'";
        query += ", '" + mammal_attribute.getUN_SPEC_RE() + "'";
        query += ", '" + mammal_attribute.getTR_EASY() + "'";
        query += ", '" + mammal_attribute.getTR_EASY_RE() + "'";
        query += ", '" + mammal_attribute.getTEMP_YN() + "'";
        query += ", '" + mammal_attribute.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertinsect_attribute(Insect_attribute insect_attribute){
        String query = "INSERT INTO insectAttribute";
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_DT,INV_PERSON,WEATHER,WIND,WIND_DIRE";
        query += ",TEMPERATUR,ETC,NUM,INV_TM,SPEC_NM,FAMI_NM,SCIEN_NM,INDI_CNT,OBS_STAT,OBS_ST_ETC";
        query += ",USE_TAR,USER_TA_ETC,MJ_ACT,MJ_ACT_ETC,INV_MEAN,INV_MN_ETC,UNUS_NOTE,GPS_LAT,GPS_LON,TEMP_YN,CONF_MOD)";

        query += " values (";
        query += " '" + insect_attribute.getGROP_ID() + "'";
        query += ", '" + insect_attribute.getPRJ_NAME() + "'";
        query += ", '" + insect_attribute.getINV_REGION() + "'";
        query += ", '" + insect_attribute.getINV_DT() + "'";
        query += ", '" + insect_attribute.getINV_PERSON() + "'";
        query += ", '" + insect_attribute.getWEATHER() + "'";
        query += ", '" + insect_attribute.getWIND() + "'";
        query += ", '" + insect_attribute.getWIND_DIRE() + "'";
        query += ", '" + insect_attribute.getTEMPERATUR() + "'";
        query += ", '" + insect_attribute.getETC() + "'";
        query += ", '" + insect_attribute.getNUM() + "'";
        query += ", '" + insect_attribute.getINV_TM() + "'";
        query += ", '" + insect_attribute.getSPEC_NM() + "'";
        query += ", '" + insect_attribute.getFAMI_NM() + "'";
        query += ", '" + insect_attribute.getSCIEN_NM() + "'";
        query += ", '" + insect_attribute.getINDI_CNT() + "'";
        query += ", '" + insect_attribute.getOBS_STAT() + "'";
        query += ", '" + insect_attribute.getOBS_ST_ETC() + "'";
        query += ", '" + insect_attribute.getUSE_TAR() + "'";
        query += ", '" + insect_attribute.getUSER_TA_ETC() + "'";
        query += ", '" + insect_attribute.getMJ_ACT() + "'";
        query += ", '" + insect_attribute.getMJ_ACT_ETC() + "'";
        query += ", '" + insect_attribute.getINV_MEAN() + "'";
        query += ", '" + insect_attribute.getINV_MN_ETC() + "'";
        query += ", '" + insect_attribute.getUNUS_NOTE() + "'";
        query += ", '" + insect_attribute.getGPS_LAT() + "'";
        query += ", '" + insect_attribute.getGPS_LON() + "'";
        query += ", '" + insect_attribute.getTEMP_YN() + "'";
        query += ", '" + insect_attribute.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertfish_attribute(Fish_attribute fish_attribute){
        String query = "INSERT INTO fishAttribute";
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_DT,INV_TM,INV_PERSON,WEATHER,WIND,WIND_DIRE";
        query += ",TEMPERATUR,ETC,MID_RAGE,CODE_NUM,RIVER_NUM,RIVER_NM,NET_CNT,NET_MIN,AD_DIST_NM,GPS_LAT";
        query += ",GPS_LON,COLL_TOOL,STREAM_W,WATER_W,WATER_D,WATER_CUR,RIV_STR,RIV_STR_IN,RIV_FORM";
        query += ",NUM,SPEC_NM,FAMI_NM,SCIEN_NM,INDI_CNT,UNIDENT,RIV_FM_CH,UN_FISH_CH,TEMP_YN,CONF_MOD)";

        query += " values (";
        query += " '" + fish_attribute.getGROP_ID() + "'";
        query += ", '" + fish_attribute.getPRJ_NAME() + "'";
        query += ", '" + fish_attribute.getINV_REGION() + "'";
        query += ", '" + fish_attribute.getINV_DT() + "'";
        query += ", '" + fish_attribute.getINV_TM() + "'";
        query += ", '" + fish_attribute.getINV_PERSON() + "'";
        query += ", '" + fish_attribute.getWEATHER() + "'";
        query += ", '" + fish_attribute.getWIND() + "'";
        query += ", '" + fish_attribute.getWIND_DIRE() + "'";
        query += ", '" + fish_attribute.getTEMPERATUR() + "'";
        query += ", '" + fish_attribute.getETC() + "'";
        query += ", '" + fish_attribute.getMID_RAGE() + "'";
        query += ", '" + fish_attribute.getCODE_NUM() + "'";
        query += ", '" + fish_attribute.getRIVER_NUM() + "'";
        query += ", '" + fish_attribute.getRIVER_NM() + "'";
        query += ", '" + fish_attribute.getNET_CNT() + "'";
        query += ", '" + fish_attribute.getNET_MIN() + "'";
        query += ", '" + fish_attribute.getAD_DIST_NM() + "'";
        query += ", '" + fish_attribute.getGPS_LAT() + "'";
        query += ", '" + fish_attribute.getGPS_LON() + "'";
        query += ", '" + fish_attribute.getCOLL_TOOL() + "'";
        query += ", '" + fish_attribute.getSTREAM_W() + "'";
        query += ", '" + fish_attribute.getWATER_W() + "'";
        query += ", '" + fish_attribute.getWATER_D() + "'";
        query += ", '" + fish_attribute.getWATER_CUR() + "'";
        query += ", '" + fish_attribute.getRIV_STR() + "'";
        query += ", '" + fish_attribute.getRIV_STR_IN() + "'";
        query += ", '" + fish_attribute.getRIV_FORM() + "'";
        query += ", '" + fish_attribute.getNUM() + "'";
        query += ", '" + fish_attribute.getSPEC_NM() + "'";
        query += ", '" + fish_attribute.getFAMI_NM() + "'";
        query += ", '" + fish_attribute.getSCIEN_NM() + "'";
        query += ", '" + fish_attribute.getINDI_CNT() + "'";
        query += ", '" + fish_attribute.getUNIDENT() + "'";
        query += ", '" + fish_attribute.getRIV_FM_CH() + "'";
        query += ", '" + fish_attribute.getUN_FISH_CH() + "'";
        query += ", '" + fish_attribute.getTEMP_YN() + "'";
        query += ", '" + fish_attribute.getCONF_MOD() + "'";

        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertflora_attribute(Flora_Attribute flora_Attribute){
        String query = "INSERT INTO floraAttribute";
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_DT,INV_PERSON,WEATHER,WIND,WIND_DIRE";
        query += ",TEMPERATUR,ETC,NUM,INV_TM,SPEC_NM,FAMI_NM,SCIEN_NM,FLORE_YN,PLANT_YN,HAB_STAT";
        query += ",HAB_ETC,COL_IN_CNT,THRE_CAU,GPS_LAT,GPS_LON,TEMP_YN,CONF_MOD)";

        query += " values (";
        query += " '" + flora_Attribute.getGROP_ID() + "'";
        query += ", '" + flora_Attribute.getPRJ_NAME() + "'";
        query += ", '" + flora_Attribute.getINV_REGION() + "'";
        query += ", '" + flora_Attribute.getINV_DT() + "'";
        query += ", '" + flora_Attribute.getINV_PERSON() + "'";
        query += ", '" + flora_Attribute.getWEATHER() + "'";
        query += ", '" + flora_Attribute.getWIND() + "'";
        query += ", '" + flora_Attribute.getWIND_DIRE() + "'";
        query += ", '" + flora_Attribute.getTEMPERATUR() + "'";
        query += ", '" + flora_Attribute.getETC() + "'";
        query += ", '" + flora_Attribute.getNUM() + "'";
        query += ", '" + flora_Attribute.getINV_TM() + "'";
        query += ", '" + flora_Attribute.getSPEC_NM() + "'";
        query += ", '" + flora_Attribute.getFAMI_NM() + "'";
        query += ", '" + flora_Attribute.getSCIEN_NM() + "'";
        query += ", '" + flora_Attribute.getFLORE_YN() + "'";
        query += ", '" + flora_Attribute.getPLANT_YN() + "'";
        query += ", '" + flora_Attribute.getHAB_STAT() + "'";
        query += ", '" + flora_Attribute.getHAB_ETC() + "'";
        query += ", '" + flora_Attribute.getCOL_IN_CNT() + "'";
        query += ", '" + flora_Attribute.getTHRE_CAU() + "'";
        query += ", '" + flora_Attribute.getGPS_LAT() + "'";
        query += ", '" + flora_Attribute.getGPS_LON() + "'";
        query += ", '" + flora_Attribute.getTEMP_YN() + "'";
        query += ", '" + flora_Attribute.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertmanyflora_attribute(ManyFloraAttribute ManyFloraAttribute){
        String query = "INSERT INTO ManyFloraAttribute";
        query += "(GROP_ID,INV_REGION,INV_PERSON,INV_DT,INV_TM,TRE_NUM,TRE_SPEC,TRE_FAMI";
        query += ",TRE_SCIEN,TRE_H,TRE_BREA,TRE_COVE,STRE_NUM,STRE_SPEC,STRE_FAMI,STRE_SCIEN,STRE_H,STRE_BREA";
        query += ",STRE_COVE,SHR_NUM,SHR_SPEC,SHR_FAMI,SHR_SCIEN,SHR_H,SHR_COVE,HER_NUM,HER_SPEC,HER_FAMI";
        query += ",HER_SCIEN,HER_H,HER_COVE,GPS_LAT,GPS_LON,TEMP_YN,CONF_MOD)";

        query += " values (";
        query += " '" + ManyFloraAttribute.getGROP_ID() + "'";
        query += ", '" + ManyFloraAttribute.getINV_REGION() + "'";
        query += ", '" + ManyFloraAttribute.getINV_PERSON() + "'";
        query += ", '" + ManyFloraAttribute.getINV_DT() + "'";
        query += ", '" + ManyFloraAttribute.getINV_TM() + "'";
        query += ", '" + ManyFloraAttribute.getTRE_NUM() + "'";
        query += ", '" + ManyFloraAttribute.getTRE_SPEC() + "'";
        query += ", '" + ManyFloraAttribute.getTRE_FAMI() + "'";
        query += ", '" + ManyFloraAttribute.getTRE_SCIEN() + "'";
        query += ", '" + ManyFloraAttribute.getTRE_H() + "'";
        query += ", '" + ManyFloraAttribute.getTRE_BREA() + "'";
        query += ", '" + ManyFloraAttribute.getTRE_COVE() + "'";
        query += ", '" + ManyFloraAttribute.getSTRE_NUM() + "'";
        query += ", '" + ManyFloraAttribute.getSTRE_SPEC() + "'";
        query += ", '" + ManyFloraAttribute.getSTRE_FAMI() + "'";
        query += ", '" + ManyFloraAttribute.getSTRE_SCIEN() + "'";
        query += ", '" + ManyFloraAttribute.getSTRE_H() + "'";
        query += ", '" + ManyFloraAttribute.getSTRE_BREA() + "'";
        query += ", '" + ManyFloraAttribute.getSTRE_COVE() + "'";
        query += ", '" + ManyFloraAttribute.getSHR_NUM() + "'";
        query += ", '" + ManyFloraAttribute.getSHR_SPEC() + "'";
        query += ", '" + ManyFloraAttribute.getSHR_FAMI() + "'";
        query += ", '" + ManyFloraAttribute.getSHR_SCIEN() + "'";
        query += ", '" + ManyFloraAttribute.getSHR_H() + "'";
        query += ", '" + ManyFloraAttribute.getSHR_COVE() + "'";
        query += ", '" + ManyFloraAttribute.getHER_NUM() + "'";
        query += ", '" + ManyFloraAttribute.getHER_SPEC() + "'";
        query += ", '" + ManyFloraAttribute.getHER_FAMI() + "'";
        query += ", '" + ManyFloraAttribute.getHER_SCIEN() + "'";
        query += ", '" + ManyFloraAttribute.getHER_H() + "'";
        query += ", '" + ManyFloraAttribute.getHER_COVE() + "'";
        query += ", '" + ManyFloraAttribute.getGPS_LAT() + "'";
        query += ", '" + ManyFloraAttribute.getGPS_LON() + "'";
        query += ", '" + ManyFloraAttribute.getTEMP_YN() + "'";
        query += ", '" + ManyFloraAttribute.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void inserttracking(Tracking tracking){
        String query = "INSERT INTO tracking";
        query += "(latitude,longitude)";

        query += " values (";
        query += " '" + tracking.getLATITUDE() + "'";
        query += ", '" + tracking.getLONGITUDE() + "'";

        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertbase(Base base){
        String query = "INSERT INTO base";
        query += "(GROP_ID,PRJ_NAME,GPS_LAT,GPS_LON,INV_PERSON,INV_DT,INV_TM)";

        query += " values (";
        query += " '" + base.getGROP_ID() + "'";
        query += ", '" + base.getPRJ_NAME() + "'";
        query += ", '" + base.getGPS_LAT() + "'";
        query += ", '" + base.getGPS_LON() + "'";
        query += ", '" + base.getINV_PERSON() + "'";
        query += ", '" + base.getINV_DT() + "'";
        query += ", '" + base.getINV_TM() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertlayers(String file_name , String layer_name , String type,String Y){
        String query = "INSERT INTO layers";
        query += "(file_name,layer_name,min_scale,max_scale,type,added,grop_id)";

        query += " values (";
        query += " '" + file_name + "'";
        query += ", '" + layer_name + "'";
        query += ", '" + 10 + "'";
        query += ", '" + 19 + "'";
        query += ", '" + type + "'";
        query += ", '" + Y + "'";
        query += ", '" + "" + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertzoobenthos(Zoobenthos_Attribute Zoobenthos_Attribute){
        String query = "INSERT INTO ZoobenthosAttribute";
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_MEAN,INV_PERSON,MAP_SYS_NM,COORD_N_D,COORD_N_M,COORD_N_S,COORD_E_D,COORD_E_M,COORD_E_S";
        query += ",INV_DT,NUM,INV_TM,WEATHER,INV_TOOL,AD_DIST_NM,RIV_W1,RIV_W2,RUN_RIV_W1";
        query += ",RUN_RIV_W2,WATER_DEPT,HAB_TY,HAB_TY_ETC,FILT_AREA,TEMPERATUR,WATER_TEM,TURBIDITY,MUD,SAND,COR_SAND,GRAVEL,STONE_S";
        query += ",STONE_B,CONCRETE,BED_ROCK,BANK_L,BANK_L_ETC,BANK_R,BANK_R_ETC,BAS_L,BAS_L_ETC,BAS_R,BAS_R_ETC,DIST_CAU,DIST_ETC";
        query += ",UNUS_NOTE,GPS_LAT,GPS_LON,SPEC_NM,FAMI_NM,SCIEN_NM,TEMP_YN,CONF_MOD)";

        query += " values (";
        query += " '" + Zoobenthos_Attribute.getGROP_ID() + "'";
        query += ", '" + Zoobenthos_Attribute.getPRJ_NAME() + "'";
        query += ", '" + Zoobenthos_Attribute.getINV_REGION() + "'";
        query += ", '" + Zoobenthos_Attribute.getINV_MEAN() + "'";
        query += ", '" + Zoobenthos_Attribute.getINV_PERSON() + "'";
        query += ", '" + Zoobenthos_Attribute.getMAP_SYS_NM() + "'";
        query += ", '" + Zoobenthos_Attribute.getCOORD_N_D() + "'";
        query += ", '" + Zoobenthos_Attribute.getCOORD_N_M() + "'";
        query += ", '" + Zoobenthos_Attribute.getCOORD_N_S() + "'";
        query += ", '" + Zoobenthos_Attribute.getCOORD_E_D() + "'";
        query += ", '" + Zoobenthos_Attribute.getCOORD_E_M() + "'";
        query += ", '" + Zoobenthos_Attribute.getCOORD_E_S() + "'";
        query += ", '" + Zoobenthos_Attribute.getINV_DT() + "'";
        query += ", '" + Zoobenthos_Attribute.getNUM() + "'";
        query += ", '" + Zoobenthos_Attribute.getINV_TM() + "'";
        query += ", '" + Zoobenthos_Attribute.getWEATHER() + "'";
        query += ", '" + Zoobenthos_Attribute.getINV_TOOL() + "'";
        query += ", '" + Zoobenthos_Attribute.getAD_DIST_NM() + "'";
        query += ", '" + Zoobenthos_Attribute.getRIV_W1() + "'";
        query += ", '" + Zoobenthos_Attribute.getRIV_W2() + "'";
        query += ", '" + Zoobenthos_Attribute.getRUN_RIV_W1() + "'";
        query += ", '" + Zoobenthos_Attribute.getRUN_RIV_W2() + "'";
        query += ", '" + Zoobenthos_Attribute.getWATER_DEPT() + "'";
        query += ", '" + Zoobenthos_Attribute.getHAB_TY() + "'";
        query += ", '" + Zoobenthos_Attribute.getHAB_TY_ETC() + "'";
        query += ", '" + Zoobenthos_Attribute.getFILT_AREA() + "'";
        query += ", '" + Zoobenthos_Attribute.getTEMPERATUR() + "'";
        query += ", '" + Zoobenthos_Attribute.getWATER_TEM() + "'";
        query += ", '" + Zoobenthos_Attribute.getTURBIDITY() + "'";
        query += ", '" + Zoobenthos_Attribute.getMUD() + "'";
        query += ", '" + Zoobenthos_Attribute.getSAND() + "'";
        query += ", '" + Zoobenthos_Attribute.getCOR_SAND() + "'";
        query += ", '" + Zoobenthos_Attribute.getGRAVEL() + "'";
        query += ", '" + Zoobenthos_Attribute.getSTONE_S() + "'";
        query += ", '" + Zoobenthos_Attribute.getSTONE_B() + "'";
        query += ", '" + Zoobenthos_Attribute.getCONCRETE() + "'";
        query += ", '" + Zoobenthos_Attribute.getBED_ROCK() + "'";
        query += ", '" + Zoobenthos_Attribute.getBANK_L() + "'";
        query += ", '" + Zoobenthos_Attribute.getBANK_L_ETC() + "'";
        query += ", '" + Zoobenthos_Attribute.getBANK_R() + "'";
        query += ", '" + Zoobenthos_Attribute.getBANK_R_ETC() + "'";
        query += ", '" + Zoobenthos_Attribute.getBAS_L() + "'";
        query += ", '" + Zoobenthos_Attribute.getBAS_L_ETC() + "'";
        query += ", '" + Zoobenthos_Attribute.getBAS_R() + "'";
        query += ", '" + Zoobenthos_Attribute.getBAS_R_ETC() + "'";
        query += ", '" + Zoobenthos_Attribute.getDIST_CAU() + "'";
        query += ", '" + Zoobenthos_Attribute.getDIST_ETC() + "'";
        query += ", '" + Zoobenthos_Attribute.getUNUS_NOTE() + "'";
        query += ", '" + Zoobenthos_Attribute.getGPS_LAT() + "'";
        query += ", '" + Zoobenthos_Attribute.getGPS_LON() + "'";
        query += ", '" + Zoobenthos_Attribute.getSPEC_NM() + "'";
        query += ", '" + Zoobenthos_Attribute.getFAMI_NM() + "'";
        query += ", '" + Zoobenthos_Attribute.getSCIEN_NM() + "'";
        query += ", '" + Zoobenthos_Attribute.getTEMP_YN() + "'";
        query += ", '" + Zoobenthos_Attribute.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void insertstockmap(StockMap StockMap){
        String query = "INSERT INTO StockMap";
        query += "(GROP_ID,PRJ_NAME,INV_REGION,INV_PERSON,INV_DT,INV_TM,NUM,FRTP_CD,KOFTR_GROUP_CD,STORUNST_CD,FROR_CD,DMCLS_CD";
        query += ",AGCLS_CD,DNST_CD,HEIGHT,LDMARK_STNDA_CD,MAP_LABEL,MAP_LABEL2,ETC_PCMTT,GPS_LAT,GPS_LON,CONF_MOD)";

        query += " values (";
        query += " '" + StockMap.getGROP_ID() + "'";
        query += ", '" + StockMap.getPRJ_NAME() + "'";
        query += ", '" + StockMap.getINV_REGION() + "'";
        query += ", '" + StockMap.getINV_PERSON() + "'";
        query += ", '" + StockMap.getINV_DT() + "'";
        query += ", '" + StockMap.getINV_TM() + "'";
        query += ", '" + StockMap.getNUM() + "'";
        query += ", '" + StockMap.getFRTP_CD() + "'";
        query += ", '" + StockMap.getKOFTR_GROUP_CD() + "'";
        query += ", '" + StockMap.getSTORUNST_CD() + "'";
        query += ", '" + StockMap.getFROR_CD() + "'";
        query += ", '" + StockMap.getDMCLS_CD() + "'";
        query += ", '" + StockMap.getAGCLS_CD() + "'";
        query += ", '" + StockMap.getDNST_CD() + "'";
        query += ", '" + StockMap.getHEIGHT() + "'";
        query += ", '" + StockMap.getLDMARK_STNDA_CD() + "'";
        query += ", '" + StockMap.getMAP_LABEL() + "'";
        query += ", '" + StockMap.getMAP_LABEL2() + "'";
        query += ", '" + StockMap.getETC_PCMTT() + "'";
        query += ", '" + StockMap.getGPS_LAT() + "'";
        query += ", '" + StockMap.getGPS_LON() + "'";
        query += ", '" + StockMap.getCONF_MOD() + "'";
        query += " ); ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }




    public void deletereptilia_attribute(Reptilia_attribute reptilia_attribute,String page) {
        String query = "DELETE FROM reptiliaAttribute WHERE id = '" + page + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deletebirds_attribute(Birds_attribute birds_attribute,String page) {
        String query = "DELETE FROM birdsAttribute WHERE id = '" + page + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deletebiotope_attribute(Biotope_attribute biotope_attribute,String page) {
        String query = "DELETE FROM biotopeAttribute WHERE id = '" + page + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deletemammal_attribute(Mammal_attribute mammal_attribute,String page) {
        String query = "DELETE FROM mammalAttribute WHERE id = '" + page + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deleteinsect_attribute(Insect_attribute insect_attribute,String page) {
        String query = "DELETE FROM insectAttribute WHERE id = '" + page + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deletefish_attribute(Fish_attribute fish_attribute,String page) {
        String query = "DELETE FROM fishAttribute WHERE id = '" + page + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }



    public void deleteflora_attribute(Flora_Attribute flora_attribute,String page) {
        String query = "DELETE FROM floraAttribute WHERE id = '" + page + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deletezoobenthous_attribute(Zoobenthos_Attribute ZoobenthosAttribute,String pk) {
        String query = "DELETE FROM ZoobenthosAttribute WHERE id = '" + pk + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }


    public void deletestockmap(String pk) {
        String query = "DELETE FROM StockMap WHERE id = '" + pk + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deletemanyflora_attribute(ManyFloraAttribute ManyFloraAttribute,String GROP_ID) {
        String query = "DELETE FROM ManyFloraAttribute WHERE GROP_ID = '" + GROP_ID + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deleteAllManyFloraAttribute(String GROP_ID) {
        String query = "DELETE FROM ManyFloraAttribute WHERE GROP_ID = '" + GROP_ID + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deletetracking() {
        String query = "DELETE FROM tracking ";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void updatereptilia_attribute(Reptilia_attribute reptilia_attribute,String pk) {

        String query = "UPDATE reptiliaAttribute SET  " +
                "INV_REGION='" + reptilia_attribute.getINV_REGION() + "'"
                + ",INV_DT='" + reptilia_attribute.getINV_DT() + "'"
                + ",INV_PERSON='" + reptilia_attribute.getINV_PERSON() + "'"
                + ",WEATHER='" + reptilia_attribute.getWEATHER() + "'"
                + ",WIND='" + reptilia_attribute.getWIND() + "'"
                + ",WIND_DIRE='" + reptilia_attribute.getWIND_DIRE() + "'"
                + ",TEMPERATUR='" + reptilia_attribute.getTEMPERATUR() + "'"
                + ",NUM='" + reptilia_attribute.getNUM() + "'"
                + ",INV_TM='" + reptilia_attribute.getINV_TM() + "'"
                + ",SPEC_NM='" + reptilia_attribute.getSPEC_NM() + "'"
                + ",FAMI_NM='" + reptilia_attribute.getFAMI_NM() + "'"
                + ",SCIEN_NM='" + reptilia_attribute.getSCIEN_NM() + "'"
                + ",ENDANGERED='" + reptilia_attribute.getENDANGERED() + "'"
                + ",IN_CNT_ADU='" + reptilia_attribute.getIN_CNT_ADU() + "'"
                + ",IN_CNT_LAR='" + reptilia_attribute.getIN_CNT_LAR() + "'"
                + ",IN_CNT_EGG='" + reptilia_attribute.getIN_CNT_EGG() + "'"
                + ",HAB_RIVEER='" + reptilia_attribute.getHAB_RIVEER() + "'"
                + ",HAB_EDGE='" + reptilia_attribute.getHAB_EDGE() + "'"
                + ",WATER_IN='" + reptilia_attribute.getWATER_IN() + "'"
                + ",WATER_OUT='" + reptilia_attribute.getWATER_OUT() + "'"
                + ",WATER_CONT='" + reptilia_attribute.getWATER_CONT() + "'"
                + ",WATER_QUAL='" + reptilia_attribute.getWATER_QUAL() + "'"
                + ",WATER_DEPT='" + reptilia_attribute.getWATER_DEPT() + "'"
                + ",HAB_AREA_W='" + reptilia_attribute.getHAB_AREA_W() + "'"
                + ",HAB_AREA_H='" + reptilia_attribute.getHAB_AREA_H() + "'"
                + ",GPS_LAT='" + reptilia_attribute.getGPS_LAT() + "'"
                + ",GPS_LON='" + reptilia_attribute.getGPS_LON() + "'"
                + ",TEMP_YN='" + reptilia_attribute.getTEMP_YN() + "'"
                + ",TEMP_YN='" + reptilia_attribute.getCONF_MOD() + "'"+
                "where id = '" + pk + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updatebirds_attribute(Birds_attribute birds_attribute,String pk) {

        String query = "UPDATE birdsAttribute SET  " +
                "INV_REGION='" + birds_attribute.getINV_REGION() + "'"
                + ",INV_DT='" + birds_attribute.getINV_DT() + "'"
                + ",INV_PERSON='" + birds_attribute.getINV_PERSON() + "'"
                + ",WEATHER='" + birds_attribute.getWEATHER() + "'"
                + ",WIND='" + birds_attribute.getWIND() + "'"
                + ",WIND_DIRE='" + birds_attribute.getWIND_DIRE() + "'"
                + ",TEMPERATUR='" + birds_attribute.getTEMPERATUR() + "'"
                + ",NUM='" + birds_attribute.getNUM() + "'"
                + ",INV_TM='" + birds_attribute.getINV_TM() + "'"
                + ",SPEC_NM='" + birds_attribute.getSPEC_NM() + "'"
                + ",FAMI_NM='" + birds_attribute.getFAMI_NM() + "'"
                + ",SCIEN_NM='" + birds_attribute.getSCIEN_NM() + "'"
                + ",ENDANGERED='" + birds_attribute.getENDANGERED() + "'"
                + ",INDI_CNT='" + birds_attribute.getINDI_CNT() + "'"
                + ",OBS_STAT='" + birds_attribute.getOBS_STAT() + "'"
                + ",OBS_ST_ETC='" + birds_attribute.getOBS_ST_ETC() + "'"
                + ",USE_TAR='" + birds_attribute.getUSE_TAR() + "'"
                + ",USE_TAR_SP='" + birds_attribute.getUSE_TAR_SP() + "'"
                + ",USE_LAYER='" + birds_attribute.getUSE_LAYER() + "'"
                + ",MJ_ACT='" + birds_attribute.getMJ_ACT() + "'"
                + ",MJ_ACT_PR='" + birds_attribute.getMJ_ACT_PR() + "'"
                + ",GPS_LAT='" + birds_attribute.getGPS_LAT() + "'"
                + ",GPS_LON='" + birds_attribute.getGPS_LON() + "'"
                + ",TEMP_YN='" + birds_attribute.getTEMP_YN() + "'"
                + ",CONF_MOD='" + birds_attribute.getCONF_MOD() + "'"+
                 "where id = '" + pk + "'";
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




    public void updatebiotope_attribute(Biotope_attribute biotope_attribute,String page) {

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
                + ",CONF_MOD='" + biotope_attribute.getCONF_MOD() + "'"
                + ",TEMP_YN='" + biotope_attribute.getTEMP_YN() + "'"+
                "where id = '" + page + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updatemammal_attribute(Mammal_attribute mammal_attribute,String pk) {

        String query = "UPDATE mammalAttribute SET  " +
                "INV_REGION='" + mammal_attribute.getINV_REGION() + "'"
                + ",INV_DT='" + mammal_attribute.getINV_DT() + "'"
                + ",INV_PERSON='" + mammal_attribute.getINV_PERSON() + "'"
                + ",WEATHER='" + mammal_attribute.getWEATHER() + "'"
                + ",WIND='" + mammal_attribute.getWIND() + "'"
                + ",WIND_DIRE='" + mammal_attribute.getWIND_DIRE() + "'"
                + ",TEMPERATUR='" + mammal_attribute.getTEMPERATUR() + "'"
                + ",NUM='" + mammal_attribute.getNUM() + "'"
                + ",INV_TM='" + mammal_attribute.getINV_TM() + "'"
                + ",SPEC_NM='" + mammal_attribute.getSPEC_NM() + "'"
                + ",FAMI_NM='" + mammal_attribute.getFAMI_NM() + "'"
                + ",SCIEN_NM='" + mammal_attribute.getSCIEN_NM() + "'"
                + ",ENDANGERED='" + mammal_attribute.getENDANGERED() + "'"
                + ",OBS_TY='" + mammal_attribute.getOBS_TY() + "'"
                + ",OBS_TY_ETC='" + mammal_attribute.getOBS_TY_ETC() + "'"
                + ",INDI_CNT='" + mammal_attribute.getINDI_CNT() + "'"
                + ",OB_PT_CHAR='" + mammal_attribute.getOB_PT_CHAR() + "'"
                + ",UNUS_NOTE='" + mammal_attribute.getUNUS_NOTE() + "'"
                + ",GPS_LAT='" + mammal_attribute.getGPS_LAT() + "'"
                + ",GPS_LON='" + mammal_attribute.getGPS_LON() + "'"
                + ",UN_SPEC='" + mammal_attribute.getUN_SPEC() + "'"
                + ",UN_SPEC_RE='" + mammal_attribute.getUN_SPEC_RE() + "'"
                + ",TR_EASY='" + mammal_attribute.getTR_EASY() + "'"
                + ",TR_EASY_RE='" + mammal_attribute.getTR_EASY_RE() + "'"
                + ",TEMP_YN='" + mammal_attribute.getTEMP_YN() + "'"
                + ",CONF_MOD='" + mammal_attribute.getCONF_MOD() + "'"+
                "where id = '" + pk + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updateinsect_attribute(Insect_attribute insect_attribute,String pk) {

        String query = "UPDATE insectAttribute SET  " +
                "INV_REGION='" + insect_attribute.getINV_REGION() + "'"
                + ",INV_DT='" + insect_attribute.getINV_DT() + "'"
                + ",INV_PERSON='" + insect_attribute.getINV_PERSON() + "'"
                + ",WEATHER='" + insect_attribute.getWEATHER() + "'"
                + ",WIND='" + insect_attribute.getWIND() + "'"
                + ",WIND_DIRE='" + insect_attribute.getWIND_DIRE() + "'"
                + ",TEMPERATUR='" + insect_attribute.getTEMPERATUR() + "'"
                + ",NUM='" + insect_attribute.getNUM() + "'"
                + ",INV_TM='" + insect_attribute.getINV_TM() + "'"
                + ",SPEC_NM='" + insect_attribute.getSPEC_NM() + "'"
                + ",FAMI_NM='" + insect_attribute.getFAMI_NM() + "'"
                + ",SCIEN_NM='" + insect_attribute.getSCIEN_NM() + "'"
                + ",INDI_CNT='" + insect_attribute.getINDI_CNT() + "'"
                + ",OBS_STAT='" + insect_attribute.getOBS_STAT() + "'"
                + ",OBS_ST_ETC='" + insect_attribute.getOBS_ST_ETC() + "'"
                + ",USE_TAR='" + insect_attribute.getUSE_TAR() + "'"
                + ",USER_TA_ETC='" + insect_attribute.getUSER_TA_ETC() + "'"
                + ",MJ_ACT='" + insect_attribute.getMJ_ACT() + "'"
                + ",MJ_ACT_ETC='" + insect_attribute.getMJ_ACT_ETC() + "'"
                + ",INV_MEAN='" + insect_attribute.getINV_MEAN() + "'"
                + ",INV_MN_ETC='" + insect_attribute.getINV_MN_ETC() + "'"
                + ",UNUS_NOTE='" + insect_attribute.getUNUS_NOTE() + "'"
                + ",GPS_LAT='" + insect_attribute.getGPS_LAT() + "'"
                + ",GPS_LON='" + insect_attribute.getGPS_LON() + "'"
                + ",TEMP_YN='" + insect_attribute.getTEMP_YN() + "'"
                + ",CONF_MOD='" + insect_attribute.getCONF_MOD() + "'"+
                "where id = '" + pk + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updatefish_attribute(Fish_attribute fish_attribute,String pk) {

        String query = "UPDATE fishAttribute SET  " +
                "INV_REGION='" + fish_attribute.getINV_REGION() + "'"
                + ",INV_DT='" + fish_attribute.getINV_DT() + "'"
                + ",INV_TM='" + fish_attribute.getINV_TM() + "'"
                + ",INV_PERSON='" + fish_attribute.getINV_PERSON() + "'"
                + ",WEATHER='" + fish_attribute.getWEATHER() + "'"
                + ",WIND='" + fish_attribute.getWIND() + "'"
                + ",WIND_DIRE='" + fish_attribute.getWIND_DIRE() + "'"
                + ",TEMPERATUR='" + fish_attribute.getTEMPERATUR() + "'"
                + ",ETC='" + fish_attribute.getETC() + "'"
                + ",MID_RAGE='" + fish_attribute.getMID_RAGE() + "'"
                + ",CODE_NUM='" + fish_attribute.getCODE_NUM() + "'"
                + ",RIVER_NUM='" + fish_attribute.getRIVER_NUM() + "'"
                + ",RIVER_NM='" + fish_attribute.getRIVER_NM() + "'"
                + ",NET_CNT='" + fish_attribute.getNET_CNT() + "'"
                + ",NET_MIN='" + fish_attribute.getNET_MIN() + "'"
                + ",AD_DIST_NM='" + fish_attribute.getAD_DIST_NM() + "'"
                + ",GPS_LAT='" + fish_attribute.getGPS_LAT() + "'"
                + ",GPS_LON='" + fish_attribute.getGPS_LON() + "'"
                + ",COLL_TOOL='" + fish_attribute.getCOLL_TOOL() + "'"
                + ",STREAM_W='" + fish_attribute.getSTREAM_W() + "'"
                + ",WATER_W='" + fish_attribute.getWATER_W() + "'"
                + ",WATER_D='" + fish_attribute.getWATER_D() + "'"
                + ",WATER_CUR='" + fish_attribute.getWATER_CUR() + "'"
                + ",RIV_STR='" + fish_attribute.getRIV_STR() + "'"
                + ",RIV_STR_IN='" + fish_attribute.getRIV_STR_IN() + "'"
                + ",RIV_FORM='" + fish_attribute.getRIV_FORM() + "'"
                + ",NUM='" + fish_attribute.getNUM() + "'"
                + ",SPEC_NM='" + fish_attribute.getSPEC_NM() + "'"
                + ",FAMI_NM='" + fish_attribute.getFAMI_NM() + "'"
                + ",SCIEN_NM='" + fish_attribute.getSCIEN_NM() + "'"
                + ",INDI_CNT='" + fish_attribute.getINDI_CNT() + "'"
                + ",UNIDENT='" + fish_attribute.getUNIDENT() + "'"
                + ",RIV_FM_CH='" + fish_attribute.getRIV_FM_CH() + "'"
                + ",UN_FISH_CH='" + fish_attribute.getUN_FISH_CH() + "'"
                + ",TEMP_YN='" + fish_attribute.getTEMP_YN() + "'"
                + ",TEMP_YN='" + fish_attribute.getCONF_MOD() + "'"+

                "where id = '" + pk + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updateflora_attribute(Flora_Attribute flora_attribute,String pk) {

        String query = "UPDATE floraAttribute SET  " +
                "INV_REGION='" + flora_attribute.getINV_REGION() + "'"
                + ",INV_DT='" + flora_attribute.getINV_DT() + "'"
                + ",INV_PERSON='" + flora_attribute.getINV_PERSON() + "'"
                + ",WEATHER='" + flora_attribute.getWEATHER() + "'"
                + ",WIND='" + flora_attribute.getWIND() + "'"
                + ",WIND_DIRE='" + flora_attribute.getWIND_DIRE() + "'"
                + ",TEMPERATUR='" + flora_attribute.getTEMPERATUR() + "'"
                + ",ETC='" + flora_attribute.getETC() + "'"
                + ",NUM='" + flora_attribute.getNUM() + "'"
                + ",INV_TM='" + flora_attribute.getINV_TM() + "'"
                + ",SPEC_NM='" + flora_attribute.getSPEC_NM() + "'"
                + ",FAMI_NM='" + flora_attribute.getFAMI_NM() + "'"
                + ",SCIEN_NM='" + flora_attribute.getSCIEN_NM() + "'"
                + ",FLORE_YN='" + flora_attribute.getFLORE_YN() + "'"
                + ",PLANT_YN='" + flora_attribute.getPLANT_YN() + "'"
                + ",HAB_STAT='" + flora_attribute.getHAB_STAT() + "'"
                + ",HAB_ETC='" + flora_attribute.getHAB_ETC() + "'"
                + ",COL_IN_CNT='" + flora_attribute.getCOL_IN_CNT() + "'"
                + ",THRE_CAU='" + flora_attribute.getTHRE_CAU() + "'"
                + ",GPS_LAT='" + flora_attribute.getGPS_LAT() + "'"
                + ",GPS_LON='" + flora_attribute.getGPS_LON() + "'"
                + ",TEMP_YN='" + flora_attribute.getTEMP_YN() + "'"
                + ",CONF_MOD='" + flora_attribute.getCONF_MOD() + "'"+

                "where id = '" + pk + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updatezoobenthous_attribute(Zoobenthos_Attribute Zoobenthos_Attribute,String pk) {

        String query = "UPDATE ZoobenthosAttribute SET  " +
                "INV_MEAN='" + Zoobenthos_Attribute.getINV_MEAN() + "'"
                + ",INV_DT='" + Zoobenthos_Attribute.getINV_DT() + "'"
                + ",MAP_SYS_NM='" + Zoobenthos_Attribute.getMAP_SYS_NM() + "'"
                + ",COORD_N_D='" + Zoobenthos_Attribute.getCOORD_N_D() + "'"
                + ",COORD_N_M='" + Zoobenthos_Attribute.getCOORD_N_M() + "'"
                + ",COORD_N_S='" + Zoobenthos_Attribute.getCOORD_N_S() + "'"
                + ",COORD_E_D='" + Zoobenthos_Attribute.getCOORD_E_D() + "'"
                + ",COORD_E_M='" + Zoobenthos_Attribute.getCOORD_E_M() + "'"
                + ",COORD_E_S='" + Zoobenthos_Attribute.getCOORD_E_S() + "'"
                + ",INV_DT='" + Zoobenthos_Attribute.getINV_DT() + "'"
                + ",NUM='" + Zoobenthos_Attribute.getNUM() + "'"
                + ",INV_TM='" + Zoobenthos_Attribute.getINV_TM() + "'"
                + ",WEATHER='" + Zoobenthos_Attribute.getWEATHER() + "'"
                + ",INV_TOOL='" + Zoobenthos_Attribute.getINV_TOOL() + "'"
                + ",AD_DIST_NM='" + Zoobenthos_Attribute.getAD_DIST_NM() + "'"
                + ",RIV_W1='" + Zoobenthos_Attribute.getRIV_W1() + "'"
                + ",RIV_W2='" + Zoobenthos_Attribute.getRIV_W2() + "'"
                + ",RUN_RIV_W1='" + Zoobenthos_Attribute.getRUN_RIV_W1() + "'"
                + ",RUN_RIV_W2='" + Zoobenthos_Attribute.getRUN_RIV_W2() + "'"
                + ",WATER_DEPT='" + Zoobenthos_Attribute.getWATER_DEPT() + "'"
                + ",HAB_TY='" + Zoobenthos_Attribute.getHAB_TY() + "'"
                + ",FILT_AREA='" + Zoobenthos_Attribute.getFILT_AREA() + "'"
                + ",TEMPERATUR='" + Zoobenthos_Attribute.getTEMPERATUR() + "'"
                + ",WATER_TEM='" + Zoobenthos_Attribute.getWATER_TEM() + "'"
                + ",TURBIDITY='" + Zoobenthos_Attribute.getTURBIDITY() + "'"
                + ",MUD='" + Zoobenthos_Attribute.getMUD() + "'"
                + ",SAND='" + Zoobenthos_Attribute.getSAND() + "'"
                + ",COR_SAND='" + Zoobenthos_Attribute.getCOR_SAND() + "'"
                + ",GRAVEL='" + Zoobenthos_Attribute.getGRAVEL() + "'"
                + ",STONE_S='" + Zoobenthos_Attribute.getSTONE_S() + "'"
                + ",STONE_B='" + Zoobenthos_Attribute.getSTONE_B() + "'"
                + ",CONCRETE='" + Zoobenthos_Attribute.getCONCRETE() + "'"
                + ",BED_ROCK='" + Zoobenthos_Attribute.getBED_ROCK() + "'"
                + ",BANK_L='" + Zoobenthos_Attribute.getBANK_L() + "'"
                + ",BANK_L_ETC='" + Zoobenthos_Attribute.getBANK_L_ETC() + "'"
                + ",BANK_R='" + Zoobenthos_Attribute.getBANK_R() + "'"
                + ",BANK_R_ETC='" + Zoobenthos_Attribute.getBANK_R_ETC() + "'"
                + ",BAS_L='" + Zoobenthos_Attribute.getBAS_L() + "'"
                + ",BAS_L_ETC='" + Zoobenthos_Attribute.getBAS_L_ETC() + "'"
                + ",BAS_R='" + Zoobenthos_Attribute.getBAS_R() + "'"
                + ",BAS_R_ETC='" + Zoobenthos_Attribute.getBAS_R_ETC() + "'"
                + ",DIST_CAU='" + Zoobenthos_Attribute.getDIST_CAU() + "'"
                + ",DIST_ETC='" + Zoobenthos_Attribute.getDIST_ETC() + "'"
                + ",UNUS_NOTE='" + Zoobenthos_Attribute.getUNUS_NOTE() + "'"
                + ",GPS_LAT='" + Zoobenthos_Attribute.getGPS_LAT() + "'"
                + ",GPS_LON='" + Zoobenthos_Attribute.getGPS_LON() + "'"
                + ",SPEC_NM='" + Zoobenthos_Attribute.getSPEC_NM() + "'"
                + ",FAMI_NM='" + Zoobenthos_Attribute.getFAMI_NM() + "'"
                + ",SCIEN_NM='" + Zoobenthos_Attribute.getSCIEN_NM() + "'"
                + ",TEMP_YN='" + Zoobenthos_Attribute.getTEMP_YN() + "'"
                + ",CONF_MOD='" + Zoobenthos_Attribute.getCONF_MOD() + "'"+

                "where id = '" + pk + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }


    public void updatemanyflora_attribute(ManyFloraAttribute ManyFloraAttribute,String pk) {

        String query = "UPDATE ManyFloraAttribute SET  " +
                "INV_DT='" + ManyFloraAttribute.getINV_DT() + "'"
                + ",INV_DT='" + ManyFloraAttribute.getINV_DT() + "'"
                + ",INV_TM='" + ManyFloraAttribute.getINV_TM() + "'"
                + ",TRE_NUM='" + ManyFloraAttribute.getTRE_NUM() + "'"
                + ",TRE_SPEC='" + ManyFloraAttribute.getTRE_SPEC() + "'"
                + ",TRE_FAMI='" + ManyFloraAttribute.getTRE_FAMI() + "'"
                + ",TRE_SCIEN='" + ManyFloraAttribute.getTRE_SCIEN() + "'"
                + ",TRE_H='" + ManyFloraAttribute.getTRE_H() + "'"
                + ",TRE_BREA='" + ManyFloraAttribute.getTRE_BREA() + "'"
                + ",TRE_COVE='" + ManyFloraAttribute.getTRE_COVE() + "'"
                + ",STRE_NUM='" + ManyFloraAttribute.getSTRE_NUM() + "'"
                + ",STRE_SPEC='" + ManyFloraAttribute.getSTRE_SPEC() + "'"
                + ",STRE_FAMI='" + ManyFloraAttribute.getSTRE_FAMI() + "'"
                + ",STRE_SCIEN='" + ManyFloraAttribute.getSTRE_SCIEN() + "'"
                + ",STRE_H='" + ManyFloraAttribute.getSTRE_H() + "'"
                + ",STRE_BREA='" + ManyFloraAttribute.getSTRE_BREA() + "'"
                + ",STRE_COVE='" + ManyFloraAttribute.getSTRE_COVE() + "'"
                + ",SHR_NUM='" + ManyFloraAttribute.getSHR_NUM() + "'"
                + ",SHR_SPEC='" + ManyFloraAttribute.getSHR_SPEC() + "'"
                + ",SHR_FAMI='" + ManyFloraAttribute.getSHR_FAMI() + "'"
                + ",SHR_SCIEN='" + ManyFloraAttribute.getSHR_SCIEN() + "'"
                + ",SHR_H='" + ManyFloraAttribute.getSHR_H() + "'"
                + ",SHR_COVE='" + ManyFloraAttribute.getSHR_COVE() + "'"
                + ",HER_NUM='" + ManyFloraAttribute.getHER_NUM() + "'"
                + ",HER_SPEC='" + ManyFloraAttribute.getHER_SPEC() + "'"
                + ",HER_FAMI='" + ManyFloraAttribute.getHER_FAMI() + "'"
                + ",HER_SCIEN='" + ManyFloraAttribute.getHER_SCIEN() + "'"
                + ",HER_H='" + ManyFloraAttribute.getHER_H() + "'"
                + ",HER_COVE='" + ManyFloraAttribute.getHER_COVE() + "'"
                + ",GPS_LAT='" + ManyFloraAttribute.getGPS_LAT() + "'"
                + ",GPS_LON='" + ManyFloraAttribute.getGPS_LON() + "'"
                + ",TEMP_YN='" + ManyFloraAttribute.getTEMP_YN() + "'"
                + ",CONF_MOD='" + ManyFloraAttribute.getCONF_MOD() + "'"+

                "where id = '" + pk + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updatestockmap(StockMap StockMap,String pk) {

        String query = "UPDATE StockMap SET  " +
                "INV_REGION='" + StockMap.getINV_REGION() + "'"
                + ",INV_DT='" + StockMap.getINV_DT() + "'"
                + ",INV_TM='" + StockMap.getINV_TM() + "'"
                + ",NUM='" + StockMap.getNUM() + "'"
                + ",FRTP_CD='" + StockMap.getFRTP_CD() + "'"
                + ",KOFTR_GROUP_CD='" + StockMap.getKOFTR_GROUP_CD() + "'"
                + ",STORUNST_CD='" + StockMap.getSTORUNST_CD() + "'"
                + ",FROR_CD='" + StockMap.getFROR_CD() + "'"
                + ",DMCLS_CD='" + StockMap.getDMCLS_CD() + "'"
                + ",AGCLS_CD='" + StockMap.getAGCLS_CD() + "'"
                + ",DNST_CD='" + StockMap.getDNST_CD() + "'"
                + ",HEIGHT='" + StockMap.getHEIGHT() + "'"
                + ",LDMARK_STNDA_CD='" + StockMap.getLDMARK_STNDA_CD() + "'"
                + ",MAP_LABEL='" + StockMap.getMAP_LABEL() + "'"
                + ",MAP_LABEL2='" + StockMap.getMAP_LABEL2() + "'"
                + ",ETC_PCMTT='" + StockMap.getETC_PCMTT() + "'"
                + ",GPS_LAT='" + StockMap.getGPS_LAT() + "'"
                + ",GPS_LON='" + StockMap.getGPS_LON() + "'"
                + ",CONF_MOD='" + StockMap.getCONF_MOD() + "'"+
                "where id = '" + pk + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updatefloratre(TreeData1 TreeData1, String GROP_ID, int TRE_NUM) {

        String query = "UPDATE ManyFloraAttribute SET  " +
                  "TRE_NUM='" + TreeData1.getPAGE() + "'"
                + ",TRE_SPEC='" + TreeData1.getSPEC() + "'"
                + ",TRE_FAMI='" + TreeData1.getFAMI() + "'"
                + ",TRE_SCIEN='" + TreeData1.getSCIEN() + "'"
                + ",TRE_H='" + TreeData1.getH() + "'"
                + ",TRE_BREA='" + TreeData1.getBREA() + "'"
                + ",TRE_COVE='" + TreeData1.getCOVE() + "'" +

                "where GROP_ID = '" + GROP_ID + "' and TRE_NUM = '" + TRE_NUM + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updateflorastre(ManyFloraAttribute ManyFloraAttribute,String GROP_ID,int STRE_NUM) {

        String query = "UPDATE ManyFloraAttribute SET  " +
                "STRE_NUM='" + ManyFloraAttribute.getSTRE_NUM() + "'"
                + ",STRE_SPEC='" + ManyFloraAttribute.getSTRE_SPEC() + "'"
                + ",STRE_FAMI='" + ManyFloraAttribute.getSTRE_FAMI() + "'"
                + ",STRE_SCIEN='" + ManyFloraAttribute.getSTRE_SCIEN() + "'"
                + ",STRE_H='" + ManyFloraAttribute.getSTRE_H() + "'"
                + ",STRE_BREA='" + ManyFloraAttribute.getSTRE_BREA() + "'"
                + ",STRE_COVE='" + ManyFloraAttribute.getSTRE_COVE() + "'" +

                "where GROP_ID = '" + GROP_ID + "' and STRE_NUM = '" + STRE_NUM + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    public void updateflorashr(ManyFloraAttribute ManyFloraAttribute,String GROP_ID,int SHR_NUM) {

        String query = "UPDATE ManyFloraAttribute SET  " +
                "SHR_NUM='" + ManyFloraAttribute.getSHR_NUM() + "'"
                + ",SHR_SPEC='" + ManyFloraAttribute.getSHR_SPEC() + "'"
                + ",SHR_FAMI='" + ManyFloraAttribute.getSHR_FAMI() + "'"
                + ",SHR_SCIEN='" + ManyFloraAttribute.getSHR_SCIEN() + "'"
                + ",SHR_H='" + ManyFloraAttribute.getSHR_H() + "'"
                + ",SHR_COVE='" + ManyFloraAttribute.getSHR_COVE() + "'" +

                "where GROP_ID = '" + GROP_ID + "' and SHR_NUM = '" + SHR_NUM + "'";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();

    }
    public void updatefloraher(ManyFloraAttribute ManyFloraAttribute,String GROP_ID,int HER_NUM) {

        String query = "UPDATE ManyFloraAttribute SET  " +
                "HER_NUM='" + ManyFloraAttribute.getHER_NUM() + "'"
                + ",HER_SPEC='" + ManyFloraAttribute.getHER_SPEC() + "'"
                + ",HER_FAMI='" + ManyFloraAttribute.getHER_FAMI() + "'"
                + ",HER_SCIEN='" + ManyFloraAttribute.getHER_SCIEN() + "'"
                + ",HER_H='" + ManyFloraAttribute.getHER_H() + "'"
                + ",HER_COVE='" + ManyFloraAttribute.getHER_COVE() + "'" +

                "where GROP_ID = '" + GROP_ID + "' and HER_NUM = '" + HER_NUM + "'";
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



    public int biotopesNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(INV_INDEX\n ,9,15)),0)+1 ,-15, 15) FROM biotopeAttribute";

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

    public int birdsNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM\n ,9,15)),0)+1 ,-15, 15) FROM birdsAttribute";

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

    public int reptiliasNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM\n ,9,15)),0)+1 ,-15, 15) FROM reptiliaAttribute";

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

    public int mammalsNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM\n ,9,15)),0)+1 ,-15, 15) FROM mammalAttribute";

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

    public int fishsNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM\n ,9,15)),0)+1 ,-15, 15) FROM fishAttribute";

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

    public int insectsNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM\n ,9,15)),0)+1 ,-15, 15) FROM insectAttribute";

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

    public int floraNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM\n ,9,15)),0)+1 ,-15, 15) FROM floraAttribute";

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

    public int zoobenthosNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM\n ,9,15)),0)+1 ,-15, 15) FROM ZoobenthosAttribute";

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

    public int stockmapNextNum(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(NUM\n ,9,15)),0)+1 ,-15, 15) FROM StockMap";

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

    public int manyfloratrenumNext(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(TRE_NUM\n ,9,15)),0)+1 ,-15, 15) FROM ManyFloraAttribute";

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

    public int manyflorastrenumNext(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(STRE_NUM\n ,9,15)),0)+1 ,-15, 15) FROM ManyFloraAttribute";

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

    public int manyflorashrnumNext(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(SHR_NUM\n ,9,15)),0)+1 ,-15, 15) FROM ManyFloraAttribute";

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

    public int manyflorahernumNext(){

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT strftime('%Y%m%d','now','localtime') ||  substr('000' || IFNULL(MAX(substr(HER_NUM\n ,9,15)),0)+1 ,-15, 15) FROM ManyFloraAttribute";

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
