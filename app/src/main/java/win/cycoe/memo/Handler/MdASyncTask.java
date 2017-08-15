package win.cycoe.memo.Handler;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zzhoujay.richtext.RichText;
import com.zzhoujay.richtext.RichTextConfig;

import java.io.File;

/**
 * Created by cycoe on 17-8-12.
 */

public class MdASyncTask extends AsyncTask<String, Void, RichTextConfig.RichTextConfigBuild> {

    private TextView markdownView;
    private ProgressBar loadProgressBar;

    public MdASyncTask(TextView markdownView, ProgressBar loadProgressBar) {
        this.markdownView = markdownView;
        this.loadProgressBar = loadProgressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loadProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected RichTextConfig.RichTextConfigBuild doInBackground(String... strings) {
        RichText.initCacheDir(new File("/storage/emulated/0/Memo/"));
        return RichText.fromMarkdown(strings[0]);
    }

    @Override
    protected void onPostExecute(RichTextConfig.RichTextConfigBuild richText) {
        super.onPostExecute(richText);
        richText.into(markdownView);
        loadProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
