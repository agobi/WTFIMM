package io.github.agobi.wtfimm.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private long timestamp;
    private int amount;
    private String source, target, guessedSource, guessedTarget, note, emailid;

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

    public String getGuessedSource() {
        return guessedSource;
    }

    public void setGuessedSource(String guessedSource) {
        this.guessedSource = guessedSource;
    }

    public String getGuessedTarget() {
        return guessedTarget;
    }

    public void setGuessedTarget(String guessedTarget) {
        this.guessedTarget = guessedTarget;
    }

    @Override
    public String toString() {
        return "Transaction[" + timestamp + ", " + amount + " " + source + " -> " + target + " (" + note + ")]";
    }
}
