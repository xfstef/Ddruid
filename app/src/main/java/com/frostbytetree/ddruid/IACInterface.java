package com.frostbytetree.ddruid;

import java.util.ArrayList;

/**
 * Created by XfStef on 12/25/2015.
 */

// This is the Inner App Communication Interface which serves to relay messages between the main
// activities, the communication service and the SQLite Controller. Only Threads may create and
// server messages between them.

public class IACInterface {
    private static IACInterface ourInstance = new IACInterface();
    static ArrayList<Message> message_buffer;
    int rowstamp;   // To be incremented whenever a new message is created.

    public static IACInterface getInstance() {

        message_buffer = new ArrayList<Message>();
        return ourInstance;
    }

    private IACInterface() {
    }
}

class Message {
    int current_rowstamp;   // Is set when the message is created.
    short caller_id;
    short target_id;
    Operation requested_operation;
    short priority; // 0 - Critical
                    // 1 - Urgent
                    // 2 - Future To Do
}

class Operation {
    short status;   // The Operation Status is used to display the current progress regarding the
                    // message. These can be:
                    // 0 - Message sent but not acknowledged
                    // 1 - Message acknowledged and queued for processing
                    // 2 - Operation finished successfully
                    // 3 - Operation finished unsuccessfully, retrying soon
                    // 4 - Operation generated ERROR and will be deleted
    short type;     // Get or Post data from or to server, SQLite, RawData using a 3 digit number:
                    // 1xx - GET; 2xx - POST;
                    // x0x - SQLite DB; x1x - Server;
                    // xx0 - Config File; xx1 - DB Table; xx2 - ... TODO: finish definitions.
    String REST_command;    // If a REST command is needed, else this string is NULL.
    Model data_model;   // If a Data is needed then a data model will be given.

}