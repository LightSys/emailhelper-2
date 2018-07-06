package org.lightsys.emailhelper.Contact;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

/**
 * Created by nicholasweg on 6/30/17.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    private ContactList contactList;
    private boolean showCheckbox;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, email;
        public CheckBox inDatabase;
        Context context;
        DatabaseHelper db;
        Resources r;
        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            email = view.findViewById(R.id.email);
            context = view.getContext();
            db = new DatabaseHelper(context);
            r = context.getResources();
            inDatabase = view.findViewById(R.id.contact_checkbox);
            inDatabase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final String mail = email.getText().toString();
                    if(isChecked){
                        if(!db.containsContact(mail)) {
                            inDatabase.setChecked(true);
                            Intent addContactDetails = new Intent(context, NewContactActivity.class);
                            addContactDetails.putExtra(r.getString(R.string.intent_email), mail);
                            addContactDetails.putExtra(r.getString(R.string.intent_first_name), "");
                            addContactDetails.putExtra(r.getString(R.string.intent_last_name), "");
                            context.startActivity(addContactDetails);
                        }

                    }
                    else{
                        Runnable cancel = new Runnable() {
                            @Override
                            public void run() {
                                inDatabase.setChecked(true);
                            }
                        };
                        Runnable delete = new Runnable() {
                            @Override
                            public void run() {
                                db.deleteContactData(mail);
                                db.deleteConversationData(mail);
                                inDatabase.setChecked(false);
                                name.setText("");

                            }
                        };
                        String message = r.getString(R.string.contact_delete_message_prestring)+" "+mail+" "+r.getString(R.string.contact_delete_message_poststring);
                        new ConfirmDialog(message,r.getString(R.string.delete_word),context,delete,cancel);
                    }
                }
            });
        }
    }

    ContactAdapter(ContactList contactList,boolean showCheckbox) {
        this.contactList = contactList;
        this.showCheckbox = showCheckbox;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position){
        holder.name.setText(contactList.get(position).getName());
        holder.email.setText(contactList.get(position).getEmail());
        holder.inDatabase.setChecked(contactList.get(position).getInContacts());
        if(showCheckbox){
            holder.inDatabase.setVisibility(View.VISIBLE);
            //holder.name.setText(Integer.toString(contactList.get(position).getNumOfReferences()));
        }
    }

    @Override
    public int getItemCount() {return contactList.size();}
}
