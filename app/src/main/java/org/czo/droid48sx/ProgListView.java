package org.czo.droid48sx;

import static org.czo.droid48sx.X48.sdcard_dir;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.icu.text.Collator;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.util.TypedValue;

public class ProgListView extends ListActivity {

    /**
     * text we use for the parent directory
     */
    private final static String PARENT_DIR = "..";
    /**
     * Currently displayed files
     */
    private final List<String> currentFiles = new ArrayList<String>();
    /**
     * Currently displayed directory
     */
    private File currentDir = null;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        // go to the root directory
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        //String last_dir = sp.getString("last_dir", "/storage/emulated/0/Download");
        //String last_dir = sp.getString("last_dir", "/sdcard");
        String last_dir = sp.getString("last_dir", sdcard_dir);

        Dlog.e("===================== ProgListView: " + last_dir);
        showDirectory(last_dir);
    }

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        if (position == 0 && PARENT_DIR.equals(this.currentFiles.get(0))) {
            showDirectory(this.currentDir.getParent());
        } else {
            final File file = new File(this.currentFiles.get(position));

            if (file.isDirectory()) {
                showDirectory(file.getAbsolutePath());
            } else {
                final Intent extras = new Intent();
                extras.putExtra("currentFile", this.currentFiles.get(position));
                setResult(RESULT_OK, extras);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                Editor e = sp.edit();
                e.putString("last_dir", file.getParent());
                e.commit();
                finish();
            }
        }
    }

    /**
     * Show the contents of a given directory as a selectable list
     *
     * @param path the directory to display
     */
    private void showDirectory(final String path) {
        // we clear any old content and add an entry to get up one level
        this.currentFiles.clear();
        this.currentDir = new File(path);
        if (this.currentDir.getParentFile() != null) {
            this.currentFiles.add(PARENT_DIR);
        }

        // get all directories and C64 files in the given path
        final File[] files = this.currentDir.listFiles();
        final Set<String> sortedfile = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        final Set<String> sortedfolder = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

        if (files != null) {
            //Arrays.sort(files);
            for (final File file : files) {
                final String name = file.getAbsolutePath();

                if (file.isDirectory()) {
                    sortedfolder.add(name);
                } else {
                    sortedfile.add(name);
                }
            }
        }

        this.currentFiles.addAll(sortedfolder);
        this.currentFiles.addAll(sortedfile);

        // display these images
        final Context context = this;

        ArrayAdapter<String> filenamesAdapter = new ArrayAdapter<String>(this, R.layout.file_row, this.currentFiles) {

            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {
                return new IconifiedTextLayout(context, getItem(position), position);
            }
        };

        setListAdapter(filenamesAdapter);
    }

    // new layout displaying a text and an associated image
    class IconifiedTextLayout extends LinearLayout {

        public IconifiedTextLayout(final Context context, final String path, final int position) {
            super(context);

            setOrientation(HORIZONTAL);

            // determine icon to display
            final ImageView imageView = new ImageView(context);
            final File file = new File(path);

            // create view for the directory name
            final TextView textView = new TextView(context);


            if (position == 0 && PARENT_DIR.equals(path)) {
                imageView.setImageResource(R.drawable.ic_action_folder);
                imageView.setColorFilter(0x77ffffff);
                textView.setText(file.getName());
            } else {
                if (file.isDirectory()) {
                    imageView.setImageResource(R.drawable.ic_action_folder);
                    imageView.setColorFilter(0x77ffffff);
                    textView.setText(file.getName());
                } else {
                    imageView.setImageResource(R.drawable.ic_action_file);
                    imageView.setColorFilter(0x77ffffff);
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(file.lastModified()));
                    String size = file.length()/1024 + "KB";
                    textView.setText(file.getName() + " [ " + date + ", " + size + " ]");
                }
            }

            int p = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            // imageView.setPadding(p,p,p,p);

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            // setMargins (int left, int top, int right, int bottom)
            layoutParams.setMargins(p, p, p, p);

            // addView(imageView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
            // LayoutParams.WRAP_CONTENT));
            addView(imageView, layoutParams);
            addView(textView, layoutParams);
        }
    }

}
