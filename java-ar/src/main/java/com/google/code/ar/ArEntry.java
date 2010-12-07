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

/**
 * Represents one entry for AR archive. 
 * 
 * @author dernasherbrezon
 *
 */
public class ArEntry {

    private String filename;
    private long fileModificationTimestamp;
    private int ownerId;
    private int groupId;
    private int fileMode;
    private byte[] data;

    /**
     * File data. Couldnt be null, but could be empty (byte[0])
     * @return
     */
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * File name. Could be null or empty. Should be in ASCII (one byte) encoding.
     * @return
     */
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Unix milliseconds from GMT 01 Jan 00:00:00 1970. Should be positive
     * @return
     */
    public long getFileModificationTimestamp() {
        return fileModificationTimestamp;
    }

    void setFileModificationTimestamp(long fileModificationTimestamp) {
        this.fileModificationTimestamp = fileModificationTimestamp;
    }

    /**
     * File owner acl. Should be positive
     * @see <a href="http://en.wikipedia.org/wiki/Filesystem_permissions">Filesystem_permissions</a>
     * @return
     */
    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * File group acl. Should be positive
     * @see <a href="http://en.wikipedia.org/wiki/Filesystem_permissions">Filesystem_permissions</a>
     * @return
     */
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * File mode. Should be positive
     * @see <a href="http://en.wikipedia.org/wiki/Filesystem_permissions">Filesystem_permissions</a>
     * @return
     */
    public int getFileMode() {
        return fileMode;
    }

    public void setFileMode(int fileMode) {
        this.fileMode = fileMode;
    }

}
