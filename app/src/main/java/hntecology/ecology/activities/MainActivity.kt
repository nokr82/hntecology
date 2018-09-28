package hntecology.ecology.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
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
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, View.OnTouchListener {

    private val PLAY_SERVICES_RESOLUTION_REQUEST: Int = 1000


    private lateinit var context: MainActivity

    private lateinit var mGestureDetector: GestureDetector
    private lateinit var googleMap: GoogleMap

    val PolygonCallBackData = 1;
    val dlg_gpsCallbackData = 10;
    //var beforePolygon: Polygon? = null
    private lateinit var polygonList:Array<Polygon>

    var latitude:Double = 126.79235
    var longitude:Double = 37.39627

    var dbManager: DataBaseHelper? = null



    var buttonController = 3;       //3. biotope  , 6.birds , 7.Reptilia , 8.mammalia  9. fish, 10.insect, 11.flora , 13. zoobenthos


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this
        dbManager = DataBaseHelper(this)
        val db = dbManager!!.createDataBase()
        val dataList:Array<String> = arrayOf("*");
        val data =  db.query("gps_set",dataList,null,null,null,null,"id desc","1");

/*        PrefUtils.setPreference(this, "latitude", latitude);
        PrefUtils.setPreference(this, "longitude", longitude);*/
        while (data.moveToNext()) {

            var gpsset:GpsSet = GpsSet(data.getInt(0),data.getDouble(1),data.getDouble(2));

            latitude = gpsset.latitude!!
            longitude = gpsset.longitude!!

        }

        mGestureDetector = GestureDetector(this, GestureListener())

        drawer_view.setOnTouchListener(this)

        btn_SH_biotop_orig.setOnClickListener(View.OnClickListener {

            Utils.showLoading(context)

            val latLngBounds = googleMap.projection.visibleRegion.latLngBounds
            googleMap.clear()

            LoadLayerTask("SH_biotop_orig").execute(latLngBounds)
        })

        btn_SH_dummy.setOnClickListener(View.OnClickListener {

            Utils.showLoading(context)

            val latLngBounds = googleMap.projection.visibleRegion.latLngBounds
            googleMap.clear()

            LoadLayerTask("SH_dummy").execute(latLngBounds)
        })
        //비오톱 추가
        btn_biotope.setOnClickListener(View.OnClickListener {

            buttonController = 3

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })

        //조류 추가
        btn_birds.setOnClickListener(View.OnClickListener {

            buttonController =6

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })

        //양서ㆍ파충류 추가
        btn_Reptilia.setOnClickListener(View.OnClickListener {


            buttonController = 7

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })

        //포유류 추가
        btn_mammalia.setOnClickListener(View.OnClickListener {


            buttonController = 8

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })

        //어류 추가
        btn_fish.setOnClickListener(View.OnClickListener {

            buttonController = 9

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })

        //곤충 추가
        btn_insect.setOnClickListener(View.OnClickListener {

            buttonController = 10

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })

        //식물 추가
        btn_flora.setOnClickListener(View.OnClickListener {

            buttonController = 11

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })


        //저서무척추동물 추가
        btn_zoobenthos.setOnClickListener(View.OnClickListener {


            buttonController = 13

            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })


        btn_clear_all.setOnClickListener(View.OnClickListener {
            googleMap.clear()
        })

        //좌표지정 버튼
        btn_gps_select.setOnClickListener {


            val intent:Intent = Intent(this, hntecology.ecology.activities.Dlg_gps::class.java);



            startActivityForResult(intent, dlg_gpsCallbackData);
        }

        btn_satellite.setOnClickListener {


           var satelite:String = btn_satellite.text.toString();

            if(satelite == "위성 지도"){

                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID;
                btn_satellite.text = "일반 지도"
            }else{

                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL;
                btn_satellite.text = "위성 지도"
            }
        }

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        startRegistrationService()

        TVtimeTV.setText(getTime())

        logoutBtn.setOnClickListener{

            var builder:AlertDialog.Builder =  AlertDialog.Builder(context);
            builder.setMessage("로그아웃 하시겠습니까?");
            builder.setCancelable(true);
            builder.setNegativeButton("취소", DialogInterface.OnClickListener{ dialogInterface, i ->
                dialogInterface.cancel();

            });
            builder.setPositiveButton("확인", DialogInterface.OnClickListener{ dialogInterface, i ->
                dialogInterface.cancel();

                PrefUtils.clear(context);

                val intent:Intent = Intent(context, LoginActivity::class.java);
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK;
                startActivity(intent);

            })
            builder.show();

        }

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
                dlg_gpsCallbackData ->{

                    latitude = data!!.getDoubleExtra("latitude",126.79235)
                    longitude = data!!.getDoubleExtra("longitude",37.39627)


                    val gpsSet:GpsSet = GpsSet(null,latitude,longitude)
                    dbManager!!.insertGpsSet(gpsSet);
                    onMapReady(googleMap)
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

            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit()

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

        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.

        val sydney = LatLng(longitude, latitude)

        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))



        // 마커클릭 이벤트 처리
        // GoogleMap 에 마커클릭 이벤트 설정 가능.
        googleMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener {
            marker ->
            Toast.makeText(this,marker.title,Toast.LENGTH_LONG).show()

            false
        })



    }

    private var parsed: Boolean = false

    override fun onCameraIdle() {

        if(parsed) {
            return
        }

        parsed = true
    }

    // android.os.AsyncTask<Params, Progress, Result>
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

            println("num : " + geometryCollection.numGeometries)

            // val available = ArrayList<Geometry>()
            for (i in 0..(geometryCollection.numGeometries - 1)) {
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

                    Thread.sleep(20)
                }
            }

            return true
        }

        override fun onProgressUpdate(vararg polygonOptions: PolygonOptions?) {
            val polygon = googleMap.addPolygon(polygonOptions[0])
            polygon.setTag(layerName)
        }

        override fun onPostExecute(result: Boolean?) {
            Utils.hideLoading(context)
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

        val firstGeoPoint = googleMap.getProjection().fromScreenLocation(point)

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
            }

            MotionEvent.ACTION_MOVE -> if (drawer_view.visibility == View.VISIBLE) {
                X1 = event.x.toInt()
                Y1 = event.y.toInt()
                point = Point()
                point.x = X1
                point.y = Y1
                val geoPoint = googleMap.getProjection().fromScreenLocation(point)

                println("geoPoint : $geoPoint")

                latlngs.add(geoPoint)
                val mPolylineOptions = PolylineOptions()
                mPolylineOptions.color(Color.RED)
                mPolylineOptions.width(3.0f)
                mPolylineOptions.addAll(latlngs)


                googleMap.addPolyline(mPolylineOptions)
            }
            MotionEvent.ACTION_UP -> {

                print("Poinnts array size : ${latlngs.size}")

                latlngs.add(firstGeoPoint)

//                val polygonOptions = PolygonOptions()
//                polygonOptions.fillColor(Color.RED)
//                polygonOptions.strokeColor(Color.TRANSPARENT)
//                polygonOptions.addAll(latlngs)
//                googleMap.addPolygon(polygonOptions)
                //여기서 부터 작업 시작.

                val polygonOptions = PolygonOptions()
                polygonOptions.fillColor(Color.RED)
                polygonOptions.strokeColor(Color.TRANSPARENT)
                polygonOptions.addAll(latlngs)


                var polygonNew:Polygon = googleMap.addPolygon(polygonOptions)
                polygonNew.setClickable(true);

                //클릭시 태그 데이터 있는지 확인 없으면 바로 넘기고 있으면 있는걸로 호출.
                //tag 리절트로 가져와서 태그 설정
                googleMap.setOnPolygonClickListener(GoogleMap.OnPolygonClickListener { polygon ->

                    var tagName = getAttrubuteKey();
                    var intent:Intent? = null
                    when(buttonController){

                        3->{
                            tagName += "biotope"
                            intent = Intent(this, BiotopeActivity::class.java);

                        }
                        6->{
                            tagName += "birds"
                            intent = Intent(this, BirdsActivity::class.java);
                        }

                        7->{
                            tagName += "reptilia"
                            intent = Intent(this, ReptiliaActivity::class.java);

                        }
                        8->{

                            tagName += "mammalia"
                            intent = Intent(this, MammaliaActivity::class.java);

                        }

                        9->{

                            tagName += "fish"
                            intent = Intent(this, FishActivity::class.java);
                        }

                        10->{
                            tagName += "insect"
                            intent = Intent(this, InsectActivity::class.java);

                        }

                        11 ->{

                            tagName += "flora"
                            intent = Intent(this, FloraActivity::class.java);

                        }

                        13 ->{
                            tagName += "zoobenthos"
                            intent = Intent(this, ZoobenthosActivity::class.java);

                        }
                    }




                    polygon.tag = tagName;

                    intent!!.putExtra("id",polygon.tag.toString());


                    startActivityForResult(intent, PolygonCallBackData);


                })




                runOnUiThread {
                    endDraw()
                }

            }
        }
        return mGestureDetector.onTouchEvent(event)
    }

    fun startDraw() {
        drawer_view.visibility = View.VISIBLE

        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.setAllGesturesEnabled(false)


        when(buttonController){

            3->{
                btn_biotope.text = "비오톱 추가 중"
            }
            6->{
                btn_birds.text = "조류 추가 중"
            }

            7->{

                btn_Reptilia.text = "양서ㆍ파충류 추가 중"
            }
            8->{
                btn_mammalia.text = "포유류 추가 중"
            }

            9->{

                btn_fish.text = "어류 추가 중"
            }

            10->{

                btn_insect.text = "곤충 추가 중"
            }

            11 ->{

                btn_flora.text = "식물상 추가 중"
            }

            13 ->{

                btn_zoobenthos.text = "저서무척추동물 추가 중"
            }

        }
    }

    fun endDraw() {

        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.uiSettings.isRotateGesturesEnabled = false

        latlngs.clear()

        drawer_view.visibility = View.GONE
        when(buttonController){

            3->{
                btn_biotope.text = "비오톱 추가"
            }
            6->{
                btn_birds.text = "조류 추가"
            }

            7->{

                btn_Reptilia.text = "양서ㆍ파충류 추가"
            }
            8->{
                btn_mammalia.text = "포유류 추가"
            }

            9->{

                btn_fish.text = "어류 추가"
            }

            10->{

                btn_insect.text = "곤충 추가"
            }

            11 ->{

                btn_flora.text = "식물상 추가"
            }

            13 ->{

                btn_zoobenthos.text = "저서무척추동물 추가"
            }

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

}
