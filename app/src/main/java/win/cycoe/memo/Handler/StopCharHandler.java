package win.cycoe.memo.Handler;

/**
 * Created by cycoe on 17-8-12.
 */

public class StopCharHandler {

    private final char[] STOPCHARLIST = {' ', '\n', ',', '.', '，', '。', '!', '?', '！', '？', '+', '-', '*', '#', '/'};

    public boolean findInList(String content, int position) {
        if(content.isEmpty())
            return false;
        char item = content.charAt(position);
        for (char stopChar : STOPCHARLIST)
            if (item == stopChar)
                return true;
        return false;
    }
}
