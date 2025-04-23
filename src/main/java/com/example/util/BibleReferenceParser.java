package com.example.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class BibleReferenceParser {
    
    // Pattern to match common Bible reference formats
    private static final Pattern BIBLE_REFERENCE_PATTERN = 
        Pattern.compile("\\b(\\d?\\s*[A-Za-z]+)\\s*(\\d+)\\s*:\\s*(\\d+)\\b");
    
    public String extractReference(String text) {
        Matcher matcher = BIBLE_REFERENCE_PATTERN.matcher(text);
        if (matcher.find()) {
            String book = matcher.group(1);
            String chapter = matcher.group(2);
            String verse = matcher.group(3);
            return String.format("%s %s:%s", book.trim(), chapter, verse);
        }
        return null;
    }
    
    public boolean containsBibleReference(String text) {
        return BIBLE_REFERENCE_PATTERN.matcher(text).find();
    }
}
