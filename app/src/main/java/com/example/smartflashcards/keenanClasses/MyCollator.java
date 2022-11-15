package com.example.smartflashcards.keenanClasses;

import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;

import java.util.Locale;

public class MyCollator {
    //TODO: make a faster comparator that quits when it knows < 0 or > 0
    // - probably have to compare one letter at a time
    // - and understand that some letters have more than one character
    // - perhaps can just double check any non-zero result by adding previous character
    // - or perhaps always check 2 characters (still shifting one at a time)

    private RuleBasedCollator myCollator;

    public MyCollator(Locale locale) {
        Collator newCollator = Collator.getInstance(locale);
        // the following if statement is constructed per documentation to protect against language code changes
        if (locale.getLanguage().equals(new Locale("es").getLanguage())) {
            String spanishRules = ((RuleBasedCollator) newCollator).getRules();
            String traditionalRules = "& C < ch, cH, Ch, CH & L < ll, lL, Ll, LL & R < rr, rR, Rr, RR";
            try {
                myCollator = new RuleBasedCollator(spanishRules + traditionalRules);
            } catch (Exception e) {
                e.printStackTrace();
                myCollator = (RuleBasedCollator) newCollator;
            }
        } else {
            myCollator = (RuleBasedCollator) newCollator;
        }
    }

    public boolean lessThan(String string1, String string2) {
        return myCollator.compare(string1, string2) < 0;
    }

    public boolean greaterThan(String string1, String string2) {
        return myCollator.compare(string1, string2) > 0;
    }
}
