package hntecology.ecology.model

import java.io.Serializable

class Flora_Attribute (
        var id : String?, var GROP_ID: String? , var PRJ_NAME: String? , var INV_REGION: String? , var INV_DT: String? , var INV_PERSON: String? , var WEATHER: String? , var WIND: String? , var WIND_DIRE: String?
        ,var TEMPERATUR: Float? , var ETC: String? , var NUM: Int? , var INV_TM: String? , var SPEC_NM: String? , var FAMI_NM: String? , var SCIEN_NM: String? , var FLORE_YN: String? , var PLANT_YN: String? , var HAB_STAT: String?
        ,var HAB_ETC: String? ,  var COL_IN_CNT: Int? , var THRE_CAU: String? , var GPS_LAT: Float?, var GPS_LON: Float?,var TEMP_YN:String?
): Serializable {
}
