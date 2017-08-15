package win.cycoe.memo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by cycoe on 17-8-15.
 */

public class SettingAdapter extends BaseAdapter {

    private List<SettingBean> settingBeanList;
    private LayoutInflater inflater;

    public SettingAdapter(Context context, List<SettingBean> settingBeanList) {
        this.settingBeanList = settingBeanList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return settingBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return settingBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        if(convertView == null)
            convertView = inflater.inflate(R.layout.setting_item, null);
        TextView settingTitle = (TextView) convertView.findViewById(R.id.settingTitle);
        TextView settingContent = (TextView) convertView.findViewById(R.id.settingContent);
        CheckBox settingCheck = (CheckBox) convertView.findViewById(R.id.settingCheck);

        SettingBean settingBean = settingBeanList.get(position);
        settingTitle.setText(settingBean.settingTitle);
        if(settingBean.settingContent.isEmpty())
            settingContent.setVisibility(View.GONE);
        else
            settingContent.setText(settingBean.settingContent);
        switch (settingBean.settingCheck) {
            case -1:
                settingCheck.setVisibility(View.GONE);
                break;
            case 0:
                settingCheck.setChecked(false);
                break;
            case 1:
                settingCheck.setChecked(true);
                break;
        }
        return convertView;
    }
}
