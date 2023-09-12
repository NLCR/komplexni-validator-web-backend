package nkp.pspValidator.web.backend.job_execution_service.jobs;

import nkp.pspValidator.web.backend.utils.Config;

import java.io.File;

public class DeletionJob extends Job {

    public DeletionJob(String validationId) {
        super(validationId);
    }

    @Override
    public void run() {
        new Thread(() -> {
            updateValidationState("DELETING");
            try {
                File validationDir = new File(Config.instanceOf().getValidationWorkingDir(), validationId);
                deleteDirectory(validationDir);
                //deleteValidationFromDb();
                updateValidationState("DELETED");
            } catch (Throwable e) {
                updateValidationState("ERROR");
            }
        }).start();
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    System.out.println("deleting " + file.getAbsolutePath());
                    file.delete();
                }
            }
        }
        System.out.println("deleting " + directory.getAbsolutePath());
        directory.delete();
    }
}
