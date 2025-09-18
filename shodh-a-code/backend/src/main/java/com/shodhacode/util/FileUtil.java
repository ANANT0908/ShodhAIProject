package com.shodhacode.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class FileUtil {

    public static List<String> listFilesSorted(String directory) {
        File dir = new File(directory);
        File[] files = dir.listFiles();
        if (files == null) {
            return List.of();
        }

        return Arrays.stream(files)
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::getName)) // <-- explicit Comparator import/usage
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
