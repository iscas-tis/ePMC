package epmc.reporting;

abstract class TableWriter {
    abstract void setWriteDocument(boolean writeDocument);
    abstract void write();
}
