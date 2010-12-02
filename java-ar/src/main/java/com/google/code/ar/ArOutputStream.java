package com.google.code.ar;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class ArOutputStream extends FilterOutputStream {

    private static final Charset ASCII = Charset.forName("ASCII");
    private final static byte[] magic = new byte[]{96, 10};
    private boolean isEntriesPresent = false;
    private boolean isClosed = false;

    public ArOutputStream(OutputStream out) {
        super(out);
        if (out == null) {
            throw new IllegalArgumentException("outputstream cannot be null");
        }
    }

    public void setEntries(ArEntry[] entries) throws IOException {
        if (isClosed) {
            throw new IOException("stream closed");
        }
        if (isEntriesPresent) {
            throw new IOException("archive entries already specified");
        }
        writeHeader();
        if (entries == null || entries.length == 0) {
            isEntriesPresent = true;
            return;
        }

        String[] newNamesInsteadOfLong = null;
        if (hasLongNames(entries)) {
            //write "//" -> empty timestamp -> empty owner id -> empty group id -> empty file mode  
            write(new byte[]{47, 47, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32});
            newNamesInsteadOfLong = new String[entries.length];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int namesDataOffset = 0;
            for (int i = 0; i < entries.length; i++) {
                ArEntry curEntry = entries[i];
                if( curEntry == null ) { 
                    continue;
                }
                newNamesInsteadOfLong[i] = "/" + namesDataOffset;
                byte[] curName = (curEntry.getFilename() + "/\n").getBytes(ASCII);
                baos.write(curName);
                namesDataOffset += curName.length;
            }
            //filesize
            write(String.valueOf(baos.size()), 10);
            write(magic);
            //actual data
            write(baos.toByteArray());
            if (baos.size() % 2 != 0) { //align to 2
                write('\n');
            }
        }

        String time = getCurTime();

        for (int i = 0; i < entries.length; i++) {
            ArEntry curEntry = entries[i];
            if( curEntry == null ) {
                continue;
            }
            if (newNamesInsteadOfLong != null) {
                write(newNamesInsteadOfLong[i], 16);
            } else {
                write(curEntry.getFilename(), 16);
            }
            write(time, 12);
            write(String.valueOf(curEntry.getOwnerId()), 6);
            write(String.valueOf(curEntry.getGroupId()), 6);
            write(String.valueOf(curEntry.getFileMode()), 8);
            write(String.valueOf(curEntry.getData().length), 10);
            write(magic);
            write(curEntry.getData());
            if( curEntry.getData().length % 2 != 0 ) {
                write('\n');
            }
        }
    }

    private static String getCurTime() {
        String curMem = String.valueOf(System.currentTimeMillis());
        return curMem.substring(0, curMem.length() - 3);
    }

    private void write(String str, int maxLenght) throws IOException {
        byte[] data = str.getBytes(ASCII);
        if (data.length > maxLenght) {
            throw new IOException("invalid string data");
        }
        write(data);
        for (int i = data.length; i < maxLenght; i++) {
            write((byte) 32);
        }
    }

    private void writeHeader() throws IOException {
        write(new byte[]{33, 60, 97, 114, 99, 104, 62, 10});
    }

    private static boolean hasLongNames(ArEntry[] entries) {
        for (int i = 0; i < entries.length; i++) {
            ArEntry curEntry = entries[i];
            if( curEntry == null ) {
                continue;
            }
            byte[] filename = curEntry.getFilename().getBytes(ASCII);
            if (filename.length > 16) {
                return true;
            }
        }
        return false;
    }

    public void close() throws IOException {
        super.close();
        isClosed = true;
    }

}
