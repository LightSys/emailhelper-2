package org.lightsys.emailhelper.Contact;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.lightsys.emailhelper.R;

import java.util.List;

/**
 * Created by nicholasweg on 6/30/17.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    private List<Contact> contactList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, email;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            email = (TextView) view.findViewById(R.id.email);
        }
    }

    public ContactAdapter(List<Contact> contactList) {this.contactList = contactList;}

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.name.setText(contact.getFirstName() + " " + contact.getLastName());
        holder.email.setText(contact.getEmail());
    }

    @Override
    public int getItemCount() {return contactList.size();}
}
