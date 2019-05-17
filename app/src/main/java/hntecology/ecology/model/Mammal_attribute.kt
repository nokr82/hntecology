package hntecology.ecology.model

import java.io.Serializable

class Mammal_attribute (
        var id : String?, var GROP_ID: String? , var PRJ_NAME: String? , var INV_REGION: String? , var INV_DT: String?
        , var INV_PERSON: String? , var WEATHER: String? , var WIND: String? , var WIND_DIRE: String?,var TEMPERATUR: Float?
        , var ETC: String? , var NUM: Int? , var INV_TM: String? , var SPEC_NM: String? , var FAMI_NM: String?
        , var SCIEN_NM: String? , var ENDANGERED: String? , var OBS_TY: String?  , var INDI_CNT: Int?
        ,var OB_PT_CHAR: String? ,  var UNUS_NOTE: String? ,var STANDARD:String?, var GPS_LAT: Float?, var GPS_LON: Float?
        , var UN_SPEC: String? , var UN_SPEC_RE: String? , var TR_EASY: String? , var TR_EASY_RE: String?,var TEMP_YN:String?
        , var CONF_MOD:String?,var GEOM:String?,var GPSLAT_DEG:Int?,var GPSLAT_MIN:Int?,var GPSLAT_SEC:Float?
        ,var GPSLON_DEG:Int?,var GPSLON_MIN:Int?,var GPSLON_SEC:Float?,var MJ_ACT_PR:String?
        , var MAC_ADDR: String?,var CURRENT_TM:String?
): Serializable {
}
