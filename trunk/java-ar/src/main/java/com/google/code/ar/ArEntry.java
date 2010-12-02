package com.google.code.ar;

public class ArEntry {

    private String filename;
    private long fileModificationTimestamp;
    private int ownerId;
    private int groupId;
    private int fileMode;
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFileModificationTimestamp() {
        return fileModificationTimestamp;
    }

    public void setFileModificationTimestamp(long fileModificationTimestamp) {
        this.fileModificationTimestamp = fileModificationTimestamp;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getFileMode() {
        return fileMode;
    }

    public void setFileMode(int fileMode) {
        this.fileMode = fileMode;
    }

}
