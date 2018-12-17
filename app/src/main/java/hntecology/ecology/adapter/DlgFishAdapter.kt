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
import hntecology.ecology.model.Birds
import hntecology.ecology.model.Fishs

class DlgFishAdapter(var context: Context, var itemList : ArrayList<Fishs>) : BaseAdapter() {

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

        var Fishs : Fishs = getItem(position)

        viewHoldar.search_item.text = Fishs.name_kr;

        if(Fishs.chkSelect == true){

            viewHoldar.search_item_LL.setBackgroundColor(Color.parseColor("#004baa"))
            viewHoldar.search_item.setTextColor(Color.parseColor("#FFFFFF"));

        } else {

            viewHoldar.search_item_LL.setBackgroundColor(Color.parseColor("#FFFFFF"))
            viewHoldar.search_item.setTextColor(Color.parseColor("#000000"));
        }

        // println("hh : " + position + ", " + hashCode())
        return view as View
    }

    override fun getItem(position: Int): Fishs {

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
    fun addItem(Fishs: Fishs){

        itemList.add(Fishs);
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

        var Fishs:Fishs = itemList.get(selectIndex);
        Fishs.chkSelect = false
        itemList.set(selectIndex,Fishs)

        var biotopeModel = getItem(position);

        biotopeModel.chkSelect = true
        selectIndex= position
        itemList.set(position,biotopeModel)
        notifyDataSetChanged()
    }




}