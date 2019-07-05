package hntecology.ecology.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.item_address.view.*
import org.json.JSONObject
import java.io.IOException

class AddressAdapter(context:Context, view:Int,select:String, data:ArrayList<JSONObject>) : ArrayAdapter<JSONObject>(context, view, data) {

    private lateinit var item: ViewHolder
    var view:Int = view
    var select:String = select
    var data:ArrayList<JSONObject> = data

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

        val item: JSONObject =  data.get(position)

        val address = item.getJSONObject("address")
        if (select=="PARCLE"){
            try {
                retView.addreesTV.text = Utils.getString(address.getString("parcel"))+" "+Utils.getString(address.getString("bldnmdc"))
            }catch (e: IOException){
                e.printStackTrace();
            }
        }else{
            try {
                retView.addreesTV.text = Utils.getString(address.getString("road"))+" "+Utils.getString(address.getString("bldnm"))
            }catch (e: IOException){
                e.printStackTrace();
            }

        }


        return retView
    }

    class ViewHolder(v: View) {
        var addreesTV: TextView = v.findViewById<View>(R.id.addreesTV) as TextView
    }
}