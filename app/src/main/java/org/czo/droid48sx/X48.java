package org.czo.droid48sx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class X48 extends Activity {

    private HPView mainView;
    private boolean need_to_quit;
    public static String config_dir;
    static final private int LOAD_ID = Menu.FIRST + 1;
    static final private int SAVE_ID = Menu.FIRST + 2;
    static final private int QUIT_ID = Menu.FIRST + 3;
    static final private int RESET_ID = Menu.FIRST + 4;
    static final private int SETTINGS_ID = Menu.FIRST + 5;
    static final private int LITEKBD_ID = Menu.FIRST + 6;
    static final private int SAVE_CHECKPOINT_ID = Menu.FIRST + 7;
    static final private int RESTORE_CHECKPOINT_ID = Menu.FIRST + 8;
    static final private int FULL_RESET_ID = Menu.FIRST + 9;
    static final private int ROM_ID = 123;
    private static EmulatorThread thread;

    private static final String ACTION_FULL_RESET = "android.intent.action.FULL_RESET";
    private static final String ACTION_RESTORE_CHECKPOINT = "android.intent.action.RESTORE_CHECKPOINT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("x48", "===================== starting activity");

        // /sdcard
        if (getExternalFilesDir(null) != null) {
            config_dir = getExternalFilesDir(null).getAbsolutePath() + "/";
        } else {
            config_dir = getFilesDir().getAbsolutePath() + "/";
        }
        // config_dir = "/badone" ;
        File hpDir = new File(config_dir);
        if (!hpDir.exists() || !hpDir.isDirectory()) {
            Log.d("x48", "===================== ERROR: cannot open " + config_dir);
            Toast.makeText(getApplicationContext(), "ERROR: cannot open " + config_dir, Toast.LENGTH_SHORT).show();
        }

        Log.d("x48", "config_dir java: " + config_dir);
        getExternalPath(config_dir);

        Log.d("x48", "copyAsset");
        AssetUtil.copyAsset(getResources().getAssets(), false);

        Log.d("x48", "================== getIntentAction = " + getIntent().getAction());
        if (ACTION_FULL_RESET.equals(getIntent().getAction())) {
            fullReset();
        }
        if (ACTION_RESTORE_CHECKPOINT.equals(getIntent().getAction())) {
            restoreCheckpoint();
        }

        readyToGo();
        if (!AssetUtil.isFilesReady()) {
            showDialog(DIALOG_ROM_KO);
        }
    }

    public void readyToGo() {

        if (Build.VERSION.SDK_INT >= 11) {
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.rgb(0, 0, 0));
            getWindow().setNavigationBarColor(Color.parseColor("#393938"));
            // getWindow().setStatusBarColor(Color.parseColor("#AA252523"));
            // getWindow().setNavigationBarColor(Color.parseColor("#AA252523"));
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#AA252523")));
        }

        if (Build.VERSION.SDK_INT < 11) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        setContentView(R.layout.main);
        mainView = (HPView) findViewById(R.id.hpview);

        checkPrefs();

        thread = new EmulatorThread(this);
        thread.start();
        mainView.resume();
    }

    private static boolean hide = false;
    private static boolean hidekey = false;

    public void Menu() {
        if (Build.VERSION.SDK_INT < 11) {
            openOptionsMenu();
        } else {
            hide ^= true;
            hidekey = true;
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (Build.VERSION.SDK_INT < 19) {
                if (hide) {
                    getActionBar().hide();
                    if (mPrefs.getBoolean("fullScreen", false)) {
                        mainView.setSystemUiVisibility(
                                HPView.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | HPView.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    }
                } else { // FIXME:
                    getActionBar().show();
                    if (mPrefs.getBoolean("fullScreen", false)) {
                        mainView.setSystemUiVisibility(
                                HPView.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | HPView.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    }
                }
            } else { // >=19
                if (hide) { // hide action bar
                    getActionBar().hide();
                    if (mPrefs.getBoolean("fullScreen", false)) {
                        mainView.setSystemUiVisibility(
                                HPView.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | HPView.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | HPView.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | HPView.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                        | HPView.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                        // | HPView.SYSTEM_UI_FLAG_IMMERSIVE);
                                        | HPView.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                } else { // show action bar
                    if (mPrefs.getBoolean("fullScreen", false)) {
                        mainView.setSystemUiVisibility(
                                HPView.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | HPView.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | HPView.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    }
                    getActionBar().show();
                }
            }
        }
    }

    // This snippet hides the system bars.
    public void hideSystemUI() {
        hide = true;
        if (Build.VERSION.SDK_INT >= 11) {
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#AA252523"));
            getWindow().setNavigationBarColor(Color.parseColor("#AA252523"));
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#AA252523")));
        }
        if (Build.VERSION.SDK_INT >= 19) {
            if (mainView != null) {
                mainView.setSystemUiVisibility(
                        HPView.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | HPView.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | HPView.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | HPView.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | HPView.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | HPView.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                getActionBar().hide();
            }
        } else {
            getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    // This snippet shows the system bars.
    public void showSystemUI() {
        hide = true;
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.rgb(0, 0, 0));
            getWindow().setNavigationBarColor(Color.parseColor("#393938"));
        }
        if (Build.VERSION.SDK_INT >= 19) {
            if (mainView != null) {
                mainView.setSystemUiVisibility(
                        HPView.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                getActionBar().hide();
            }
        } else {
            getWindow().addFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void hideActionBar() {
        if (hidekey) {
            checkfullscreen();
            hidekey = false;
        }
    }

    public void checkfullscreen() {
        Log.d("x48", "===================== checkfullscreen");
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (mPrefs.getBoolean("fullScreen", false)) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    public void checkPrefs() {
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        saveonExit = mPrefs.getBoolean("saveOnExit", true);
        if (mainView != null) {
            mainView.setKeybLite(mPrefs.getBoolean("keybLite", false));
            mainView.setHapticFeedbackEnabled(mPrefs.getBoolean("haptic", false));
            mainView.setSound(mPrefs.getBoolean("sound", false));
            mainView.setFullWidth(mPrefs.getBoolean("large_width", false));
            mainView.setScaleControls(mPrefs.getBoolean("scale_buttons", true));
        }
        checkfullscreen();
        mainView.requestLayout();
    }

    public void changeKeybLite() {
        if (mainView != null) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            mainView.setKeybLite(!mainView.isKeybLite());
            Editor e = mPrefs.edit();
            e.putBoolean("keybLite", mainView.isKeybLite());
            e.commit();
            mainView.backBuffer = null;
            mainView.needFlip = true;
        }
    }

    public void refreshMainScreen(short data[]) {
        mainView.refreshMainScreen(data);
    }

    public int waitEvent() {
        return mainView.waitEvent();
    }

    public void refreshIcons(boolean i[]) {
        mainView.refreshIcons(i);
    }

    public native void startHPEmulator();

    public native void resetHPEmulator();

    public native void saveState();

    public native void stopHPEmulator();

    public native int buttonPressed(int code);

    public native int buttonReleased(int code);

    public native void registerClass(X48 instance);

    public native int fillAudioData(short data[]);

    public native int fillScreenData(short data[], boolean ann[]);

    public native void flipScreen();

    public native int loadProg(String filename);

    public native void getExternalPath(String path);

    public native void setBlankColor(short s);

    public void emulatorReady() {
        mainView.emulatorReady();
    }

    public void pauseEvent() {
        mainView.pauseEvent();
    }

    static {
        System.loadLibrary("droid48sx");
    }

    @Override
    protected void onResume() {
        Log.d("x48", "===================== resume");
        super.onResume();
        if (mainView != null) {
            mainView.resume();
            checkfullscreen();
            hidekey = false;
            mainView.requestLayout();
        }
    }

    /**
     * Called when your activity's options menu needs to be created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item;
        super.onCreateOptionsMenu(menu);
        // We are going to create two menus. Note that we assign them
        // unique integer IDs, labels from our string resources, and
        // given them shortcuts.
        // menu.add(0, RESET_ID, 0, R.string.reset);

        // menu.add(0, LITEKBD_ID, 0, R.string.toggle_lite_keyb);
        // menu.add(0, SAVE_ID, 0, R.string.save_state);

        item = menu.add(0, FULL_RESET_ID, 0, R.string.full_reset_memory);
        item.setIcon(R.drawable.ic_delete_white_24dp);
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        item = menu.add(0, RESTORE_CHECKPOINT_ID, 0, R.string.restore_checkpoint);
        item.setIcon(R.drawable.ic_restore_white_24dp);
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        item = menu.add(0, SAVE_CHECKPOINT_ID, 0, R.string.save_checkpoint);
        item.setIcon(R.drawable.ic_archive_white_24dp);
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        item = menu.add(0, SETTINGS_ID, 0, R.string.settings);
        item.setIcon(R.drawable.ic_settings_white_24dp);
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        item = menu.add(0, LOAD_ID, 0, R.string.load_prog);
        item.setIcon(R.drawable.ic_playlist_add_white_24dp);
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        item = menu.add(0, QUIT_ID, 0, R.string.button_quit);
        item.setIcon(R.drawable.ic_power_settings_new_white_24dp);
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        return true;
    }

    public void deleteFile(File src) throws IOException {
        if (src.exists()) {
            src.delete();
        }
    }

    public void copyFile(File src, File dst) throws IOException {
        if (src.exists()) {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    protected void restoreCheckpoint() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor spe = mPrefs.edit();

        Log.d("x48", "===================== Checkpoint restored...");
        Toast.makeText(getApplicationContext(), "Checkpoint restored...", Toast.LENGTH_SHORT).show();

        try {
            for (String s : new String[] { "port1", "port2" }) {
                deleteFile(new File(X48.config_dir + s));
            }
            for (String s : new String[] { "hp48", "ram", "rom", "port1", "port2" }) {
                copyFile(new File(X48.config_dir + "checkpoint/" + s), new File(X48.config_dir + s));
            }
            File p1 = new File(X48.config_dir + "port1");
            if (p1.exists()) {
                spe.putString("port1", "" + p1.length() / 1024);
            }
            File p2 = new File(X48.config_dir + "port2");
            if (p2.exists()) {
                spe.putString("port2", "" + p2.length() / 1024);
            }
            spe.commit();

        } catch (IOException e) {
            Log.d("x48", "Error: " + e.getMessage());
        }

        need_to_quit = true;
        saveonExit = false;
        finish();
    }

    protected void fullReset() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor spe = mPrefs.edit();

        Log.d("x48", "===================== Full reset done...");
        Toast.makeText(getApplicationContext(), "Full reset done...", Toast.LENGTH_SHORT).show();

        try {
            spe.putString("port1", "0");
            spe.putString("port2", "0");
            spe.commit();
            for (String s : new String[] { "hp48", "ram", "rom", "port1", "port2" }) {
                deleteFile(new File(X48.config_dir + s));
            }
        } catch (IOException e) {
            Log.d("x48", "Error: " + e.getMessage());
        }

        need_to_quit = true;
        saveonExit = false;
        finish();
    }

    protected void saveCheckpoint() {

        Log.d("x48", "===================== Checkpoint saved...");
        Toast.makeText(getApplicationContext(), "Checkpoint saved...", Toast.LENGTH_SHORT).show();

        saveState();

        File hpDir = new File(X48.config_dir, "checkpoint");
        if (!hpDir.exists()) {
            hpDir.mkdir();
        }
        try {
            for (String s : new String[] { "port1", "port2" }) {
                deleteFile(new File(X48.config_dir + "checkpoint/" + s));
            }
            for (String s : new String[] { "hp48", "ram", "rom", "port1", "port2" }) {
                copyFile(new File(X48.config_dir + s), new File(X48.config_dir + "checkpoint/" + s));
            }
        } catch (IOException e) {
            Log.d("x48", "Error: " + e.getMessage());
        }

    }

    /**
     * Called when a menu item is selected.
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor spe = mPrefs.edit();

        switch (item.getItemId()) {

            case RESTORE_CHECKPOINT_ID:
                restoreCheckpoint();
                return true;

            case FULL_RESET_ID:
                fullReset();
                return true;

            case SAVE_CHECKPOINT_ID:
                saveCheckpoint();
                return true;

            case RESET_ID:
                Log.d("x48", "===================== Reset done...");
                Toast.makeText(getApplicationContext(), "Reset done...", Toast.LENGTH_SHORT).show();
                AssetUtil.copyAsset(getResources().getAssets(), true);
                need_to_quit = true;
                saveonExit = false;
                finish();
                return true;

            case SAVE_ID:
                Log.d("x48", "===================== State saved...");
                Toast.makeText(getApplicationContext(), "State saved...", Toast.LENGTH_SHORT).show();
                saveState();
                return true;

            case LOAD_ID:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(
                                new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                123);
                    } else {
                        Intent loadFileIntent = new Intent();
                        loadFileIntent.setClass(this, ProgListView.class);
                        startActivityForResult(loadFileIntent, LOAD_ID);
                    }
                } else {
                    Intent loadFileIntent = new Intent();
                    loadFileIntent.setClass(this, ProgListView.class);
                    startActivityForResult(loadFileIntent, LOAD_ID);
                }

                break;

            case LITEKBD_ID:
                changeKeybLite();
                break;

            case SETTINGS_ID:
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(this, Settings.class);
                startActivityForResult(settingsIntent, SETTINGS_ID);
                break;

            case QUIT_ID:
                // stopHPEmulator();
                // mainView.stop();
                finish();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private static final int DIALOG_PROG_OK = 1;
    private static final int DIALOG_PROG_KO = 2;
    private static final int DIALOG_ROM_KO = 3;
    private static final int DIALOG_RAM_KO = 4;
    private static final int DIALOG_RAM_OK = 5;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROG_OK:
                return new AlertDialog.Builder(X48.this)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setTitle(R.string.help)
                        .setMessage(R.string.prog_ok)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .create();
            case DIALOG_PROG_KO:
                return new AlertDialog.Builder(X48.this)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setTitle(R.string.help)
                        .setMessage(R.string.prog_ko)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                /* User clicked OK so do some stuff */
                            }
                        })
                        .create();
            case DIALOG_ROM_KO:
                return new AlertDialog.Builder(X48.this)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setTitle(R.string.help)
                        .setMessage(R.string.rom_ko)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                onDestroy();
                            }
                        })
                        .create();
            case DIALOG_RAM_KO:
                return new AlertDialog.Builder(X48.this)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setTitle(R.string.help)
                        .setMessage(R.string.ram_install_error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .create();
            case DIALOG_RAM_OK:
                return new AlertDialog.Builder(X48.this)
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setTitle(R.string.help)
                        .setMessage(R.string.ram_install_warning)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .create();
        }
        return null;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent extras) {
        Log.d("x48", "requestCode: " + requestCode + " / " + resultCode);
        super.onActivityResult(requestCode, resultCode, extras);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ROM_ID: {
                    /*
                     * if (true || isRomReady()) {
                     * Log.d("x48", "Rom Ready... starting emulator");
                     * readyToGo();
                     * } else {
                     * Log.d("x48", "Rom not Ready... quitting");
                     * onDestroy();
                     * finish();
                     * }
                     */
                    break;
                }
                case LOAD_ID: {
                    final String filename = extras.getStringExtra("currentFile");
                    if (filename != null) {
                        int retCode = loadProg(filename);
                        if (retCode == 1) {
                            flipScreen();
                            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                            boolean msgbox = mPrefs.getBoolean("no_loadprog_msgbox", false);
                            if (!msgbox)
                                showDialog(DIALOG_PROG_OK);
                        } else {
                            showDialog(DIALOG_PROG_KO);
                        }
                    }
                    break;
                }
                case SETTINGS_ID: {
                    if (mainView != null)
                        mainView.updateContrast();

                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                    String port1 = mPrefs.getString("port1", "0");
                    managePort(1, port1);
                    String port2 = mPrefs.getString("port2", "0");
                    managePort(2, port2);
                    checkPrefs();

                }
            }
        }
    }

    private boolean saveonExit;

    private void managePort(int number, String value) {
        int size = Integer.parseInt(value);
        File f = AssetUtil.getSDDir();
        if (f != null) {
            boolean change = false;
            File port = new File(f, "port" + number);
            /*
             * if (port.exists()) {
             * port.renameTo(new File(f, "bkp.port" + number));
             * }
             */
            if (size == 0) {
                if (port.exists()) {
                    port.delete();
                    Log.d("x48", "===================== Deleting port" + number + " file.");
                    change = true;
                }
            } else {
                if (port.exists() && (port.length() == 1024 * size)) {

                } else {
                    Log.d("x48", "===================== Port" + number
                            + " file does not exists or is incomplete. Writing a blank file.");
                    byte data[] = new byte[1024];
                    for (int i = 0; i < data.length; i++)
                        data[i] = 0;
                    try {
                        FileOutputStream fout = new FileOutputStream(port);
                        for (int l = 0; l < size; l++)
                            fout.write(data);
                        fout.close();
                    } catch (IOException e) {
                        Log.d("x48", "Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                    change = true;
                }
            }
            if (change)
                showDialog(DIALOG_RAM_OK);
        } else {
            showDialog(DIALOG_RAM_KO);
        }
    }

    @Override
    protected void onStop() {
        Log.d("x48", "===================== stop");
        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.d("x48", "===================== start");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d("x48", "===================== pause");
        super.onPause();
        if (mainView != null)
            mainView.pause();
    }

    @Override
    protected void onDestroy() {
        Log.d("x48", "===================== onDestroy");
        super.onDestroy();
        if (saveonExit)
            saveState();
        stopHPEmulator();
        if (mainView != null)
            mainView.unpauseEvent();
        if (need_to_quit)
            System.exit(0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
