package com.example.filetransfer.model;

public class FileMetadata {
    private String filename;
    private String originalName;
    private String title;
    private String description;
    private long size;
    private long uploadTime;
    private long expirationTime;

    public FileMetadata() {
    }

    public FileMetadata(String filename, String originalName, String title, String description, long size, long uploadTime, long expirationTime) {
        this.filename = filename;
        this.originalName = originalName;
        this.title = title;
        this.description = description;
        this.size = size;
        this.uploadTime = uploadTime;
        this.expirationTime = expirationTime;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    public long getTimeRemainingSeconds() {
        long remaining = expirationTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    public String getFormattedSize() {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
