package hntecology.ecology.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog that allows to stay open even if the buttons are clicked
 * 
 * @author plahoda
 * 
 */
public class OpenAlertDialog extends AlertDialog {

    private boolean stayOpen = true;

    public OpenAlertDialog(Context context) {
        super(context);
    }

    @Override
    public void dismiss() {
        // eat the original dismiss code if the stay open flag is set to true
        if (stayOpen == false) {
            super.dismiss();
        }
        // Original code (2.2)
        // if (Thread.currentThread() != mUiThread) {
        // mHandler.post(mDismissAction);
        // } else {
        // mDismissAction.run();
        // }
    }

    /**
     * @param stayOpen
     *            the stayOpen to set
     */
    public void setStayOpen(boolean stayOpen) {
        this.stayOpen = stayOpen;
    }

    /**
     * @return the stayOpen
     */
    public boolean isStayOpen() {
        return stayOpen;
    }


    //select 1. 날씨 , 2. 바람 , 3.풍향
   public String show(Context context, int select)
    {
        final List<String> ListItems = new ArrayList<>();
         String[] str = {"선택안함"};

        switch (select){

            case 1 :

                ListItems.add("맑음");
                ListItems.add("흐림");
                ListItems.add("안개");
                ListItems.add("비");
                break;

            case 2 :

                ListItems.add("강");
                ListItems.add("중");
                ListItems.add("약");
                ListItems.add("무");
                break;

            case 3 :


                ListItems.add("N");
                ListItems.add("NE");
                ListItems.add("E");
                ListItems.add("SE");
                ListItems.add("S");
                ListItems.add("SW");
                ListItems.add("W");
                ListItems.add("NW");

                break;
        }

        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("AlertDialog Title");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                String selectedText = items[pos].toString();
                Toast.makeText(context, selectedText, Toast.LENGTH_SHORT).show();
                str[0] = items[pos].toString();

            }
        });
        builder.show();

        return str[0];
    }

}
