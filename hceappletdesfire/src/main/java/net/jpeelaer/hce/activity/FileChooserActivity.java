/*
 * Copyright 2013 Gerhard Klostermeier
 * Copyright 2015 Jeroen Peelaerts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package net.jpeelaer.hce.activity;

import java.io.File;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import net.jpeelaer.hce.R;

/**
 * A simple generic file chooser that lets the user choose a file from
 * a given directory. Optionally, it is also possible to delete files or to
 * create new ones. This Activity should be called via startActivityForResult()
 * with an Intent containing the {@link #EXTRA_DIR}.
 * The result codes are:
 * <ul>
 * <li>{@link Activity#RESULT_OK} - Everything is O.K. The chosen file will be
 * in the Intent ({@link #EXTRA_CHOSEN_FILE}).</li>
 * <li>1 - Directory from {@link #EXTRA_DIR} does not
 * exist.</li>
 * <li>2 - No directory specified in Intent
 * ({@link #EXTRA_DIR})</li>
 * <li>3 - External Storage is not read/writable. This error is
 * displayed to the user via Toast.</li>
 * <li>4 - Directory from {@link #EXTRA_DIR} is not a directory.</li>
 * </ul>
 * @author Gerhard Klostermeier
 */
public class FileChooserActivity extends Activity {

    // Input parameters.
    /**
     * Path to a directory with files. The files in the directory
     * are the files the user can choose from. This must be in the Intent.
     */
    public final static String EXTRA_DIR =
            "de.syss.MifareClassicTool.Activity.DIR";
    /**
     * The title of the activity. Optional.
     * e.g. "Open Dump File"
     */
    public final static String EXTRA_TITLE =
            "de.syss.MifareClassicTool.Activity.TITLE";
    /**
     * The small text above the files. Optional.
     * e.g. "Please choose a file:
     */
    public final static String EXTRA_CHOOSER_TEXT =
            "de.syss.MifareClassicTool.Activity.CHOOSER_TEXT";
    /**
     * The text of the choose button. Optional.
     * e.g. "Open File"
     */
    public final static String EXTRA_BUTTON_TEXT =
            "de.syss.MifareClassicTool.Activity.BUTTON_TEXT";
    /**
     * Enable/Disable the menu item  that allows the user to delete a file.
     * Optional. Boolean value. Disabled (false) by default.
     */
    public final static String EXTRA_ENABLE_DELETE_FILE =
            "de.syss.MifareClassicTool.Activity.ENABLE_DELETE_FILE";


    // Output parameter.
    /**
     * The file (with full path) that will be passed via Intent
     * to onActivityResult() method. The result code will be
     * {@link Activity#RESULT_OK}.
     */
    public final static String EXTRA_CHOSEN_FILE =
            "de.syss.MifareClassicTool.Activity.CHOSEN_FILE";
    /**
     * The filename (without path) that will be passed via Intent
     * to onActivityResult() method. The result code will be
     * {@link Activity#RESULT_OK}.
     */
    public final static String EXTRA_CHOSEN_FILENAME =
            "de.syss.MifareClassicTool.Activity.EXTRA_CHOSEN_FILENAME";


    private static final String LOG_TAG =
            FileChooserActivity.class.getSimpleName();
    private RadioGroup mGroupOfFiles;
    private Button mChooserButton;
    private TextView mChooserText;
    private MenuItem mDeleteFile;
    private File mDir;
    private boolean mIsDirEmpty;
    private boolean mCreateFileEnabled = false;
    private boolean mDeleteFileEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        mGroupOfFiles = (RadioGroup) findViewById(R.id.radioGroupFileChooser);
    }

    /**
     * Initialize the file chooser with the data from the calling Intent
     * (if external storage is mounted).
     * @see #EXTRA_DIR
     * @see #EXTRA_TITLE
     * @see #EXTRA_CHOOSER_TEXT
     * @see #EXTRA_BUTTON_TEXT
     */
    @Override
    public void onStart() {
        super.onStart();

        if (!ActivityUtil.isExternalStorageWritableErrorToast(this)) {
            setResult(3);
            finish();
            return;
        }
        mChooserText = (TextView) findViewById(
                R.id.textViewFileChooser);
        mChooserButton = (Button) findViewById(
                R.id.buttonFileChooserChoose);
        Intent intent = getIntent();

        // Set title.
        if (intent.hasExtra(EXTRA_TITLE)) {
            setTitle(intent.getStringExtra(EXTRA_TITLE));
        }
        // Set chooser text.
        if (intent.hasExtra(EXTRA_CHOOSER_TEXT)) {
            mChooserText.setText(intent.getStringExtra(EXTRA_CHOOSER_TEXT));
        }
        // Set button text.
        if (intent.hasExtra(EXTRA_BUTTON_TEXT)) {
            mChooserButton.setText(intent.getStringExtra(EXTRA_BUTTON_TEXT));
        }

        // Check path and initialize file list.
        if (intent.hasExtra(EXTRA_DIR)) {
            File path = new File(intent.getStringExtra(EXTRA_DIR));
            if (path.exists()) {
                if (!path.isDirectory()) {
                    setResult(4);
                    finish();
                    return;
                }
                mDir = path;
                mIsDirEmpty = updateFileIndex(path);
            } else {
                // Path does not exist.
                Log.e(LOG_TAG, "Directory for FileChooser does not exist.");
                setResult(1);
                finish();
                return;
            }
        } else {
            Log.d(LOG_TAG, "Directory for FileChooser was not in intent.");
            setResult(2);
            finish();
            return;
        }
    }

    /**
     * Add the menu to the Activity.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.file_chooser_functions, menu);
        mDeleteFile = menu.findItem(R.id.menuFileChooserDeleteFile);
        // Only use the enable/disable system for the delete file menu item
        // if there is a least one file.
        if (!mIsDirEmpty) {
            mDeleteFile.setEnabled(mDeleteFileEnabled);
        } else {
            mDeleteFile.setEnabled(false);
        }
        return true;
    }

    /**
     * Handle selected function form the menu (create new file, delete file).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.menuFileChooserDeleteFile:
                onDeleteFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Finish the Activity with an Intent containing
     * {@link #EXTRA_CHOSEN_FILE} and {@link #EXTRA_CHOSEN_FILENAME} as result.
     * You can catch that result by overriding onActivityResult() in the
     * Activity that called the file chooser via startActivityForResult().
     * @param view The View object that triggered the function
     * (in this case the choose file button).
     * @see #EXTRA_CHOSEN_FILE
     * @see #EXTRA_CHOSEN_FILENAME
     */
    public void onFileChosen(View view) {
        RadioButton selected = (RadioButton) findViewById(
                mGroupOfFiles.getCheckedRadioButtonId());
        Intent intent = new Intent();
        File file = new File(mDir.getPath(), selected.getText().toString());
        intent.putExtra(EXTRA_CHOSEN_FILE, file.getPath());
        intent.putExtra(EXTRA_CHOSEN_FILENAME, file.getName());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Update the file list and the components that depend on it
     * (e.g. disable the open file button if there is no file).
     * @param path Path to the directory which will be listed.
     * @return True if directory is empty. False otherwise.
     */
    private boolean updateFileIndex(File path) {
        File[] files = path.listFiles();
        Arrays.sort(files);
        mGroupOfFiles.removeAllViews();
        // Refresh file list.
        if (files.length > 0) {
            for(File f : files) {
                RadioButton r = new RadioButton(this);
                r.setText(f.getName());
                mGroupOfFiles.addView(r);
            }
            // Check first file.
            ((RadioButton)mGroupOfFiles.getChildAt(0)).setChecked(true);
            mChooserButton.setEnabled(true);
            if (mDeleteFile != null) {
                mDeleteFile.setEnabled(mDeleteFileEnabled);
            }
            return false;
        } else {
            // No files in directory.
            mChooserButton.setEnabled(false);
            if (mDeleteFile != null) {
                mDeleteFile.setEnabled(false);
            }
            mChooserText.setText(mChooserText.getText()
                    + "\n   --- "
                    + getString(R.string.text_no_files_in_chooser)
                    + " ---");
        }
        return true;
    }

    /**
     * Delete the selected file and update the file list.
     * @see #updateFileIndex(File)
     */
    private void onDeleteFile() {
        RadioButton selected = (RadioButton) findViewById(
                mGroupOfFiles.getCheckedRadioButtonId());
        File file  = new File(mDir.getPath(), selected.getText().toString());
        file.delete();
        mIsDirEmpty = updateFileIndex(mDir);
    }
}
