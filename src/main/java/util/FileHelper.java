package main.java.util;

import com.mpatric.mp3agic.*;
import main.java.exceptions.FileNameBadFormatException;
import main.java.service.Mp3Service;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FileHelper {

    private static Logger logger = Logger.getLogger(FileHelper.class);

    @NotNull
    private static String translate(String message) {
        String[] abcCyr = {" ", ",", "(", ")", "а", "б", "в", "г", "д", "ђ", "е", "ж", "з", "и", "ј", "к", "л", "љ",  "м", "н", "њ",  "о", "п", "р", "с", "т", "ћ", "у", "ф", "х", "ц", "ч", "џ",  "ш", "А", "Б", "В", "Г", "Д", "Ђ", "Е", "Ж", "З", "И", "Ј", "К", "Л", "Љ",  "М", "Н", "Њ",  "О", "П", "Р", "С", "Т", "Ћ", "У", "Ф", "Х", "Ц", "Ч", "Џ",  "Ш", "a", "b", "v", "g", "d", "đ", "e", "ž", "z", "i", "j", "k", "l", "lj", "m", "n", "nj", "o", "p", "r", "s", "t", "ć", "u", "f", "h", "c", "č", "dž", "š", "A", "B", "V", "G", "D", "Đ", "E", "Ž", "Z", "I", "J", "K", "L", "Lj", "M", "N", "Nj", "O", "P", "R", "S", "T", "Ć", "U", "F", "H", "C", "Č", "Dž", "Š"};
        String[] abcLat = {" ", ",", "(", ")", "a", "b", "v", "g", "d", "đ", "e", "ž", "z", "i", "j", "k", "l", "lj", "m", "n", "nj", "o", "p", "r", "s", "t", "ć", "u", "f", "h", "c", "č", "dž", "š", "A", "B", "V", "G", "D", "Đ", "E", "Ž", "Z", "I", "J", "K", "L", "Lj", "M", "N", "Nj", "O", "P", "R", "S", "T", "Ć", "U", "F", "H", "C", "Č", "Dž", "Š", "а", "б", "в", "г", "д", "ђ", "е", "ж", "з", "и", "ј", "к", "л", "љ",  "м", "н", "њ",  "о", "п", "р", "с", "т", "ћ", "у", "ф", "х", "ц", "ч", "џ",  "ш", "А", "Б", "В", "Г", "Д", "Ђ", "Е", "Ж", "З", "И", "Ј", "К", "Л", "Љ",  "М", "Н", "Њ",  "О", "П", "Р", "С", "Т", "Ћ", "У", "Ф", "Х", "Ц", "Ч", "Џ",  "Ш"};
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
                    } else if (message.charAt(i) == 'D' && (i+1) < message.length() && (message.charAt(i+1) == 'j' || message.charAt(i+1) == 'J')) {
                        builder.append("Ђ");
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
                    } else if (message.charAt(i) == 'd' && (i+1) < message.length() && message.charAt(i+1) == 'j') {
                        builder.append("ђ");
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

    @NotNull
    public static String getFileExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf("."));
    }

    public static ID3v2 crateID3v2Tag(Mp3File file, boolean isCyrillicTags) throws FileNameBadFormatException {
        String filePath = file.getFilename();
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        String[] fileParts = fileName.split("-");
        if (fileParts.length < 2 || fileParts.length > 3) {
            throw new FileNameBadFormatException("File nam has bad format. Valid format is Artist - Year - Title.mp3");
        }

        String artistName = fileParts[0].trim();
        String titleName = fileParts[fileParts.length - 1].trim();
        String year = (fileParts.length == 3 ? fileParts[1].trim() : "");
        if (isCyrillicTags) {
            artistName = translate(artistName);
            titleName = translate(titleName);
        }

        ID3v2 id3v2Tag = new ID3v23Tag();
        id3v2Tag.setArtist(artistName);
        id3v2Tag.setTitle(titleName);
        id3v2Tag.setYear(year);

        return id3v2Tag;
    }

    public static boolean ifContainsAnyMp3File(List<File> files) {
        for (File file : files) {
            if (Constants.MP3_FILE_TYPE_EXTENSION.equalsIgnoreCase(getFileExtension(file))) {
                return true;
            }
        }

        return false;
    }

    public static List<File> getAllMp3Files(List<File> files) {
        List<File> mp3Files = new LinkedList<>();
        if (files != null) {
            for (File file : files) {
                if (Constants.MP3_FILE_TYPE_EXTENSION.equalsIgnoreCase(getFileExtension(file))) {
                    mp3Files.add(file);
                }
            }
        }

        return mp3Files;
    }

}
