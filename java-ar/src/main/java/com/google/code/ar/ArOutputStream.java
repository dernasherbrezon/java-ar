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

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * OutputStream to write AR files. Normal scenario:
 * <p><blockquote><pre>
 * OutputStream os = ...
 * ArEntry[] entries = ...
 * ArOutputStream aros = null;
 * try {
 *      aros = new ArOutputStream(os);
 *      aros.setEntries(entries);
 * } catch(Exception e) {
 *  //do logging. handle exception
 * } finally {
 *      if( aros != null ) {
 *          try {
 *              aros.close();
 *          } catch(IOException e) {
 *              //do logging
 *          }
 *      }
 * }
 * </pre></blockquote></p>
 * 
 * If ArEntry contains long file name, then ArOutputStream produces GNU compatible AR data.
 * 
 * @author dernasherbrezon
 *
 */
public class ArOutputStream extends FilterOutputStream {

    static final Charset ASCII = Charset.forName("ASCII");
    final static byte[] MAGIC = new byte[]{96, 10};
    final static byte[] HEADER = new byte[]{33, 60, 97, 114, 99, 104, 62, 10};
    private boolean isEntriesPresent = false;
    private boolean isClosed = false;

    /**
     * @param out - underlaying OutputStream. Cannot be closed or null
     * @throws IllegalArgumentException if provided OutputStream is null
     */
    public ArOutputStream(OutputStream out) {
        super(out);
        if (out == null) {
            throw new IllegalArgumentException("outputstream cannot be null");
        }
    }

    /**
     * Write entries to the underlaying OutputStream. Must be called once for every OutputStream. Could be empty or null.
     * @param entries
     * @throws IOException if stream has been closed<br>underlaying stream has been closed<br>second attempt to set entries<br>
     * @throws IllegalArgumentException if provided entries contain invalid data.
     * @see com.google.code.ar.ArEntry
     */
    public void setEntries(ArEntry[] entries) throws IOException {
        if (isClosed) {
            throw new IOException("stream closed");
        }
        if (isEntriesPresent) {
            throw new IOException("archive entries already specified");
        }
        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                ArEntryValidator.validateInMemoryEntry(entries[i]);
            }
        }
        write(HEADER);
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
                if (curEntry == null) {
                    continue;
                }
                newNamesInsteadOfLong[i] = "/" + namesDataOffset;
                byte[] curName = (curEntry.getFilename() + "/\n").getBytes(ASCII);
                baos.write(curName);
                namesDataOffset += curName.length;
            }
            //filesize
            write(String.valueOf(baos.size()), 10);
            write(MAGIC);
            //actual data
            write(baos.toByteArray());
            if (baos.size() % 2 != 0) { //align to 2
                write('\n');
            }
        }

        String time = getCurTime();

        for (int i = 0; i < entries.length; i++) {
            ArEntry curEntry = entries[i];
            if (curEntry == null) {
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
            write(MAGIC);
            write(curEntry.getData());
            if (curEntry.getData().length % 2 != 0) {
                write('\n');
            }
        }
        isEntriesPresent = true;
    }

    static String getCurTime() {
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

    private static boolean hasLongNames(ArEntry[] entries) {
        for (int i = 0; i < entries.length; i++) {
            ArEntry curEntry = entries[i];
            if (curEntry == null) {
                continue;
            }
            byte[] filename = curEntry.getFilename().getBytes(ASCII);
            if (filename.length > 16) {
                return true;
            }
        }
        return false;
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
