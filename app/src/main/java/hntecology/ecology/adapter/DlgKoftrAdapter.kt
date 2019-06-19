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
import hntecology.ecology.model.Koftr_group

class DlgKoftrAdapter(var context: Context, var itemList : ArrayList<Koftr_group>) : BaseAdapter() {

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

        var Koftr_group : Koftr_group = getItem(position)

        viewHoldar.search_item.text = Koftr_group.code_name;


        return view as View
    }

    override fun getItem(position: Int): Koftr_group {

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
    fun addItem(Koftr_group: Koftr_group){

        itemList.add(Koftr_group);
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

        var Koftr_group:Koftr_group = itemList.get(selectIndex);
        itemList.set(selectIndex,Koftr_group)

        var biotopeModel = getItem(position);

        selectIndex= position
        itemList.set(position,biotopeModel)
        notifyDataSetChanged()
    }




}