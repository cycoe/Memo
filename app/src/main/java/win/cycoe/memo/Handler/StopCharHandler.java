package win.cycoe.memo.Handler;

/**
 * Created by cycoe on 17-8-12.
 */

public class StopCharHandler {

    private final String[] STOPCHARLIST = {" ", "\n", ",", ".", "+", "-", "*", "#", "/", "，", "。", "!", "?", "！", "？"};

    public boolean findInList(String content) {
        if(content.isEmpty())
            return false;
        for (String stopChar : STOPCHARLIST)
            if (content.contains(stopChar))
                return true;
        return false;
    }
}
