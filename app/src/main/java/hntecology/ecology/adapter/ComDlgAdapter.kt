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


class ComDlgAdapter(var context: Context, var itemList : ArrayList<String>) : BaseAdapter() {


    private class ViewHoldar(row:View?){

        var biotope_item : TextView
        var search1_item:LinearLayout


        init {
            this.biotope_item = row?.findViewById(R.id.biotope_item) as TextView
            this.search1_item = row?.findViewById(R.id.search1_item) as LinearLayout
        }
    }


    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        var view : View

        var viewHoldar : ViewHoldar
        if (convertView == null){

            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.item_area_search1,parent,false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar


        }else {
            view =  convertView
            viewHoldar = view.tag as ViewHoldar
        }

        var name : String = getItem(position)

        viewHoldar.biotope_item.text = name;



        return view as View
    }

    override fun getItem(position: Int): String {

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
    fun addItem(String: String){

        itemList.add(String);
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){

        itemList.removeAt(position)
        notifyDataSetChanged()

    }



}