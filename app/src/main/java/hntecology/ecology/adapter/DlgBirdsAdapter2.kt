package hntecology.ecology.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.model.*

class DlgBirdsAdapter2(var context: Context, var itemList : ArrayList<EndangeredSelect>) : BaseAdapter() {

    var selectIndex:Int = 0;

    private class ViewHoldar(row:View?){

        var item_name : TextView
        var item_checkbox:CheckBox


        init {
            this.item_name = row?.findViewById(R.id.item_name) as TextView
            this.item_checkbox = row?.findViewById(R.id.item_checkbox) as CheckBox
        }
    }

    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        var view : View

        var viewHoldar : ViewHoldar
        if (convertView == null){

            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.item_class_select,parent,false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar

        }else {
            view =  convertView
            viewHoldar = view.tag as ViewHoldar
        }

        var EndangeredSelect : EndangeredSelect = getItem(position)

        viewHoldar.item_name.text = EndangeredSelect.SIGN + " : " +EndangeredSelect.CONTENT;
        viewHoldar.item_checkbox.isChecked = EndangeredSelect.is_checked

        viewHoldar.item_name.setOnClickListener {

            if(EndangeredSelect.is_checked == false){
                EndangeredSelect.is_checked = true
            }else {
                EndangeredSelect.is_checked =false
            }
            notifyDataSetChanged()
        }

        viewHoldar.item_checkbox.setOnClickListener {
            var checkBox:CheckBox = it as CheckBox
            var test = checkBox.isChecked.toString() + "-------------------------"

            EndangeredSelect.is_checked = checkBox.isChecked

            notifyDataSetChanged()
        }

        return view as View
    }

    fun setItemSelect(position: Int){
        //전체 선택값 초기화.
/*        for(i in 0.. (itemList.size-1)){

            var bioListModel:BiotopeModel = itemList.get(i);
            bioListModel.chkSelect = false
            itemList.set(i,bioListModel)
        }*/

        var EndangeredSelect:EndangeredSelect = itemList.get(selectIndex);
        EndangeredSelect.is_checked = false
        itemList.set(selectIndex,EndangeredSelect)

        var biotopeModel = getItem(position);

        biotopeModel.is_checked = true
        selectIndex= position
        itemList.set(position,biotopeModel)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): EndangeredSelect {

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
    fun addItem(EndangeredSelect: EndangeredSelect){

        itemList.add(EndangeredSelect);
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){

        itemList.removeAt(position)
        notifyDataSetChanged()

    }




}