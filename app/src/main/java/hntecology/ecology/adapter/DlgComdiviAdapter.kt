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
import hntecology.ecology.model.CommonDivision

class DlgComdiviAdapter(var context: Context, var itemList : ArrayList<CommonDivision>) : BaseAdapter() {

    var selectIndex:Int = 0;


    private class ViewHoldar(row:View?){

        var biotope_item : TextView
        var search1_item:LinearLayout


        init {
            this.biotope_item = row?.findViewById(R.id.biotope_item) as TextView
            this.search1_item = row?.findViewById(R.id.search1_item) as LinearLayout
        }
    }


    override fun getView(position: Int, convertView: View?, parent : ViewGroup?): View {

        var view : View

        var viewHoldar : ViewHoldar
        if (convertView == null){

            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.item_area_search1,parent,false)
            viewHoldar = ViewHoldar(view)
            view.tag = viewHoldar


        }else {
            view =  convertView
            viewHoldar = view.tag as ViewHoldar
        }

        var CommonDivision : CommonDivision = getItem(position)

        viewHoldar.biotope_item.text = CommonDivision.title;

        if(CommonDivision.chkSelect == true){

            viewHoldar.search1_item.setBackgroundColor(Color.parseColor("#004baa"))
            viewHoldar.biotope_item.setTextColor(Color.parseColor("#FFFFFF"));

        } else {

            viewHoldar.search1_item.setBackgroundColor(Color.parseColor("#FFFFFF"))
            viewHoldar.biotope_item.setTextColor(Color.parseColor("#000000"));
        }

        // println("hh : " + position + ", " + hashCode())
        return view as View
    }

    override fun getItem(position: Int): CommonDivision {

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
    fun addItem(CommonDivision: CommonDivision){

        itemList.add(CommonDivision);
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

        var CommonDivision:CommonDivision = itemList.get(selectIndex);
        CommonDivision.chkSelect = false
        itemList.set(selectIndex,CommonDivision)

        var biotopeModel = getItem(position);

        biotopeModel.chkSelect = true
        selectIndex= position
        itemList.set(position,biotopeModel)
        notifyDataSetChanged()
    }




}