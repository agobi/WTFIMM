package io.github.agobi.wtfimm.model;

import android.text.Editable;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private long timestamp;
    private int amount;
    private String source, target, note, emailid;

    public Transaction() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public Date getDate() {
        return new Date(timestamp * 1000);
    }

    public void setDate(Date date) {
        this.timestamp = date.getTime() / 1000;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    @Override
    public String toString() {
        return "Transaction[" + timestamp + ", " + amount + " " + source + " -> " + target + " (" + note + ")]";
    }
}
