package nkp.pspValidator.web.backend.job_execution_service.jobs;

import nkp.pspValidator.web.backend.utils.Config;

import java.io.*;
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
            File extractionLogFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "extraction.log");
            PrintStream out = null;
            try {
                out = new PrintStream(extractionLogFile);
                out.println("Extraction log for validation " + validationId);
            } catch (IOException e) {
                System.err.println("error initializing log file for extraction job: " + e.getMessage());
                updateValidationState("ERROR");
                return;
            }
            try {
                extractZipFile(validationId, out);
                //TODO: vytahnout z rozbaleneho baliku dalsi data (id baliku, etc) a ulozit do zaznamu Validace
                updateValidationState("READY_FOR_EXECUTION");
                out.close();
            } catch (Throwable e) {
                out.println("error extracting zip file: " + e.getMessage());
                updateValidationState("ERROR");
            }
        }).start();
    }

    private void extractZipFile(String validationId, PrintStream out) throws IOException {
        File workingDir = new File(Config.instanceOf().getValidationWorkingDir(), validationId);
        File zipFile = workingDir.listFiles((dir, name) -> name.endsWith(".zip"))[0];
        String packageName = zipFile.getName().substring(0, zipFile.getName().length() - ".zip".length());
        File destDir = new File(workingDir, packageName);
        out.println(String.format("Extracting: %s -> %s", zipFile.getAbsolutePath(), destDir.getAbsolutePath()));
        new ZipHelper().unzip(zipFile, destDir);
        out.println("Extraction finished");
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
}
