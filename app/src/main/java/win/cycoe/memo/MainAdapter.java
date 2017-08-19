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
 * Created by cycoe on 17-8-19.
 */

public class MainAdapter extends BaseAdapter {

    private HashMap<Integer, Boolean> isSelected;

    private List<MainBean> mainBeanList;
    private LayoutInflater inflater;

    public MainAdapter(Context context, List<MainBean> mainBeanList) {
        this.mainBeanList = mainBeanList;
        this.inflater = LayoutInflater.from(context);
        initData();
    }

    private void initData() {
        isSelected = new HashMap<>();
        for(int i = 0; i < mainBeanList.size(); i++)
            isSelected.put(i, false);
    }

    public HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }

    @Override
    public int getCount() {
        return mainBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return mainBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.content = (TextView) convertView.findViewById(R.id.content);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.mainCheckBox = (CheckBox) convertView.findViewById(R.id.mainCheckBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MainBean mainBean = mainBeanList.get(position);
        viewHolder.title.setVisibility(mainBean.title.isEmpty() ? View.GONE : View.VISIBLE);
        viewHolder.title.setText(mainBean.title);
        viewHolder.content.setVisibility(mainBean.content.isEmpty() ? View.GONE : View.VISIBLE);
        viewHolder.content.setText(mainBean.content);
        viewHolder.date.setText(mainBean.date);
        viewHolder.mainCheckBox.setVisibility(View.GONE);

        viewHolder.mainCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSelected.put(position, !isSelected.get(position));
            }
        });

        return convertView;
    }

    private class ViewHolder {
        TextView title;
        TextView content;
        TextView date;
        CheckBox mainCheckBox;
    }
}
