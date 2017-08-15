package win.cycoe.memo.Handler;

/**
 * Created by cycoe on 17-8-14.
 */

public class Finder {

    public boolean findInList(String[] list, String item) {
        for(int i = 0; i < list.length; i++) {
            if(list[i].equals(item))
                return true;
        }
        return false;
    }

}
