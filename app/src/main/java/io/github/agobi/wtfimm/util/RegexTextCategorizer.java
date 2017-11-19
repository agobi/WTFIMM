package io.github.agobi.wtfimm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.agobi.wtfimm.model.Transaction;

/**
 * Created by gobi on 10/26/16.
 */

public class RegexTextCategorizer extends TextCategorizer {
    private static final String MAESTRO = "account/maestro";
    private static final String CASH = "account/cash";
    private static final String MASTERCARD = "account/mastercard";

    private static class NoMatchException extends Exception {
    }

    private static abstract class TRMatcher {
        private final Pattern pattern;

        TRMatcher(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        protected abstract Transaction process(Matcher m, SMSData sms) throws ParseException;

        public Transaction check(SMSData sms) throws ParseException, NoMatchException {
            Matcher matcher = pattern.matcher(sms.msg);
            if(!matcher.matches())
                throw new NoMatchException();
            return process(matcher, sms);
        }

    }


    private static final List<TRMatcher> matchers = new ArrayList<>();
    private static final Map<String, String> sources = new HashMap<>();
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private static class TransactionBuilder {
        private final Transaction tr = new Transaction();


        public Transaction build() {
            return tr;
        }

        public TransactionBuilder setAmount(String amount) {
            tr.setAmount(Integer.parseInt(amount.replaceAll(" ", "")));
            return this;
        }

        public TransactionBuilder setNote(String... group) {
            StringBuffer sb = new StringBuffer();
            int i=0;
            for(; i<group.length; ++i)
                if(group[i]!=null) {
                    sb.append(group[i]);
                    break;
                }

            ++i;
            for(; i<group.length; ++i)
                if(group[i]!=null)
                    sb.append("\n").append(group[i]);

            tr.setNote(sb.toString());
            return this;
        }

        public TransactionBuilder setSource(String group) {
            tr.setGuessedSource(sources.get(group));
            return this;
        }

        public TransactionBuilder setTimestamp(long ts) throws ParseException {
            tr.setTimestamp(ts);
            return this;
        }

        public TransactionBuilder setTimestamp(String group) throws ParseException {
            tr.setTimestamp(fmt.parse(group).getTime()/1000);
            return this;
        }

        public TransactionBuilder setRealTarget(String target) {
            tr.setGuessedTarget(target);
            return this;
        }

        public TransactionBuilder setTarget(String group) {
            tr.setGuessedTarget(sources.get(group));
            return this;
        }
    }

    static {
        sources.put("Cirrus Maestro", MAESTRO);
        sources.put("Befektetési", MASTERCARD);
        sources.put("Bef kàrtya szla (696718)", MASTERCARD);
        sources.put("HUF fizetési szàmla (696718)", MAESTRO);

        matchers.add(new TRMatcher("(Sikertelen|Törölt|Budapest Internetbank|Az Ön belépési kòdja: |Az Ön jelszava |Budapest Mobil Internetbank |Mobilszàma/e-mail cìme).*") {
            @Override
            protected Transaction process(Matcher m, SMSData sms) throws ParseException {
                return null;
            }
        });

        // Cirrus Maestro POS tranzakciò&nbsp; 1 300 Ft Idöpont: 2016.09.12 18:19:08 E: 425 387 Ft Hely: MIR Etterem Budapest HU
        matchers.add(
                new TRMatcher("(.*) POS tranzakciò  (.*) Ft Idöpont: (.*) E: .* Hely: (.*)") {
                    @Override
                    protected Transaction process(Matcher m, SMSData sms) throws ParseException {
                        return new TransactionBuilder()
                                .setSource(m.group(1))
                                .setAmount(m.group(2))
                                .setTimestamp(m.group(3))
                                .setNote(m.group(4))
                                .build();
                    }
                });

        // Cirrus Maestro utòlagos jòvàiràs 300 HUF  Idöpont: 2016.07.07. Hely: MV-START BUDAPEST BP0
        matchers.add(
                new TRMatcher("(.*) utòlagos jòvàiràs (.*) HUF  Idöpont: (.*) Hely: (.*)") {
                    @Override
                    protected Transaction process(Matcher m, SMSData sms) throws ParseException {
                        return new TransactionBuilder()
                                .setTarget(m.group(1))
                                .setAmount(m.group(2))
                                .setTimestamp(sms.ts)
                                .setNote(m.group(4))
                                .build();
                    }
                });



        // Cirrus Maestro ATM tranzakciò&nbsp; 239 000 Ft Idöpont: 2016.09.07 10:53:40 E: 489 227 Ft Hely: NYUGATI TER 4-5. BUDAPEST HU
        matchers.add(
                new TRMatcher("(.*) ATM tranzakciò  (.*) Ft Idöpont: (.*) E: .* Hely: (.*)") {
                    @Override
                    protected Transaction process(Matcher m, SMSData sms) throws ParseException {
                        return new TransactionBuilder()
                                .setSource(m.group(1))
                                .setAmount(m.group(2))
                                .setTimestamp(m.group(3))
                                .setNote(m.group(4))
                                .setRealTarget(CASH)
                                .build();
                    }
                });


        // HUF fizetési szàmla (696718) utalàs érkezett 798 000 Ft 2016.09.05 E: 888 156 Ft Küldö: HIYA HUNGARY KFT Közl: fizetés 2016. augusztus
        // Bef kàrtya szla (696718) utalàs érkezett 50 000 Ft 2016.09.09 E: 50 031 Ft Küldö: GOBI SÅNDORNÉ Közl: szàmìtògéphez
        // Bef kàrtya szla (696718) utalàs érkezett 2 000 Ft 2016.08.31 E: 2 167 Ft Küldö: Gòbi Attila
        matchers.add(new TRMatcher("(.*) utalàs érkezett (.*) Ft (.*) E: .*?( Küldö: (.*?))?( Közl: (.*))?") {
            @Override
            protected Transaction process(Matcher m, SMSData sms) throws ParseException {
                return new TransactionBuilder()
                        .setTarget(m.group(1))
                        .setAmount(m.group(2))
                        .setTimestamp(sms.ts)
                        .setNote(m.group(5), m.group(7))
                        .build();
            }
        });

        // HUF fizetési szàmla (696718) készpénz befizetés 750 000 Ft Hely: Budapest Bank ZRT Kecsk 2015.06.18 E: 1 389 339 Ft
        matchers.add(new TRMatcher("(.*) készpénz befizetés (.*) Ft Hely: (.*) ([^ ]*) E: .*") {
            @Override
            protected Transaction process(Matcher m, SMSData sms) throws ParseException {
                return new TransactionBuilder()
                        .setTarget(m.group(1))
                        .setAmount(m.group(2))
                        .setTimestamp(sms.ts)
                        .setNote(m.group(3))
                        .build();
            }
        });

        // HUF fizetési szàmla (696718) utalàsi megbìzàs teljesült 444 Ft 2015.08.11 E: 1 079 946 Ft
        // HUF fizetési szàmla (696718) àllandò utalàsi megbìzàs teljesült 125 000 Ft 2016.09.06 E: 761 056 Ft Kedv.: VEZER BOGLARKA
        // HUF fizetési szàmla (696718) àllandò utalàsi megbìzàs teljesült 7 050 Ft 2016.09.12 E: 430 713 Ft Kedv.: NAV EGÉSZSÉGBIZTOSITÅSI ALAP Közl: VEZÉR BOGLÅRKA 8
        // HUF fizetési szàmla (696718) utalàsi megbìzàs teljesült 110 000 Ft 2015.06.05 E: 639 339 Ft Kedv.: Gòbi Attila
        matchers.add(new TRMatcher("(.*?) (àllandò )?utalàsi megbìzàs teljesült (.*) Ft (.*) E: .*?( Kedv.: (.*?))?( Közl: (.*))?") {
            @Override
            protected Transaction process(Matcher m, SMSData sms) throws ParseException {
                return new TransactionBuilder()
                        .setSource(m.group(1))
                        .setAmount(m.group(3))
                        .setTimestamp(sms.ts)
                        .setNote(m.group(6), m.group(8))
                        .build();
            }
        });

        // Az Ön belépési kòdja: ue76fb56 Ezt a belépéshez 2016.09.16 15:51:17-ig hasznàlhatja. Kapcsolat azonosìtò: 154117 Budapest Bank
//        new Matcher("^Az Ön belépési kòdja/") {
//
//        }


    }


    @Override
    public Transaction parseSMS(SMSData smsData) throws IllegalFormatException, ParseException {
        for(TRMatcher m : matchers) {
            try {
                return m.check(smsData);
            } catch (NoMatchException e) {
            }
        }
        throw new IllegalArgumentException("Not sms "+smsData.msg);
    }

}
