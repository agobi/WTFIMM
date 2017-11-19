package io.github.agobi.wtfimm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import io.github.agobi.wtfimm.model.Transaction;
import io.github.agobi.wtfimm.util.TextCategorizer;

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";

    public SMSReceiver() {
        Log.d(TAG, "ASD");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            SharedPreferences blockerPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            TextCategorizer textCategorizer = TextCategorizer.getInstance();
            if(!blockerPreferences.getBoolean("categorizer_enable", false) || textCategorizer == null)
                return;

            String sender = blockerPreferences.getString("categorizer_sender", "?");
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                if(sender.equals(smsMessage.getOriginatingAddress())) {
                    try {
                        Transaction tr = textCategorizer.parseSMS(smsMessage);
                        FireBaseApplication mApplication = ((FireBaseApplication)context.getApplicationContext());
                        mApplication.getTransactions().push().setValue(tr).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Exception e = task.getException();
                                if(e != null) {
                                    Log.e(TAG, "Cannot process sms", e);
                                } else {
                                    Log.d(TAG, "Saved");
                                }
                            }
                        });
                        Log.d(TAG, "" + tr);
                    } catch (Exception e) {
                        Log.e(TAG, "Cannot process sms", e);
                    }
                }
            }
        }
    }
}
