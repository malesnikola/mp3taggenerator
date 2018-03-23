package main.java.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import main.java.domain.Mp3FileWrapper;
import main.java.enums.Mp3FilePattern;
import main.java.exceptions.FileNameBadFormatException;
import main.java.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for manipulation with ".mp3" files.
 */
public class Mp3Service {
    /**
     * All possible cyrillic letters and marks.
     */
    private static String[] abcCyr = {" ", ".", "!", "%", "'", "-", "&", ",", "(", ")", "а", "б", "в", "г", "д", "ђ", "е", "ж", "з", "и", "ј", "к", "л", "љ",  "м", "н", "њ",  "о", "п", "р", "с", "т", "ћ", "у", "ф", "х", "ц", "ч", "џ",  "ш", "А", "Б", "В", "Г", "Д", "Ђ", "Е", "Ж", "З", "И", "Ј", "К", "Л", "Љ",  "М", "Н", "Њ",  "О", "П", "Р", "С", "Т", "Ћ", "У", "Ф", "Х", "Ц", "Ч", "Џ",  "Ш", "a", "b", "v", "g", "d", "đ", "e", "ž", "z", "i", "j", "k", "l", "lj", "m", "n", "nj", "o", "p", "r", "s", "t", "ć", "u", "f", "h", "c", "č", "dž", "š", "x",  "A", "B", "V", "G", "D", "Đ", "E", "Ž", "Z", "I", "J", "K", "L", "Lj", "M", "N", "Nj", "O", "P", "R", "S", "T", "Ć", "U", "F", "H", "C", "Č", "Dž", "Š", "X",  "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    /**
     * All possible latin letters and marks.
     */
    private static String[] abcLat = {" ", ".", "!", "%", "'", "-", "&", ",", "(", ")", "a", "b", "v", "g", "d", "đ", "e", "ž", "z", "i", "j", "k", "l", "lj", "m", "n", "nj", "o", "p", "r", "s", "t", "ć", "u", "f", "h", "c", "č", "dž", "š", "A", "B", "V", "G", "D", "Đ", "E", "Ž", "Z", "I", "J", "K", "L", "Lj", "M", "N", "Nj", "O", "P", "R", "S", "T", "Ć", "U", "F", "H", "C", "Č", "Dž", "Š", "а", "б", "в", "г", "д", "ђ", "е", "ж", "з", "и", "ј", "к", "л", "љ",  "м", "н", "њ",  "о", "п", "р", "с", "т", "ћ", "у", "ф", "х", "ц", "ч", "џ",  "ш", "кс", "А", "Б", "В", "Г", "Д", "Ђ", "Е", "Ж", "З", "И", "Ј", "К", "Л", "Љ",  "М", "Н", "Њ",  "О", "П", "Р", "С", "Т", "Ћ", "У", "Ф", "Х", "Ц", "Ч", "Џ",  "Ш", "КС", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    /**
     * Translate message from latin to cyrillic letters.
     * @param message Original latin message.
     * @return
     */
    @NotNull
    private static String translate(String message) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++) {
                if (message.charAt(i) == abcCyr[x].charAt(0)) {
                    if (message.charAt(i) == 'L' && (i+1) < message.length() && (message.charAt(i+1) == 'j' || message.charAt(i+1) == 'J')) {
                        builder.append("Љ");
                        i++;
                    } else if (message.charAt(i) == 'N' && (i+1) < message.length() && (message.charAt(i+1) == 'j' || message.charAt(i+1) == 'J')) {
                        builder.append("Њ");
                        i++;
                    } else if (message.charAt(i) == 'D' && (i+1) < message.length() && (message.charAt(i+1) == 'ž' || message.charAt(i+1) == 'Ž')) {
                        builder.append("Џ");
                        i++;
                    } else if (message.charAt(i) == 'Њ') {
                        builder.append("Nj");
                    } else if (message.charAt(i) == 'Љ') {
                        builder.append("Lj");
                    } else if (message.charAt(i) == 'Џ') {
                        builder.append("Dž");
                    } else if (message.charAt(i) == 'l' && (i+1) < message.length() && message.charAt(i+1) == 'j') {
                        builder.append("љ");
                        i++;
                    } else if (message.charAt(i) == 'n' && (i+1) < message.length() && message.charAt(i+1) == 'j') {
                        builder.append("њ");
                        i++;
                    } else if (message.charAt(i) == 'd' && (i+1) < message.length() && message.charAt(i+1) == 'ž') {
                        builder.append("џ");
                        i++;
                    } else if (message.charAt(i) == 'њ') {
                        builder.append("nj");
                    } else if (message.charAt(i) == 'љ') {
                        builder.append("lj");
                    } else if (message.charAt(i) == 'џ') {
                        builder.append("dž");
                    } else {
                        builder.append(abcLat[x]);
                    }
                    break;
                }
            }
        }
        return builder.toString();
    }

    /**
     * Get extension from file.
     * @param file File.
     * @return Returns extension of fole (e.g. ".mp3")
     */
    @NotNull
    public static String getFileExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf("."));
    }

    /***
     * Get index of interested appearance of character.
     * For example: getIndexOf("A - B - C", 2, '-') returns 6, because second appearance of '-' is on index 6 (between B and C).
     * @param name Represent name.
     * @param appearance Represent number of appearance of character for search which is interested.
     * @param characterForSearch Represent character which index is search in name.
     * @return Index of interested appearance of character.
     */
    private static int getIndexOf(String name, int appearance, char characterForSearch) {
        for (int i = 0; i < name.length(); i++) {
            char currChar = name.charAt(i);
            if ((currChar == characterForSearch) && (--appearance == 0)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get artist from file name based on file pattern.
     * @param fileName File name.
     * @param pattern Selected pattern (e.g. "A-Y-T").
     * @return Returns string which represent artist.
     * @throws FileNameBadFormatException If file name has bad format for selected pattern.
     */
    private static String getArtistFromFileNameAndPattern(String fileName, Mp3FilePattern pattern) throws FileNameBadFormatException {
        switch (pattern) {
            case ARTIST_YEAR_TITLE:
            case ARTIST_TITLE:
                int firstIndexOfDash = getIndexOf(fileName, 1, '-');
                if (firstIndexOfDash == -1) {
                    throw new FileNameBadFormatException("File name has bad format for pattern: " + pattern.getPatternCode());
                }

                return fileName.substring(0, firstIndexOfDash).trim();

            case ARTIST_LIVE_YEAR_TITLE:
                int secondIndexOfDash = getIndexOf(fileName, 2, '-');
                if (secondIndexOfDash == -1) {
                    throw new FileNameBadFormatException("File name has bad format for pattern: " + pattern.getPatternCode());
                }

                return fileName.substring(0, secondIndexOfDash).trim();

            default:
                return "";
        }
    }

    /**
     * Get year from file name based on file pattern.
     * @param fileName File name.
     * @param pattern Selected pattern (e.g. "A-Y-T").
     * @return Returns string which represent year.
     * @throws FileNameBadFormatException If file name has bad format for selected pattern.
     */
    private static String getYearFromFileNameAndPattern(String fileName, Mp3FilePattern pattern) throws FileNameBadFormatException {
        switch (pattern) {
            case ARTIST_YEAR_TITLE:
                int firstIndexOfDash = getIndexOf(fileName, 1, '-');
                int secondIndexOfDash = getIndexOf(fileName, 2, '-');
                if ((firstIndexOfDash == -1) || (secondIndexOfDash == -1)) {
                    throw new FileNameBadFormatException("File name has bad format for pattern: " + pattern.getPatternCode());
                }

                return fileName.substring((firstIndexOfDash + 1), secondIndexOfDash).trim();

            case ARTIST_LIVE_YEAR_TITLE:
                secondIndexOfDash = getIndexOf(fileName, 2, '-');
                int thirdIndexOfDash = getIndexOf(fileName, 3, '-');
                if ((secondIndexOfDash == -1) || (thirdIndexOfDash == -1)) {
                    throw new FileNameBadFormatException("File name has bad format for pattern: " + pattern.getPatternCode());
                }

                return fileName.substring((secondIndexOfDash + 1), thirdIndexOfDash).trim();

            default:
                return "";
        }
    }

    /**
     * Get title from file name based on file pattern.
     * @param fileName File name.
     * @param pattern Selected pattern (e.g. "A-Y-T").
     * @return Returns string which represent title.
     * @throws FileNameBadFormatException If file name has bad format for selected pattern.
     */
    private static String getTitleFromFileNameAndPattern(String fileName, Mp3FilePattern pattern) throws FileNameBadFormatException {
        switch (pattern) {
            case ARTIST_YEAR_TITLE:
                int secondIndexOfDash = getIndexOf(fileName, 2, '-');
                if (secondIndexOfDash == -1) {
                    throw new FileNameBadFormatException("File name has bad format for pattern: " + pattern.getPatternCode());
                }

                return fileName.substring(secondIndexOfDash + 1).trim();

            case ARTIST_TITLE:
                int firstIndexOfDash = getIndexOf(fileName, 1, '-');
                if (firstIndexOfDash == -1) {
                    throw new FileNameBadFormatException("File name has bad format for pattern: " + pattern.getPatternCode());
                }

                return fileName.substring(firstIndexOfDash + 1).trim();

            case ARTIST_LIVE_YEAR_TITLE:
                int thirdIndexOfDash = getIndexOf(fileName, 3, '-');
                if (thirdIndexOfDash == -1) {
                    throw new FileNameBadFormatException("File name has bad format for pattern: " + pattern.getPatternCode());
                }

                return fileName.substring(thirdIndexOfDash + 1).trim();

            default:
                return fileName.trim();
        }
    }

    /**
     * Create ID3v2 tag for forwarded ".mp3" file.
     * Cyrillic tags for files which name contains 'y' or 'MC' are not possible. In that case, tags will always be latin.
     * @param file Mp3FileWrapper
     * @param isCyrillicTags Indicates if tags has to be cyrillic.
     * @return Returns created ID3v2 tags.
     * @throws FileNameBadFormatException If file name has bad format for selected pattern.
     */
    public static ID3v2 crateID3v2Tag(Mp3FileWrapper file, boolean isCyrillicTags) throws FileNameBadFormatException {
        String filePath = file.getFilename();
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        String artist = getArtistFromFileNameAndPattern(fileName, file.getPattern());
        String year = getYearFromFileNameAndPattern(fileName, file.getPattern());
        String title = getTitleFromFileNameAndPattern(fileName, file.getPattern());

        if (isCyrillicTags && !(artist.contains("y") || artist.contains("Y") || artist.contains("MC"))
                           && !(title.contains("y") || title.contains("Y") || title.contains("MC"))) {
            artist = translate(artist);
            title = translate(title);
        }

        ID3v2 id3v2Tag = new ID3v23Tag();
        id3v2Tag.setArtist(artist);
        id3v2Tag.setTitle(title);
        id3v2Tag.setYear(year);

        return id3v2Tag;
    }

    /**
     * Check does list contains any file with ".mp3" extension.
     * @param files List with files.
     * @return Returns true if contains, else false.
     */
    public static boolean ifContainsAnyMp3File(List<File> files) {
        for (File file : files) {
            if (Constants.MP3_FILE_TYPE_EXTENSION.equalsIgnoreCase(getFileExtension(file))) {
                return true;
            }
        }

        return false;
    }

}
