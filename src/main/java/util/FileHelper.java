package main.java.util;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import main.java.exceptions.FileNameBadFormat;

import java.io.File;

public class FileHelper {

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

    public static String getFileExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf("."));
    }

    public static void crateMp3Tags(File file, boolean isCyrillicTags) throws FileNameBadFormat {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        String[] fileParts = fileName.split("-");
        if (fileParts.length < 2 || fileParts.length > 3) {
            throw new FileNameBadFormat();
        }

        String artistName = fileParts[0].trim();
        String titleName = fileParts[fileParts.length - 1].trim();
        String year = (fileParts.length == 3 ? fileParts[2].trim() : "");

        ID3v1 id3v1Tag = new ID3v1Tag();
        ID3v2 id3v2Tag = new ID3v24Tag();
        id3v2Tag.setArtist(artistName);
        id3v2Tag.setTitle(titleName);
        id3v2Tag.setYear(year);
    }
}
