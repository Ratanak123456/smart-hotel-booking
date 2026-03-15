package com.lowagie.text;

/**
 * Bridge class to satisfy JasperReports 6.21.0 dependency on unpatched iText.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Paragraph extends Phrase {
    protected int alignment = Element.ALIGN_UNDEFINED;
    protected float multipliedLeading = 0f;
    protected float indentationLeft;
    protected float indentationRight;
    protected float spacingBefore;
    protected float spacingAfter;
    protected boolean keeptogether = false;

    public Paragraph() { super(); }
    public Paragraph(float leading) { super(leading); }
    public Paragraph(Chunk chunk) { super(chunk); }
    public Paragraph(float leading, Chunk chunk) { super(leading, chunk); }
    public Paragraph(String string) { super(string); }
    public Paragraph(String string, Font font) { super(string, font); }
    public Paragraph(float leading, String string) { super(leading, string); }
    public Paragraph(float leading, String string, Font font) { super(leading, string, font); }
    public Paragraph(Phrase phrase) { super(phrase); }

    public int type() { return Element.PARAGRAPH; }
    
    @Override
    public boolean add(Object o) { return super.add(o); }

    @Override
    public boolean add(Element e) { return super.add(e); }

    public void setAlignment(int alignment) { this.alignment = alignment; }
    public void setAlignment(String alignment) {
        if ("Center".equalsIgnoreCase(alignment)) this.alignment = Element.ALIGN_CENTER;
        else if ("Right".equalsIgnoreCase(alignment)) this.alignment = Element.ALIGN_RIGHT;
        else if ("Justified".equalsIgnoreCase(alignment)) this.alignment = Element.ALIGN_JUSTIFIED;
        else if ("JustifiedAll".equalsIgnoreCase(alignment)) this.alignment = Element.ALIGN_JUSTIFIED_ALL;
        else this.alignment = Element.ALIGN_LEFT;
    }
    public void setLeading(float leading) { this.leading = leading; this.multipliedLeading = 0f; }
    public void setMultipliedLeading(float multipliedLeading) { this.multipliedLeading = multipliedLeading; this.leading = 0f; }
    public void setLeading(float fixedLeading, float multipliedLeading) { this.leading = fixedLeading; this.multipliedLeading = multipliedLeading; }
    public void setIndentationLeft(float indentation) { this.indentationLeft = indentation; }
    public void setIndentationRight(float indentation) { this.indentationRight = indentation; }
    public void setFirstLineIndent(float indentation) { /* Not available in 2.1.7 simple */ }
    public void setSpacingBefore(float spacing) { this.spacingBefore = spacing; }
    public void setSpacingAfter(float spacing) { this.spacingAfter = spacing; }
    public void setKeepTogether(boolean keeptogether) { this.keeptogether = keeptogether; }
    public boolean getKeepTogether() { return keeptogether; }
    public int getAlignment() { return alignment; }
    public float getMultipliedLeading() { return multipliedLeading; }
    public float getTotalLeading() { return leading; }
    public float getIndentationLeft() { return indentationLeft; }
    public float getIndentationRight() { return indentationRight; }
    public float getFirstLineIndent() { return 0f; }
    public float getSpacingBefore() { return spacingBefore; }
    public float getSpacingAfter() { return spacingAfter; }
    public float getExtraParagraphSpace() { return 0f; }
    public void setExtraParagraphSpace(float extraParagraphSpace) { }
    public float spacingBefore() { return spacingBefore; }
    public float spacingAfter() { return spacingAfter; }
}
