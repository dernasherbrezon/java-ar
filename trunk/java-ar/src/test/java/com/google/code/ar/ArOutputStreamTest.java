package com.google.code.ar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

public class ArOutputStreamTest extends TestCase {
    
    public void testEmpty() throws Exception {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ArOutputStream aros = new ArOutputStream(baos);
        aros.setEntries(null);
        aros.flush();
        aros.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ArInputStream aris = new ArInputStream(bais);
        ArEntry[] results = new ArEntry[0];
        ArEntry curEntry = null;
        int counter = 0;
        while((curEntry = aris.getNextEntry()) != null) {
            results[counter] = curEntry;
            counter++;
        }
        assertEquals(0, results.length);
    }

    public void testSaveAndLoadShortLongNames() throws Exception {
        ArEntry[] values = createValidEntries();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ArOutputStream aros = new ArOutputStream(baos);
        aros.setEntries(values);
        aros.flush();
        aros.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ArInputStream aris = new ArInputStream(bais);
        ArEntry[] results = new ArEntry[values.length];
        ArEntry curEntry = null;
        int counter = 0;
        while((curEntry = aris.getNextEntry()) != null) {
            results[counter] = curEntry;
            counter++;
        }

        assertEquals(values.length, results.length);
        for (int i = 0; i < results.length; i++) {
            assertEquals(values[i], results[i]);
        }
    }

    private static ArEntry[] createValidEntries() {
        ArEntry[] result = new ArEntry[2];
        result[0] = new ArEntry();
        result[0].setFileMode(777);
        result[0].setData("str".getBytes());
        result[0].setFileModificationTimestamp(System.currentTimeMillis());
        result[0].setFilename("thisisvery very very long name");
        result[0].setGroupId(1);
        result[0].setOwnerId(1);
        result[1] = new ArEntry();
        result[1].setFileMode(100);
        result[1].setData("123nasd".getBytes());
        result[1].setFileModificationTimestamp(System.currentTimeMillis());
        result[1].setFilename("somefile.tar.gz");
        result[1].setGroupId(1);
        result[1].setOwnerId(1);
        return result;
    }

    private static void assertEquals(ArEntry expected, ArEntry got) {
        assertEquals(expected.getFileMode(), got.getFileMode());
        assertEquals(expected.getFilename(), got.getFilename());
        assertEquals(expected.getGroupId(), got.getGroupId());
        assertEquals(expected.getOwnerId(), got.getOwnerId());
        assertEquals(expected.getData().length, got.getData().length);
        assertTrue(Arrays.equals(expected.getData(), got.getData()));
    }
    
    public static void main(String[] args) throws Exception {
        ArOutputStream aris = new ArOutputStream(new FileOutputStream("test.a"));
        aris.setEntries(createValidEntries());
        aris.close();
    }
}
