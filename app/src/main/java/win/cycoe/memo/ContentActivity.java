package win.cycoe.memo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private ImageButton saveButton;
    private ImageButton delButton;
    private TextView dateView;
    private EditText titleLine;
    private EditText contentLine;

    private DialogInterface.OnClickListener clickListenerSave;
    private DialogInterface.OnClickListener clickListenerDel;
    private DialogInterface.OnClickListener clickListenerDiscard;
    private DialogInterface.OnClickListener clickListenerNothing;

    private String[] content = new String[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        itemToolbar = (Toolbar) findViewById(R.id.itemToolbar);

        dateView = (TextView) findViewById(R.id.dateView);
        titleLine = (EditText) findViewById(R.id.titleLine);
        contentLine = (EditText) findViewById(R.id.contentLine);

        clickListenerSave = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setBack(true, MODIFIED);
            }
        };
        clickListenerDel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setBack(false, DELETE);
            }
        };
        clickListenerDiscard = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setBack(false, UNMODIFIED);
            }
        };
        clickListenerNothing = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        };

        getContent();
        fillView();
        setToolbar();
        setButtonVisibility();
    }

    @Override
    public void onBackPressed() {
        setBackWithConfirm();
    }


    private void getContent() {
        content = (String[]) super.getIntent().getStringArrayExtra("content");
    }

    private void fillView() {
        titleLine.setText(content[0]);
        titleLine.setSelection(content[0].length());
        contentLine.setText(content[1]);
        if(content[2].isEmpty())
            dateView.setText("");
        else
            dateView.setText("最后修改时间：" + content[2]);
    }

    private String getNowTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void setToolbar() {
        itemToolbar.inflateMenu(R.menu.toolbar_menu);
        itemToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackWithConfirm();
            }
        });

        saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setVisibility(View.GONE);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBack(true, MODIFIED);
            }
        });

        delButton = (ImageButton) findViewById(R.id.delButton);
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteWithConfirm();
            }
        });

        itemToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.backButton)
                    setBackWithConfirm();
                return true;
            }
        });
    }

    private void deleteWithConfirm() {
        new AlertDialog.Builder(this).setTitle("提示").setMessage("是否删除此便签")
                .setPositiveButton("确定", clickListenerDel)
                .setNegativeButton("取消", clickListenerNothing).show();
    }

    private void setBackWithConfirm() {
        if(saveButton.getVisibility() == View.VISIBLE) {
            new AlertDialog.Builder(this).setTitle("提示").setMessage("是否保存")
                    .setPositiveButton("确定", clickListenerSave)
                    .setNegativeButton("取消", clickListenerDiscard).show();
        }
        else if(titleLine.getText().toString().trim().isEmpty() && contentLine.getText().toString().trim().isEmpty()) {
            new AlertDialog.Builder(this).setTitle("提示").setMessage("是否删除此空白便签")
                    .setPositiveButton("确定", clickListenerDel)
                    .setNegativeButton("取消", clickListenerDiscard).show();
        }
        else
            setBack(false, UNMODIFIED);
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

    private void setButtonVisibility() {

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(titleLine.getText().toString().trim().isEmpty() && contentLine.getText().toString().trim().isEmpty())
                    saveButton.setVisibility(View.GONE);
                else
                    saveButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        titleLine.addTextChangedListener(textWatcher);
        contentLine.addTextChangedListener(textWatcher);
    }
}
