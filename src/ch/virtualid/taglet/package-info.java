/**
 * This package provides classes to support multi-argument custom block tags in the Javadoc generated for this project.
 * If the referenced classes cannot be found, you need to add the <a href="http://www.oracle.com/technetwork/java/javase/documentation/index-137483.html#com.sun.javadoc">{@code tools.jar}</a> library included in the Java 2 SDK.
 * <p>
 * The options for the Javadoc Generator are (please note that you need to adapt the paths)
 * <p>
 * {@code -quiet -linksource -taglet ch.virtualid.taglet.Author -taglet ch.virtualid.taglet.Require -taglet ch.virtualid.taglet.Ensure -taglet ch.virtualid.taglet.Invariant -tagletpath "/VirtualID/build/classes" -link "http://docs.oracle.com/javase/7/docs/api/" -overview "/VirtualID/src/overview.html" -doctitle "Virtual ID Reference Implementation"}
 * <p>
 * or, if the custom taglets are not working,
 * <p>
 * {@code -quiet -linksource -tag require:cm:"Requires:" -tag ensure:cm:"Ensures:" -tag invariant:tf:"Invariant:" -link "http://docs.oracle.com/javase/7/docs/api/" -overview "/VirtualID/src/overview.html" -doctitle "Virtual ID Reference Implementation"}
 * 
 * @version 1.0
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @see <a href="http://docs.oracle.com/javase/6/docs/technotes/tools/solaris/javadoc.html">Javadoc Documentation</a>
 * @see <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/javadoc/taglet/overview.html">Taglet Overview</a>
 */
package ch.virtualid.taglet;
