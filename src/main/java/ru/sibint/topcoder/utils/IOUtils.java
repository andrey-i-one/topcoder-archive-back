package ru.sibint.topcoder.utils;

import java.io.*;
import java.util.Scanner;

public class IOUtils {

    public static void saveToFile(String fileName, String content) throws Exception {
        PrintWriter printWriter = new PrintWriter(fileName);
        printWriter.print(content);
        printWriter.flush();
        printWriter.close();
    }

    public static String readInputStream(InputStream is) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder lines = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            lines.append(line).append("\n");
        }
        return lines.toString().trim();
    }

    public static String readOutputFromFile(String fileName) throws Exception {
        Scanner scanner = new Scanner(new File(fileName));
        StringBuilder stringBuilder = new StringBuilder();
        while(scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
            if(scanner.hasNextLine()) {
                stringBuilder.append("\n");
            }
        }
        scanner.close();
        return stringBuilder.toString();
    }

    public static boolean isEqualOutput(String actual, String expected) {
        return actual.trim().equals(expected.trim());
    }

}
