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
import hntecology.ecology.model.Vegetation

class DlgVegetationAdapter(var context: Context, var itemList : ArrayList<Vegetation>) : BaseAdapter() {

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

        var Vegetation : Vegetation = getItem(position)

        viewHoldar.search_item.text = Vegetation.CORRESPONDINGNAME;


        return view as View
    }

    override fun getItem(position: Int): Vegetation {

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
    fun addItem(Vegetation: Vegetation){

        itemList.add(Vegetation);
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){

        itemList.removeAt(position)
        notifyDataSetChanged()

    }

    fun setItemSelect(position: Int){


        var Vegetation:Vegetation = itemList.get(selectIndex);
        itemList.set(selectIndex,Vegetation)

        var biotopeModel = getItem(position);

        selectIndex= position
        itemList.set(position,biotopeModel)
        notifyDataSetChanged()
    }




}