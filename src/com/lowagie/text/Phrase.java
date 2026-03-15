package com.lowagie.text;

import java.util.ArrayList;
import java.util.Collection;
import com.lowagie.text.pdf.HyphenationEvent;

/**
 * Bridge class to satisfy JasperReports 6.21.0 dependency on unpatched iText.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Phrase extends ArrayList implements TextElementArray {
    protected float leading = Float.NaN;
    protected Font font = new Font();
    protected HyphenationEvent hyphenation = null;

    public Phrase() { this(16f); }
    public Phrase(Phrase p) {
        super();
        this.addAll(p);
        this.leading = p.getLeading();
        this.font = p.getFont();
        this.hyphenation = p.getHyphenation();
    }
    public Phrase(float leading) { this.leading = leading; }
    public Phrase(Chunk c) { super.add(c); }
    public Phrase(float leading, Chunk c) { this.leading = leading; super.add(c); }
    public Phrase(String s) { this(Float.NaN, s, new Font()); }
    public Phrase(String s, Font f) { this(Float.NaN, s, f); }
    public Phrase(float leading, String s) { this(leading, s, new Font()); }
    public Phrase(float leading, String s, Font f) {
        this.leading = leading;
        this.font = f;
        if (s != null) super.add(new Chunk(s, f));
    }

    public boolean process(ElementListener listener) {
        try {
            for (Object o : this) listener.add((Element)o);
            return true;
        } catch (DocumentException e) { return false; }
    }
    public int type() { return Element.PHRASE; }
    public java.util.ArrayList getChunks() {
        ArrayList list = new ArrayList();
        for (Object o : this) list.addAll(((Element)o).getChunks());
        return list;
    }
    public boolean isContent() { return true; }
    public boolean isNestable() { return true; }
    
    // This override is crucial for JasperReports 6.x
    @Override
    public boolean add(Element e) {
        return super.add(e);
    }
    
    // We inherit add(Object) from ArrayList and implement TextElementArray.add(Object) through it.

    public void add(int index, Element element) { super.add(index, element); }
    public boolean addAll(Collection c) { return super.addAll(c); }
    protected boolean addChunk(Chunk c) { return super.add(c); }
    protected void addSpecial(Object element) { super.add(element); }

    public void setLeading(float leading) { this.leading = leading; }
    public void setFont(Font font) { this.font = font; }
    public float getLeading() { return leading; }
    public boolean hasLeading() { return !Float.isNaN(leading); }
    public Font getFont() { return font; }
    public String getContent() {
        StringBuilder sb = new StringBuilder();
        for (Object chunk : getChunks()) sb.append(chunk.toString());
        return sb.toString();
    }
    public boolean isEmpty() { return super.isEmpty(); }
    public HyphenationEvent getHyphenation() { return hyphenation; }
    public void setHyphenation(HyphenationEvent h) { this.hyphenation = h; }
    
    public static final Phrase getInstance(String s) { return new Phrase(s); }
    public static final Phrase getInstance(int leading, String s) { return new Phrase((float)leading, s); }
    public static final Phrase getInstance(int leading, String s, Font f) { return new Phrase((float)leading, s, f); }
}
