package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.FragmentActivity
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
import hntecology.ecology.model.GpsSet
import kotlinx.android.synthetic.main.activity_main.*
import org.geotools.data.DataUtilities
import org.geotools.data.DefaultTransaction
import org.geotools.data.collection.ListFeatureCollection
import org.geotools.data.shapefile.ShapefileDataStore
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.geotools.data.simple.SimpleFeatureStore
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile
import org.opengis.feature.simple.SimpleFeature
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, View.OnTouchListener, GoogleMap.OnCameraMoveListener {

    private val PLAY_SERVICES_RESOLUTION_REQUEST: Int = 1000
    private val PolygonCallBackData = 1001
    private val dlg_gpsCallbackData = 1002
    private val REQUEST_LAYER = 1003


    private val LAYER_BIOTOPE = 2001
    private val LAYER_BIRDS = 2002
    private val LAYER_REPTILIA = 2003
    private val LAYER_MAMMALIA = 2004
    private val LAYER_FISH = 2005
    private val LAYER_INSECT = 2006
    private val LAYER_FLORA = 2007
    private val LAYER_ZOOBENTHOS = 2008


    private lateinit var context: MainActivity

    private lateinit var mGestureDetector: GestureDetector
    private lateinit var googleMap: GoogleMap

    private var points = ArrayList<Marker>()
    private var polygonsToUnion = ArrayList<Polygon>()

    var latitude:Double = 126.79235
    var longitude:Double = 37.39627

    var dbManager: DataBaseHelper? = null


    // 3. biotope  , 6.birds , 7.Reptilia , 8.mammalia  9. fish, 10.insect, 11.flora , 13. zoobenthos
    var currentLayer = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this
        dbManager = DataBaseHelper(this)

        val db = dbManager!!.createDataBase()
        val dataList:Array<String> = arrayOf("*")
        val data =  db.query("settings",dataList,null,null,null,null,"id desc","1")

/*        PrefUtils.setPreference(this, "latitude", latitude);
        PrefUtils.setPreference(this, "longitude", longitude);*/
        while (data.moveToNext()) {

            var gpsset:GpsSet = GpsSet(data.getInt(0),data.getDouble(1),data.getDouble(2))

            latitude = gpsset.latitude!!
            longitude = gpsset.longitude!!

        }

        mGestureDetector = GestureDetector(this, GestureListener())

        drawer_view.setOnTouchListener(this)

        btn_layer.setOnClickListener {
            val intent = Intent(this, DlgLayersActivity::class.java)
            startActivityForResult(intent, REQUEST_LAYER)
        }

        // 비오톱 추가
        btn_biotope.setOnClickListener {

            currentLayer = LAYER_BIOTOPE

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //조류 추가
        btn_birds.setOnClickListener {

            currentLayer = LAYER_BIRDS

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //양서ㆍ파충류 추가
        btn_Reptilia.setOnClickListener {

            currentLayer = LAYER_REPTILIA

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //포유류 추가
        btn_mammalia.setOnClickListener {

            currentLayer = LAYER_MAMMALIA

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //어류 추가
        btn_fish.setOnClickListener {

            currentLayer = LAYER_FISH

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //곤충 추가
        btn_insect.setOnClickListener {

            currentLayer = LAYER_INSECT

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }

        //식물 추가
        btn_flora.setOnClickListener {

            currentLayer = LAYER_FLORA

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        }


        //저서무척추동물 추가
        btn_zoobenthos.setOnClickListener {

            currentLayer = LAYER_ZOOBENTHOS

            if(drawer_view.visibility == View.VISIBLE) {
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

            endDraw()
        }

        //좌표지정 버튼
        btn_gps_select.setOnClickListener {
            val intent:Intent = Intent(this, hntecology.ecology.activities.Dlg_gps::class.java)
            startActivityForResult(intent, dlg_gpsCallbackData)
        }

        btn_satellite.setOnClickListener {


           var satelite:String = btn_satellite.text.toString()

            if(satelite == "위성 지도"){

                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                btn_satellite.text = "일반 지도"
            }else{

                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                btn_satellite.text = "위성 지도"
            }
        }

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        startRegistrationService()

        TVtimeTV.text = getTime()

        logoutBtn.setOnClickListener{

            var builder:AlertDialog.Builder =  AlertDialog.Builder(context)
            builder.setMessage("로그아웃 하시겠습니까?")
            builder.setCancelable(true)
            builder.setNegativeButton("취소", DialogInterface.OnClickListener{ dialogInterface, i ->
                dialogInterface.cancel()

            })
            builder.setPositiveButton("확인", DialogInterface.OnClickListener{ dialogInterface, i ->
                dialogInterface.cancel()

                PrefUtils.clear(context)

                val intent:Intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            })
            builder.show()

        }

        layerNameTV.setOnClickListener {
            loadLayer(currentFileName, currentLayerName)
        }

        exportBtn.setOnClickListener {
            export()
        }

        delPointRL.setOnClickListener {

            when(currentLayer) {
                LAYER_BIOTOPE -> {
                    if(latlngs.size > 0) {
                        latlngs.removeAt(latlngs.size - 1)
                        drawPolygon()
                    }
                }

                else -> {

                    println("points.size : ${points.size}")

                    if(points.size > 0) {
                        val lastPoint = points.removeAt(points.size - 1)
                        lastPoint.remove()
                    }
                }
            }


        }

        // 도형 분리
        splitRL.setOnClickListener {
            if(splitRL.isSelected) {
                offSplitBtn()
            } else {
                onSplitBtn()
            }
        }

        // 도형 합지기
        combineRL.setOnClickListener {

            endDraw()

            if(combineRL.isSelected) {

                combineRL.isSelected = false
                combineTV.setTextColor(Color.parseColor("#333333"))
                combineTV.setTypeface(null, Typeface.NORMAL);

                unionPolygons()

            } else {
                combineRL.isSelected = true

                combineTV.setTextColor(Color.BLACK)
                combineTV.setTypeface(null, Typeface.BOLD);
            }


        }
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

        if(resultCode == Activity.RESULT_OK){

            when (requestCode) {

                PLAY_SERVICES_RESOLUTION_REQUEST ->{

                    initilizeMap()
                }

                PolygonCallBackData -> {


                }

                dlg_gpsCallbackData -> {

                    latitude = data!!.getDoubleExtra("latitude",126.79235)
                    longitude = data.getDoubleExtra("longitude",37.39627)


                    val gpsSet:GpsSet = GpsSet(null,latitude,longitude)
                    dbManager!!.insertGpsSet(gpsSet)
                    onMapReady(googleMap)
                }

                REQUEST_LAYER -> {
                    val file_name = data!!.getStringExtra("file_name")
                    val layer_name = data.getStringExtra("layer_name")
                    loadLayer(file_name, layer_name)
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
        if(map == null) {
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialMapCenter, 16f))



        // 마커클릭 이벤트 처리
        // GoogleMap 에 마커클릭 이벤트 설정 가능.
        googleMap.setOnMarkerClickListener {
            marker ->

            var attrubuteKey = getAttrubuteKey()
            var intent:Intent? = null
            when(currentLayer){

                LAYER_BIRDS -> {
                    attrubuteKey += "birds"
                    intent = Intent(this, BirdsActivity::class.java)
                }

                LAYER_REPTILIA -> {
                    attrubuteKey += "reptilia"
                    intent = Intent(this, ReptiliaActivity::class.java)

                }

                LAYER_MAMMALIA -> {

                    attrubuteKey += "mammalia"
                    intent = Intent(this, MammaliaActivity::class.java)

                }

                LAYER_FISH -> {

                    attrubuteKey += "fish"
                    intent = Intent(this, FishActivity::class.java)
                }

                LAYER_INSECT -> {
                    attrubuteKey += "insect"
                    intent = Intent(this, InsectActivity::class.java)

                }

                LAYER_FLORA -> {

                    attrubuteKey += "flora"
                    intent = Intent(this, FloraActivity::class.java)

                }

                LAYER_ZOOBENTHOS ->{
                    attrubuteKey += "zoobenthos"
                    intent = Intent(this, ZoobenthosActivity::class.java)

                }
            }

            val layerInfo = LayerInfo()
            layerInfo.attrubuteKey = attrubuteKey
            layerInfo.layer = currentLayer

            marker.tag = layerInfo

            intent!!.putExtra("id", attrubuteKey.toString())

            startActivityForResult(intent, PolygonCallBackData)


            false
        }

        // 클릭시 태그 데이터 있는지 확인 없으면 바로 넘기고 있으면 있는걸로 호출.
        // tag 리절트로 가져와서 태그 설정
        googleMap.setOnPolygonClickListener { polygon ->

            val layerInfo = polygon.tag as LayerInfo
            var myLayer = layerInfo.layer

            var attrubuteKey = getAttrubuteKey()
            var intent:Intent? = null
            when(myLayer){

                LAYER_BIOTOPE -> {

                    if(combineRL.isSelected) {

                        polygonsToUnion.add(polygon)

                        polygon.strokeColor = Color.MAGENTA

                        return@setOnPolygonClickListener

                    } else {
                        attrubuteKey += "biotope"
                        intent = Intent(this, BiotopeActivity::class.java)
                    }
                }

                LAYER_BIRDS -> {
                    attrubuteKey += "birds"
                    intent = Intent(this, BirdsActivity::class.java)
                }

                LAYER_REPTILIA -> {
                    attrubuteKey += "reptilia"
                    intent = Intent(this, ReptiliaActivity::class.java)

                }

                LAYER_MAMMALIA -> {

                    attrubuteKey += "mammalia"
                    intent = Intent(this, MammaliaActivity::class.java)

                }

                LAYER_FISH -> {

                    attrubuteKey += "fish"
                    intent = Intent(this, FishActivity::class.java)
                }

                LAYER_INSECT -> {
                    attrubuteKey += "insect"
                    intent = Intent(this, InsectActivity::class.java)

                }

                LAYER_FLORA -> {

                    attrubuteKey += "flora"
                    intent = Intent(this, FloraActivity::class.java)

                }

                LAYER_ZOOBENTHOS ->{
                    attrubuteKey += "zoobenthos"
                    intent = Intent(this, ZoobenthosActivity::class.java)

                }
            }

            intent!!.putExtra("id", attrubuteKey.toString())
            startActivityForResult(intent, PolygonCallBackData)
        }


    }

    private var parsed: Boolean = false

    override fun onCameraIdle() {

        if(parsed) {
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

    private fun loadLayer(fileName: String, layerName: String) {

        if(fileName == null || fileName.length == 0) {
            return
        }

        val zoom = googleMap.cameraPosition.zoom
        if(zoom < 16) {
            Utils.showNotification(context, "지도 레벨을 16이상으로 확대한 후 이용하세요.")
            return
        }

        googleMap.clear()

        currentFileName = fileName
        currentLayerName = layerName

        layerNameTV.text = currentLayerName

        val bounds = googleMap.projection.visibleRegion.latLngBounds
        LoadLayerTask(fileName).execute(bounds)
    }

    private inner class LoadLayerTask(layerName: String) : AsyncTask<LatLngBounds, PolygonOptions, Boolean>() {

        var layerName = layerName

        override fun doInBackground(vararg latLngBounds:LatLngBounds): Boolean {

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

                if(!geometry.intersects(mapBoundary)) {
                    continue
                }

                if (geometry.geometryType.equals("Polygon")) {
                    val polygonOptions = PolygonOptions()
                    polygonOptions.fillColor(Color.parseColor(getLayerColor(layerName)))
                    polygonOptions.strokeColor(Color.TRANSPARENT)

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
            polygon.tag = layerName

            val layerInfo = LayerInfo()
            layerInfo.layer = currentLayer

            polygon.tag = layerInfo

        }

        override fun onPostExecute(result: Boolean?) {
            Utils.hideLoading(context)

            print("Post........")
        }
    }

    private fun getLayerColor(layerName: String): String? {
        if("SH_biotop_orig".equals(layerName)) {
            return "#85b66f"
        } else if("SH_dummy".equals(layerName)) {
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

    /**
     * Ontouch event will draw poly line along the touch points
     *
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var X1 = event.x.toInt()
        var Y1 = event.y.toInt()

        // println("${event.action} $X1, $Y1")

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

            }

            MotionEvent.ACTION_MOVE -> if (drawer_view.visibility == View.VISIBLE) {

                /*
                X1 = event.x.toInt()
                Y1 = event.y.toInt()
                point = Point()
                point.x = X1
                point.y = Y1
                val geoPoint = googleMap.projection.fromScreenLocation(point)

                println("geoPoint : $geoPoint")

                latlngs.add(geoPoint)
                val mPolylineOptions = PolylineOptions()
                mPolylineOptions.color(Color.RED)
                mPolylineOptions.width(3.0f)
                mPolylineOptions.addAll(latlngs)


                googleMap.addPolyline(mPolylineOptions)
                */
            }

            MotionEvent.ACTION_UP -> {

                X1 = event.x.toInt()
                Y1 = event.y.toInt()
                point = Point()
                point.x = X1
                point.y = Y1
                val geoPoint = googleMap.projection.fromScreenLocation(point)

                if(splitRL.isSelected) {

                    if(editingPolygon == null) {
                        return false
                    }

                    val myLayer = editingPolygon?.tag as LayerInfo

                    val polylineOptions = PolylineOptions()
                    polylineOptions.add(startGeoPoint)
                    polylineOptions.add(geoPoint)

                    polylineOptions.width(5f)
                    polylineOptions.color(Color.YELLOW);

                    val polyline = googleMap.addPolyline(polylineOptions)

                    val splited = Utils.splitPolygon(toJTSPolygon(editingPolygon!!), toJTSLineString(polyline!!))

                    offSplitBtn()

                    polyline.remove()

                    editingPolygon?.remove()
                    editingPolygon = null

                    for(idx in 0..(splited.numGeometries - 1)) {
                        var polygon = splited.getGeometryN(idx)
                        polygon = polygon.buffer(-0.00002)

                        val polygonOptions = PolygonOptions()
                        polygonOptions.fillColor(getColor())
                        polygonOptions.strokeColor(Color.TRANSPARENT)

                        for(coordinate in polygon.coordinates) {
                            polygonOptions.add(LatLng(coordinate.y, coordinate.x))
                        }

                        val po = googleMap.addPolygon(polygonOptions)
                        po.tag = myLayer
                        po.isClickable = true
                    }

                } else {

                    if(!combineRL.isSelected) {
                        // print("Poinnts array size : ${latlngs.size}")

                        // 3. biotope  , 6.birds , 7.Reptilia , 8.mammalia  9. fish, 10.insect, 11.flora , 13. zoobenthos
                        when(currentLayer) {

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

    private fun drawPoint(geoPoint:LatLng) {
        val markerOptions = MarkerOptions()
        markerOptions.position(geoPoint)
        // markerOptions.title("Marker in Sydney")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        markerOptions.alpha(1.0f)
        markerOptions.draggable(true)

        val point = googleMap.addMarker(markerOptions)

        points.add(point)

    }

    fun initEditingPolygon() {
        val polygonOptions = PolygonOptions()
        polygonOptions.fillColor(getColor())
        polygonOptions.strokeColor(Color.TRANSPARENT)
        polygonOptions.addAll(latlngs)

        editingPolygon = googleMap.addPolygon(polygonOptions)

        editingPolygon?.isClickable = true

        val layerInfo = LayerInfo()
        layerInfo.attrubuteKey = getAttrubuteKey()
        layerInfo.layer = currentLayer

        editingPolygon?.tag = layerInfo

    }

    private fun drawPolygon() {

        updateDelPointText()

        if (latlngs.size == 0) {

            editingPolygon = null

            return
        }

        editingPolygon!!.points = latlngs
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

    fun getTime():String{

        val date = Date()
        val fullTime = SimpleDateFormat("yyyy-MM-dd")

        return fullTime.format(date).toString()
    }

    fun getAttrubuteKey():String{

        val time = System.currentTimeMillis()
        val dayTime = SimpleDateFormat("yyyyMM")
        val strDT = dayTime.format(Date(time))

        return strDT
    }

    fun export() {

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
            featureStore.transaction = transaction

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

        val geometries = Array<Geometry?>(polygonsToUnion.size) { null }

        for (idx in 0..(polygonsToUnion.size - 1)) {
            val polygon = polygonsToUnion.get(idx)
            var createdPolygon = toJTSPolygon(polygon)
            if(idx == 0) {
                createdPolygon = createdPolygon.buffer(0.0006) as org.locationtech.jts.geom.Polygon
            }

            println("createdPolygon : $createdPolygon")

            geometries[idx] = createdPolygon

        }

        val gc = GeometryFactory().createGeometryCollection(geometries);

        try {
            val unioned = gc.union()

            println(unioned)

            println("unioned.geometryType : ${unioned.geometryType}")

            if("Polygon" == unioned.geometryType) {

                val layerInfo = polygonsToUnion.first().tag as LayerInfo
                val myLayer = layerInfo.layer

                // delete
                for (polygon in polygonsToUnion) {
                    polygon.remove()
                }

                val polygonOptions = PolygonOptions()
                polygonOptions.fillColor(getColor())
                polygonOptions.strokeColor(Color.TRANSPARENT)

                for(coordinate in unioned.coordinates) {
                    polygonOptions.add(LatLng(coordinate.y, coordinate.x))
                }


                editingPolygon = googleMap.addPolygon(polygonOptions)
                editingPolygon?.tag = layerInfo
                editingPolygon?.isClickable = true

                polygonsToUnion.clear()
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
}
