package net.digitalid.core.taglets;

import com.sun.javadoc.Tag;
import java.beans.Introspector;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class serves as a template for custom block tags.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class Taglet implements com.sun.tools.doclets.Taglet {
    
    static void register(Map<String, Taglet> map, Taglet taglet) {
        System.out.println("Registering: " + taglet.getName());
        String name = taglet.getName();
        Object other = map.get(name);
        if (other != null) map.remove(name);
        map.put(name, taglet);
    }
    
    @Override
    public boolean inField() {
        return false;
    }
    
    @Override
    public boolean inConstructor() {
        return false;
    }
    
    @Override
    public boolean inMethod() {
        return false;
    }
    
    @Override
    public boolean inOverview() {
        return false;
    }
    
    @Override
    public boolean inPackage() {
        return false;
    }
    
    @Override
    public boolean inType() {
        return false;
    }
    
    @Override
    public boolean isInlineTag() {
        return false;
    }
    
    protected abstract String getTitle();
    
    private String getTitleWithHTML() {
        return "<dt><span class=\"strong\">" + getTitle() + ":</span></dt>\n";
    }
    
    private static final Pattern pattern = Pattern.compile("(.+) : \"(.+)\";");
    
    protected String getText(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            return "<code> " + matcher.group(1) + "</code> - " + Introspector.decapitalize(matcher.group(2));
        } else {
            return text;
        }
    }
    
    private String getTextWithHTML(String text) {
        return "<dd>" + getText(text) + "</dd>\n";
    }
    
    @Override
    public String toString(Tag tag) {
        return getTitleWithHTML()+ getTextWithHTML(tag.text());
    }
    
    @Override
    public String toString(Tag[] tags) {
        if (tags.length == 0) return "";
        StringBuilder string = new StringBuilder(getTitleWithHTML());
        for (Tag tag : tags) string.append(getTextWithHTML(tag.text()));
        return string.toString();
    }
    
}
