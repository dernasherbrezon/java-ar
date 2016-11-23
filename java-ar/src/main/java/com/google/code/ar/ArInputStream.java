package com.google.code.ar;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * InputStream to read AR files. Normal scenario:
 * 
 * <blockquote><pre>
 * InputStream is = ...
 * ArInputStream aris = null;
 * try {
 *      aris = new ArInputStream(is);
 *      ArEntry curEntry = null;
 *      while( (curEntry = aris.getNextEntry()) != null ) {
 *          //process entry
 *      }
 * } catch(Exception e) {
 *  //do logging. handle exception
 * } finally {
 *      if( aris != null ) {
 *          try {
 *              aris.close();
 *          } catch(IOException e) {
 *              //do logging
 *          }
 *      }
 * }
 * </pre></blockquote>
 * 
 * Supports only GNU compatible AR data.
 * 
 * @author dernasherbrezon
 *
 */
public class ArInputStream extends FilterInputStream {

    private static final Charset ASCII = Charset.forName("ASCII");
    private byte[] longFileNames;
    private boolean isClosed = false;

    /**
     * @param in - underlying InputStream. Cannot be closed or null
     * @throws IOException if provided stream is not AR stream
     * @throws IllegalArgumentException if provided stream is null
     */
    public ArInputStream(InputStream in) throws IOException {
        super(in);
        if (in == null) {
            throw new IllegalArgumentException("input stream cannot be null");
        }
        readHeader();
    }

    private void readHeader() throws IOException {
        byte[] header = new byte[8];
        long readBytes = read(header);
        if (readBytes != 8) {
            throw new IOException("unexpected end of stream. Expected: 8. Read: " + readBytes);
        }
        String headerStr = new String(header, ASCII);
        if (headerStr.trim().length() == 0 || !headerStr.equals("!<arch>\n")) {
            throw new IOException("not an \"AR\" archive");
        }
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
    	//do nothing
    }

    @Override
    public synchronized void reset() throws IOException {
    	//do nothing
    }

    /**
     * Get next entry from AR stream.
     * @return null if end of stream reached.
     * @throws IOException if stream has been closed or the underlaying stream is closed or isn't available or corrupted archive or unsupported AR archive format
     */
    public ArEntry getNextEntry() throws IOException {
        if (available() == 0) {
            return null;
        }

        if (isClosed) {
            throw new IOException("Stream closed");
        }

        byte[] fileName = new byte[16];
        long readBytes = read(fileName);
        if (readBytes != 16) {
            throw new IOException("unexpected end of stream. Expected: 16. Read: " + readBytes);
        }
        byte[] timestamp = new byte[12];
        readBytes = read(timestamp);
        if (readBytes != 12) {
            throw new IOException("unexpected end of stream. Expected: 12. Read: " + readBytes);
        }
        byte[] ownerId = new byte[6];
        readBytes = read(ownerId);
        if (readBytes != 6) {
            throw new IOException("unexpected end of stream. Expected: 6. Read: " + readBytes);
        }
        byte[] groupId = new byte[6];
        readBytes = read(groupId);
        if (readBytes != 6) {
            throw new IOException("unexpected end of stream. Expected: 6. Read: " + readBytes);
        }
        byte[] fileMode = new byte[8];
        readBytes = read(fileMode);
        if (readBytes != 8) {
            throw new IOException("unexpected end of stream. Expected: 8. Read: " + readBytes);
        }
        byte[] fileSize = new byte[10];
        readBytes = read(fileSize);
        if (readBytes != 10) {
            throw new IOException("unexpected end of stream. Expected: 10. Read: " + readBytes);
        }
        byte[] magic = new byte[2];
        readBytes = read(magic);
        if (readBytes != 2) {
            throw new IOException("unexpected end of stream. Expected: 2. Read: " + readBytes);
        }
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
        if (fileSizeStr.length() == 0) {
            throw new IOException("corrupted archive data. invalid file data lenght");
        }
        byte[] data = new byte[Integer.valueOf(fileSizeStr).intValue()];
        readBytes = read(data);
        if (readBytes != data.length) {
            throw new IOException("unexpected end of stream. Expected: " + data.length + ". Read: " + readBytes);
        }
        result.setData(data);
        if (data.length % 2 == 1) {
            long skippedBytes = skip(1);
            if (skippedBytes != 1) {
                throw new IOException("unexpected end of stream. Expected: 1. Read: " + skippedBytes);
            }
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

    /**
     * Closes underlying stream
     */
    @Override
    public void close() throws IOException {
        super.close();
        isClosed = true;
    }

}
