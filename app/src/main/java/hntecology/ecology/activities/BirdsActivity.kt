package hntecology.ecology.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.OpenAlertDialog
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_birds.*

class BirdsActivity : Activity(), OnLocationUpdatedListener {

    lateinit var openAlertDialog: OpenAlertDialog;

    lateinit var context: Context;

    val SET_DATA1 = 1;
    val SET_DATA2 = 2;
    val SET_DATA3 = 3;
    val SET_BIRDS = 4;

    var userName = "";

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101

    var latitude = 0.0f;
    var longitude = 0.0f;

    private var progressDialog: ProgressDialog? = null

    var type = "write";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birds)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        var today = Utils.todayStr();
        var time = Utils.timeStr();

        invDtTV.text = today;
        timeTV.text = time;

        userName = PrefUtils.getStringPreference(context, "name");
        invPersonTV.text = userName;

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        val num = dataBaseHelper.birdsNextNum();
        numTV.text = num.toString()

        cancelBtn.setOnClickListener {
            finish()
        }

        saveBtn.setOnClickListener{
            type = "save";
            finish()
        }

        delBtn.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        type = "del";
                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        btn1.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "날씨")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 1);

            startActivityForResult(intent, SET_DATA1);

        }

        btn2.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "바람")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 2);

            startActivityForResult(intent, SET_DATA2);

        }

        btn3.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "풍향")
            intent.putExtra("DlgHeight", 500f);
            intent.putExtra("selectDlg", 3);

            startActivityForResult(intent, SET_DATA3);

        }

        birdsTV.setOnClickListener {
            startDlgBirds()
        }

        familyNameTV.setOnClickListener {
            startDlgBirds()
        }

        zoologicalTV.setOnClickListener {
            startDlgBirds()
        }

        // 이용 층위
        useLayerTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("교목층");
            listItems.add("아교목층");
            listItems.add("관목층");
            listItems.add("초본층");
            listItems.add("수면");
            listItems.add("수변");
            listItems.add("취소");

            alert(listItems, "이용 층위 선택", useLayerTV, "useLayer");

        }

        // 주요 행위
        mjActTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("채이");
            listItems.add("휴식");
            listItems.add("Song");
            listItems.add("Call");
            listItems.add("Flying");
            listItems.add("Pass");
            listItems.add("목욕");
            listItems.add("물먹기");
            listItems.add("번식 및 번식행동");
            listItems.add("취소");

            alert(listItems, "주요 행위 선택", mjActTV, "mjAct");

        }

        // 이용 대상
        useTarTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("수종명 기록");
            listItems.add("흙");
            listItems.add("물");
            listItems.add("인공물");
            listItems.add("취소");

            alert(listItems, "이용 대상 선택", useTarTV, "useTar");

        }

        initGPS();


    }

    fun startDlgBirds(){
        val intent = Intent(context, DlgBirdsActivity::class.java)
        startActivityForResult(intent, SET_BIRDS);
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

            // 주요행동
            if ("mjAct" == type) {
                if (selectItem == "번식 및 번식행동") {
                    mjActPrLL.visibility = View.VISIBLE
                } else {
                    mjActPrLL.visibility = View.GONE
                    mjActPrET.setText("");
                }
            } else if ("useTar" == type) {
                if (selectItem == "수종명 기록") {
                    useTarSpLL.visibility = View.VISIBLE
                } else {
                    useTarSpLL.visibility = View.GONE
                    useTarSpET.setText("");
                }
            }

        })

        builder.show();
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_DATA1 -> {

                    btn1.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_DATA2 -> {

                    btn2.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_DATA3 -> {

                    btn3.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_BIRDS -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    birdsTV.text = name
                    familyNameTV.text = family_name
                    zoologicalTV.text = zoological

                };
            }
        }
    }

    override fun onLocationUpdated(p0: Location?) {

        stopLocation()

        if (p0 != null) {

            latitude = p0.getLatitude().toFloat()
            longitude = p0.getLongitude().toFloat()

            var str = latitude.toString() + " / " + longitude.toString()

            gpsTV.text = str

            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        }

    }


    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
    }

    override fun finish() {

        if (type == "write") {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener {

                        dialog, id -> dialog.cancel()
                        super.finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        } else {
            super.finish()
        }

    }

}
