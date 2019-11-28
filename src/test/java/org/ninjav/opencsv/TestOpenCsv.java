package org.ninjav.opencsv;

import com.opencsv.CSVReader;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;

public class TestOpenCsv {

    @Test
    public void canReadCsv() {
        final String csvFile = "/home/ninjav/brokerbase/20190701/MEBFL1PF.TXT";

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            String[] line;
            while ((line = reader.readNext()) != null) {
                System.out.println("Broker house code [" + line[0] + "]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
