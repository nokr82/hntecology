package hntecology.ecology.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.base.Utils
import hntecology.ecology.model.LayerModel
import org.json.JSONObject


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

        var layerData:LayerModel =  data.get(position)

        item.layerNameTV.text = layerData.layer_name

        item.layerNameTV.setOnClickListener {
            if(item.layerCheckCB.isChecked == false){
                item.layerCheckCB.isChecked = true
                notifyDataSetChanged()
            }else {
                item.layerCheckCB.isChecked = false
                notifyDataSetChanged()
            }

        }

        item.layerCheckCB.setOnClickListener {
            var checkBox:CheckBox = it as CheckBox
            var test = checkBox.isChecked.toString() + "-------------------------"
            println(test)

            layerData.is_checked = checkBox.isChecked
        }

        return retView
    }

    class ViewHolder(v: View) {

        var layerNameTV: TextView
        var layerCheckCB: CheckBox

        init {
            layerNameTV = v.findViewById<View>(R.id.layer_name) as TextView
            layerCheckCB = v.findViewById<View>(R.id.layer_checkbox) as CheckBox
        }
    }
}