package win.cycoe.memo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cycoe on 7/27/17.
 */

public class ContentActivity extends Activity {

    // set the constants of resultcodes
    private final int UNMODIFIED = 0;
    private final int MODIFIED = 1;
    private final int DELETE = 2;

    private Toolbar itemToolbar;
    private TextView dateView;
    private EditText titleLine;
    private EditText contentLine;
    private String[] content = new String[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        itemToolbar = (Toolbar) findViewById(R.id.itemToolbar);

        dateView = (TextView) findViewById(R.id.dateView);
        titleLine = (EditText) findViewById(R.id.titleLine);
        contentLine = (EditText) findViewById(R.id.contentLine);

        getContent();
        setToolbar();
        fillView();
    }

    @Override
    public void onBackPressed() {
        setBack(false, UNMODIFIED);
    }


    private void getContent() {
        content = (String[]) super.getIntent().getStringArrayExtra("content");
    }

    private void fillView() {
        titleLine.setText(content[0]);
        contentLine.setText(content[1]);
        dateView.setText("最后修改时间：" + content[2]);
    }

    private String getNowTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void setToolbar() {
        itemToolbar.inflateMenu(R.menu.toolbar_menu);
        itemToolbar.setNavigationIcon(R.mipmap.back);
        itemToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBack(false, UNMODIFIED);
            }
        });

        itemToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.backButton)
                    setBack(false, UNMODIFIED);
                return true;
            }
        });
    }

    private void setBack(boolean modified, int actionFlag) {
        Intent data = new Intent();
        if(modified) {
            content[0] = titleLine.getText().toString();
            content[1] = contentLine.getText().toString();
            content[2] = getNowTime();
        }
        data.putExtra("content", content);
        setResult(actionFlag, data);

        //结束当前页面
        finish();
    }

}
