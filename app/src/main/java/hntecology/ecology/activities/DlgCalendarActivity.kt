package hntecology.ecology.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_dlg_calendar.*
import java.util.*

class DlgCalendarActivity : Activity() {

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    private lateinit var context: Context;

    val LEFTTIME = 1000
    val RIGHTTIME = 1001

    internal var locale = Locale("ko")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_calendar)

        context = applicationContext;

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        val intent = getIntent()

        window.setLayout(Utils.dpToPx(640F).toInt(), Utils.dpToPx(400F).toInt());
        this.setFinishOnTouchOutside(true);

        val today = Utils.todayStr()
        val time = Utils.timeStr()
        val timesplit = time.split(":")

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

                val newDate:Calendar =Calendar.getInstance()
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

                val newDate:Calendar =Calendar.getInstance()
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
            val leftday = leftdayTV.text.toString()
            val lefttime = lefttimeTV.text.toString()
            val rightday = rightdayTV.text.toString()
            val righttime = righttimeTV.text.toString()

            var intent = Intent()
            intent.putExtra("leftday",leftday)
            intent.putExtra("lefttime",lefttime)
            intent.putExtra("rightday",rightday)
            intent.putExtra("righttime",righttime)

            setResult(RESULT_OK,intent)
            finish()
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

    fun onDateSet(view: DlgCalendarActivity, year: Int, monthOfYear: Int, dayOfMonth: Int, type:String) {
        if (type == "l"){
            leftdayTV.setText(year.toString() + "-" + monthOfYear.toString() + "-" + dayOfMonth.toString() + " ")
        } else {
            rightdayTV.setText(year.toString() + "-" + monthOfYear.toString() + "-" + dayOfMonth.toString() + " ")
        }
    }
}
