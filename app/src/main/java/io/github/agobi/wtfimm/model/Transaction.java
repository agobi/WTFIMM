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

    public String source() {
        if(source != null)
            return source;
        else
            return guessedSource;
    }

    public String target() {
        if(target != null)
            return target;
        else
            return guessedTarget;
    }

    @Override
    public String toString() {
        return "Transaction[" + timestamp + ", " + amount + ", " + source() + " -> " + target() + " (" + note + ")]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (timestamp != that.timestamp) return false;
        if (amount != that.amount) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        if (guessedSource != null ? !guessedSource.equals(that.guessedSource) : that.guessedSource != null)
            return false;
        if (guessedTarget != null ? !guessedTarget.equals(that.guessedTarget) : that.guessedTarget != null)
            return false;
        if (note != null ? !note.equals(that.note) : that.note != null) return false;
        if (emailid != null ? !emailid.equals(that.emailid) : that.emailid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + amount;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (guessedSource != null ? guessedSource.hashCode() : 0);
        result = 31 * result + (guessedTarget != null ? guessedTarget.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        result = 31 * result + (emailid != null ? emailid.hashCode() : 0);
        return result;
    }
}
