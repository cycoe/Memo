package win.cycoe.memo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toolbar;

import com.zzhoujay.richtext.RichText;

import java.util.Map;

import win.cycoe.memo.Handler.ConfigHandler;
import win.cycoe.memo.Handler.ContentStack;
import win.cycoe.memo.Handler.DateParser;
import win.cycoe.memo.Handler.DialogBuilder;
import win.cycoe.memo.Handler.MdASyncTask;
import win.cycoe.memo.Handler.SpanHandler;
import win.cycoe.memo.Handler.StopCharHandler;
import win.cycoe.memo.Handler.UriToPathUtil;


/**
 * Created by cycoe on 7/27/17.
 */

public class ContentActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {

    // set the constants of resultCodes
    private final int UNMODIFIED = 0;
    private final int MODIFIED = 1;
    private final int DELETE = 2;

    private Toolbar itemToolbar;
    private ImageButton undoButton;
    private ImageButton redoButton;
    private ImageButton photoButton;
    private ImageButton delButton;
    private ImageButton boldButton;
    private ImageButton italicButton;
    private ImageButton ulButton;
    private ImageButton olButton;
    private ImageButton deletelineButton;
    private Switch mdSwitch;
    private TextView dateView;
    private EditText titleLine;
    private EditText contentLine;
    private ScrollView contentView;
    private FrameLayout mdLayoutView;
    private EditText markdownView;
    private ProgressBar loadProgressBar;

    private ContentStack contentStack;
    private StopCharHandler stopCharHandler;
    private DialogBuilder builder;
    private SharedPreferences pref;
    private ConfigHandler configHandler;
    private SpanHandler spanHandler;
    private DateParser dateParser;

    private DialogInterface.OnClickListener clickListenerSave;
    private DialogInterface.OnClickListener clickListenerDel;
    private DialogInterface.OnClickListener clickListenerDiscard;
    private DialogInterface.OnClickListener clickListenerNothing;

    private String[] content = new String[3];
    private boolean isInput = true;
    private boolean isisInput = true;
    private boolean isModified = false;
    private int mainTheme;
    private int dialogTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadConfig();
        setTheme(mainTheme);
        setContentView(R.layout.activity_content);

        initView();
        initData();
        initDialogListener();
        fillView();
        setToolbar();
        setTextChangeListener();
    }

    //override the default back event
    @Override
    public void onBackPressed() {
        setBackWithConfirm();
    }

    // override the default onClick methods
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delButton:
                deleteWithConfirm();
                break;
            case R.id.undoButton:
                undo();
                break;
            case R.id.redoButton:
                redo();
                break;
            case R.id.photoButton:
                selectPhoto();
                break;
            case R.id.boldButton:
                spanHandler.insertSpan(0);
                break;
            case R.id.italicButton:
                spanHandler.insertSpan(1);
                break;
            case R.id.ulButton:
                spanHandler.insertSpan(2);
                break;
            case R.id.olButton:
                spanHandler.insertSpan(3);
                break;
            case R.id.deletelineButton:
                spanHandler.insertSpan(4);
                break;
            case R.id.mdSwitch:
                switchMdView();
                break;
        }
    }

    private void loadConfig() {
        SharedPreferences pref = getSharedPreferences("config", MODE_PRIVATE);
        configHandler = new ConfigHandler(pref);
        mainTheme = configHandler.getValue("darkTheme") == 1 ? R.style.AppTheme_dark : R.style.AppTheme;
        dialogTheme = configHandler.getValue("darkTheme") == 1 ? R.style.DialogTheme_dark : R.style.DialogTheme;
    }

    // initiate view by id
    // set on click listeners
    private void initView() {
        itemToolbar = (Toolbar) findViewById(R.id.itemToolbar);
        dateView = (TextView) findViewById(R.id.dateView);
        titleLine = (EditText) findViewById(R.id.titleLine);
        contentLine = (EditText) findViewById(R.id.contentLine);
        markdownView = (EditText) findViewById(R.id.markdownView);
        contentView = (ScrollView) findViewById(R.id.contentView);
        mdLayoutView = (FrameLayout) findViewById(R.id.mdLayoutView);
        undoButton = (ImageButton) findViewById(R.id.undoButton);
        redoButton = (ImageButton) findViewById(R.id.redoButton);
        photoButton = (ImageButton) findViewById(R.id.photoButton);
        delButton = (ImageButton) findViewById(R.id.delButton);
        boldButton = (ImageButton) findViewById(R.id.boldButton);
        italicButton = (ImageButton) findViewById(R.id.italicButton);
        ulButton = (ImageButton) findViewById(R.id.ulButton);
        olButton = (ImageButton) findViewById(R.id.olButton);
        deletelineButton = (ImageButton) findViewById(R.id.deletelineButton);
        mdSwitch = (Switch) findViewById(R.id.mdSwitch);
        loadProgressBar = (ProgressBar) findViewById(R.id.loadProgressBar);

        setButtonEnabled(undoButton, false);
        setButtonEnabled(redoButton, false);
        contentLine.setOnFocusChangeListener(this);
        delButton.setOnClickListener(this);
        undoButton.setOnClickListener(this);
        redoButton.setOnClickListener(this);
        photoButton.setOnClickListener(this);
        boldButton.setOnClickListener(this);
        italicButton.setOnClickListener(this);
        ulButton.setOnClickListener(this);
        olButton.setOnClickListener(this);
        deletelineButton.setOnClickListener(this);
        mdSwitch.setOnClickListener(this);

    }

    // create different clickListener for dialog
    private void initDialogListener() {
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
    }

    // get the Intent Object, get the content string array with key "content"
    private void initData() {
        content = getIntent().getStringArrayExtra("content");
        contentStack = new ContentStack(10);
        contentStack.put(content[1], content[1].length());

        stopCharHandler = new StopCharHandler();
        spanHandler = new SpanHandler(contentLine);
        dateParser = new DateParser();

        builder = new DialogBuilder(this, dialogTheme);

        pref = getSharedPreferences("config", MODE_PRIVATE);
        configHandler = new ConfigHandler(pref);
    }

    private void fillView() {
        /**
         * 1. initiate titleLine, contentLine, dateView with content
         * 2. set the dateView unvisible if with no date string
         */
        titleLine.setText(content[0]);
        contentLine.setText(content[1]);
        contentLine.requestFocus();
        contentLine.setSelection(content[1].length());
        dateView.setText(content[2]);

        if(content[2].isEmpty())
            dateView.setVisibility(View.GONE);
        else
            dateView.setVisibility(View.VISIBLE);
    }

    private void switchMdView() {
        Animation fadeShow = AnimationUtils.loadAnimation(this, R.anim.fadeshow);
        Animation fadeHide = AnimationUtils.loadAnimation(this, R.anim.fadehide);

        mdLayoutView.startAnimation(mdSwitch.isChecked() ? fadeShow : fadeHide);
        mdLayoutView.setVisibility(mdSwitch.isChecked() ? View.VISIBLE : View.GONE);
        if(configHandler.getValue("fullScreen") == 1) {
            contentView.startAnimation(mdSwitch.isChecked() ? fadeHide : fadeShow);
            contentView.setVisibility(mdSwitch.isChecked() ? View.GONE : View.VISIBLE);
        } else
            contentView.startAnimation(fadeShow);

        if(mdSwitch.isChecked())
            new MdASyncTask(markdownView, loadProgressBar, configHandler.getValue("showBorder") == 1)
                    .execute(contentLine.getText().toString());
        else
            contentLine.requestFocus();
    }

    private void setToolbar() {
        /**
         * 1. inflateMenu(res/menu/menu.xml): set the flate menu items
         * 2. setNavigationOnClickListener()
         */

        itemToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(titleLine.getText().toString().trim().isEmpty()
                        && contentLine.getText().toString().trim().isEmpty()) {
                    if (!content[2].isEmpty())
                        setBack(false, DELETE);
                    else
                        setBack(false, UNMODIFIED);
                }
                else if(isModified)
                    setBack(true, MODIFIED);
                else
                    setBack(false, UNMODIFIED);
            }
        });
    }

    private void setButtonEnabled(View view, boolean flag) {
        if(flag) {
            view.setEnabled(flag);
            view.setAlpha((float) 1);
        }
        else {
            view.setEnabled(flag);
            view.setAlpha((float) 0.5);
        }
    }

    private void deleteWithConfirm() {
        builder.createDialog("警告", "是否删除此便签", clickListenerDel, clickListenerNothing);
    }

    private void setBackWithConfirm() {
        /**
         * 1. if saveButton is visible, means you need to save before back
         * 2. else if the title and content are both empty, besides, the date is not empty, means
         *    that you delete the content
         * 3. others, go back with no modification
         */
        if(isModified) {
            if(titleLine.getText().toString().trim().isEmpty()
                    && contentLine.getText().toString().trim().isEmpty()
                    && !content[2].isEmpty())
                builder.createDialog("警告", "是否删除此空白便签", clickListenerDel, clickListenerDiscard);
            else
                builder.createDialog("提示", "是否保存", clickListenerSave, clickListenerDiscard);
        }
        else
            setBack(false, UNMODIFIED);
    }

    // the exit for upper activity
    private void setBack(boolean modified, int actionFlag) {
        /**
         * 1. initiate a new Intent
         * 2. if content is modified, then replace the content array with new data
         * 3. put the content array into Intent
         * 4. setResult with resultCode and Intent
         */
        Intent data = new Intent();
        if(modified) {
            saveContent();
        }
        data.putExtra("content", content);
        setResult(actionFlag, data);

        RichText.clear(this);
        finish();
    }

    private void selectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            Uri uri = data.getData();
            UriToPathUtil uriToPathUtil = new UriToPathUtil();
            String path = uriToPathUtil.getRealFilePath(this, uri);
            String fullPath = "![img](" + path + ")";

            spanHandler.insertUrl(fullPath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveContent() {
        content[0] = titleLine.getText().toString();
        content[1] = contentLine.getText().toString();
        content[2] = dateParser.getCurrentTime();
    }

    private void undo() {
        String content = contentLine.getText().toString();
        int length = contentLine.getText().length();
        if(isisInput)
            contentStack.put(content, length);
        isInput = false;
        Map<String, Object> map = contentStack.undo();
        contentLine.setText((String) map.get("content"));
        contentLine.setSelection((int) map.get("cursor"));
        if(contentStack.canUndo())
            setButtonEnabled(undoButton, true);
        else
            setButtonEnabled(undoButton, false);
        setButtonEnabled(redoButton, true);
    }

    private void redo() {
        isInput = false;
        Map<String, Object> map = contentStack.redo();
        contentLine.setText((String) map.get("content"));
        contentLine.setSelection((int) map.get("cursor"));
        if(contentStack.canRedo())
            setButtonEnabled(redoButton, true);
        else
            setButtonEnabled(redoButton, false);
        setButtonEnabled(undoButton, true);
    }

    // set the visibility of the saveButton
    private void setTextChangeListener() {
        titleLine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                itemToolbar.setNavigationIcon(R.mipmap.done);
                itemToolbar.setNavigationIcon(R.mipmap.done);
                isModified = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        contentLine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                if(count > after && isInput) {
                    int cursor = start + count;
                    if (stopCharHandler.findInList(charSequence.toString().substring(start, cursor)))
                        contentStack.put(charSequence.toString(), cursor);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                itemToolbar.setNavigationIcon(R.mipmap.done);
                itemToolbar.setNavigationIcon(R.mipmap.done);
                isModified = true;

                if(isInput) {
                    setButtonEnabled(undoButton, true);
                    setButtonEnabled(redoButton, false);

                    if(count > before) {
                        int cursor = start + count;
                        if (stopCharHandler.findInList(charSequence.toString().substring(start, cursor)))
                            contentStack.put(charSequence.toString(), cursor);
                    }
                }

                if(mdSwitch.isChecked())
                    new MdASyncTask(markdownView, loadProgressBar, configHandler.getValue("showBorder") == 1)
                            .execute(contentLine.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                isisInput = isInput;
                isInput = true;
            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        if(isFocused) {
            setButtonEnabled(photoButton, true);
            setButtonEnabled(boldButton, true);
            setButtonEnabled(italicButton, true);
            setButtonEnabled(ulButton, true);
            setButtonEnabled(olButton, true);
            setButtonEnabled(deletelineButton, true);
        }

        else {
            setButtonEnabled(photoButton, false);
            setButtonEnabled(boldButton, false);
            setButtonEnabled(italicButton, false);
            setButtonEnabled(ulButton, false);
            setButtonEnabled(olButton, false);
            setButtonEnabled(deletelineButton, false);
        }
    }
}
