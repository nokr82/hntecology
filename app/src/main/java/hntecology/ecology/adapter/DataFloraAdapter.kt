package hntecology.ecology.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.model.Birds_attribute
import hntecology.ecology.model.Fish_attribute
import hntecology.ecology.model.Flora_Attribute
import hntecology.ecology.model.Vegetation

class DataFloraAdapter(var context: Context, var itemList : ArrayList<Flora_Attribute>) : BaseAdapter() {

    var selectIndex:Int = 0;


    private class ViewHoldar(row:View?){

        var class_biotope_item : TextView
        var class_search1_item:LinearLayout


        init {
            this.class_biotope_item = row?.findViewById(R.id.class_biotope_item) as TextView
            this.class_search1_item = row?.findViewById(R.id.class_search1_item) as LinearLayout
        }
    }


    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        var view : View

        var viewHoldar : ViewHoldar
        if (convertView == null){

            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.item_biotopeclass,parent,false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar


        }else {
            view =  convertView
            viewHoldar = view.tag as ViewHoldar
        }

        var Flora_Attribute : Flora_Attribute = getItem(position)

        if (Flora_Attribute.SPEC_NM != null && Flora_Attribute.SPEC_NM != ""){
            viewHoldar.class_biotope_item.text = Flora_Attribute.INV_DT  + " / " + Flora_Attribute.INV_TM + " / " + Flora_Attribute.SPEC_NM + " / " + Flora_Attribute.COL_IN_CNT.toString()
        } else {
            viewHoldar.class_biotope_item.text = Flora_Attribute.INV_DT  + " / " + Flora_Attribute.INV_TM
        }

        return view as View
    }

    override fun getItem(position: Int): Flora_Attribute {

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
    fun addItem(Flora_Attribute: Flora_Attribute){

        itemList.add(Flora_Attribute);
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){

        itemList.removeAt(position)
        notifyDataSetChanged()

    }

    fun setItemSelect(position: Int){
        //전체 선택값 초기화.
/*        for(i in 0.. (itemList.size-1)){

            var bioListModel:BiotopeModel = itemList.get(i);
            bioListModel.chkSelect = false
            itemList.set(i,bioListModel)
        }*/

        var Flora_Attribute:Flora_Attribute = itemList.get(selectIndex);
        itemList.set(selectIndex,Flora_Attribute)

        var flora_Attribute = getItem(position);

        selectIndex= position
        itemList.set(position,flora_Attribute)
        notifyDataSetChanged()
    }




}