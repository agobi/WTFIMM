package io.github.agobi.wtfimm.util;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import static org.junit.Assert.*;

import io.github.agobi.wtfimm.model.Transaction;

public class TextCategorizerUnitTest {
    private final ArrayList<TestData> samples = new ArrayList<>();

    static class TestData {
        RegexTextCategorizer.SMSData smsData;
        Transaction transaction;

        public TestData(RegexTextCategorizer.SMSData smsData, Transaction transaction) {
            this.smsData = smsData;
            this.transaction = transaction;
        }
    }
    public TextCategorizerUnitTest() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("src/test/res/raw/smsexamples.txt"));
        String line = br.readLine();
        while(line != null) {
            if(!"".equals(line)) {
                Transaction tr = parseTransaction(line);
                line = br.readLine();
                RegexTextCategorizer.SMSData sms = parseSMS(line);
                samples.add(new TestData(sms, tr));
            }
            line = br.readLine();
        }
    }

    private RegexTextCategorizer.SMSData parseSMS(String line) {
        String[] split = line.split(" ", 3);
        return new RegexTextCategorizer.SMSData(Long.parseLong(split[0]), Long.parseLong(split[1]), split[2]);
    }

    private String getOrNull(String str) {
        if("null".equals(str))
            return null;
        return str;
    }

    private Transaction parseTransaction(String line) {
        if("null".equals(line))
            return null;

        String[] split = line.split(" ", 5);
        Transaction transaction = new Transaction();
        transaction.setTimestamp(Long.parseLong(split[0]));
        transaction.setAmount(Integer.parseInt(split[1]));
        transaction.setGuessedSource(getOrNull(split[2]));
        transaction.setGuessedTarget(getOrNull(split[3]));
        transaction.setNote(split[4].replace("\\n", "\n"));
        return transaction;
    }

    @Test
    public void canRead() throws IOException, ParseException {
        TextCategorizer textCategorizer = new RegexTextCategorizer();
        for(TestData data : samples) {
            Transaction actual = textCategorizer.parseSMS(data.smsData);
            assertEquals(data.smsData.msg, data.transaction, actual);
        }
    }

    @Test
    public void canReadAll() throws IOException, ParseException {
        TextCategorizer textCategorizer = new RegexTextCategorizer();
        BufferedReader br = new BufferedReader(new FileReader("src/test/res/raw/testdata.txt"));
        String line = br.readLine();
        while(line != null) {
            if(!"".equals(line)) {
                RegexTextCategorizer.SMSData sms = parseSMS(line.substring(20));
                textCategorizer.parseSMS(sms);
            }
            line = br.readLine();
        }
    }

}
