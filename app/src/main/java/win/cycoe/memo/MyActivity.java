package win.cycoe.memo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by cycoe on 17-8-22.
 */

public class MyActivity extends AppCompatActivity {

    private int dialogTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initTheme();
        super.onCreate(savedInstanceState);
    }

    private void initTheme() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int colorTheme = pref.getBoolean("darkTheme", false) ? R.style.AppTheme_dark : R.style.AppTheme;
        dialogTheme = pref.getBoolean("darkTheme", false) ? R.style.DialogTheme_dark : R.style.DialogTheme;
        setTheme(colorTheme);
    }

    public int getDialogTheme() {
        return dialogTheme;
    }
}
