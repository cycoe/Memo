package win.cycoe.memo;

import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private final String[] HEADERLIST = {"title", "content", "date"};

    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, Object>> dataList;
//    private String[][] contentList;
//    private String[] contentTemp;
//    private int[] idList;
    private Intent intent;
    private SQLiteDatabase db;
    private DatabaseHandler dbHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
         * 初始化一个意图 (intent)，跳转至 ContentActivity
         * 为 fab 按钮设置点击监听
         * 为 intent 放置额外数据 content
         *
         * startActivityForResult(intent, requestcode);
         * 启动一个带返回值的 activity
         */
        intent = new Intent(this, ContentActivity.class);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("content", new String[] {"", "", ""});
                startActivityForResult(MainActivity.this.intent, 0);
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        initDatabase();
        listView = (ListView) findViewById(R.id.listView);
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

    private void fillListView() {
        dataList = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this,
                getData(),
                R.layout.listitem_layout,
                HEADERLIST,
                new int[] {R.id.title, R.id.content, R.id.date});
        /*
         * SimpleAdapter(context, data, resource, from, to)
         * context : 上下文对象
         * data : 特定的泛型的集合，map 组成的 list 集合，每个 map 中的键必须包含 from 中指定的键
         * resource ： 列表项的布局文件 ID
         * from ： 记录 map 中的键名
         * to ： 绑定数据视图中的 ID，与 from 的对应关系
         */

        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(this);
    }

    private void refreshListView() {
        dbHandler.readDatabase();
        dataList.removeAll(dataList);
        dataList = getData();
        simpleAdapter.notifyDataSetChanged();
    }

    private List<Map<String, Object>> getData() {
        for(int i = 0; i < dbHandler.contentList.length; i++) {
            Map<String, Object>map = new HashMap<String, Object>();
            for(int j = 0; j < HEADERLIST.length; j++)
                map.put(HEADERLIST[j], dbHandler.contentList[i][j]);
            dataList.add(map);
        }
        return dataList;
    }

    private void initDatabase() {
        db = openOrCreateDatabase("memo.db", MODE_PRIVATE, null);
        dbHandler = new DatabaseHandler(db, "memotb");
        dbHandler.createTable();
        dbHandler.readDatabase();
    }

//    private void addItemInDatabase(String[] content) {
//        SQLiteDatabase db = openOrCreateDatabase("memo.db", MODE_PRIVATE, null);
//        ContentValues contentValues = new ContentValues();
//        for(int i = 0; i < HEADERLIST.length; i++)
//            contentValues.put(HEADERLIST[i], content[i]);
//        db.insert("memotb", null, contentValues);
//        db.close();
//    }
//
//    private void modifyItemInDatabase(int selectedItem, String[] content) {
//        SQLiteDatabase db = openOrCreateDatabase("memo.db", MODE_PRIVATE, null);
//        ContentValues contentValues = new ContentValues();
//        for(int i = 0; i < HEADERLIST.length; i++)
//            contentValues.put(HEADERLIST[i], content[i]);
//        String whereClause = "_id=?";
//        String[] whereArgs = new String[] {String.valueOf(selectedItem)};
//        db.update("memotb", contentValues, whereClause, whereArgs);
//        db.close();
//    }
//
//    private void deleteItemInDatabase(int selectedItem) {
//        SQLiteDatabase db = openOrCreateDatabase("memo.db", MODE_PRIVATE, null);
//        String whereClause = "_id=?";
//        String[] whereArgs = new String[] {String.valueOf(selectedItem)};
//        db.delete("memotb", whereClause, whereArgs);
//        db.close();
//    }
//
//    private void createDatabase() {
//        SQLiteDatabase db = openOrCreateDatabase("memo.db", MODE_PRIVATE, null);
//        db.execSQL("create table if not exists memotb (_id integer primary key autoincrement, title text not null, content text not null, date text not null)");
//        db.close();
//    }
//
//    private void readDatabase() {
//        SQLiteDatabase db = openOrCreateDatabase("memo.db", MODE_PRIVATE, null);
//        Cursor cr = db.rawQuery("select * from memotb ORDER BY date desc", null);
//        idList = new int[cr.getCount()];
//        contentList = new String[cr.getCount()][4];
//        if(cr != null) {
//            for(int i = 0; cr.moveToNext(); i++) {
//                idList[i] = cr.getInt(cr.getColumnIndex("_id"));
//                for(int j = 0; j < HEADERLIST.length; j++)
//                    contentList[i][j] = cr.getString(cr.getColumnIndex(HEADERLIST[j]));
//            }
//        }
//
//        cr.close();
//        db.close();
//    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        intent.putExtra("content", dbHandler.contentList[position]);
        startActivityForResult(MainActivity.this.intent, position + 1);
    }

    private void itemOnLongClick() {
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(0, 0, 0, R.string.delete);
                contextMenu.add(0, 1, 0, "复制到剪贴板");
            }
        });
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = (int) info.id;
        switch (item.getItemId()) {
            case 0:
                dbHandler.deleteItemInDatabase(position);
                refreshListView();
                Snackbar.make(listView, "是否撤销删除", Snackbar.LENGTH_LONG)
                        .setAction("撤销", new View.OnClickListener(){
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

        return super.onContextItemSelected(item);
    }
}
