package org.lightsys.emailhelper.qr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;
import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

import java.util.List;

import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;

/**
 * Created by otter57 on 5/9/17.
 * https://android-arsenal.com/details/1/4516
 * Pulled from eventApp for use in Email Helper
 */

public class launchQRScanner extends AppCompatActivity implements BarcodeRetriever{

    private static final String QR_DATA_EXTRA = "qr_data";
    private Dialog dialog;
    private DatabaseHelper db;
    Contact newContact;
    Context activityContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscan_layout);

        initiatePopupWindow();

        BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);

        barcodeCapture.setRetrieval(launchQRScanner.this);

    }

    // for one time scan
    @Override
    public void onRetrieved(final Barcode barcode) {
        Log.d("launchQRScanner", "Barcode read: " + barcode.displayValue);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newContact = new Contact();
                newContact.setEmail(barcode.contactInfo.emails[0].address);
                String name = (String)(barcode.contactInfo.name.formattedName);
                newContact.setFirstName(name.substring(0,name.indexOf(" ")));
                newContact.setLastName(name.substring(name.indexOf(" ")+1));
                db = new DatabaseHelper(getApplicationContext());
                dialog.dismiss();
                String confirmMessage = "Would you like to add "+newContact.getFirstName()+" "+newContact.getLastName()+" to contacts?";
                String confirmWord= "Confirm";
                new ConfirmDialog(confirmMessage,confirmWord,activityContext,addContactRunnable,cancelRunnable);
            }
            Runnable addContactRunnable = new Runnable() {
                @Override
                public void run() {
                    db.insertContactData(newContact);
                    db.insertConversationData(newContact, CommonMethods.getCurrentTime(),CommonMethods.getCurrentDate());
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            };
            Runnable cancelRunnable = new Runnable() {
                @Override
                public void run() {
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            };
        });
    }


    // for multiple callback
    @Override
    public void onRetrievedMultiple(final Barcode closetToClick, final List<BarcodeGraphic> barcodeGraphics) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = "Code selected : " + closetToClick.displayValue + "\n\nother " +
                        "codes in frame include : \n";
                for (int index = 0; index < barcodeGraphics.size(); index++) {
                    Barcode barcode = barcodeGraphics.get(index).getBarcode();
                    message += (index + 1) + ". " + barcode.displayValue + "\n";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(launchQRScanner.this)
                        .setTitle("code retrieved")
                        .setMessage(message);
                builder.show();
            }
        });

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
        // when image is scanned and processed
    }

    @Override
    public void onRetrievedFailed(String reason) {
        // in case of failure
    }

    private void initiatePopupWindow() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.TOP;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

            wlp.y = Math.round(400*getResources().getDisplayMetrics().density/2);
            window.setAttributes(wlp);
        }
        dialog.show();
    }

}
