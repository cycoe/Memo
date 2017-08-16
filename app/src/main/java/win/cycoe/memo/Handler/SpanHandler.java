package win.cycoe.memo.Handler;

import android.text.Editable;
import android.widget.EditText;

/**
 * Created by cycoe on 17-8-16.
 */

public class SpanHandler {

    private final String[][] SPANLIST = {
            {"**", "**"},
            {"*", "*"},
            {"\n- ", ""},
            {"\n1. ", ""},
            {"~~", "~~"},
    };

    private EditText contentLine;
    private Editable contentEditable;
    private String content;
    private int start;
    private int end;

    public SpanHandler(EditText contentLine) {
        this.contentLine = contentLine;
    }

    public void insertSpan(int flag) {
        this.contentEditable = contentLine.getText();
        this.content = contentEditable.toString();
        start = contentLine.getSelectionStart();
        end = contentLine.getSelectionEnd();
        int lengthBefore = getStringBefore(flag);
        int lengthAfter = getStringAfter(flag);

        contentEditable.replace(start - lengthBefore, start, SPANLIST[flag][0]);
        contentEditable.replace(end + SPANLIST[flag][0].length() - lengthBefore,
                end + SPANLIST[flag][0].length() - lengthBefore + lengthAfter,
                SPANLIST[flag][1]);
        contentLine.setSelection(start + SPANLIST[flag][0].length() - lengthBefore,
                end + SPANLIST[flag][0].length() - lengthBefore);
    }

    public void insertUrl(String url) {
        this.contentEditable = contentLine.getText();
        this.content = contentEditable.toString();
        start = contentLine.getSelectionStart();
        end = contentLine.getSelectionEnd();
        int lengthBefore = getStringBefore();

        contentEditable.replace(start, end, url);
        contentEditable.replace(start - lengthBefore, start, "\n\n");
        contentLine.setSelection(start + 2 - lengthBefore, start + 2 - lengthBefore + url.length());

    }

    private int getStringBefore() {
        String subString = content.substring(start > 2 ? start - 2 : 0 , start);
        for(int i = subString.length(); i > 0; i--) {
            if("\n\n".contains(subString))
                return i;
            subString = subString.substring(1);
        }
        return 0;
    }

    private int getStringBefore(int flag) {
        String subString = content.substring(start > SPANLIST[flag][0].length() ? start - SPANLIST[flag][0].length() : 0 , start);
        for(int i = subString.length(); i > 0; i--) {
            if(SPANLIST[flag][0].contains(subString))
                return i;
            subString = subString.substring(1);
        }
        return 0;
    }

    private int getStringAfter(int flag) {
        String subString = content.substring(end, end + SPANLIST[flag][1].length() < content.length() ? end + SPANLIST[flag][1].length() : content.length());
        for(int i = subString.length(); i > 0; i--) {
            if(SPANLIST[flag][1].contains(subString))
                return i;
            subString = subString.substring(0, subString.length() - 1);
        }
        return 0;
    }

}
