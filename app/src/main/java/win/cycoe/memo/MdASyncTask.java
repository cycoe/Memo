package win.cycoe.memo;

import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zzhoujay.markdown.MarkDown;

/**
 * Created by cycoe on 17-8-12.
 */

public class MdASyncTask extends AsyncTask<String, Void, Spanned> {

    private TextView markdownView;
    private ProgressBar loadProgressBar;

    public MdASyncTask(TextView view, ProgressBar progressBar) {
        markdownView = view;
        loadProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loadProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Spanned doInBackground(String... strings) {
        return MarkDown.fromMarkdown(strings[0], null, markdownView);
    }

    @Override
    protected void onPostExecute(Spanned spannedText) {
        super.onPostExecute(spannedText);
        markdownView.setText(spannedText);
        loadProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
