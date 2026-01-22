package org.group10.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static com.google.common.io.MoreFiles.deleteRecursively;

public class FolderCleaner {
    public static void cleanTmpDir(String prefix) {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

        if (!Files.isDirectory(tmpDir)) return;

        try {
            DirectoryStream<Path> stream =
                     Files.newDirectoryStream(tmpDir, prefix+"compiled_*");

            for (Path dir : stream) {
                if (Files.isDirectory(dir)) {
                    deleteRecursively(dir);
                }
            }
        } catch (IOException e) {
            // Ignore
        }
    }
}
