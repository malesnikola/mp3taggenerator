package main.java.domain;

public class FailedFileDetails {
    private String filePath;
    private String errorMessage;

    public FailedFileDetails() {
    }

    public FailedFileDetails(String filePath, String errorMessage) {
        this.filePath = filePath;
        this.errorMessage = errorMessage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
