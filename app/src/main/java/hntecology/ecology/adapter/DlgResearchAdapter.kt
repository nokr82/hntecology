package hntecology.ecology.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.model.*

class DlgResearchAdapter(var context: Context, var itemList : ArrayList<Research>) : BaseAdapter() {

    var selectIndex:Int = 0;


    private class ViewHoldar(row:View?){

        var INV_REGIONTV : TextView
        var PRJ_NAME:TextView
        var INV_DT:TextView
        var INV_TM:TextView
        var CLASS_NAME:TextView
        var SPEC_NM:TextView
        var INDI_CNT:TextView
        var PICTURE_YN:TextView



        init {
            this.INV_REGIONTV = row?.findViewById(R.id.INV_REGIONTV) as TextView
            this.PRJ_NAME = row?.findViewById(R.id.PRJ_NAME) as TextView
            this.INV_DT = row?.findViewById(R.id.INV_DT) as TextView
            this.INV_TM = row?.findViewById(R.id.INV_TM) as TextView
            this.CLASS_NAME = row?.findViewById(R.id.CLASS_NAME) as TextView
            this.SPEC_NM = row?.findViewById(R.id.SPEC_NM) as TextView
            this.INDI_CNT = row?.findViewById(R.id.INDI_CNT) as TextView
            this.PICTURE_YN = row?.findViewById(R.id.PICTURE_YN) as TextView
        }
    }

    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        var view : View

        var viewHoldar : ViewHoldar
        if (convertView == null){

            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.item_research,parent,false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar


        }else {
            view =  convertView
            viewHoldar = view.tag as ViewHoldar
        }

        var Research : Research = getItem(position)

        viewHoldar.INV_REGIONTV.text = Research.INV_REGION;
        viewHoldar.PRJ_NAME.text = Research.PRJ_NAME;
        viewHoldar.INV_DT.text = Research.INV_DT;
        viewHoldar.INV_TM.text = Research.INV_TM;
        viewHoldar.CLASS_NAME.text = Research.CLASS_NAME;
        viewHoldar.SPEC_NM.text = Research.SPEC_NM;
        viewHoldar.INDI_CNT.text = Research.INDI_CNT;
        viewHoldar.PICTURE_YN.text = Research.PICTURE_YN;

        return view as View
    }

    override fun getItem(position: Int): Research {

        return itemList.get(position)
    }

    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getCount(): Int {

        return itemList.count()
    }

    fun clearItem(){

        itemList.clear();
        notifyDataSetChanged();
    }
    fun addItem(Research: Research){

        itemList.add(Research);
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){

        itemList.removeAt(position)
        notifyDataSetChanged()

    }

}