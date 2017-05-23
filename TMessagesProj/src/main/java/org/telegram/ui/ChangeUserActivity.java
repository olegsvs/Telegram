package org.telegram.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ApplicationLoader2;
import org.telegram.messenger.ChangeUserHelper;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig2;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Adapters.UserItemsAdapter;
import org.telegram.ui.Components.UserItems;

import java.io.File;
import java.util.ArrayList;

public class ChangeUserActivity extends Activity implements AdapterView.OnItemClickListener {

    ListView lvUserList = null;
    UserItemsAdapter adapter = null;
    private ArrayList<Object> itemList;
    private UserItems userItems ;
    public static Bitmap book_user = null;
    static ProgressDialog prepareProgress;
    static Context ctx ;

    @Override
    public void onBackPressed()
    {
        System.gc();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        book_user = drawableToBitmap(R.drawable.book_user);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user);
        try {
            ctx = this;
            itemList = new ArrayList<Object>();
            lvUserList = (ListView) findViewById(R.id.users_listview);
            lvUserList.setOnItemClickListener(this);
            lvUserList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                    // TODO Auto-generated method stub
                    // TODO deleting user on random position
                    if (position > 0)
                        showAlertDeleteUser(position);
                    return true;
                }
            });
            prepareArrayLits();
            prepareProgress.dismiss();
            Thread prepareThread = new Thread(
                    new Runnable() {
                        public void run() {
                            prepareArrayLits();
                            runOnUiThread(new Runnable() {
                                public void run() {
//                                    prepareProgress.dismiss();
                                }
                            });
                        }
                    }
            );
//            prepareThread.start();

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (lvUserList.getCount() <= 9)
                        addUser();
                    else
                        Toast.makeText(ChangeUserActivity.this, "Maximum 10 users!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Throwable th) {
            Log.i("TGM", "onCreate: " + th.toString());
        }
    }


    public static void showPrepareDialog(Context ctx) {
        prepareProgress= new ProgressDialog(ctx);
        prepareProgress.setMessage("Сканирование профилей");
        prepareProgress.setIndeterminate(false);
        prepareProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prepareProgress.setCancelable(false);
        prepareProgress.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        itemList.clear();
         lvUserList = null;
         adapter = null;
         itemList = null;
         userItems = null;
         Bitmap book_user = null;
         System.gc();
    }

    private void deleteUser(int position) {
        SharedPreferences sharedPref = getSharedPreferences("userID", Context.MODE_PRIVATE);
        sharedPref.edit().putInt("usersCount", getUsersCount() -1).commit();
        sharedPref.edit().apply();
        deleteDir(getApplicationInfo().dataDir + "/files_user_" + String.valueOf(position));
//        Toast.makeText(this, getApplicationInfo().dataDir + "/files_user_" + String.valueOf(position), Toast.LENGTH_SHORT).show();
        adapter.remove(position);
        adapter.notifyDataSetChanged();
    }

    private void showAlertDeleteUser(final int position) {
        String title = "You are sure?";
        String message = "Delete user?";
        String button1String = "Yes";
        String button2String = "No";

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                deleteUser(position);
            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.show();
    }

    public void deleteDir(String folder) {
        File dir = new File(folder);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
            dir.delete();
        }
    }

    public void addUser() {
        ChangeUserHelper.setUserTag(getUsersCount());
        SharedPreferences sharedPref = getSharedPreferences("userID", Context.MODE_PRIVATE);
        sharedPref.edit().putInt("userID", ChangeUserHelper.getID()).commit();
        sharedPref.edit().putInt("usersCount", getUsersCount() + 1).commit();
        sharedPref.edit().apply();
        Log.i("userTAG", "addUser: tag changed to " + ChangeUserHelper.getUserTag());
        restart();
    }

    public int getUsersCount() {
        SharedPreferences userPhone = getSharedPreferences("userID", Context.MODE_PRIVATE);
        return userPhone.getInt("usersCount",1);
    }

    public void setUser(int position) {
        ChangeUserHelper.setUserTag(position);
        SharedPreferences sharedPref = getSharedPreferences("userID", Context.MODE_PRIVATE);
        sharedPref.edit().putInt("userID", ChangeUserHelper.getID()).commit();
        sharedPref.edit().apply();
        Log.i("userTAG", "setUser: tag changed to " + ChangeUserHelper.getUserTag());
        restart();
    }

    public void restart() {
        Intent launchIntent = new Intent(getApplicationContext(), org.telegram.ui.LaunchActivity.class);
        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent , 0);
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 500, intent);
        Log.i("userTAG", "restarting... " + ChangeUserHelper.getUserTag());
        System.exit(2);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserItems userItems = (UserItems) adapter.getItem(position);
        Toast.makeText(this, "Name => "+userItems.getName()+" \n Phone => "+userItems.getPhone(), Toast.LENGTH_SHORT).show();
        setUser(position);
    }

    public void prepareArrayLits()
    {   itemList = new ArrayList<Object>();
        int usersCount = getUsersCount();
        for (int i = 0; i < usersCount ; i++) {
            String first_name = "null";
            if (getUserByTag("_user_" + i).last_name == null) first_name = getUserByTag("_user_" + i).first_name;
            else first_name = getUserByTag("_user_" + i).first_name + " " + getUserByTag("_user_" + i).last_name;
            String phone = getUserByTag("_user_" + i).phone;
            Bitmap photo = getBitmap(getUserByTag("_user_" + i));
            if(ChangeUserHelper.getID() == i) first_name += " ---- текущий";

            AddObjectToList(photo, first_name, phone);
        }
        adapter = new UserItemsAdapter(this, itemList);
        lvUserList.setAdapter(adapter);
//        System.gc();
    }

    private TLRPC.User getUserByTag(String tag) {
        ApplicationLoader2.convertConfig2(tag);
        TLRPC.User user = UserConfig2.getCurrentUser(tag);
        return user;
    }

    public Bitmap getBitmap(TLRPC.User user) {
        RectF bitmapRect;
        TLRPC.FileLocation photo = null;
        if (user.photo != null) {
            photo =user.photo.photo_small;
        }
        Bitmap bitmap = null;
        if (photo != null) {
            try {
                File path = FileLoader.getPathToAttach(photo, true);
                bitmap = BitmapFactory.decodeFile(path.toString());
                if (bitmap != null) {
                    int size = AndroidUtilities.dp(58);
                    Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                    result.eraseColor(Color.TRANSPARENT);
                    Canvas canvas = new Canvas(result);
                    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Paint roundPaint =  new Paint(Paint.ANTI_ALIAS_FLAG);
                    bitmapRect = new RectF();
                    float scale = size / (float) bitmap.getWidth();
                    canvas.save();
                    canvas.scale(scale, scale);
                    roundPaint.setShader(shader);
                    bitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    canvas.drawRoundRect(bitmapRect, bitmap.getWidth(), bitmap.getHeight(), roundPaint);
                    canvas.restore();
                    Drawable drawable = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.book_logo);
                    int w = AndroidUtilities.dp(15);
                    drawable.setBounds(drawable.getBounds().left, drawable.getBounds().top, drawable.getBounds().right, drawable.getBounds().bottom);
                    drawable.draw(canvas);
                    try {
                        canvas.setBitmap(null);
                    } catch (Exception e) {
                        //don't promt, this will crash on 2.x
                    }
                    bitmap = result;
                    return bitmap;
                }
            } catch (Throwable e) {
                Log.i("TGM", "getBitmap: " + e.toString());
            }
        }
        return null;
    }

    // Add one item into the Array List
    public void AddObjectToList(Bitmap image, String title, String desc)
    {
        userItems = new UserItems();
        userItems.setPhone(desc);
        userItems.setPhoto(image);
        userItems.setName(title);
        itemList.add(userItems);
    }

    public Bitmap drawableToBitmap (int drawable) {
        Bitmap b = null;
        Drawable d = getResources().getDrawable(drawable);
        Drawable currentState = d.getCurrent();
        if(currentState instanceof BitmapDrawable)
            b = ((BitmapDrawable)currentState).getBitmap();
        return b;
    }
}
