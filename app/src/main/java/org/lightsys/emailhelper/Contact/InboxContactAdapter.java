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
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

public class InboxContactAdapter extends RecyclerView.Adapter<InboxContactAdapter.EmailContactItemViewHolder>{
    private int numItems;
    private ContactList contactList;
    Context context;

    public InboxContactAdapter(ContactList contactList, boolean showDatabaseContacts){
        if( contactList != null){
            if(showDatabaseContacts){
                this.contactList = new ContactList();
                for(int i = 0;i<contactList.size();i++){
                    ContactList.ContactListItem temp = contactList.get(i);
                    if(!temp.inContacts){
                        this.contactList.add(temp.getContactEmail());
                    }
                }
                for(int i = 0;i<contactList.size();i++){
                    ContactList.ContactListItem temp = contactList.get(i);
                    if(temp.inContacts){
                        this.contactList.add(temp.getContactEmail(),true);
                    }
                }
            }else{
                this.contactList = new ContactList();
                for(int i = 0;i<contactList.size();i++){
                    ContactList.ContactListItem temp = contactList.get(i);
                    if(!temp.inContacts){
                        this.contactList.add(temp.getContactEmail());
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
        holder.bind(contactList.get(position).getContactEmail());
    }

    @Override
    public int getItemCount() {
        return numItems;
    }

    class EmailContactItemViewHolder extends RecyclerView.ViewHolder {
        TextView email;
        CheckBox isInDatabase;
        Button addToDatabase;
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
            addToDatabase = itemView.findViewById(R.id.button_contact_list);
            if(!isInDatabase.isChecked()){
                addToDatabase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        db.insertConversationData(email.getText().toString(),"",CommonMethods.getCurrentTime(),CommonMethods.getCurrentDate());
                        isInDatabase.setChecked(true);
                        addToDatabase.setClickable(false);
                        Intent editContactDetails = new Intent(context,EditContactActivity.class);
                        editContactDetails.putExtra(r.getString(R.string.intent_email),email.getText().toString());
                        editContactDetails.putExtra(r.getString(R.string.intent_first_name),"");
                        editContactDetails.putExtra(r.getString(R.string.intent_last_name),"");
                        context.startActivity(editContactDetails);
                    }
                });
            }else{
                addToDatabase.setClickable(false);
            }
        }
        void bind(String email){
            Boolean inDatabase = db.containsContact(email);
            this.email.setText(email);
            this.isInDatabase.setChecked(inDatabase);
            if(inDatabase){
                addToDatabase.setClickable(false);
            }
        }
    }
}
