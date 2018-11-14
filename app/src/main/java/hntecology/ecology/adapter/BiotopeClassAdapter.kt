package hntecology.ecology.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.model.BiotopeClass
import hntecology.ecology.model.BiotopeModel
import hntecology.ecology.model.Vegetation

class BiotopeClassAdapter(var context: Context, var itemList : ArrayList<BiotopeClass>) : BaseAdapter() {

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

        var BiotopeClass : BiotopeClass = getItem(position)

        viewHoldar.class_biotope_item.text = BiotopeClass.middlecategory;

        if(BiotopeClass.chkSelect == true){

            viewHoldar.class_search1_item.setBackgroundColor(Color.parseColor("#004baa"))
            viewHoldar.class_biotope_item.setTextColor(Color.parseColor("#FFFFFF"));

        } else {

            viewHoldar.class_search1_item.setBackgroundColor(Color.parseColor("#FFFFFF"))
            viewHoldar.class_biotope_item.setTextColor(Color.parseColor("#000000"));
        }

        println("hh : " + position + ", " + hashCode())
        return view as View
    }

    override fun getItem(position: Int): BiotopeClass {

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
    fun addItem(BiotopeClass: BiotopeClass){

        itemList.add(BiotopeClass);
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

        var BiotopeClass:BiotopeClass = itemList.get(selectIndex);
        BiotopeClass.chkSelect = false
        itemList.set(selectIndex,BiotopeClass)

        var biotopeModel = getItem(position);

        biotopeModel.chkSelect = true
        selectIndex= position
        itemList.set(position,biotopeModel)
        notifyDataSetChanged()
    }




}