package hntecology.ecology

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
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
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_main.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile

class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, View.OnTouchListener {

    private val PLAY_SERVICES_RESOLUTION_REQUEST: Int = 1000

    private lateinit var context: MainActivity

    private lateinit var mGestureDetector: GestureDetector
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this

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

        btn_draw.setOnClickListener(View.OnClickListener {
            if(drawer_view.visibility == View.VISIBLE) {
                endDraw()
            } else {
                startDraw()
            }
        })

        btn_clear_all.setOnClickListener(View.OnClickListener {
            googleMap.clear()
        })

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        startRegistrationService()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            PLAY_SERVICES_RESOLUTION_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                initilizeMap()
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
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

        print("onMapReady onMapReady")

        googleMap = map

        googleMap.uiSettings.isRotateGesturesEnabled = false

        googleMap.setOnCameraIdleListener(this)

        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        val sydney = LatLng(37.39627, 126.79235)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))


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

                val polygonOptions = PolygonOptions()
                polygonOptions.fillColor(Color.RED)
                polygonOptions.strokeColor(Color.TRANSPARENT)
                polygonOptions.addAll(latlngs)
                googleMap.addPolygon(polygonOptions)

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

        btn_draw.text = "Stop drawing"
    }

    fun endDraw() {

        runOnUiThread(Runnable {
            drawer_view.visibility == View.GONE
        })

        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.uiSettings.isRotateGesturesEnabled = false

        latlngs.clear()

        drawer_view.visibility = View.GONE

        btn_draw.text = "Start Draw"
    }
}
