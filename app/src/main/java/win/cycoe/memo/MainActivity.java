package win.cycoe.memo;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private final String[] HEADERLIST = {"title", "content", "date"};

    private ListView listView;
    private ListView tableView;
    private EditText tableNameInput;
    private ImageButton newTableButton;
    private SimpleAdapter simpleAdapter;
    private Intent intent;
    private SQLiteDatabase db;
    private DatabaseHandler dbHandler;
    private DialogBuiler builer;
    private Finder finder;

    private DialogInterface.OnClickListener clickListenerNewTab;
    private DialogInterface.OnClickListener clickListenerRenameTab;
    private DialogInterface.OnClickListener clickListenerDelTab;
    private DialogInterface.OnClickListener clickListenerNothing;

    private ArrayAdapter<String> arrayAdapter;
    private List<Map<String, Object>> listViewData;
    private ArrayList<String> tableViewData;
    private int currentPos = 0;
    private int position;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        initData();
        initDatabase();
        initView();
        initDialogListener();
        setToolBar();
        setFloatButton();
        fillTableView();
        fillListView();
        itemOnLongClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 1) {
            if(requestCode == 0)
                dbHandler.addItemInDatabase(data.getStringArrayExtra("content"));
            else
                dbHandler.modifyItemInDatabase(requestCode - 1, data.getStringArrayExtra("content"));
        }
        else if(resultCode == 2 && requestCode != 0)
            dbHandler.deleteItemInDatabase(requestCode - 1);
        refreshListView();
    }

    private void initData() {
        intent = new Intent(this, ContentActivity.class);
        builer = new DialogBuiler(this);
        finder = new Finder();
    }

    private void initDialogListener() {
        clickListenerNothing = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        };
        clickListenerNewTab = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!tableNameInput.getText().toString().isEmpty()) {
                    if(finder.findInList(dbHandler.tbList, tableNameInput.getText().toString()))
                        Snackbar.make(tableView, "该分类已存在", Snackbar.LENGTH_SHORT).show();
                    else {
                        dbHandler.createTable(tableNameInput.getText().toString());
                        refreshTableView();
                    }
                }
                else
                    Snackbar.make(tableView, "分类名称不能为空", Snackbar.LENGTH_SHORT).show();
            }
        };
        clickListenerRenameTab = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!tableNameInput.getText().toString().isEmpty()) {
                    if(finder.findInList(dbHandler.tbList, tableNameInput.getText().toString()))
                        builer.createDialog("提示", "该分类已存在", clickListenerNothing);
                    else {
                        dbHandler.renameTable(tableNameInput.getText().toString(), position);
                        refreshTableView();
                        setTitle(dbHandler.tbList[position]);
                    }
                }
                else
                    builer.createDialog("提示", "分类名称不能为空", clickListenerNothing);
            }
        };
        clickListenerDelTab = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbHandler.deleteTable(position);
                refreshTableView();
                if(currentPos == position) {
                    currentPos = 0;
                    dbHandler.handle(dbHandler.tbList[currentPos]);
                    refreshListView();
                    tableView.setItemChecked(currentPos, true);
                    setTitle(dbHandler.tbList[currentPos]);
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    }
                }
            }
        };
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView);
        tableView = (ListView) findViewById(R.id.tableView);
        newTableButton = (ImageButton) findViewById(R.id.newTabButton);
        tableNameInput = new EditText(this);
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if(source.equals(" "))
                    return "";
                else
                    return null;
            }
        };
        tableNameInput.setFilters(new InputFilter[] {filter});
        tableNameInput.setInputType(InputType.TYPE_CLASS_TEXT);

        newTableButton.setOnClickListener(this);
    }

    private void setFloatButton() {
        /**
         * 初始化一个意图 (intent)，跳转至 ContentActivity
         * 为 fab 按钮设置点击监听
         * 为 intent 放置额外数据 content
         *
         * startActivityForResult(intent, requestcode);
         * 启动一个带返回值的 activity
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("content", new String[] {"", "", ""});
                startActivityForResult(MainActivity.this.intent, 0);
            }
        });
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void fillListView() {
        listViewData = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this,
                getListViewData(),
                R.layout.listitem_layout,
                HEADERLIST,
                new int[] {R.id.title, R.id.content, R.id.date});
        /**
         * SimpleAdapter(context, data, resource, from, to)
         * context : 上下文对象
         * data : 特定的泛型的集合，map 组成的 list 集合，每个 map 中的键必须包含 from 中指定的键
         * resource ： 列表项的布局文件 ID
         * from ： 记录 map 中的键名
         * to ： 绑定数据视图中的 ID，与 from 的对应关系
         */

        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(this);
        setTitle(dbHandler.tbList[currentPos]);
    }

    private void fillTableView() {
        tableViewData = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, getTableViewData());
        tableView.setAdapter(arrayAdapter);
        tableView.setOnItemClickListener(this);
        tableView.setItemChecked(currentPos, true);
    }

    private void refreshListView() {
        dbHandler.readDatabase();
        listViewData.removeAll(listViewData);
        listViewData = getListViewData();
        simpleAdapter.notifyDataSetChanged();
    }

    private void refreshTableView() {
        dbHandler.readTables();
        tableViewData.removeAll(tableViewData);
        tableViewData = getTableViewData();
        arrayAdapter.notifyDataSetChanged();
    }

    private List<Map<String, Object>> getListViewData() {
        for(int i = 0; i < dbHandler.contentList.length; i++) {
            Map<String, Object>map = new HashMap<String, Object>();
            for(int j = 0; j < HEADERLIST.length; j++)
                map.put(HEADERLIST[j], dbHandler.contentList[i][j]);
            listViewData.add(map);
        }
        return listViewData;
    }

    private ArrayList<String> getTableViewData() {
        for(int i = 0; i < dbHandler.tbList.length; i++) {
            tableViewData.add(dbHandler.tbList[i]);
        }
        return tableViewData;
    }

    private void initDatabase() {
        db = openOrCreateDatabase("memo.db", MODE_PRIVATE, null);
        dbHandler = new DatabaseHandler(db);
        dbHandler.readTables();
        if(dbHandler.tbList.length > 0)
            dbHandler.handle(dbHandler.tbList[currentPos]);
        else {
            dbHandler.createTable("默认");
            dbHandler.handle("默认");
        }

        dbHandler.readDatabase();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()) {
            case R.id.listView:
                intent.putExtra("content", dbHandler.contentList[position]);
                startActivityForResult(MainActivity.this.intent, position + 1);
                break;
            case R.id.tableView:
                dbHandler.handle(dbHandler.tbList[position]);
                refreshListView();
                setTitle(dbHandler.tbList[position]);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                currentPos = position;
                break;
        }
    }

    private void itemOnLongClick() {

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(0, 0, 0, R.string.delete);
                contextMenu.add(0, 1, 0, R.string.copyToClipBoard);
            }
        });

        tableView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(1, 0, 0, R.string.rename);
                if(dbHandler.tbList.length > 1)
                    contextMenu.add(1, 1, 0, R.string.delete);
            }
        });
    }

    private void listViewContextClick(int itemId, int position) {
        switch (itemId) {
            case 0:
                dbHandler.deleteItemInDatabase(position);
                refreshListView();
                Snackbar.make(listView, "是否撤销删除", Snackbar.LENGTH_LONG)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dbHandler.addItemInDatabase(dbHandler.contentTemp);
                                refreshListView();
                            }
                        })
                        .show();
                break;

            case 1:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(dbHandler.contentList[position][1]);
                Snackbar.make(listView, "已复制到剪贴板", Snackbar.LENGTH_SHORT).show();
                break;
        }
    }

    private void tableViewContentClick(int itemId, final int position) {
        this.position = position;
        switch (itemId) {
            case 0:
                tableNameInput.setHint(dbHandler.tbList[position]);
                builer.createDialog("提示", "请输入新分类的名称", clickListenerRenameTab, clickListenerNothing, tableNameInput);
                break;

            case 1:
                builer.createDialog("警告", "是否删除此分类", clickListenerDelTab, clickListenerNothing);
                break;
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = (int) info.id;
        switch(item.getGroupId()) {
            case 0:
                listViewContextClick(item.getItemId(), position);
                break;
            case 1:
                tableViewContentClick(item.getItemId(), position);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newTabButton:
                tableNameInput.setHint("");
                builer.createDialog("提示", "输入新建分类名称", clickListenerNewTab, clickListenerNothing, tableNameInput);
                break;
        }
    }
}
