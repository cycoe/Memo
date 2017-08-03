package win.cycoe.memo;

import android.app.Notification;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.EditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by cycoe on 8/3/17.
 */

public class SpannedEditText extends EditText {


    private Map<String, Object> SPANMAP;

    private SpannableString spannedText;

    StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);


    public SpannedEditText(Context context) {
        super(context);

    }


    private void initSpanMap() {
        SPANMAP = new HashMap<String, Object>();
        SPANMAP.put("test", boldSpan);
    }


    public void setSpannedText(String content, Map<String, int[]> spanPosition) {

        spannedText = new SpannableString(content);
        for(int i = 0; i < spanPosition.size(); i++) {
            for(int j = 0; j < spanPosition.get(i).length / 2; j++) {
                spannedText.setSpan(SPANMAP.get("test"), spanPosition.get(i)[j], spanPosition.get(i)[j], Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }

        setText(spannedText);
    }

    public int[][] getSpanPosition() {
        spannedText.getSpanStart()
    }
}
