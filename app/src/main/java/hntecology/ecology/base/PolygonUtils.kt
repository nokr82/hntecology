package hntecology.ecology.base

import android.content.Context
import android.graphics.*
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlin.math.roundToInt

object PolygonUtils {

    fun drawTextOnPolygon(context:Context, label:String, polygonOptions: PolygonOptions, map:GoogleMap): Marker {
        val scale = context.resources.displayMetrics.density
        val bounds1 = Rect()
        val paint1 = Paint()
        paint1.color = Color.WHITE

        Log.d("폴리건",polygonOptions.toString())
        // text size in pixels
        paint1.textSize = (14 * scale).roundToInt().toFloat()
        paint1.getTextBounds(label, 0, label.length, bounds1)
        val conf = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(bounds1.width()+10, bounds1.height()+5, conf)

        val x = 0.toFloat()
        val y = bounds1.height().toFloat()

        val canvas = Canvas(bitmap)

        canvas.drawText(label.replace("-1",""), x, y, paint1)

        val markerOptions = MarkerOptions()
                .position(PolygonUtils.getPolygonCenterPoint(polygonOptions.points))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .anchor(0.5f, 1f)

        return map.addMarker(markerOptions)

    }

    fun getPolygonCenterPoint(polygonPointsList: List<LatLng>): LatLng {
        var centerLatLng: LatLng? = null
        val builder = LatLngBounds.Builder()
        for (i in 0 until polygonPointsList.size) {
            builder.include(polygonPointsList[i])
        }
        val bounds = builder.build()
        centerLatLng = bounds.center

        return centerLatLng
    }
}