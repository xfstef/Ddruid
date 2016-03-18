package com.frostbytetree.ddruid;

/**
 * Created by Tomi on 06/02/16.
 */
public interface IDataInflateListener {
    void signalDataArrived(final Table my_table);
    void codeScanned(String code);
    void signalOffline(String could_not_send_now);
    void signalOnline(String operation_finished);
}
