package hntecology.ecology

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_main.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile


class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private lateinit var context: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this

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

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        val mapFragment = SupportMapFragment.newInstance()
        mapFragment.getMapAsync(this)

        getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit()
    }

    private lateinit var googleMap: GoogleMap

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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14f))


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

            val inputStream = assets.open("SH_biotop_orig.shp")

            val shapeReader = Shapefile(inputStream)

            val geometryCollection = shapeReader.read(geometryFactory)

            println("num : " + geometryCollection.numGeometries)

            val available = ArrayList<Geometry>()
            for (i in 0..(geometryCollection.numGeometries - 1)) {
                val geometry = geometryCollection.getGeometryN(i)

                if(!geometry.intersects(mapBoundary)) {
                    continue
                }

                if (geometry.geometryType.equals("Polygon")) {
                    val polygonOptions = PolygonOptions()
                    polygonOptions.fillColor(Color.parseColor("#85b66f"))
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
            googleMap.addPolygon(polygonOptions[0])
        }

        override fun onPostExecute(result: Boolean?) {
            Utils.hideLoading(context)
        }
    }


}
