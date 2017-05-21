/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.messenger_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class NotificationDismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications"+Change_user_helper.getUserTag(), Context.MODE_PRIVATE);
        preferences.edit().putInt("dismissDate", intent.getIntExtra("messageDate", 0)).commit();
    }
}
