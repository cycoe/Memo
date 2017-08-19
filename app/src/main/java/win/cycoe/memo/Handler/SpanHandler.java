package win.cycoe.memo.Handler;

import android.text.Editable;
import android.widget.EditText;

/**
 * Created by cycoe on 17-8-16.
 */

public class SpanHandler {

    private final String[][] SPANLIST = {
            {"", "**", "**"},
            {"", "*", "*"},
            {"\n", "- ", ""},
            {"\n", "1. ", ""},
            {"", "~~", "~~"},
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
        contentEditable = contentLine.getText();
        content = contentEditable.toString();
        start = contentLine.getSelectionStart();
        end = contentLine.getSelectionEnd();

        int lengthBefore = getStringBefore(SPANLIST[flag][0]);
        int insertBeforeLen;
        if(start != lengthBefore) {
            contentEditable.replace(start - lengthBefore, start, SPANLIST[flag][0]);
            insertBeforeLen = SPANLIST[flag][0].length() - lengthBefore;
        }
        else
            insertBeforeLen = 0;
        contentEditable.insert(start + insertBeforeLen, SPANLIST[flag][1]);
        contentEditable.insert(end + insertBeforeLen + SPANLIST[flag][1].length(), SPANLIST[flag][2]);
        contentLine.setSelection(start + insertBeforeLen + SPANLIST[flag][1].length(),
                end + insertBeforeLen + SPANLIST[flag][1].length());
    }

    public void insertUrl(String url) {
        contentEditable = contentLine.getText();
        content = contentEditable.toString();
        start = contentLine.getSelectionStart();
        end = contentLine.getSelectionEnd();
        contentEditable.replace(start, end, url);

        int lengthBefore = getStringBefore("\n\n");
        int insertBeforeLen;
        if(start != lengthBefore) {
            contentEditable.replace(start - lengthBefore, start, "\n\n");
            insertBeforeLen = 2 - lengthBefore;
        }
        else
            insertBeforeLen = 0;
        contentLine.setSelection(start + insertBeforeLen, start + insertBeforeLen + url.length());

    }

    private int getStringBefore(String stringBefore) {
        String subString = content.substring(start > stringBefore.length() ? start - stringBefore.length() : 0 , start);
        for(int i = subString.length(); i > 0; i--) {
            if(stringBefore.contains(subString))
                return i;
            subString = subString.substring(1);
        }
        return 0;
    }
}
