package hntecology.ecology.activities

import android.Manifest
import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Reptilia_attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_reptilia.*

class ReptiliaActivity : Activity() , OnLocationUpdatedListener{

    val SET_WEATHER = 1;
    val SET_WIND = 2;
    val SET_WIND_DIRE = 3;
    val SET_REPTILIA = 4

    lateinit var context: Context;

    var chkdata: Boolean = false;

    var userName = "";

    var keyId: String? = null;

    var page:Int? = null

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101


    var dataArray:ArrayList<Reptilia_attribute> = ArrayList<Reptilia_attribute>()

    private var progressDialog: ProgressDialog? = null

    var latitude = 0.0f;
    var longitude = 0.0f;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reptilia)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        this.context = this;
        // 조사 일자
        createdDateTV.text = Utils.todayStr()
        invtmTV.text = Utils.timeStr()

        userName = PrefUtils.getStringPreference(context, "name");

        invpersonET.setText(userName)

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        var intent: Intent = getIntent();

        if (intent.getSerializableExtra("id") != null) {
            keyId = intent.getStringExtra("id")

            val dataList: Array<String> = arrayOf("*");

            val data= db.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getInt(16), data.getInt(17), data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                        , data.getString(22), data.getString(23), data.getString(24), data.getInt(25), data.getInt(26), data.getInt(27), data.getFloat(28), data.getFloat(29),data.getString(30))

                invregionET.setText(reptilia_attribute.INV_REGION)
                createdDateTV.setText(reptilia_attribute.INV_DT)

                weatherTV.setText(reptilia_attribute.WEATHER)
                windTV.setText(reptilia_attribute.WIND)
                windDireTV.setText(reptilia_attribute.WIND_DIRE)

                temperaturET.setText(reptilia_attribute.TEMPERATUR.toString())
                etcET.setText(reptilia_attribute.ETC)

                numET.setText(reptilia_attribute.NUM.toString())
                invtmTV.setText(reptilia_attribute.INV_TM)

                specnmET.setText(reptilia_attribute.SPEC_NM)
                famiET.setText(reptilia_attribute.FAMI_NM)
                scienET.setText(reptilia_attribute.SCIEN_NM)

                incntaduET.setText(reptilia_attribute.IN_CNT_ADU.toString())
                incntlarET.setText(reptilia_attribute.IN_CNT_LAR.toString())
                incnteggET.setText(reptilia_attribute.IN_CNT_EGG.toString())

                habriveerET.setText(reptilia_attribute.HAB_RIVEER)
                habedgeET.setText(reptilia_attribute.HAB_EDGE)

                waterinET.setText(reptilia_attribute.WATER_IN)
                wateroutET.setText(reptilia_attribute.WATER_OUT)

                habareawET.setText(reptilia_attribute.HAB_AREA_W.toString())
                habareahET.setText(reptilia_attribute.HAB_AREA_H.toString())

                watercontET.setText(reptilia_attribute.WATER_CONT)
                waterqualET.setText(reptilia_attribute.WATER_QUAL)
                waterdeptET.setText(reptilia_attribute.WATER_DEPT.toString())

                gpslatTV.setText(reptilia_attribute.GPS_LAT.toString())
                gpslonTV.setText(reptilia_attribute.GPS_LON.toString())

                val id = reptilia_attribute.id

                if(reptilia_attribute.TEMP_YN.equals("N")){
                    dbManager.deletereptilia_attribute(reptilia_attribute,id)
                }

                if(reptilia_attribute.TEMP_YN.equals("Y")){
                    dataArray.add(reptilia_attribute)
                }


            }

            page = dataArray.size

            reptiliapageTV.setText(page.toString() + " / " + dataArray.size)

        }

        reptilialeftLL.setOnClickListener {

            val dataList: Array<String> = arrayOf("*");

            val data= db.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getInt(16), data.getInt(17), data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                        , data.getString(22), data.getString(23), data.getString(24), data.getInt(25), data.getInt(26), data.getInt(27), data.getFloat(28), data.getFloat(29),data.getString(30))

                dataArray.add(reptilia_attribute)

            }

            if(page == dataArray.size && page!! > 1 ){
                page = page!! - 1
                reptiliapageTV.setText(page.toString() + " / " + dataArray.size)

                clear()

                resetPage(page!!)
            }else if (page!! < dataArray.size && page!! > 1){
                page = page!! - 1
                reptiliapageTV.setText(page.toString() + " / " + dataArray.size)

                clear()

                resetPage(page!!)
            }

        }


        reptiliarightLL.setOnClickListener {
            clear()

            val dataList: Array<String> = arrayOf("*");

            val data= db.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getInt(16), data.getInt(17), data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                        , data.getString(22), data.getString(23), data.getString(24), data.getInt(25), data.getInt(26), data.getInt(27), data.getFloat(28), data.getFloat(29),data.getString(30))

                dataArray.add(reptilia_attribute)

            }

            var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null,null,null,null,null)

            reptilia_attribute.id = keyId + page.toString()

            reptilia_attribute.GROP_ID = keyId

            reptilia_attribute.PRJ_NAME = ""

            reptilia_attribute.INV_REGION = invregionET.text.toString()
            reptilia_attribute.INV_DT = Utils.todayStr()
            reptilia_attribute.INV_PERSON = invpersonET.text.toString()

            reptilia_attribute.WEATHER = weatherTV.text.toString()
            reptilia_attribute.WIND = windTV.text.toString()
            reptilia_attribute.WIND_DIRE = windDireTV.text.toString()

            if(temperaturET.text.isNotEmpty()){
                reptilia_attribute.TEMPERATUR = temperaturET.text.toString().toFloat()
            }


            reptilia_attribute.ETC = etcET.text.toString()


            if(numET.text.isNotEmpty()){
                reptilia_attribute.NUM = numET.text.toString().toInt()
            }

            reptilia_attribute.INV_TM = Utils.timeStr()

            reptilia_attribute.SPEC_NM = specnmET.text.toString()
            reptilia_attribute.FAMI_NM = famiET.text.toString()
            reptilia_attribute.SCIEN_NM = scienET.text.toString()

            if(incntaduET.text.isNotEmpty()){
                reptilia_attribute.IN_CNT_ADU = incntaduET.text.toString().toInt()
            }
            if(incntlarET.text.isNotEmpty()){
                reptilia_attribute.IN_CNT_LAR = incntlarET.text.toString().toInt()
            }
            if(incnteggET.text.isNotEmpty()){
                reptilia_attribute.IN_CNT_EGG = incnteggET.text.toString().toInt()
            }

            reptilia_attribute.HAB_RIVEER = habriveerET.text.toString()
            reptilia_attribute.HAB_EDGE = habedgeET.text.toString()

            reptilia_attribute.WATER_IN = waterinET.text.toString()
            reptilia_attribute.WATER_OUT = wateroutET.text.toString()

            reptilia_attribute.WATER_CONT = watercontET.text.toString()
            reptilia_attribute.WATER_QUAL = waterqualET.text.toString()

            if(waterdeptET.text.isNotEmpty()){
                reptilia_attribute.WATER_DEPT = waterdeptET.text.toString().toInt()
            }

            if(habareawET.text.isNotEmpty()){
                reptilia_attribute.HAB_AREA_W = habareawET.text.toString().toInt()
            }

            if(habareahET.text.isNotEmpty()){
                reptilia_attribute.HAB_AREA_H = habareahET.text.toString().toInt()
            }


            if(gpslatTV.text.isNotEmpty()){
                reptilia_attribute.GPS_LAT = 0F
            }

            if(gpslonTV.text.isNotEmpty()){
                reptilia_attribute.GPS_LON = 0F
            }

            reptilia_attribute.TEMP_YN = "N"

            if(page == dataArray.size){
                dbManager.insertreptilia_attribute(reptilia_attribute)
                page = page!! + 1
            }

            val data2= db.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data2.moveToNext()) {

                chkdata = true

                var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getString(7),
                        data2.getString(8), data2.getFloat(9), data2.getString(10), data2.getInt(11), data2.getString(12), data2.getString(13), data2.getString(14)
                        , data2.getString(15), data2.getInt(16), data2.getInt(17), data2.getInt(18), data2.getString(19), data2.getString(20), data2.getString(21)
                        , data2.getString(22), data2.getString(23), data2.getString(24), data2.getInt(25), data2.getInt(26), data2.getInt(27), data2.getFloat(28), data2.getFloat(29),data2.getString(30))

                dataArray.add(reptilia_attribute)

            }

            if (page!! < dataArray.size){
                page = page!! + 1
            }

            reptiliapageTV.setText(page.toString() + " / " + dataArray.size)

            resetPage(page!!)

        }

        specnmET.setOnClickListener {
            startDlgReptilia()
        }

        famiET.setOnClickListener {
            startDlgReptilia()
        }

        scienET.setOnClickListener {
            startDlgReptilia()
        }

        weatherTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "날씨")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 1);

            startActivityForResult(intent, SET_WEATHER);
        }

        windTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "바람")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 2);

            startActivityForResult(intent, SET_WIND);
        }

        windDireTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "풍향")
            intent.putExtra("DlgHeight", 500f);
            intent.putExtra("selectDlg", 3);

            startActivityForResult(intent, SET_WIND_DIRE);
        }

        waterqualET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("좋음");
            listItems.add("보통");
            listItems.add("나쁨");

            alert(listItems, "수질", waterqualET, "waterqual");
        }

        watercontET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("100m이상 연속");
            listItems.add("100m이상 연속지 중 단절된 웅덩이");
            listItems.add("단절된 웅덩이");

            alert(listItems, "수계", watercontET, "watercont");
        }

        btn_biotopDelete.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null)

                        val tmppage = page!! - 1 !!
                        val id = keyId + tmppage.toString()

                        dbManager.deletereptilia_attribute(reptilia_attribute,id)

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        btn_biotopCancle1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        btn_biotopSave1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null
                        ,null,null,null,null,null,null,null,null,null,null)

                        val id = keyId + page.toString()

                        reptilia_attribute.id = id
                        reptilia_attribute.GROP_ID = keyId

                        reptilia_attribute.PRJ_NAME = ""

                        reptilia_attribute.INV_REGION = invregionET.text.toString()
                        reptilia_attribute.INV_DT = Utils.todayStr()
                        reptilia_attribute.INV_PERSON = invpersonET.text.toString()

                        reptilia_attribute.WEATHER = weatherTV.text.toString()
                        reptilia_attribute.WIND = windTV.text.toString()
                        reptilia_attribute.WIND_DIRE = windDireTV.text.toString()

                        if(temperaturET.text.isNotEmpty()){
                            reptilia_attribute.TEMPERATUR = temperaturET.text.toString().toFloat()
                        }

                        reptilia_attribute.ETC = etcET.text.toString()

                        if(numET.text.isNotEmpty()){
                            reptilia_attribute.NUM = numET.text.toString().toInt()
                        }

                        reptilia_attribute.INV_TM = Utils.timeStr()

                        reptilia_attribute.SPEC_NM = specnmET.text.toString()
                        reptilia_attribute.FAMI_NM = famiET.text.toString()
                        reptilia_attribute.SCIEN_NM = scienET.text.toString()

                        if(incntaduET.text.isNotEmpty()){
                            reptilia_attribute.IN_CNT_ADU = incntaduET.text.toString().toInt()
                        }
                        if(incntlarET.text.isNotEmpty()){
                            reptilia_attribute.IN_CNT_LAR = incntlarET.text.toString().toInt()
                        }
                        if(incnteggET.text.isNotEmpty()){
                            reptilia_attribute.IN_CNT_EGG = incnteggET.text.toString().toInt()
                        }

                        reptilia_attribute.HAB_RIVEER = habriveerET.text.toString()
                        reptilia_attribute.HAB_EDGE = habedgeET.text.toString()

                        reptilia_attribute.WATER_IN = waterinET.text.toString()
                        reptilia_attribute.WATER_OUT = wateroutET.text.toString()

                        reptilia_attribute.WATER_CONT = watercontET.text.toString()
                        reptilia_attribute.WATER_QUAL = waterqualET.text.toString()

                        if(waterdeptET.text.isNotEmpty()){
                            reptilia_attribute.WATER_DEPT = waterdeptET.text.toString().toInt()
                        }

                        if(habareawET.text.isNotEmpty()){
                            reptilia_attribute.HAB_AREA_W = habareawET.text.toString().toInt()
                        }

                        if(habareahET.text.isNotEmpty()){
                            reptilia_attribute.HAB_AREA_H = habareahET.text.toString().toInt()
                        }


                        if(gpslatTV.text.isNotEmpty()){
                            reptilia_attribute.GPS_LAT = latitude
                        }

                        if(gpslonTV.text.isNotEmpty()){
                            reptilia_attribute.GPS_LON = longitude
                        }

                        reptilia_attribute.TEMP_YN = "Y"


                        if (chkdata){

                            val tmppage = page!! - 1
                            val pk = keyId + tmppage.toString()
                            dbManager.updatereptilia_attribute(reptilia_attribute,pk)

                        } else {
                            dbManager.insertreptilia_attribute(reptilia_attribute)
                        }



                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()


        }

        initGPS()

    }
    fun startDlgReptilia(){
        val intent = Intent(context, DlgReptiliaActivity::class.java)
        startActivityForResult(intent, SET_REPTILIA);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_WEATHER -> {

                    weatherTV.text = data!!.getStringExtra("selectDlg")

                }
                SET_WIND -> {

                    windTV.text = data!!.getStringExtra("selectDlg")

                }
                SET_WIND_DIRE -> {

                    windDireTV.text = data!!.getStringExtra("selectDlg")

                }

                SET_REPTILIA -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    specnmET.text = name
                    famiET.text = family_name
                    scienET.text = zoological

                }
            }
        }
    }


    fun alert(ListItems: ArrayList<String>, title: String, textView: TextView, type: String) {

        val items = Array<CharSequence>(ListItems.size, { i -> ListItems.get(i) })

        var size = ListItems.size

        var builder: AlertDialog.Builder = AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->


            var selectItem = ListItems.get(i);

            if (selectItem != "취소") {
                textView.text = selectItem
            }

            if ("waterqual" == type){

            }



        })

        builder.show();
    }

    fun clear(){

        invregionET.setText("")
        createdDateTV.setText("")

        weatherTV.setText("")
        windTV.setText("")
        windDireTV.setText("")

        temperaturET.setText("")
        etcET.setText("")

        invtmTV.setText("")

        specnmET.setText("")
        famiET.setText("")
        scienET.setText("")

        incntaduET.setText("")
        incntlarET.setText("")
        incnteggET.setText("")

        habriveerET.setText("")
        habedgeET.setText("")

        waterinET.setText("")
        wateroutET.setText("")
        waterdeptET.setText("")

        habareawET.setText("")
        habareahET.setText("")

    }

    fun resetPage(page : Int){
        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val tmppages = page - 1

        val id = keyId + tmppages.toString()

        val data= db.query("reptiliaAttribute", dataList, "id = '$id'", null, null, null, "", null)

        if (dataArray != null){
            dataArray.clear()
        }

        while (data.moveToNext()) {

            chkdata = true

            var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getInt(16), data.getInt(17), data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getString(22), data.getString(23), data.getString(24), data.getInt(25), data.getInt(26), data.getInt(27), data.getFloat(28), data.getFloat(29),data.getString(30))

            dataArray.add(reptilia_attribute)

            invregionET.setText(reptilia_attribute.INV_REGION)
            if(invregionET.text == null){
                invregionET.setText("")
            }

            createdDateTV.setText(reptilia_attribute.INV_DT)
            if(createdDateTV.text == null){
                createdDateTV.setText("")
            }

            weatherTV.setText(reptilia_attribute.WEATHER)
            if(weatherTV.text == null){
                weatherTV.setText("")
            }

            windTV.setText(reptilia_attribute.WIND)
            if(windTV.text == null){
                windTV.setText("")
            }

            windDireTV.setText(reptilia_attribute.WIND_DIRE)
            if(windDireTV.text == null){
                windDireTV.setText("")
            }

            temperaturET.setText(reptilia_attribute.TEMPERATUR.toString())
            if(temperaturET.text == null){
                temperaturET.setText("")
            }

            etcET.setText(reptilia_attribute.ETC)
            if(etcET.text == null){
                etcET.setText("")
            }

            numET.setText(reptilia_attribute.NUM.toString())
            if(numET.text == null){
                numET.setText("")
            }

            invtmTV.setText(reptilia_attribute.INV_TM)
            if(invtmTV.text == null){
                invtmTV.setText("")
            }

            specnmET.setText(reptilia_attribute.SPEC_NM)
            if(specnmET.text == null){
                specnmET.setText("")
            }

            famiET.setText(reptilia_attribute.FAMI_NM)
            if(famiET.text == null){
                famiET.setText("")
            }

            scienET.setText(reptilia_attribute.SCIEN_NM)
            if(scienET.text == null){
                scienET.setText("")
            }

            incntaduET.setText(reptilia_attribute.IN_CNT_ADU.toString())
            if(incntaduET.text == null){
                incntaduET.setText("")
            }
            incntlarET.setText(reptilia_attribute.IN_CNT_LAR.toString())
            if(incntlarET.text == null){
                incntlarET.setText("")
            }
            incnteggET.setText(reptilia_attribute.IN_CNT_EGG.toString())
            if(incnteggET.text == null){
                incnteggET.setText("")
            }

            habriveerET.setText(reptilia_attribute.HAB_RIVEER)
            if(habriveerET.text == null){
                habriveerET.setText("")
            }
            habedgeET.setText(reptilia_attribute.HAB_EDGE)
            if(habedgeET.text == null){
                habedgeET.setText("")
            }

            waterinET.setText(reptilia_attribute.WATER_IN)
            if(waterinET.text == null){
                waterinET.setText("")
            }

            wateroutET.setText(reptilia_attribute.WATER_OUT)
            if(wateroutET.text == null){
                wateroutET.setText("")
            }

            watercontET.setText(reptilia_attribute.WATER_CONT)
            if(watercontET.text == null){
                watercontET.setText("")
            }

            waterqualET.setText(reptilia_attribute.WATER_QUAL)
            if(waterqualET.text == null){
                waterqualET.setText("")
            }

            waterdeptET.setText(reptilia_attribute.WATER_DEPT.toString())
            if(waterdeptET.text == null){
                waterdeptET.setText("")
            }

            habareawET.setText(reptilia_attribute.HAB_AREA_W.toString())
            if(habareawET.text == null){
                habareawET.setText("0")
            }
            habareahET.setText(reptilia_attribute.HAB_AREA_H.toString())
            if(habareahET.text == null){
                habareahET.setText("0")
            }

            gpslatTV.setText(reptilia_attribute.GPS_LAT.toString())
            gpslonTV.setText(reptilia_attribute.GPS_LON.toString())


        }


    }


    private fun initGPS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION)
        } else {
            checkGPs()
        }
    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (Manifest.permission.ACCESS_FINE_LOCATION == perm) {
                loadPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION)
            } else if (Manifest.permission.ACCESS_COARSE_LOCATION == perm) {
                checkGPs()
            }
        }
    }

    private fun checkGPs() {
        if (Utils.availableLocationService(context)) {
            startLocation()
        } else {
            gpsCheckAlert.sendEmptyMessage(0)
        }
    }

    internal var gpsCheckAlert: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val mainGpsSearchCount = PrefUtils.getIntPreference(context, "mainGpsSearchCount", 0)

            if (mainGpsSearchCount == 0) {
                latitude = -1.0f
                longitude = -1.0f

                val builder = AlertDialog.Builder(context)
                builder.setTitle("확인")
                builder.setMessage("위치 서비스 이용이 제한되어 있습니다.\n설정에서 위치 서비스 이용을 허용해주세요.")
                builder.setCancelable(true)
                builder.setNegativeButton("취소") { dialog, id ->
                    dialog.cancel()

                    latitude = 37.5203175f
                    longitude = 126.9107831f

                }
                builder.setPositiveButton("설정") { dialog, id ->
                    dialog.cancel()
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                val alert = builder.create()
                alert.show()
            }
        }
    }

    private fun startLocation() {
        if (progressDialog != null) {
            // show dialog
            //progressDialog.setMessage("현재 위치 확인 중...");
            progressDialog!!.show()
        }

        val smartLocation = SmartLocation.Builder(context).logging(true).build()
        val locationControl = smartLocation.location(LocationManagerProvider()).oneFix()

        if (SmartLocation.with(context).location(LocationManagerProvider()).state().isGpsAvailable()) {
            val locationParams = LocationParams.Builder().setAccuracy(LocationAccuracy.MEDIUM).build()
            locationControl.config(locationParams)
        } else if (SmartLocation.with(context).location(LocationManagerProvider()).state().isNetworkAvailable()) {
            val locationParams = LocationParams.Builder().setAccuracy(LocationAccuracy.LOW).build()
            locationControl.config(locationParams)
        }
        smartLocation.location().oneFix().start(this)
    }

    override fun onLocationUpdated(p0: Location?) {

        stopLocation()

        if (p0 != null) {

            latitude = p0.getLatitude().toFloat()
            longitude = p0.getLongitude().toFloat()

            gpslatTV.setText(latitude.toString())
            gpslonTV.setText(longitude.toString())

            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        }

    }


    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
    }


}
