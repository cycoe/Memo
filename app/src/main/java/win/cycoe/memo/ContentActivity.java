package win.cycoe.memo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * Created by cycoe on 7/27/17.
 */

public class ContentActivity extends Activity implements View.OnClickListener {

    // set the constants of resultCodes
    private final int UNMODIFIED = 0;
    private final int MODIFIED = 1;
    private final int DELETE = 2;
    private final char[] STOPCHARLIST = {' ', '\n', ',', '.', '，', '。', '!', '?', '！', '？'};

    private Toolbar itemToolbar;
    private ImageButton undoButton;
    private ImageButton redoButton;
    private ImageButton saveButton;
    private ImageButton delButton;
    private TextView dateView;
    private EditText titleLine;
    private EditText contentLine;
    private AlertDialog.Builder builder;
    private ContentStack contentStack;

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
        }
    }

    // initiate view by id
    // set on click listeners
    private void initView() {
        itemToolbar = (Toolbar) findViewById(R.id.itemToolbar);
        dateView = (TextView) findViewById(R.id.dateView);
        titleLine = (EditText) findViewById(R.id.titleLine);
        contentLine = (EditText) findViewById(R.id.contentLine);
        undoButton = (ImageButton) findViewById(R.id.undoButton);
        redoButton = (ImageButton) findViewById(R.id.redoButton);
        saveButton = (ImageButton) findViewById(R.id.saveButton);
        delButton = (ImageButton) findViewById(R.id.delButton);

        setButtonEnabled(saveButton, false);
        setButtonEnabled(undoButton, false);
        setButtonEnabled(redoButton, false);
        saveButton.setOnClickListener(this);
        delButton.setOnClickListener(this);
        undoButton.setOnClickListener(this);
        redoButton.setOnClickListener(this);

        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tip);
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
        content = (String[]) getIntent().getStringArrayExtra("content");
        contentStack = new ContentStack(10);
        contentStack.put(content[1], content[1].length());
    }

    // create dialog with message and 2 clickListener
    private void createDialog(String message,
                              DialogInterface.OnClickListener positiveListener,
                              DialogInterface.OnClickListener negativeListener) {
        /*
         * 1. setMessage: set the massage to show
         * 2. setPositiveButton(buttonString, clickListener)
         * 3. setNegativeButton(buttonString, clickListener)
         * 4. show(): remember to show dialog
         */
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes,positiveListener);
        builder.setNegativeButton(R.string.no, negativeListener);
        builder.show();

    }

    private void fillView() {
        /*
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
        /*
         * 1. instantiate a dateFormat object with the pattern of "yyyy-MM-dd HH:mm:ss"
         * 2. return the formated date string
         */
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void setToolbar() {
        /*
         * 1. inflateMenu(res/menu/menu.xml): set the flate menu items
         * 2. setNavigationOnClickListener()
         */

        itemToolbar.inflateMenu(R.menu.toolbar_menu);
        itemToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBackWithConfirm();
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

    private void setTextBold() {
        SpannableString spanText = new SpannableString(contentLine.getText());
        int start = contentLine.getSelectionStart();
        int end = contentLine.getSelectionEnd();
        spanText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        contentLine.setText(spanText);
        contentLine.setSelection(end);
    }

    private boolean findInList(String content, int position) {
        if(content.isEmpty())
            return false;
        char item = content.charAt(position);
        for (char stopChar : STOPCHARLIST)
            if (item == stopChar)
                return true;
        return false;
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
        createDialog("是否删除此便签", clickListenerDel, clickListenerNothing);
    }

    private void setBackWithConfirm() {
        /*
         * 1. if saveButton is visible, means you need to save before back
         * 2. else if the title and content are both empty, besides, the date is not empty, means
         *    that you delete the content
         * 3. others, go back with no modification
         */
        if(saveButton.isEnabled()) {
            createDialog("是否保存", clickListenerSave, clickListenerDiscard);
        }
        else if(titleLine.getText().toString().trim().isEmpty()
                && contentLine.getText().toString().trim().isEmpty()
                && !content[2].isEmpty()) {
            createDialog("是否删除此空白便签", clickListenerDel, clickListenerDiscard);
        }
        else
            setBack(false, UNMODIFIED);
    }

    // the exit for upper activity
    private void setBack(boolean modified, int actionFlag) {
        /*
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
        if(isisInput && !findInList(content, length - 1))
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
                }
                if(isInput && findInList(charSequence.toString(), start + count - 1)) {
                    contentStack.put(charSequence.toString(), start + count);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                isisInput = isInput;
                isInput = true;
            }
        });
    }
}
