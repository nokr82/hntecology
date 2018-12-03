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
import hntecology.ecology.model.ZoobenthosSelect
import org.json.JSONObject


open class DlgZoobenthosAdapter(context:Context, view:Int, data:ArrayList<ZoobenthosSelect>) : ArrayAdapter<ZoobenthosSelect>(context, view, data) {

    private lateinit var item: ViewHolder
    var view:Int = view
    var data:ArrayList<ZoobenthosSelect> = data

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

        var ZoobenthosSelect:ZoobenthosSelect =  data.get(position)

        item.layerNameTV.text = ZoobenthosSelect.title
        item.layerCheckCB.isChecked = ZoobenthosSelect.chkSelect

        item.layerNameTV.setOnClickListener {

            if(ZoobenthosSelect.chkSelect == false){
                ZoobenthosSelect.chkSelect = true
            }else {
                ZoobenthosSelect.chkSelect = false
            }

            notifyDataSetChanged()

        }

        item.layerCheckCB.setOnClickListener {
            var checkBox:CheckBox = it as CheckBox
            var test = checkBox.isChecked.toString() + "-------------------------"
            println(test)

            ZoobenthosSelect.chkSelect = checkBox.isChecked

            notifyDataSetChanged()
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