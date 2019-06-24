package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.*
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_dlg_data_list.*

class DlgDataListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var biotopeData: ArrayList<Biotope_attribute>
    private lateinit var birdsData : ArrayList<Birds_attribute>
    private lateinit var reptiliasData : ArrayList<Reptilia_attribute>
    private lateinit var mammalsData : ArrayList<Mammal_attribute>
    private lateinit var fishsData : ArrayList<Fish_attribute>
    private lateinit var insectData : ArrayList<Insect_attribute>
    private lateinit var florasData : ArrayList<Flora_Attribute>
    private  lateinit var zoobenthosData : ArrayList<Zoobenthos_Attribute>
    private lateinit var manyflorasData : ArrayList<ManyFloraAttribute>
    private lateinit var stocksData : ArrayList<StockMap>

    private lateinit var biotopeAdaper : DataBiotopeAdapter
    private lateinit var birdsAadapter: DataBirdsAdapter;
    private lateinit var reptiliaAdapter : DataReptiliaAdapter
    private lateinit var mammalAdapter : DataMammalAdapter
    private lateinit var fishAdapter : DataFIshAdapter
    private lateinit var insectAdapter : DataInsectAdapter
    private lateinit var floraAdapter : DataFloraAdapter
    private lateinit var zoobenthousAdapter : DataZoobenthosAdapter
    private lateinit var manyfloraAdapter : DataManyFloraAdapter
    private lateinit var stockmapAdapter : DataStockAdapter

    private val MarkerCallBackData = 1004

    val BIOTOPE_DATA = 3000
    val BIRDS_DATA = 3001
    val REPTILIA_DATA = 3002
    val MAMMALIA_DATA = 3003
    val FISH_DATA = 3004
    val INSECT_DATA = 3005
    val FLORA_DATA = 3006
    val ZOOBENTHOS_DATA = 3007
    val FLORA_DATA2 = 3008
    val STOCKMAP_DATA = 3009

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F
    var GROP_ID:String = ""
    var markerid:String? = null
    var polygonid:String? = null
    var landuse:String? = null

    val BIOTOPE = 1
    val BIRDS = 2
    val REPTILIA = 3
    val MAMMAL = 4
    val FISH = 5
    val INSECT = 6
    val FLORA = 7
    val ZOOBENTHOUS = 8
    val FLORA2 = 9
    val STOCKMAP = 10

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_data_list)

        context = applicationContext;

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);
        GROP_ID = intent.getStringExtra("GROP_ID")

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        titleTV.setText(titleName)

        listView1 = findViewById(R.id.listLV)

        biotopeData = ArrayList()
        birdsData = ArrayList()
        reptiliasData = ArrayList()
        mammalsData = ArrayList()
        fishsData = ArrayList()
        insectData = ArrayList()
        florasData = ArrayList()
        zoobenthosData = ArrayList()
        manyflorasData = ArrayList()
        stocksData = ArrayList()

        biotopeAdaper = DataBiotopeAdapter(context,biotopeData)
        birdsAadapter = DataBirdsAdapter(context,birdsData)
        reptiliaAdapter = DataReptiliaAdapter(context,reptiliasData)
        mammalAdapter = DataMammalAdapter(context,mammalsData)
        fishAdapter = DataFIshAdapter(context,fishsData)
        insectAdapter = DataInsectAdapter(context,insectData)
        floraAdapter = DataFloraAdapter(context,florasData)
        zoobenthousAdapter = DataZoobenthosAdapter(context,zoobenthosData)
        manyfloraAdapter = DataManyFloraAdapter(context,manyflorasData)
        stockmapAdapter = DataStockAdapter(context,stocksData)

        if(intent.getStringExtra("markerid") != null){
            markerid = intent.getStringExtra("markerid")
        }

        if(intent.getStringExtra("polygonid") != null){
            polygonid = intent.getStringExtra("polygonid")
        }

        if (intent.getIntExtra("landuse",0) != null){
            val num = intent.getIntExtra("landuse",0)
            landuse = num.toString()
        }
        val dataList: Array<String> = arrayOf("*");
        if(tableName.equals("biotopeAttribute")) {

            Log.d("데이터",biotopeData.toString())
            val biotopsdata=  db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,"GROP_ID",null,null,null);
            Log.d("데이터",biotopsdata.toString())

            biotopesdataList(biotopeData,biotopsdata)

            listView1.adapter = biotopeAdaper

            biotopsdata.close()

        }

        if(tableName.equals("birdsAttribute")){

            val birdsdata=  db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            birdsdataList(birdsData,birdsdata)

            listView1.adapter = birdsAadapter

            birdsdata.close()

        }

        if(tableName.equals("reptiliaAttribute")){


            val reptiliasdata=  db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            reptiliasdataList(reptiliasData,reptiliasdata)

            listView1.adapter = reptiliaAdapter

            reptiliasdata.close()
        }

        if(tableName.equals("mammalAttribute")) {


            val mammalsdata=  db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            mammalsdataList(mammalsData,mammalsdata)

            listView1.adapter = mammalAdapter

            mammalsdata.close()

        }

        if(tableName.equals("fishAttribute")) {

            val fishsdata=  db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            fishsdataList(fishsData,fishsdata)

            listView1.adapter = fishAdapter

            fishsdata.close()

        }

        if(tableName.equals("insectAttribute")) {


            val insectsdata=  db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            insectsdataList(insectData,insectsdata)

            listView1.adapter = insectAdapter

            insectsdata.close()

        }

        if(tableName.equals("floraAttribute")) {


            val florasdata=  db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            florasdataList(florasData,florasdata)

            listView1.adapter = floraAdapter

            florasdata.close()
        }

        if(tableName.equals("ZoobenthosAttribute")){

            val zoobentousdata = db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null)

            zoobenthossdataList(zoobenthosData,zoobentousdata)

            listView1.adapter = zoobenthousAdapter

            zoobentousdata.close()

        }

        if(tableName.equals("ManyFloraAttribute")){



            val manyFloraData = db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null)

            manyflorasdataList(manyflorasData,manyFloraData)

            listView1.adapter = manyfloraAdapter

            manyFloraData.close()

        }

        if(tableName.equals("StockMap")){

            val dataList: Array<String> = arrayOf("*");

            val stocksdata=  db!!.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            stockmapsdataList(stocksData,stocksdata)

            listView1.adapter = stockmapAdapter

            stocksdata.close()

        }



        listView1.setOnItemClickListener{ parent, view, position, id ->

            if(tableName.equals("biotopeAttribute")){

                val biotopedata = biotopeAdaper.getItem(position)

                val intent = Intent(this, BiotopeActivity::class.java)
                intent!!.putExtra("position",position)
                intent!!.putExtra("id", biotopedata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",biotopedata.GROP_ID)
                if (landuse != null) {
                    intent!!.putExtra("landuse", landuse)
                }
                println("gropid_-------------------------------------------------------------${biotopedata.GROP_ID} ----")
                intent!!.putExtra("polygonid",polygonid)

                startActivityForResult(intent, BIOTOPE_DATA)

            }

            if(tableName.equals("birdsAttribute")){

                val birdsdata = birdsAadapter.getItem(position)

                val intent = Intent(this, BirdsActivity::class.java)

                intent.putExtra("id", birdsdata.id.toString())
                intent.putExtra("set",3)
                intent.putExtra("GROP_ID",birdsdata.GROP_ID)
                intent.putExtra("export", 70)
                intent.putExtra("markerid",markerid)

                println("markerid $markerid")

                startActivityForResult(intent, BIRDS_DATA)

            }

            if(tableName.equals("reptiliaAttribute")){

                val reptiliadata = reptiliaAdapter.getItem(position)

                val intent = Intent(this, ReptiliaActivity::class.java)

                intent!!.putExtra("id", reptiliadata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",reptiliadata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, REPTILIA_DATA)

            }

            if(tableName.equals("mammalAttribute")){

                val mammaldata = mammalAdapter.getItem(position)

                val intent = Intent(this, MammaliaActivity::class.java)

                intent!!.putExtra("id", mammaldata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",mammaldata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, MAMMALIA_DATA)

            }

            if(tableName.equals("fishAttribute")){

                val fishdata = fishAdapter.getItem(position)

                val intent = Intent(this, FishActivity::class.java)

                intent!!.putExtra("id", fishdata.id.toString())
                intent.putExtra("set",3)
                intent.putExtra("export", 70)
                intent!!.putExtra("GROP_ID",fishdata.GROP_ID)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, FISH_DATA)

            }

            if(tableName.equals("insectAttribute")){

                val insecthdata = insectAdapter.getItem(position)

                val intent = Intent(this, InsectActivity::class.java)

                intent!!.putExtra("id", insecthdata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",insecthdata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, INSECT_DATA)

            }

            if(tableName.equals("floraAttribute")){

                val floradata = floraAdapter.getItem(position)

                val intent = Intent(this, FloraActivity::class.java)

                intent!!.putExtra("id", floradata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",floradata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, FLORA_DATA)

            }

            if(tableName.equals("ZoobenthosAttribute")){

                val floradata = zoobenthousAdapter.getItem(position)

                val intent = Intent(this, ZoobenthosActivity::class.java)

                intent!!.putExtra("id", floradata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",floradata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, ZOOBENTHOS_DATA)

            }

            if(tableName.equals("ManyFloraAttribute")){

                val floradata = manyfloraAdapter.getItem(position)

                val intent = Intent(this, Flora2Activity::class.java)

                intent!!.putExtra("id", floradata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",floradata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, FLORA_DATA2)

            }

            if(tableName.equals("StockMap")){

                val stockdata = stockmapAdapter.getItem(position)

                val intent = Intent(this, StockActivity::class.java)

                intent!!.putExtra("id", stockdata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",stockdata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("polygonid",polygonid)
                if (landuse != null) {
                    intent!!.putExtra("landuse", landuse)
                }

                startActivityForResult(intent, STOCKMAP_DATA)

            }

        }

        closeLL.setOnClickListener {
            finish()
        }

    }

    fun biotopesdataList(listdata: java.util.ArrayList<Biotope_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Biotope_attribute;

            model = Biotope_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                    data.getString(8), data.getFloat(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getFloat(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27), data.getFloat(28)
                    , data.getString(29), data.getString(30), data.getString(31), data.getFloat(32), data.getFloat(33), data.getFloat(34), data.getString(35)
                    , data.getString(36), data.getString(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41), data.getString(42)
                    , data.getFloat(43), data.getFloat(44), data.getString(45), data.getString(46), data.getString(47), data.getString(48), data.getDouble(49)
                    , data.getDouble(50), data.getString(51), data.getString(52), data.getString(53), data.getString(54), data.getString(55), data.getString(56), data.getString(57)
                    , data.getFloat(58), data.getFloat(59), data.getFloat(60), data.getFloat(61), data.getFloat(62), data.getFloat(63)
                    , data.getFloat(64), data.getFloat(65), data.getFloat(66), data.getFloat(67), data.getFloat(68), data.getFloat(69), data.getString(70), data.getFloat(71)
                    , data.getString(72), data.getString(73), data.getString(74), data.getInt(75), data.getInt(76), data.getInt(77), data.getInt(78)
            )
            listdata.add(model)
        }
    }

    fun birdsdataList(listdata: java.util.ArrayList<Birds_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Birds_attribute;

            model = ps_birds_attribute(data)

            listdata.add(model)
        }

    }

    fun reptiliasdataList(listdata: java.util.ArrayList<Reptilia_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Reptilia_attribute;

            model = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16),data.getInt(17), data.getInt(18), data.getInt(19), data.getString(20), data.getString(21), data.getString(22)
                    , data.getString(23), data.getString(24), data.getString(25), data.getInt(26), data.getInt(27), data.getInt(28), data.getFloat(29), data.getFloat(30),data.getString(31),data.getString(32),data.getString(33)
                    , data.getInt(34), data.getInt(35), data.getFloat(36), data.getInt(37), data.getInt(38), data.getFloat(39), data.getFloat(40), data.getString(41), data.getString(42)
            )

            listdata.add(model)
        }
    }

    fun mammalsdataList(listdata: java.util.ArrayList<Mammal_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Mammal_attribute;

            model = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17),data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26),data.getString(27),data.getString(28),data.getString(29),data.getString(30)
                    ,data.getInt(31), data.getInt(32),data.getFloat(33),data.getInt(34),data.getInt(35),data.getFloat(36),data.getString(37), data.getString(38), data.getString(39))

            listdata.add(model)
        }
    }

    fun fishsdataList(listdata: java.util.ArrayList<Fish_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Fish_attribute;

            model = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14), data.getString(15),
                    data.getFloat(16), data.getFloat(17), data.getString(18),data.getString(19), data.getInt(20), data.getInt(21), data.getInt(22), data.getInt(23), data.getString(24), data.getString(25),
                    data.getInt(26),data.getInt(27),data.getInt(28),data.getInt(29),data.getInt(30), data.getString(31), data.getInt(32), data.getString(33), data.getString(34), data.getString(35),
                    data.getInt(36), data.getString(37), data.getString(38), data.getString(39), data.getString(40), data.getString(41), data.getString(42),data.getInt(43),data.getInt(44),data.getFloat(45)
                    ,data.getInt(46), data.getInt(47), data.getFloat(48),data.getString(49), data.getString(50),data.getString(51),data.getString(52))

            listdata.add(model)

        }
    }

    fun insectsdataList(listdata: java.util.ArrayList<Insect_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Insect_attribute;

            model = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4)
                    , data.getString(5), data.getString(6), data.getString(7), data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11)
                    , data.getString(12), data.getString(13), data.getString(14), data.getString(15), data.getInt(16), data.getString(17)
                    , data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26)
                    , data.getInt(27), data.getInt(28), data.getFloat(29), data.getInt(30), data.getInt(31), data.getFloat(32),data.getString(33),data.getString(34))
            listdata.add(model)
        }
    }

    fun florasdataList(listdata: java.util.ArrayList<Flora_Attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Flora_Attribute;

            model = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                    , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25),data.getString(26)
                        , data.getInt(27), data.getInt(28), data.getFloat(29), data.getInt(30), data.getInt(31)
                    , data.getFloat(32),data.getString(33),data.getString(34))

            listdata.add(model)
        }
    }

    fun zoobenthossdataList(listdata: java.util.ArrayList<Zoobenthos_Attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Zoobenthos_Attribute;

            model = Zoobenthos_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                    data.getInt(8), data.getFloat(9), data.getInt(10), data.getInt(11), data.getFloat(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                    , data.getInt(22)
                    , data.getInt(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27)
                    , data.getString(28), data.getFloat(29), data.getFloat(30), data.getFloat(31), data.getFloat(32)
                    , data.getFloat(33), data.getFloat(34), data.getFloat(35), data.getFloat(36), data.getString(37)
                    , data.getString(38), data.getString(39), data.getString(40)
                    , data.getString(41), data.getString(42), data.getFloat(43), data.getFloat(44)
                    , data.getString(45), data.getString(46), data.getString(47), data.getString(48), data.getString(49)
                    , data.getString(20), data.getInt(51),data.getString(52),data.getString(53))

            listdata.add(model)
        }
    }

    fun manyflorasdataList(listdata: java.util.ArrayList<ManyFloraAttribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : ManyFloraAttribute;
            model = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                    data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getFloat(13), data.getString(14)
                    , data.getInt(15), data.getString(16), data.getString(17), data.getString(18), data.getFloat(19), data.getFloat(20), data.getFloat(21), data.getFloat(22)
                    , data.getString(23), data.getInt(24), data.getString(25), data.getString(26), data.getString(27), data.getFloat(28), data.getFloat(29), data.getFloat(30), data.getInt(31), data.getString(32)
                    , data.getString(33), data.getString(34), data.getString(35), data.getString(36), data.getFloat(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41)
                    , data.getString(42), data.getString(43),data.getString(44),data.getString(45),data.getString(46)
                    , data.getFloat(47), data.getFloat(48), data.getFloat(49), data.getFloat(50), data.getFloat(51), data.getFloat(52), data.getFloat(53)
                    , data.getFloat(54), data.getFloat(55), data.getFloat(56), data.getFloat(57), data.getFloat(58), data.getFloat(59), data.getFloat(60)
                    , data.getFloat(61), data.getFloat(62), data.getFloat(63), data.getFloat(64), data.getFloat(65), data.getFloat(66), data.getFloat(67)
                    , data.getFloat(68), data.getFloat(69), data.getFloat(70))

            listdata.add(model)
        }
    }

    fun stockmapsdataList(listdata: java.util.ArrayList<StockMap>, data: Cursor) {

        while (data.moveToNext()){

            var model : StockMap;

            model = StockMap(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                    data.getString(8), data.getString(9), data.getString(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getFloat(20), data.getFloat(21)
                    , data.getString(22),data.getString(23),data.getString(24), data.getString(25), data.getString(26), data.getString(27), data.getString(28), data.getString(29))

            listdata.add(model)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        val dataList: Array<String> = arrayOf("*");

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                BIOTOPE_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val biotopsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                        if(biotopeData != null){
                            biotopeData.clear()
                        }

                        biotopesdataList(biotopeData,biotopsdata)

                        listView1.adapter = biotopeAdaper

                        biotopeAdaper.notifyDataSetChanged()

                        biotopsdata.close()

                    }

                    if(data!!.getStringExtra("polygonid") != null){

                        val polygonid = data!!.getStringExtra("polygonid")

                        println("polygonid ----------------------------------$polygonid")

                        var intent = Intent()
                        intent.putExtra("polygonid", polygonid)
                        setResult(RESULT_OK, intent);

                        finish()

                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                BIRDS_DATA -> {
                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val birdsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(birdsData != null){
                                birdsData.clear()
                            }

                            birdsdataList(birdsData,birdsdata)

                            listView1.adapter = birdsAadapter

                            birdsAadapter.notifyDataSetChanged()

                            birdsdata.close()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                REPTILIA_DATA -> {
                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                                val reptiliasdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                                if(reptiliasData != null){
                                    reptiliasData.clear()
                                }

                                reptiliasdataList(reptiliasData,reptiliasdata)

                                listView1.adapter = reptiliaAdapter

                            reptiliaAdapter.notifyDataSetChanged()

                            reptiliasdata.close()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }


                }

                MAMMALIA_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val mammalsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(mammalsData != null){
                                mammalsData.clear()
                            }

                            mammalsdataList(mammalsData,mammalsdata)

                            listView1.adapter = mammalAdapter

                            mammalAdapter.notifyDataSetChanged()

                            mammalsdata.close()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                FISH_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val fishsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(fishsData != null){
                                fishsData.clear()
                            }

                            fishsdataList(fishsData,fishsdata)

                            listView1.adapter = fishAdapter

                            fishAdapter.notifyDataSetChanged()

                            fishsdata.close()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                INSECT_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val insectsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(insectData != null){
                                insectData.clear()
                            }

                            insectsdataList(insectData,insectsdata)

                            listView1.adapter = insectAdapter

                            insectAdapter.notifyDataSetChanged()

                            insectsdata.close()

                        }

                        if(data!!.getIntExtra("export" , 0) != null){
                            var intent = Intent()

                            val export = data!!.getIntExtra("export",0)

                            intent.putExtra("export",export)
                            setResult(RESULT_OK, intent)

                            finish()
                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                FLORA_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val florasdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(florasData != null){
                                florasData.clear()
                            }

                            florasdataList(florasData,florasdata)

                            listView1.adapter = floraAdapter

                            floraAdapter.notifyDataSetChanged()

                            florasdata.close()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }
                }

                ZOOBENTHOS_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val zoobenthoussdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(zoobenthosData != null){
                                zoobenthosData.clear()
                            }

                            zoobenthossdataList(zoobenthosData,zoobenthoussdata)

                            listView1.adapter = zoobenthousAdapter

                            zoobenthousAdapter.notifyDataSetChanged()

                            zoobenthoussdata.close()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                FLORA_DATA2 -> {
                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val dataList: Array<String> = arrayOf("*");

                            val manyFloraData = db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null)

                            if(manyflorasData != null){
                                manyflorasData.clear()
                            }

                            manyflorasdataList(manyflorasData,manyFloraData)

                            listView1.adapter = manyfloraAdapter

                            manyfloraAdapter.notifyDataSetChanged()

                            manyFloraData.close()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }
                }

                STOCKMAP_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val dataList: Array<String> = arrayOf("*");

                        val stockmapsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                        if(stocksData != null){
                            stocksData.clear()
                        }

                        stockmapsdataList(stocksData,stockmapsdata)

                        listView1.adapter = stockmapAdapter

                        stockmapAdapter.notifyDataSetChanged()

                        stockmapsdata.close()

                    }

                    if(data!!.getStringExtra("polygonid") != null){

                        val polygonid = data!!.getStringExtra("polygonid")

                        var intent = Intent()
                        intent.putExtra("polygonid", polygonid)
                        setResult(RESULT_OK, intent);

                        finish()

                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }
            }
        }
    }

    fun ps_birds_attribute(data: Cursor): Birds_attribute {
        var birds_attribute: Birds_attribute =Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                , data.getString(15), data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20)
                , data.getString(21), data.getString(22), data.getFloat(23), data.getFloat(24), data.getString(25), data.getString(26), data.getString(27)
                , data.getInt(28), data.getInt(29), data.getFloat(30), data.getInt(31), data.getInt(32), data.getFloat(33), data.getString(34), data.getString(35)
        )
        return birds_attribute
    }

}
