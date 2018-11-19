package hntecology.ecology.activities

import android.os.Environment
import com.google.android.gms.maps.model.Polygon
import org.gdal.ogr.Feature
import org.gdal.ogr.FieldDefn
import org.gdal.ogr.Geometry
import org.gdal.ogr.ogr
import org.gdal.osr.SpatialReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object Exporter {

    class ExportItem constructor(layerInt:Int, columnDefs: ArrayList<ColumnDef>, polygon : Polygon) {
        val layerInt:Int = layerInt
        var columnDefs: ArrayList<ColumnDef> = columnDefs
        var polygon : Polygon = polygon
    }

    class ColumnDef constructor(columnName: String, columnType: Int, columnValue: Any?) {
        val columnName = columnName
        val columnType = columnType
        val columnValue:Any? = columnValue
    }

    fun export(exportItem:ExportItem) {

        ogr.RegisterAll()

        val GetDriverCount = ogr.GetDriverCount()

        println("GetDriverCount : $GetDriverCount")

        var layerName = ""

        when(exportItem.layerInt) {
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
        }

        // set up the shapefile driver
        val driver = ogr.GetDriverByName("ESRI Shapefile")

        // String outPath = "/data/data/com.wshunli.gdal.android.demo/outputs/";
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + timeStamp + File.separator
        val outPathFile = "$outPath$layerName.shp"




        println("outPathFile : $outPathFile")

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
        var layerType = ogr.wkbPoint
        if(exportItem.layerInt == MainActivity.LAYER_BIOTOPE) {
            layerType = ogr.wkbPolygon
        }
        var layer = data_source!!.CreateLayer("volcanoes", srs, layerType)

        println("layer : " + layer!!)

        // column names
        for (columnDef in exportItem.columnDefs) {
            layer.CreateField(FieldDefn(columnDef.columnName, columnDef.columnType))
        }

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

        // Dereference the feature
        // feature = null

        layer = null

        // Save and close the data source
        data_source.delete()

    }

}