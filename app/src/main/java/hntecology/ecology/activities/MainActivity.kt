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
import org.geotools.data.DataUtilities
import org.geotools.data.DefaultTransaction
import org.geotools.data.Transaction
import org.geotools.data.collection.ListFeatureCollection
import org.geotools.data.shapefile.ShapefileDataStore
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.geotools.data.simple.SimpleFeatureStore
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.operation.distance.DistanceOp
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile
import org.opengis.feature.simple.SimpleFeature
import java.io.File
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

public class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, View.OnTouchListener, GoogleMap.OnCameraMoveListener, OnLocationUpdatedListener {

    val REQUEST_FINE_LOCATION = 1
    val REQUEST_ACCESS_COARSE_LOCATION = 2

    private val PLAY_SERVICES_RESOLUTION_REQUEST: Int = 1000
    private val PolygonCallBackData = 1001
    private val dlg_gpsCallbackData = 1002
    private val REQUEST_LAYER = 1003


    private val LAYER = 2000
    private val LAYER_BIOTOPE = 2001
    private val LAYER_BIRDS = 2002
    private val LAYER_REPTILIA = 2003
    private val LAYER_MAMMALIA = 2004
    private val LAYER_FISH = 2005
    private val LAYER_INSECT = 2006
    private val LAYER_FLORA = 2007
    private val LAYER_ZOOBENTHOS = 2008
    private val LAYER_MYLOCATION = 2009

    var types : ArrayList<String> = ArrayList<String>()


    private lateinit var context: Context

    private lateinit var mGestureDetector: GestureDetector
    private lateinit var googleMap: GoogleMap

    private var points = ArrayList<Marker>()
    private var polygonsToUnion = ArrayList<Polygon>()

    var latitude: Double = 126.79235
    var longitude: Double = 37.39627

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var biotopeData:ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()
    var birdsData:ArrayList<Birds_attribute> = ArrayList<Birds_attribute>()
    var fishData:ArrayList<Fish_attribute> = ArrayList<Fish_attribute>()
    var floraData:ArrayList<Flora_Attribute> = ArrayList<Flora_Attribute>()
    var insectData:ArrayList<Insect_attribute> = ArrayList<Insect_attribute>()
    var mammaliaData:ArrayList<Mammal_attribute> = ArrayList<Mammal_attribute>()
    var reptiliaData:ArrayList<Reptilia_attribute> = ArrayList<Reptilia_attribute>()

    var biotopePk:String? = String()
    var birdsPk:String? = String()
    var fishPk:String? = String()
    var floraPk:String? = String()
    var insectPk:String? = String()
    var mammaliaPk:String? = String()
    var reptiliaPk:String? = String()

    val ColumnName:ArrayList<String> = ArrayList<String>()

    // 3. biotope  , 6.birds , 7.Reptilia , 8.mammalia  9. fish, 10.insect, 11.flora , 13. zoobenthos

    var currentLayer = -1

    var myLocation: Tracking? = null

    var prevPoint: Geometry? = null

    private var showLoading = false

    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this
        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase()
        val dataList: Array<String> = arrayOf("*")
        val data = db!!.query("settings", dataList, null, null, null, null, "id desc", "1")

        ColumnName.add("BIOTOPEATTRIBUTE")
        ColumnName.add("BIRDSATTRIBUTE")
        ColumnName.add("REPTILIAATTRIBUTE")
        ColumnName.add("MAMMALATTRIBUTE")
        ColumnName.add("FISHATTRIBUTE")
        ColumnName.add("INSECTATTRIBUTE")
        ColumnName.add("FLORAATTRIBUTE")

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

            startActivityForResult(intent, REQUEST_LAYER)
        }

        // 비오톱 추가
        btn_biotope.setOnClickListener {

            currentLayer = LAYER_BIOTOPE

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //조류 추가
        btn_birds.setOnClickListener {

            currentLayer = LAYER_BIRDS

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //양서ㆍ파충류 추가
        btn_Reptilia.setOnClickListener {

            currentLayer = LAYER_REPTILIA

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //포유류 추가
        btn_mammalia.setOnClickListener {

            currentLayer = LAYER_MAMMALIA

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //어류 추가
        btn_fish.setOnClickListener {

            currentLayer = LAYER_FISH

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //곤충 추가
        btn_insect.setOnClickListener {

            currentLayer = LAYER_INSECT

            if (drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //식물 추가
        btn_flora.setOnClickListener {

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

        layerNameTV.setOnClickListener {
            loadLayer(currentFileName, currentLayerName, "")
        }

        exportBtn.setOnClickListener {
            export()
        }

        btn_mygps.setOnClickListener {

            currentLayer = LAYER_MYLOCATION

            val latlng = LatLng(this.latitude, this.longitude)

            drawPoint(latlng)

//            val makerOption =MarkerOptions()
//
//            makerOption.position(latlng)
//                    .title("현재 위치")
//
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

        myLocation = Tracking(null, latitude, longitude)


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
            polygon.strokeWidth = 5.0f
            polygon.strokeColor = Color.WHITE
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

                dlg_gpsCallbackData -> {

                    latitude = data!!.getDoubleExtra("latitude", 126.79235)
                    longitude = data.getDoubleExtra("longitude", 37.39627)

                    val gpsSet: GpsSet = GpsSet(null, latitude, longitude)
                    dbManager!!.insertGpsSet(gpsSet)
                    onMapReady(googleMap)
                }

                REQUEST_LAYER -> {
                    val file_name = data!!.getStringExtra("file_name")
                    val layer_name = data.getStringExtra("layer_name")


                    var jsonOb: ArrayList<LayerModel> = ArrayList<LayerModel>()

                    jsonOb = data.getSerializableExtra("data") as ArrayList<LayerModel>

                    googleMap.clear()

                    if(types.size >= 1 && types!=null){
                        types.clear()
                    }

                    for (i in 0..jsonOb.size - 1) {
                        loadLayer(jsonOb.get(i).file_name, jsonOb.get(i).layer_name,jsonOb.get(i).type)

                        println("jsonOB . filename ${jsonOb.get(i).file_name}")

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

            val layerInfo = marker.tag as LayerInfo
            var myLayer = layerInfo.layer

            var attrubuteKey = layerInfo.attrubuteKey
            var intent: Intent? = null
            when (myLayer) {

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

            }

            if(myLayer != LAYER_MYLOCATION && myLayer != LAYER){
                intent!!.putExtra("id", attrubuteKey.toString())

                startActivityForResult(intent, PolygonCallBackData)
            }

            false
        }

        // 클릭시 태그 데이터 있는지 확인 없으면 바로 넘기고 있으면 있는걸로 호출.
        // tag 리절트로 가져와서 태그 설정
        googleMap.setOnPolygonClickListener { polygon ->

            println("click")

            val layerInfo = polygon.tag as LayerInfo
            var myLayer = layerInfo.layer

            var attrubuteKey = layerInfo.attrubuteKey
            var intent: Intent? = null

            println("attrubuteKey================================================== : " + attrubuteKey)

            when (myLayer) {

                LAYER_BIOTOPE -> {

                    if (unionRL.isSelected) {

                        if (polygonsToUnion.contains(polygon)) {
                            polygonsToUnion.remove(polygon)
                            polygon.strokeWidth = 5.0f
                            polygon.strokeColor = Color.WHITE
                            return@setOnPolygonClickListener
                        }

                        if (polygonsToUnion.size == 2) {
                            Utils.alert(context, "2 곳만 선택해서 합칠 수 있습니다.")
                            return@setOnPolygonClickListener
                        }

                        polygonsToUnion.add(polygon)
                        polygon.strokeWidth = 10.0f
                        polygon.strokeColor = Color.MAGENTA

                        return@setOnPolygonClickListener

                    } else {
                        intent = Intent(this, BiotopeActivity::class.java)
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
            }

            println("aa : $attrubuteKey")

            if(myLayer != LAYER_MYLOCATION && myLayer != LAYER){
                intent!!.putExtra("id", attrubuteKey.toString())

                startActivityForResult(intent, PolygonCallBackData)
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

    private fun loadLayer(fileName: String, layerName: String, Type: String) {

        if (fileName == null || fileName.length == 0) {
            return
        }

        val zoom = googleMap.cameraPosition.zoom
        if (zoom < 16) {
            Utils.showNotification(context, "지도 레벨을 16이상으로 확대한 후 이용하세요.")
            // return
        }

        if (zoom < 13) {
            Utils.showNotification(context, "지도 레벨을 16이상으로 확대한 후 이용하세요. 정말 안되요 ㅠㅠㅠ")
            return
        }

        currentFileName = fileName
        currentLayerName = layerName

//        layerNameTV.text = currentLayerName

        val bounds = googleMap.projection.visibleRegion.latLngBounds
        LoadLayerTask(fileName,Type).execute(bounds)
    }

    private inner class LoadLayerTask(layerName: String , Type: String) : AsyncTask<LatLngBounds, PolygonOptions, Boolean>() {

        var layerName = layerName

        var type = Type


        override fun doInBackground(vararg latLngBounds: LatLngBounds): Boolean {

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

            val inputStream = assets.open("$layerName.shp")

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
                    polygonOptions.strokeWidth(5.0f)
                    polygonOptions.strokeColor(Color.WHITE)

                    val coordinates = geometry.coordinates
                    for (j in 0..(coordinates.size - 1)) {
                        val coordinate = coordinates[j]
                        polygonOptions.add(LatLng(coordinate.y, coordinate.x))
                    }

                    publishProgress(polygonOptions)

                    Thread.sleep(10)

                    loadedCnt++
                }

                println("loadedCnt : $loadedCnt / $num")

            }

            return true
        }

        override fun onProgressUpdate(vararg polygonOptions: PolygonOptions?) {
            val polygon = googleMap.addPolygon(polygonOptions[0])
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

            println("layerinfo .layer ===== ${layerInfo.layer}")

            polygon.tag = layerInfo

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

    private val latlngs: ArrayList<LatLng> = ArrayList<LatLng>()

    private var startGeoPoint: LatLng? = null

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

                startGeoPoint = googleMap.projection.fromScreenLocation(point)

                if (splitRL.isSelected) {

                    val polylineOptions = PolylineOptions()
                    polylineOptions.add(startGeoPoint)

                    polylineOptions.width(5f)
                    polylineOptions.color(Color.YELLOW);

                    polylineForSplitGuide = googleMap.addPolyline(polylineOptions)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (splitRL.isSelected) {

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

                        // copy data
                        copyRow("biotopeAttribute", oldAttributeKey, newAttributeKey)

                    }

                } else {

                    if (!unionRL.isSelected) {
                        // print("Poinnts array size : ${latlngs.size}")

                        // 3. biotope  , 6.birds , 7.Reptilia , 8.mammalia  9. fish, 10.insect, 11.flora , 13. zoobenthos
                        when (currentLayer) {

                            LAYER_BIOTOPE -> {

                                latlngs.add(geoPoint)

                                if (latlngs.size == 1) {
                                    initEditingPolygon()
                                }

                                drawPolygon()
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

    private fun drawPoint(geoPoint: LatLng) {
        val markerOptions = MarkerOptions()
        markerOptions.position(geoPoint)
        // markerOptions.title("Marker in Sydney")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        markerOptions.alpha(1.0f)
        markerOptions.draggable(true)

        val marker = googleMap.addMarker(markerOptions)

        val layerInfo = LayerInfo()
        layerInfo.attrubuteKey = getAttributeKey(layerInfo.layer)
        layerInfo.layer = currentLayer

        marker.tag = layerInfo

        var myLayer = layerInfo.layer

        var attrubuteKey = layerInfo.attrubuteKey

        var intent: Intent? = null

        points.add(marker)

        when (myLayer) {

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
        }

        if(myLayer != LAYER_MYLOCATION && myLayer != LAYER) {

            intent!!.putExtra("id", attrubuteKey.toString())

            startActivityForResult(intent, PolygonCallBackData)

            endDraw()
        }

    }

    private fun initEditingPolygon() {
        val polygonOptions = PolygonOptions()
        polygonOptions.fillColor(getColor())
        polygonOptions.strokeWidth(5.0f)
        polygonOptions.strokeColor(Color.WHITE)
        polygonOptions.addAll(latlngs)

        editingPolygon = googleMap.addPolygon(polygonOptions)
        // editingPolygon?.zIndex = 5.0f
        editingPolygon?.isClickable = true

        val layerInfo = LayerInfo()
        layerInfo.attrubuteKey = getAttributeKey(layerInfo.layer)
        layerInfo.layer = currentLayer

        println("getAttributeKey(layerInfo.layer) : " + getAttributeKey(layerInfo.layer))

        editingPolygon?.tag = layerInfo

    }

    private fun drawPolygon() {

        updateDelPointText()

        if (latlngs.size == 0) {

            editingPolygon = null

            return
        }

        editingPolygon!!.points = latlngs

        if(latlngs.size >= 3) {
            val jtsPolygon = toJTSPolygon(editingPolygon!!)
            if(!jtsPolygon.isValid) {
                latlngs.remove(latlngs.last())
                drawPolygon()

                Utils.showNotification(context, "잘못된 지점입니다. 다른 곳을 선택해주세요.")
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
        }

        println("attributeKey : $attributeKey")

        return attributeKey
    }

    fun export() {

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        val dataList: Array<String> = arrayOf("*")

        var biotopedata= db.query("biotopeAttribute", dataList, null, null, "id", null, "", null)

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

            biotopeData.add(biotope_attribute)
        }

        if(biotopeData.size >= 1 && biotopeData.get(0).id != null) {

            for (i in 0..biotopeData.size - 1) {
                biotopePk += biotopeData.get(i).id + "\n"
            }
        }

        val birdsdata= db.query("birdsAttribute", dataList, null, null, "id", null, "", null)

        while (birdsdata.moveToNext()) {

            var birds_attribute: Birds_attribute = Birds_attribute(birdsdata.getString(0), birdsdata.getString(1), birdsdata.getString(2), birdsdata.getString(3)
                    , birdsdata.getString(4), birdsdata.getString(5), birdsdata.getString(6), birdsdata.getString(7)
                    , birdsdata.getString(8), birdsdata.getFloat(9), birdsdata.getString(10), birdsdata.getInt(11), birdsdata.getString(12), birdsdata.getString(13), birdsdata.getString(14)
                    , birdsdata.getString(15), birdsdata.getInt(16), birdsdata.getString(17), birdsdata.getString(18), birdsdata.getString(19), birdsdata.getString(20), birdsdata.getString(21)
                    , birdsdata.getString(22), birdsdata.getString(23), birdsdata.getFloat(24), birdsdata.getFloat(25) , birdsdata.getString(26))

            birdsData.add(birds_attribute)
        }

        if(birdsData.size >= 1) {

            println("-----------------------${birdsData.size}")

            for (i in 0..birdsData.size - 1) {
                birdsPk += birdsData.get(i).id + "\n"
            }

        }

        val reptiliadata= db.query("reptiliaAttribute", dataList, null, null, "id", null, "", null)

        while (reptiliadata.moveToNext()) {

            var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(reptiliadata.getString(0), reptiliadata.getString(1), reptiliadata.getString(2), reptiliadata.getString(3), reptiliadata.getString(4)
                    , reptiliadata.getString(5), reptiliadata.getString(6), reptiliadata.getString(7), reptiliadata.getString(8), reptiliadata.getFloat(9), reptiliadata.getString(10), reptiliadata.getInt(11)
                    , reptiliadata.getString(12), reptiliadata.getString(13), reptiliadata.getString(14), reptiliadata.getString(15), reptiliadata.getInt(16), reptiliadata.getInt(17), reptiliadata.getInt(18)
                    , reptiliadata.getString(19), reptiliadata.getString(20), reptiliadata.getString(21), reptiliadata.getString(22), reptiliadata.getString(23), reptiliadata.getString(24)
                    , reptiliadata.getInt(25), reptiliadata.getInt(26), reptiliadata.getInt(27), reptiliadata.getFloat(28), reptiliadata.getFloat(29),reptiliadata.getString(30))

            reptiliaData.add(reptilia_attribute)

        }

        if(reptiliaData.size >= 1 && reptiliaData.get(0).id != null) {

            for (i in 0..reptiliaData.size - 1) {
                reptiliaPk += reptiliaData.get(i).id + "\n"
            }
        }




        val mammaldata = db.query("mammalAttribute", dataList, null, null, "id", null, "", null)

        while (mammaldata.moveToNext()) {

            var mammal_attribute: Mammal_attribute = Mammal_attribute(mammaldata.getString(0), mammaldata.getString(1), mammaldata.getString(2), mammaldata.getString(3), mammaldata.getString(4)
                    , mammaldata.getString(5), mammaldata.getString(6), mammaldata.getString(7), mammaldata.getString(8), mammaldata.getFloat(9), mammaldata.getString(10), mammaldata.getInt(11)
                    , mammaldata.getString(12), mammaldata.getString(13), mammaldata.getString(14), mammaldata.getString(15), mammaldata.getString(16), mammaldata.getString(17)
                    , mammaldata.getInt(18), mammaldata.getString(19), mammaldata.getString(20), mammaldata.getFloat(21), mammaldata.getFloat(22), mammaldata.getString(23), mammaldata.getString(24)
                    , mammaldata.getString(25), mammaldata.getString(26), mammaldata.getString(27))

            mammaliaData.add(mammal_attribute)

        }

        if(mammaliaData.size >= 1 && mammaliaData.get(0).id != null) {

            for (i in 0..mammaliaData.size - 1) {
                mammaliaPk += mammaliaData.get(i).id + "\n"
            }
        }

        val fishdata = db.query("fishAttribute", dataList, null, null, "id", null, "", null)

        while (fishdata.moveToNext()) {

            var fish_attribute: Fish_attribute = Fish_attribute(fishdata.getString(0), fishdata.getString(1), fishdata.getString(2), fishdata.getString(3), fishdata.getString(4), fishdata.getString(5)
                    , fishdata.getString(6), fishdata.getString(7), fishdata.getString(8), fishdata.getFloat(9), fishdata.getString(10), fishdata.getString(11), fishdata.getString(12)
                    , fishdata.getInt(13), fishdata.getString(14), fishdata.getInt(15), fishdata.getInt(16), fishdata.getString(17), fishdata.getFloat(18), fishdata.getFloat(19), fishdata.getString(20)
                    , fishdata.getInt(21), fishdata.getInt(22), fishdata.getInt(23), fishdata.getInt(24), fishdata.getString(25), fishdata.getString(26), fishdata.getString(27), fishdata.getInt(28)
                    , fishdata.getString(29), fishdata.getString(30), fishdata.getString(31), fishdata.getInt(32), fishdata.getString(33), fishdata.getString(34), fishdata.getString(35), fishdata.getString(36))

            fishData.add(fish_attribute)

        }

        if(fishData.size >= 1 && fishData.get(0).id != null) {

            for (i in 0..fishData.size - 1) {
                fishPk += fishData.get(i).id + "\n"
            }
        }

        val insectdata = db.query("insectAttribute", dataList, null, null, "id", null, "", null)

        while (insectdata.moveToNext()) {

            var insect_attribute: Insect_attribute = Insect_attribute(insectdata.getString(0), insectdata.getString(1), insectdata.getString(2), insectdata.getString(3), insectdata.getString(4)
                    , insectdata.getString(5), insectdata.getString(6), insectdata.getString(7), insectdata.getString(8), insectdata.getFloat(9), insectdata.getString(10), insectdata.getInt(11)
                    , insectdata.getString(12), insectdata.getString(13), insectdata.getString(14), insectdata.getString(15), insectdata.getInt(16), insectdata.getString(17), insectdata.getString(18)
                    , insectdata.getString(19), insectdata.getString(20), insectdata.getString(21), insectdata.getString(22), insectdata.getString(23), insectdata.getString(24), insectdata.getString(25)
                    , insectdata.getFloat(26), insectdata.getFloat(27), insectdata.getString(28))

            insectData.add(insect_attribute)

        }

        if(insectData.size >= 1 && insectData.get(0).id != null) {

            for (i in 0..insectData.size - 1) {
                insectPk += insectData.get(i).id + "\n"
            }
        }

        val floradata = db.query("floraAttribute", dataList, null, null, "id", null, "", null)

        while (floradata.moveToNext()) {

            var flora_Attribute: Flora_Attribute = Flora_Attribute(floradata.getString(0), floradata.getString(1), floradata.getString(2), floradata.getString(3), floradata.getString(4)
                    , floradata.getString(5), floradata.getString(6), floradata.getString(7), floradata.getString(8), floradata.getFloat(9), floradata.getString(10), floradata.getInt(11)
                    , floradata.getString(12), floradata.getString(13), floradata.getString(14), floradata.getString(15), floradata.getString(16), floradata.getString(17), floradata.getString(18)
                    , floradata.getString(19), floradata.getInt(20), floradata.getString(21), floradata.getFloat(22), floradata.getFloat(23), floradata.getString(24))

            floraData.add(flora_Attribute)

        }

        if(floraData.size >= 1 && floraData.get(0).id != null) {

            for (i in 0..floraData.size - 1) {
                floraPk += floraData.get(i).id + "\n"
            }

        }

        println("biotopePk : $biotopePk  ,  birdsPk : $birdsPk  , reptiliaPk : $reptiliaPk   ,  mammalPk : $mammaliaPk  ,  fishPk : $fishPk   ,   insectPk : $insectPk   , floraPk $floraPk ")







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
                polygonOptions.strokeWidth(5.0f)
                polygonOptions.strokeColor(Color.WHITE)

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

        val geometryFactory = GeometryFactory()

        val currentPoint = geometryFactory.createPoint(Coordinate(location!!.latitude, location!!.longitude))

        if (prevPoint != null) {
            val distance = DistanceOp.distance(prevPoint!!, currentPoint);
            if(distance > 5) {
                // insert

                val tracking : Tracking = Tracking(null,location.latitude,location.longitude)

                dbManager!!.inserttracking(tracking)
            }
        } else {
            // insert
            val tracking : Tracking = Tracking(null,location.latitude,location.longitude)

            dbManager!!.inserttracking(tracking)
        }

        prevPoint = currentPoint

//         System.out.println("onLocationUpdated : " + location);

        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude

            System.out.println("latitude ===: " + latitude);
            System.out.println("longitude ===: " + longitude);

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

}
