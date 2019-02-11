package hntecology.ecology.model

import java.io.Serializable

class Waypoint (
        var id: Int? , var GROP_ID: String? , var INV_REGION: String? , var INV_DT: String?, var INV_TM: String? ,var NUM:String? , var INV_PERSON: String?,var PRJ_NAME:String?,var GPS_LAT:Float?,var GPS_LON:Float?
        ,var MEMO:String?,var GEOM:String?
): Serializable {
}