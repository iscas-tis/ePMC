package epmc.reporting;

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

final class TableWriterHTML extends TableWriter {
    private final static String HTML = "html";
    private final static String HEAD = "head";
    private final static String TITLE = "title";
    private final static String BODY = "body";
    private final static String TABLE = "table";
    private final static String TR = "tr";
    private final static String TH = "th";
    private final static String TD = "td";
    private final static String COLSPAN = "colspan";
    private final static String ROWSPAN = "rowspan";
    private final static String CLASS = "class";
//    private final static String REPORTING = "reporting";
    private final static String STYLE = "style";
    private final static String TYPE = "type";
    private final static String TEXT_CSS = "text/css";
    private final static String FONT_WEIGHT = "font-weight";
    private final static String BOLD = "bold";
    private final static String MARKED = "marked";
    private final static String BORDER = "border";
    private final static String BORDER_COLLAPSE = "border-collapse";
    private final static String COLLAPSE = "collapse";
    private final static String SOLID = "solid";
    private final static String BLACK = "black";
    private final static String ENDLINE = "\n";
    private final Table table;
    private boolean writeDocument;
    private final XMLStreamWriter eventWriter;

    TableWriterHTML(Table table, OutputStream out) {
        assert table != null;
        assert out != null;
        assert table.isFixed();
        assert table.isOutputTypeValid(OutputType.HTML);
        this.table = table;
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try {
            this.eventWriter = outputFactory.createXMLStreamWriter(out);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            assert false;
            throw new AssertionError();
        }
    }
    
    @Override
    void write() {
        try {
            if (writeDocument) {
                writeHeader();
            }
            writeTable();
            if (writeDocument) {
                writeFooter();
            }
            eventWriter.flush();
        } catch (XMLStreamException e) {
            e.printStackTrace();
            assert false;
        }
    }

    private void writeHeader() throws XMLStreamException {
        writeStartDocument();
        writeDTD(
                "\n<!DOCTYPE html PUBLIC\n"
                + "\"-//W3C//DTD XHTML Basic 1.1//EN\"\n"
                + "\"http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd\">\n"
                );
        writeStartElement(HTML);
        writeStartElement(HEAD);
        writeStartElement(STYLE);
        writeAttribute(TYPE, TEXT_CSS);
        writeCharacters(ENDLINE);
        writeCCSBegin(TABLE, TR, TD, TH);
        writeCCSElement(BORDER, "1px", SOLID, BLACK);
        writeCCSElement(BORDER_COLLAPSE, COLLAPSE);
        writeCCSEnd();
        writeCCSBegin(TD + "." + MARKED);
        writeCCSElement(FONT_WEIGHT, BOLD);
        writeCCSEnd();
        writeEndElement();
        writeStartElement(TITLE);
        writeCharacters("Table test");
        writeEndElement();
        writeEndElement();
        writeStartElement(BODY);        
        writeCharacters(ENDLINE);
    }

    private void writeFooter() throws XMLStreamException {
        writeCharacters(ENDLINE);
        writeEndElement();
        writeEndElement();
        eventWriter.writeEndDocument();
    }

    private void writeCharacters(String content) throws XMLStreamException {
        assert content != null;
        eventWriter.writeCharacters(content);
    }
    
    private void writeCCSBegin(String... forWhat) throws XMLStreamException {
        assert forWhat != null;
        assert forWhat.length >= 0;
        for (String what : forWhat) {
            assert what != null;
        }        
        for (int index = 0; index < forWhat.length; index++) {
            writeCharacters(forWhat[index]);
            if (index < forWhat.length - 1) {
                writeCharacters(", ");
            }
        }
        writeCharacters(" {");
        writeCharacters(ENDLINE);
    }
    
    private void writeCCSElement(String name, String... values) throws XMLStreamException {
        assert name != null;
        assert values != null;
        for (String value : values) {
            assert value != null;
        }
        writeCharacters("  ");
        writeCharacters(name);
        writeCharacters(": ");
        for (int index = 0; index < values.length; index++) {
            writeCharacters(values[index]);
            if (index < values.length - 1) {
                writeCharacters(" ");
            }
        }
        writeCharacters(";");
        writeCharacters(ENDLINE);
    }
    
    private void writeCCSEnd() throws XMLStreamException {
        writeCharacters("}");
        writeCharacters(ENDLINE);
    }

    private void writeStartElement(String localName) throws XMLStreamException {
        assert localName != null;
        eventWriter.writeStartElement(localName);
    }
    
    private void writeAttribute(String localName, String value) throws XMLStreamException {
        eventWriter.writeAttribute(localName, value);
    }
    
    private void writeEndElement() throws XMLStreamException {
        eventWriter.writeEndElement();
    }
    
    private void writeStartDocument() throws XMLStreamException {
        eventWriter.writeStartDocument();
    }
    
    private void writeDTD(String dtd) throws XMLStreamException {
        eventWriter.writeDTD(dtd);
    }
    
    private void writeTable() throws XMLStreamException {
        writeStartElement(TABLE);
        for (Row row : table.getRows()) {
            writeStartElement(TR);
            for (Cell cell : row.getCells()) {
                if (cell.isHeader()) {
                    writeStartElement(TH);
                } else {
                    writeStartElement(TD);
                }
                if (cell.getSpanCols() > 1) {
                    writeAttribute(COLSPAN, String.valueOf(cell.getSpanCols()));
                }
                if (cell.getSpanRows() > 1) {
                    writeAttribute(ROWSPAN, String.valueOf(cell.getSpanRows()));
                }
                if (cell.isMarked()) {
                    writeAttribute(CLASS, MARKED);
                }
                writeCharacters(cell.getText(OutputType.HTML));
                writeEndElement();
            }
            writeEndElement();
        }
        writeEndElement();
    }
    
    @Override
    void setWriteDocument(boolean writeDocument) {
        this.writeDocument = writeDocument;
    }
}
