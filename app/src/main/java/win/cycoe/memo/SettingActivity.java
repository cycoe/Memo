package win.cycoe.memo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import win.cycoe.memo.Handler.ConfigHandler;
import win.cycoe.memo.Handler.DatabaseHandler;

/**
 * Created by cycoe on 17-8-15.
 */

public class SettingActivity extends Activity implements AdapterView.OnItemClickListener {

    private Toolbar settingToolbar;
    private ListView listViewOthers;
    private ListView listViewEditor;

    private List<SettingBean> otherBeanList;
    private List<SettingBean> editorBeanList;
    private SettingAdapter othersAdapter;
    private SettingAdapter editAdapter;
    private DialogBuiler builer;
    private ConfigHandler configHandler;
    private SharedPreferences pref;
    private DatabaseHandler dbHandler;

    private String license;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getSharedPreferences("config", MODE_PRIVATE);
        ConfigHandler configHandler = new ConfigHandler(pref);
        setTheme(configHandler.getValue("darkTheme") == 1 ? R.style.AppTheme_dark : R.style.AppTheme);
        setContentView(R.layout.activity_setting);

        initData();
        initView();
        initToolbar();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setBack();
    }

    private void initData() {
        pref = getSharedPreferences("config", MODE_PRIVATE);
        configHandler = new ConfigHandler(pref);

        otherBeanList = new ArrayList<>();
        otherBeanList.add(new SettingBean("导入数据库", "从目录/Sdcard/Memo/memo.db 导入数据库", -1));
        otherBeanList.add(new SettingBean("导出数据库", "导出至目录 /Sdcard/Memo/memo.db", -1));
        otherBeanList.add(new SettingBean("开源协议", "The MIT License", -1));
        otherBeanList.add(new SettingBean("关于", "", -1));
        
        editorBeanList = new ArrayList<>();
        editorBeanList.add(new SettingBean("Markdown 全屏预览", "开启将启用 MarkDown 的全屏预览模式\n关闭将使用半屏预览", configHandler.getValue("fullScreen")));
        editorBeanList.add(new SettingBean("显示图片边框", "Markdown 预览图片显示边框", configHandler.getValue("showBorder")));
        editorBeanList.add(new SettingBean("暗色主题", "重启应用生效", configHandler.getValue("darkTheme")));

        builer = new DialogBuiler(this);
        dbHandler = new DatabaseHandler();
    }

    private void initView() {
        listViewOthers = (ListView) findViewById(R.id.listViewOthers);
        othersAdapter = new SettingAdapter(this, otherBeanList);
        listViewOthers.setAdapter(othersAdapter);
        listViewOthers.setOnItemClickListener(this);
        
        listViewEditor = (ListView) findViewById(R.id.listViewEditor);
        editAdapter = new SettingAdapter(this, editorBeanList);
        editAdapter.initData(new boolean[] {(configHandler.getValue("fullScreen") == 1),
                                                configHandler.getValue("showBorder") == 1,
                                                configHandler.getValue("darkTheme") == 1});
        listViewEditor.setAdapter(editAdapter);
        listViewEditor.setOnItemClickListener(this);
    }

    private void initToolbar() {
        settingToolbar = (Toolbar) findViewById(R.id.settingToolbar);
        settingToolbar.setTitle(R.string.setting);
        settingToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBack();
            }
        });
    }

    private void setBack() {
        HashMap<Integer, Boolean> isSelectedEdit = editAdapter.getIsSelected();
        configHandler.createKV("fullScreen", isSelectedEdit.get(0) ? 1 : 0);
        configHandler.createKV("showBorder", isSelectedEdit.get(1) ? 1 : 0);
        configHandler.createKV("darkTheme", isSelectedEdit.get(2) ? 1 : 0);
        setResult(3);
        finish();
    }

    private void openLicense() {
        InputStream is = getResources().openRawResource(R.raw.license);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        license = "";
        try {
            for (String out = ""; out != null; out = br.readLine()) {
                license += out;
                license += "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        builer.createDialog("License", license);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (adapterView.getId()) {
            case R.id.listViewOthers:
                switch (position) {
                    case 0:
                        Snackbar.make(listViewOthers, dbHandler.exportDb(false) ? "导入数据库成功" : "导入数据库失败", Snackbar.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Snackbar.make(listViewOthers, dbHandler.exportDb(true) ? "导出数据库成功" : "导出数据库失败", Snackbar.LENGTH_SHORT).show();
                        break;
                    case 2:
                        openLicense();
                        break;
                    case 3:
                        openAbout();
                }
                break;
        }
    }

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
