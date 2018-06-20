package org.lightsys.emailhelper.Contact;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

public class InboxContactAdapter extends RecyclerView.Adapter<InboxContactAdapter.EmailContactItemViewHolder>{
    private int numItems;
    private ContactList contactList;
    Context context;

    public InboxContactAdapter(ContactList contactList, boolean showDatabaseContacts){
        if( contactList != null){
            if(showDatabaseContacts){
                this.contactList = contactList;
            }else{
                this.contactList = new ContactList();
                for(int i = 0;i<contactList.size();i++){
                    ContactList.ContactListItem temp = contactList.get(i);
                    if(temp != null){
                        if(!temp.inContacts){
                            this.contactList.add(temp.getContactEmail());
                        }
                    }else{
                        break;
                    }
                }
            }
            numItems = this.contactList.size();
        }else{
            numItems = 0;
        }
    }

    @NonNull
    @Override
    public EmailContactItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layout = R.layout.email_contact_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layout,parent,false);
        return new EmailContactItemViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailContactItemViewHolder holder, int position) {
        ContactList.ContactListItem temp = contactList.get(position);
        holder.bind(temp.getContactEmail(),temp.getNumOfReferences());
    }

    @Override
    public int getItemCount() {
        return numItems;
    }

    class EmailContactItemViewHolder extends RecyclerView.ViewHolder {
        TextView email;
        TextView messages;
        CheckBox isInDatabase;
        DatabaseHelper db;
        Context context;
        Resources r;
        public EmailContactItemViewHolder(View itemView, final Context context) {
            super(itemView);
            this.context = context;
            r = context.getResources();
            db = new DatabaseHelper(context);
            email = itemView.findViewById(R.id.email_contact_list);
            isInDatabase = itemView.findViewById(R.id.checkBox_contact_list);
            messages = itemView.findViewById(R.id.messages_contact_list);

            isInDatabase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final String mail = email.getText().toString();
                    if(isChecked){
                        if(!db.containsContact(mail)){
                            db.insertConversationData(mail, "", CommonMethods.getCurrentTime(), CommonMethods.getCurrentDate());
                            isInDatabase.setChecked(true);
                            Intent editContactDetails = new Intent(context, EditContactActivity.class);
                            editContactDetails.putExtra(r.getString(R.string.intent_email), mail);
                            editContactDetails.putExtra(r.getString(R.string.intent_first_name), "");
                            editContactDetails.putExtra(r.getString(R.string.intent_last_name), "");
                            context.startActivity(editContactDetails);
                        }

                    }
                    else{
                        Runnable cancel = new Runnable() {
                            @Override
                            public void run() {
                                isInDatabase.setChecked(true);
                            }
                        };
                        Runnable delete = new Runnable() {
                            @Override
                            public void run() {
                                db.deleteContactData(mail);
                                db.deleteConversationData(mail);
                                isInDatabase.setChecked(false);
                            }
                        };
                        String message = r.getString(R.string.contact_delete_message_prestring)+" "+mail+" "+r.getString(R.string.contact_delete_message_poststring);
                        new ConfirmDialog(message,r.getString(R.string.delete_word),context,delete,cancel);
                    }
                }
            });
        }
        void bind(String email,int number){
            Boolean inDatabase = db.containsContact(email);
            this.email.setText(email);
            this.isInDatabase.setChecked(inDatabase);
            messages.setText(Integer.toString(number)+" messages");
        }
    }
}
