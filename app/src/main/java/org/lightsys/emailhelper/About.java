package org.lightsys.emailhelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.lightsys.emailhelper.Conversation.ConversationAttachmentAdapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * The About Class created by DSHADE Summer 2018
 * I think think this is one of my finer creations/additions. This class uses a recycler view to
 * create the about page. It allows for everything used in the EventApp but with much more
 * flexibility. To change the about page simple change the .txt file and use the approppriate HTML
 * style encoding.
 */
public class About extends AppCompatActivity {
    RecyclerView recyclerView;
    AboutItemAdapter adapter;
    List<String> aboutItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        recyclerView = findViewById(R.id.aboutRecyclerView);
        aboutItems = new ArrayList<>();
        getMessage();
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        adapter = new AboutItemAdapter(aboutItems);
        recyclerView.setAdapter(adapter);
    }

    private void getMessage(){
        InputStream temp = getApplicationContext().getResources().openRawResource(R.raw.about);
        try{
            Scanner readFile = new Scanner(temp);
            while(readFile.hasNextLine()){
                aboutItems.add(readFile.nextLine());
            }
        }
        catch(Exception e){
        }
    }

    private class AboutItemAdapter extends RecyclerView.Adapter<AboutItemAdapter.AboutViewHolder> {
        int numOfItems;
        List<String> items;

        public AboutItemAdapter(List<String> aboutItems) {
            items = new ArrayList<>(aboutItems);
            numOfItems = items.size();
        }
        @NonNull
        @Override
        public AboutItemAdapter.AboutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            int layout = R.layout.item_about;
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(layout,parent,false);
            return new AboutItemAdapter.AboutViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AboutItemAdapter.AboutViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return numOfItems;
        }

        class AboutViewHolder extends RecyclerView.ViewHolder {
            TextView aboutMessage;
            public AboutViewHolder(View itemView) {
                super(itemView);
                aboutMessage = itemView.findViewById(R.id.aboutText);
            }
            public void bind(String message){
                if(message.length() == 0){
                    message = "  ";//Prevents erros when dealing with substring in if statement
                }
                if(message.substring(0,2).contains("//")){
                    CommonMethods.textViewMinimize(aboutMessage);
                }
                else if(message.contains("<Header>")){
                    CommonMethods.textViewExpand(aboutMessage);
                    aboutMessage.setText(message.substring(8));//removes <Header>
                    aboutMessage.setTextSize(20);
                    aboutMessage.setTypeface(Typeface.DEFAULT_BOLD);
                    aboutMessage.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                else{
                    CommonMethods.textViewExpand(aboutMessage);
                    aboutMessage.setText(message);
                    aboutMessage.setTextSize(16);
                    aboutMessage.setTypeface(Typeface.DEFAULT);
                    aboutMessage.setTextColor(getResources().getColor(R.color.black));
                }
            }
        }
    }

}
