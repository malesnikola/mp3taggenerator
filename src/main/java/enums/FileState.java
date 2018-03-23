package main.java.enums;

import org.jetbrains.annotations.Contract;

/**
 * State of file:
 * SAVED: File is saved on disc.
 * MODIFIED: File is modified, but modification has not been saved on disc jet.
 * FAILED_MODIFIED: File modification has failed.
 * FAILED_SAVED: File has not saved.
 */
public enum FileState {
    SAVED ("saved"),
    MODIFIED("modified"),
    FAILED_MODIFIED("failed modified"),
    FAILED_SAVED("failed saved");

    private String stateText;

    FileState(String stateText) {
        this.stateText = stateText;
    }

    @Contract(pure = true)
    public String getStateText() {
        return stateText;
    }

    public static FileState fromString(String text) {
        for (FileState b : FileState.values()) {
            if (b.stateText.equalsIgnoreCase(text)) {
                return b;
            }
        }

        return null;
    }
}
