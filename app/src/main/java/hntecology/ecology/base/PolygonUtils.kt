package hntecology.ecology.base

import android.content.Context
import android.graphics.*
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlin.math.roundToInt

object PolygonUtils {

    fun drawTextOnPolygon(context:Context, label:String, polygonOptions: PolygonOptions, map:GoogleMap): Marker {

        val labels = label.split("\n")

        val scale = context.resources.displayMetrics.density
        val bounds1 = Rect()
        val bounds2 = Rect()

        val paint1 = Paint()
        val paint2 = Paint()

        paint1.color = Color.BLACK
        paint2.color = Color.BLACK

        Log.d("폴리건",polygonOptions.toString())

        // text size in pixels
        paint1.textSize = (14 * scale).roundToInt().toFloat()
        paint2.textSize = (14 * scale).roundToInt().toFloat()

        if(labels.count() > 0) {
            paint1.getTextBounds(labels[0], 0, labels[0].length, bounds1)
        }

        if(labels.count() > 1) {
            paint2.getTextBounds(labels[1], 0, labels[1].length, bounds2)
        }

        val conf = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(Math.max(bounds1.width(), bounds2.width()) + 10, bounds1.height() + bounds2.height() + 5, conf)

        val x = 0.toFloat()
        val y = bounds1.height().toFloat()

        val canvas = Canvas(bitmap)

        canvas.drawText(labels[0].replace("-1",""), x, y, paint1)

        if(labels.count() > 1) {
            canvas.drawText(labels[1].replace("-1", ""), x, y + bounds2.height(), paint2)
        }

        val markerOptions = MarkerOptions()
                .position(PolygonUtils.getPolygonCenterPoint(polygonOptions.points))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .anchor(0.5f, 1f)

        val marker = map.addMarker(markerOptions)

        return marker
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