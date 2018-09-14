package hntecology.ecology

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.ViewParent
import android.widget.Switch
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeModel
import hntecology.ecology.model.Biotope_attribute
import kotlinx.android.synthetic.main.activity_biotope.*
import java.text.SimpleDateFormat
import java.util.*
import android.view.WindowManager
import hntecology.ecology.R.style.BiotopeEditText
import org.locationtech.jts.geom.Dimension.P


class BiotopeActivity : Activity() {

    lateinit var biotopeModelList:Array<BiotopeModel>;




    val SET_DATA1 = 1;
    var keyId:String?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biotope)

        window.setGravity(Gravity.RIGHT);

        /*
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(getWindow().getAttributes())
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        */

        window.setLayout(Utils.dpToPx(700F).toInt(),WindowManager.LayoutParams.WRAP_CONTENT);

        etinvesDatetimeTV.text = getTime()


        val dbManager:DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        var intent:Intent = getIntent();

        if(intent.getSerializableExtra("id") !=null){

            keyId = intent.getStringExtra("id")
            val dataList:Array<String> = arrayOf("*");

            val data =  db.query("biotope_Attribute",dataList,"id = '"+keyId+"'",null,null,null,"",null);

            while (data.moveToNext()){


                var biotope_attribute:Biotope_attribute = Biotope_attribute(data.getString(0),data.getString(1),data.getString(2),data.getString(3),data.getInt(4),data.getString(5),data.getFloat(6),
                                                                            data.getFloat(7),data.getString(8),data.getString(9),data.getString(10),data.getFloat(11),data.getString(12),data.getString(13)
                                                                            ,data.getString(14),data.getString(15),data.getString(16),data.getString(17),data.getString(18),data.getString(19),data.getString(20)
                                                                            ,data.getFloat(21),data.getFloat(22),data.getFloat(23),data.getString(24),data.getFloat(25),data.getFloat(26),data.getFloat(27)
                                                                            ,data.getString(28),data.getFloat(29),data.getFloat(30),data.getString(31),data.getFloat(32),data.getFloat(33),data.getString(34)
                                                                            ,data.getString(35),data.getString(36),data.getString(37))

//                etinvesRegionET.text        = biotope_attribute.INVES_REGION
                etinvesRegionET.setText(biotope_attribute.INVES_REGION);
                tvinvestigatorTV.setText(biotope_attribute.INVESTIGATOR)
                etinvesDatetimeTV.setText(biotope_attribute.INVES_DATETIME)
                tvinvesIndexTV.setText(biotope_attribute.INVES_INDEX.toString())
                ETlumGroupNumET.setText(biotope_attribute.LUM_GROUP_NUM)
                etlumTypeRateET.setText(biotope_attribute.LUM_TYPE_RATE.toString())
                etstandardHeightET.setText(biotope_attribute.STANDARD_HEIGHT.toString())
                ETlcmGroupNumET.setText(biotope_attribute.LCM_GROUP_NUM)

                if(biotope_attribute.LUM_GROUP_NUM != null){

                    val dataSelectList:Array<String> = arrayOf("name");
                    val data =  db.query("biotopeM",dataList,"code = '"+biotope_attribute.LUM_GROUP_NUM+"'",null,null,null,"",null);


                    while (data.moveToNext()){

                        TVlumGroupNumTV.setText(data.getString(0))
                    }
                }

                if(biotope_attribute.LCM_GROUP_NUM !=null){

                    val dataSelectList:Array<String> = arrayOf("name");
                    val data =  db.query("biotopeS",dataList,"code = '"+biotope_attribute.LCM_GROUP_NUM+"'",null,null,null,"",null);


                    while (data.moveToNext()){

                        TVlcmGroupNumTV.setText(data.getString(0))
                    }
                }

                //투수
                if(biotope_attribute.LCM_TYPE == "P"){

                    etlcmTypepET.setText(biotope_attribute.LCM_TYPE)
                //불투수
                } else if (biotope_attribute.LCM_TYPE == "I"){

                    etlcmTypeiET.setText(biotope_attribute.LCM_TYPE)
                //녹지
                } else if ( biotope_attribute.LCM_TYPE == "G"){

                    etlcmTypegET.setText(biotope_attribute.LCM_TYPE)
                //수공간
                } else if (biotope_attribute.LCM_TYPE == "W"){

                    etlcmTypewET.setText(biotope_attribute.LCM_TYPE)
                }


                ETtypeMarkET.setText(biotope_attribute.TYPE_MARK)
                etgvRateET.setText(biotope_attribute.GV_RATE.toString())
                etgvStructureET.setText(biotope_attribute.GV_STRUCTURE)
                etdistReturnET.setText(biotope_attribute.DIST_RETURN)
                etrestorePotET.setText(biotope_attribute.RESTORE_POT)
                etcompIntactET.setText(biotope_attribute.COMP_INTACT)
                etvpIntactET.setText(biotope_attribute.VP_INTACT)
                etimpFormET.setText(biotope_attribute.IMP_FORM)
                etbreastDiaET.setText(biotope_attribute.BREAST_DIA)
                etfinalEstET.setText(biotope_attribute.FINAL_EST)
                ettreeSpeciesET.setText(biotope_attribute.TREE_SPECIES)
                ettreeHeightET.setText(biotope_attribute.TREE_HEIGHT.toString())
                ettreeBreastET.setText(biotope_attribute.TREE_BREAST.toString())
                ettreeCoveET.setText(biotope_attribute.TREE_COVE.toString())
                etsubTreeSpecET.setText(biotope_attribute.SUB_TREE_SPEC)
                etsubTreeHeightET.setText(biotope_attribute.SUB_TREE_HEIGHT.toString())
                etsubTreeBreastET.setText(biotope_attribute.SUB_TREE_BREAST.toString())
                etsubTreeCoverET.setText(biotope_attribute.SUB_TREE_COVER.toString())
                etshrubSpeciesET.setText(biotope_attribute.SHRUB_SPECIES)
                etshrubHeightET.setText(biotope_attribute.SHRUB_HEIGHT.toString())
                etshrubCoverET.setText(biotope_attribute.SHRUB_COVER.toString())
                etherbSpeciesET.setText(biotope_attribute.HERB_SPECIES)
                etherbHeightET.setText(biotope_attribute.HERB_HEIGHT.toString())
                etherbCoverET.setText(biotope_attribute.HERB_COVER.toString())
                etpictureFolderET.setText(biotope_attribute.PICTURE_FOLDER)
                etwildAniET.setText(biotope_attribute.WILD_ANI)
                etrepBiotopPotET.setText(biotope_attribute.REP_BIOTOP_POT)
                etunusualNoteET.setText(biotope_attribute.UNUSUAL_NOTE)
            }
        }

        //토지이용현황 분류 버튼  높이 450F
        btn_Dlg1.setOnClickListener {

            val intent = Intent(this,DlgCommonActivity::class.java)
            intent.putExtra("title","토지이용유형 분류기준")
            intent.putExtra("table","biotopeM")
            intent.putExtra("DlgHeight",450F);
//            startActivity(intent)
            startActivityForResult(intent, SET_DATA1);

        }
        //토지피복현황 분류 버튼 사이즈 높이 600F 줄 것.
        btn_Dlg2.setOnClickListener {

            val intent = Intent(this,DlgCommonActivity::class.java)
            intent.putExtra("title","토지피복현황 분류기준")
            intent.putExtra("table","biotopeS")
            intent.putExtra("DlgHeight",600F);
            startActivityForResult(intent, SET_DATA1);

        }
        //현존식생현황 분류 버튼
/*        btn_Dlg3.setOnClickListener {

            val intent = Intent(this,DlgCommonActivity::class.java)
            intent.putExtra("title","현존식생현황 분류기준")
            //intent.putExtra("table","biotopeM") 아직 코드 미정.
            startActivityForResult(intent, SET_DATA1);

        }*/
        //취소버튼
        btn_biotopCancle1.setOnClickListener {
            finish()
        }

        //sqlite 저장.
        btn_biotopSave1.setOnClickListener {


            var intent = Intent();
            val biotope_attribute:Biotope_attribute = Biotope_attribute(null,"","","",0,"",0f
                    ,0f,"","","",0f,"","","",""
                    ,"","","","","",0f,0f,0f,"",0f
                    ,0f,0f,"",0f,0f,"",0f,0f,""
                    ,"","","");


            biotope_attribute.INVES_REGION         =   etinvesRegionET.text.toString()
            biotope_attribute.INVESTIGATOR         =   tvinvestigatorTV.text.toString()
            biotope_attribute.INVES_DATETIME       =   etinvesDatetimeTV.text.toString()

            if(tvinvesIndexTV.text.toString() != ""){

                biotope_attribute.INVES_INDEX          =    tvinvesIndexTV.text.toString().toInt()
            }

            biotope_attribute.LUM_GROUP_NUM        =   ETlumGroupNumET.text.toString()

            if(etlumTypeRateET.text .toString() != ""){

                biotope_attribute.LUM_TYPE_RATE        =   Utils.getString(etlumTypeRateET).toFloat();
            }
            if(etstandardHeightET.text .toString() != ""){

                biotope_attribute.STANDARD_HEIGHT      =    Utils.getString(etstandardHeightET).toFloat();
            }


            biotope_attribute.LCM_GROUP_NUM        =   ETlcmGroupNumET.text.toString()
            biotope_attribute.TYPE_MARK            =   ETtypeMarkET.text.toString()

            if(etgvRateET.text .toString() != ""){

                biotope_attribute.GV_RATE              =  Utils.getString(etgvRateET).toFloat();
            }

            biotope_attribute.GV_STRUCTURE         =   etgvStructureET.text.toString()
            biotope_attribute.DIST_RETURN          =   etdistReturnET.text.toString()
            biotope_attribute.RESTORE_POT          =   etrestorePotET.text.toString()
            biotope_attribute.COMP_INTACT          =   etcompIntactET.text.toString()
            biotope_attribute.VP_INTACT            =   etvpIntactET.text.toString()
            biotope_attribute.IMP_FORM             =   etimpFormET.text.toString()
            biotope_attribute.BREAST_DIA           =   etbreastDiaET.text.toString()
            biotope_attribute.FINAL_EST            =   etfinalEstET.text.toString()
            biotope_attribute.TREE_SPECIES         =   ettreeSpeciesET.text.toString()

            if(ettreeHeightET.text .toString() != ""){

                biotope_attribute.TREE_HEIGHT          = Utils.getString(ettreeHeightET).toFloat();
            }

            if(ettreeBreastET.text .toString() != ""){

                biotope_attribute.TREE_BREAST          =   Utils.getString(ettreeBreastET).toFloat();

            }

            if(ettreeCoveET.text .toString() != ""){

                biotope_attribute.TREE_COVE            =   Utils.getString(ettreeCoveET).toFloat();

            }

            biotope_attribute.SUB_TREE_SPEC        =   etsubTreeSpecET.text.toString()

            if(etsubTreeHeightET.text .toString() != ""){

                biotope_attribute.SUB_TREE_HEIGHT      =   Utils.getString(etsubTreeHeightET).toFloat();
            }

            if(etsubTreeBreastET.text .toString() != ""){

                biotope_attribute.SUB_TREE_BREAST      =   Utils.getString(etsubTreeBreastET).toFloat();
            }

            if(etsubTreeCoverET.text .toString() != ""){

                biotope_attribute.SUB_TREE_COVER       =   Utils.getString(etsubTreeCoverET).toFloat();
            }

            biotope_attribute.SHRUB_SPECIES        =   etshrubSpeciesET.text.toString()

            if(etshrubHeightET.text .toString() != ""){

                biotope_attribute.SHRUB_HEIGHT         =   Utils.getString(etshrubHeightET).toFloat();
            }

            if(etshrubCoverET.text .toString() != ""){

                biotope_attribute.SHRUB_COVER          =   Utils.getString(etshrubCoverET).toFloat();
            }

            biotope_attribute.HERB_SPECIES         =   etherbSpeciesET.text.toString()

            if(etherbHeightET.text .toString() != ""){

                biotope_attribute.HERB_HEIGHT          =   Utils.getString(etherbHeightET).toFloat();

            }

            if(etherbCoverET.text .toString() != ""){

                biotope_attribute.HERB_COVER           =    Utils.getString(etherbCoverET).toFloat();
            }


            biotope_attribute.PICTURE_FOLDER       =   etpictureFolderET.text.toString()
            biotope_attribute.WILD_ANI             =   etwildAniET.text.toString()
            biotope_attribute.REP_BIOTOP_POT       =   etrepBiotopPotET.text.toString()
            biotope_attribute.UNUSUAL_NOTE         =   etunusualNoteET.text.toString()

            //투수
            if(etlcmTypepET.text.toString() != ""){

                biotope_attribute.LCM_TYPE = etlcmTypepET.text.toString()
                //불투수
            } else if (etlcmTypepET.text.toString() != ""){

                biotope_attribute.LCM_TYPE = etlcmTypeiET.text.toString()
                //녹지
            } else if ( etlcmTypepET.text.toString() != ""){

                biotope_attribute.LCM_TYPE = etlcmTypegET.text.toString()
                //수공간
            } else if (etlcmTypepET.text.toString() != ""){

                biotope_attribute.LCM_TYPE = etlcmTypewET.text.toString()
            }
            if(keyId != null){

                biotope_attribute.id = keyId
                dbManager.updatebiotope_attribute(biotope_attribute)
            }else {

                biotope_attribute.id = getAttrubuteKey();
                dbManager.insertbiotope_attribute(biotope_attribute);

            }
            intent.putExtra("bio_attri",biotope_attribute);

            setResult(RESULT_OK, intent);
            finish()
        }
    }


    fun getTime() :String{


        val date = Date()
        val fullTime = SimpleDateFormat("yyyy-MM-dd")


        return fullTime.format(date).toString()
    }
    fun createId():String{

        val date = Date()
        val fullTime = SimpleDateFormat("yyyyMMddHHmmssSSS")

        return fullTime.format(date).toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        var biotopeModel:BiotopeModel

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SET_DATA1 -> {

                    biotopeModel = data!!.getSerializableExtra("bioModel") as BiotopeModel

                    //토지이용현황
                    if(biotopeModel.codeType == "biotopeM"){

                        TVlumGroupNumTV.setText( biotopeModel.name)
                        ETlumGroupNumET.setText(biotopeModel.code)
                        //토지피복현황
                    }else if (biotopeModel.codeType == "biotopeS"){

                        TVlcmGroupNumTV.setText( biotopeModel.name)
                        ETlcmGroupNumET.setText(biotopeModel.code)

                        var bioModelParent:BiotopeModel
                        bioModelParent = data!!.getSerializableExtra("bioModelParent") as BiotopeModel

                        //불투수 투수
                        etlcmTypeiET.setText("");
                        etlcmTypepET.setText("")
                        etlcmTypegET.setText("")
                        etlcmTypewET.setText("")

                        //불투수
                        if(bioModelParent.code == "A"){

                            etlcmTypeiET.setText("I")
                        //투수
                        }else if(bioModelParent.code == "B"){

                            etlcmTypepET.setText("P")
                         //녹지
                        }else if(bioModelParent.code == "C"){

                            etlcmTypegET.setText("G")
                         //수공간
                        }else if(bioModelParent.code == "D"){

                            etlcmTypewET.setText("W")
                        }

                        //현존식생현황  아직 테이블 명 코드 미정
                    } else if (biotopeModel.codeType == "biotopeS"){

                        TVlcmGroupNumTV.setText( biotopeModel.name)
                        ETlcmGroupNumET.setText(biotopeModel.code)
                    }

                };
            }
        }
    }
    fun getAttrubuteKey():String{

        val time = System.currentTimeMillis()
        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val strDT = dayTime.format(Date(time))

        return strDT
    }

}