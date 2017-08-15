package win.cycoe.memo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cycoe on 17-8-15.
 */

public class SettingActivity extends Activity implements AdapterView.OnItemClickListener {

    private Toolbar settingToolbar;

    private ListView listViewOthers;
    private List<SettingBean> settingBeanList;
    private SettingAdapter settingAdapter;
    private DialogBuiler builer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initData();
        initView();
        initToolbar();
    }

    private void initData() {
        settingBeanList = new ArrayList<>();
        settingBeanList.add(new SettingBean("开源协议", "GPL V3.0", -1));
        settingBeanList.add(new SettingBean("关于", "", -1));

        builer = new DialogBuiler(this);
    }

    private void initView() {
        listViewOthers = (ListView) findViewById(R.id.listViewOthers);
        settingAdapter = new SettingAdapter(this, settingBeanList);
        listViewOthers.setAdapter(settingAdapter);
        listViewOthers.setOnItemClickListener(this);
    }

    private void initToolbar() {
        settingToolbar = (Toolbar) findViewById(R.id.settingToolbar);
        settingToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (position) {
            case 0:
                builer.createDialog("", "测试");
        }
    }
}
