package org.lightsys.emailhelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * The goal of this class is to facilitate the creation of a popup.
 * The class takes in the parameters and then runs its given executables after the alert dialog.
 * The class does not extend AlertDialog but is a wrapper class.
 * @author DSHADE
 */
public class ConfirmDialog  {

    /**
     * @param message The message to display in the dialog
     * @param confirmationWord The word for the confirm button i.e. delete, confirm
     * @param context The activity context for the popup
     * @param confirm The runnable to be ran when confirm is selected. Can be left null for nothing to happen.
     * @param cancel The runnable to be ran when the user cancels. Can be left null for nothing to happen.
     */
    public ConfirmDialog(String message, String confirmationWord, Context context, final Runnable confirm, final Runnable cancel){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(confirmationWord, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(confirm != null){
                    confirm.run();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(cancel != null){
                    cancel.run();
                }
            }
        });
        builder.create().show();
    }
}
