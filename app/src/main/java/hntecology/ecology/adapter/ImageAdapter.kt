package hntecology.ecology.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.joooonho.SelectableRoundedImageView
import hntecology.ecology.R
import hntecology.ecology.base.ImageLoader
import hntecology.ecology.model.PhotoData
import java.util.*

class ImageAdapter(var context:Context,  var itemList:ArrayList<PhotoData>, var imageLoader:ImageLoader, var selected:LinkedList<String>) : BaseAdapter() {


    private class ViewHoldar(row: View?){

        var imageIV: SelectableRoundedImageView? = null
        var numberTV: TextView? = null
//        var cameraLL: LinearLayout? = null
        var selectedLL: LinearLayout? = null
        var imageRL: RelativeLayout? = null
        var number: String? = null
        var selected: Boolean = false
        var photoPath: String? = null

        init {


            this.imageIV = row!!.findViewById(R.id.imageIV) as SelectableRoundedImageView
            this.numberTV = row!!.findViewById(R.id.numberTV) as TextView
//            this.cameraLL = row!!.findViewById(R.id.cameraLL) as LinearLayout
            this.selectedLL = row!!.findViewById(R.id.selectedLL) as LinearLayout
            this.imageRL = row!!.findViewById(R.id.imageRL) as RelativeLayout
        }
    }


    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        var view : View

        var viewHoldar : ViewHoldar
        if (convertView == null){

            view = View.inflate(this.context, R.layout.item_write_album, null)
            var layout = LayoutInflater.from(context)
//            view = layout.inflate(R.layout.item_area_search1,parent,false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar


        }else {
            view =  convertView
            viewHoldar = view.tag as ViewHoldar
        }
        val photo:PhotoData = itemList.get(position)

        val photoID = photo.photoID
        if (photoID == -1) {
//            viewHoldar.cameraLL!!.setVisibility(View.VISIBLE)
            viewHoldar.imageRL!!.setVisibility(View.GONE)
        } else {
//            viewHoldar.cameraLL!!.setVisibility(View.GONE)
            viewHoldar.imageRL!!.setVisibility(View.VISIBLE)

            if (selected.contains(position.toString())) {
                val idx = selected.indexOf(position.toString())
                viewHoldar.numberTV!!.setText((idx + 1).toString())
                viewHoldar.numberTV!!.setVisibility(View.VISIBLE)
                viewHoldar.selectedLL!!.setVisibility(View.VISIBLE)
            } else {
                viewHoldar.numberTV!!.setText("")
                viewHoldar.numberTV!!.setVisibility(View.GONE)
                viewHoldar.selectedLL!!.setVisibility(View.GONE)
            }
            viewHoldar.photoPath = photo.photoPath

            viewHoldar.imageIV!!.setImageBitmap(imageLoader.getImage(photo.photoID!!, photo.photoPath, photo.orientation!!))
        }


        return view as View
    }

    override fun getItem(position: Int): Objects? {

        return null;
    }

    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getCount(): Int {

        return itemList.count()
    }


}