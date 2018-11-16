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
import hntecology.ecology.model.Insect_attribute
import hntecology.ecology.model.Mammal_attribute
import hntecology.ecology.model.Vegetation

class DataInsectAdapter(var context: Context, var itemList : ArrayList<Insect_attribute>) : BaseAdapter() {

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

        var Insect_attribute : Insect_attribute = getItem(position)

        viewHoldar.class_biotope_item.text = Insect_attribute.INV_DT + " / " + Insect_attribute.INV_TM

        println("hh : " + position + ", " + hashCode())
        return view as View
    }

    override fun getItem(position: Int): Insect_attribute {

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
    fun addItem(Insect_attribute: Insect_attribute){

        itemList.add(Insect_attribute);
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

        var Insect_attribute:Insect_attribute = itemList.get(selectIndex);
        itemList.set(selectIndex,Insect_attribute)

        var biotopeModel = getItem(position);

        selectIndex= position
        itemList.set(position,biotopeModel)
        notifyDataSetChanged()
    }




}