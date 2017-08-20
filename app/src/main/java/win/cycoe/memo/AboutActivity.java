package win.cycoe.memo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import win.cycoe.memo.Handler.ConfigHandler;

/**
 * Created by cycoe on 17-8-16.
 */

public class AboutActivity extends Activity {

    private Toolbar aboutToolbar;
    private TextView aboutView;

    private ConfigHandler configHandler;

    private String about;
    private int colorTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadConfig();
        setTheme(colorTheme);
        setContentView(R.layout.activity_about);

        setToolbar();
        initView();
    }

    private void loadConfig() {
        SharedPreferences pref = getSharedPreferences("config", MODE_PRIVATE);
        configHandler = new ConfigHandler(pref);
        colorTheme = configHandler.getValue("darkTheme") == 1 ? R.style.AppTheme_dark : R.style.AppTheme;
    }

    private void setToolbar() {
        aboutToolbar = (Toolbar) findViewById(R.id.aboutToolbar);
        aboutToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initView() {
        aboutView = findViewById(R.id.aboutView);
        aboutView.setMovementMethod(LinkMovementMethod.getInstance());
        InputStream is = getResources().openRawResource(R.raw.about);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        about = "";
        try {
            for (String out = ""; out != null; out = br.readLine()) {
                about += out;
                about += "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        aboutView.setText(about);
    }
}
