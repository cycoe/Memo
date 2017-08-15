package win.cycoe.memo.Handler;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.lang.UCharacter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by cycoe on 7/26/17.
 */

class FileParser {

    private File file;
    private String[] titleList;
    private String[] contentList;
    private String[] dateList;
    private int count;

    public void initFile(String defaultFileDir) {
        file = new File(defaultFileDir + "/memo");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readFile() {

    }

    private void wirteFile() {

    }

}
