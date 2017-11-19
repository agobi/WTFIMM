package io.github.agobi.wtfimm.util;

import android.telephony.SmsMessage;

import java.text.ParseException;
import java.util.IllegalFormatException;

import io.github.agobi.wtfimm.model.Transaction;

public abstract class TextCategorizer {
    public abstract Transaction parseSMS(SMSData smsData) throws IllegalFormatException, ParseException;
    public Transaction parseSMS(SmsMessage smsData) throws IllegalFormatException, ParseException {
        return parseSMS(new SMSData(smsData.getTimestampMillis()/1000, smsData.getIndexOnIcc(), smsData.getMessageBody()));
    }

    private static TextCategorizer createInstance() {
        try {
            return (TextCategorizer) Class.forName("io.github.agobi.wtfimm.util.RegexTextCategorizer").newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private static final TextCategorizer instance = createInstance();

    public static TextCategorizer getInstance() {
        return instance;
    }

    public static class SMSData {
        long ts, id;
        String msg;

        public SMSData(long ts, long id, String msg) {
            this.ts = ts;
            this.id = id;
            this.msg = msg;
        }
    }
}
