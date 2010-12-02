package com.google.code.ar;

import java.io.InputStream;

import junit.framework.TestCase;

public class ArInputStreamTest extends TestCase {

    public void testSuccess() throws Exception {
        InputStream file = ArInputStream.class.getClassLoader().getResourceAsStream("nginx_0.7.67-3ubuntu1_i386.deb");
        if (file == null) {
            throw new Exception("cannot find the file specified");
        }
        ArInputStream stream = new ArInputStream(file);
        ArEntry f1 = stream.getNextEntry();
        assertNotNull(f1);
        assertEquals("debian-binary", f1.getFilename());
        assertEquals(1281992580000l, f1.getFileModificationTimestamp());
        assertEquals(0, f1.getGroupId());
        assertEquals(0, f1.getOwnerId());
        assertEquals(100644, f1.getFileMode());
        assertNotNull(f1.getData());
        assertEquals(4, f1.getData().length);
        ArEntry f2 = stream.getNextEntry();
        assertNotNull(f2);
        assertEquals("control.tar.gz", f2.getFilename());
        assertEquals(1281992580000l, f2.getFileModificationTimestamp());
        assertEquals(0, f2.getGroupId());
        assertEquals(0, f2.getOwnerId());
        assertEquals(100644, f2.getFileMode());
        assertNotNull(f2.getData());
        assertEquals(1984, f2.getData().length);
        ArEntry f3 = stream.getNextEntry();
        assertNotNull(f3);
        assertEquals("data.tar.gz", f3.getFilename());
        assertEquals(1281992580000l, f3.getFileModificationTimestamp());
        assertEquals(0, f3.getGroupId());
        assertEquals(0, f3.getOwnerId());
        assertEquals(100644, f3.getFileMode());
        assertNotNull(f3.getData());
        assertEquals(338549, f3.getData().length);
        ArEntry f4 = stream.getNextEntry();
        assertNull(f4);
        stream.close();
    }

    public void testLongFileNames() throws Exception {
        InputStream file = ArInputStream.class.getClassLoader().getResourceAsStream("result.a");
        if (file == null) {
            throw new Exception("cannot find the file specified");
        }
        ArInputStream stream = new ArInputStream(file);
        ArEntry f1 = stream.getNextEntry();
        assertNotNull(f1);
        assertEquals("filewithlonglongname", f1.getFilename());
        assertEquals(1291275347000l, f1.getFileModificationTimestamp());
        assertEquals(2222, f1.getGroupId());
        assertEquals(233481, f1.getOwnerId());
        assertEquals(100640, f1.getFileMode());
        assertNotNull(f1.getData());
        assertEquals(11, f1.getData().length);
        ArEntry f2 = stream.getNextEntry();
        assertNotNull(f2);
        assertEquals("anotherlonglongfilename", f2.getFilename());
        assertEquals(1291275797000l, f2.getFileModificationTimestamp());
        assertEquals(2222, f2.getGroupId());
        assertEquals(233481, f2.getOwnerId());
        assertEquals(100640, f2.getFileMode());
        assertNotNull(f2.getData());
        assertEquals(85, f2.getData().length);
        ArEntry f3 = stream.getNextEntry();
        assertNotNull(f3);
        assertEquals("sname", f3.getFilename());
        assertEquals(1291277804000l, f3.getFileModificationTimestamp());
        assertEquals(2222, f3.getGroupId());
        assertEquals(233481, f3.getOwnerId());
        assertEquals(100640, f3.getFileMode());
        assertNotNull(f3.getData());
        assertEquals(1477, f3.getData().length);
        ArEntry f4 = stream.getNextEntry();
        assertNull(f4);
        stream.close();
    }

}
