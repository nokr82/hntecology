package hntecology.ecology.activities

import android.content.Context
import android.os.Environment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import org.gdal.ogr.Feature
import org.gdal.ogr.FieldDefn
import org.gdal.ogr.Geometry
import org.gdal.ogr.ogr
import org.gdal.osr.SpatialReference
import java.io.File
import java.util.*
import kotlin.coroutines.experimental.coroutineContext


object Exporter {

    lateinit var exportItem:ExportItem
    lateinit var exportPointItem:ExportPointItem

    class ExportItem constructor(layerInt:Int, columnDefs: ArrayList<ColumnDef>, polygon : Polygon) {
        val layerInt:Int = layerInt
        var columnDefs: ArrayList<ColumnDef> = columnDefs
        var polygon : Polygon = polygon
    }

    class ExportPointItem constructor(layerInt:Int, columnDefs: ArrayList<ColumnDef>, point : Marker) {
        val layerInt:Int = layerInt
        var columnDefs: ArrayList<ColumnDef> = columnDefs
        var point : Marker = point
    }

    class ColumnDef constructor(columnName: String, columnType: Int, columnValue: Any?) {
        val columnName = columnName
        val columnType = columnType
        val columnValue:Any? = columnValue
    }

    fun export(exportItems:ArrayList<ExportItem>) {
        export(exportItems, null)
    }

    fun exportPoint(exportPointItems:ArrayList<ExportPointItem>) {
        export(null, exportPointItems)
    }

    private fun export(exportItems:ArrayList<ExportItem>?, exportPointItems:ArrayList<ExportPointItem>?) {

        if (exportItems != null) {
            if (exportItems.isEmpty()) {
                return
            }
        }

        if (exportPointItems != null) {
            if (exportPointItems.isEmpty()) {
                return
            }
        }

        ogr.RegisterAll()

        val GetDriverCount = ogr.GetDriverCount()

        println("GetDriverCount : $GetDriverCount")

        var layerName = ""

        if (exportItems != null){
            exportItem = exportItems!!.get(0)

            when (exportItem.layerInt) {
                MainActivity.LAYER_BIOTOPE -> {
                    layerName = "biotope"
                }

                MainActivity.LAYER_BIRDS -> {
                    layerName = "birds"
                }

                MainActivity.LAYER_REPTILIA -> {
                    layerName = "reptilia"
                }

                MainActivity.LAYER_MAMMALIA -> {
                    layerName = "mammalia"
                }

                MainActivity.LAYER_FISH -> {
                    layerName = "fish"
                }

                MainActivity.LAYER_INSECT -> {
                    layerName = "insect"
                }

                MainActivity.LAYER_FLORA -> {
                    layerName = "flora"
                }

                MainActivity.LAYER_ZOOBENTHOS -> {
                    layerName = "zoobenthos"
                }

                MainActivity.LAYER_MYLOCATION -> {
                    layerName = "mylocation"
                }

                MainActivity.TRACKING -> {
                    layerName = "tracking"
                }

                MainActivity.NOTHING -> {
                    layerName = "nothing"
                }

                MainActivity.LAYER_FLORA2 -> {
                    layerName = "flora2"
                }
            }
        }

        if (exportPointItems != null){
            exportPointItem = exportPointItems.get(0)

            when (exportPointItem.layerInt) {
                MainActivity.LAYER_BIOTOPE -> {
                    layerName = "biotope"
                }

                MainActivity.LAYER_BIRDS -> {
                    layerName = "birds"
                }

                MainActivity.LAYER_REPTILIA -> {
                    layerName = "reptilia"
                }

                MainActivity.LAYER_MAMMALIA -> {
                    layerName = "mammalia"
                }

                MainActivity.LAYER_FISH -> {
                    layerName = "fish"
                }

                MainActivity.LAYER_INSECT -> {
                    layerName = "insect"
                }

                MainActivity.LAYER_FLORA -> {
                    layerName = "flora"
                }

                MainActivity.LAYER_ZOOBENTHOS -> {
                    layerName = "zoobenthos"
                }

                MainActivity.LAYER_MYLOCATION -> {
                    layerName = "mylocation"
                }

                MainActivity.TRACKING -> {
                    layerName = "tracking"
                }

                MainActivity.LAYER_FLORA2 -> {
                    layerName = "flora2"
                }

            }

        }

        // set up the shapefile driver
        val driver = ogr.GetDriverByName("ESRI Shapefile")

        // String outPath = "/data/data/com.wshunli.gdal.android.demo/outputs/";
//        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + layerName + File.separator
        var outPathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + layerName + File.separator + layerName + ".shp"

        if(layerName == "tracking"){
            val today = Utils.todayStr()

            val time = Utils.timeStr()

            val path = today + " " + time

            outPathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + layerName + File.separator + path + ".shp"

        }

        val outputsDir = File(outPath)

        if (outputsDir.exists()) {
            println("Exit : $outPath")

            val files = outputsDir.listFiles()
            if (files != null) {
                for (i in files.indices) {
                    println("f : " + files[i])
                }
            }

        } else {
            val made = outputsDir.mkdirs()

            println("made : $made")
        }

        // create the data source
        val data_source = driver!!.CreateDataSource(outPathFile)

        // create the spatial reference, WGS84
        val srs = SpatialReference()
        // srs.ImportFromEPSG(32632);
        // srs.ImportFromProj4("+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");
        srs.ImportFromWkt("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]")

        // create the layer
        var layerType = ogr.wkbPolygon
        if (exportPointItems != null) {
            layerType = ogr.wkbPoint
        }

        if(exportItems != null) {
            val exportitem = exportItems.get(0)
            if (exportitem.layerInt == MainActivity.LAYER_BIOTOPE) {
                layerType = ogr.wkbPolygon
            }
        }

        var layer = data_source!!.CreateLayer("volcanoes", srs, layerType)

        // column names
        if(exportItems != null){
            for (i in 0..exportItems.size-1) {
                val exportitem = exportItems.get(i)
                for (columnDef in exportitem.columnDefs) {
                    layer.CreateField(FieldDefn(columnDef.columnName, columnDef.columnType))
                }
            }
        }

        if(exportPointItems != null){
            for(i in 0..exportPointItems.size-1) {
                val exportPointitem = exportPointItems.get(i)
                for (columnDef in exportPointitem.columnDefs) {
                    layer.CreateField(FieldDefn(columnDef.columnName, columnDef.columnType))
                }
            }
        }

        if(exportItems != null) {
            for (exportItem in exportItems) {
                // create the feature
                var feature: Feature = Feature(layer.GetLayerDefn()) ?: return

                // Set the attributes using the values from the delimited text file
                for(columnDef in exportItem.columnDefs) {
                    if(columnDef.columnValue is Double) {
                        feature.SetField(columnDef.columnName, columnDef.columnValue)
                    } else if(columnDef.columnValue is Int) {
                        feature.SetField(columnDef.columnName, columnDef.columnValue)
                    } else if(columnDef.columnValue is String) {
                        feature.SetField(columnDef.columnName, columnDef.columnValue)
                    }
                }

                // create the WKT for the feature using Python string formatting
                val ring = Geometry(ogr.wkbLinearRing)

                val points = exportItem.polygon.points
                for(point in points) {
                    ring.AddPoint(point.longitude, point.latitude)
                }

                val poly = Geometry(ogr.wkbPolygon)
                poly.AddGeometry(ring)

                // Set the feature geometry using the point
                feature.SetGeometry(poly)

                // Create the feature in the layer (shapefile)
                val created = layer.CreateFeature(feature)
            }
        } else {
            if(exportPointItems != null) {
                for (exportPointItem in exportPointItems) {
                    // create the feature
                    var feature: Feature = Feature(layer.GetLayerDefn()) ?: return

                    // Set the attributes using the values from the delimited text file
                    for(columnDef in exportPointItem.columnDefs) {
                        if(columnDef.columnValue is Double) {
                            feature.SetField(columnDef.columnName, columnDef.columnValue)
                        } else if(columnDef.columnValue is Int) {
                            feature.SetField(columnDef.columnName, columnDef.columnValue)
                        } else if(columnDef.columnValue is String) {
                            feature.SetField(columnDef.columnName, columnDef.columnValue)
                        }
                    }

                    // create the WKT for the feature using Python string formatting
                    val ring = Geometry(ogr.wkbLinearRing)

//                    val points = exportItem.polygon.points
//                    for(point in points) {
//                        ring.AddPoint(point.longitude, point.latitude)
//                    }

                    val point = Geometry(ogr.wkbPoint)
                    point.AddPoint(exportPointItem.point.position.longitude, exportPointItem.point.position.latitude)

                    // Set the feature geometry using the point
                    feature.SetGeometry(point)

                    // Create the feature in the layer (shapefile)
                    val created = layer.CreateFeature(feature)
                }
            }
        }

        // Dereference the feature
        // feature = null

        layer = null

        // Save and close the data source
        data_source.delete()

    }



}