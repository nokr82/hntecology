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
import hntecology.ecology.model.ManyFloraAttribute

class DlgModiFlora2Adapter(var context: Context, var itemList: ArrayList<ManyFloraAttribute>, var itemList2: ArrayList<ManyFloraAttribute>) : BaseAdapter() {

    var selectIndex: Int = 0;
    var dbManager: DataBaseHelper? = null

    var trespec = ArrayList<String>()
    var strespec = ArrayList<String>()
    var shrespec = ArrayList<String>()
    var herspec = ArrayList<String>()

    private class ViewHoldar(row: View?) {

        var dominTV: TextView
        var donumTV: TextView
        var treTV: TextView
        var streTV: TextView
        var shrTV: TextView
        var herTV: TextView


        init {
            this.dominTV = row?.findViewById(R.id.dominTV) as TextView
            this.donumTV = row?.findViewById(R.id.donumTV) as TextView
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
        Log.d("아이템리스트", itemList.size.toString())

        viewHoldar.donumTV.visibility = View.GONE
        var ManyFloraAttribute: ManyFloraAttribute = getItem(position)
        if (ManyFloraAttribute.DOMIN=="null"){
            viewHoldar.dominTV.text = ""
        }else{
            viewHoldar.dominTV.text = ManyFloraAttribute.DOMIN
        }

            for (j in 0..itemList2.size - 1) {
                if (ManyFloraAttribute.GROP_ID == itemList2[j].GROP_ID) {
                    if (itemList2[j].TRE_SPEC.toString() != "" && itemList2[j].TRE_SPEC.toString() != "null") {
                        trespec.add(itemList2[j].TRE_SPEC.toString())
                    }
                    if (itemList2[j].STRE_SPEC.toString() != "" && itemList2[j].STRE_SPEC.toString() != "null") {
                        strespec.add(itemList2[j].STRE_SPEC.toString())
                    }
                    if (itemList2[j].SHR_SPEC.toString() != "" && itemList2[j].SHR_SPEC.toString() != "null") {
                        shrespec.add(itemList2[j].SHR_SPEC.toString())
                    }
                    if (itemList2[j].HER_SPEC.toString() != "" && itemList2[j].HER_SPEC.toString() != "null") {
                        herspec.add(itemList2[j].HER_SPEC.toString())
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

    override fun getItem(position: Int): ManyFloraAttribute {

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

    fun addItem(ManyFloraAttribute: ManyFloraAttribute) {
        itemList.add(ManyFloraAttribute);
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {

        itemList2.removeAt(position)
        notifyDataSetChanged()

    }


}