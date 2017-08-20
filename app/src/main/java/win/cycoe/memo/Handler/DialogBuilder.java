package win.cycoe.memo.Handler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import win.cycoe.memo.R;

/**
 * Created by cycoe on 17-8-13.
 */

public class DialogBuilder {

    private AlertDialog.Builder builder;

    public DialogBuilder(Context context, int theme){
        builder = new AlertDialog.Builder(context, theme);
    }

    public void createDialog(String title,
                             String message,
                             DialogInterface.OnClickListener positiveListener,
                             DialogInterface.OnClickListener negativeListener,
                             View view) {

        /**
         * 1. setMessage: set the massage to show
         * 2. setPositiveButton(buttonString, clickListener)
         * 3. setNegativeButton(buttonString, clickListener)
         * 4. show(): remember to show dialog
         */
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes,positiveListener);
        builder.setNegativeButton(R.string.no, negativeListener);
        builder.setView(view);

        builder.show();
    }

    public void createDialog(String title,
                             String message,
                             DialogInterface.OnClickListener positiveListener,
                             DialogInterface.OnClickListener negativeListener) {

        /**
         * 1. setMessage: set the massage to show
         * 2. setPositiveButton(buttonString, clickListener)
         * 3. setNegativeButton(buttonString, clickListener)
         * 4. show(): remember to show dialog
         */
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes,positiveListener);
        builder.setNegativeButton(R.string.no, negativeListener);

        builder.show();
    }

    public void createDialog(String title, String message) {

        /**
         * 1. setMessage: set the massage to show
         * 2. setPositiveButton(buttonString, clickListener)
         * 3. setNegativeButton(buttonString, clickListener)
         * 4. show(): remember to show dialog
         */
        builder.setTitle(title);
        builder.setMessage(message);

        builder.show();
    }

}
