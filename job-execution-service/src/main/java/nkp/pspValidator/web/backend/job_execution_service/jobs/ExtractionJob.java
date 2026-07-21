package nkp.pspValidator.web.backend.job_execution_service.jobs;

import nkp.pspValidator.web.backend.utils.Config;

import java.io.*;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExtractionJob extends Job {

    public ExtractionJob(String validationId) {
        super(validationId);
    }

    @Override
    public void run() {
        new Thread(() -> {
            updateValidationState("EXTRACTING");

            File workingDir = new File(Config.instanceOf().getValidationWorkingDir(), validationId);
            workingDir.setReadable(true, false);
            workingDir.setExecutable(true, false);

            File extractionLogFile = new File(workingDir, "extraction.log");
            File avLogFile = new File(workingDir, "clamav.log");

            try (
                    PrintStream extractionOut = new PrintStream(extractionLogFile);
                    PrintStream avLogOut = new PrintStream(avLogFile)
            ) {
                extractionOut.println("Extraction log for validation " + validationId);
                avLogOut.println("ClamAV log for validation " + validationId);

                File extractedDir;

                try {
                    extractedDir = extractZipFile(validationId, extractionOut);
                } catch (Throwable e) {
                    extractionOut.println("Error extracting zip file: " + e.getMessage());
                    e.printStackTrace(extractionOut);

                    updateValidationState("ERROR");
                    return;
                }

                if (Config.instanceOf().isJobExecutionServiceClamAvEnabled()) {
                    try {
                        extractionOut.println("Scanning extracted files by ClamAV...");
                        avLogOut.println("Scanning extracted files by ClamAV...");

                        new ClamAvHelper().scanDirectory(extractedDir, avLogOut);
                    } catch (Throwable e) {
                        avLogOut.println("Error scanning extracted files by ClamAV: " + e.getMessage());
                        e.printStackTrace(avLogOut);

                        updateValidationState("ERROR");
                        return;
                    }
                } else {
                    extractionOut.println("ClamAV scanning disabled in configuration (job-execution-service.clamav.enabled), skipping.");
                    avLogOut.println("ClamAV scanning disabled in configuration (job-execution-service.clamav.enabled), skipping.");
                }

                //TODO: vytahnout z rozbaleneho baliku dalsi data (id baliku, etc) a ulozit do zaznamu Validace
                updateValidationState("READY_FOR_EXECUTION");

            } catch (IOException e) {
                System.err.println("Error initializing validation log files: " + e.getMessage());
                updateValidationState("ERROR");
            }
        }).start();
    }

    private File extractZipFile(String validationId, PrintStream out) throws IOException {
        File workingDir = new File(Config.instanceOf().getValidationWorkingDir(), validationId);
        File zipFile = workingDir.listFiles((dir, name) -> name.endsWith(".zip"))[0];
        String packageName = zipFile.getName().substring(0, zipFile.getName().length() - ".zip".length());
        File destDir = new File(workingDir, packageName);
        out.println(String.format("Extracting: %s -> %s", zipFile.getAbsolutePath(), destDir.getAbsolutePath()));
        new ZipHelper().unzip(zipFile, destDir);
        flattenSingleWrapperDirs(destDir, out);
        out.println("Extraction finished");
        return destDir;
    }

    /*
     * Nektere zipy maji obsah PSP balicku zabaleny v obalove slozce (klidne i vicenasobne).
     * Obsah se vytahne primo do korene rozbaleneho adresare, aby validator nasel PRIMARY-METS.
     */
    private void flattenSingleWrapperDirs(File destDir, PrintStream out) throws IOException {
        while (true) {
            File[] entries = destDir.listFiles();
            if (entries == null || entries.length != 1 || !entries[0].isDirectory()) {
                return;
            }
            File wrapper = entries[0];
            out.println(String.format("Package content wrapped in single directory '%s', moving its content one level up", wrapper.getName()));
            //prejmenovani obalu na docasny nazev kvuli mozne kolizi jmen (obal muze obsahovat stejnojmennou polozku)
            File tmp = new File(destDir, wrapper.getName() + ".unwrapping.tmp");
            if (!wrapper.renameTo(tmp)) {
                throw new IOException("failed to rename " + wrapper.getAbsolutePath() + " to " + tmp.getAbsolutePath());
            }
            File[] children = tmp.listFiles();
            if (children != null) {
                for (File child : children) {
                    File target = new File(destDir, child.getName());
                    if (!child.renameTo(target)) {
                        throw new IOException("failed to move " + child.getAbsolutePath() + " to " + target.getAbsolutePath());
                    }
                }
            }
            if (!tmp.delete()) {
                throw new IOException("failed to delete empty wrapper dir " + tmp.getAbsolutePath());
            }
        }
    }

    private static class ZipHelper {

        public void unzip(File zipFile, File destDir) throws IOException {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zis.getNextEntry();
            if (zipEntry == null) {
                throw new IOException("no zip entries");
            }
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }

        private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
            File destFile = new File(destinationDir, zipEntry.getName());
            String destDirPath = destinationDir.getCanonicalPath();
            String destFilePath = destFile.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
            }
            return destFile;
        }

    }

    public static class ClamAvHelper {

        private static final String CLAMD_HOST = "127.0.0.1";
        private static final int CLAMD_PORT = 3310;

        public void scanDirectory(File dir, PrintStream logOut) throws IOException {
            if (!dir.isDirectory()) {
                throw new IOException("Not a directory: " + dir.getAbsolutePath());
            }
            if (!dir.exists()) {
                throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
            }
            if (!dir.canRead()) {
                throw new IOException("Directory is not readable: " + dir.getAbsolutePath());
            }
            if (!dir.canExecute()) {
                throw new IOException("Directory is not executable: " + dir.getAbsolutePath());
            }

            // Establish a connection to clamd
            try (Socket socket = new Socket(CLAMD_HOST, CLAMD_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                // Send PING command to check if clamd is running
                out.println("PING");
                String response = in.readLine();
                if ("PONG".equals(response)) {
                    logOut.println("ClamAV daemon is up and running!");
                } else {
                    logOut.println("ClamAV daemon is not responding.");
                    return;
                }
            }

            // Start scanning the directory
            logOut.println("Scanning directory: " + dir.getAbsolutePath());
            updateAccessRightsToBeReadableByAll(dir);
            scanFileOrDir(dir, logOut);
        }

        /*
        set access rights, because clamd needs to read the files, but is not running as the same user as the webapp
         */
        private void updateAccessRightsToBeReadableByAll(File fileOrDir) {
            fileOrDir.setReadable(true, false);
            if (fileOrDir.isDirectory()) {
                fileOrDir.setExecutable(true, false);
                File[] files = fileOrDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        updateAccessRightsToBeReadableByAll(file);
                    }
                }
            }
        }

        private void scanFileOrDir(File fileOrDir, PrintStream logOut) throws IOException {
            if (fileOrDir.isDirectory()) {
                //logOut.println("Listing files for AV scan in: " + fileOrDir.getAbsolutePath());
                File[] files = fileOrDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        scanFileOrDir(file, logOut);
                    }
                }
            } else {
                //logOut.println("Scanning file: " + fileOrDir.getAbsolutePath());
                scanFile(fileOrDir, logOut);
            }
        }

        private void scanFile(File file, PrintStream logOut) throws IOException {
            try (Socket socket = new Socket(CLAMD_HOST, CLAMD_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send SCAN command to scan a specific file
                out.println("SCAN " + file.getAbsolutePath());
                String response;
                while ((response = in.readLine()) != null && !response.trim().isEmpty()) {
                    logOut.println(response); // Log each line of response
                    if (response.contains("FOUND")) {
                        throw new IOException("Malware/Virus Detected: " + response);
                    } else if (response.contains("ERROR")) {
                        throw new IOException("Error during scanning: " + response);
                    }
                }
            }
        }
    }
}
