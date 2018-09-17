package hntecology.ecology

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.ViewParent
import android.widget.Switch
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeModel
import hntecology.ecology.model.Biotope_attribute
import kotlinx.android.synthetic.main.activity_biotope.*
import java.text.SimpleDateFormat
import java.util.*
import android.view.WindowManager
import hntecology.ecology.R.style.BiotopeEditText
import org.locationtech.jts.geom.Dimension.P
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.os.*
import android.preference.PreferenceActivity
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.fido.u2f.api.common.RequestParams
import com.google.android.gms.location.*
import hntecology.ecology.R.id.*
import hntecology.ecology.base.PrefUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import android.provider.Settings
import org.locationtech.jts.linearref.LengthLocationMap.getLocation

@Suppress("DEPRECATION")
class BiotopeActivity : Activity(),com.google.android.gms.location.LocationListener{

    //gps 관련
    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */


    val SET_DATA1 = 1;
    var keyId:String?=null;
    var chkdata:Boolean =false;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biotope)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);
        buildGoogleApiClient();

        window.setLayout(Utils.dpToPx(700f).toInt(),WindowManager.LayoutParams.WRAP_CONTENT);

        etinvesDatetimeTV.text = getTime()


        val dbManager:DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        var intent:Intent = getIntent();




        if(intent.getSerializableExtra("id") !=null){

            keyId = intent.getStringExtra("id")
            val dataList:Array<String> = arrayOf("*");

            val data =  db.query("biotopeAttribute",dataList,"id = '"+keyId+"'",null,null,null,"",null);

            while (data.moveToNext()){

                chkdata = true
                var biotope_attribute:Biotope_attribute = Biotope_attribute(data.getString(0),data.getString(1),data.getString(2),data.getString(3),data.getInt(4),data.getString(5),data.getFloat(6),
                                                                            data.getFloat(7),data.getString(8),data.getString(9),data.getString(10),data.getFloat(11),data.getString(12),data.getString(13)
                                                                            ,data.getString(14),data.getString(15),data.getString(16),data.getString(17),data.getString(18),data.getString(19),data.getString(20)
                                                                            ,data.getFloat(21),data.getFloat(22),data.getFloat(23),data.getString(24),data.getFloat(25),data.getFloat(26),data.getFloat(27)
                                                                            ,data.getString(28),data.getFloat(29),data.getFloat(30),data.getString(31),data.getFloat(32),data.getFloat(33),data.getString(34)
                                                                            ,data.getString(35),data.getString(36),data.getString(37),data.getString(38))

//                etinvesRegionET.text        = biotope_attribute.INVES_REGION
                etinvesRegionET.setText(biotope_attribute.INVES_REGION);
                tvinvestigatorTV.setText(biotope_attribute.INVESTIGATOR)
                etinvesDatetimeTV.setText(biotope_attribute.INVES_DATETIME)
                tvinvesIndexTV.setText(biotope_attribute.INVES_INDEX.toString())
                ETlumGroupNumET.setText(biotope_attribute.LUM_GROUP_NUM)
                etlumTypeRateET.setText(biotope_attribute.LUM_TYPE_RATE.toString())
                etstandardHeightET.setText(biotope_attribute.STANDARD_HEIGHT.toString())
                ETlcmGroupNumET.setText(biotope_attribute.LCM_GROUP_NUM)

                if(biotope_attribute.LUM_GROUP_NUM != null){

                    val dataSelectList:Array<String> = arrayOf("name");
                    val data =  db.query("biotopeM",dataList,"code = '"+biotope_attribute.LUM_GROUP_NUM+"'",null,null,null,"",null);


                    while (data.moveToNext()){

                        TVlumGroupNumTV.setText(data.getString(0))
                    }
                }

                if(biotope_attribute.LCM_GROUP_NUM !=null){

                    val dataSelectList:Array<String> = arrayOf("name");
                    val data =  db.query("biotopeS",dataList,"code = '"+biotope_attribute.LCM_GROUP_NUM+"'",null,null,null,"",null);


                    while (data.moveToNext()){

                        TVlcmGroupNumTV.setText(data.getString(0))
                    }
                }

                //투수
                if(biotope_attribute.LCM_TYPE == "P"){

                    etlcmTypepET.setText(biotope_attribute.LCM_TYPE)
                //불투수
                } else if (biotope_attribute.LCM_TYPE == "I"){

                    etlcmTypeiET.setText(biotope_attribute.LCM_TYPE)
                //녹지
                } else if ( biotope_attribute.LCM_TYPE == "G"){

                    etlcmTypegET.setText(biotope_attribute.LCM_TYPE)
                //수공간
                } else if (biotope_attribute.LCM_TYPE == "W"){

                    etlcmTypewET.setText(biotope_attribute.LCM_TYPE)
                }


                ETtypeMarkET.setText(biotope_attribute.TYPE_MARK)
                etgvRateET.setText(biotope_attribute.GV_RATE.toString())
                etgvStructureET.setText(biotope_attribute.GV_STRUCTURE)
                etdistReturnET.setText(biotope_attribute.DIST_RETURN)
                etrestorePotET.setText(biotope_attribute.RESTORE_POT)
                etcompIntactET.setText(biotope_attribute.COMP_INTACT)
                etvpIntactET.setText(biotope_attribute.VP_INTACT)
                etimpFormET.setText(biotope_attribute.IMP_FORM)
                etbreastDiaET.setText(biotope_attribute.BREAST_DIA)
                etfinalEstET.setText(biotope_attribute.FINAL_EST)
                ettreeSpeciesET.setText(biotope_attribute.TREE_SPECIES)
                ettreeHeightET.setText(biotope_attribute.TREE_HEIGHT.toString())
                ettreeBreastET.setText(biotope_attribute.TREE_BREAST.toString())
                ettreeCoveET.setText(biotope_attribute.TREE_COVE.toString())
                etsubTreeSpecET.setText(biotope_attribute.SUB_TREE_SPEC)
                etsubTreeHeightET.setText(biotope_attribute.SUB_TREE_HEIGHT.toString())
                etsubTreeBreastET.setText(biotope_attribute.SUB_TREE_BREAST.toString())
                etsubTreeCoverET.setText(biotope_attribute.SUB_TREE_COVER.toString())
                etshrubSpeciesET.setText(biotope_attribute.SHRUB_SPECIES)
                etshrubHeightET.setText(biotope_attribute.SHRUB_HEIGHT.toString())
                etshrubCoverET.setText(biotope_attribute.SHRUB_COVER.toString())
                etherbSpeciesET.setText(biotope_attribute.HERB_SPECIES)
                etherbHeightET.setText(biotope_attribute.HERB_HEIGHT.toString())
                etherbCoverET.setText(biotope_attribute.HERB_COVER.toString())
                etpictureFolderET.setText(biotope_attribute.PICTURE_FOLDER)
                etwildAniET.setText(biotope_attribute.WILD_ANI)
                etrepBiotopPotET.setText(biotope_attribute.REP_BIOTOP_POT)
                etunusualNoteET.setText(biotope_attribute.UNUSUAL_NOTE)
            }
        }

        //토지이용현황 분류 버튼  높이 450f
        btn_Dlg1.setOnClickListener {

            val intent = Intent(this,DlgCommonActivity::class.java)
            intent.putExtra("title","토지이용유형 분류기준")
            intent.putExtra("table","biotopeM")
            intent.putExtra("DlgHeight",450f);
//            startActivity(intent)
            startActivityForResult(intent, SET_DATA1);

        }
        //토지피복현황 분류 버튼 사이즈 높이 600f 줄 것.
        btn_Dlg2.setOnClickListener {

            val intent = Intent(this,DlgCommonActivity::class.java)
            intent.putExtra("title","토지피복현황 분류기준")
            intent.putExtra("table","biotopeS")
            intent.putExtra("DlgHeight",600f);
            startActivityForResult(intent, SET_DATA1);

        }
        //현존식생현황 분류 버튼
/*        btn_Dlg3.setOnClickListener {

            val intent = Intent(this,DlgCommonActivity::class.java)
            intent.putExtra("title","현존식생현황 분류기준")
            //intent.putExtra("table","biotopeM") 아직 코드 미정.
            startActivityForResult(intent, SET_DATA1);

        }*/
        //취소버튼
        btn_biotopCancle1.setOnClickListener {
            finish()
        }

        //sqlite 저장.
        btn_biotopSave1.setOnClickListener {

            getGps()

            var intent = Intent();
            val biotope_attribute:Biotope_attribute = Biotope_attribute(null,"","","",0,"",null
                    ,null,"","","",null,"","","",""
                    ,"","","","","",null,null,null,"",null
                    ,null,null,"",null,null,"",null,null,""
                    ,"","","",null);


            biotope_attribute.INVES_REGION         =   etinvesRegionET.text.toString()
            biotope_attribute.INVESTIGATOR         =   PrefUtils.getStringPreference(this,"name");
            biotope_attribute.INVES_DATETIME       =   etinvesDatetimeTV.text.toString()

            if(tvinvesIndexTV.text.toString() != ""){

                biotope_attribute.INVES_INDEX          =    tvinvesIndexTV.text.toString().toInt()
            }

            biotope_attribute.LUM_GROUP_NUM        =   ETlumGroupNumET.text.toString()

            if(etlumTypeRateET.text .toString() != ""){

                biotope_attribute.LUM_TYPE_RATE        =   Utils.getString(etlumTypeRateET).toFloat();
            }
            if(etstandardHeightET.text .toString() != ""){

                biotope_attribute.STANDARD_HEIGHT      =    Utils.getString(etstandardHeightET).toFloat();
            }


            biotope_attribute.LCM_GROUP_NUM        =   ETlcmGroupNumET.text.toString()
            biotope_attribute.TYPE_MARK            =   ETtypeMarkET.text.toString()

            if(etgvRateET.text .toString() != ""){

                biotope_attribute.GV_RATE              =  Utils.getString(etgvRateET).toFloat();
            }

            biotope_attribute.GV_STRUCTURE         =   etgvStructureET.text.toString()
            biotope_attribute.DIST_RETURN          =   etdistReturnET.text.toString()
            biotope_attribute.RESTORE_POT          =   etrestorePotET.text.toString()
            biotope_attribute.COMP_INTACT          =   etcompIntactET.text.toString()
            biotope_attribute.VP_INTACT            =   etvpIntactET.text.toString()
            biotope_attribute.IMP_FORM             =   etimpFormET.text.toString()
            biotope_attribute.BREAST_DIA           =   etbreastDiaET.text.toString()
            biotope_attribute.FINAL_EST            =   etfinalEstET.text.toString()
            biotope_attribute.TREE_SPECIES         =   ettreeSpeciesET.text.toString()

            if(ettreeHeightET.text .toString() != ""){

                biotope_attribute.TREE_HEIGHT          = Utils.getString(ettreeHeightET).toFloat();
            }

            if(ettreeBreastET.text .toString() != ""){

                biotope_attribute.TREE_BREAST          =   Utils.getString(ettreeBreastET).toFloat();

            }

            if(ettreeCoveET.text .toString() != ""){

                biotope_attribute.TREE_COVE            =   Utils.getString(ettreeCoveET).toFloat();

            }

            biotope_attribute.SUB_TREE_SPEC        =   etsubTreeSpecET.text.toString()

            if(etsubTreeHeightET.text .toString() != ""){

                biotope_attribute.SUB_TREE_HEIGHT      =   Utils.getString(etsubTreeHeightET).toFloat();
            }

            if(etsubTreeBreastET.text .toString() != ""){

                biotope_attribute.SUB_TREE_BREAST      =   Utils.getString(etsubTreeBreastET).toFloat();
            }

            if(etsubTreeCoverET.text .toString() != ""){

                biotope_attribute.SUB_TREE_COVER       =   Utils.getString(etsubTreeCoverET).toFloat();
            }

            biotope_attribute.SHRUB_SPECIES        =   etshrubSpeciesET.text.toString()

            if(etshrubHeightET.text .toString() != ""){

                biotope_attribute.SHRUB_HEIGHT         =   Utils.getString(etshrubHeightET).toFloat();
            }

            if(etshrubCoverET.text .toString() != ""){

                biotope_attribute.SHRUB_COVER          =   Utils.getString(etshrubCoverET).toFloat();
            }

            biotope_attribute.HERB_SPECIES         =   etherbSpeciesET.text.toString()

            if(etherbHeightET.text .toString() != ""){

                biotope_attribute.HERB_HEIGHT          =   Utils.getString(etherbHeightET).toFloat();

            }

            if(etherbCoverET.text .toString() != ""){

                biotope_attribute.HERB_COVER           =    Utils.getString(etherbCoverET).toFloat();
            }


            biotope_attribute.PICTURE_FOLDER       =   etpictureFolderET.text.toString()
            biotope_attribute.WILD_ANI             =   etwildAniET.text.toString()
            biotope_attribute.REP_BIOTOP_POT       =   etrepBiotopPotET.text.toString()
            biotope_attribute.UNUSUAL_NOTE         =   etunusualNoteET.text.toString()

            //투수
            if(etlcmTypepET.text.toString() != ""){

                biotope_attribute.LCM_TYPE = etlcmTypepET.text.toString()
                //불투수
            } else if (etlcmTypeiET.text.toString() != ""){

                biotope_attribute.LCM_TYPE = etlcmTypeiET.text.toString()
                //녹지
            } else if ( etlcmTypegET.text.toString() != ""){

                biotope_attribute.LCM_TYPE = etlcmTypegET.text.toString()
                //수공간
            } else if (etlcmTypewET.text.toString() != ""){

                biotope_attribute.LCM_TYPE = etlcmTypewET.text.toString()
            }
            biotope_attribute.id = keyId;

            if(chkdata){

                dbManager.updatebiotope_attribute(biotope_attribute)
            }else {
                biotope_attribute.point_gps = etpointGpsET.text.toString();
                dbManager.insertbiotope_attribute(biotope_attribute);

            }
            intent.putExtra("bio_attri",biotope_attribute);

            setResult(RESULT_OK, intent);
            finish()
        }


        btn_biotopDelete.setOnClickListener {

            var intent = Intent();
            val biotope_attribute:Biotope_attribute = Biotope_attribute(null,"","","",0,"",null
                    ,null,"","","",null,"","","",""
                    ,"","","","","",null,null,null,"",null
                    ,null,null,"",null,null,"",null,null,""
                    ,"","","",null);

            biotope_attribute.id = keyId;


            dbManager.deletebiotope_attribute(biotope_attribute)

            intent.putExtra("bio_attri",biotope_attribute);

            setResult(RESULT_OK, intent);
            finish()

        }





    }

    fun getGps(){


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                getLocation();
            } else {
                //Request Location Permission
                checkLocationPermission()
            }

        } else {
            getLocation();
        }

    }

    fun getTime() :String{


        val date = Date()
        val fullTime = SimpleDateFormat("yyyy-MM-dd")


        return fullTime.format(date).toString()
    }
    fun createId():String{

        val date = Date()
        val fullTime = SimpleDateFormat("yyyyMMddHHmmssSSS")

        return fullTime.format(date).toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        var biotopeModel:BiotopeModel

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SET_DATA1 -> {

                    biotopeModel = data!!.getSerializableExtra("bioModel") as BiotopeModel

                    //토지이용현황
                    if(biotopeModel.codeType == "biotopeM"){

                        TVlumGroupNumTV.setText( biotopeModel.name)
                        ETlumGroupNumET.setText(biotopeModel.code)
                        //토지피복현황
                    }else if (biotopeModel.codeType == "biotopeS"){

                        TVlcmGroupNumTV.setText( biotopeModel.name)
                        ETlcmGroupNumET.setText(biotopeModel.code)

                        var bioModelParent:BiotopeModel
                        bioModelParent = data!!.getSerializableExtra("bioModelParent") as BiotopeModel

                        //불투수 투수
                        etlcmTypeiET.setText("");
                        etlcmTypepET.setText("")
                        etlcmTypegET.setText("")
                        etlcmTypewET.setText("")

                        //불투수
                        if(bioModelParent.code == "A"){

                            etlcmTypeiET.setText("I")
                        //투수
                        }else if(bioModelParent.code == "B"){

                            etlcmTypepET.setText("P")
                         //녹지
                        }else if(bioModelParent.code == "C"){

                            etlcmTypegET.setText("G")
                         //수공간
                        }else if(bioModelParent.code == "D"){

                            etlcmTypewET.setText("W")
                        }

                        //현존식생현황  아직 테이블 명 코드 미정
                    } else if (biotopeModel.codeType == "biotopeS"){

                        TVlcmGroupNumTV.setText( biotopeModel.name)
                        ETlcmGroupNumET.setText(biotopeModel.code)
                    }

                };
            }
        }
    }
    fun getAttrubuteKey():String{

        val time = System.currentTimeMillis()
        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val strDT = dayTime.format(Date(time))

        return strDT
    }

        /*
        *  gps function
        * */

    override fun onLocationChanged(location: Location?) {
        // You can now create a LatLng Object for use with maps
        // val latLng = LatLng(location.latitude, location.longitude)
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {
//            tvLatitude.text =mLocation!!.latitude.toString()
//            tvLongitude.text = mLocation!!.longitude.toString()
            etpointGpsET.setText (mLocation!!.latitude.toString() + "," +mLocation!!.longitude.toString());
//            Toast.makeText(this, "성공"+mLocation!!.latitude.toString(), Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    private fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
    }


    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient!!.connect()
    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun showAlert() {
        val dialog = android.support.v7.app.AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                android.support.v7.app.AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                        })
                        .create()
                        .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
//                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }
}