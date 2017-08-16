package win.cycoe.memo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private String license;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        otherBeanList.add(new SettingBean("开源协议", "The MIT License", -1));
        otherBeanList.add(new SettingBean("关于", "", -1));
        
        editorBeanList = new ArrayList<>();
        editorBeanList.add(new SettingBean("Markdown 全屏预览", "开启将启用 MarkDown 的全屏预览模式\n关闭将使用半屏预览", configHandler.getValue("fullScreen")));
        editorBeanList.add(new SettingBean("显示图片边框", "", configHandler.getValue("showBorder")));

        builer = new DialogBuiler(this);
    }

    private void initView() {
        listViewOthers = (ListView) findViewById(R.id.listViewOthers);
        othersAdapter = new SettingAdapter(this, otherBeanList);
        listViewOthers.setAdapter(othersAdapter);
        listViewOthers.setOnItemClickListener(this);
        
        listViewEditor = (ListView) findViewById(R.id.listViewEditor);
        editAdapter = new SettingAdapter(this, editorBeanList);
        editAdapter.initData(new boolean[] {(configHandler.getValue("fullScreen") == 1),
                                                configHandler.getValue("showBorder") == 1});
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
                        openLicense();
                        break;
                    case 1:
                        openAbout();
                }
                break;
            case R.id.listViewEditor:
                switch (position) {
                    case 0:
                        break;
                }
                break;
        }
    }

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
