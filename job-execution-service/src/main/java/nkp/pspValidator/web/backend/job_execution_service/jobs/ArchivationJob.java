package nkp.pspValidator.web.backend.job_execution_service.jobs;

import nkp.pspValidator.web.backend.utils.Config;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

public class ArchivationJob extends Job {

    public ArchivationJob(String validationId) {
        super(validationId);
    }

    @Override
    public void run() {
        new Thread(() -> {
            updateValidationState("ARCHIVING");
            File executionLogFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "archivation.log");
            PrintStream out = null;
            try {
                out = new PrintStream(executionLogFile);
                out.println("Archivation log for validation " + validationId);
            } catch (IOException e) {
                System.err.println("error initializing log file for archivation job: " + e.getMessage());
                updateValidationState("ERROR");
                return;
            }
            try {
                archive(validationId, out);
                updateValidationState("ARCHIVED");
                out.close();
            } catch (Throwable e) {
                out.println("error archiving: " + e.getMessage());
                updateValidationState("ERROR");
            }
        }).start();
    }

    private void archive(String validationId, PrintStream out) {
        File workingDir = new File(Config.instanceOf().getValidationWorkingDir(), validationId);
        //delete zip files
        Arrays.stream(workingDir.listFiles(pathname -> pathname.getName().endsWith(".zip"))).forEach(file -> {
            out.println("deleting zip file: " + file.getAbsolutePath());
            file.delete();
        });
        //delete folders
        Arrays.stream(workingDir.listFiles(pathname -> pathname.isDirectory())).forEach(file -> {
            out.println("deleting folder: " + file.getAbsolutePath());
            deleteDirectory(file, out);
        });
    }

    private void deleteDirectory(File directory, PrintStream out) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file, out);
                } else {
                    out.println("deleting " + file.getAbsolutePath());
                    file.delete();
                }
            }
        }
        out.println("deleting " + directory.getAbsolutePath());
        directory.delete();
    }

}
