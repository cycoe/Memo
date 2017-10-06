package win.cycoe.memo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import win.cycoe.memo.Handler.DatabaseHandler;
import win.cycoe.memo.Handler.DialogBuilder;

/**
 * Created by cycoe on 17-8-21.
 */

public class SettingActivity extends PreferenceActivity {

    private Toolbar settingToolbar;

    private DialogBuilder builder;
    private DatabaseHandler dbHandler;
    private Intent intent;
    private SharedPreferences pref;

    private int colorTheme;
    private int dialogTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadConfig();
        setTheme(colorTheme);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_setting);
        initData();
    }
    
    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.activity_setting, new LinearLayout(this), false);
        ViewGroup contentWrapper = contentView.findViewById(R.id.settingWrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);
        getWindow().setContentView(contentView);

        settingToolbar = contentView.findViewById(R.id.settingToolbar);
        settingToolbar.setTitle(getResources().getString(R.string.setting));
        settingToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBack();
            }
        });
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch(preference.getKey()) {
            case "darkTheme":
                reloadActivity();
                break;
            case "importDatabase":
                intent.putExtra("refresh", true);
                Snackbar.make(settingToolbar, dbHandler.exportDb(false) ? "导入数据库成功" : "导入数据库失败", Snackbar.LENGTH_SHORT).show();
                break;
            case "exportDatabase":
                Snackbar.make(settingToolbar, dbHandler.exportDb(true) ? "导出数据库成功" : "导出数据库失败", Snackbar.LENGTH_SHORT).show();
                break;
            case "license":
                openLicense();
                break;
            case "about":
                openAbout();
                break;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onBackPressed() {
        setBack();
        super.onBackPressed();
    }

    private void loadConfig() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        colorTheme = pref.getBoolean("darkTheme", false) ? R.style.AppTheme_dark : R.style.AppTheme;
        dialogTheme = pref.getBoolean("darkTheme", false) ? R.style.DialogTheme_dark : R.style.DialogTheme;
    }

    private void initData() {
        builder = new DialogBuilder(this, dialogTheme);
        dbHandler = new DatabaseHandler();
        intent = new Intent();
    }

    private void openLicense() {
        InputStream is = getResources().openRawResource(R.raw.license);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String license = "";
        try {
            for (String out = ""; out != null; out = br.readLine()) {
                license += out;
                license += "\n";
            }
        } catch (IOException e) {
            Snackbar.make(settingToolbar, "License 加载失败", Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        builder.createDialog("License", license);
    }

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void setBack() {
        setResult(3, intent);
        finish();
    }

    private void reloadActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
