package hntecology.ecology.model

import java.io.Serializable

class Zoobenthos_Attribute (
     var id : String?, var GROP_ID: String?, var PRJ_NAME:String?, var INV_REGION:String?, var INV_MEAN:String?
     , var INV_PERSON:String?, var MAP_SYS_NM:String?,var GPSLAT_DEG:Int?,var GPSLAT_MIN:Int?,var GPSLAT_SEC:Float?
     ,var GPSLON_DEG:Int?,var GPSLON_MIN:Int?,var GPSLON_SEC:Float?, var INV_DT:String? , var NUM:String?
     , var INV_TM:String?, var WEATHER:String?, var INV_TOOL:String?, var AD_DIST_NM:String?, var RIV_W:String?,
     var RIV_W2:Int?, var RUN_RIV_W:String?, var RUN_RIV_W2:Int?, var WATER_DEPT:Int?, var HAB_TY:String?
    , var FILT_AREA:String?, var TEMPERATUR:Float?, var WATER_TEM:Float?, var TURBIDITY:String?, var MUD:Float?
     , var SAND:Float? , var COR_SAND:Float?, var GRAVEL:Float?, var STONE_S:Float?, var STONE_B:Float?
     , var CONCRETE:Float?, var BED_ROCK:Float?, var BANK_L:String?, var BANK_R:String?, var BAS_L:String?
     , var BAS_R:String?, var DIST_CAU:String?, var UNUS_NOTE:String?, var GPS_LAT:Float?, var GPS_LON:Float?
     ,var SPEC_NM:String?, var FAMI_NM:String?, var SCIEN_NM:String?, var TEMP_YN:String?, var CONF_MOD:String?
     ,var GEOM:String?,var ZOO_CNT:Int?

): Serializable {
}
