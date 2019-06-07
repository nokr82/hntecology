package hntecology.ecology.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.model.Fish_attribute

class DlgPointModiFishAdapter(var context: Context, var itemList: ArrayList<Fish_attribute>, var itemList2: ArrayList<Fish_attribute>) : BaseAdapter() {

    var selectIndex: Int = 0;
    var dbManager: DataBaseHelper? = null

    var title_list = ArrayList<String>()


    private class ViewHoldar(row: View?) {

        var timeTV: TextView
        var titleTV: TextView


        init {
            this.timeTV = row?.findViewById(R.id.timeTV) as TextView
            this.titleTV = row?.findViewById(R.id.titleTV) as TextView

        }
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view: View

        var viewHoldar: ViewHoldar
        if (convertView == null) {

            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.item_point_modi, parent, false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar


        } else {
            view = convertView
            viewHoldar = view.tag as ViewHoldar
        }
        Log.d("아이템리스트", itemList2.size.toString())

        var Fish_attribute: Fish_attribute = getItem(position)
            viewHoldar.timeTV.text = Fish_attribute.INV_DT+"\n"+Fish_attribute.INV_TM;
            for (i in 0..itemList2.size - 1) {
                if (Fish_attribute.GROP_ID == itemList2[i].GROP_ID) {
                    if (itemList2[i].SPEC_NM.toString() != "" && itemList2[i].SPEC_NM.toString() != "null") {
                        title_list.add(itemList2[i].SPEC_NM.toString())
                    }
                }
            }

        viewHoldar.titleTV.text = title_list.joinToString(",")
        title_list.clear()



        return view as View
    }

    override fun getItem(position: Int): Fish_attribute {

        return itemList.get(position)
    }

    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getCount(): Int {

        return itemList.count()
    }

    fun clearItem() {
        itemList.clear();
        notifyDataSetChanged();
    }

    fun addItem(Biotope_attribute: Fish_attribute) {
        itemList.add(Biotope_attribute);
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {

        itemList.removeAt(position)
        notifyDataSetChanged()

    }


}