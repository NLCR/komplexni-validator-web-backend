package nkp.pspValidator.web.backend.job_execution_service.jobs;

import nkp.pspValidator.web.backend.utils.Config;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ExecutionJob extends Job {

    private final JSONObject validation;

    public ExecutionJob(String validationId, JSONObject validation) {
        super(validationId);
        this.validation = validation;
    }

    @Override
    public void run() {
        new Thread(() -> {
            updateValidationState("EXECUTING");
            File executionLogFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "execution.log");
            PrintStream out = null;
            try {
                out = new PrintStream(executionLogFile);
                out.println("Execution log for validation " + validationId);
            } catch (IOException e) {
                System.err.println("error initializing log file for execution job: " + e.getMessage());
                updateValidationState("ERROR");
                return;
            }
            try {
                int returnCode = executeValidation(validationId, out);
                if (returnCode == 0) {
                    updateValidationState("FINISHED");
                } else {
                    updateValidationState("ERROR");
                }
                out.close();
            } catch (Throwable e) {
                out.println("error executing validation: " + e.getMessage());
                updateValidationState("ERROR");
            }
        }).start();
    }

    private int executeValidation(String validationId, PrintStream out) {
        //System.out.println("validating  " + validationId);
        File workingDir = new File(Config.instanceOf().getValidationWorkingDir(), validationId);
        try {
            File pspDir = workingDir.listFiles(pathname -> pathname.isDirectory())[0];

            File logFileTxt = new File(workingDir, "validation-log.txt");
            File logFileXml = new File(workingDir, "validation-log.xml");

            List<String> dmfVersionParams = extractDmfVersionParams();

            List<String> command = new ArrayList<>();
            command.add(Config.instanceOf().getJobExecutionServiceValidatorJavaHome() + "/bin/java");
            command.add("-jar");
            command.add(Config.instanceOf().getJobExecutionServiceValidatorJar());

            command.add("--action");
            command.add("VALIDATE_PSP");

            command.add("--psp");
            command.add(pspDir.getAbsolutePath());

            command.add("--config-dir");
            command.add(Config.instanceOf().getJobExecutionServiceValidatorConfigDir());

            command.add("--xml-protocol-file");
            command.add(logFileXml.getAbsolutePath());

            command.add("--disable-imagemagick");
            command.add("--disable-jhove");
            command.add("--disable-jpylyzer");
            command.add("--disable-kakadu");
            command.add("--disable-mp3val");
            command.add("--disable-shntool");
            command.add("--disable-checkmate");

            command.addAll(dmfVersionParams);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectOutput(logFileTxt);
            processBuilder.redirectError(logFileTxt);
            Process process = processBuilder.start();
            return process.waitFor(); // Wait for the process to complete
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private List<String> extractDmfVersionParams() {
        List<String> result = new ArrayList<>();
        if (validation.has("dmfType")) {
            String dmfType = validation.getString("dmfType");
            if (validation.has("preferredDmfVersion")) {
                result.add("--preferred-dmf-" + dmfType + "-version");
                result.add(validation.getString("preferredDmfVersion"));
            }
            if (validation.has("forcedDmfVersion")) {
                result.add("--forced-dmf-" + dmfType + "-version");
                result.add(validation.getString("forcedDmfVersion"));
            }
        }
        return result;
    }

}
