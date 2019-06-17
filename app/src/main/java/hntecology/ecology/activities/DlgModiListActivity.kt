package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgModiAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Biotope_attribute
import java.io.IOException
import java.util.*


class DlgModiListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView
    var u_name = ""
    private lateinit var listdata1: ArrayList<Biotope_attribute>
    private lateinit var listdata2: ArrayList<Biotope_attribute>
    private lateinit var listdata3: ArrayList<Biotope_attribute>

    private lateinit var listAdapte1: DlgModiAdapter;

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var DlgHeight: Float = 430F
    var dbManager: DataBaseHelper? = null
    var chkData = false
    var polygonid = ""
    var grop_id = ""
    var geom = ""
    var ufid = ""
    var lat: String = ""
    var log: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_modi_list)

        context = applicationContext;

        dbManager = DataBaseHelper(this)
        u_name = PrefUtils.getStringPreference(context, "name");
        val db = dbManager!!.createDataBase();

        val intent = getIntent()

        DlgHeight = intent.getFloatExtra("DlgHeight", 430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            println("==============$lat")
        }
        if (intent.getStringExtra("ufid") != null) {
            ufid = intent.getStringExtra("ufid")
            println("==============아이디$ufid")
        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            println("==============$log")
        }


        grop_id = intent.getStringExtra("GROP_ID")
        if (intent.getStringExtra("polygonid") != null) {
            polygonid = intent.getStringExtra("polygonid")

            println("폴리아이디 ---------$polygonid")
        }
        if (intent.getStringExtra("geom") != null) {
            geom = intent.getStringExtra("geom")
            println("---------점스스스 $geom")
        }

        val dataList: Array<String> = arrayOf("biotopeAttribute.*", "min(id) as minId");
        val dataList2: Array<String> = arrayOf("*");
        val data1 = db.query("biotopeAttribute", dataList, null, null, "GROP_ID", null, "minId asc", null);
        val data2 = db.query("biotopeAttribute", dataList2, null, null, null, null, "id asc", null);


        listView1 = findViewById(R.id.modiLV)

        listdata3 = ArrayList()
        listdata2 = ArrayList()
        listdata1 = ArrayList()

        dataList(listdata1, data1)
        dataList(listdata2, data2)
        listAdapte1 = DlgModiAdapter(context, listdata1, listdata2);

        listView1.adapter = listAdapte1


        listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var attribute = null_biotope_attribute()
            var bio_data = listAdapte1.getItem(position)
            val data = db.query("biotopeAttribute", dataList2, "GROP_ID='${bio_data.GROP_ID}'", null, null, null, "id asc", null);
            dataList(listdata3, data)
            dbManager!!.deletegrop_biotope(grop_id)
            Log.d("버드데이터", listdata3.size.toString())
            Log.d("버드데이터", bio_data.GROP_ID.toString())

            for (i in 0..listdata3.size - 1) {
                addbiotope(attribute ,listdata3[i].INV_INDEX.toString(), listdata3[i].LU_GR_NUM.toString(), listdata3[i].LU_TY_RATE.toString()
                        , listdata3[i].STAND_H.toString(), listdata3[i].LC_GR_NUM.toString(), listdata3[i].LC_TY.toString()
                        , listdata3[i].TY_MARK.toString(), listdata3[i].GV_RATE.toString()
                        , listdata3[i].GV_STRUCT.toString(), listdata3[i].DIS_RET.toString()
                        , listdata3[i].RESTOR_POT.toString(), listdata3[i].COMP_INTA.toString(), listdata3[i].VP_INTA.toString(),
                        listdata3[i].IMP_FORM.toString(), listdata3[i].BREA_DIA.toString()
                        , listdata3[i].FIN_EST.toString(), listdata3[i].TRE_SPEC.toString(), listdata3[i].TRE_FAMI.toString()
                        , listdata3[i].TRE_SCIEN.toString(), listdata3[i].TRE_H.toString(), listdata3[i].TRE_BREA.toString()
                        , listdata3[i].TRE_COVE.toString(), listdata3[i].STRE_SPEC.toString()
                        , listdata3[i].STRE_FAMI.toString(), listdata3[i].STRE_SCIEN.toString(), listdata3[i].STRE_H.toString()
                        , listdata3[i].STRE_BRT.toString(), listdata3[i].STRE_COVE.toString()
                        , listdata3[i].SHR_SPEC.toString(), listdata3[i].SHR_FAMI.toString(), listdata3[i].SHR_SCIEN.toString()
                        , listdata3[i].SHR_H.toString(), listdata3[i].STR_COVE.toString()
                        , listdata3[i].HER_SPEC.toString(), listdata3[i].HER_FAMI.toString(), listdata3[i].HER_SCIEN.toString()
                        , listdata3[i].HER_H.toString(), listdata3[i].HER_COVE.toString(),
                        listdata3[i].PIC_FOLDER.toString(), listdata3[i].WILD_ANI.toString(), listdata3[i].BIOTOP_POT.toString()
                        , listdata3[i].UNUS_NOTE.toString(), listdata3[i].GPS_LAT.toString()
                        , listdata3[i].GPS_LON.toString(), listdata3[i].NEED_CONF.toString(), listdata3[i].CONF_MOD.toString()
                        , listdata3[i].TEMP_YN.toString(), listdata3[i].LANDUSE.toString()
                         , listdata3[i].CHECK.toString(), listdata3[i].TRE_H_N.toString(),
                        listdata3[i].TRE_H_X.toString()
                        , listdata3[i].TRE_BREA_N.toString(), listdata3[i].TRE_BREA_X.toString(), listdata3[i].STRE_H_N.toString(), listdata3[i].STRE_H_X.toString()
                        , listdata3[i].STRE_BRT_N.toString(), listdata3[i].STRE_BRT_X.toString(), listdata3[i].SHR_HET_N.toString(), listdata3[i].SHR_HET_X.toString()
                        , listdata3[i].HER_HET_X.toString(), listdata3[i].HER_HET_N.toString(), listdata3[i].BIO_TYPE.toString(), listdata3[i].IMPERV.toString()
                        , listdata3[i].DOMIN.toString(),
                        listdata3[i].TRE_NUM!!.toInt(), listdata3[i].STRE_NUM!!.toInt(), listdata3[i].SHR_NUM!!.toInt(), listdata3[i].HER_NUM!!.toInt())
            }
            last_finish()

        }

    }

    fun addbiotope(biotope_attribute: Biotope_attribute, INV_INDEX: String?, LU_GR_NUM: String?, LU_TY_RATE: String?
                   , STAND_H: String?, LC_GR_NUM: String?, LC_TY: String?, TY_MARK: String?, GV_RATE: String?
                   , GV_STRUCT: String?, DIS_RET: String?, RESTOR_POT: String?, COMP_INTA: String?, VP_INTA: String?,
                   IMP_FORM: String?, BREA_DIA: String?, FIN_EST: String?, TRE_SPEC: String?, TRE_FAMI: String?
                   , TRE_SCIEN: String?, TRE_H: String?, TRE_BREA: String?, TRE_COVE: String?, STRE_SPEC: String?
                   , STRE_FAMI: String?, STRE_SCIEN: String?, STRE_H: String?, STRE_BRT: String?, STRE_COVE: String?
                   , SHR_SPEC: String?, SHR_FAMI: String?, SHR_SCIEN: String?, SHR_H: String?, STR_COVE: String?
                   , HER_SPEC: String?, HER_FAMI: String?, HER_SCIEN: String?, HER_H: String?, HER_COVE: String?,
                   PIC_FOLDER: String?, WILD_ANI: String?, BIOTOP_POT: String?, UNUS_NOTE: String?, GPS_LAT: String?
                   , GPS_LON: String?, NEED_CONF: String?, CONF_MOD: String?, TEMP_YN: String?, LANDUSE: String?
                , CHECK: String?, TRE_H_N: String?, TRE_H_X: String?
                   , TRE_BREA_N: String?, TRE_BREA_X: String?, STRE_H_N: String?, STRE_H_X: String?
                   , STRE_BRT_N: String?, STRE_BRT_X: String?, SHR_HET_N: String?, SHR_HET_X: String?
                   , HER_HET_X: String?, HER_HET_N: String?, BIO_TYPE: String?, IMPERV: String?
                   , DOMIN: String?, TRE_NUM: Int?, STRE_NUM: Int?, SHR_NUM: Int?, HER_NUM: Int?) {
        var region = ""
        try {
            var geocoder: Geocoder = Geocoder(context);

            var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

            if (list.isNotEmpty()) {
                System.out.println("list : " + list);

                region = list.get(0).getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace();
        }
        Log.d("고향",region)
        biotope_attribute.INV_REGION = region
        biotope_attribute.GROP_ID = grop_id
        biotope_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
        biotope_attribute.INV_DT = Utils.todayStr()
        biotope_attribute.INV_TM = Utils.timeStr()
        biotope_attribute.INV_INDEX = INV_INDEX.toString().toInt()
        biotope_attribute.LU_TY_RATE = LU_TY_RATE.toString().toFloat()
        biotope_attribute.LU_GR_NUM = LU_GR_NUM
        biotope_attribute.LC_GR_NUM = LC_GR_NUM
        biotope_attribute.TY_MARK = TY_MARK
        biotope_attribute.STAND_H = STAND_H.toString().toFloat()
        biotope_attribute.BIO_TYPE = BIO_TYPE
        biotope_attribute.IMPERV = IMPERV.toString().toFloat()
        biotope_attribute.GV_RATE = GV_RATE.toString().toFloat()

        biotope_attribute.GV_STRUCT = GV_STRUCT
        biotope_attribute.DIS_RET = DIS_RET
        biotope_attribute.RESTOR_POT = RESTOR_POT
        biotope_attribute.COMP_INTA = COMP_INTA
        biotope_attribute.VP_INTA = VP_INTA
        biotope_attribute.IMP_FORM = IMP_FORM
        biotope_attribute.BREA_DIA = BREA_DIA
        biotope_attribute.FIN_EST = FIN_EST
        biotope_attribute.TRE_SPEC = TRE_SPEC
        biotope_attribute.TRE_FAMI = TRE_FAMI
        biotope_attribute.TRE_SCIEN = TRE_SCIEN
        biotope_attribute.TRE_H = TRE_H.toString().toFloat()
        biotope_attribute.TRE_BREA = TRE_BREA.toString().toFloat()
        biotope_attribute.TRE_COVE = TRE_COVE.toString().toFloat()

        biotope_attribute.STRE_SPEC = STRE_SPEC
        biotope_attribute.STRE_FAMI = STRE_FAMI
        biotope_attribute.STRE_SCIEN = STRE_SCIEN

        biotope_attribute.STRE_H = STRE_H.toString().toFloat()
        biotope_attribute.STRE_BRT = STRE_BRT.toString().toFloat()
        biotope_attribute.STRE_COVE = STRE_COVE.toString().toFloat()
        biotope_attribute.SHR_SPEC = SHR_SPEC

        biotope_attribute.SHR_FAMI = SHR_FAMI
        biotope_attribute.SHR_SCIEN = SHR_SCIEN

        biotope_attribute.SHR_H = SHR_H.toString().toFloat()


        biotope_attribute.STR_COVE = STR_COVE.toString().toFloat()

        biotope_attribute.HER_SPEC = HER_SPEC

        biotope_attribute.HER_FAMI = HER_FAMI
        biotope_attribute.HER_SCIEN = HER_SCIEN


        biotope_attribute.HER_H = HER_H.toString().toFloat()
        biotope_attribute.HER_COVE = HER_COVE.toString().toFloat()
        biotope_attribute.WILD_ANI = WILD_ANI
        biotope_attribute.BIOTOP_POT = BIOTOP_POT
        biotope_attribute.UNUS_NOTE = UNUS_NOTE
        biotope_attribute.LC_TY = LC_TY
        biotope_attribute.GPS_LAT = GPS_LAT.toString().toDouble()
        biotope_attribute.GPS_LON = GPS_LON.toString().toDouble()

        biotope_attribute.CONF_MOD = "N"

        biotope_attribute.LANDUSE = LANDUSE
        biotope_attribute.GEOM = geom
        biotope_attribute.UFID = ufid
        biotope_attribute.CHECK = CHECK

        biotope_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

        biotope_attribute.CURRENT_TM = Utils.current_tm()

        biotope_attribute.TRE_H_N = TRE_H_N.toString().toFloat()
        biotope_attribute.TRE_H_X = TRE_H_X.toString().toFloat()
        biotope_attribute.TRE_BREA_N = TRE_BREA_N.toString().toFloat()

        biotope_attribute.TRE_BREA_X = TRE_BREA_X.toString().toFloat()

        biotope_attribute.STRE_H_N = STRE_H_N.toString().toFloat()

        biotope_attribute.STRE_H_X = STRE_H_X.toString().toFloat()

        biotope_attribute.STRE_BRT_N = STRE_BRT_N.toString().toFloat()

        biotope_attribute.STRE_BRT_X = STRE_BRT_X.toString().toFloat()

        biotope_attribute.SHR_HET_N = SHR_HET_N.toString().toFloat()

        biotope_attribute.SHR_HET_X = SHR_HET_X.toString().toFloat()

        biotope_attribute.HER_HET_N = HER_HET_N.toString().toFloat()

        biotope_attribute.HER_HET_X = HER_HET_X.toString().toFloat()
        biotope_attribute.DOMIN = DOMIN

        biotope_attribute.TRE_NUM= TRE_NUM
        biotope_attribute.STRE_NUM= STRE_NUM
        biotope_attribute.SHR_NUM= SHR_NUM
        biotope_attribute.HER_NUM= HER_NUM

        dbManager!!.insertbiotope_attribute(biotope_attribute);
    }

    fun last_finish() {
        var intent = Intent()
        Log.d("점스", geom)
        intent.putExtra("export", 70);
        intent.putExtra("geom", geom);
        setResult(RESULT_OK, intent);
        Toast.makeText(context, "복사되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun null_biotope_attribute(): Biotope_attribute {
        val biotope_attribute: Biotope_attribute = Biotope_attribute(null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null
        )
        return biotope_attribute
    }

    fun dataList(listdata: ArrayList<Biotope_attribute>, data2: Cursor) {

        while (data2.moveToNext()) {

            var model: Biotope_attribute;

            model = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                    data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                    , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                    , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                    , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                    , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                    , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                    , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53), data2.getString(54), data2.getString(55), data2.getString(56), data2.getString(57)
                    , data2.getFloat(58), data2.getFloat(59), data2.getFloat(60), data2.getFloat(61), data2.getFloat(62), data2.getFloat(63)
                    , data2.getFloat(64), data2.getFloat(65), data2.getFloat(66), data2.getFloat(67), data2.getFloat(68), data2.getFloat(69), data2.getString(70), data2.getFloat(71)
                    , data2.getString(72), data2.getString(73), data2.getString(74), data2.getInt(75), data2.getInt(76), data2.getInt(77), data2.getInt(78)
            )
            if (model.GROP_ID!=grop_id&&model.INV_PERSON ==u_name){
                if (model.DOMIN!="null"||model.TRE_SPEC!=""||model.STRE_SPEC!=""||model.HER_SPEC!=""||model.SHR_SPEC!=""){
                    listdata.add(model)
                }
            }


        }
    }


}
