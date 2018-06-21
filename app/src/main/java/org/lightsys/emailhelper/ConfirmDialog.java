package org.lightsys.emailhelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * @author SHADE
 * The goal of this class is to facilitate the creation of a popup.
 * The class takes in the parameters and then runs its given executables.
 */
public class ConfirmDialog  {
    public ConfirmDialog(String message, String confirmationWord, Context activity, final Runnable confirm, final Runnable cancel){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        builder.setPositiveButton(confirmationWord, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirm.run();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel.run();
            }
        });
        builder.create().show();
    }
}
