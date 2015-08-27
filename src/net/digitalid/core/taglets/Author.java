package net.digitalid.core.taglets;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;

/**
 * This class defines a custom block tag for class authors.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class Author extends Taglet {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Registration –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Registers this taglet at the given map.
     * 
     * @param map the map at which this taglet is registered.
     */
    public static void register(@Nonnull Map<String, Taglet> map) {
        Taglet.register(map, new Author());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Overrides –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean inOverview() {
        return true;
    }
    
    @Pure
    @Override
    public boolean inPackage() {
        return true;
    }
    
    @Pure
    @Override
    public boolean inType() {
        return true;
    }
    
    @Pure
    @Override
    public @Nonnull String getName() {
        return "author";
    }
    
    @Pure
    @Override
    public @Nonnull String getTitle() {
        return "Author";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Text –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the pattern that a text needs to match in order to be displayed as an email address.
     */
    private static final @Nonnull Pattern pattern = Pattern.compile("(.+) \\((.+)\\)");
    
    @Pure
    @Override
    protected @Nonnull String getText(@Nonnull String text) {
        final @Nonnull Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) return "<a href=\"mailto:" + matcher.group(2) + "\">" + matcher.group(1) + "</a>";
        else return text;
    }
    
}
