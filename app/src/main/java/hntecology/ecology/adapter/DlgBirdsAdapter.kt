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
import hntecology.ecology.model.*

class DlgBirdsAdapter(var context: Context, var itemList : ArrayList<Birds>) : BaseAdapter() {

    var selectIndex:Int = 0;


    private class ViewHoldar(row:View?){

        var search_item : TextView
        var search_item_LL:LinearLayout

        init {
            this.search_item = row?.findViewById(R.id.search_item) as TextView
            this.search_item_LL = row?.findViewById(R.id.search_item_LL) as LinearLayout
        }
    }

    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        var view : View

        var viewHoldar : ViewHoldar
        if (convertView == null){

            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.item_search,parent,false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar


        }else {
            view =  convertView
            viewHoldar = view.tag as ViewHoldar
        }

        var Birds : Birds = getItem(position)

        viewHoldar.search_item.text = Birds.name_kr;

        if(Birds.chkSelect == true){

            viewHoldar.search_item_LL.setBackgroundColor(Color.parseColor("#004baa"))
            viewHoldar.search_item.setTextColor(Color.parseColor("#FFFFFF"));

        } else {

            viewHoldar.search_item_LL.setBackgroundColor(Color.parseColor("#FFFFFF"))
            viewHoldar.search_item.setTextColor(Color.parseColor("#000000"));
        }

        println("hh : " + position + ", " + hashCode())
        return view as View
    }

    override fun getItem(position: Int): Birds {

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
    fun addItem(Birds: Birds){

        itemList.add(Birds);
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

        var Birds:Birds = itemList.get(selectIndex);
        Birds.chkSelect = false
        itemList.set(selectIndex,Birds)

        var biotopeModel = getItem(position);

        biotopeModel.chkSelect = true
        selectIndex= position
        itemList.set(position,biotopeModel)
        notifyDataSetChanged()
    }




}