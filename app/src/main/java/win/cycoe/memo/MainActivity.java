package win.cycoe.memo;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
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

import java.util.ArrayList;
import java.util.List;

import win.cycoe.memo.Handler.ConfigHandler;
import win.cycoe.memo.Handler.DatabaseHandler;
import win.cycoe.memo.Handler.DateParser;
import win.cycoe.memo.Handler.Finder;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private final String[] permissions = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ListView listView;
    private ListView tableView;
    private EditText tableNameInput;
    private ImageButton newTableButton;
    private SearchView searchView;

    private MainAdapter mainAdapter;
    private Intent intent;
    private SQLiteDatabase db;
    private DatabaseHandler dbHandler;
    private DialogBuiler builer;
    private Finder finder;
    private DateParser dateParser;
    private InputFilter filter;

    private DialogInterface.OnClickListener clickListenerNewTab;
    private DialogInterface.OnClickListener clickListenerRenameTab;
    private DialogInterface.OnClickListener clickListenerDelTab;
    private DialogInterface.OnClickListener clickListenerNothing;

    private ArrayAdapter<String> arrayAdapter;
    private List<MainBean> mainBeanList;
    private List<String> tableViewData;
    private int currentTable = 0;
    private int selectTable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getSharedPreferences("config", MODE_PRIVATE);
        ConfigHandler configHandler = new ConfigHandler(pref);
        setTheme(configHandler.getValue("darkTheme") == 1 ? R.style.AppTheme_dark : R.style.AppTheme);
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
        dateParser = new DateParser();
        // request storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(permissions, 321);
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
                        Snackbar.make(tableView, "该分类已存在", Snackbar.LENGTH_SHORT).show();
                    else {
                        dbHandler.renameTable(tableNameInput.getText().toString(), selectTable);
                        refreshTableView();
                        setTitle(dbHandler.tbList[selectTable]);
                    }
                }
                else
                    Snackbar.make(tableView, "分类名称不能为空", Snackbar.LENGTH_SHORT).show();
            }
        };
        clickListenerDelTab = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbHandler.deleteTable(selectTable);
                refreshTableView();
                if(currentTable == selectTable) {
                    currentTable = 0;
                    dbHandler.handle(dbHandler.tbList[currentTable]);
                    refreshListView();
                    tableView.setItemChecked(currentTable, true);
                    setTitle(dbHandler.tbList[currentTable]);
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
        filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if(source.equals(" "))
                    return "";
                else
                    return null;
            }
        };

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

    /**
     * SimpleAdapter(context, data, resource, from, to)
     * context : 上下文对象
     * data : 特定的泛型的集合，map 组成的 list 集合，每个 map 中的键必须包含 from 中指定的键
     * resource ： 列表项的布局文件 ID
     * from ： 记录 map 中的键名
     * to ： 绑定数据视图中的 ID，与 from 的对应关系
     */
    private void fillListView() {
        mainBeanList = new ArrayList<>();
        mainAdapter = new MainAdapter(this, getListViewData());
        listView.setAdapter(mainAdapter);
        listView.setOnItemClickListener(this);
        setTitle(dbHandler.tbList[currentTable]);
    }

    private void fillTableView() {
        tableViewData = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, R.layout.simple_list_item_single_choice, getTableViewData());
        tableView.setAdapter(arrayAdapter);
        tableView.setOnItemClickListener(this);
        tableView.setItemChecked(currentTable, true);
    }

    private void refreshListView() {
        dbHandler.readDatabase();
        mainBeanList.removeAll(mainBeanList);
        getListViewData();
        mainAdapter.notifyDataSetChanged();
    }

    private void refreshTableView() {
        dbHandler.readTables();
        tableViewData.removeAll(tableViewData);
        getTableViewData();
        arrayAdapter.notifyDataSetChanged();
    }

    private List<MainBean> getListViewData() {
        for(int i = 0; i < dbHandler.contentList.length; i++)
            mainBeanList.add(new MainBean(dbHandler.contentList[i][0],
                    dbHandler.contentList[i][1],
                    dateParser.getRelativeTime(dbHandler.contentList[i][2])));
        return mainBeanList;
    }

    private List<String> getTableViewData() {
        for(int i = 0; i < dbHandler.tbList.length; i++)
            tableViewData.add(dbHandler.tbList[i]);
        return tableViewData;
    }

    private void initDatabase() {
        db = openOrCreateDatabase("memo.db", MODE_PRIVATE, null);
        dbHandler = new DatabaseHandler(db);
        dbHandler.readTables();
        if(dbHandler.tbList.length == 0)
            dbHandler.createTable("默认");
        dbHandler.readTables();
        dbHandler.handle(dbHandler.tbList[currentTable]);
        dbHandler.readDatabase();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else if(!searchView.isIconified())
            searchView.setIconified(true);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mainBeanList.removeAll(mainBeanList);

                for(int i = 0; i < dbHandler.contentList.length; i++) {
                    if(finder.findAllInString(dbHandler.contentList[i][0], newText.split(" "))
                            || finder.findAllInString(dbHandler.contentList[i][1], newText.split(" "))
                            || finder.findAllInString(dateParser.getRelativeTime(dbHandler.contentList[i][2]), newText.split(" ")))
                        mainBeanList.add(new MainBean(dbHandler.contentList[i][0],
                                dbHandler.contentList[i][1],
                                dateParser.getRelativeTime(dbHandler.contentList[i][2])));
                }

                mainAdapter.notifyDataSetChanged();
                return true;
            }
        });

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
            startActivityForResult(intent, 0);
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
                currentTable = position;
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
        this.selectTable = position;
        switch (itemId) {
            case 0:
                tableNameInput = new EditText(this);
                tableNameInput.setFilters(new InputFilter[] {filter});
                tableNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
                tableNameInput.setHint(dbHandler.tbList[position]);
                builer.createDialog("提示", "请输入新分类的名称", clickListenerRenameTab, clickListenerNothing, tableNameInput);
                break;

            case 1:
                builer.createDialog("警告", "是否删除此分类", clickListenerDelTab, clickListenerNothing, null);
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
                tableNameInput = new EditText(this);
                tableNameInput.setFilters(new InputFilter[] {filter});
                tableNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
                builer.createDialog("提示", "输入新建分类名称", clickListenerNewTab, clickListenerNothing, tableNameInput);
                break;
        }
    }
}
