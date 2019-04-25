package hntecology.ecology.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgResearchAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_dlg_research.*
import java.io.File
import java.util.*

class DlgResearchActivity : Activity() {

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    private lateinit var context: Context;

    val LEFTTIME = 1000
    val RIGHTTIME = 1001

    var datas:ArrayList<Research> = ArrayList<Research>()

    private lateinit var adapter: DlgResearchAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_research)

        context = applicationContext;

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        adapter = DlgResearchAdapter(context,datas)

        listviewLV.adapter = adapter

        val intent = getIntent()

        window.setLayout(Utils.dpToPx(1000F).toInt(), Utils.dpToPx(650F).toInt());
        this.setFinishOnTouchOutside(true);

        val today = Utils.todayStr()
        val time = Utils.timeStr()
        val timesplit = time.split(":")

        closeLL.setOnClickListener {
            finish()
        }



        leftdayTV.setText(today + " ")
        lefttimeTV.setText(timesplit.get(0) + "시")
        rightdayTV.setText(today + " ")
        righttimeTV.setText(timesplit.get(0) + "시")

        leftcalendarLL.setOnClickListener {
            val now = Calendar.getInstance()
            var thisAYear = now.get(Calendar.YEAR).toInt()
            var thisAMonth = now.get(Calendar.MONTH).toInt()
            var thisADay = now.get(Calendar.DAY_OF_MONTH).toInt()

            var dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view2, thisYear, thisMonth, thisDay ->
                // Display Selected date in textbox
                thisAMonth = thisMonth + 1
                thisADay = thisDay
                thisAYear = thisYear

                val newDate: Calendar = Calendar.getInstance()
                newDate.set(thisYear, thisMonth, thisDay)

                onDateSet(this,thisAYear,thisAMonth,thisADay,"l")

            }, thisAYear, thisAMonth, thisADay)

            dpd.show()

        }

        rightcalendarLL.setOnClickListener {
            val now = Calendar.getInstance()
            var thisAYear = now.get(Calendar.YEAR).toInt()
            var thisAMonth = now.get(Calendar.MONTH).toInt()
            var thisADay = now.get(Calendar.DAY_OF_MONTH).toInt()

            var dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view2, thisYear, thisMonth, thisDay ->
                // Display Selected date in textbox
                thisAMonth = thisMonth + 1
                thisADay = thisDay
                thisAYear = thisYear

                val newDate: Calendar = Calendar.getInstance()
                newDate.set(thisYear, thisMonth, thisDay)

                onDateSet(this,thisAYear,thisAMonth,thisADay,"r")

            }, thisAYear, thisAMonth, thisADay)

            dpd.show()
        }

        lefttimeTV.setOnClickListener {
            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "시간")
            intent.putExtra("DlgHeight", 500f);
            intent.putExtra("selectDlg", 1000);
            startActivityForResult(intent, LEFTTIME);
        }

        righttimeTV.setOnClickListener {
            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "시간")
            intent.putExtra("DlgHeight", 500f);
            intent.putExtra("selectDlg", 1000);
            startActivityForResult(intent, RIGHTTIME);
        }

        dlgClick.setOnClickListener {
            if (datas != null){
                datas.clear()
            }

            val dataList: Array<String> = arrayOf("*")
            val leftday = leftdayTV.text.toString()
            val lefttime = lefttimeTV.text.toString()
            val rightday = rightdayTV.text.toString()
            val righttime = righttimeTV.text.toString()

            var leftreplace = lefttime.replace("시", ":00")
            var rightreplace = righttime.replace("시", ":59")

            var lftday = leftday + leftreplace
            var rgtday = rightday + rightreplace
            var biotopedata = db!!.query("biotopeAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            biotopeSelect(biotopedata)

            var birdsdata = db!!.query("birdsAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            birdsSelect(birdsdata)

            var reptiliadata = db!!.query("reptiliaAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            reptiliaSelect(reptiliadata)

            var mammaldata = db!!.query("mammalAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            mammalSelect(mammaldata)

            var fishdata = db!!.query("fishAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            fishSelect(fishdata)

            var insectdata = db!!.query("insectAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            insectSelect(insectdata)

            var floradata = db!!.query("floraAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            floraSelect(floradata)

            var zoobendata = db!!.query("ZoobenthosAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            zoobenSelect(zoobendata)

            var waypointdata = db!!.query("Waypoint", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            waypointSelect(waypointdata)

            var flora2data = db!!.query("ManyFloraAttribute", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            flora2Select(flora2data)

            var stockmapdata = db!!.query("StockMap", dataList, "INV_DT || ' ' || INV_TM between '$lftday' and '$rgtday' ", null, null, null, "", null)
            stockmapSelect(stockmapdata)

        }


    }

    fun onDateSet(view: DlgResearchActivity, year: Int, monthOfYear: Int, dayOfMonth: Int, type:String) {
        val month = monthOfYear
        var monthString = ""
        monthString = month.toString()

        val day = dayOfMonth
        var dayString = ""
        dayString = day.toString()

        if (month < 10){
            monthString = ""
            monthString += "0" + month.toString()
        }

        if (dayOfMonth < 10){
            dayString = ""
            dayString += "0" + day.toString()
        }
        if (type == "l"){
            leftdayTV.setText(year.toString() + "-" + monthString.toString() + "-" + dayString.toString() + " ")
        } else {
            rightdayTV.setText(year.toString() + "-" + monthString.toString() + "-" + dayString.toString() + " ")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                LEFTTIME -> {

                    lefttimeTV.setText(data!!.getStringExtra("selectDlg") + "시")

                };
                RIGHTTIME -> {

                    righttimeTV.setText(data!!.getStringExtra("selectDlg") + "시")

                };
            }
        }
    }



    fun biotopeSelect(biotopedata:Cursor){
        while (biotopedata.moveToNext()) {
            var biotope_attribute: Biotope_attribute = Biotope_attribute(biotopedata.getString(0), biotopedata.getString(1), biotopedata.getString(2), biotopedata.getString(3)
                    , biotopedata.getString(4), biotopedata.getString(5), biotopedata.getString(6), biotopedata.getInt(7),
                    biotopedata.getString(8), biotopedata.getFloat(9), biotopedata.getFloat(10), biotopedata.getString(11), biotopedata.getString(12), biotopedata.getString(13), biotopedata.getFloat(14)
                    , biotopedata.getString(15), biotopedata.getString(16), biotopedata.getString(17), biotopedata.getString(18), biotopedata.getString(19), biotopedata.getString(20), biotopedata.getString(21)
                    , biotopedata.getString(22), biotopedata.getString(23), biotopedata.getString(24), biotopedata.getString(25), biotopedata.getFloat(26), biotopedata.getFloat(27), biotopedata.getFloat(28)
                    , biotopedata.getString(29), biotopedata.getString(30), biotopedata.getString(31), biotopedata.getFloat(32), biotopedata.getFloat(33), biotopedata.getFloat(34), biotopedata.getString(35)
                    , biotopedata.getString(36), biotopedata.getString(37), biotopedata.getFloat(38), biotopedata.getFloat(39), biotopedata.getString(40), biotopedata.getString(41), biotopedata.getString(42)
                    , biotopedata.getFloat(43), biotopedata.getFloat(44), biotopedata.getString(45), biotopedata.getString(46), biotopedata.getString(47), biotopedata.getString(48), biotopedata.getDouble(49)
                    , biotopedata.getDouble(50), biotopedata.getString(51), biotopedata.getString(52), biotopedata.getString(53), biotopedata.getString(54), biotopedata.getString(55), biotopedata.getString(56), biotopedata.getString(57)
                    , biotopedata.getFloat(58), biotopedata.getFloat(59),biotopedata.getFloat(60),biotopedata.getFloat(61),biotopedata.getFloat(62),biotopedata.getFloat(63)
                    ,biotopedata.getFloat(64),biotopedata.getFloat(65),biotopedata.getFloat(66),biotopedata.getFloat(67),biotopedata.getFloat(68),biotopedata.getFloat(69), biotopedata.getString(70),biotopedata.getFloat(71))
            val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +biotope_attribute.GROP_ID+ File.separator)
            var tmpfileList = tmpfiles.listFiles()
            var PICTURE_YN = "없음"
            if (tmpfileList != null){
                PICTURE_YN = "있음"
            }
            var TRE_SPEC = biotope_attribute.TRE_SPEC
            if (TRE_SPEC == null || TRE_SPEC == ""){
                TRE_SPEC = "-"
            }

            var STRE_SPEC = biotope_attribute.STRE_SPEC
            if (STRE_SPEC == null || STRE_SPEC == ""){
                STRE_SPEC = "-"
            }

            var SHR_SPEC = biotope_attribute.SHR_SPEC
            if (SHR_SPEC == null || SHR_SPEC == ""){
                SHR_SPEC = "-"
            }

            var HER_SPEC = biotope_attribute.HER_SPEC
            if (HER_SPEC == null || HER_SPEC == ""){
                HER_SPEC = "-"
            }


            var spec = TRE_SPEC+","+STRE_SPEC+","+SHR_SPEC+","+HER_SPEC
            var item = Research(biotope_attribute.id,biotope_attribute.GROP_ID,biotope_attribute.INV_REGION,biotope_attribute.PRJ_NAME,biotope_attribute.INV_DT,biotope_attribute.INV_TM,"비오톱"
                    ,spec,"-",PICTURE_YN)
            datas.add(item)
        }
    }

    fun birdsSelect(data:Cursor){
        while (data.moveToNext()) {

            var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21), data.getString(22)
                    , data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27), data.getString(28), data.getString(29), data.getString(30)
                    , data.getInt(31), data.getInt(32), data.getFloat(33), data.getInt(34), data.getInt(35), data.getFloat(36)
            )

            val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images"+ File.separator +birds_attribute.GROP_ID+ File.separator)
            var tmpfileList = tmpfiles.listFiles()
            var PICTURE_YN = "없음"
            if (tmpfileList != null){
                PICTURE_YN = "있음"
            }

            var SPEC_NM = birds_attribute.SPEC_NM
            if (SPEC_NM == null || SPEC_NM == ""){
                SPEC_NM = "-"
            }

            var item = Research(birds_attribute.id,birds_attribute.GROP_ID,birds_attribute.INV_REGION,birds_attribute.PRJ_NAME,birds_attribute.INV_DT,birds_attribute.INV_TM,"조류"
                    ,SPEC_NM,birds_attribute.INDI_CNT.toString(),PICTURE_YN)
            datas.add(item)

        }
    }

    fun reptiliaSelect(reptiliadata:Cursor){

        while (reptiliadata.moveToNext()) {

            var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(reptiliadata.getString(0), reptiliadata.getString(1), reptiliadata.getString(2), reptiliadata.getString(3), reptiliadata.getString(4), reptiliadata.getString(5), reptiliadata.getString(6), reptiliadata.getString(7),
                    reptiliadata.getString(8), reptiliadata.getFloat(9), reptiliadata.getString(10), reptiliadata.getInt(11), reptiliadata.getString(12), reptiliadata.getString(13), reptiliadata.getString(14)
                    , reptiliadata.getString(15), reptiliadata.getString(16), reptiliadata.getInt(17), reptiliadata.getInt(18), reptiliadata.getInt(19), reptiliadata.getString(20), reptiliadata.getString(21), reptiliadata.getString(22)
                    , reptiliadata.getString(23), reptiliadata.getString(24), reptiliadata.getString(25), reptiliadata.getInt(26), reptiliadata.getInt(27), reptiliadata.getInt(28), reptiliadata.getFloat(29), reptiliadata.getFloat(30)
                    , reptiliadata.getString(31), reptiliadata.getString(32), reptiliadata.getString(33) , reptiliadata.getInt(34), reptiliadata.getInt(35), reptiliadata.getFloat(36), reptiliadata.getInt(37), reptiliadata.getInt(38), reptiliadata.getFloat(39)
                    , reptiliadata.getFloat(39)
            )
            val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images"+ File.separator +reptilia_attribute.GROP_ID+ File.separator)
            var tmpfileList = tmpfiles.listFiles()
            var PICTURE_YN = "없음"
            if (tmpfileList != null){
                PICTURE_YN = "있음"
            }
            var SPEC_NM = reptilia_attribute.SPEC_NM
            if (SPEC_NM == null || SPEC_NM == ""){
                SPEC_NM = "-"
            }

            var IN_CNT_ADU = "성채 : " + reptilia_attribute.IN_CNT_ADU.toString()
            if (reptilia_attribute.IN_CNT_ADU == 0 || reptilia_attribute.IN_CNT_ADU == null){
                IN_CNT_ADU = "성채 : -"
            }

            var IN_CNT_LAR = "유생 : " + reptilia_attribute.IN_CNT_LAR.toString()
            if (reptilia_attribute.IN_CNT_LAR == 0 || reptilia_attribute.IN_CNT_LAR == null){
                IN_CNT_LAR = "유생 : -"
            }

            var IN_CNT_EGG = "알덩이 : " + reptilia_attribute.IN_CNT_EGG.toString()
            if (reptilia_attribute.IN_CNT_EGG == 0 || reptilia_attribute.IN_CNT_EGG == null){
                IN_CNT_EGG = "알덩이 : -"
            }

            var INDI_CNT = IN_CNT_ADU + "," + IN_CNT_LAR + "," + IN_CNT_EGG

            var item = Research(reptilia_attribute.id,reptilia_attribute.GROP_ID,reptilia_attribute.INV_REGION,reptilia_attribute.PRJ_NAME,reptilia_attribute.INV_DT,reptilia_attribute.INV_TM,"양서/파충류"
                    ,SPEC_NM,INDI_CNT,PICTURE_YN)
            datas.add(item)

        }

    }

    fun mammalSelect(data:Cursor){
        while (data.moveToNext()) {

            var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getInt(19), data.getString(20), data.getString(21), data.getString(22)
                    , data.getFloat(23), data.getFloat(24), data.getString(25), data.getString(26), data.getString(27), data.getString(28), data.getString(29), data.getString(30), data.getString(31))

            val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +mammal_attribute.GROP_ID+ File.separator)
            var tmpfileList = tmpfiles.listFiles()
            var PICTURE_YN = "없음"
            if (tmpfileList != null){
                PICTURE_YN = "있음"
            }

            var SPEC_NM = mammal_attribute.SPEC_NM
            if (SPEC_NM == null || SPEC_NM == ""){
                SPEC_NM = "-"
            }

            var item = Research(mammal_attribute.id,mammal_attribute.GROP_ID,mammal_attribute.INV_REGION,mammal_attribute.PRJ_NAME,mammal_attribute.INV_DT,mammal_attribute.INV_TM,"포유류"
                    ,SPEC_NM,mammal_attribute.INDI_CNT.toString(),PICTURE_YN)
            datas.add(item)

        }
    }

    fun fishSelect(fishdata:Cursor){
        while (fishdata.moveToNext()) {

            var fish_attribute: Fish_attribute = Fish_attribute(fishdata.getString(0), fishdata.getString(1), fishdata.getString(2), fishdata.getString(3), fishdata.getString(4), fishdata.getString(5), fishdata.getString(6), fishdata.getString(7),
                    fishdata.getString(8), fishdata.getString(9), fishdata.getFloat(10), fishdata.getString(11), fishdata.getString(12), fishdata.getString(13), fishdata.getInt(14), fishdata.getString(15), fishdata.getInt(16), fishdata.getInt(17), fishdata.getString(18),
                    fishdata.getFloat(19), fishdata.getFloat(20), fishdata.getString(21),fishdata.getString(22), fishdata.getInt(23), fishdata.getInt(24), fishdata.getInt(25), fishdata.getInt(26), fishdata.getString(27), fishdata.getString(28),
                    fishdata.getInt(29),fishdata.getInt(30),fishdata.getInt(31),fishdata.getInt(32),fishdata.getInt(33), fishdata.getString(34), fishdata.getInt(35), fishdata.getString(36), fishdata.getString(37), fishdata.getString(38),
                    fishdata.getInt(39), fishdata.getString(40), fishdata.getString(41), fishdata.getString(42), fishdata.getString(43), fishdata.getString(44), fishdata.getString(45))

            val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images"+ File.separator +fish_attribute.GROP_ID+ File.separator)
            var tmpfileList = tmpfiles.listFiles()
            var PICTURE_YN = "없음"
            if (tmpfileList != null){
                PICTURE_YN = "있음"
            }
            var SPEC_NM = fish_attribute.SPEC_NM
            if (SPEC_NM == null || SPEC_NM == ""){
                SPEC_NM = "-"
            }
            var item = Research(fish_attribute.id,fish_attribute.GROP_ID,fish_attribute.INV_REGION,fish_attribute.PRJ_NAME,fish_attribute.INV_DT,fish_attribute.INV_TM,"어류"
                    ,SPEC_NM,fish_attribute.INDI_CNT.toString(),PICTURE_YN)
            datas.add(item)

        }
    }

    fun insectSelect(insectdata:Cursor){
        while (insectdata.moveToNext()) {

            var insect_attribute: Insect_attribute = Insect_attribute(insectdata.getString(0), insectdata.getString(1), insectdata.getString(2), insectdata.getString(3), insectdata.getString(4)
                    , insectdata.getString(5), insectdata.getString(6), insectdata.getString(7), insectdata.getString(8), insectdata.getFloat(9), insectdata.getString(10), insectdata.getInt(11)
                    , insectdata.getString(12), insectdata.getString(13), insectdata.getString(14), insectdata.getString(15), insectdata.getInt(16), insectdata.getString(17), insectdata.getString(18)
                    , insectdata.getString(19), insectdata.getString(20), insectdata.getString(21), insectdata.getString(22), insectdata.getString(23), insectdata.getString(24), insectdata.getString(25)
                    , insectdata.getFloat(26), insectdata.getFloat(27), insectdata.getString(28), insectdata.getString(29), insectdata.getString(30))

            val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "insect/images"+ File.separator +insect_attribute.GROP_ID+ File.separator)
            var tmpfileList = tmpfiles.listFiles()
            var PICTURE_YN = "없음"
            if (tmpfileList != null) {
                PICTURE_YN = "있음"
            }

            var SPEC_NM = insect_attribute.SPEC_NM
            if (SPEC_NM == null || SPEC_NM == "") {
                SPEC_NM = "-"
            }
            var item = Research(insect_attribute.id,insect_attribute.GROP_ID,insect_attribute.INV_REGION,insect_attribute.PRJ_NAME,insect_attribute.INV_DT,insect_attribute.INV_TM,"곤충"
                    ,SPEC_NM,insect_attribute.INDI_CNT.toString(),PICTURE_YN)
            datas.add(item)

        }
    }

    fun floraSelect(floradata:Cursor){
        while (floradata.moveToNext()) {

            var flora_Attribute: Flora_Attribute = Flora_Attribute(floradata.getString(0), floradata.getString(1), floradata.getString(2), floradata.getString(3), floradata.getString(4)
                    , floradata.getString(5), floradata.getString(6), floradata.getString(7), floradata.getString(8), floradata.getFloat(9), floradata.getString(10), floradata.getInt(11)
                    , floradata.getString(12), floradata.getString(13), floradata.getString(14), floradata.getString(15), floradata.getString(16), floradata.getString(17), floradata.getString(18)
                    , floradata.getString(19), floradata.getInt(20), floradata.getString(21), floradata.getFloat(22), floradata.getFloat(23), floradata.getString(24), floradata.getString(25), floradata.getString(26))

            val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images"+ File.separator +flora_Attribute.GROP_ID+ File.separator)
            var tmpfileList = tmpfiles.listFiles()
            var PICTURE_YN = "없음"
            if (tmpfileList != null){
                PICTURE_YN = "있음"
            }

            var SPEC_NM = flora_Attribute.SPEC_NM
            if (SPEC_NM == null || SPEC_NM == "") {
                SPEC_NM = "-"
            }

            var item = Research(flora_Attribute.id,flora_Attribute.GROP_ID,flora_Attribute.INV_REGION,flora_Attribute.PRJ_NAME,flora_Attribute.INV_DT,flora_Attribute.INV_TM,"식물"
                    ,SPEC_NM,"-",PICTURE_YN)
            datas.add(item)

        }
    }

    fun zoobenSelect(data:Cursor){
        while (data.moveToNext()) {

            var zoo: Zoobenthos_Attribute = Zoobenthos_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                    data.getInt(8), data.getFloat(9), data.getInt(10), data.getInt(11), data.getFloat(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getInt(19), data.getInt(20), data.getInt(21), data.getInt(22)
                    , data.getInt(23), data.getString(24), data.getString(25), data.getString(26), data.getFloat(27), data.getFloat(28), data.getString(29), data.getFloat(30), data.getFloat(31), data.getFloat(32), data.getFloat(33)
                    , data.getFloat(34), data.getFloat(35), data.getFloat(36), data.getFloat(37), data.getString(38), data.getString(39), data.getString(40), data.getString(41), data.getString(42), data.getString(43), data.getString(44)
                    , data.getString(45), data.getString(46), data.getString(47), data.getString(48), data.getFloat(49), data.getFloat(50), data.getString(51), data.getString(52), data.getString(53)
                    , data.getString(54), data.getString(55), data.getString(56), data.getInt(57))

            val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images"+ File.separator +zoo.GROP_ID+ File.separator)
            var tmpfileList = tmpfiles.listFiles()
            var PICTURE_YN = "없음"
            if (tmpfileList != null){
                PICTURE_YN = "있음"
            }

            var SPEC_NM = zoo.SPEC_NM
            if (SPEC_NM == null || SPEC_NM == "") {
                SPEC_NM = "-"
            }

            var item = Research(zoo.id,zoo.GROP_ID,zoo.INV_REGION,zoo.PRJ_NAME,zoo.INV_DT,zoo.INV_TM,"저서무척추동물"
                    ,SPEC_NM,"-",PICTURE_YN)
            datas.add(item)

        }
    }

    fun waypointSelect(data:Cursor){
        while (data.moveToNext()) {
            var waypoint: Waypoint = Waypoint(data.getInt(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7), data.getFloat(8), data.getFloat(9), data.getString(10), data.getString(11))

            var PICTURE_YN = "-"
            var SPEC_NM = "-"

            var item = Research(waypoint.id.toString(),waypoint.GROP_ID,waypoint.INV_REGION,waypoint.PRJ_NAME,waypoint.INV_DT,waypoint.INV_TM,"웨이포인트"
                    ,SPEC_NM,"-",PICTURE_YN)
            datas.add(item)
        }
    }

    fun flora2Select(data:Cursor){
        while (data.moveToNext()) {

            var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                    data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getString(12), data.getInt(13), data.getString(14)
                    , data.getInt(15), data.getString(16), data.getString(17), data.getString(18), data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22)
                    , data.getString(23), data.getInt(24), data.getString(25), data.getString(26), data.getString(27), data.getFloat(28), data.getFloat(29), data.getString(30), data.getInt(31), data.getString(32)
                    , data.getString(33), data.getString(34), data.getFloat(35), data.getFloat(36), data.getInt(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41), data.getString(42))

            var PICTURE_YN = "-"
            var HER_SPECET = "초본층 : "+manyFloraAttribute.HER_SPEC
            if (HER_SPECET == "" || HER_SPECET == null){
                HER_SPECET = "초본층 : -"
            }

            var TRE_SPECET = "교목층 : "+manyFloraAttribute.TRE_SPEC
            if (TRE_SPECET == "" || TRE_SPECET == null){
                TRE_SPECET = "교목층 : -"
            }

            var STRE_SPECET = "아교목층 : "+manyFloraAttribute.STRE_SPEC
            if (STRE_SPECET == "" || STRE_SPECET == null){
                STRE_SPECET = "아교목층 : -"
            }

            var STR_SPECET = "관목층 : "+manyFloraAttribute.SHR_SPEC
            if (STR_SPECET == "" || STR_SPECET == null){
                STR_SPECET = "관목층 : -"
            }

            var SPEC_NM = HER_SPECET+","+TRE_SPECET+","+STRE_SPECET+","+STR_SPECET

            var item = Research(manyFloraAttribute.id,manyFloraAttribute.GROP_ID,manyFloraAttribute.INV_REGION,"-",manyFloraAttribute.INV_DT,manyFloraAttribute.INV_TM,"식생조사 위치"
                    ,SPEC_NM,"-",PICTURE_YN)
            datas.add(item)


        }
    }

    fun stockmapSelect(data:Cursor) {
        while (data.moveToNext()) {

            var stockMap: StockMap = StockMap(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                    data.getString(8), data.getString(9), data.getString(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getFloat(20), data.getFloat(21)
                    , data.getString(22), data.getString(23), data.getString(24))

            var PICTURE_YN = "-"
            var SPEC_NM = stockMap.KOFTR_GROUP_CD
            var item = Research(stockMap.id,stockMap.GROP_ID,stockMap.INV_REGION,"-",stockMap.INV_DT,stockMap.INV_TM,"식생조사"
                    ,SPEC_NM,"-",PICTURE_YN)
            datas.add(item)
        }

        println("----datas.size : ${datas.size}")

        adapter.notifyDataSetChanged()
    }

}
