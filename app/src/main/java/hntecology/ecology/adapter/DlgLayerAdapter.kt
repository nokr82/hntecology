package hntecology.ecology.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.model.LayerModel


open class DlgLayerAdapter(context:Context, view:Int, data:ArrayList<LayerModel>) : ArrayAdapter<LayerModel>(context, view, data) {

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<LayerModel> = data

    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        lateinit var retView: View

        if (convertView == null) {
            retView = View.inflate(context, view, null)
            item = ViewHolder(retView)
            retView.tag = item
        } else {
            retView = convertView
            item = convertView.tag as ViewHolder
            if (item == null) {
                retView = View.inflate(context, view, null)
                item = ViewHolder(retView)
                retView.tag = item
            }
        }

        var data:LayerModel =  data.get(position)

        item.layerNameTV.text = data.layer_name

        return retView
    }

    class ViewHolder(v: View) {

        var layerNameTV: TextView

        init {
            layerNameTV = v.findViewById<View>(R.id.layer_name) as TextView
        }
    }
}