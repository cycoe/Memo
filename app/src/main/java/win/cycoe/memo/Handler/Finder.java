package win.cycoe.memo.Handler;

/**
 * Created by cycoe on 17-8-14.
 */

public class Finder {

    public boolean findInList(String[] list, String item) {
        for (String aList : list) {
            if (aList.equals(item))
                return true;
        }
        return false;
    }

    public boolean findAllInString(String content, String[] items) {
        int flag = 0;
        for (String item : items) {
            if (content.contains(item))
                flag++;
        }
        return flag == items.length;
    }
}
