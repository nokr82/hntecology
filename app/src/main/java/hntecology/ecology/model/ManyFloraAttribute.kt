package hntecology.ecology.model

import java.io.Serializable

class ManyFloraAttribute (
        var id : String?, var GROP_ID: String?  , var INV_REGION: String? , var INV_PERSON: String?  , var INV_DT: String? , var INV_TM: String? , var TRE_NUM: Int? , var TRE_SPEC: String?,
        var TRE_FAMI: String? , var TRE_SCIEN: String? , var TRE_DBH: Float?, var TRE_TOIL: Float? , var TRE_UNDER: String? ,var TRE_WATER: Int? ,var TRE_TYPE: String?
        , var STRE_NUM: Int?,var STRE_SPEC: String? , var STRE_FAMI: String?, var STRE_SCIEN: String? , var STRE_DBH: Float?, var STRE_TOIL: Float?, var STRE_UNDER: String?, var STRE_WATER: Int?
        ,var STRE_TYPE: String? , var SHR_NUM: Int?, var SHR_SPEC: String? , var SHR_FAMI: String? , var SHR_SCIEN: String? , var SHR_TOIL: Float? ,var SHR_WATER: Float?  ,var SHR_UNDER: String?
        , var HER_NUM: Int?,var HER_SPEC: String? ,  var HER_FAMI: String? , var HER_SCIEN: String?, var HER_DOMIN: Float? , var HER_GUNDO: Float?  , var HER_HEIGHT: Float?
        , var GPS_LAT: Float?, var GPS_LON: Float?, var TEMP_YN: String?, var CONF_MOD: String?,var GEOM:String?,var DOMIN:String? , var MAC_ADDR: String?, var CURRENT_TM: String?

): Serializable {
}
