package com.example.ben.emailhelper;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class AutoUpdate extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AutoUpdate(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        GetMail.execute(this,action);
    }
}
