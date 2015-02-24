package net.digitalid.core.taglets;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines a custom block tag for class authors.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Author extends Taglet {
    
    public static void register(Map<String, Taglet> map) {
        Taglet.register(map, new Author());
    }
    
    @Override
    public boolean inOverview() {
        return true;
    }
    
    @Override
    public boolean inPackage() {
        return true;
    }
    
    @Override
    public boolean inType() {
        return true;
    }
    
    @Override
    public String getName() {
        return "author";
    }
    
    @Override
    public String getTitle() {
        return "Author";
    }
    
    private static final Pattern pattern = Pattern.compile("(.+) \\((.+)\\)");
    
    @Override
    protected String getText(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            return "<a href=\"mailto:" + matcher.group(2) + "\">" + matcher.group(1) + "</a>";
        } else {
            return text;
        }
    }
    
}
