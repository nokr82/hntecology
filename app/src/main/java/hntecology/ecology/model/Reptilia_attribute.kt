package hntecology.ecology.model

import java.io.Serializable

class Reptilia_attribute (
        var id : String?, var GROP_ID: String? , var PRJ_NAME: String? , var INV_REGION: String? , var INV_DT: String? , var INV_PERSON: String? , var WEATHER: String? , var WIND: String? , var WIND_DIRE: String?
        ,var TEMPERATUR: Float? , var ETC: String? , var NUM: Int? , var INV_TM: String? , var SPEC_NM: String? , var FAMI_NM: String? , var SCIEN_NM: String? , var ENDANGERED: String?, var IN_CNT_ADU: Int? , var IN_CNT_LAR: Int? , var IN_CNT_EGG: Int?
        ,var HAB_RIVEER: String? ,  var HAB_EDGE: String? , var WATER_IN: String?, var WATER_OUT: String? , var WATER_CONT: String? , var WATER_QUAL: String? , var WATER_DEPT: Int?
        ,var HAB_AREA_W: Int? , var HAB_AREA_H: Int?, var GPS_LAT: Float?, var GPS_LON: Float?,var TEMP_YN:String? , var CONF_MOD:String?,var GEOM:String?
        ,var GPSLAT_DEG:Int?,var GPSLAT_MIN:Int?,var GPSLAT_SEC:Float?,var GPSLON_DEG:Int?,var GPSLON_MIN:Int?,var GPSLON_SEC:Float?,var HAB_AREA:Float?
        , var MAC_ADDR:String?,var CURRENT_TM:String?, var ETC_UNUS: String?
): Serializable {
}
