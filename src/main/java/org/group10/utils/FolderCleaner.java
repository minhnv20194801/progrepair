package org.group10.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.io.MoreFiles.deleteRecursively;

/**
 * Utility class for cleaning temporary directories. <br>
 *
 * Created because the temporary directories that was not cleaned
 * properly crashed my laptop and made it non-bootable. <br>
 *
 * Which is why I suggest running this project with docker
 */
public class FolderCleaner {
    public static void cleanTmpDir(String prefix) {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

        if (!Files.isDirectory(tmpDir)) return;

        try {
            long id = ProcessHandle.current().pid();
            DirectoryStream<Path> stream =
                    Files.newDirectoryStream(tmpDir, prefix + id + "compiled_*");

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
