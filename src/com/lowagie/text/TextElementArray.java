package com.lowagie.text;

/**
 * Bridge interface to satisfy JasperReports 6.21.0 dependency on unpatched iText.
 */
public interface TextElementArray extends Element {
    // This is the original method in iText 2.1.7
    public abstract boolean add(Object o);

    // This is the method JasperReports 6.x expects
    public default boolean add(Element e) {
        return add((Object)e);
    }
}
