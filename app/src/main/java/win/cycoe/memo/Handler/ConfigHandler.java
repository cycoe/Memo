package win.cycoe.memo.Handler;

import android.content.SharedPreferences;

/**
 * Created by cycoe on 17-8-15.
 */

public class ConfigHandler {

    private SharedPreferences pref;

    public ConfigHandler(SharedPreferences pref) {
        this.pref = pref;
    }

    public void createKV(String key, int value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getValue(String key) {
        return pref.getInt(key, 0);
    }
}
