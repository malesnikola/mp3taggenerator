package main.java.exceptions;

public class FileNameBadFormatException extends Exception {

    public FileNameBadFormatException() {
        super();
    }

    public FileNameBadFormatException(String message) {
        super(message);
    }
}
