package hntecology.ecology.model

import java.io.Serializable

class Flora_Attribute (
        var id : String?, var GROP_ID: String? , var PRJ_NAME: String? , var INV_REGION: String? , var INV_DT: String?
        , var INV_PERSON: String? , var WEATHER: String?, var WIND: String? , var WIND_DIRE: String?,var TEMPERATUR: Float?
        , var ETC: String? , var NUM: Int? , var INV_TM: String? , var SPEC_NM: String? , var FAMI_NM: String?
        , var SCIEN_NM: String? , var FLORE_YN: String? , var PLANT_YN: String? , var HAB_STAT: String?,var HAB_ETC: String?
        ,  var COL_IN_CNT: Int? , var THRE_CAU: String? , var GPS_LAT: Float?, var GPS_LON: Float?,var TEMP_YN:String?
        ,var CONF_MOD:String?,var GEOM:String?,var GPSLAT_DEG:Int?,var GPSLAT_MIN:Int?,var GPSLAT_SEC:Float?
        ,var GPSLON_DEG:Int?,var GPSLON_MIN:Int?,var GPSLON_SEC:Float?, var MAC_ADDR: String? , var CURRENT_TM: String?
        , var ETC_UNUS: String?

): Serializable {
}
