package com.example.ben.emailhelper.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of Messages from JavaMail API
     */
    public static final List<Message> ITEMS = new ArrayList<Message>();

    /**
     * A map of Messages from JavaMail API, by ID.      We might not need a map. Still figuring out how the list works
     */
    public static final Map<String, Message> ITEM_MAP = new HashMap<String, Message>();

    //changed it to not be final, since the size will be changing
    private static int COUNT = 25;

    /*
        Pretty sure we don't need this function, but we'll see

        static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }*/

    private static void addItem(Message item) {
        ITEMS.add(item);
        //ITEM_MAP.put(item.id, item);      Maybe we don't need a map. Don't know yet
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
