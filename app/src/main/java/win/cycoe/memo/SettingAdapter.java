package win.cycoe.memo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by cycoe on 17-8-15.
 */

public class SettingAdapter extends BaseAdapter {

    private HashMap<Integer, Boolean> isSelected;

    private List<SettingBean> settingBeanList;
    private LayoutInflater inflater;

    public SettingAdapter(Context context, List<SettingBean> settingBeanList) {
        this.settingBeanList = settingBeanList;
        this.inflater = LayoutInflater.from(context);
    }

    public void initData(boolean[] checked) {
        isSelected = new HashMap<>();
        for(int i = 0; i < settingBeanList.size(); i++)
            isSelected.put(i, checked[i]);
    }

    public HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
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
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.setting_item, null);
            viewHolder = new ViewHolder();
            viewHolder.settingTitle = (TextView) convertView.findViewById(R.id.settingTitle);
            viewHolder.settingContent = (TextView) convertView.findViewById(R.id.settingContent);
            viewHolder.settingCheck = (CheckBox) convertView.findViewById(R.id.settingCheck);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SettingBean settingBean = settingBeanList.get(position);
        viewHolder.settingTitle.setText(settingBean.settingTitle);
        viewHolder.settingContent.setText(settingBean.settingContent);
        viewHolder.settingContent.setVisibility(settingBean.settingContent.isEmpty() ? View.GONE : View.VISIBLE);
        viewHolder.settingCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSelected.put(position, !isSelected.get(position));
            }
        });

        switch (settingBean.settingCheck) {
            case -1:
                viewHolder.settingCheck.setVisibility(View.GONE);
                break;
            case 0:
                viewHolder.settingCheck.setChecked(false);
                break;
            case 1:
                viewHolder.settingCheck.setChecked(true);
                break;
        }
        return convertView;
    }

    private class ViewHolder {
        TextView settingTitle;
        TextView settingContent;
        CheckBox settingCheck;
    }
}
