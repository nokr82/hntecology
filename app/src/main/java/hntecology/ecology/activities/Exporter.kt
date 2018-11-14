package hntecology.ecology.activities

import android.os.Environment
import org.gdal.ogr.Feature
import org.gdal.ogr.FieldDefn
import org.gdal.ogr.ogr
import org.gdal.osr.SpatialReference
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*


object Exporter {

    class ColumnDef constructor(columnName: String, columnType: Int) {
        val columnName = ""
        val columnType = -1
    }

    class ColumnValue constructor(columnName: String, columnValue: Any?) {
        val columnName = ""
        val columnValue:Any? = null
    }

    fun export(layerInt:Int, columnDefs: Array<ColumnDef>, columnValues: Array<ColumnValue>) {

        ogr.RegisterAll()

        val GetDriverCount = ogr.GetDriverCount()

        println("GetDriverCount : $GetDriverCount")

        // set up the shapefile driver
        val driver = ogr.GetDriverByName("ESRI Shapefile")

        // String outPath = "/data/data/com.wshunli.gdal.android.demo/outputs/";
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + timeStamp + File.separator
        val outPathFile = "$outPath$layerInt.shp"




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
        var layer = data_source!!.CreateLayer("volcanoes", srs, ogr.wkbPoint)

        println("layer : " + layer!!)

        // column names
        for (columnDef in columnDefs) {
            layer.CreateField(FieldDefn(columnDef.columnName, columnDef.columnType))
        }

        // create the feature
        var feature: Feature? = Feature(layer.GetLayerDefn())

        // Set the attributes using the values from the delimited text file

        for(columnValue in columnValues) {
            if(columnValue.columnValue is Double) {
                feature!!.SetField(columnValue.columnName, columnValue.columnValue)
            } else if(columnValue.columnValue is Int) {
                feature!!.SetField(columnValue.columnName, columnValue.columnValue)
            } else if(columnValue.columnValue is String) {
                feature!!.SetField(columnValue.columnName, columnValue.columnValue)
            }
        }

        var euc_kr: String? = null
        try {
            euc_kr = URLEncoder.encode("핫", "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }


        /*
        try {
            // euc_kr = new String("핫".getBytes("utf-8"), "euc-kr");
            // euc_kr = new String(euc_kr.getBytes("utf-8"), "euc-kr");
            // euc_kr = new String(euc_kr.getBytes("euc-kr"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        */
        feature!!.SetField("Elevation", euc_kr)

        // create the WKT for the feature using Python string formatting
        val wkt = "POINT(126.7545308 37.4889683)"

        // Create the point from the Well Known Txt
        val point = ogr.CreateGeometryFromWkt(wkt)

        println("point : " + point!!.ExportToWkt())

        // Set the feature geometry using the point
        feature.SetGeometry(point)
        // Create the feature in the layer (shapefile)
        val created = layer.CreateFeature(feature)

        println("created : $created")

        layer.CommitTransaction()

        // Dereference the feature
        feature = null

        layer = null

        // Save and close the data source
        data_source.delete()

    }

}