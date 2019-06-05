package hntecology.ecology.model

import android.view.View
import android.widget.Toast
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_flora2.*
import java.io.Serializable

class TreeData1 (
        var dataPk: Int? ,  var PAGE: Int? , var SPEC: String? , var FAMI: String?, var SCIEN: String? ,var M_DBH:Float?,var DBH:Float?
        ,var X_DBH:Float? ,var M_TOIL:Float? , var TOIL: Float? ,var X_TOIL:Float?,var M_UNDER:Float?,var UNDER: Float?
        ,var X_UNDER:Float?,var M_WATER:Float?,var WATERWIDTH :Float?,var X_WATER:Float?,var TYPE :String?
): Serializable {
}
