package win.cycoe.memo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * Created by cycoe on 7/27/17.
 */

public class ContentActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {

    // set the constants of resultCodes
    private final int UNMODIFIED = 0;
    private final int MODIFIED = 1;
    private final int DELETE = 2;
    private final String[][] SPANLIST = {
            {"**", "**"},
            {"*", "*"},
            {"- ", ""},
            {"1. ", ""},
            {"~~", "~~"},
    };

    private Toolbar itemToolbar;
    private ImageButton undoButton;
    private ImageButton redoButton;
    private ImageButton saveButton;
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
    private FrameLayout mdLayoutView;
    private EditText markdownView;
    private ProgressBar loadProgressBar;

    private ContentStack contentStack;
    private StopCharHandler stopCharHandler;
    private DialogBuiler builer;
    private MdASyncTask mdASyncTask;

    private DialogInterface.OnClickListener clickListenerSave;
    private DialogInterface.OnClickListener clickListenerDel;
    private DialogInterface.OnClickListener clickListenerDiscard;
    private DialogInterface.OnClickListener clickListenerNothing;

    private String[] content = new String[3];
    private boolean isInput = true;
    private boolean isisInput = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        initData();
        initView();
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
            case R.id.saveButton:
                setBack(true, MODIFIED);
                break;
            case R.id.delButton:
                deleteWithConfirm();
                break;
            case R.id.undoButton:
                undo();
                break;
            case R.id.redoButton:
                redo();
                break;
            case R.id.boldButton:
                insertSpan(0);
                break;
            case R.id.italicButton:
                insertSpan(1);
                break;
            case R.id.ulButton:
                insertSpan(2);
                break;
            case R.id.olButton:
                insertSpan(3);
                break;
            case R.id.deletelineButton:
                insertSpan(4);
                break;
            case R.id.mdSwitch:
                switchMdView();
                break;
        }
    }

    // initiate view by id
    // set on click listeners
    private void initView() {
        itemToolbar = (Toolbar) findViewById(R.id.itemToolbar);
        dateView = (TextView) findViewById(R.id.dateView);
        titleLine = (EditText) findViewById(R.id.titleLine);
        contentLine = (EditText) findViewById(R.id.contentLine);
        markdownView = (EditText) findViewById(R.id.markdownView);
        mdLayoutView = (FrameLayout) findViewById(R.id.mdLayoutView);
        undoButton = (ImageButton) findViewById(R.id.undoButton);
        redoButton = (ImageButton) findViewById(R.id.redoButton);
        saveButton = (ImageButton) findViewById(R.id.saveButton);
        delButton = (ImageButton) findViewById(R.id.delButton);
        boldButton = (ImageButton) findViewById(R.id.boldButton);
        italicButton = (ImageButton) findViewById(R.id.italicButton);
        ulButton = (ImageButton) findViewById(R.id.ulButton);
        olButton = (ImageButton) findViewById(R.id.olButton);
        deletelineButton = (ImageButton) findViewById(R.id.deletelineButton);
        mdSwitch = (Switch) findViewById(R.id.mdSwitch);
        loadProgressBar = (ProgressBar) findViewById(R.id.loadProgressBar);

        setButtonEnabled(saveButton, false);
        setButtonEnabled(undoButton, false);
        setButtonEnabled(redoButton, false);
        contentLine.setOnFocusChangeListener(this);
        saveButton.setOnClickListener(this);
        delButton.setOnClickListener(this);
        undoButton.setOnClickListener(this);
        redoButton.setOnClickListener(this);
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
        mdASyncTask = new MdASyncTask(markdownView, loadProgressBar);

        builer = new DialogBuiler(this);
    }

//    // create dialog with message and 2 clickListener
//    private void createDialog(String message,
//                              DialogInterface.OnClickListener positiveListener,
//                              DialogInterface.OnClickListener negativeListener) {
//        /**
//         * 1. setMessage: set the massage to show
//         * 2. setPositiveButton(buttonString, clickListener)
//         * 3. setNegativeButton(buttonString, clickListener)
//         * 4. show(): remember to show dialog
//         */
//        builder.setMessage(message);
//        builder.setPositiveButton(R.string.yes,positiveListener);
//        builder.setNegativeButton(R.string.no, negativeListener);
//        builder.show();
//
//    }

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

    private String getNowTime() {
        /**
         * 1. instantiate a dateFormat object with the pattern of "yyyy-MM-dd HH:mm:ss"
         * 2. return the formated date string
         */
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void switchMdView() {
        mdLayoutView.setVisibility(mdSwitch.isChecked() ? View.VISIBLE : View.GONE);
        if(mdSwitch.isChecked()) {
            markdownView.post(new Runnable() {
                @Override
                public void run() {
                    new MdASyncTask(markdownView, loadProgressBar).execute(contentLine.getText().toString());
                }
            });
        }
    }

    private void insertSpan(int flag) {
        int start = contentLine.getSelectionStart();
        int end = contentLine.getSelectionEnd();
        contentLine.getText().insert(start, SPANLIST[flag][0]);
        contentLine.getText().insert(end+SPANLIST[flag][0].length(), SPANLIST[flag][1]);
        contentLine.setSelection(start+SPANLIST[flag][0].length(), end+SPANLIST[flag][0].length());
    }

    private void setToolbar() {
        /**
         * 1. inflateMenu(res/menu/menu.xml): set the flate menu items
         * 2. setNavigationOnClickListener()
         */

//        itemToolbar.inflateMenu(R.menu.toolbar_menu);
        itemToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBackWithConfirm();
            }
        });
//        itemToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                if(menuItem.getItemId() == R.id.backButton)
//                    setBackWithConfirm();
//                return true;
//            }
//        });
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
        builer.createDialog("是否删除此便签", clickListenerDel, clickListenerNothing);
    }

    private void setBackWithConfirm() {
        /**
         * 1. if saveButton is visible, means you need to save before back
         * 2. else if the title and content are both empty, besides, the date is not empty, means
         *    that you delete the content
         * 3. others, go back with no modification
         */
        if(saveButton.isEnabled()) {
            builer.createDialog("是否保存", clickListenerSave, clickListenerDiscard);
        }
        else if(titleLine.getText().toString().trim().isEmpty()
                && contentLine.getText().toString().trim().isEmpty()
                && !content[2].isEmpty()) {
            builer.createDialog("是否删除此空白便签", clickListenerDel, clickListenerDiscard);
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

        finish();
    }

    private void saveContent() {
        content[0] = titleLine.getText().toString();
        content[1] = contentLine.getText().toString();
        content[2] = getNowTime();
    }

    private void undo() {
        String content = contentLine.getText().toString();
        int length = contentLine.getText().length();
        if(isisInput && !stopCharHandler.findInList(content, length - 1))
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
                if(charSequence.toString().trim().isEmpty() && contentLine.getText().toString().trim().isEmpty())
                    setButtonEnabled(saveButton, false);
                else
                    setButtonEnabled(saveButton, true);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        contentLine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(titleLine.getText().toString().trim().isEmpty() && charSequence.toString().trim().isEmpty())
                    setButtonEnabled(saveButton, false);
                else
                    setButtonEnabled(saveButton, true);

                if(isInput) {
                    setButtonEnabled(undoButton, true);
                    setButtonEnabled(redoButton, false);

                    int cursor = start + count;
                    if(stopCharHandler.findInList(charSequence.toString(), cursor == 0 ? 0 : cursor - 1))
                        contentStack.put(charSequence.toString(), cursor);
                }

                if(mdSwitch.isChecked())
                    new MdASyncTask(markdownView, loadProgressBar).execute(contentLine.getText().toString());
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
            setButtonEnabled(boldButton, true);
            setButtonEnabled(italicButton, true);
            setButtonEnabled(ulButton, true);
            setButtonEnabled(olButton, true);
        }
        else {
            setButtonEnabled(boldButton, false);
            setButtonEnabled(italicButton, false);
            setButtonEnabled(ulButton, false);
            setButtonEnabled(olButton, false);
        }
    }
}
