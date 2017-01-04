package epmc.reporting;

import java.io.FileOutputStream;
import java.io.IOException;

import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;

public class Test {
    public static void main(String[] args) throws EPMCException, IOException {
        Options options = UtilOptionsEPMC.newOptions();
        // TODO
//        options.set(OptionsMessages.LOG, UtilMessages.newLog(UtilMessages.newMessageChannelCommandLine(options)));
//        Properties props = options.toProperties();
//        System.out.println(props);

        Table table = new Table();
        table.add(new Row().add(new Cell(1, 2, "title").setHeader()));
        table.add(new Row().add(3, 1, "asa").add(new Cell("ggg").setMarked()));
        table.add(new Row().add("asdf"));
        table.add(new Row().add(";;;"));
        table.add(new Row().add(1, 2, "zzz"));
        table.fix();
        FileOutputStream out = new FileOutputStream("/Users/emhahn/test.html");
        TableWriter writer = new TableWriterHTML(table, out);
        writer.setWriteDocument(true);
        writer.write();
        out.flush();
    }
}
