package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.location.Location
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.gdal.ogr.ogr
import org.geotools.data.DataUtilities
import org.geotools.data.DefaultTransaction
import org.geotools.data.Transaction
import org.geotools.data.collection.ListFeatureCollection
import org.geotools.data.shapefile.ShapefileDataStore
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.geotools.data.simple.SimpleFeatureStore
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.json.JSONObject
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile
import org.opengis.feature.simple.SimpleFeature
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


public class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, View.OnTouchListener, GoogleMap.OnCameraMoveListener, OnLocationUpdatedListener {

    companion object {
        val REQUEST_FINE_LOCATION = 1
        val REQUEST_ACCESS_COARSE_LOCATION = 2

        val WRITE_EXTERNAL_STORAGE = 3
        val READ_EXTERNAL_STORAGE = 4

        private val PLAY_SERVICES_RESOLUTION_REQUEST: Int = 1000
        private val PolygonCallBackData = 1001
        private val dlg_gpsCallbackData = 1002
        private val REQUEST_LAYER = 1003
        private val MarkerCallBackData = 1004

        val LAYER = 2000
        val LAYER_BIOTOPE = 2001
        val LAYER_BIRDS = 2002
        val LAYER_REPTILIA = 2003
        val LAYER_MAMMALIA = 2004
        val LAYER_FISH = 2005
        val LAYER_INSECT = 2006
        val LAYER_FLORA = 2007
        val LAYER_ZOOBENTHOS = 2008
        val LAYER_MYLOCATION = 2009
        val TRACKING = 2010

        val BIOTOPE_DATA = 3000
        val BIRDS_DATA = 3001
        val REPTILIA_DATA = 3002
        val MAMMALIA_DATA = 3003
        val FISH_DATA = 3004
        val INSECT_DATA = 3005
        val FLORA_DATA = 3006

        val SEARCHADDRESS = 4000

    }

    var types : ArrayList<String> = ArrayList<String>()

    private lateinit var context: Context

    private lateinit var mGestureDetector: GestureDetector
    private lateinit var googleMap: GoogleMap

    private var allPolygons: ArrayList<Polygon> = ArrayList<Polygon>()
    private var polygons : ArrayList<Polygon> = ArrayList<Polygon>()
    private var points = ArrayList<Marker>()
    private var allpoints = ArrayList<Marker>()
    private var polygonsToUnion = ArrayList<Polygon>()
    private var trackpoints = ArrayList<Marker>()
    private var getTrackingPoints = ArrayList<Marker>()

    private var start = false

    var latitude: Double = 126.79235
    var longitude: Double = 37.39627

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var layerFileName:ArrayList<String> = ArrayList<String>()

    var biotopeDatas:ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()
    var birdsDatas:ArrayList<Birds_attribute> = ArrayList<Birds_attribute>()
    var fishDatas:ArrayList<Fish_attribute> = ArrayList<Fish_attribute>()
    var floraDatas:ArrayList<Flora_Attribute> = ArrayList<Flora_Attribute>()
    var insectDatas:ArrayList<Insect_attribute> = ArrayList<Insect_attribute>()
    var mammaliaDatas:ArrayList<Mammal_attribute> = ArrayList<Mammal_attribute>()
    var reptiliaDatas:ArrayList<Reptilia_attribute> = ArrayList<Reptilia_attribute>()
    var trackingDatas:ArrayList<Tracking> = ArrayList<Tracking>()

    var biotopeGrop_id:String? = String()
    var birdsPk:String? = String()
    var fishPk:String? = String()
    var floraPk:String? = String()
    var insectPk:String? = String()

    var mammaliaPk:String? = String()
    var reptiliaPk:String? = String()

    val BIOTOPEATTRIBUTE:ArrayList<Exporter.ColumnDef> = ArrayList<Exporter.ColumnDef>()
    val BIRDSATTRIBUTE:ArrayList<Exporter.ColumnDef> = ArrayList<Exporter.ColumnDef>()
    val REPTILIAATTRIBUTE:ArrayList<Exporter.ColumnDef> = ArrayList<Exporter.ColumnDef>()
    val MAMMALATTRIBUTE:ArrayList<Exporter.ColumnDef> = ArrayList<Exporter.ColumnDef>()
    val FISHATTRIBUTE:ArrayList<Exporter.ColumnDef> = ArrayList<Exporter.ColumnDef>()
    var INSECTATTRIBUTE:ArrayList<Exporter.ColumnDef> = ArrayList<Exporter.ColumnDef>()
    var FLORAATTRIBUTE:ArrayList<Exporter.ColumnDef> = ArrayList<Exporter.ColumnDef>()
    var TRACKINGS:ArrayList<Exporter.ColumnDef> = ArrayList<Exporter.ColumnDef>()

    private val latlngs: ArrayList<LatLng> = ArrayList<LatLng>()
    private val latlngsGPS: ArrayList<BiotopeBaseGPS> = ArrayList<BiotopeBaseGPS>()

    // 3. biotope  , 6.birds , 7.Reptilia , 8.mammalia  9. fish, 10.insect, 11.flora , 13. zoobenthos

    var currentLayer = -1

    var myLocation: Tracking? = null

    var trackingdiv = false

    var prevPoint: Geometry? = null

    private var showLoading = false

    var progressDialog: ProgressDialog? = null

    var biotopedataArray:ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()
    var birdsdataArray:ArrayList<Birds_attribute> = ArrayList<Birds_attribute>()
    var reptiliadataArray:ArrayList<Reptilia_attribute> = ArrayList<Reptilia_attribute>()
    var mammaldataArray:ArrayList<Mammal_attribute> = ArrayList<Mammal_attribute>()
    var fishdataArray:ArrayList<Fish_attribute> = ArrayList<Fish_attribute>()
    var insectdataArray:ArrayList<Insect_attribute> = ArrayList<Insect_attribute>()
    var floradataArray:ArrayList<Flora_Attribute> = ArrayList<Flora_Attribute>()

    var mygps = false

    var markerRemove = false

    var polygonRemove = false

    var trackingChk = false

    var trackingPointChk = false

    var nowTime = System.currentTimeMillis()

    lateinit var inputStream : InputStream

    private var timer: Timer? = null

    private val BACK_PRESSED_TERM = (1000 * 2).toLong()
    private var backPressedTime: Long = 0

    private var layersDatas:ArrayList<LayerModel> = ArrayList<LayerModel>()

    var chkDivision = false

    var layerDivision = 0

    internal var loadDataHandler: Handler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            initGps()
            var location: Location = Location("route")
            location.latitude = latitude
            location.longitude = longitude
            onLocationUpdated(location)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this
        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase()
        val dataList: Array<String> = arrayOf("*")
        val data = db!!.query("settings", dataList, null, null, null, null, "id desc", "1")

        progressDialog = ProgressDialog(this, R.style.progressDialogTheme)
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Large)
        progressDialog!!.setCancelable(false)

/*        PrefUtils.setPreference(this, "latitude", latitude);
        PrefUtils.setPreference(this, "longitude", longitude);*/
        while (data.moveToNext()) {

            var gpsset: GpsSet = GpsSet(data.getInt(0), data.getDouble(1), data.getDouble(2))

            latitude = gpsset.latitude!!
            longitude = gpsset.longitude!!

        }

        mGestureDetector = GestureDetector(this, GestureListener())

        drawer_view.setOnTouchListener(this)

        btn_layer.setOnClickListener {
            currentLayer = LAYER

            val zoom = googleMap.cameraPosition.zoom
            val intent = Intent(this, DlgLayersActivity::class.java)

            intent.putExtra("zoom", zoom)
            if(layerFileName != null){
                intent.putExtra("layerFileName",layerFileName)
            }
            startActivityForResult(intent, REQUEST_LAYER)
        }

        // 비오톱 추가
        btn_biotope.setOnClickListener {

            if(chkDivision){
                if( !chkDivision(LAYER_BIOTOPE)) {
                    return@setOnClickListener
                }
            }

            currentLayer = LAYER_BIOTOPE

            if (drawer_view.visibility == View.VISIBLE) {

                if(polygon != null) {
                    endPolygonDraw(polygon!!)
                }

                if(polygon == null){
                    endDraw()
                }

            } else {
                startDraw()
            }
        }

        //조류 추가
        btn_birds.setOnClickListener {
            if(chkDivision){
                if( !chkDivision(LAYER_BIRDS)) {
                    return@setOnClickListener
                }
            }
            currentLayer = LAYER_BIRDS

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }

        }

        //양서ㆍ파충류 추가
        btn_Reptilia.setOnClickListener {

            if(chkDivision){
                if( !chkDivision(LAYER_REPTILIA)) {
                    return@setOnClickListener
                }
            }

            currentLayer = LAYER_REPTILIA

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //포유류 추가
        btn_mammalia.setOnClickListener {

            if(chkDivision){
                if( !chkDivision(LAYER_MAMMALIA)) {
                    return@setOnClickListener
                }
            }

            currentLayer = LAYER_MAMMALIA

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //어류 추가
        btn_fish.setOnClickListener {

            if(chkDivision){
                if( !chkDivision(LAYER_FISH)) {
                    return@setOnClickListener
                }
            }

            currentLayer = LAYER_FISH

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //곤충 추가
        btn_insect.setOnClickListener {

            if(chkDivision){
                if( !chkDivision(LAYER_INSECT)) {
                    return@setOnClickListener
                }
            }

            currentLayer = LAYER_INSECT

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }

        }

        //식물 추가
        btn_flora.setOnClickListener {

            if(chkDivision){
                if( !chkDivision(LAYER_FLORA)) {
                    return@setOnClickListener
                }
            }

            currentLayer = LAYER_FLORA

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }

        }

        //저서무척추동물 추가
        btn_zoobenthos.setOnClickListener {

            currentLayer = LAYER_ZOOBENTHOS

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        btn_clear_all.setOnClickListener {

            googleMap.clear()

            points.clear()
            latlngs.clear()
            polygonsToUnion.clear()

            splitRL.isSelected = false
            unionRL.isSelected = false

            endDraw()

        }

        //좌표지정 버튼
        btn_gps_select.setOnClickListener {
            val intent: Intent = Intent(this, hntecology.ecology.activities.Dlg_gps::class.java)
            startActivityForResult(intent, dlg_gpsCallbackData)
        }

        btn_satellite.setOnClickListener {

            var satelite: String = btn_satellite.text.toString()

            if (satelite == "위성 지도") {

                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                btn_satellite.text = "일반 지도"
            } else {

                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                btn_satellite.text = "위성 지도"
            }
        }

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        startRegistrationService()

        TVtimeTV.text = Utils.getToday("yyyy-MM-dd")

        logoutBtn.setOnClickListener {

            var builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage("로그아웃 하시겠습니까?")
            builder.setCancelable(true)
            builder.setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()

            })
            builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()

                PrefUtils.clear(context)

                val intent: Intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            })

            builder.show()

        }

        resetTV.setOnClickListener {

            val dbManager: DataBaseHelper = DataBaseHelper(this)

            val db = dbManager.createDataBase();

            val dataList:Array<String> = arrayOf("file_name", "layer_name","min_scale","max_scale","type","added","grop_id");

            var datas:ArrayList<LayerModel> = ArrayList<LayerModel>()

            val zoom = intent.getFloatExtra("zoom", 0.0F)

            var chkData = false

            if (layersDatas != null){
                for (i in 0..layersDatas.size-1) {
                    var layerdata = db.query("layers", dataList, "grop_id = '${layersDatas.get(i).grop_id}' and min_scale = ${zoom.toInt()}", null, null, null, "", null)

                    while(layerdata.moveToNext()){
                        chkData = true

                        val layerModel = LayerModel(data.getString(0), data.getString(1), data.getInt(2),data.getInt(3),data.getString(4),data.getString(5),data.getString(6),false);

                        datas.add(layerModel)
                    }
                }

                if (chkData){
                    layersDatas.clear()
                    for (i in 0..datas.size-1){
                        layersDatas.add(datas.get(i))
                        loadLayer(datas.get(i).file_name, datas.get(i).layer_name,datas.get(i).type, datas.get(i).added)
                    }
                }

            }
        }

        exportBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
            } else {
                export()
            }

        }

        btn_mygps.setOnClickListener {

            currentLayer = LAYER_MYLOCATION

            val latlng = LatLng(this.latitude, this.longitude)

            mygps = true

            drawPoint(latlng)

//            val makerOption =MarkerOptions()
//
//            makerOption.position(latlng)
//                    .title("현재 위치")

//            googleMap.addMarker(makerOption)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16f))

//            val point = Point()
//            point.x = latitude.toInt()
//            point.y = longitude.toInt()
//            val geoPoint = googleMap.projection.fromScreenLocation(point)

        }

        delPointRL.setOnClickListener {

            when (currentLayer) {
                LAYER_BIOTOPE -> {
                    if (latlngs.size > 0) {
                        latlngs.removeAt(latlngs.size - 1)
                        drawPolygon()
                    }
                }

                else -> {

                    println("points.size : ${points.size}")

                    if (points.size > 0) {
                        val lastPoint = points.removeAt(points.size - 1)
                        lastPoint.remove()
                    }
                }
            }
        }

        // 도형 분리
        splitRL.setOnClickListener {
            if (splitRL.isSelected) {
                splitPolygon()
                offSplitBtn()
            } else {
                onSplitBtn()
            }
        }

        // 도형 합지기
        unionRL.setOnClickListener {

            endDraw()

            if (unionRL.isSelected) {
                unionPolygons()
            } else {
                onUnionBtn()
            }

        }

        initGps()

        trackingBtn.setOnClickListener {

            currentLayer = TRACKING

            val title = trackingBtn.text.toString()

            val dbManager: DataBaseHelper = DataBaseHelper(this)

            val db = dbManager.createDataBase()

            if ( title.equals("Tracking 켜기")){
                start = true
                dbManager.deletetracking()
                trackingBtn.setText("Tracking 끄기")
                timerStart()

//                val tracking = Tracking(null,37.4954,126.7720)
//                val tracking1 = Tracking(null,37.4947,126.7723)
//                val tracking2= Tracking(null,37.4943,126.7723)
//                dbManager.inserttracking(tracking)
//                dbManager.inserttracking(tracking1)
//                dbManager.inserttracking(tracking2)

                trackingdiv = true
            } else if(title.equals("Tracking 끄기")){
                start = false
                trackingBtn.setText("Tracking 켜기")
                trackingdiv = false
                if (timer != null) {
                    timer!!.cancel()
                }
                exportTracking()
            }

        }

        markerdeleteBtn.setOnClickListener {

            val title = markerdeleteBtn.text.toString()

            if(title.equals("마커 삭제")){
                markerRemove = true
                markerdeleteBtn.setText("마커 삭제중")
            }else if(title.equals("마커 삭제중")){
                markerRemove = false
                markerdeleteBtn.setText("마커 삭제")
            }

        }

        polygondeleteBtn.setOnClickListener {

            val title = polygondeleteBtn.text.toString()

            if(title.equals("비오톱 삭제")){
                polygonRemove = true
                polygondeleteBtn.setText("비오톱 삭제중")
            }else if(title.equals("비오톱 삭제중")){
                polygonRemove = false
                polygondeleteBtn.setText("비오톱 삭제")
            }

        }

        mytrackingBtn.setOnClickListener {

            currentLayer = TRACKING

            val title = mytrackingBtn.text.toString()

            if(title == "이동경로 보기"){
                mytrackingBtn.setText("이동경로 숨기기")

                trackingPointChk = true

                if(getTrackingPoints != null){
                    getTrackingPoints.clear()
                }

                val dbManager: DataBaseHelper = DataBaseHelper(this)

                val db = dbManager.createDataBase();

                val trackingdata = db.query("tracking", dataList, null, null, null, null, "id", null)

                while (trackingdata.moveToNext()) {
                    var tracking : Tracking = Tracking(trackingdata.getInt(0),trackingdata.getDouble(1),trackingdata.getDouble(2))

                    val latlng = LatLng(tracking.LATITUDE!!,tracking.LONGITUDE!!)

                    drawPoint(latlng)
                }

            }

            if(title == "이동경로 숨기기"){
                mytrackingBtn.setText("이동경로 보기")

                trackingPointChk = false

                if(getTrackingPoints.size > 0){
                    for(i in 0..getTrackingPoints.size-1){
                        getTrackingPoints.get(i).remove()
                    }
                }
            }

        }

        transparentRL.setOnClickListener {
            seekbarSB.visibility = View.VISIBLE
        }

        searchaddressBT.setOnClickListener {

            var intent = Intent()

            intent = Intent(this, SearchAddressActivity::class.java)

            val url = "http://postcode.map.daum.net/search?origin=http%3A%2F%2Fpostcode.map.daum.net&indaum=off&banner=on&mode=transmit&vt=layer&am=on&ani=off&sd=on&plrg=&plrgt=1.5&hmb=off&heb=off&asea=off&smh=off&zo=off&us=on&msi=5&ahs=off&whas=275&sm=on&a51=off&zn=Y&fullpath=%2Fguide"

            intent.putExtra("url",url)

            startActivityForResult(intent, SEARCHADDRESS)

        }

        seekbarSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(allPolygons != null) {
                    for (i in 0..allPolygons.size - 1) {
                        allPolygons.get(i)
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        myLocation = Tracking(null, latitude, longitude)
        loadLayer("","","","Y")

    }

    private fun onUnionBtn() {
        unionRL.isSelected = true
        unionTV.setTextColor(Color.BLACK)
        unionTV.setTypeface(null, Typeface.BOLD)
    }

    private fun offUnionBtn() {
        unionRL.isSelected = false
        unionTV.setTextColor(Color.parseColor("#333333"))
        unionTV.setTypeface(null, Typeface.NORMAL)

        for (polygon in polygonsToUnion) {
            polygon.strokeWidth = 1.0f
            polygon.strokeColor = Color.BLACK
        }

        polygonsToUnion.clear()

    }

    private fun onSplitBtn() {
        splitRL.isSelected = true

        splitTV.setTextColor(Color.BLACK)
        splitTV.setTypeface(null, Typeface.BOLD);

        drawer_view.visibility = View.VISIBLE
    }

    private fun startRegistrationService() {
        val api = GoogleApiAvailability.getInstance()
        val code = api.isGooglePlayServicesAvailable(this)
        if (code == ConnectionResult.SUCCESS) {
            onActivityResult(PLAY_SERVICES_RESOLUTION_REQUEST, Activity.RESULT_OK, Intent())
        } else if (api.isUserResolvableError(code) && api.showErrorDialogFragment(this, code, PLAY_SERVICES_RESOLUTION_REQUEST)) {
            // wait for onActivityResult call (see below)
        } else {
            Toast.makeText(this, api.getErrorString(code), Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                PLAY_SERVICES_RESOLUTION_REQUEST -> {

                    initilizeMap()
                }

                PolygonCallBackData -> {

                }

                MarkerCallBackData -> {
                    if(data!!.getStringExtra("markerid") != null){
                        val markerid = data!!.getStringExtra("markerid")
                        for(i in 0..points.size -1){
                            if(points.get(i).id == markerid){
                                points.get(i).remove()
                            }
                        }
                    }
                }

                dlg_gpsCallbackData -> {
                    latitude = data!!.getDoubleExtra("latitude", 126.79235)
                    longitude = data.getDoubleExtra("longitude", 37.39627)

                    val gpsSet: GpsSet = GpsSet(null, latitude, longitude)
                    dbManager!!.insertGpsSet(gpsSet)
                    onMapReady(googleMap)
                }

                BIOTOPE_DATA -> {
                    if(data!!.getStringExtra("polygonid") != null) {
                        val polygonid = data!!.getStringExtra("polygonid")
                        println("biotope_data  $polygonid")

                        println(polygons.size.toString()  + "-----------------------------")
                        for(i in 0..polygons.size -1){
                            val polygon = polygons.get(i)
                            println("polygonid : ${polygon.id}")
                            println("polygonid : ${polygonid}")

                                if ((polygon.id).equals(polygonid)) {

                                    println("ssssssssssssss : ${polygon.hashCode()}")

                                    polygons.get(i).remove()
                                    polygons.remove(polygons.get(i))

                                    runOnUiThread(Runnable {
                                        polygon.remove()
//                                        polygons.removeAt(i)
                                    })
//                                polygons.remove(polygons.get(i))
                                }
                        }

                        println("polygons : " + polygons)
                        println("polygons.size : " + polygons.size)

                    }

                    if(data!!.getIntExtra("export" , 0) != null){

                        val export = data!!.getIntExtra("export",0)

                        println("----------------$export")

                        if(export == 70){
                            layerDivision = 0
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                            } else {
                                exportBiotope()
                            }
                        }

                    }

                }

                BIRDS_DATA ->{

                    if(data!!.getStringExtra("markerid") != null){
                        val markerid = data!!.getStringExtra("markerid")
                        for(i in 0..points.size -1){
                            println("------------remove-------${points.size}")
                            if(points.get(i).id == markerid){
                                points.get(i).remove()
                                break
                            }
                        }
                        println("------------removeiiiiii-------${points.size}")
                    }

                    if(data!!.getStringExtra("reset") != null){

                    }

                    if(data!!.getIntExtra("export", 0 ) != null){
                        val export = data!!.getIntExtra("export",0)

                        if(export == 70){
                            layerDivision = 1
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                            } else {
                                exportBirds()
                            }
                        }
                    }

                }

                REPTILIA_DATA ->{
                    if(data!!.getStringExtra("markerid") != null){
                        val markerid = data!!.getStringExtra("markerid")
                        for(i in 0..points.size -1){
                            if(points.get(i).id == markerid){
                                points.get(i).remove()
                                break
                            }
                        }
                    }

                    if(data!!.getStringExtra("reset") != null){

                    }

                    if(data!!.getIntExtra("export", 0 ) != null){
                        val export = data!!.getIntExtra("export",0)

                        if(export == 70){
                            layerDivision = 2
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                            } else {
                                exportReptilia()
                            }
                        }
                    }

                }

                MAMMALIA_DATA -> {
                    if(data!!.getStringExtra("markerid") != null){
                        val markerid = data!!.getStringExtra("markerid")
                        for(i in 0..points.size -1){
                            if(points.get(i).id == markerid){
                                points.get(i).remove()
                                break
                            }
                        }
                    }

                    if(data!!.getStringExtra("reset") != null){

                    }

                    if(data!!.getIntExtra("export", 0 ) != null){
                        val export = data!!.getIntExtra("export",0)

                        if(export == 70){
                            layerDivision = 3
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                            } else {
                                exportMammal()
                            }
                        }
                    }
                }

                FISH_DATA -> {
                    if(data!!.getStringExtra("markerid") != null){
                        val markerid = data!!.getStringExtra("markerid")
                        for(i in 0..points.size -1){
                            if(points.get(i).id == markerid){
                                points.get(i).remove()
                                break
                            }
                        }
                    }

                    if(data!!.getStringExtra("reset") != null){

                    }

                    if(data!!.getIntExtra("export", 0 ) != null){
                        val export = data!!.getIntExtra("export",0)

                        if(export == 70){
                            layerDivision = 4
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                            } else {
                                exportFish()
                            }
                        }
                    }
                }

                INSECT_DATA -> {
                    if(data!!.getStringExtra("markerid") != null){
                        val markerid = data!!.getStringExtra("markerid")
                        for(i in 0..points.size -1){
                            if(points.get(i).id == markerid){
                                points.get(i).remove()
                                break
                            }
                        }
                    }

                    if(data!!.getStringExtra("reset") != null){

                    }

                    if(data!!.getIntExtra("export", 0 ) != null){
                        val export = data!!.getIntExtra("export",0)

                        if(export == 70){
                            layerDivision = 5
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                            } else {
                                exportInsects()
                            }
                        }
                    }
                }

                FLORA_DATA -> {
                    if(data!!.getStringExtra("markerid") != null){
                        val markerid = data!!.getStringExtra("markerid")
                        for(i in 0..points.size -1){
                            if(points.get(i).id == markerid){
                                points.get(i).remove()
                                break
                            }
                        }
                    }

                    if(data!!.getStringExtra("reset") != null){

                    }

                    if(data!!.getIntExtra("export", 0 ) != null){
                        val export = data!!.getIntExtra("export",0)

                        if(export == 70){
                            layerDivision = 6
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                            } else {
                                exportFlora()
                            }
                        }
                    }
                }

                REQUEST_LAYER -> {
                    var jsonOb: ArrayList<LayerModel> = ArrayList<LayerModel>()

                    jsonOb = data!!.getSerializableExtra("data") as ArrayList<LayerModel>

                    if(types.size != null){
                        types.clear()
                    }

                    if(layerFileName != null){
                        layerFileName.clear()
                    }

                    if(jsonOb.size == 0){
                        println("000000")
                        loadLayer("","","","")
                    }

                    googleMap.clear()

                    if(layersDatas != null){
                        layersDatas.clear()
                    }

                    for (i in 0..jsonOb.size - 1) {
                        loadLayer(jsonOb.get(i).file_name, jsonOb.get(i).layer_name,jsonOb.get(i).type, jsonOb.get(i).added)

                        println("jsonOB . filename ${jsonOb.get(i).file_name}")

                        layerFileName.add(jsonOb.get(i).file_name)
                        layersDatas.add(jsonOb.get(i))
                    }

                }

                SEARCHADDRESS -> {

                    if(data!!.getStringExtra("url") != null){
                        val url = data!!.getStringExtra("url")

                        val reverse = "https://maps.googleapis.com/maps/api/geocode/json?address=$url,+Mountain+View,+CA&key=AIzaSyDwxugiCyvcZY7rQmaZywr6MlOSlgBGlHg"

                        println("reverse $reverse")

                    }

                }

                else -> super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    /**
     * Setting up map
     *
     */

    private fun initilizeMap() {
        val status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(applicationContext)
        if (status == ConnectionResult.SUCCESS) {
            val mapFragment = SupportMapFragment.newInstance()
            mapFragment.getMapAsync(this)

            supportFragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit()
            supportFragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit()

        } else if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
            // showErrorDialog(status);
        } else {
            Toast.makeText(this, "No Support for Google Play Service", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        if (map == null) {
            return
        }

        googleMap = map

        googleMap.uiSettings.isRotateGesturesEnabled = false

        googleMap.setOnCameraIdleListener(this)
        googleMap.setOnCameraMoveListener(this)

        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.

        val initialMapCenter = LatLng(longitude, latitude)

        // googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialMapCenter, 15.6f))

        // 마커클릭 이벤트 처리
        // GoogleMap 에 마커클릭 이벤트 설정 가능.
        googleMap.setOnMarkerClickListener { marker ->

            println("click")
            val builder = AlertDialog.Builder(context)
            if(markerRemove == true) {

                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()
                            marker.remove()

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }

            if(markerRemove == false) {

                val layerInfo = marker.tag as LayerInfo
                var myLayer = layerInfo.layer

                var attrubuteKey = layerInfo.attrubuteKey
                var intent: Intent? = null

                val dbManager: DataBaseHelper = DataBaseHelper(this)

                val db = dbManager.createDataBase();

                when (myLayer) {

                    LAYER_BIRDS -> {

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("birdsAttribute", dataList, "GROP_ID = '$attrubuteKey'", null, null, null, "", null)


                        if (birdsdataArray != null) {
                            birdsdataArray.clear()
                        }

                        var title = ""

                        while (data.moveToNext()) {

                            var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                    , data.getString(15),data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21), data.getString(22)
                                    , data.getString(23), data.getString(24), data.getFloat(25), data.getFloat(26), data.getString(27), data.getString(28))
                            birdsdataArray.add(birds_attribute)

                        }

                        for (i in 0..birdsdataArray.size - 1) {
                            title = "조류"

                            marker.title = title
                        }

                        if (birdsdataArray.size == 0) {
                            title = "조류"

                            marker.title = title

                            intent = Intent(this, BirdsActivity::class.java)

                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            println("intent-----------------------------------${attrubuteKey.toString()}")

                            startActivityForResult(intent, BIRDS_DATA)
                        }

                        if(birdsdataArray.size == 1 ){

                            intent = Intent(this, BirdsActivity::class.java)

                            intent!!.putExtra("id" , birdsdataArray.get(0).id)
                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, BIRDS_DATA)

                        }

                        if (birdsdataArray.size > 1) {
                            val intent = Intent(this, DlgDataListActivity::class.java)
                            intent.putExtra("title", "야생조류")
                            intent.putExtra("table", "birdsAttribute")
                            intent.putExtra("DlgHeight", 600f);
                            intent!!.putExtra("markerid", marker.id)
                            intent.putExtra("GROP_ID", attrubuteKey)
                            startActivityForResult(intent, BIRDS_DATA);
                        }

                    }

                    LAYER_REPTILIA -> {
                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("reptiliaAttribute", dataList, "GROP_ID = '$attrubuteKey'", null, null, null, "", null)

                        if (reptiliadataArray != null) {
                            reptiliadataArray.clear()
                        }

                        var title = ""

                        while (data.moveToNext()) {
                            var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                    , data.getString(15), data.getString(16),data.getInt(17), data.getInt(18), data.getInt(19), data.getString(20), data.getString(21), data.getString(22)
                                    , data.getString(23), data.getString(24), data.getString(25), data.getInt(26), data.getInt(27), data.getInt(28), data.getFloat(29), data.getFloat(30),data.getString(31),data.getString(32))


                            reptiliadataArray.add(reptilia_attribute)
                        }

                        for (i in 0..reptiliadataArray.size - 1) {
                            title = "양서,파충류"

                            marker.title = title
                        }

                        if (reptiliadataArray.size == 0) {
                            title = "양서,파충류"

                            marker.title = title

                            intent = Intent(this, ReptiliaActivity::class.java)

                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, REPTILIA_DATA)
                        }

                        if (reptiliadataArray.size == 1){

                            intent = Intent(this, ReptiliaActivity::class.java)

                            intent!!.putExtra("id" , reptiliadataArray.get(0).id)
                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, BIRDS_DATA)

                        }

                        if (reptiliadataArray.size > 1) {
                            val intent = Intent(this, DlgDataListActivity::class.java)
                            intent.putExtra("title", "양서,파충류")
                            intent.putExtra("table", "reptiliaAttribute")
                            intent.putExtra("DlgHeight", 600f);
                            intent!!.putExtra("markerid", marker.id)
                            intent.putExtra("GROP_ID", attrubuteKey)
                            startActivityForResult(intent, REPTILIA_DATA);
                        }

                    }

                    LAYER_MAMMALIA -> {

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("mammalAttribute", dataList, "GROP_ID = '$attrubuteKey'", null, null, null, "", null)

                        if (mammaldataArray != null) {
                            mammaldataArray.clear()
                        }

                        var title = ""

                        while (data.moveToNext()) {
                            var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18),data.getInt(19), data.getString(20), data.getString(21), data.getFloat(22)
                                    , data.getFloat(23), data.getString(24), data.getString(25), data.getString(26), data.getString(27),data.getString(28),data.getString(29))

                            mammaldataArray.add(mammal_attribute)
                        }

                        for (i in 0..mammaldataArray.size - 1) {
                            title = "포유류"

                            marker.title = title
                        }

                        if (mammaldataArray.size == 0) {
                            title = "포유류"

                            marker.title = title

                            intent = Intent(this, MammaliaActivity::class.java)

                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, MAMMALIA_DATA)
                        }

                        if (mammaldataArray.size == 1){
                            intent = Intent(this, MammaliaActivity::class.java)

                            intent!!.putExtra("id" , mammaldataArray.get(0).id)
                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, MAMMALIA_DATA)
                        }

                        if (mammaldataArray.size > 1) {
                            val intent = Intent(this, DlgDataListActivity::class.java)
                            intent.putExtra("title", "포유류")
                            intent.putExtra("table", "mammalAttribute")
                            intent.putExtra("DlgHeight", 600f);
                            intent!!.putExtra("markerid", marker.id)
                            intent.putExtra("GROP_ID", attrubuteKey)
                            startActivityForResult(intent, MAMMALIA_DATA);
                        }

                    }

                    LAYER_FISH -> {

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("fishAttribute", dataList, "GROP_ID = '$attrubuteKey'", null, null, null, "", null)

                        if (fishdataArray != null) {
                            fishdataArray.clear()
                        }

                        var title = ""

                        while (data.moveToNext()) {
                            var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8), data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                                    data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                                    data.getInt(29), data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36), data.getString(37),data.getString(38))
                            fishdataArray.add(fish_attribute)
                        }

                        for (i in 0..fishdataArray.size - 1) {
                            title = "어류"

                            marker.title = title
                        }

                        if (fishdataArray.size == 0) {
                            title = "어류"

                            marker.title = title

                            intent = Intent(this, FishActivity::class.java)

                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, FISH_DATA)
                        }

                        if (fishdataArray.size == 1){

                            intent = Intent(this, FishActivity::class.java)

                            intent!!.putExtra("id" , fishdataArray.get(0).id)
                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, FISH_DATA)

                        }

                        if (fishdataArray.size > 1) {
                            val intent = Intent(this, DlgDataListActivity::class.java)
                            intent.putExtra("title", "어류")
                            intent.putExtra("table", "fishAttribute")
                            intent.putExtra("DlgHeight", 600f);
                            intent!!.putExtra("markerid", marker.id)
                            intent.putExtra("GROP_ID", attrubuteKey)
                            startActivityForResult(intent, FISH_DATA);
                        }

                    }

                    LAYER_INSECT -> {

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("insectAttribute", dataList, "GROP_ID = '$attrubuteKey'", null, null, null, "", null)

                        if (insectdataArray != null) {
                            insectdataArray.clear()
                        }

                        var title = ""

                        while (data.moveToNext()) {
                            var insect_attribute: Insect_attribute = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                    , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                                    , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27), data.getString(28), data.getString(29))
                            insectdataArray.add(insect_attribute)
                        }


                        for (i in 0..insectdataArray.size - 1) {
                            title = "곤충"

                            marker.title = title
                        }

                        if (insectdataArray.size == 0) {
                            title = "곤충"

                            marker.title = title

                            intent = Intent(this, InsectActivity::class.java)

                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, INSECT_DATA)
                        }

                        if (insectdataArray.size == 1){

                            intent = Intent(this, InsectActivity::class.java)

                            intent!!.putExtra("id" , insectdataArray.get(0).id)
                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, INSECT_DATA)

                        }

                        if (insectdataArray.size > 1) {
                            val intent = Intent(this, DlgDataListActivity::class.java)
                            intent.putExtra("title", "곤충")
                            intent.putExtra("table", "insectAttribute")
                            intent.putExtra("DlgHeight", 600f);
                            intent!!.putExtra("markerid", marker.id)
                            intent.putExtra("GROP_ID", attrubuteKey)
                            startActivityForResult(intent, INSECT_DATA);
                        }
                    }

                    LAYER_FLORA -> {

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("floraAttribute", dataList, "GROP_ID = '$attrubuteKey'", null, null, null, "", null)

                        if (floradataArray != null) {
                            floradataArray.clear()
                        }

                        var title = ""

                        while (data.moveToNext()) {
                            var flora_attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                                    , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25))
                            floradataArray.add(flora_attribute)
                        }

                        for (i in 0..floradataArray.size - 1) {
                            title = "식물"

                            marker.title = title
                        }

                        if (floradataArray.size == 0) {
                            title = "식물"

                            marker.title = title

                            intent = Intent(this, FloraActivity::class.java)

                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, FLORA_DATA)
                        }

                        if (floradataArray.size == 1 ){

                            intent = Intent(this, FloraActivity::class.java)

                            intent!!.putExtra("id" , floradataArray.get(0).id)
                            intent!!.putExtra("GROP_ID", attrubuteKey)
                            intent!!.putExtra("markerid", marker.id)

                            startActivityForResult(intent, FLORA_DATA)

                        }

                        if (floradataArray.size > 1) {
                            val intent = Intent(this, DlgDataListActivity::class.java)
                            intent.putExtra("title", "식물")
                            intent.putExtra("table", "floraAttribute")
                            intent.putExtra("DlgHeight", 600f);
                            intent!!.putExtra("markerid", marker.id)
                            intent.putExtra("GROP_ID", attrubuteKey)
                            startActivityForResult(intent, FLORA_DATA);
                        }

                    }

                    LAYER_ZOOBENTHOS -> {
                        intent = Intent(this, ZoobenthosActivity::class.java)
                    }

                    LAYER_MYLOCATION -> {

                    }

                    LAYER -> {

                    }

                    TRACKING -> {
                        marker.title = "이동 경로"
                    }

                }

                if (myLayer == LAYER_BIRDS || myLayer == LAYER_REPTILIA || myLayer == LAYER_MAMMALIA || myLayer == LAYER_FISH || myLayer == LAYER_INSECT || myLayer == LAYER_FLORA ) {

                }

                if (myLayer != LAYER_MYLOCATION && myLayer != LAYER && myLayer != LAYER_BIRDS && myLayer != LAYER_REPTILIA && myLayer != LAYER_MAMMALIA && myLayer != LAYER_FISH && myLayer != LAYER_INSECT
                        && myLayer != LAYER_FLORA && myLayer != TRACKING && myLayer != LAYER_MYLOCATION) {
                    intent!!.putExtra("id", attrubuteKey.toString())

                    startActivityForResult(intent, MarkerCallBackData)
                }

            }
            false
        }

        // 클릭시 태그 데이터 있는지 확인 없으면 바로 넘기고 있으면 있는걸로 호출.
        // tag 리절트로 가져와서 태그 설정
        googleMap.setOnPolygonClickListener { polygon ->

            val builder = AlertDialog.Builder(context)
            if (polygonRemove == true) {
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()
                            polygon.remove()

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }

            if (polygonRemove == false) {

                println("click -------------------------${polygon.id}")

                val layerInfo = polygon.tag as LayerInfo

                var myLayer = layerInfo.layer

                var attrubuteKey = layerInfo.attrubuteKey

                println("=========================================attrubuteKey $attrubuteKey")
                var intent: Intent? = null

                val dbManager: DataBaseHelper = DataBaseHelper(this)

                val db = dbManager.createDataBase();

                when (myLayer) {

                    LAYER_BIOTOPE -> {

                        if (unionRL.isSelected) {

                            if (polygonsToUnion.contains(polygon)) {
                                polygonsToUnion.remove(polygon)
                                polygon.strokeWidth = 0.0f
                                polygon.strokeColor = Color.TRANSPARENT
                                return@setOnPolygonClickListener
                            }

                            if (polygonsToUnion.size == 2) {
                                Utils.alert(context, "2 곳만 선택해서 합칠 수 있습니다.")
                                return@setOnPolygonClickListener
                            }

                            polygonsToUnion.add(polygon)
                            polygon.strokeWidth = 0.0f
                            polygon.strokeColor = Color.TRANSPARENT

                            return@setOnPolygonClickListener

                        } else {

                            val dataList: Array<String> = arrayOf("*");

                            val data = db.query("biotopeAttribute", dataList, "GROP_ID = '$attrubuteKey'", null, null, null, "", null)

                            if (biotopedataArray != null) {
                                biotopedataArray.clear()
                            }

                            var title = ""

                            while (data.moveToNext()) {
                                var biotope_attribute: Biotope_attribute = Biotope_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                                        data.getString(8), data.getFloat(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getFloat(14)
                                        , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                                        , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27), data.getFloat(28)
                                        , data.getString(29), data.getString(30), data.getString(31), data.getFloat(32), data.getFloat(33), data.getFloat(34), data.getString(35)
                                        , data.getString(36), data.getString(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41), data.getString(42)
                                        , data.getFloat(43), data.getFloat(44), data.getString(45), data.getString(46), data.getString(47), data.getString(48), data.getDouble(49)
                                        , data.getDouble(50), data.getString(51), data.getString(52), data.getString(53))
                                biotopedataArray.add(biotope_attribute)
                            }

                            if (biotopedataArray.size == 0) {

                                intent = Intent(this, BiotopeActivity::class.java)

                                intent!!.putExtra("GROP_ID", attrubuteKey.toString())
                                intent!!.putExtra("polygonid",polygon.id)
                                intent!!.putExtra("latitude", polygon.points.get(0).latitude.toString())
                                intent!!.putExtra("longitude", polygon.points.get(0).longitude.toString())

                                if (latlngs != null) {
                                    latlngs.clear()
                                }

                                if (latlngsGPS != null) {
                                    latlngsGPS.clear()
                                }

                                endDraw()

                                startActivityForResult(intent, BIOTOPE_DATA)

                            }

                            if(biotopedataArray.size == 1){
                                val intent = Intent(this, BiotopeActivity::class.java)
                                intent.putExtra("title", "비오톱")
                                intent.putExtra("table", "biotopeAttribute")
                                intent.putExtra("id", biotopedataArray.get(0).id)
                                intent.putExtra("DlgHeight", 600f);
                                intent.putExtra("GROP_ID", attrubuteKey)
                                intent.putExtra("polygonid",polygon.id)
                                startActivityForResult(intent, BIOTOPE_DATA);

                                if (latlngs != null) {
                                    latlngs.clear()
                                }

                                if (latlngsGPS != null) {
                                    latlngsGPS.clear()
                                }

                            }

                            if (biotopedataArray.size > 1) {
                                val intent = Intent(this, DlgDataListActivity::class.java)
                                intent.putExtra("title", "비오톱")
                                intent.putExtra("table", "biotopeAttribute")
                                intent.putExtra("DlgHeight", 600f);
                                intent.putExtra("GROP_ID", attrubuteKey)
                                intent.putExtra("polygonid",polygon.id)
                                startActivityForResult(intent, BIOTOPE_DATA);

                                if (latlngs != null) {
                                    latlngs.clear()
                                }

                                if (latlngsGPS != null) {
                                    latlngsGPS.clear()
                                }
                            }

                            if(polygons.size == 0){
                                polygons.add(polygon)
                            }

                            if(allPolygons.size == 0){
                                allPolygons.add(polygon)
                            }

                            for (i in 0..polygons.size-1){
                                if (polygons.get(i).id == polygon.id){
                                } else{
                                    polygons.add(polygon)
                                }
                            }

                            for (i in 0..allPolygons.size-1){
                                if (allPolygons.get(i).id == polygon.id){
                                } else{
                                    allPolygons.add(polygon)
                                }
                            }
                        }
                    }

                    LAYER_BIRDS -> {
                        intent = Intent(this, BirdsActivity::class.java)
                    }

                    LAYER_REPTILIA -> {
                        intent = Intent(this, ReptiliaActivity::class.java)
                    }

                    LAYER_MAMMALIA -> {
                        intent = Intent(this, MammaliaActivity::class.java)
                    }

                    LAYER_FISH -> {
                        intent = Intent(this, FishActivity::class.java)
                    }

                    LAYER_INSECT -> {
                        intent = Intent(this, InsectActivity::class.java)
                    }

                    LAYER_FLORA -> {
                        intent = Intent(this, FloraActivity::class.java)
                    }

                    LAYER_ZOOBENTHOS -> {
                        intent = Intent(this, ZoobenthosActivity::class.java)
                    }

                    LAYER_MYLOCATION -> {

                    }

                    LAYER -> {

                    }

                    TRACKING -> {

                    }

                }

                println("aa : $attrubuteKey")

                if (myLayer != LAYER_BIOTOPE || myLayer == LAYER_BIRDS || myLayer == LAYER_REPTILIA || myLayer == LAYER_MAMMALIA || myLayer == LAYER_FISH || myLayer == LAYER_INSECT
                        || myLayer == LAYER_FLORA) {
                    intent!!.putExtra("GROP_ID", attrubuteKey.toString())

                    println("intent-----------------------------------${attrubuteKey.toString()}")

                    startActivityForResult(intent, PolygonCallBackData)
                }
                if (myLayer != LAYER_MYLOCATION && myLayer != LAYER && myLayer != LAYER_BIOTOPE && myLayer != LAYER_BIRDS && myLayer != LAYER_REPTILIA && myLayer != LAYER_MAMMALIA && myLayer != LAYER_FISH
                        && myLayer != LAYER_INSECT && myLayer != LAYER_FLORA && myLayer != TRACKING) {
                    intent!!.putExtra("id", attrubuteKey.toString())

                    startActivityForResult(intent, PolygonCallBackData)
                }

            }
        }

    }

    private var parsed: Boolean = false

    override fun onCameraIdle() {

        if (parsed) {
            return
        }

        parsed = true
    }

    override fun onCameraMove() {

        val zoom = googleMap.cameraPosition.zoom
        zoomTV.text = zoom.toString()

    }

    // android.os.AsyncTask<Params, Progress, Result>

    private var currentFileName = ""
    private var currentLayerName = ""

    private fun loadLayer(fileName: String, layerName: String, Type: String,added : String) {

        if (fileName == null || fileName.length == 0) {
            return
        }

        val zoom = googleMap.cameraPosition.zoom
        if (zoom < 16) {
//            Utils.showNotification(context, "지도 레벨을 16이상으로 확대한 후 이용하세요.")
//            return
        }

        if (zoom < 13) {
//            Utils.showNotification(context, "지도 레벨을 16이상으로 확대한 후 이용하세요. 정말 안되요 ㅠㅠㅠ")
//            return
        }

        currentFileName = fileName
        currentLayerName = layerName

//        layerNameTV.text = currentLayerName

        val bounds = googleMap.projection.visibleRegion.latLngBounds
        LoadLayerTask(fileName,Type,added).execute(bounds)

    }

    private inner class LoadLayerTask(layerName: String , Type: String , added: String) : AsyncTask<LatLngBounds, Any, Boolean>() {

        var layerName = layerName

        var type = Type

        var added = added

        override fun doInBackground(vararg latLngBounds: LatLngBounds): Boolean {

            val northeast = latLngBounds[0].northeast
            val southwest = latLngBounds[0].southwest

            val coordinate1 = Coordinate(southwest.longitude, northeast.latitude)
            val coordinate2 = Coordinate(northeast.longitude, northeast.latitude)
            val coordinate3 = Coordinate(northeast.longitude, southwest.latitude)
            val coordinate4 = Coordinate(southwest.longitude, southwest.latitude)

            val ring = org.gdal.ogr.Geometry(ogr.wkbLinearRing)
            ring.AddPoint(coordinate1.x, coordinate1.y)
            ring.AddPoint(coordinate2.x, coordinate2.y)
            ring.AddPoint(coordinate3.x, coordinate3.y)
            ring.AddPoint(coordinate4.x, coordinate4.y)
            ring.AddPoint(coordinate1.x, coordinate1.y)

            val mapBoundary = org.gdal.ogr.Geometry(ogr.wkbPolygon)
            mapBoundary.AddGeometry(ring)

            ogr.RegisterAll()

            // set up the shapefile driver
            val driver = ogr.GetDriverByName("ESRI Shapefile")

            var shpFilePath = context.applicationInfo.dataDir + File.separator + "$layerName.shp"

            println("shpFilePath : $shpFilePath")

            if (added == "Y"){
                shpFilePath = "$layerName.shp"
            }

            println("shpFilePath 2 : $shpFilePath")

            val f = File(shpFilePath)

            println("g : ${f.exists()}")

            val dataSource = driver.Open(shpFilePath, 0)

            println("dataSource : $dataSource")

            val layerCount = dataSource.GetLayerCount()

            println("layerCount : $layerCount")

            for (idx in 0..(layerCount -1)) {
                val layer = dataSource.GetLayer(idx)
                val featureCount = layer.GetFeatureCount()
                for (fid in 0..(featureCount -1)) {
                    val feature = layer.GetFeature(fid)

                    val geometryRef = feature.GetGeometryRef()

                    if(!geometryRef.Intersects(mapBoundary)) {
                        // continue
                    }

                    // dbf
                    val metadata = HashMap<String, Any>()
                    val fieldCount = feature.GetFieldCount()
                    for (fieldIdx in 0..(fieldCount -1)) {
                        val fieldType = feature.GetFieldType(fieldIdx)
                        val key = feature.GetDefnRef().GetFieldDefn(fieldIdx).GetName()
                        val value = feature.GetFieldAsString(fieldIdx)

                        println("$fieldIdx / ${fieldCount -1} : $key $value")

                        metadata.put(key, value)

                    }

                    val geometryType = geometryRef.GetGeometryType()

                    println("geometryType : $geometryType")

                    if (geometryType == ogr.wkbPoint) {
                        val pointCount = geometryRef.GetPointCount()
                        for(pc in 0 until pointCount) {
                            val point = geometryRef.GetPoint(pc)

                            val x = point[0]
                            val y = point[1]

                            println("geometryType : $geometryType, point : $x, $y")

                            val latlng = LatLng(y, x)

                            val markerOptions = MarkerOptions()
                            markerOptions.position(latlng)

                            // markerOptions.title("Marker in Sydney")
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            markerOptions.alpha(1.0f)

                            publishProgress(markerOptions, JSONObject(metadata).toString())

                            Thread.sleep(10)
                        }
                    } else if (geometryType == ogr.wkbPolygon) {

                        val polygonOptions = PolygonOptions()
                        polygonOptions.fillColor(Color.parseColor(getLayerColor(layerName)))
                        polygonOptions.strokeWidth(1.0f)
                        polygonOptions.strokeColor(Color.BLACK)

                        val pointCount = geometryRef.GetPointCount()
                        for(pc in 0 until pointCount) {
                            val point = geometryRef.GetPoint(pc)

                            val x = point[0]
                            val y = point[1]

                            println("geometryType : $geometryType, point : $x, $y")

                            val latlng = LatLng(y, x)

                            polygonOptions.add(latlng)
                        }

                        publishProgress(polygonOptions, JSONObject(metadata).toString())

                        Thread.sleep(10)

                    }
                }
            }

            return true

        }

        fun doInBackgroundOld(vararg latLngBounds: LatLngBounds): Boolean {

            val northeast = latLngBounds[0].northeast
            val southwest = latLngBounds[0].southwest

            val coordinate1 = Coordinate(southwest.longitude, northeast.latitude)
            val coordinate2 = Coordinate(northeast.longitude, northeast.latitude)
            val coordinate3 = Coordinate(northeast.longitude, southwest.latitude)
            val coordinate4 = Coordinate(southwest.longitude, southwest.latitude)

            val coordinates = arrayOfNulls<Coordinate>(5)
            coordinates.set(0, coordinate1)
            coordinates.set(1, coordinate2)
            coordinates.set(2, coordinate3)
            coordinates.set(3, coordinate4)
            coordinates.set(4, coordinate1)

            val geometryFactory = GeometryFactory()
            var linearRing = geometryFactory.createLinearRing(coordinates)
            val mapBoundary = geometryFactory.createPolygon(linearRing)

            if (added == "N") {
                inputStream = assets.open("$layerName.shp")
            }

            if (added == "Y"){
                inputStream = FileInputStream("$layerName.shp")
            }

            println(layerName + "===============================")

            val shapeReader = Shapefile(inputStream)

            val geometryCollection = shapeReader.read(geometryFactory)
            val num = geometryCollection.numGeometries

            println("num : $num")

            var loadedCnt = 0
            // val available = ArrayList<Geometry>()
            for (i in 0..(num - 1)) {
                val geometry = geometryCollection.getGeometryN(i)

                if (!geometry.intersects(mapBoundary)) {
                    continue
                }

                if (geometry.geometryType.equals("Polygon")) {
                    val polygonOptions = PolygonOptions()
                    polygonOptions.fillColor(Color.parseColor(getLayerColor(layerName)))
                    polygonOptions.strokeWidth(1.0f)
                    polygonOptions.strokeColor(Color.BLACK)

                    val coordinates = geometry.coordinates

                    for (j in 0..(coordinates.size - 1)) {
                        val coordinate = coordinates[j]
                        polygonOptions.add(LatLng(coordinate.y, coordinate.x))
                    }

                    publishProgress(polygonOptions)

                    Thread.sleep(10)

                    loadedCnt++

                }

                println("geometry.type ${geometry.geometryType}")

                if (geometry.geometryType.equals("Point")){

                    val coordinates = geometry.coordinates

                    for (j in 0..(coordinates.size - 1)) {
                        val coordinate = coordinates[j]

                        val latlng = LatLng(coordinate.y, coordinate.x)

                        val markerOptions = MarkerOptions()
                        markerOptions.position(latlng)

                        // markerOptions.title("Marker in Sydney")
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        markerOptions.alpha(1.0f)

                        publishProgress(markerOptions)

                    }

                }

            }

            return true

        }

        override fun onProgressUpdate(vararg geoms: Any?) {

            val metadataP = geoms[1] as String
            val metadata = JSONObject(metadataP)

            println(metadata)

            if (geoms[0] is PolygonOptions) {
                val polygon = googleMap.addPolygon(geoms[0] as PolygonOptions)
                polygon.zIndex = 0.0f
                polygon.tag = layerName

                println("layerName .layer ===== $layerName")

                polygon.isClickable = true

                val layerInfo = LayerInfo()

                if(type.equals("biotope")){
                    layerInfo.layer = LAYER_BIOTOPE
                }

                if (type.equals("birds")){
                    layerInfo.layer = LAYER_BIRDS
                }

                if (type.equals("reptilia")){
                    layerInfo.layer = LAYER_REPTILIA
                }

                if (type.equals("mammalia")){
                    layerInfo.layer = LAYER_MAMMALIA
                }

                if (type.equals("fish")){
                    layerInfo.layer = LAYER_BIRDS
                }

                if (type.equals("insect")){
                    layerInfo.layer = LAYER_INSECT
                }

                if (type.equals("flora")){
                    layerInfo.layer = LAYER_FLORA
                }

                if (type.equals("tracking")){
                    layerInfo.layer = TRACKING
                }

                // metadata
                layerInfo.metadata = metadata

                val id = Utils.getString(layerInfo.metadata , "ID")
                val grop_id = Utils.getString(layerInfo.metadata , "GROP_ID")
                val landuse = Utils.getString(layerInfo.metadata, "landuse")

                println("id : $id  grop_id : $grop_id  landuse $landuse")

                polygon.tag = layerInfo

                println("polygon.tag ${polygon.tag}")

                polygons.add(polygon)
                allPolygons.add(polygon)

            } else if (geoms[0] is MarkerOptions) {

                val marker = googleMap.addMarker(geoms[0] as MarkerOptions)
                marker.zIndex = 0.0f
                marker.tag = layerName

                println("layerName .layer ===== $layerName")

                val layerInfo = LayerInfo()

                if(type.equals("biotope")){
                    layerInfo.layer = LAYER_BIOTOPE
                }

                if (type.equals("birds")){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    layerInfo.layer = LAYER_BIRDS
                }

                if (type.equals("reptilia")){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    layerInfo.layer = LAYER_REPTILIA
                }

                if (type.equals("mammalia")){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    layerInfo.layer = LAYER_MAMMALIA
                }

                if (type.equals("fish")){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    layerInfo.layer = LAYER_BIRDS
                }

                if (type.equals("insect")){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                    layerInfo.layer = LAYER_INSECT
                }

                if (type.equals("flora")){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    layerInfo.layer = LAYER_FLORA
                }

                if (type.equals("tracking")){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    layerInfo.layer = TRACKING

                }

                // metadata
                layerInfo.metadata = metadata

                val id = Utils.getString(layerInfo.metadata , "id")
                val grop_id = Utils.getString(layerInfo.metadata , "grop_id")
                val landuse = Utils.getString(layerInfo.metadata, "landuse")

                println("id : $id  grop_id : $grop_id  landuse $landuse")

                marker.tag = layerInfo

                points.add(marker)

            }

        }

        override fun onPostExecute(result: Boolean?) {
            Utils.hideLoading(this@MainActivity)

            print("Post........")
        }

    }

    private fun getLayerColor(layerName: String): String? {
        if ("SH_biotop_orig".equals(layerName)) {
            return "#85b66f"
        } else if ("SH_dummy".equals(layerName)) {
            return "#d5b43c"
        }

        return "#85b66f"
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float,
                             velocityY: Float): Boolean {
            return false
        }
    }

//    private var startGeoPoint: LatLng? = null

    private var polylineForSplitGuide: Polyline? = null

    /**
     * Ontouch event will draw poly line along the touch points
     *
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var X1 = event.x.toInt()
        var Y1 = event.y.toInt()

        println("${event.action} $X1, $Y1")

        var point = Point()
        point.x = X1
        point.y = Y1

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                X1 = event.x.toInt()
                Y1 = event.y.toInt()
                point = Point()
                point.x = X1
                point.y = Y1

                val geoPoint = googleMap.projection.fromScreenLocation(point)

                if (splitRL.isSelected) {
                    if(polylineForSplitGuide != null) {

                        val polylineForSplitGuidePoints = ArrayList<LatLng?>()

                        for(point in polylineForSplitGuide!!.points) {
                            polylineForSplitGuidePoints.add(point)
                        }

                        polylineForSplitGuidePoints.add(geoPoint)

                        polylineForSplitGuide?.points = polylineForSplitGuidePoints

                    } else {
                        val polylineOptions = PolylineOptions()
                        polylineOptions.add(geoPoint)

                        polylineOptions.width(5f)
                        polylineOptions.color(Color.YELLOW);

                        polylineForSplitGuide = googleMap.addPolyline(polylineOptions)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (splitRL.isSelected) {

                    /*
                    X1 = event.x.toInt()
                    Y1 = event.y.toInt()
                    point = Point()
                    point.x = X1
                    point.y = Y1
                    val geoPoint = googleMap.projection.fromScreenLocation(point)

                    val polylineForSplitGuidePoints = ArrayList<LatLng?>()
                    polylineForSplitGuidePoints.add(startGeoPoint)
                    polylineForSplitGuidePoints.add(geoPoint)

                    polylineForSplitGuide?.points = polylineForSplitGuidePoints
                    */
                }
            }

            MotionEvent.ACTION_UP -> {

                X1 = event.x.toInt()
                Y1 = event.y.toInt()
                point = Point()
                point.x = X1
                point.y = Y1

                val geoPoint = googleMap.projection.fromScreenLocation(point)

                if (splitRL.isSelected) {

                    if (editingPolygon == null) {
                        return false
                    }

                    /*
                    val layerInfo = editingPolygon?.tag as LayerInfo

                    val oldAttributeKey = layerInfo.attrubuteKey

                    val splited = Utils.splitPolygon(toJTSPolygon(editingPolygon!!), toJTSLineString(polylineForSplitGuide!!))

                    offSplitBtn()

                    polylineForSplitGuide?.remove()

                    editingPolygon?.remove()
                    editingPolygon = null

                    for (idx in 0..(splited.numGeometries - 1)) {
                        var polygon = splited.getGeometryN(idx)
                        // polygon = polygon.buffer(-0.00002)

                        val polygonOptions = PolygonOptions()
                        polygonOptions.fillColor(getColor())
                        polygonOptions.strokeWidth(5.0f)
                        polygonOptions.strokeColor(Color.WHITE)

                        for (coordinate in polygon.coordinates) {
                            polygonOptions.add(LatLng(coordinate.y, coordinate.x))
                        }

                        val po = googleMap.addPolygon(polygonOptions)
                        // po.zIndex = 5.0f

                        val newAttributeKey = getAttributeKey(layerInfo.layer)
                        val newLayerInfo = LayerInfo()
                        newLayerInfo.attrubuteKey = newAttributeKey
                        newLayerInfo.layer = layerInfo.layer

                        po.tag = newLayerInfo

                        po.isClickable = true

                        polygons.add(po)
                        allPolygons.add(po)

                        // copy data
                        copyRow("biotopeAttribute", oldAttributeKey, newAttributeKey)

                    }
                    */

                } else {

                    if (!unionRL.isSelected) {
                        // print("Poinnts array size : ${latlngs.size}")

                        // 3. biotope  , 6.birds , 7.Reptilia , 8.mammalia  9. fish, 10.insect, 11.flora , 13. zoobenthos
                        when (currentLayer) {

                            LAYER_BIOTOPE -> {

                                latlngs.add(geoPoint)
                                val GPS = BiotopeBaseGPS(geoPoint.latitude.toString(),geoPoint.longitude.toString())
                                latlngsGPS.add(GPS)

                                if (latlngs.size == 1) {
                                    initEditingPolygon()
                                }

                                drawPolygon()

//                                if(latlngs.size >= 3){
//                                    endDraw()
//                                    latlngs.clear()
//                                }

                            }

                            LAYER_BIRDS -> {
                                drawPoint(geoPoint)
                            }

                            LAYER_REPTILIA -> {
                                drawPoint(geoPoint)
                            }

                            LAYER_MAMMALIA -> {
                                drawPoint(geoPoint)
                            }

                            LAYER_FISH -> {
                                drawPoint(geoPoint)
                            }

                            LAYER_INSECT -> {
                                drawPoint(geoPoint)
                            }

                            LAYER_FLORA -> {
                                drawPoint(geoPoint)
                            }

                            LAYER_ZOOBENTHOS -> {
                                drawPoint(geoPoint)
                            }

                            LAYER_MYLOCATION -> {
                                drawPoint(geoPoint)
                            }

                        }

                    }
                }
            }
        }

        return mGestureDetector.onTouchEvent(event)
    }

    private fun offSplitBtn() {

        endDraw()

        splitRL.isSelected = false

        splitTV.setTextColor(Color.parseColor("#333333"))
        splitTV.setTypeface(null, Typeface.NORMAL);

        drawer_view.visibility = View.GONE

    }

    private var editingPolygon: Polygon? = null
    private var polygon: Polygon? = null

    private fun drawPoint(geoPoint: LatLng) {
        val markerOptions = MarkerOptions()
        markerOptions.position(geoPoint)

        // markerOptions.title("Marker in Sydney")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        markerOptions.alpha(1.0f)
        markerOptions.draggable(true)

        val marker = googleMap.addMarker(markerOptions)

        if(mygps){
            marker.title = "현재 위치"
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            mygps = false
        }

        if(trackingChk){
            trackpoints.add(marker)
        }

        if(trackingPointChk){
            getTrackingPoints.add(marker)
        }

        val layerInfo = LayerInfo()
        layerInfo.attrubuteKey = getAttributeKey(layerInfo.layer)
        layerInfo.layer = currentLayer

        marker.tag = layerInfo
        println("marker.tag ${marker.id}")

        var myLayer = layerInfo.layer

        var attrubuteKey = layerInfo.attrubuteKey

        var intent: Intent? = null

        when (myLayer) {

            LAYER_BIRDS -> {

                var intent = Intent(this, BirdsActivity::class.java)

                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                marker.title = "조류"

                points.add(marker)
                allpoints.add(marker)

                intent.putExtra("latitude", geoPoint.latitude.toString())
                intent.putExtra("longitude", geoPoint.longitude.toString())
                intent.putExtra("markerid",marker.id)
                intent.putExtra("GROP_ID", attrubuteKey.toString())

                startActivityForResult(intent, BIRDS_DATA)

                endDraw()
            }

            LAYER_REPTILIA -> {
                intent = Intent(this, ReptiliaActivity::class.java)

                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                marker.title = "양서,파충류"

                points.add(marker)
                allpoints.add(marker)

                intent!!.putExtra("latitude", geoPoint.latitude.toString())
                intent!!.putExtra("longitude", geoPoint.longitude.toString())
                intent!!.putExtra("markerid",marker.id)
                intent!!.putExtra("GROP_ID", attrubuteKey.toString())

                startActivityForResult(intent, REPTILIA_DATA)

                endDraw()

            }

            LAYER_MAMMALIA -> {
                intent = Intent(this, MammaliaActivity::class.java)

                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                marker.title = "포유류"

                points.add(marker)
                allpoints.add(marker)

                intent!!.putExtra("latitude", geoPoint.latitude.toString())
                intent!!.putExtra("longitude", geoPoint.longitude.toString())
                intent!!.putExtra("markerid",marker.id)
                intent!!.putExtra("GROP_ID", attrubuteKey.toString())

                startActivityForResult(intent, MAMMALIA_DATA)

                endDraw()
            }

            LAYER_FISH -> {
                intent = Intent(this, FishActivity::class.java)

                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                marker.title = "어류"

                points.add(marker)
                allpoints.add(marker)

                intent!!.putExtra("latitude", geoPoint.latitude.toString())
                intent!!.putExtra("longitude", geoPoint.longitude.toString())
                intent!!.putExtra("markerid",marker.id)
                intent!!.putExtra("GROP_ID", attrubuteKey.toString())

                startActivityForResult(intent, FISH_DATA)

                endDraw()
            }

            LAYER_INSECT -> {
                intent = Intent(this, InsectActivity::class.java)

                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                marker.title = "곤충"

                points.add(marker)
                allpoints.add(marker)

                intent!!.putExtra("latitude", geoPoint.latitude.toString())
                intent!!.putExtra("longitude", geoPoint.longitude.toString())
                intent!!.putExtra("markerid",marker.id)
                intent!!.putExtra("GROP_ID", attrubuteKey.toString())

                startActivityForResult(intent, INSECT_DATA)

                endDraw()
            }

            LAYER_FLORA -> {
                intent = Intent(this, FloraActivity::class.java)

                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                marker.title = "식물"

                points.add(marker)
                allpoints.add(marker)

                intent!!.putExtra("latitude", geoPoint.latitude.toString())
                intent!!.putExtra("longitude", geoPoint.longitude.toString())
                intent!!.putExtra("markerid",marker.id)
                intent!!.putExtra("GROP_ID", attrubuteKey.toString())

                startActivityForResult(intent, FLORA_DATA)

                endDraw()
            }

            LAYER_ZOOBENTHOS -> {
                intent = Intent(this, ZoobenthosActivity::class.java)

                marker.title = "저서무척추동물"

                points.add(marker)
                allpoints.add(marker)

                intent!!.putExtra("latitude", geoPoint.latitude.toString())
                intent!!.putExtra("longitude", geoPoint.longitude.toString())
                intent!!.putExtra("markerid",marker.id)
                intent!!.putExtra("GROP_ID", attrubuteKey.toString())

                startActivityForResult(intent, FLORA_DATA)

                endDraw()
            }

            LAYER_MYLOCATION -> {

            }

            LAYER -> {

            }

            TRACKING -> {
                marker.title = "이동 경로"
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))

                points.add(marker)
                allpoints.add(marker)
            }

        }

        chkDivision = false

    }

    private fun initEditingPolygon() {
        val polygonOptions = PolygonOptions()
        polygonOptions.fillColor(getColor())
        polygonOptions.strokeWidth(1.0f)
        polygonOptions.strokeColor(Color.BLACK)
        polygonOptions.addAll(latlngs)

        editingPolygon = googleMap.addPolygon(polygonOptions)
        polygon = googleMap.addPolygon(polygonOptions)
        // editingPolygon?.zIndex = 5.0f
        editingPolygon?.isClickable = true
        polygon?.isClickable = true

        val layerInfo = LayerInfo()
        layerInfo.attrubuteKey = getAttributeKey(layerInfo.layer)
        layerInfo.layer = currentLayer

        editingPolygon?.tag = layerInfo
        polygon?.tag = layerInfo

    }

    private fun drawPolygon() {

        updateDelPointText()

        if (latlngs.size == 0) {

            editingPolygon = null
            polygon = null

            return

        }

        editingPolygon!!.points = latlngs
        polygon!!.points = latlngs

        if(latlngs.size >= 3) {
            val jtsEditPolygon = toJTSPolygon(editingPolygon!!)
            val jtsPolygon = toJTSPolygon(polygon!!)
            if(!jtsEditPolygon.isValid) {

                latlngs.remove(latlngs.last())
                drawPolygon()

                Utils.showNotification(context, "잘못된 지점입니다. 다시 선택해주세요.")

                latlngs.clear()

            }

        }
    }

    private fun startDraw() {
        drawer_view.visibility = View.VISIBLE

        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.setAllGesturesEnabled(false)

        // 3. biotope  , 6.birds , 7.Reptilia , 8.mammalia  9. fish, 10.insect, 11.flora , 13. zoobenthos
        when(currentLayer){

            LAYER_BIOTOPE -> {
                btn_biotope.text = "비오톱 추가 중"
            }

            LAYER_BIRDS -> {
                btn_birds.text = "조류 추가 중"
            }

            LAYER_REPTILIA -> {
                btn_Reptilia.text = "양서ㆍ파충류 추가 중"
            }

            LAYER_MAMMALIA -> {
                btn_mammalia.text = "포유류 추가 중"
            }

            LAYER_FISH -> {
                btn_fish.text = "어류 추가 중"
            }

            LAYER_INSECT -> {
                btn_insect.text = "곤충 추가 중"
            }

            LAYER_FLORA -> {
                btn_flora.text = "식물상 추가 중"
            }

            LAYER_ZOOBENTHOS -> {
                btn_zoobenthos.text = "저서무척추동물 추가 중"
            }

            LAYER_MYLOCATION -> {
                btn_mygps.text = "내 위치로 이동"
            }

        }
        chkDivision = true
    }

    fun endPolygonDraw(polygon:Polygon) {

        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.uiSettings.isRotateGesturesEnabled = false

        if(latlngs.size < 3){
            Toast.makeText(this,"세곳 이상 클릭해주세요.",Toast.LENGTH_SHORT).show()
        }

        if (latlngs.size == 0) {

            editingPolygon = null
            this.polygon = null

            return

        }

        if(latlngs.size >= 3 && polygon != null) {

            val layerInfo = polygon.tag as LayerInfo

            var myLayer = layerInfo.layer

            var attrubuteKey = layerInfo.attrubuteKey

            intent = Intent(this, BiotopeActivity::class.java)

            intent!!.putExtra("GROP_ID", attrubuteKey.toString())
            intent!!.putExtra("latitude", polygon.points.get(0).latitude.toString())
            intent!!.putExtra("longitude", polygon.points.get(0).longitude.toString())
            intent!!.putExtra("polygonid",polygon.id)

            startActivityForResult(intent, BIOTOPE_DATA);

            editingPolygon!!.points = latlngs
            this.polygon!!.points = latlngs

            val jtsEditPolygon = toJTSPolygon(editingPolygon!!)
            val jtsPolygon = toJTSPolygon(polygon)

            polygons.add(polygon)
            allPolygons.add(polygon)

            if(!jtsPolygon.isValid) {

                latlngs.remove(latlngs.last())
                drawPolygon()

                Utils.showNotification(context, "잘못된 지점입니다. 다시 선택해주세요.")

            }

            latlngs.clear()
            updateDelPointText()

            drawer_view.visibility = View.GONE

            when (currentLayer) {

                LAYER_BIOTOPE -> {
                    btn_biotope.text = "비오톱 추가"
                }

                LAYER_BIRDS -> {
                    btn_birds.text = "조류 추가"
                }

                LAYER_REPTILIA -> {
                    btn_Reptilia.text = "양서ㆍ파충류 추가"
                }

                LAYER_MAMMALIA -> {
                    btn_mammalia.text = "포유류 추가"
                }

                LAYER_FISH -> {
                    btn_fish.text = "어류 추가"
                }

                LAYER_INSECT -> {
                    btn_insect.text = "곤충 추가"
                }

                LAYER_FLORA -> {
                    btn_flora.text = "식물상 추가"
                }

                LAYER_ZOOBENTHOS -> {
                    btn_zoobenthos.text = "저서무척추동물 추가"
                }

                LAYER_MYLOCATION -> {
                    btn_mygps.text = "내 위치로 이동"
                }

            }

            currentLayer = -1
            chkDivision = false

        }
    }

    fun endDraw() {

        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.uiSettings.isRotateGesturesEnabled = false

        latlngs.clear()
        updateDelPointText()

        drawer_view.visibility = View.GONE

        when(currentLayer){

            LAYER_BIOTOPE -> {
                btn_biotope.text = "비오톱 추가"
            }

            LAYER_BIRDS -> {
                btn_birds.text = "조류 추가"
            }

            LAYER_REPTILIA -> {
                btn_Reptilia.text = "양서ㆍ파충류 추가"
            }

            LAYER_MAMMALIA -> {
                btn_mammalia.text = "포유류 추가"
            }

            LAYER_FISH -> {
                btn_fish.text = "어류 추가"
            }

            LAYER_INSECT -> {
                btn_insect.text = "곤충 추가"
            }

            LAYER_FLORA -> {
                btn_flora.text = "식물상 추가"
            }

            LAYER_ZOOBENTHOS -> {
                btn_zoobenthos.text = "저서무척추동물 추가"
            }

            LAYER_MYLOCATION -> {
                btn_mygps.text = "내 위치로 이동"
            }

        }

        currentLayer = -1

        chkDivision = false

    }

    private fun updateDelPointText() {
        delPointTV.text = "포인트 취소 (${latlngs.size})"

        if(latlngs.size == 0) {
            delPointTV.visibility = View.GONE
        } else {
            delPointTV.visibility = View.VISIBLE
        }
    }

    private fun getAttributeKey(layer: Int):String {

//        var attributeKey = Utils.getToday("yyyy-MM-dd") + "_" + System.currentTimeMillis()
        var attributeKey:String = System.currentTimeMillis().toString()

        var myLayer = currentLayer
        if(myLayer == -1 && layer > 0) {
            myLayer = layer
        }

        when(myLayer) {
            LAYER_BIOTOPE -> {
                attributeKey += "biotope"
            }

            LAYER_BIRDS -> {
                attributeKey += "birds"
            }

            LAYER_REPTILIA -> {
                attributeKey += "reptilia"
            }

            LAYER_MAMMALIA -> {
                attributeKey += "mammalia"
            }

            LAYER_FISH -> {
                attributeKey += "fish"
            }

            LAYER_INSECT -> {
                attributeKey += "insect"
            }

            LAYER_FLORA -> {
                attributeKey += "flora"
            }

            LAYER_ZOOBENTHOS -> {
                attributeKey += "zoobenthos"
            }

            LAYER_MYLOCATION -> {
                attributeKey += "mylocation"
            }

            LAYER -> {
            }

            TRACKING -> {
                attributeKey += "tracking"
            }

        }

        return attributeKey

    }

    fun exportBiotope() {
        var biotopeArray: ArrayList<Exporter.ExportItem> = ArrayList<Exporter.ExportItem>()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*")
        var biotopedata = db.query("biotopeAttribute", dataList, null, null, "GROP_ID", null, "", null)
        var chkData = false

        while (biotopedata.moveToNext()) {
            var biotope_attribute: Biotope_attribute = Biotope_attribute(biotopedata.getString(0), biotopedata.getString(1), biotopedata.getString(2), biotopedata.getString(3)
                    , biotopedata.getString(4), biotopedata.getString(5), biotopedata.getString(6), biotopedata.getInt(7),
                    biotopedata.getString(8), biotopedata.getFloat(9), biotopedata.getFloat(10), biotopedata.getString(11), biotopedata.getString(12), biotopedata.getString(13), biotopedata.getFloat(14)
                    , biotopedata.getString(15), biotopedata.getString(16), biotopedata.getString(17), biotopedata.getString(18), biotopedata.getString(19), biotopedata.getString(20), biotopedata.getString(21)
                    , biotopedata.getString(22), biotopedata.getString(23), biotopedata.getString(24), biotopedata.getString(25), biotopedata.getFloat(26), biotopedata.getFloat(27), biotopedata.getFloat(28)
                    , biotopedata.getString(29), biotopedata.getString(30), biotopedata.getString(31), biotopedata.getFloat(32), biotopedata.getFloat(33), biotopedata.getFloat(34), biotopedata.getString(35)
                    , biotopedata.getString(36), biotopedata.getString(37), biotopedata.getFloat(38), biotopedata.getFloat(39), biotopedata.getString(40), biotopedata.getString(41), biotopedata.getString(42)
                    , biotopedata.getFloat(43), biotopedata.getFloat(44), biotopedata.getString(45), biotopedata.getString(46), biotopedata.getString(47), biotopedata.getString(48), biotopedata.getDouble(49)
                    , biotopedata.getDouble(50), biotopedata.getString(51), biotopedata.getString(52), biotopedata.getString(53))

            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString, biotope_attribute.id))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString, biotope_attribute.GROP_ID))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString, biotope_attribute.PRJ_NAME))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTInteger, biotope_attribute.INV_REGION))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTReal, biotope_attribute.INV_PERSON))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString, biotope_attribute.INV_DT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString, biotope_attribute.INV_TM))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_INDEX", ogr.OFTInteger, biotope_attribute.INV_INDEX))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("LU_GR_NUM", ogr.OFTString, biotope_attribute.LU_GR_NUM))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("LU_TY_RATE", ogr.OFTReal, biotope_attribute.LU_TY_RATE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STAND_H", ogr.OFTReal, biotope_attribute.STAND_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("LC_GR_NUM", ogr.OFTString, biotope_attribute.LC_GR_NUM))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("LC_TY", ogr.OFTString, biotope_attribute.LC_TY))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TY_MARK", ogr.OFTString, biotope_attribute.TY_MARK))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GV_RATE", ogr.OFTReal, biotope_attribute.GV_RATE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GV_STRUCT", ogr.OFTString, biotope_attribute.GV_STRUCT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("DIS_RET", ogr.OFTString, biotope_attribute.DIS_RET))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("RESTOR_POT", ogr.OFTString, biotope_attribute.RESTOR_POT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("COMP_INTA", ogr.OFTString, biotope_attribute.COMP_INTA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("VP_INTA", ogr.OFTString, biotope_attribute.VP_INTA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("IMP_FORM", ogr.OFTString, biotope_attribute.IMP_FORM))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("BREA_DIA", ogr.OFTString, biotope_attribute.BREA_DIA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("FIN_EST", ogr.OFTString, biotope_attribute.FIN_EST))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_SPEC", ogr.OFTString, biotope_attribute.TRE_SPEC))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_FAMI", ogr.OFTString, biotope_attribute.TRE_FAMI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_SCIEN", ogr.OFTString, biotope_attribute.TRE_SCIEN))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_H", ogr.OFTReal, biotope_attribute.TRE_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_BREA", ogr.OFTReal, biotope_attribute.TRE_BREA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_COVE", ogr.OFTReal, biotope_attribute.TRE_COVE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_SPEC", ogr.OFTString, biotope_attribute.STRE_SPEC))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_FAMI", ogr.OFTString, biotope_attribute.STRE_FAMI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_SCIEN", ogr.OFTString, biotope_attribute.STRE_SCIEN))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_H", ogr.OFTReal, biotope_attribute.STRE_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_BREA", ogr.OFTReal, biotope_attribute.STRE_BREA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_COVE", ogr.OFTReal, biotope_attribute.STRE_COVE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("SHR_SPEC", ogr.OFTString, biotope_attribute.SHR_SPEC))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("SHR_FAMI", ogr.OFTString, biotope_attribute.SHR_FAMI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("SHR_SCIEN", ogr.OFTString, biotope_attribute.SHR_SCIEN))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("SHR_H", ogr.OFTReal, biotope_attribute.SHR_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STR_COVE", ogr.OFTReal, biotope_attribute.STR_COVE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_SPEC", ogr.OFTString, biotope_attribute.HER_SPEC))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_FAMI", ogr.OFTString, biotope_attribute.HER_FAMI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_SCIEN", ogr.OFTString, biotope_attribute.HER_SCIEN))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_H", ogr.OFTReal, biotope_attribute.HER_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_COVE", ogr.OFTReal, biotope_attribute.HER_COVE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("PIC_FOLDER", ogr.OFTString, biotope_attribute.PIC_FOLDER))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("WILD_ANI", ogr.OFTString, biotope_attribute.WILD_ANI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("BIOTOP_POT", ogr.OFTString, biotope_attribute.BIOTOP_POT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("UNUS_NOTE", ogr.OFTString, biotope_attribute.UNUS_NOTE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal, biotope_attribute.GPS_LAT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal, biotope_attribute.GPS_LON))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("NEED_CONF", ogr.OFTString, biotope_attribute.NEED_CONF))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString, biotope_attribute.CONF_MOD))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString, biotope_attribute.TEMP_YN))

            biotopeDatas.add(biotope_attribute)

        }

        if (biotopeDatas.size != null) {

            println("biotopeDatas.size ${biotopeDatas.size}")

            for (i in 0..biotopeDatas.size - 1) {

                val grop_id = biotopeDatas.get(i).GROP_ID

                if (allPolygons.size > 0) {

                    println(allPolygons.size.toString() + "----------------------------")

                    for (j in 0..allPolygons.size - 1) {

                        println("layerinfo ---- ${allPolygons.get(j).tag}")
                        if(allPolygons.get(j).tag != null) {
                            val layerInfo = allPolygons.get(j).tag as LayerInfo

                            var attrubuteKey = layerInfo.attrubuteKey

                            println("attrubutekey $attrubuteKey")
                            if (attrubuteKey.equals(grop_id)) {

                                val exporter = Exporter.ExportItem(LAYER_BIOTOPE, BIOTOPEATTRIBUTE, allPolygons.get(j))

                                biotopeArray.add(exporter)

                                println("adddddddddddd")

                            }
                        }
                    }
                }

                val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "biotope" + File.separator + "biotope"

                val layerData= db.query("layers", dataList, "file_name = '$file_path'", null, null ,null, "", null)

                while (layerData.moveToNext()){
                    chkData = true
                }

                if(chkData){

                }else {
                    dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "biotope" + File.separator + "biotope", "비오톱", "biotope", "Y")
                }

                Exporter.export(biotopeArray)


            }
            biotopeDatas.clear()
        }

    }

    fun exportBirds(){
        var pointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*")
        val birdsdata= db.query("birdsAttribute", dataList, null, null, "GROP_ID", null, "", null)
        var datas:ArrayList<Birds_attribute> = ArrayList<Birds_attribute>()
        var chkData = false
        while (birdsdata.moveToNext()) {

            var birds_attribute: Birds_attribute = Birds_attribute(birdsdata.getString(0), birdsdata.getString(1), birdsdata.getString(2), birdsdata.getString(3), birdsdata.getString(4), birdsdata.getString(5), birdsdata.getString(6), birdsdata.getString(7),
                    birdsdata.getString(8), birdsdata.getFloat(9), birdsdata.getString(10), birdsdata.getInt(11), birdsdata.getString(12), birdsdata.getString(13), birdsdata.getString(14)
                    , birdsdata.getString(15),birdsdata.getString(16), birdsdata.getInt(17), birdsdata.getString(18), birdsdata.getString(19), birdsdata.getString(20), birdsdata.getString(21), birdsdata.getString(22)
                    , birdsdata.getString(23), birdsdata.getString(24), birdsdata.getFloat(25), birdsdata.getFloat(26), birdsdata.getString(27), birdsdata.getString(28))

            datas.add(birds_attribute)

        }

        if(datas != null){
            for(i in 0..datas.size-1){
                println("-----datassize${datas.size}")

                val data = datas.get(i)

                val birdsdata= db.query("birdsAttribute", dataList, "GROP_ID = '${data.GROP_ID}'", null, null, null, "", null)

                while (birdsdata.moveToNext()) {

                    var birds_attribute: Birds_attribute = Birds_attribute(birdsdata.getString(0), birdsdata.getString(1), birdsdata.getString(2), birdsdata.getString(3), birdsdata.getString(4), birdsdata.getString(5), birdsdata.getString(6), birdsdata.getString(7),
                            birdsdata.getString(8), birdsdata.getFloat(9), birdsdata.getString(10), birdsdata.getInt(11), birdsdata.getString(12), birdsdata.getString(13), birdsdata.getString(14)
                            , birdsdata.getString(15),birdsdata.getString(16), birdsdata.getInt(17), birdsdata.getString(18), birdsdata.getString(19), birdsdata.getString(20), birdsdata.getString(21), birdsdata.getString(22)
                            , birdsdata.getString(23), birdsdata.getString(24), birdsdata.getFloat(25), birdsdata.getFloat(26), birdsdata.getString(27), birdsdata.getString(28))

                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,birds_attribute.id))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,birds_attribute.GROP_ID))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,birds_attribute.PRJ_NAME))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString,birds_attribute.INV_REGION))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,birds_attribute.INV_DT))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString,birds_attribute.INV_PERSON))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString,birds_attribute.WEATHER))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString,birds_attribute.WIND))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString,birds_attribute.WIND_DIRE))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal,birds_attribute.TEMPERATUR))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString,birds_attribute.ETC))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger,birds_attribute.NUM))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,birds_attribute.INV_TM))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString,birds_attribute.SPEC_NM))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString,birds_attribute.FAMI_NM))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString,birds_attribute.SCIEN_NM))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("ENDANGERED", ogr.OFTString,birds_attribute.ENDANGERED))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("INDI_CNT", ogr.OFTInteger,birds_attribute.INDI_CNT))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("OBS_STAT", ogr.OFTString,birds_attribute.OBS_STAT))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("OBS_ST_ETC", ogr.OFTString,birds_attribute.OBS_ST_ETC))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("USE_TAR", ogr.OFTString,birds_attribute.USE_TAR))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("USE_TAR_SP", ogr.OFTString,birds_attribute.USE_TAR_SP))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("USE_LAYER", ogr.OFTString,birds_attribute.USE_LAYER))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("MJ_ACT", ogr.OFTString,birds_attribute.MJ_ACT))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("MJ_ACT_PR", ogr.OFTString,birds_attribute.MJ_ACT_PR))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,birds_attribute.GPS_LAT))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,birds_attribute.GPS_LON))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,birds_attribute.TEMP_YN))
                    BIRDSATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,birds_attribute.CONF_MOD))

                    birdsDatas.add(birds_attribute)
                }
            }
        }

        if(birdsDatas.size > 0) {

            for (i in 0..birdsDatas.size - 1) {

                birdsPk += birdsDatas.get(i).id + "\n"

                val grop_id = birdsDatas.get(i).GROP_ID

                if(points.size > 0) {

                    for (j in 0..points.size - 1) {

                        println(points.size.toString() + "sizeeeeeeeeeeeeeeeeeeeeeeeeeeeee${points.get(j).tag}")
                            if (points.get(j).tag != null) {
                            val layerInfo = points.get(j).tag as LayerInfo

                            var attrubuteKey = layerInfo.attrubuteKey

                            if (attrubuteKey.equals(grop_id)) {

                                val exporter = Exporter.ExportPointItem(LAYER_BIRDS, BIRDSATTRIBUTE, points.get(j))

                                pointsArray.add(exporter)

                                Exporter.exportPoint(pointsArray)

                            }
                        }
                    }
                }
            }

            val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "birds" + File.separator + "birds"

            val layerData= db.query("layers", dataList, "file_name = '$file_path'", null, null ,null, "", null)

            while (layerData.moveToNext()){
                chkData = true
            }

            if(chkData){

            }else {
                dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "birds" + File.separator + "birds" ,"조류", "birds","Y")
            }

            pointsArray.clear()
            birdsDatas.clear()

        }
    }

    fun exportReptilia(){
        var pointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*")
        val reptiliadata= db.query("reptiliaAttribute", dataList, null, null, "GROP_ID", null, "", null)
        var datas: ArrayList<Reptilia_attribute> = ArrayList<Reptilia_attribute>()
        var chkData = false

        while (reptiliadata.moveToNext()) {

            var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(reptiliadata.getString(0), reptiliadata.getString(1), reptiliadata.getString(2), reptiliadata.getString(3), reptiliadata.getString(4), reptiliadata.getString(5), reptiliadata.getString(6), reptiliadata.getString(7),
                    reptiliadata.getString(8), reptiliadata.getFloat(9), reptiliadata.getString(10), reptiliadata.getInt(11), reptiliadata.getString(12), reptiliadata.getString(13), reptiliadata.getString(14)
                    , reptiliadata.getString(15), reptiliadata.getString(16),reptiliadata.getInt(17), reptiliadata.getInt(18), reptiliadata.getInt(19), reptiliadata.getString(20), reptiliadata.getString(21), reptiliadata.getString(22)
                    , reptiliadata.getString(23), reptiliadata.getString(24), reptiliadata.getString(25), reptiliadata.getInt(26), reptiliadata.getInt(27), reptiliadata.getInt(28), reptiliadata.getFloat(29), reptiliadata.getFloat(30),reptiliadata.getString(31),reptiliadata.getString(32))

            datas.add(reptilia_attribute)
        }

        if(datas != null){

            for(i in 0..datas.size-1){
                val data = datas.get(i)

                val reptiliadata= db.query("reptiliaAttribute", dataList, "GROP_ID = '${data.GROP_ID}'", null, null, null, "", null)

                while (reptiliadata.moveToNext()) {

                    var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(reptiliadata.getString(0), reptiliadata.getString(1), reptiliadata.getString(2), reptiliadata.getString(3), reptiliadata.getString(4), reptiliadata.getString(5), reptiliadata.getString(6), reptiliadata.getString(7),
                            reptiliadata.getString(8), reptiliadata.getFloat(9), reptiliadata.getString(10), reptiliadata.getInt(11), reptiliadata.getString(12), reptiliadata.getString(13), reptiliadata.getString(14)
                            , reptiliadata.getString(15), reptiliadata.getString(16),reptiliadata.getInt(17), reptiliadata.getInt(18), reptiliadata.getInt(19), reptiliadata.getString(20), reptiliadata.getString(21), reptiliadata.getString(22)
                            , reptiliadata.getString(23), reptiliadata.getString(24), reptiliadata.getString(25), reptiliadata.getInt(26), reptiliadata.getInt(27), reptiliadata.getInt(28), reptiliadata.getFloat(29), reptiliadata.getFloat(30),reptiliadata.getString(31),reptiliadata.getString(32))

                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,reptilia_attribute.id))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,reptilia_attribute.GROP_ID))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,reptilia_attribute.PRJ_NAME))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString,reptilia_attribute.INV_REGION))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,reptilia_attribute.INV_DT))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString,reptilia_attribute.INV_PERSON))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString,reptilia_attribute.WEATHER))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString,reptilia_attribute.WIND))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString,reptilia_attribute.WIND_DIRE))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal,reptilia_attribute.TEMPERATUR))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString,reptilia_attribute.ETC))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger,reptilia_attribute.NUM))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,reptilia_attribute.INV_TM))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString,reptilia_attribute.SPEC_NM))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString,reptilia_attribute.FAMI_NM))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString,reptilia_attribute.SCIEN_NM))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("IN_CNT_ADU", ogr.OFTInteger,reptilia_attribute.IN_CNT_ADU))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("IN_CNT_LAR", ogr.OFTInteger,reptilia_attribute.IN_CNT_LAR))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("IN_CNT_EGG", ogr.OFTInteger,reptilia_attribute.IN_CNT_EGG))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("HAB_RIVEER", ogr.OFTString,reptilia_attribute.HAB_RIVEER))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("HAB_EDGE", ogr.OFTString,reptilia_attribute.HAB_EDGE))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_IN", ogr.OFTString,reptilia_attribute.WATER_IN))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_OUT", ogr.OFTString,reptilia_attribute.WATER_OUT))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_CONT", ogr.OFTString,reptilia_attribute.WATER_CONT))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_QUAL", ogr.OFTString,reptilia_attribute.WATER_QUAL))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_DEPT", ogr.OFTInteger,reptilia_attribute.WATER_DEPT))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("HAB_AREA_W", ogr.OFTInteger,reptilia_attribute.HAB_AREA_W))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("HAB_AREA_H", ogr.OFTInteger,reptilia_attribute.HAB_AREA_H))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,reptilia_attribute.GPS_LAT))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,reptilia_attribute.GPS_LON))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,reptilia_attribute.TEMP_YN))
                    REPTILIAATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,reptilia_attribute.CONF_MOD))

                    reptiliaDatas.add(reptilia_attribute)

                }

            }

        }

        if(reptiliaDatas.size > 0) {

            for (i in 0..reptiliaDatas.size - 1) {
                reptiliaPk += reptiliaDatas.get(i).id + "\n"

                val grop_id = reptiliaDatas.get(i).GROP_ID

                if(points.size > 0) {

                    for (j in 0..points.size - 1) {

                        if (points.get(j).tag != null) {
                            val layerInfo = points.get(j).tag as LayerInfo

                            var attrubuteKey = layerInfo.attrubuteKey

                            if (attrubuteKey.equals(grop_id)) {

                                val exporter = Exporter.ExportPointItem(LAYER_REPTILIA, REPTILIAATTRIBUTE, points.get(j))

                                pointsArray.add(exporter)
                                Exporter.exportPoint(pointsArray)
                            }

                        }
                    }

                }
            }

            val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "reptilia" + File.separator + "reptilia"

            val layerData= db.query("layers", dataList, "file_name = '$file_path'", null, null ,null, "", null)

            while (layerData.moveToNext()){
                chkData = true
            }

            if(chkData){

            }else {
                dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "reptilia" + File.separator + "reptilia","양서,파충류", "reptilia","Y")
            }

            pointsArray.clear()
            reptiliaDatas.clear()
        }

    }

    fun exportMammal(){
        var pointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*")
        val mammaldata = db.query("mammalAttribute", dataList, null, null, "GROP_ID", null, "", null)
        var datas:ArrayList<Mammal_attribute> = ArrayList<Mammal_attribute>()
        var chkData = false

        while (mammaldata.moveToNext()) {

            var mammal_attribute: Mammal_attribute = Mammal_attribute(mammaldata.getString(0), mammaldata.getString(1), mammaldata.getString(2), mammaldata.getString(3), mammaldata.getString(4), mammaldata.getString(5), mammaldata.getString(6), mammaldata.getString(7),
                    mammaldata.getString(8), mammaldata.getFloat(9), mammaldata.getString(10), mammaldata.getInt(11), mammaldata.getString(12), mammaldata.getString(13), mammaldata.getString(14)
                    , mammaldata.getString(15), mammaldata.getString(16), mammaldata.getString(17), mammaldata.getString(18),mammaldata.getInt(19), mammaldata.getString(20), mammaldata.getString(21), mammaldata.getFloat(22)
                    , mammaldata.getFloat(23), mammaldata.getString(24), mammaldata.getString(25), mammaldata.getString(26), mammaldata.getString(27),mammaldata.getString(28),mammaldata.getString(29))

            datas.add(mammal_attribute)

        }

        if(datas != null){

            for (i in 0..datas.size-1) {
                val data = datas.get(i)

                val mammaldata = db.query("mammalAttribute", dataList, "GROP_ID = '${data.GROP_ID}'", null, null, null, "", null)

                while (mammaldata.moveToNext()) {

                    var mammal_attribute: Mammal_attribute = Mammal_attribute(mammaldata.getString(0), mammaldata.getString(1), mammaldata.getString(2), mammaldata.getString(3), mammaldata.getString(4), mammaldata.getString(5), mammaldata.getString(6), mammaldata.getString(7),
                            mammaldata.getString(8), mammaldata.getFloat(9), mammaldata.getString(10), mammaldata.getInt(11), mammaldata.getString(12), mammaldata.getString(13), mammaldata.getString(14)
                            , mammaldata.getString(15), mammaldata.getString(16), mammaldata.getString(17), mammaldata.getString(18),mammaldata.getInt(19), mammaldata.getString(20), mammaldata.getString(21), mammaldata.getFloat(22)
                            , mammaldata.getFloat(23), mammaldata.getString(24), mammaldata.getString(25), mammaldata.getString(26), mammaldata.getString(27),mammaldata.getString(28),mammaldata.getString(29))

                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString, mammal_attribute.id))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString, mammal_attribute.GROP_ID))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString, mammal_attribute.PRJ_NAME))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString, mammal_attribute.INV_REGION))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString, mammal_attribute.INV_DT))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString, mammal_attribute.INV_PERSON))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString, mammal_attribute.WEATHER))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString, mammal_attribute.WIND))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString, mammal_attribute.WIND_DIRE))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal, mammal_attribute.TEMPERATUR))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString, mammal_attribute.ETC))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger, mammal_attribute.NUM))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString, mammal_attribute.INV_TM))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString, mammal_attribute.SPEC_NM))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString, mammal_attribute.FAMI_NM))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString, mammal_attribute.SCIEN_NM))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("ENDANGERED", ogr.OFTString, mammal_attribute.ENDANGERED))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("OBS_TY", ogr.OFTString, mammal_attribute.OBS_TY))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("OBS_TY_ETC", ogr.OFTString, mammal_attribute.OBS_TY_ETC))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("INDI_CNT", ogr.OFTInteger, mammal_attribute.INDI_CNT))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("OB_PT_CHAR", ogr.OFTString, mammal_attribute.OB_PT_CHAR))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("UNUS_NOTE", ogr.OFTString, mammal_attribute.UNUS_NOTE))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal, mammal_attribute.GPS_LAT))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal, mammal_attribute.GPS_LON))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("UN_SPEC", ogr.OFTString, mammal_attribute.UN_SPEC))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("UN_SPEC_RE", ogr.OFTString, mammal_attribute.UN_SPEC_RE))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("TR_EASY", ogr.OFTString, mammal_attribute.TR_EASY))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("TR_EASY_RE", ogr.OFTString, mammal_attribute.TR_EASY_RE))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString, mammal_attribute.TEMP_YN))
                    MAMMALATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString, mammal_attribute.CONF_MOD))

                    mammaliaDatas.add(mammal_attribute)
                }
            }
        }

        if(mammaliaDatas.size > 0) {

            for (i in 0..mammaliaDatas.size - 1) {
                mammaliaPk += mammaliaDatas.get(i).id + "\n"

                val grop_id = mammaliaDatas.get(i).GROP_ID

                if(points.size > 0) {

                    for (j in 0..points.size - 1) {

                            if(points.get(j).tag != null){
                            val layerInfo = points.get(j).tag as LayerInfo

                            var attrubuteKey = layerInfo.attrubuteKey

                            if (attrubuteKey.equals(grop_id)) {

                                val exporter = Exporter.ExportPointItem(LAYER_MAMMALIA, MAMMALATTRIBUTE, points.get(j))

                                pointsArray.add(exporter)
                                Exporter.exportPoint(pointsArray)

                            }

                        }
                    }

                }
            }

            val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia" + File.separator + "mammalia"

            val layerData= db.query("layers", dataList, "file_name = '$file_path'", null, null ,null, "", null)

            while (layerData.moveToNext()){
                chkData = true
            }

            if(chkData){

            }else {
                dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia" + File.separator + "mammalia","포유류", "mammalia","Y")
            }

            pointsArray.clear()
            mammaliaDatas.clear()
        }
    }

    fun exportFish(){
        var pointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*")
        val fishdata = db.query("fishAttribute", dataList, null, null, "GROP_ID", null, "", null)
        var datas: ArrayList<Fish_attribute> = ArrayList<Fish_attribute>()
        var chkData = false

        while (fishdata.moveToNext()) {

            var fish_attribute: Fish_attribute = Fish_attribute(fishdata.getString(0), fishdata.getString(1), fishdata.getString(2), fishdata.getString(3), fishdata.getString(4), fishdata.getString(5), fishdata.getString(6), fishdata.getString(7),
                    fishdata.getString(8),fishdata.getString(9), fishdata.getFloat(10), fishdata.getString(11), fishdata.getString(12), fishdata.getString(13), fishdata.getInt(14), fishdata.getString(15), fishdata.getInt(16), fishdata.getInt(17), fishdata.getString(18),
                    fishdata.getFloat(19), fishdata.getFloat(20), fishdata.getString(21), fishdata.getInt(22), fishdata.getInt(23), fishdata.getInt(24), fishdata.getInt(25), fishdata.getString(26), fishdata.getString(27), fishdata.getString(28),
                    fishdata.getInt(29) ,fishdata.getString(30), fishdata.getString(31), fishdata.getString(32), fishdata.getInt(33), fishdata.getString(33), fishdata.getString(35), fishdata.getString(36),fishdata.getString(37),fishdata.getString(38))

            datas.add(fish_attribute)
        }

        if(datas != null) {
            for (i in 0..datas.size - 1) {
                val data = datas.get(i)

                val fishdata = db.query("fishAttribute", dataList, "GROP_ID = '${data.GROP_ID}'", null, null, null, "", null)

                while (fishdata.moveToNext()) {
                    var fish_attribute: Fish_attribute = Fish_attribute(fishdata.getString(0), fishdata.getString(1), fishdata.getString(2), fishdata.getString(3), fishdata.getString(4), fishdata.getString(5), fishdata.getString(6), fishdata.getString(7),
                            fishdata.getString(8),fishdata.getString(9), fishdata.getFloat(10), fishdata.getString(11), fishdata.getString(12), fishdata.getString(13), fishdata.getInt(14), fishdata.getString(15), fishdata.getInt(16), fishdata.getInt(17), fishdata.getString(18),
                            fishdata.getFloat(19), fishdata.getFloat(20), fishdata.getString(21), fishdata.getInt(22), fishdata.getInt(23), fishdata.getInt(24), fishdata.getInt(25), fishdata.getString(26), fishdata.getString(27), fishdata.getString(28),
                            fishdata.getInt(29) ,fishdata.getString(30), fishdata.getString(31), fishdata.getString(32), fishdata.getInt(33), fishdata.getString(33), fishdata.getString(35), fishdata.getString(36),fishdata.getString(37),fishdata.getString(38))

                    FISHATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString, fish_attribute.id))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString, fish_attribute.GROP_ID))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString, fish_attribute.PRJ_NAME))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString, fish_attribute.INV_REGION))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString, fish_attribute.INV_DT))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString, fish_attribute.INV_TM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString, fish_attribute.INV_PERSON))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString, fish_attribute.WEATHER))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString, fish_attribute.WIND))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString, fish_attribute.WIND_DIRE))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal, fish_attribute.TEMPERATUR))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString, fish_attribute.ETC))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("MID_RAGE", ogr.OFTString, fish_attribute.MID_RAGE))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("CODE_NUM", ogr.OFTString, fish_attribute.CODE_NUM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("RIVER_NUM", ogr.OFTInteger, fish_attribute.RIVER_NUM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("RIVER_NM", ogr.OFTString, fish_attribute.RIVER_NM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("NET_CNT", ogr.OFTInteger, fish_attribute.NET_CNT))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("NET_MIN", ogr.OFTInteger, fish_attribute.NET_MIN))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("AD_DIST_NM", ogr.OFTString, fish_attribute.AD_DIST_NM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal, fish_attribute.GPS_LAT))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal, fish_attribute.GPS_LON))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("COLL_TOOL", ogr.OFTString, fish_attribute.COLL_TOOL))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("STREAM_W", ogr.OFTString, fish_attribute.STREAM_W))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("WATER_W", ogr.OFTInteger, fish_attribute.WATER_W))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("WATER_D", ogr.OFTInteger, fish_attribute.WATER_D))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("WATER_CUR", ogr.OFTInteger, fish_attribute.WATER_CUR))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("RIV_STR", ogr.OFTString, fish_attribute.RIV_STR))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("RIV_STR_IN", ogr.OFTString, fish_attribute.RIV_STR_IN))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("RIV_FORM", ogr.OFTString, fish_attribute.RIV_FORM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger, fish_attribute.NUM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString, fish_attribute.SPEC_NM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString, fish_attribute.FAMI_NM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString, fish_attribute.SCIEN_NM))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("INDI_CNT", ogr.OFTInteger, fish_attribute.INDI_CNT))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("UNIDENT", ogr.OFTString, fish_attribute.UNIDENT))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("RIV_FM_CH", ogr.OFTString, fish_attribute.RIV_FM_CH))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("UN_FISH_CH", ogr.OFTString, fish_attribute.UN_FISH_CH))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString, fish_attribute.TEMP_YN))
                    FISHATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString, fish_attribute.CONF_MOD))

                    fishDatas.add(fish_attribute)

                }
            }
        }

        if(fishDatas.size > 0 ) {

            for (i in 0..fishDatas.size - 1) {
                fishPk += fishDatas.get(i).id + "\n"

                val grop_id = fishDatas.get(i).GROP_ID

                if (points.size > 0) {

                    for (j in 0..points.size - 1) {

                        if (points.get(j).tag != null) {
                            val layerInfo = points.get(j).tag as LayerInfo

                            var attrubuteKey = layerInfo.attrubuteKey
                            if (attrubuteKey.equals(grop_id)) {

                                val exporter = Exporter.ExportPointItem(LAYER_FISH, FISHATTRIBUTE, points.get(j))

                                pointsArray.add(exporter)
                                Exporter.exportPoint(pointsArray)

                            }

                        }
                    }

                }
            }

            val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish" + File.separator + "fish"

            val layerData= db.query("layers", dataList, "file_name = '$file_path'", null, null ,null, "", null)

            while (layerData.moveToNext()){
                chkData = true
            }

            if(chkData){

            }else {
                dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish" + File.separator + "fish","어류", "fish","Y")
            }

            pointsArray.clear()
            fishDatas.clear()
        }
    }

    fun exportInsects(){
        var pointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*")
        val insectdata = db.query("insectAttribute", dataList, null, null, "GROP_ID", null, "", null)
        var datas: ArrayList<Insect_attribute> = ArrayList<Insect_attribute>()
        var chkData = false

        while (insectdata.moveToNext()) {

            var insect_attribute: Insect_attribute = Insect_attribute(insectdata.getString(0), insectdata.getString(1), insectdata.getString(2), insectdata.getString(3), insectdata.getString(4)
                    , insectdata.getString(5), insectdata.getString(6), insectdata.getString(7), insectdata.getString(8), insectdata.getFloat(9), insectdata.getString(10), insectdata.getInt(11)
                    , insectdata.getString(12), insectdata.getString(13), insectdata.getString(14), insectdata.getString(15), insectdata.getInt(16), insectdata.getString(17), insectdata.getString(18)
                    , insectdata.getString(19), insectdata.getString(20), insectdata.getString(21), insectdata.getString(22), insectdata.getString(23), insectdata.getString(24), insectdata.getString(25)
                    , insectdata.getFloat(26), insectdata.getFloat(27), insectdata.getString(28), insectdata.getString(29))

            datas.add(insect_attribute)

        }

        if(datas != null){
            for(i in 0..datas.size-1){
                val data = datas.get(i)

                val insectdata = db.query("insectAttribute", dataList, "GROP_ID = '${data.GROP_ID}'", null, null, null, "", null)

                while (insectdata.moveToNext()) {

                    var insect_attribute: Insect_attribute = Insect_attribute(insectdata.getString(0), insectdata.getString(1), insectdata.getString(2), insectdata.getString(3), insectdata.getString(4)
                            , insectdata.getString(5), insectdata.getString(6), insectdata.getString(7), insectdata.getString(8), insectdata.getFloat(9), insectdata.getString(10), insectdata.getInt(11)
                            , insectdata.getString(12), insectdata.getString(13), insectdata.getString(14), insectdata.getString(15), insectdata.getInt(16), insectdata.getString(17), insectdata.getString(18)
                            , insectdata.getString(19), insectdata.getString(20), insectdata.getString(21), insectdata.getString(22), insectdata.getString(23), insectdata.getString(24), insectdata.getString(25)
                            , insectdata.getFloat(26), insectdata.getFloat(27), insectdata.getString(28), insectdata.getString(29))

                    INSECTATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,insect_attribute.id))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,insect_attribute.GROP_ID))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,insect_attribute.PRJ_NAME))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString,insect_attribute.INV_REGION))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,insect_attribute.INV_DT))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString,insect_attribute.INV_PERSON))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString,insect_attribute.WEATHER))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString,insect_attribute.WIND))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString,insect_attribute.WIND_DIRE))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal,insect_attribute.TEMPERATUR))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString,insect_attribute.ETC))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger,insect_attribute.NUM))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,insect_attribute.INV_TM))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString,insect_attribute.SPEC_NM))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString,insect_attribute.FAMI_NM))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString,insect_attribute.SCIEN_NM))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("INDI_CNT", ogr.OFTInteger,insect_attribute.INDI_CNT))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("OBS_STAT", ogr.OFTString,insect_attribute.OBS_STAT))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("OBS_ST_ETC", ogr.OFTString,insect_attribute.OBS_ST_ETC))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("USE_TAR", ogr.OFTString,insect_attribute.USE_TAR))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("USER_TA_ETC", ogr.OFTString,insect_attribute.USER_TA_ETC))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("MJ_ACT", ogr.OFTString,insect_attribute.MJ_ACT))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("MJ_ACT_ETC", ogr.OFTString,insect_attribute.MJ_ACT_ETC))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_MEAN", ogr.OFTString,insect_attribute.INV_MEAN))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_MN_ETC", ogr.OFTString,insect_attribute.INV_MN_ETC))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("UNUS_NOTE", ogr.OFTString,insect_attribute.UNUS_NOTE))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,insect_attribute.GPS_LAT))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,insect_attribute.GPS_LON))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,insect_attribute.TEMP_YN))
                    INSECTATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,insect_attribute.CONF_MOD))

                    insectDatas.add(insect_attribute)

                }
            }
        }

        if(insectDatas.size > 0) {

            for (i in 0..insectDatas.size - 1) {
                insectPk += insectDatas.get(i).id + "\n"

                val grop_id = insectDatas.get(i).GROP_ID

                if (points.size > 0) {

                    for (j in 0..points.size - 1) {
                        if (points.get(j).tag != null) {
                            val layerInfo = points.get(j).tag as LayerInfo

                            var attrubuteKey = layerInfo.attrubuteKey

                            if (attrubuteKey.equals(grop_id)) {

                                val exporter = Exporter.ExportPointItem(LAYER_INSECT, INSECTATTRIBUTE, points.get(j))

                                pointsArray.add(exporter)

                                Exporter.exportPoint(pointsArray)

                            }

                        }
                    }

                }
            }

            val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect" + File.separator + "insect"

            val layerData= db.query("layers", dataList, "file_name = '$file_path'", null, null ,null, "", null)

            while (layerData.moveToNext()){
                chkData = true
            }

            if(chkData){

            }else {
                dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect" + File.separator + "insect","곤충", "insect","Y")
            }

            pointsArray.clear()
            insectDatas.clear()
        }
    }

    fun exportFlora(){
        var pointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*")
        val floradata = db.query("floraAttribute", dataList, null, null, "GROP_ID", null, "", null)
        var datas:ArrayList<Flora_Attribute> = ArrayList<Flora_Attribute>()
        var chkData = false

        while (floradata.moveToNext()) {

            var flora_Attribute: Flora_Attribute = Flora_Attribute(floradata.getString(0), floradata.getString(1), floradata.getString(2), floradata.getString(3), floradata.getString(4)
                    , floradata.getString(5), floradata.getString(6), floradata.getString(7), floradata.getString(8), floradata.getFloat(9), floradata.getString(10), floradata.getInt(11)
                    , floradata.getString(12), floradata.getString(13), floradata.getString(14), floradata.getString(15), floradata.getString(16), floradata.getString(17), floradata.getString(18)
                    , floradata.getString(19), floradata.getInt(20), floradata.getString(21), floradata.getFloat(22), floradata.getFloat(23), floradata.getString(24),floradata.getString(25))

            datas.add(flora_Attribute)
        }

        if(datas != null){
            for(i in 0..datas.size-1){
                val data = datas.get(i)
                val floradata = db.query("floraAttribute", dataList, "GROP_ID = '${data.GROP_ID}'", null, null, null, "", null)

                while (floradata.moveToNext()) {

                    var flora_Attribute: Flora_Attribute = Flora_Attribute(floradata.getString(0), floradata.getString(1), floradata.getString(2), floradata.getString(3), floradata.getString(4)
                            , floradata.getString(5), floradata.getString(6), floradata.getString(7), floradata.getString(8), floradata.getFloat(9), floradata.getString(10), floradata.getInt(11)
                            , floradata.getString(12), floradata.getString(13), floradata.getString(14), floradata.getString(15), floradata.getString(16), floradata.getString(17), floradata.getString(18)
                            , floradata.getString(19), floradata.getInt(20), floradata.getString(21), floradata.getFloat(22), floradata.getFloat(23), floradata.getString(24),floradata.getString(25))

                    FLORAATTRIBUTE.add(Exporter.ColumnDef("ID",ogr.OFTString,flora_Attribute.id))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("GROP_ID",ogr.OFTString,flora_Attribute.GROP_ID))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME",ogr.OFTString,flora_Attribute.PRJ_NAME))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("INV_REGION",ogr.OFTString,flora_Attribute.INV_REGION))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("INV_DT",ogr.OFTString,flora_Attribute.INV_DT))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON",ogr.OFTString,flora_Attribute.INV_PERSON))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("WEATHER",ogr.OFTString,flora_Attribute.WEATHER))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("WIND",ogr.OFTString,flora_Attribute.WIND))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE",ogr.OFTString,flora_Attribute.WIND_DIRE))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR",ogr.OFTReal,flora_Attribute.TEMPERATUR))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("ETC",ogr.OFTString,flora_Attribute.ETC))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("NUM",ogr.OFTInteger,flora_Attribute.NUM))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("INV_TM",ogr.OFTString,flora_Attribute.INV_TM))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM",ogr.OFTString,flora_Attribute.SPEC_NM))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM",ogr.OFTString,flora_Attribute.FAMI_NM))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM",ogr.OFTString,flora_Attribute.SCIEN_NM))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("FLORE_YN",ogr.OFTString,flora_Attribute.FLORE_YN))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("PLANT_YN",ogr.OFTString,flora_Attribute.PLANT_YN))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("HAB_STAT",ogr.OFTString,flora_Attribute.HAB_STAT))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("HAB_ETC",ogr.OFTString,flora_Attribute.HAB_ETC))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("COL_IN_CNT",ogr.OFTInteger,flora_Attribute.COL_IN_CNT))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("THRE_CAU",ogr.OFTString,flora_Attribute.THRE_CAU))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT",ogr.OFTReal,flora_Attribute.GPS_LAT))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("GPS_LON",ogr.OFTReal,flora_Attribute.GPS_LON))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN",ogr.OFTString,flora_Attribute.TEMP_YN))
                    FLORAATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD",ogr.OFTString,flora_Attribute.CONF_MOD))

                    floraDatas.add(flora_Attribute)

                }
            }
        }

        if(floraDatas.size > 0) {

            for (i in 0..floraDatas.size - 1) {

                val grop_id = floraDatas.get(i).GROP_ID

                if (points.size > 0) {

                    for (j in 0..points.size - 1) {

                        if(points.get(j).tag != null) {
                            val layerInfo = points.get(j).tag as LayerInfo

                            var attrubuteKey = layerInfo.attrubuteKey

                            if (attrubuteKey.equals(grop_id)) {

                                val exporter = Exporter.ExportPointItem(LAYER_FLORA, FLORAATTRIBUTE, points.get(j))

                                pointsArray.add(exporter)

                                Exporter.exportPoint(pointsArray)

                            }
                        }

                    }

                }
            }

            val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora" + File.separator + "flora"

            val layerData= db.query("layers", dataList, "file_name = '$file_path'", null, null ,null, "", null)

            while (layerData.moveToNext()){
                chkData = true
            }

            if(chkData){

            }else {
                dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora" + File.separator + "flora","식물", "flora","Y")
            }

            pointsArray.clear()
            floraDatas.clear()

        }
    }

    fun exportTracking(){
        var trackingPointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*")

        val trackingdata = db.query("tracking", dataList, null, null, null, null, "id", null)

        var trackingpk: String  = ""

        while (trackingdata.moveToNext()) {
            trackingChk = true

            var tracking : Tracking = Tracking(trackingdata.getInt(0),trackingdata.getDouble(1),trackingdata.getDouble(2))

            TRACKINGS.add(Exporter.ColumnDef("ID",ogr.OFTInteger,tracking.id))
            println("------------------------------------${ogr.OFTInteger}----------------------")
            TRACKINGS.add(Exporter.ColumnDef("LATITUDE",ogr.OFTReal,tracking.LATITUDE))
            TRACKINGS.add(Exporter.ColumnDef("LONGITUDE",ogr.OFTReal,tracking.LONGITUDE))


            println("tracking.id ${tracking.id}")
            println("tracking.LATITUDE ${tracking.LATITUDE}")
            println("tracking.LONGITUDE ${tracking.LONGITUDE}")
            trackingDatas.add(tracking)

            val latlng = LatLng(tracking.LATITUDE!!,tracking.LONGITUDE!!)

            drawPoint(latlng)

            trackingBtn.setText("Tracking 켜기")

            trackingpk += tracking.id.toString()

        }

        if(trackingDatas.size > 0){

            for (i in 0..trackpoints.size -1 ){
                println("trackpoint ${trackpoints.size}")

                val exporter = Exporter.ExportPointItem(TRACKING, TRACKINGS,trackpoints.get(i))

                println("============================================exporter" + exporter)

                trackingPointsArray.add(exporter)
            }

            trackingChk = false

            println("trackingPoints${trackingPointsArray.size}")

            val today = Utils.todayStr()
            val time = Utils.timeStr()

            val path = today + " " + time

            dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tracking" + File.separator + path,"경로 : " + path, "tracking","Y")

            Exporter.exportPoint(trackingPointsArray)
            trackingPointsArray.clear()
            trackingDatas.clear()

            for(k in 0..trackpoints.size-1){
                trackpoints.get(k).remove()
            }
        }
    }

    fun export() {

        println("====export")

        var biotopeArray: ArrayList<Exporter.ExportItem> = ArrayList<Exporter.ExportItem>()

        var pointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()

        var trackingPointsArray: ArrayList<Exporter.ExportPointItem> = ArrayList<Exporter.ExportPointItem>()

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        val dataList: Array<String> = arrayOf("*")

        var biotopedata= db.query("biotopeAttribute", dataList, null, null, "GROP_ID", null, "", null)

        while (biotopedata.moveToNext()) {
            var biotope_attribute: Biotope_attribute = Biotope_attribute(biotopedata.getString(0), biotopedata.getString(1), biotopedata.getString(2), biotopedata.getString(3)
                    , biotopedata.getString(4), biotopedata.getString(5), biotopedata.getString(6), biotopedata.getInt(7),
                    biotopedata.getString(8), biotopedata.getFloat(9), biotopedata.getFloat(10), biotopedata.getString(11), biotopedata.getString(12), biotopedata.getString(13), biotopedata.getFloat(14)
                    , biotopedata.getString(15), biotopedata.getString(16), biotopedata.getString(17), biotopedata.getString(18), biotopedata.getString(19), biotopedata.getString(20), biotopedata.getString(21)
                    , biotopedata.getString(22), biotopedata.getString(23), biotopedata.getString(24), biotopedata.getString(25), biotopedata.getFloat(26), biotopedata.getFloat(27), biotopedata.getFloat(28)
                    , biotopedata.getString(29), biotopedata.getString(30), biotopedata.getString(31), biotopedata.getFloat(32), biotopedata.getFloat(33), biotopedata.getFloat(34), biotopedata.getString(35)
                    , biotopedata.getString(36), biotopedata.getString(37), biotopedata.getFloat(38), biotopedata.getFloat(39), biotopedata.getString(40), biotopedata.getString(41), biotopedata.getString(42)
                    , biotopedata.getFloat(43), biotopedata.getFloat(44), biotopedata.getString(45), biotopedata.getString(46), biotopedata.getString(47), biotopedata.getString(48), biotopedata.getDouble(49)
                    , biotopedata.getDouble(50), biotopedata.getString(51), biotopedata.getString(52),biotopedata.getString(53))

            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,biotope_attribute.id))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,biotope_attribute.GROP_ID))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,biotope_attribute.PRJ_NAME))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTInteger,biotope_attribute.INV_REGION))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTReal,biotope_attribute.INV_PERSON))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,biotope_attribute.INV_DT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,biotope_attribute.INV_TM))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("INV_INDEX", ogr.OFTInteger,biotope_attribute.INV_INDEX))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("LU_GR_NUM", ogr.OFTString,biotope_attribute.LU_GR_NUM))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("LU_TY_RATE", ogr.OFTReal,biotope_attribute.LU_TY_RATE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STAND_H", ogr.OFTReal,biotope_attribute.STAND_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("LC_GR_NUM", ogr.OFTString,biotope_attribute.LC_GR_NUM))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("LC_TY", ogr.OFTString,biotope_attribute.LC_TY))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TY_MARK", ogr.OFTString,biotope_attribute.TY_MARK))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GV_RATE", ogr.OFTReal,biotope_attribute.GV_RATE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GV_STRUCT", ogr.OFTString,biotope_attribute.GV_STRUCT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("DIS_RET", ogr.OFTString,biotope_attribute.DIS_RET))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("RESTOR_POT", ogr.OFTString,biotope_attribute.RESTOR_POT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("COMP_INTA", ogr.OFTString,biotope_attribute.COMP_INTA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("VP_INTA", ogr.OFTString,biotope_attribute.VP_INTA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("IMP_FORM", ogr.OFTString,biotope_attribute.IMP_FORM))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("BREA_DIA", ogr.OFTString,biotope_attribute.BREA_DIA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("FIN_EST", ogr.OFTString,biotope_attribute.FIN_EST))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_SPEC", ogr.OFTString,biotope_attribute.TRE_SPEC))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_FAMI", ogr.OFTString,biotope_attribute.TRE_FAMI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_SCIEN", ogr.OFTString,biotope_attribute.TRE_SCIEN))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_H", ogr.OFTReal,biotope_attribute.TRE_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_BREA", ogr.OFTReal,biotope_attribute.TRE_BREA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TRE_COVE", ogr.OFTReal,biotope_attribute.TRE_COVE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_SPEC", ogr.OFTString,biotope_attribute.STRE_SPEC))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_FAMI", ogr.OFTString,biotope_attribute.STRE_FAMI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_SCIEN", ogr.OFTString,biotope_attribute.STRE_SCIEN))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_H", ogr.OFTReal,biotope_attribute.STRE_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_BREA", ogr.OFTReal,biotope_attribute.STRE_BREA))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STRE_COVE", ogr.OFTReal,biotope_attribute.STRE_COVE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("SHR_SPEC", ogr.OFTString,biotope_attribute.SHR_SPEC))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("SHR_FAMI", ogr.OFTString,biotope_attribute.SHR_FAMI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("SHR_SCIEN", ogr.OFTString,biotope_attribute.SHR_SCIEN))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("SHR_H", ogr.OFTReal,biotope_attribute.SHR_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("STR_COVE", ogr.OFTReal,biotope_attribute.STR_COVE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_SPEC", ogr.OFTString,biotope_attribute.HER_SPEC))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_FAMI", ogr.OFTString,biotope_attribute.HER_FAMI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_SCIEN", ogr.OFTString,biotope_attribute.HER_SCIEN))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_H", ogr.OFTReal,biotope_attribute.HER_H))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("HER_COVE", ogr.OFTReal,biotope_attribute.HER_COVE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("PIC_FOLDER", ogr.OFTString,biotope_attribute.PIC_FOLDER))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("WILD_ANI", ogr.OFTString,biotope_attribute.WILD_ANI))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("BIOTOP_POT", ogr.OFTString,biotope_attribute.BIOTOP_POT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("UNUS_NOTE", ogr.OFTString,biotope_attribute.UNUS_NOTE))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,biotope_attribute.GPS_LAT))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,biotope_attribute.GPS_LON))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("NEED_CONF", ogr.OFTString,biotope_attribute.NEED_CONF))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,biotope_attribute.CONF_MOD))
            BIOTOPEATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,biotope_attribute.TEMP_YN))

            biotopeDatas.add(biotope_attribute)

        }

        if(biotopeDatas.size > 0 ) {

            println("biotopeDatas.size ${biotopeDatas.size}")

            for (i in 0..biotopeDatas.size - 1) {

                biotopeGrop_id += biotopeDatas.get(i).GROP_ID

                val grop_id = biotopeDatas.get(i).GROP_ID

                if(allPolygons.size > 0) {

                    for(k in 0..allPolygons.size - 1){
                        println("layerinfo ---- ${allPolygons.get(k).tag}")
                    }

                    println(allPolygons.size.toString() + "----------------------------")

                    for (j in 0..allPolygons.size - 1) {

                        println("layerinfo ---- ${allPolygons.get(j).tag}")
                        val layerInfo = allPolygons.get(j).tag as LayerInfo

                        var attrubuteKey = layerInfo.attrubuteKey

                        println("attrubutekey $attrubuteKey")
                        if (attrubuteKey.equals(grop_id)) {

                            val exporter = Exporter.ExportItem(LAYER_BIOTOPE, BIOTOPEATTRIBUTE, allPolygons.get(j))

                            biotopeArray.add(exporter)

                            println("adddddddddddd")

                        }
                    }
                }

                Exporter.export(biotopeArray)
                dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "biotope" + File.separator + "biotope","비오톱", "biotope","Y")
                biotopeDatas.clear()

            }

        }

        val birdsdata= db.query("birdsAttribute", dataList, null, null, "GROP_ID", null, "", null)

        while (birdsdata.moveToNext()) {

            var birds_attribute: Birds_attribute = Birds_attribute(birdsdata.getString(0), birdsdata.getString(1), birdsdata.getString(2), birdsdata.getString(3), birdsdata.getString(4), birdsdata.getString(5), birdsdata.getString(6), birdsdata.getString(7),
                    birdsdata.getString(8), birdsdata.getFloat(9), birdsdata.getString(10), birdsdata.getInt(11), birdsdata.getString(12), birdsdata.getString(13), birdsdata.getString(14)
                    , birdsdata.getString(15),birdsdata.getString(16), birdsdata.getInt(17), birdsdata.getString(18), birdsdata.getString(19), birdsdata.getString(20), birdsdata.getString(21), birdsdata.getString(22)
                    , birdsdata.getString(23), birdsdata.getString(24), birdsdata.getFloat(25), birdsdata.getFloat(26), birdsdata.getString(27), birdsdata.getString(28))

            BIRDSATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,birds_attribute.id))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,birds_attribute.GROP_ID))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,birds_attribute.PRJ_NAME))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString,birds_attribute.INV_REGION))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,birds_attribute.INV_DT))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString,birds_attribute.INV_PERSON))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString,birds_attribute.WEATHER))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString,birds_attribute.WIND))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString,birds_attribute.WIND_DIRE))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal,birds_attribute.TEMPERATUR))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString,birds_attribute.ETC))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger,birds_attribute.NUM))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,birds_attribute.INV_TM))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString,birds_attribute.SPEC_NM))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString,birds_attribute.FAMI_NM))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString,birds_attribute.SCIEN_NM))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("ENDANGERED", ogr.OFTString,birds_attribute.ENDANGERED))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("INDI_CNT", ogr.OFTInteger,birds_attribute.INDI_CNT))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("OBS_STAT", ogr.OFTString,birds_attribute.OBS_STAT))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("OBS_ST_ETC", ogr.OFTString,birds_attribute.OBS_ST_ETC))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("USE_TAR", ogr.OFTString,birds_attribute.USE_TAR))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("USE_TAR_SP", ogr.OFTString,birds_attribute.USE_TAR_SP))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("USE_LAYER", ogr.OFTString,birds_attribute.USE_LAYER))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("MJ_ACT", ogr.OFTString,birds_attribute.MJ_ACT))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("MJ_ACT_PR", ogr.OFTString,birds_attribute.MJ_ACT_PR))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,birds_attribute.GPS_LAT))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,birds_attribute.GPS_LON))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,birds_attribute.TEMP_YN))
            BIRDSATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,birds_attribute.CONF_MOD))

            birdsDatas.add(birds_attribute)
        }

        if(birdsDatas.size > 0) {

            for (i in 0..birdsDatas.size - 1) {

                birdsPk += birdsDatas.get(i).id + "\n"

                val grop_id = birdsDatas.get(i).GROP_ID

                if(points.size > 0) {

                    for (j in 0..points.size - 1) {
                        val layerInfo = points.get(j).tag as LayerInfo

                        var attrubuteKey = layerInfo.attrubuteKey

                        if (attrubuteKey.equals(grop_id)) {

                            val exporter = Exporter.ExportPointItem(LAYER_BIRDS, BIRDSATTRIBUTE, points.get(j))

                            pointsArray.add(exporter)

                        }

                    }

                }
            }

            dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "birds" + File.separator + "birds" ,"조류", "birds","Y")
            Exporter.exportPoint(pointsArray)
            pointsArray.clear()
            birdsDatas.clear()
        }

        val reptiliadata= db.query("reptiliaAttribute", dataList, null, null, "GROP_ID", null, "", null)

        while (reptiliadata.moveToNext()) {

            var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(reptiliadata.getString(0), reptiliadata.getString(1), reptiliadata.getString(2), reptiliadata.getString(3), reptiliadata.getString(4), reptiliadata.getString(5), reptiliadata.getString(6), reptiliadata.getString(7),
                    reptiliadata.getString(8), reptiliadata.getFloat(9), reptiliadata.getString(10), reptiliadata.getInt(11), reptiliadata.getString(12), reptiliadata.getString(13), reptiliadata.getString(14)
                    , reptiliadata.getString(15), reptiliadata.getString(16),reptiliadata.getInt(17), reptiliadata.getInt(18), reptiliadata.getInt(19), reptiliadata.getString(20), reptiliadata.getString(21), reptiliadata.getString(22)
                    , reptiliadata.getString(23), reptiliadata.getString(24), reptiliadata.getString(25), reptiliadata.getInt(26), reptiliadata.getInt(27), reptiliadata.getInt(28), reptiliadata.getFloat(29), reptiliadata.getFloat(30),reptiliadata.getString(31),reptiliadata.getString(32))

                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,reptilia_attribute.id))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,reptilia_attribute.GROP_ID))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,reptilia_attribute.PRJ_NAME))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString,reptilia_attribute.INV_REGION))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,reptilia_attribute.INV_DT))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString,reptilia_attribute.INV_PERSON))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString,reptilia_attribute.WEATHER))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString,reptilia_attribute.WIND))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString,reptilia_attribute.WIND_DIRE))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal,reptilia_attribute.TEMPERATUR))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString,reptilia_attribute.ETC))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger,reptilia_attribute.NUM))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,reptilia_attribute.INV_TM))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString,reptilia_attribute.SPEC_NM))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString,reptilia_attribute.FAMI_NM))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString,reptilia_attribute.SCIEN_NM))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("IN_CNT_ADU", ogr.OFTInteger,reptilia_attribute.IN_CNT_ADU))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("IN_CNT_LAR", ogr.OFTInteger,reptilia_attribute.IN_CNT_LAR))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("IN_CNT_EGG", ogr.OFTInteger,reptilia_attribute.IN_CNT_EGG))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("HAB_RIVEER", ogr.OFTString,reptilia_attribute.HAB_RIVEER))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("HAB_EDGE", ogr.OFTString,reptilia_attribute.HAB_EDGE))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_IN", ogr.OFTString,reptilia_attribute.WATER_IN))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_OUT", ogr.OFTString,reptilia_attribute.WATER_OUT))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_CONT", ogr.OFTString,reptilia_attribute.WATER_CONT))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_QUAL", ogr.OFTString,reptilia_attribute.WATER_QUAL))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("WATER_DEPT", ogr.OFTInteger,reptilia_attribute.WATER_DEPT))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("HAB_AREA_W", ogr.OFTInteger,reptilia_attribute.HAB_AREA_W))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("HAB_AREA_H", ogr.OFTInteger,reptilia_attribute.HAB_AREA_H))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,reptilia_attribute.GPS_LAT))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,reptilia_attribute.GPS_LON))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,reptilia_attribute.TEMP_YN))
                REPTILIAATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,reptilia_attribute.CONF_MOD))

            reptiliaDatas.add(reptilia_attribute)

        }

        if(reptiliaDatas.size > 0) {

            for (i in 0..reptiliaDatas.size - 1) {
                reptiliaPk += reptiliaDatas.get(i).id + "\n"

                val grop_id = reptiliaDatas.get(i).GROP_ID

                if(points.size > 0) {

                    for (j in 0..points.size - 1) {
                        val layerInfo = points.get(j).tag as LayerInfo

                        var attrubuteKey = layerInfo.attrubuteKey

                        if (attrubuteKey.equals(grop_id)) {

                            val exporter = Exporter.ExportPointItem(LAYER_REPTILIA, REPTILIAATTRIBUTE, points.get(j))

                            pointsArray.add(exporter)

                        }

                    }

                }
            }

            dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "reptilia" + File.separator + "reptilia","양서,파충류", "reptilia","Y")
            Exporter.exportPoint(pointsArray)
            pointsArray.clear()
            reptiliaDatas.clear()
        }

        val mammaldata = db.query("mammalAttribute", dataList, null, null, "GROP_ID", null, "", null)

        while (mammaldata.moveToNext()) {

            var mammal_attribute: Mammal_attribute = Mammal_attribute(mammaldata.getString(0), mammaldata.getString(1), mammaldata.getString(2), mammaldata.getString(3), mammaldata.getString(4), mammaldata.getString(5), mammaldata.getString(6), mammaldata.getString(7),
                    mammaldata.getString(8), mammaldata.getFloat(9), mammaldata.getString(10), mammaldata.getInt(11), mammaldata.getString(12), mammaldata.getString(13), mammaldata.getString(14)
                    , mammaldata.getString(15), mammaldata.getString(16), mammaldata.getString(17), mammaldata.getString(18),mammaldata.getInt(19), mammaldata.getString(20), mammaldata.getString(21), mammaldata.getFloat(22)
                    , mammaldata.getFloat(23), mammaldata.getString(24), mammaldata.getString(25), mammaldata.getString(26), mammaldata.getString(27),mammaldata.getString(28),mammaldata.getString(29))

            MAMMALATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,mammal_attribute.id))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,mammal_attribute.GROP_ID))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,mammal_attribute.PRJ_NAME))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString,mammal_attribute.INV_REGION))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,mammal_attribute.INV_DT))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString,mammal_attribute.INV_PERSON))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString,mammal_attribute.WEATHER))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString,mammal_attribute.WIND))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString,mammal_attribute.WIND_DIRE))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal,mammal_attribute.TEMPERATUR))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString,mammal_attribute.ETC))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger,mammal_attribute.NUM))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,mammal_attribute.INV_TM))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString,mammal_attribute.SPEC_NM))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString,mammal_attribute.FAMI_NM))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString,mammal_attribute.SCIEN_NM))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("ENDANGERED", ogr.OFTString,mammal_attribute.ENDANGERED))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("OBS_TY", ogr.OFTString,mammal_attribute.OBS_TY))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("OBS_TY_ETC", ogr.OFTString,mammal_attribute.OBS_TY_ETC))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("INDI_CNT", ogr.OFTInteger,mammal_attribute.INDI_CNT))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("OB_PT_CHAR", ogr.OFTString,mammal_attribute.OB_PT_CHAR))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("UNUS_NOTE", ogr.OFTString,mammal_attribute.UNUS_NOTE))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,mammal_attribute.GPS_LAT))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,mammal_attribute.GPS_LON))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("UN_SPEC", ogr.OFTString,mammal_attribute.UN_SPEC))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("UN_SPEC_RE", ogr.OFTString,mammal_attribute.UN_SPEC_RE))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("TR_EASY", ogr.OFTString,mammal_attribute.TR_EASY))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("TR_EASY_RE", ogr.OFTString,mammal_attribute.TR_EASY_RE))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,mammal_attribute.TEMP_YN))
            MAMMALATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,mammal_attribute.CONF_MOD))

            mammaliaDatas.add(mammal_attribute)

        }

        if(mammaliaDatas.size > 0) {

            for (i in 0..mammaliaDatas.size - 1) {
                mammaliaPk += mammaliaDatas.get(i).id + "\n"

                val grop_id = mammaliaDatas.get(i).GROP_ID

                if(points.size > 0) {

                    for (j in 0..points.size - 1) {
                        val layerInfo = points.get(j).tag as LayerInfo

                        var attrubuteKey = layerInfo.attrubuteKey

                        if (attrubuteKey.equals(grop_id)) {

                            val exporter = Exporter.ExportPointItem(LAYER_MAMMALIA, MAMMALATTRIBUTE, points.get(j))

                            pointsArray.add(exporter)

                        }

                    }

                }
            }

            dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia" + File.separator + "mammalia","포유류", "mammalia","Y")
            Exporter.exportPoint(pointsArray)
            pointsArray.clear()
            mammaliaDatas.clear()
        }

        val fishdata = db.query("fishAttribute", dataList, null, null, "GROP_ID", null, "", null)

        while (fishdata.moveToNext()) {

            var fish_attribute: Fish_attribute = Fish_attribute(fishdata.getString(0), fishdata.getString(1), fishdata.getString(2), fishdata.getString(3), fishdata.getString(4), fishdata.getString(5), fishdata.getString(6), fishdata.getString(7),
                    fishdata.getString(8),fishdata.getString(9), fishdata.getFloat(10), fishdata.getString(11), fishdata.getString(12), fishdata.getString(13), fishdata.getInt(14), fishdata.getString(15), fishdata.getInt(16), fishdata.getInt(17), fishdata.getString(18),
                    fishdata.getFloat(19), fishdata.getFloat(20), fishdata.getString(21), fishdata.getInt(22), fishdata.getInt(23), fishdata.getInt(24), fishdata.getInt(25), fishdata.getString(26), fishdata.getString(27), fishdata.getString(28),
                    fishdata.getInt(29) ,fishdata.getString(30), fishdata.getString(31), fishdata.getString(32), fishdata.getInt(33), fishdata.getString(33), fishdata.getString(35), fishdata.getString(36),fishdata.getString(37),fishdata.getString(38))

            FISHATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,fish_attribute.id))
            FISHATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,fish_attribute.GROP_ID))
            FISHATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,fish_attribute.PRJ_NAME))
            FISHATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString,fish_attribute.INV_REGION))
            FISHATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,fish_attribute.INV_DT))
            FISHATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,fish_attribute.INV_TM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString,fish_attribute.INV_PERSON))
            FISHATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString,fish_attribute.WEATHER))
            FISHATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString,fish_attribute.WIND))
            FISHATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString,fish_attribute.WIND_DIRE))
            FISHATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal,fish_attribute.TEMPERATUR))
            FISHATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString,fish_attribute.ETC))
            FISHATTRIBUTE.add(Exporter.ColumnDef("MID_RAGE", ogr.OFTString,fish_attribute.MID_RAGE))
            FISHATTRIBUTE.add(Exporter.ColumnDef("CODE_NUM", ogr.OFTString,fish_attribute.CODE_NUM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("RIVER_NUM", ogr.OFTInteger,fish_attribute.RIVER_NUM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("RIVER_NM", ogr.OFTString,fish_attribute.RIVER_NM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("NET_CNT", ogr.OFTInteger,fish_attribute.NET_CNT))
            FISHATTRIBUTE.add(Exporter.ColumnDef("NET_MIN", ogr.OFTInteger,fish_attribute.NET_MIN))
            FISHATTRIBUTE.add(Exporter.ColumnDef("AD_DIST_NM", ogr.OFTString,fish_attribute.AD_DIST_NM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,fish_attribute.GPS_LAT))
            FISHATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,fish_attribute.GPS_LON))
            FISHATTRIBUTE.add(Exporter.ColumnDef("COLL_TOOL", ogr.OFTString,fish_attribute.COLL_TOOL))
            FISHATTRIBUTE.add(Exporter.ColumnDef("STREAM_W", ogr.OFTString,fish_attribute.STREAM_W))
            FISHATTRIBUTE.add(Exporter.ColumnDef("WATER_W", ogr.OFTInteger,fish_attribute.WATER_W))
            FISHATTRIBUTE.add(Exporter.ColumnDef("WATER_D", ogr.OFTInteger,fish_attribute.WATER_D))
            FISHATTRIBUTE.add(Exporter.ColumnDef("WATER_CUR", ogr.OFTInteger,fish_attribute.WATER_CUR))
            FISHATTRIBUTE.add(Exporter.ColumnDef("RIV_STR", ogr.OFTString,fish_attribute.RIV_STR))
            FISHATTRIBUTE.add(Exporter.ColumnDef("RIV_STR_IN", ogr.OFTString,fish_attribute.RIV_STR_IN))
            FISHATTRIBUTE.add(Exporter.ColumnDef("RIV_FORM", ogr.OFTString,fish_attribute.RIV_FORM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger,fish_attribute.NUM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString,fish_attribute.SPEC_NM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString,fish_attribute.FAMI_NM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString,fish_attribute.SCIEN_NM))
            FISHATTRIBUTE.add(Exporter.ColumnDef("INDI_CNT", ogr.OFTInteger,fish_attribute.INDI_CNT))
            FISHATTRIBUTE.add(Exporter.ColumnDef("UNIDENT", ogr.OFTString,fish_attribute.UNIDENT))
            FISHATTRIBUTE.add(Exporter.ColumnDef("RIV_FM_CH", ogr.OFTString,fish_attribute.RIV_FM_CH))
            FISHATTRIBUTE.add(Exporter.ColumnDef("UN_FISH_CH", ogr.OFTString,fish_attribute.UN_FISH_CH))
            FISHATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,fish_attribute.TEMP_YN))
            FISHATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,fish_attribute.CONF_MOD))

            fishDatas.add(fish_attribute)

        }

        if(fishDatas.size > 0 ) {

            for (i in 0..fishDatas.size - 1) {
                fishPk += fishDatas.get(i).id + "\n"

                val grop_id = fishDatas.get(i).GROP_ID

                if (points.size > 0) {

                    for (j in 0..points.size - 1) {
                        val layerInfo = points.get(j).tag as LayerInfo

                        var attrubuteKey = layerInfo.attrubuteKey
                        if (attrubuteKey.equals(grop_id)) {

                            val exporter = Exporter.ExportPointItem(LAYER_FISH, FISHATTRIBUTE, points.get(j))

                            pointsArray.add(exporter)

                        }

                    }

                }
            }

            dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish" + File.separator + "fish","어류", "fish","Y")
            Exporter.exportPoint(pointsArray)
            pointsArray.clear()
            fishDatas.clear()
        }

        val insectdata = db.query("insectAttribute", dataList, null, null, "GROP_ID", null, "", null)

        while (insectdata.moveToNext()) {

            var insect_attribute: Insect_attribute = Insect_attribute(insectdata.getString(0), insectdata.getString(1), insectdata.getString(2), insectdata.getString(3), insectdata.getString(4)
                    , insectdata.getString(5), insectdata.getString(6), insectdata.getString(7), insectdata.getString(8), insectdata.getFloat(9), insectdata.getString(10), insectdata.getInt(11)
                    , insectdata.getString(12), insectdata.getString(13), insectdata.getString(14), insectdata.getString(15), insectdata.getInt(16), insectdata.getString(17), insectdata.getString(18)
                    , insectdata.getString(19), insectdata.getString(20), insectdata.getString(21), insectdata.getString(22), insectdata.getString(23), insectdata.getString(24), insectdata.getString(25)
                    , insectdata.getFloat(26), insectdata.getFloat(27), insectdata.getString(28), insectdata.getString(29))

            INSECTATTRIBUTE.add(Exporter.ColumnDef("ID", ogr.OFTString,insect_attribute.id))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("GROP_ID", ogr.OFTString,insect_attribute.GROP_ID))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME", ogr.OFTString,insect_attribute.PRJ_NAME))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_REGION", ogr.OFTString,insect_attribute.INV_REGION))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_DT", ogr.OFTString,insect_attribute.INV_DT))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON", ogr.OFTString,insect_attribute.INV_PERSON))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("WEATHER", ogr.OFTString,insect_attribute.WEATHER))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("WIND", ogr.OFTString,insect_attribute.WIND))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE", ogr.OFTString,insect_attribute.WIND_DIRE))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR", ogr.OFTReal,insect_attribute.TEMPERATUR))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("ETC", ogr.OFTString,insect_attribute.ETC))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("NUM", ogr.OFTInteger,insect_attribute.NUM))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_TM", ogr.OFTString,insect_attribute.INV_TM))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM", ogr.OFTString,insect_attribute.SPEC_NM))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM", ogr.OFTString,insect_attribute.FAMI_NM))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM", ogr.OFTString,insect_attribute.SCIEN_NM))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("INDI_CNT", ogr.OFTInteger,insect_attribute.INDI_CNT))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("OBS_STAT", ogr.OFTString,insect_attribute.OBS_STAT))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("OBS_ST_ETC", ogr.OFTString,insect_attribute.OBS_ST_ETC))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("USE_TAR", ogr.OFTString,insect_attribute.USE_TAR))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("USER_TA_ETC", ogr.OFTString,insect_attribute.USER_TA_ETC))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("MJ_ACT", ogr.OFTString,insect_attribute.MJ_ACT))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("MJ_ACT_ETC", ogr.OFTString,insect_attribute.MJ_ACT_ETC))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_MEAN", ogr.OFTString,insect_attribute.INV_MEAN))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("INV_MN_ETC", ogr.OFTString,insect_attribute.INV_MN_ETC))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("UNUS_NOTE", ogr.OFTString,insect_attribute.UNUS_NOTE))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT", ogr.OFTReal,insect_attribute.GPS_LAT))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("GPS_LON", ogr.OFTReal,insect_attribute.GPS_LON))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN", ogr.OFTString,insect_attribute.TEMP_YN))
            INSECTATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD", ogr.OFTString,insect_attribute.CONF_MOD))

            insectDatas.add(insect_attribute)

        }

        if(insectDatas.size > 0) {

            for (i in 0..insectDatas.size - 1) {
                insectPk += insectDatas.get(i).id + "\n"

                val grop_id = insectDatas.get(i).GROP_ID

                if (points.size > 0) {

                    for (j in 0..points.size - 1) {
                        val layerInfo = points.get(j).tag as LayerInfo

                        var attrubuteKey = layerInfo.attrubuteKey

                        if (attrubuteKey.equals(grop_id)) {

                            val exporter = Exporter.ExportPointItem(LAYER_INSECT, INSECTATTRIBUTE, points.get(j))

                            pointsArray.add(exporter)

                        }

                    }

                }
            }

            dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect" + File.separator + "insect","곤충", "insect","Y")
            Exporter.exportPoint(pointsArray)
            pointsArray.clear()
            insectDatas.clear()
        }

        val floradata = db.query("floraAttribute", dataList, null, null, "GROP_ID", null, "", null)

        while (floradata.moveToNext()) {

            var flora_Attribute: Flora_Attribute = Flora_Attribute(floradata.getString(0), floradata.getString(1), floradata.getString(2), floradata.getString(3), floradata.getString(4)
                    , floradata.getString(5), floradata.getString(6), floradata.getString(7), floradata.getString(8), floradata.getFloat(9), floradata.getString(10), floradata.getInt(11)
                    , floradata.getString(12), floradata.getString(13), floradata.getString(14), floradata.getString(15), floradata.getString(16), floradata.getString(17), floradata.getString(18)
                    , floradata.getString(19), floradata.getInt(20), floradata.getString(21), floradata.getFloat(22), floradata.getFloat(23), floradata.getString(24),floradata.getString(25))

            FLORAATTRIBUTE.add(Exporter.ColumnDef("ID",ogr.OFTInteger,flora_Attribute.id))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("GROP_ID",ogr.OFTString,flora_Attribute.GROP_ID))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("PRJ_NAME",ogr.OFTString,flora_Attribute.PRJ_NAME))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("INV_REGION",ogr.OFTString,flora_Attribute.INV_REGION))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("INV_DT",ogr.OFTString,flora_Attribute.INV_DT))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("INV_PERSON",ogr.OFTString,flora_Attribute.INV_PERSON))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("WEATHER",ogr.OFTString,flora_Attribute.WEATHER))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("WIND",ogr.OFTString,flora_Attribute.WIND))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("WIND_DIRE",ogr.OFTString,flora_Attribute.WIND_DIRE))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("TEMPERATUR",ogr.OFTString,flora_Attribute.TEMPERATUR))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("ETC",ogr.OFTString,flora_Attribute.ETC))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("NUM",ogr.OFTInteger,flora_Attribute.NUM))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("INV_TM",ogr.OFTString,flora_Attribute.INV_TM))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("SPEC_NM",ogr.OFTString,flora_Attribute.SPEC_NM))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("FAMI_NM",ogr.OFTString,flora_Attribute.FAMI_NM))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("SCIEN_NM",ogr.OFTString,flora_Attribute.SCIEN_NM))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("FLORE_YN",ogr.OFTString,flora_Attribute.FLORE_YN))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("PLANT_YN",ogr.OFTString,flora_Attribute.PLANT_YN))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("HAB_STAT",ogr.OFTString,flora_Attribute.HAB_STAT))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("HAB_ETC",ogr.OFTString,flora_Attribute.HAB_ETC))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("COL_IN_CNT",ogr.OFTInteger,flora_Attribute.COL_IN_CNT))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("THRE_CAU",ogr.OFTString,flora_Attribute.THRE_CAU))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("GPS_LAT",ogr.OFTReal,flora_Attribute.GPS_LAT))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("GPS_LON",ogr.OFTReal,flora_Attribute.GPS_LON))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("TEMP_YN",ogr.OFTInteger,flora_Attribute.TEMP_YN))
            FLORAATTRIBUTE.add(Exporter.ColumnDef("CONF_MOD",ogr.OFTInteger,flora_Attribute.CONF_MOD))

            floraDatas.add(flora_Attribute)

        }

        if(floraDatas.size > 0) {

            for (i in 0..floraDatas.size - 1) {
                floraPk += floraDatas.get(i).id + "\n"

                val grop_id = floraDatas.get(i).GROP_ID

                if (points.size > 0) {

                    for (j in 0..points.size - 1) {
                        val layerInfo = points.get(j).tag as LayerInfo

                        var attrubuteKey = layerInfo.attrubuteKey

                        if (attrubuteKey.equals(grop_id)) {

                            val exporter = Exporter.ExportPointItem(LAYER_FLORA, FLORAATTRIBUTE, points.get(j))

                            pointsArray.add(exporter)

                        }

                    }

                }
            }

            dbManager.insertlayers(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora" + File.separator + "flora","조류", "flora","Y")
            Exporter.exportPoint(pointsArray)
            pointsArray.clear()
            floraDatas.clear()
        }

        val trackingdata = db.query("tracking", dataList, null, null, null, null, "id", null)

        var trackingpk: String  = ""

        while (trackingdata.moveToNext()) {
            trackingChk = true

            var tracking : Tracking = Tracking(trackingdata.getInt(0),trackingdata.getDouble(1),trackingdata.getDouble(2))

            TRACKINGS.add(Exporter.ColumnDef("ID",ogr.OFTInteger,tracking.id))
            println("------------------------------------${ogr.OFTInteger}----------------------")
            TRACKINGS.add(Exporter.ColumnDef("LATITUDE",ogr.OFTReal,tracking.LATITUDE))
            TRACKINGS.add(Exporter.ColumnDef("LONGITUDE",ogr.OFTReal,tracking.LONGITUDE))


            println("tracking.id ${tracking.id}")
            println("tracking.LATITUDE ${tracking.LATITUDE}")
            println("tracking.LONGITUDE ${tracking.LONGITUDE}")
            trackingDatas.add(tracking)

            val latlng = LatLng(tracking.LATITUDE!!,tracking.LONGITUDE!!)

            drawPoint(latlng)

            trackingBtn.setText("Tracking 켜기")

            trackingpk += tracking.id.toString()

        }

        if(trackingDatas.size > 0){

            for (i in 0..trackpoints.size -1 ){
                println("trackpoint ${trackpoints.size}")

                    val exporter = Exporter.ExportPointItem(TRACKING, TRACKINGS,trackpoints.get(i))

                println("============================================exporter" + exporter)

                    trackingPointsArray.add(exporter)
            }

            trackingChk = false

            println("trackingPoints${trackingPointsArray.size}")

            Exporter.exportPoint(trackingPointsArray)
            trackingPointsArray.clear()
            trackingDatas.clear()

            for(k in 0..trackpoints.size-1){
                trackpoints.get(k).remove()
            }

        }

        println("biotopePk : $biotopeGrop_id  ,  birdsPk : $birdsPk  , reptiliaPk : $reptiliaPk   ,  mammalPk : $mammaliaPk  ,  fishPk : $fishPk   ,   insectPk : $insectPk   , floraPk $floraPk , trackingpk $trackingpk")

    }

    fun export2() {

        /*
         * Get an output file name and create the new shapefile
         */
        val newFile = getNewShapeFile()

        val dataStoreFactory = ShapefileDataStoreFactory()

        val params = HashMap<String, Serializable>()
        params.put("url", newFile.toURI().toURL())
        params.put("create spatial index", true)

        val newDataStore = dataStoreFactory.createNewDataStore(params) as ShapefileDataStore

        val simpleFeatureType = DataUtilities.createType("test", "geom:Polygon:srid=4326")
        newDataStore.createSchema(simpleFeatureType)

        /*
         * You can comment out this line if you are using the createFeatureType method (at end of
         * class file) rather than DataUtilities.createType
         */
        newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84)


        /*
         * Write the features to the shapefile
         */
        val transaction = DefaultTransaction("create")

        val typeName = newDataStore.typeNames[0]
        val featureSource = newDataStore.getFeatureSource(typeName)
        val SHAPE_TYPE = featureSource.schema
        /*
         * The Shapefile format has a couple limitations:
         * - "the_geom" is always first, and used for the geometry attribute name
         * - "the_geom" must be of type Point, MultiPoint, MuiltiLineString, MultiPolygon
         * - Attribute names are limited in length
         * - Not all data types are supported (example Timestamp represented as Date)
         *
         * Each data store has different limitations so check the resulting SimpleFeatureType.
         */
        System.out.println("SHAPE:" + SHAPE_TYPE)

        if (featureSource is SimpleFeatureStore) {
            val featureStore = featureSource as SimpleFeatureStore

            /*
             * SimpleFeatureStore has a method to add features from a
             * SimpleFeatureCollection object, so we use the ListFeatureCollection
             * class to wrap our list of features.
             */

            val features = ArrayList<SimpleFeature>()

            val collection = ListFeatureCollection(SHAPE_TYPE, features)
            featureStore.transaction = transaction as Transaction?

            featureStore.addFeatures(collection)
            transaction.commit()


            try {
                featureStore.addFeatures(collection)
                transaction.commit()
            } finally {
                transaction.close()
            }

            System.exit(0) // success!
        } else {
            System.out.println(typeName + " does not support read/write access")
            System.exit(1)
        }

    }

    private fun getNewShapeFile():File {
        val shpFileName = "${System.currentTimeMillis()}.shp"
        val shpFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), shpFileName)

        return shpFile
    }

    private fun unionPolygons() {

        if(polygonsToUnion.size != 2) {
            Utils.alert(context, "2 곳을 선택하고 합치기를 하세요.")
            return
        }

        val geometries = Array<Geometry?>(polygonsToUnion.size) { null }

        for (idx in 0..(polygonsToUnion.size - 1)) {
            val polygon = polygonsToUnion.get(idx)
            geometries[idx] = toJTSPolygon(polygon)
        }

        val geometry0 = geometries[0]
        val geometry1 = geometries[1]

        if(geometry0 == null || geometry1 == null) {
            Utils.alert(context, "선택한 곳이 이상합니다. 지우고 다시 해 주세요.")

            offUnionBtn()

            return

        }

        if(!geometry0!!.intersects(geometry1)) {
            Utils.alert(context, "선택한 2 곳은 겹치지 않아 합칠 수 없습니다.")

            offUnionBtn()

            return
        }

        val gc = GeometryFactory().createGeometryCollection(geometries);

        try {

            val unioned = gc.union()

            println("unioned.geometryType : ${unioned.geometryType}")

            if("Polygon" == unioned.geometryType) {

                val firstLayerInfo = polygonsToUnion.get(0).tag as LayerInfo

                println("firstLayerInfo : ${firstLayerInfo.attrubuteKey}")

                // delete row
                for (idx in 1..(polygonsToUnion.size - 1)) {
                    val polygon = polygonsToUnion.get(idx)
                    val layerInfo = polygon.tag as LayerInfo
                    deleteRow("biotopeAttribute", layerInfo.attrubuteKey)
                }

                for (idx in 0..(polygonsToUnion.size - 1)) {
                    val polygon = polygonsToUnion.get(idx)
                    polygon.remove()
                }

                val polygonOptions = PolygonOptions()
                polygonOptions.fillColor(getColor())
                polygonOptions.strokeWidth(1.0f)
                polygonOptions.strokeColor(Color.BLACK)

                for(coordinate in unioned.coordinates) {
                    polygonOptions.add(LatLng(coordinate.y, coordinate.x))
                }

                editingPolygon = googleMap.addPolygon(polygonOptions)
                // editingPolygon?.zIndex = 5.0f
                editingPolygon?.tag = firstLayerInfo
                editingPolygon?.isClickable = true

                polygonsToUnion.clear()

                offUnionBtn()
            }

        } catch (e:Exception) {
            e.printStackTrace()
        }

    }

    private fun getColor(): Int {
        val colors = arrayListOf<String>("#fffec1", "#f9d58b", "#fce9c4", "#efdffd", "#eafbf7", "#ea39c1", "#2a73f6")

        val idx = Utils.rand(0, colors.size - 1)

        return Color.parseColor(colors[idx])
    }

    private fun toJTSPolygon(polygon:Polygon):org.locationtech.jts.geom.Polygon {
        val points = polygon.points

        val coordinates = Array<Coordinate>(points.size) { Coordinate() }

        for(idx2 in 0..(points.size - 1)) {
            val point = points.get(idx2)
            val coordinate = Coordinate(point.longitude, point.latitude)
            coordinates[idx2] = coordinate
        }

        val geometryFactory = GeometryFactory()
        return geometryFactory.createPolygon(coordinates)
    }

    private fun toJTSLineString(polyline:Polyline):org.locationtech.jts.geom.LineString {
        val points = polyline.points

        val coordinates = Array<Coordinate>(points.size) { Coordinate() }

        for(idx2 in 0..(points.size - 1)) {
            val point = points.get(idx2)

            val coordinate = Coordinate(point.longitude, point.latitude)
            coordinates[idx2] = coordinate
        }

        val geometryFactory = GeometryFactory()
        return geometryFactory.createLineString(coordinates)
    }

    private fun copyRow(tableName:String, keyId:String, newKeyId:String) {

        println("keyId : $keyId, newKeyId : $newKeyId")

        val dbCursor = db!!.query(tableName, null, null, null, null, null, null)
        val columnNames = dbCursor.columnNames
        val columnNamesStr = columnNames.joinToString(separator = ",")

        val dataList: Array<String> = arrayOf("*");

        val data = db!!.query(tableName, dataList, "id = '$keyId'", null, null, null, "", null);

        val values = ArrayList<Any>()
        while (data.moveToNext()) {

            for(idx in 0..(data.columnCount - 1)) {
                var value = data.getString(idx)
                val columnName = data.getColumnName(idx)

                // println("value : $value")

                if("id" == columnName) {
                    value = newKeyId
                }

                values.add("\"$value\"")
            }

            break
        }

        if(values.size == 0) {
            return
        }

        val qry = "INSERT INTO $tableName ($columnNamesStr) values(${values.joinToString(separator = ",")})"

        println(qry)

        db!!.execSQL(qry)

    }

    private fun deleteRow(tableName:String, attrubuteKey: String) {

        println("$attrubuteKey deleted..")

        db!!.delete(tableName, "id = '$attrubuteKey'", null)
    }

    protected fun initGps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            loadPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION)
        } else {
            checkGPs()
        }
    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (android.Manifest.permission.ACCESS_FINE_LOCATION == perm) {
                loadPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION)
            } else if (android.Manifest.permission.ACCESS_COARSE_LOCATION == perm) {
                checkGPs()
            } else if(android.Manifest.permission.WRITE_EXTERNAL_STORAGE == perm){
                loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE )
            } else if(android.Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
                if (layerDivision == 0 ){
                    exportBiotope()
                }

                if (layerDivision == 1 ){
                    exportBirds()
                }

                if (layerDivision == 2 ){
                    exportReptilia()
                }

                if (layerDivision == 3 ){
                    exportMammal()
                }

                if (layerDivision == 4 ){
                    exportFish()
                }

                if (layerDivision == 5 ){
                    exportInsects()
                }

                if (layerDivision == 6 ){
                    exportFlora()
                }
            }
        }
    }

    private fun checkGPs() {
        if (Utils.availableLocationService(context)) {
            startLocation()
        } else {
            gpsCheckAlert.sendEmptyMessage(0)
        }
    }

    private fun startLocation() {

        if (showLoading) {
            if (progressDialog != null) {
                progressDialog!!.show()
            }
        }

        val smartLocation = SmartLocation.Builder(context).logging(true).build()
        smartLocation.location(LocationGooglePlayServicesWithFallbackProvider(context)).oneFix().start(this)

        val myLooper = Looper.myLooper()
        val myHandler = Handler(myLooper)
        myHandler.postDelayed({
            if (latitude == -1.0 || longitude == -1.0) {
                stopLocation()
            }
        }, (5 * 1000).toLong())
    }

    override fun onLocationUpdated(location: Location?) {

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        val time = System.currentTimeMillis()

        val difference = ((nowTime - time) / 1000)

        println("difference $difference")

        val geometryFactory = GeometryFactory()


        val currentPoint = geometryFactory.createPoint(Coordinate(location!!.latitude, location!!.longitude))

            if(trackingdiv) {
                val tracking: Tracking = Tracking(null, location.latitude, location.longitude)

                dbManager.inserttracking(tracking)

                Toast.makeText(this,"insert.",Toast.LENGTH_SHORT).show()
            }

//        if (prevPoint != null) {
//            val distance = DistanceOp.distance(prevPoint!!, currentPoint);
//            if(distance < 3) {
//                // insert
//
//                if(trackingdiv == true) {
//                    val tracking: Tracking = Tracking(null, location.latitude, location.longitude)
//
//                    dbManager!!.inserttracking(tracking)
//                }
//            }
//        } else {
//            // insert
//
//            if(trackingdiv == true) {
//                val tracking: Tracking = Tracking(null, location.latitude, location.longitude)
//
//                dbManager!!.inserttracking(tracking)
//            }
//        }

        prevPoint = currentPoint

//         System.out.println("onLocationUpdated : " + location);

        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
        }

    }

    internal var gpsCheckAlert: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val mainGpsSearchCount = PrefUtils.getIntPreference(context, "mainGpsSearchCount", 0)

            if (mainGpsSearchCount == 0) {

                val builder = AlertDialog.Builder(context)
                builder.setTitle("확인")
                builder.setMessage("위치 서비스 이용이 제한되어 있습니다.\n설정에서 위치 서비스 이용을 허용해주세요.")
                builder.setCancelable(true)
                builder.setNegativeButton("취소") { dialog, id ->
                    dialog.cancel()

                    latitude = 37.5203175
                    longitude = 126.9107831
                }
                builder.setPositiveButton("설정") { dialog, id ->
                    dialog.cancel()
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                val alert = builder.create()
                alert.show()
            }
        }
    }

    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_FINE_LOCATION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION)
            }
            REQUEST_ACCESS_COARSE_LOCATION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGPs()
            }
            WRITE_EXTERNAL_STORAGE -> {
                    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)

    //                export()
                }else {
                    Toast.makeText(this,"권한사용을 동의해주셔야 이용이 가능합니다.",Toast.LENGTH_SHORT).show()
                }
            }
            READ_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (layerDivision == 0 ){
                        exportBiotope()
                    }

                    if (layerDivision == 1 ){
                        exportBirds()
                    }

                    if (layerDivision == 2 ){
                        exportReptilia()
                    }

                    if (layerDivision == 3 ){
                        exportMammal()
                    }

                    if (layerDivision == 4 ){
                        exportFish()
                    }

                    if (layerDivision == 5 ){
                        exportInsects()
                    }

                    if (layerDivision == 6 ){
                        exportFlora()
                    }
                }
            }
        }

    }

    fun timerStart(){
        val task = object : TimerTask() {
            override fun run() {
                loadDataHandler.sendEmptyMessage(0)
            }
        }

        timer = Timer()
        timer!!.schedule(task, 0, 5000)

    }

    private fun splitPolygon() {

        if (!splitRL.isSelected) {
            return
        }

        if (editingPolygon == null) {
            return
        }

        val layerInfo = editingPolygon?.tag as LayerInfo

        val oldAttributeKey = layerInfo.attrubuteKey

        val splited = Utils.splitPolygon(toJTSPolygon(editingPolygon!!), toJTSLineString(polylineForSplitGuide!!))

        offSplitBtn()

        polylineForSplitGuide?.remove()

        polylineForSplitGuide = null

        editingPolygon?.remove()
        editingPolygon = null

        for (idx in 0..(splited.numGeometries - 1)) {
            var polygon = splited.getGeometryN(idx)
            // polygon = polygon.buffer(-0.00002)

            val polygonOptions = PolygonOptions()
            polygonOptions.fillColor(getColor())
            polygonOptions.strokeWidth(1.0f)
            polygonOptions.strokeColor(Color.BLACK)

            for (coordinate in polygon.coordinates) {
                polygonOptions.add(LatLng(coordinate.y, coordinate.x))
            }

            val po = googleMap.addPolygon(polygonOptions)
            // po.zIndex = 5.0f

            val newAttributeKey = getAttributeKey(layerInfo.layer)
            val newLayerInfo = LayerInfo()
            newLayerInfo.attrubuteKey = newAttributeKey
            newLayerInfo.layer = layerInfo.layer

            po.tag = newLayerInfo

            po.isClickable = true

            polygons.add(po)
            allPolygons.add(po)

            // copy data
            copyRow("biotopeAttribute", oldAttributeKey, newAttributeKey)

        }
    }

    override fun onBackPressed() {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("종료하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        finish()
                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
    }

    fun chkDivision(clickLayer: Int):Boolean{
        if (clickLayer == currentLayer){
            return true
        } else {
            return false
        }
    }

}
