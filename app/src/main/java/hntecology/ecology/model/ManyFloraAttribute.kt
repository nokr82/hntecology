package hntecology.ecology.model

import java.io.Serializable

class ManyFloraAttribute (
        var id : String?, var GROP_ID: String?  , var INV_REGION: String? , var INV_PERSON: String?  , var INV_DT: String? , var INV_TM: String? , var TRE_NUM: Int? , var TRE_SPEC: String?,
        var TRE_FAMI: String? , var TRE_SCIEN: String? , var TRE_H: Float?, var TRE_BREA: Float? , var TRE_COVE: Float? , var STRE_NUM: Int?,var STRE_SPEC: String? , var STRE_FAMI: String? , var STRE_SCIEN: String? , var STRE_H: Float?
        ,var STRE_BREA: Float? ,  var STRE_COVE: Float? , var SHR_NUM: Int?, var SHR_SPEC: String? , var SHR_FAMI: String? , var SHR_SCIEN: String? , var SHR_H: Float? ,var SHR_COVE: Float? , var HER_NUM: Int?
        ,var HER_SPEC: String? ,  var HER_FAMI: String? , var HER_SCIEN: String?, var HER_H: Float? , var HER_COVE: Float? , var GPS_LAT: Float?, var GPS_LON: Float?, var TEMP_YN: String?, var CONF_MOD: String?
): Serializable {
}
