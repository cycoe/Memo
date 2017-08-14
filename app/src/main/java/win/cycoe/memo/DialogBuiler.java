package win.cycoe.memo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

/**
 * Created by cycoe on 17-8-13.
 */

public class DialogBuiler {

    private AlertDialog.Builder builder;

    public DialogBuiler(Context context){
        builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.tip);
    }

    public void createDialog(String message,
                             DialogInterface.OnClickListener positiveListener,
                             DialogInterface.OnClickListener negativeListener) {

        /**
         * 1. setMessage: set the massage to show
         * 2. setPositiveButton(buttonString, clickListener)
         * 3. setNegativeButton(buttonString, clickListener)
         * 4. show(): remember to show dialog
         */
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes,positiveListener);
        builder.setNegativeButton(R.string.no, negativeListener);
        builder.show();
    }

}
