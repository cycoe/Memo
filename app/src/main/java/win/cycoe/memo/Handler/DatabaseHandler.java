package win.cycoe.memo.Handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by cycoe on 17-8-12.
 */

public class DatabaseHandler {

    private final String[] HEADERLIST = {"title", "content", "date"};

    public String[][] contentList;
    public String[] contentTemp;
    public int[] idList;
    public String[] tbList;
    private String tb;

    private SQLiteDatabase db;

    public DatabaseHandler() {}

    public DatabaseHandler(SQLiteDatabase database) {
        db = database;
    }

    public void handle(String tbName) {
        tb = tbName;
    }

    public void addItemInDatabase(String[] content) {
        ContentValues contentValues = new ContentValues();
        for(int i = 0; i < HEADERLIST.length; i++)
            contentValues.put(HEADERLIST[i], content[i]);
        db.insert(tb, null, contentValues);
    }

    public void modifyItemInDatabase(int postion, String[] content) {
        ContentValues contentValues = new ContentValues();
        for(int i = 0; i < HEADERLIST.length; i++)
            contentValues.put(HEADERLIST[i], content[i]);
        String whereClause = "_id=?";
        String[] whereArgs = new String[] {String.valueOf(idList[postion])};
        db.update(tb, contentValues, whereClause, whereArgs);
    }

    public void deleteItemInDatabase(int position) {
        String whereClause = "_id=?";
        String[] whereArgs = new String[] {String.valueOf(idList[position])};
        db.delete(tb, whereClause, whereArgs);
        contentTemp = contentList[position];
    }

    public void readDatabase(String[] filter) {
        String queryString;
        String[] argvList;
        if(filter == null) {
            queryString = null;
            argvList = null;
        } else {
            queryString = "";
            argvList = new String[HEADERLIST.length * filter.length];
            for (int i = 0; i < HEADERLIST.length; i++) {
                for (int j = 0; j < filter.length; j++) {
                    queryString += HEADERLIST[i];
                    queryString += " LIKE ?";
                    if (j < filter.length - 1)
                        queryString += " and ";
                    argvList[i * filter.length + j] = "%" + filter[j] + "%";
                }
                if (i < HEADERLIST.length - 1)
                    queryString += " or ";
            }
        }
        Cursor cr = db.query(tb, null, queryString, argvList, null, null, "date DESC");
        idList = new int[cr.getCount()];
        contentList = new String[cr.getCount()][3];
        for(int i = 0; cr.moveToNext(); i++) {
            idList[i] = cr.getInt(cr.getColumnIndex("_id"));
            for(int j = 0; j < HEADERLIST.length; j++)
                contentList[i][j] = cr.getString(cr.getColumnIndex(HEADERLIST[j]));
        }

        cr.close();
    }

    public void readTables() {
        Cursor cr = db.rawQuery("select name from sqlite_master where type='table'", null);
        tbList = new String[cr.getCount()];
        for(int i = 0; cr.moveToNext(); i++)
            tbList[i] = cr.getString(0);

        cr.close();
        List<String> tbListTemp = new ArrayList<>();
        for(String tbName : tbList) {
            if(!tbName.equals("android_metadata") && !tbName.equals("sqlite_sequence"))
                tbListTemp.add(tbName);
        }
        tbList = tbListTemp.toArray(new String[0]);
    }

    public void createTable(String newTableName) {
        db.execSQL("create table if not exists " + newTableName + " (_id integer primary key autoincrement, title text not null, content text not null, date text not null)");
    }

    public void deleteTable(int position) {
        db.execSQL("DROP TABLE " + tbList[position]);
    }

    public void renameTable(String newTableName, int position) {
        db.execSQL("ALTER TABLE " + tbList[position] + " RENAME TO " + newTableName);
    }

    public boolean exportDb(boolean direction) {
        try {
            int bytesum = 0;
            int byteread = 0;
            String INTERPATH = Environment.getDataDirectory().toString() + "/data/win.cycoe.memo/databases/memo.db";
            String OUTERPATH = Environment.getExternalStorageDirectory().toString() + "/Memo/memo.db";
            File backupDir = new File(Environment.getExternalStorageDirectory().toString() + "/Memo");
            if (!backupDir.exists())
                backupDir.mkdir();
            InputStream inStream = new FileInputStream(direction ? INTERPATH : OUTERPATH);
            FileOutputStream outStream = new FileOutputStream(direction ? OUTERPATH : INTERPATH);
            byte[] buffer = new byte[1024];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                outStream.write(buffer, 0, byteread);
            }
            inStream.close();
            outStream.flush();
            outStream.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
