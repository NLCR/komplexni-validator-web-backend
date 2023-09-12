package nkp.pspValidator.web.backend.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {

    public static void deleteNonemptyDir(File dir) throws IOException {
        /*File[] files = dir.listFiles();
        for (File file : files) {
            if (!file.delete() && file.isDirectory()) {
                deleteNonemptyDir(file);
            }
        }
        boolean deleted = dir.delete();*/
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void unzip(File zipFile, File outFolder) throws IOException {
        // Create Output outFolder if it does not exists
        //File outFolder = new File(outputDir);
        if (!outFolder.exists()) {
            outFolder.mkdirs();
        }
        // Create buffer
        byte[] buffer = new byte[1024];
        ZipInputStream zipIs = null;
        try {
            // Create ZipInputStream read a file from path.
            zipIs = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry = null;
            // Read ever Entry (From top to bottom until the end)
            while ((entry = zipIs.getNextEntry()) != null) {
                String entryName = entry.getName();
                //System.out.println("entry name: " + entryName);
                String outFileName = outFolder.getAbsolutePath() + File.separator + entryName;
                //System.out.println("Unzip: " + outFileName);
                File entryFile = new File(outFileName);
                if (entry.isDirectory()) {
                    // Make directories
                    entryFile.mkdirs();
                } else {
                    // Make parent directories
                    entryFile.getParentFile().mkdirs();

                    // Create Stream to write file.
                    FileOutputStream fos = new FileOutputStream(outFileName);
                    int len;
                    // Read the data on the current entry.
                    while ((len = zipIs.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
        } finally {
            try {
                zipIs.close();
            } catch (Exception e) {
            }
        }
    }

    public static void zipFolder(String sourceFolder, String outputZipFile) {
        try {
            FileOutputStream fos = new FileOutputStream(outputZipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            addFolderToZip(sourceFolder, sourceFolder, zos);

            zos.close();
            fos.close();

            System.out.println("Adresář " + sourceFolder + " byl zabalen do souboru " + outputZipFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addFolderToZip(String folderPath, String sourceFolder, ZipOutputStream zos) throws IOException {
        File folder = new File(folderPath);

        for (String fileName : folder.list()) {
            String filePath = folderPath + File.separator + fileName;
            if (new File(filePath).isDirectory()) {
                addFolderToZip(filePath, sourceFolder, zos);
            } else {
                String relativePath = filePath.substring(sourceFolder.length() + 1);
                ZipEntry ze = new ZipEntry(relativePath);
                zos.putNextEntry(ze);

                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                fis.close();
                zos.closeEntry();
            }
        }
    }
}
