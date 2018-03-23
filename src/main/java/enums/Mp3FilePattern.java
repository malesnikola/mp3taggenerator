package main.java.enums;

/**
 * Patterns for ".mp3" files based on style of file name.
 * When file is loaded, default pattern is UNKNOWN.
 * Examples for other patterns:
 * File name                                            => Pattern
 * Milomir Miljanić - 2009 - Da se ne zaboravi          => A-Y-T
 * Milomir Miljanić - UŽIVO - 2009 - Da se ne zaboravi  => A-L-Y-T
 * Milomir Miljanić - Da se ne zaboravi                 => A-T
 * Da se ne zaboravi                                    => T
 */
public enum Mp3FilePattern {
    UNKNOWN("unknown"),
    ARTIST_YEAR_TITLE("A-Y-T"),
    ARTIST_LIVE_YEAR_TITLE("A-L-Y-T"),
    ARTIST_TITLE("A-T"),
    TITLE("T");

    private String patternCode;

    Mp3FilePattern(String patternCode) {
        this.patternCode = patternCode;
    }

    public String getPatternCode() {
        return patternCode;
    }
}
