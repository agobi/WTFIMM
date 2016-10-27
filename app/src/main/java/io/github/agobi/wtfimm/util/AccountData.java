package io.github.agobi.wtfimm.util;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by gobi on 10/27/16.
 */
public interface AccountData {
    long getBalance();

    String getName();

    DataSnapshot getData();
}
