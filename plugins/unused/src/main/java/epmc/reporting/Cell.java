package epmc.reporting;

public final class Cell {
    private boolean fixed;
    private boolean header;
    private boolean marked;
    private final int spanRows;
    private final int spanCols;
    private final String[] text = new String[OutputType.values().length];

    public Cell(int spanRows, int spanCols, String... text) {
        assert spanRows >= 1;
        assert spanCols >= 1;
        assert text == null || text.length <= OutputType.values().length
                : text.length;
        this.spanRows = spanRows;
        this.spanCols = spanCols;
        if (text != null) {
            for (int index = 0; index < text.length; index++) {
                this.text[index] = text[index];
            }
        }
    }
    
    public Cell() {
        this(1, 1);
    }

    public Cell(String... text) {
        this(1, 1, text);
    }

    public Cell(String text) {
        this(1, 1, repeatText(text));
    }

    public Cell(int spanRows, int spanCols, String text) {
        this(spanRows, spanCols, repeatText(text));
    }
    
    private static String[] repeatText(String text) {
        assert text != null;
        String[] result = new String[OutputType.values().length];
        for (int index = 0; index < result.length; index++) {
            result[index] = text;
        }
        return result;
    }

    public Cell setText(OutputType outputType, String text) {
        assert !fixed;
        assert outputType != null;
        assert text != null;
        assert this.text[outputType.ordinal()] == null;
        this.text[outputType.ordinal()] = text;
        return this;
    }
    
    public Cell setHeader(boolean header) {
        assert !fixed;
        this.header = header;
        return this;
    }
    
    public Cell setHeader() {
        assert !fixed;
        setHeader(true);
        return this;
    }
    
    public Cell setMarked(boolean marked) {
        assert !fixed;
        this.marked = marked;
        return this;
    }
    
    public boolean isMarked() {
        return this.marked;
    }
    
    public Cell setMarked() {
        assert !fixed;
        setMarked(true);
        return this;
    }
    
    int getSpanCols() {
        return spanCols;
    }
    
    int getSpanRows() {
        return spanRows;
    }
    
    void fix() {
        assert !fixed;
        fixed = true;
        boolean oneValid = false;
        for (String t : text) {
            oneValid |= t != null;
        }
        assert oneValid;
    }
    
    String getText(OutputType outputType) {
        assert outputType != null;
        return this.text[outputType.ordinal()];
    }
    
    boolean isTextSet(OutputType outputType) {
        assert outputType != null;
        return this.text[outputType.ordinal()] != null;
    }
    
    boolean isHeader() {
        return header;
    }
}
