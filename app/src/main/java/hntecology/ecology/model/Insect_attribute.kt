package hntecology.ecology.model

import java.io.Serializable

class Insect_attribute (
        var id : String?, var GROP_ID: String? , var PRJ_NAME: String? , var INV_REGION: String? , var INV_DT: String? , var INV_PERSON: String? , var WEATHER: String? , var WIND: String? , var WIND_DIRE: String?
        ,var TEMPERATUR: Float? , var ETC: String? , var NUM: Int? , var INV_TM: String? , var SPEC_NM: String? , var FAMI_NM: String? , var SCIEN_NM: String? , var INDI_CNT: Int? , var OBS_STAT: String? , var OBS_ST_ETC: String?
        ,var USE_TAR: String? ,  var USER_TA_ETC: String? , var MJ_ACT: String?, var MJ_ACT_ETC: String? , var INV_MEAN: String? , var INV_MN_ETC: String? , var UNUS_NOTE: String? , var GPS_LAT: Float?, var GPS_LON: Float?
        ,var TEMP_YN:String? ,var CONF_MOD:String?,var GEOM:String?,var GPSLAT_DEG:Int?,var GPSLAT_MIN:Int?,var GPSLAT_SEC:Float?,var GPSLON_DEG:Int?,var GPSLON_MIN:Int?,var GPSLON_SEC:Float?
): Serializable {
}
