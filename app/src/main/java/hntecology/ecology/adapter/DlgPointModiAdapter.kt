package hntecology.ecology.adapter

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.model.Biotope_attribute

class DlgPointModiAdapter(var context: Context, var itemList: ArrayList<Biotope_attribute>, var itemList2: ArrayList<Biotope_attribute>) : BaseAdapter() {

    var selectIndex: Int = 0;
    private lateinit var listdata1: java.util.ArrayList<Biotope_attribute>
    var dbManager: DataBaseHelper? = null

    var trespec = ArrayList<String>()
    var strespec = ArrayList<String>()
    var shrespec = ArrayList<String>()
    var herspec = ArrayList<String>()

    private class ViewHoldar(row: View?) {

        var dominTV: TextView
        var treTV: TextView
        var streTV: TextView
        var shrTV: TextView
        var herTV: TextView


        init {
            this.dominTV = row?.findViewById(R.id.dominTV) as TextView
            this.treTV = row?.findViewById(R.id.treTV) as TextView
            this.streTV = row?.findViewById(R.id.streTV) as TextView
            this.shrTV = row?.findViewById(R.id.shrTV) as TextView
            this.herTV = row?.findViewById(R.id.herTV) as TextView
        }
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view: View

        var viewHoldar: ViewHoldar
        if (convertView == null) {

            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.item_modi, parent, false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar


        } else {
            view = convertView
            viewHoldar = view.tag as ViewHoldar
        }
        Log.d("아이템리스트", itemList2.size.toString())


        var Biotope_attribute: Biotope_attribute = getItem(position)
        for (i in 0..itemList2.size - 1) {
            if (Biotope_attribute.DOMIN.toString() != "" && itemList2[i].DOMIN.toString() != "null") {
                if (Biotope_attribute.GROP_ID == itemList2[i].GROP_ID) {
                    viewHoldar.dominTV.text = itemList2[i].DOMIN;
                    if (itemList2[i].TRE_SPEC.toString() != "" && itemList2[i].TRE_SPEC.toString() != "null") {
                        trespec.add(itemList2[i].TRE_SPEC.toString())
                    }
                    if (itemList2[i].STRE_SPEC.toString() != "" && itemList2[i].STRE_SPEC.toString() != "null") {
                        strespec.add(itemList2[i].STRE_SPEC.toString())
                    }
                    if (itemList2[i].SHR_SPEC.toString() != "" && itemList2[i].SHR_SPEC.toString() != "null") {
                        shrespec.add(itemList2[i].SHR_SPEC.toString())
                    }
                    if (itemList2[i].HER_SPEC.toString() != "" && itemList2[i].HER_SPEC.toString() != "null") {
                        herspec.add(itemList2[i].HER_SPEC.toString())
                    }
                }
            }
        }
        viewHoldar.treTV.text = trespec.joinToString(",")
        viewHoldar.streTV.text = strespec.joinToString(",")
        viewHoldar.shrTV.text = shrespec.joinToString(",")
        viewHoldar.herTV.text = herspec.joinToString(",")
        trespec.clear()
        strespec.clear()
        shrespec.clear()
        herspec.clear()



        return view as View
    }

    override fun getItem(position: Int): Biotope_attribute {

        return itemList2.get(position)
    }

    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getCount(): Int {

        return itemList2.count()
    }

    fun clearItem() {
        itemList2.clear()
        itemList.clear();
        notifyDataSetChanged();
    }

    fun addItem(Biotope_attribute: Biotope_attribute) {
        itemList2.add(Biotope_attribute)
        itemList.add(Biotope_attribute);
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {

        itemList2.removeAt(position)
        notifyDataSetChanged()

    }


}