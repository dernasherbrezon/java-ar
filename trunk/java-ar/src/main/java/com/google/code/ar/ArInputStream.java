package com.google.code.ar;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ArInputStream extends FilterInputStream {

    private static final Charset ASCII = Charset.forName("ASCII");
    private byte[] longFileNames;
    private boolean isClosed = false;

    public ArInputStream(InputStream in) throws IOException {
        super(in);
        if (in == null) {
            throw new IllegalArgumentException("input stream cannot be null");
        }
        readHeader();
    }

    private void readHeader() throws IOException {
        byte[] header = new byte[8];
        read(header);
        String headerStr = new String(header, ASCII);
        if (headerStr.trim().length() == 0 || !headerStr.equals("!<arch>\n")) {
            throw new IOException("not an \"AR\" archive");
        }
    }

    public boolean markSupported() {
        return false;
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
    }

    public ArEntry getNextEntry() throws IOException {
        if (available() == 0) {
            return null;
        }

        if (isClosed) {
            throw new IOException("Stream closed");
        }

        byte[] fileName = new byte[16];
        read(fileName);
        byte[] timestamp = new byte[12];
        read(timestamp);
        byte[] ownerId = new byte[6];
        read(ownerId);
        byte[] groupId = new byte[6];
        read(groupId);
        byte[] fileMode = new byte[8];
        read(fileMode);
        byte[] fileSize = new byte[10];
        read(fileSize);
        byte[] magic = new byte[2];
        read(magic);
        if (magic[0] != 0x60 || magic[1] != 0x0A) {
            throw new IOException("corrupted archive data");
        }

        ArEntry result = new ArEntry();
        String filenameStr = new String(fileName, ASCII).trim();
        result.setFilename(filenameStr);
        String timeStampStr = new String(timestamp, ASCII).trim();
        if (timeStampStr.length() != 0) {
            result.setFileModificationTimestamp(Long.valueOf(timeStampStr + "000").longValue());
        } else {
            result.setFileModificationTimestamp(0l);
        }
        String ownerIdStr = new String(ownerId, ASCII).trim();
        if (ownerIdStr.length() != 0) {
            result.setOwnerId(Integer.valueOf(ownerIdStr).intValue());
        } else {
            result.setOwnerId(0);
        }
        String groupIdStr = new String(groupId, ASCII).trim();
        if (groupIdStr.length() != 0) {
            result.setGroupId(Integer.valueOf(groupIdStr).intValue());
        } else {
            result.setGroupId(0);
        }
        String fileModeStr = new String(fileMode, ASCII).trim();
        if (fileModeStr.length() != 0) {
            result.setFileMode(Integer.valueOf(fileModeStr).intValue());
        } else {
            result.setFileMode(0);
        }
        String fileSizeStr = new String(fileSize, ASCII).trim();
        if( fileSizeStr.length() == 0 ) {
            throw new IOException("corrupted archive data. invalid file data lenght");
        }
        byte[] data = new byte[Integer.valueOf(fileSizeStr).intValue()];
        read(data);
        result.setData(data);
        if (data.length % 2 == 1) {
            skip(1);
        }
        if (filenameStr.equals("//") && result.getFileModificationTimestamp() == 0) {
            longFileNames = result.getData();
            result = getNextEntry();
        }

        if (!filenameStr.equals("//") && longFileNames != null) {
            result.setFilename(resolveName(result.getFilename()));
        }

        return result;
    }

    private String resolveName(String offsetName) throws IOException {
        if (!offsetName.startsWith("/")) {
            if (!offsetName.endsWith("/")) {
                throw new IOException("corrupted archive data. invalid short file name: " + offsetName);
            }
            return offsetName.substring(0, offsetName.length() - 1);
        }
        int offset;
        try {
            offset = Integer.valueOf(offsetName.substring(1).trim()).intValue();
        } catch(Throwable e) {
            throw new IOException("corrupted archive data. invalid long file name offset: " + offsetName);
        }
        if (offset > longFileNames.length - 1) {
            throw new IOException("corrupted archive data. invalid long file name offset: " + offset);
        }
        int end = offset;
        for (int i = offset; i < longFileNames.length; i++) {
            if (longFileNames[i] == '/') {
                if (i == longFileNames.length - 1 || longFileNames[i + 1] == '\n') {
                    end = i;
                    break;
                }
            }
        }
        if (end == offset) {
            throw new IOException("corrupted archive data. invalid long file name offset: " + offset);
        }
        byte[] filename = new byte[end - offset];
        System.arraycopy(longFileNames, offset, filename, 0, filename.length);
        return new String(filename, ASCII).trim();
    }
    
    public void close() throws IOException {
        super.close();
        isClosed = true;
    }

}
