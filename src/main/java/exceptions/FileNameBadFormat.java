package main.java.exceptions;

public class FileNameBadFormat extends Exception {

    public FileNameBadFormat() {
        super();
    }

    public FileNameBadFormat(String message) {
        super(message);
    }
}
