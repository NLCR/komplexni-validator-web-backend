package npk.pspValidator.web.backend.planner;

import npk.pspValidator.web.backend.utils.Config;
import npk.pspValidator.web.backend.utils.HttpHelper;
import npk.pspValidator.web.backend.utils.Quotas;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Planner {

    StringBuilder builder;

    public String run() {
        clearLog();
        log("Planner STARTED");

        log("Fetching quotas ...");
        Quotas quotas = getQuotas();
        log("Quotas collected");

        log("Fetching validations ...");
        JSONArray validationsAll = getValidations();
        log("Collected %d validations", validationsAll.length());
        ValidationCounters counters = new ValidationCounters(validationsAll);
        //System.out.println(counters);

        int activeJobs = counters.extractionInProgress + counters.executionInProgress + counters.archivationInProgress + counters.deletionInProgress;
        if (activeJobs < quotas.maxParallelJobs) {
            log("Checking new jobs to be scheduled ...");
        } else {
            log("No slots for new jobs available");
        }

        //schedule DELETION jobs
        if (activeJobs < quotas.maxParallelJobs) {
            if (counters.readyForDeletion.size() > 0 && counters.deletionInProgress < quotas.maxParallelDeletionJobs) {
                int slots = Math.min(quotas.maxParallelJobs - activeJobs, quotas.maxParallelDeletionJobs);
                List<JSONObject> validations = selectValidationsForDeletion(counters.readyForDeletion, slots);
                for (JSONObject validation : validations) {
                    //TODO: schedule DELETION job
                    log("TODO: schedule DELETION job for " + validation.toString());
                    activeJobs++;
                }
            }
        }

        //schedule ARCHIVATION jobs
        if (activeJobs < quotas.maxParallelJobs) {
            if (counters.readyForArchivation.size() > 0 && counters.archivationInProgress < quotas.maxParallelArchivationJobs) {
                int slots = Math.min(quotas.maxParallelJobs - activeJobs, quotas.maxParallelArchivationJobs);
                List<JSONObject> validations = selectValidationsForArchivation(counters.readyForArchivation, slots);
                for (JSONObject validation : validations) {
                    //TODO: schedule ARCHIVATION job
                    log("TODO: schedule ARCHIVATION job for " + validation.toString());
                    activeJobs++;
                }
            }
        }

        //schedule EXECUTION jobs
        if (activeJobs < quotas.maxParallelJobs) {
            if (counters.readyForExecution.size() > 0 && counters.executionInProgress < quotas.maxParallelValidationJobs) {
                int slots = Math.min(quotas.maxParallelJobs - activeJobs, quotas.maxParallelValidationJobs);
                List<JSONObject> validations = selectValidationsForExecution(counters.readyForExecution, slots);
                for (JSONObject validation : validations) {
                    String validationId = validation.getString("id");
                    updateValidationState(validationId, "TO_BE_EXECUTED");
                    log("scheduling EXECUTION job for " + validationId);
                    scheduleJob("execution", validationId);
                    activeJobs++;
                }
            }
        }

        //schedule EXTRACTION jobs
        if (activeJobs < quotas.maxParallelJobs) {
            if (counters.readyForExtraction.size() > 0 && counters.executionInProgress < quotas.maxParallelExtractionJobs) {
                int slots = Math.min(quotas.maxParallelJobs - activeJobs, quotas.maxParallelExtractionJobs);
                List<JSONObject> validations = selectValidationsForExtraction(counters.readyForExtraction, slots);
                for (JSONObject validation : validations) {
                    String validationId = validation.getString("id");
                    updateValidationState(validationId, "TO_BE_EXTRACTED");
                    log("scheduling EXTRACTION job for " + validationId);
                    scheduleJob("extraction", validationId);
                    activeJobs++;
                }
            }
        }

        log("Planner FINISHED");
        return getLog();
    }

    private List<JSONObject> selectValidationsForExtraction(List<JSONObject> readyForExtraction, int slots) {
        //TODO: sort by priority, date
        return readyForExtraction.subList(0, Math.min(slots, readyForExtraction.size()));
    }

    private List<JSONObject> selectValidationsForExecution(List<JSONObject> readyForExecution, int slots) {
        //TODO: sort by priority, package size, date
        return readyForExecution.subList(0, Math.min(slots, readyForExecution.size()));
    }

    private List<JSONObject> selectValidationsForArchivation(List<JSONObject> readyForArchivation, int slots) {
        //TODO: sort by date
        return readyForArchivation.subList(0, Math.min(slots, readyForArchivation.size()));
    }

    private List<JSONObject> selectValidationsForDeletion(List<JSONObject> readyForDeletion, int slots) {
        //TODO: sort by date
        return readyForDeletion.subList(0, Math.min(slots, readyForDeletion.size()));
    }


    private void clearLog() {
        builder = new StringBuilder();
    }

    private void log(String message) {
        builder.append(message).append('\n');
    }

    private void log(String template, Object... params) {
        builder.append(String.format(template, params)).append('\n');
    }

    private String getLog() {
        return builder.toString();
    }

    private JSONArray getValidations() {
        try {
            String url = Config.instanceOf().getValidationMgrServiceUrl();
            HttpHelper.Response response = HttpHelper.sendGetReturningJsonArray(url);
            if (!response.isOk()) {
                log("error: " + response.errorMessage);
                return null;
            }
            return (JSONArray) response.result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Quotas getQuotas() {
        try {
            String url = Config.instanceOf().getQuotasServiceUrl();
            HttpHelper.Response response = HttpHelper.sendGetReturningJsonObject(url);
            if (!response.isOk()) {
                log("error: " + response.errorMessage);
                return null;
            }
            JSONObject json = (JSONObject) response.result;
            return new Quotas(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void scheduleJob(String jobType, String validationId) {
        try {
            JSONObject json = new JSONObject();
            json.put("jobType", jobType);
            json.put("validationId", validationId);
            String url = Config.instanceOf().getJobsExecutionService();
            HttpHelper.Response response = HttpHelper.sendPostReturningJsonObject(url, json.toString());
            if (!response.isOk()) {
                throw new RuntimeException(String.format("error scheduling job %s for %s: %s: %s", jobType, validationId, response.responseCode, response.errorMessage));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateValidationState(String validationId, String state) {
        try {
            JSONObject body = new JSONObject();
            body.put("validationId", validationId);
            String url = String.format("%s/%s/state", Config.instanceOf().getValidationMgrServiceUrl(), validationId);
            System.out.println(url);
            HttpHelper.Response response = HttpHelper.sendPutReturningNothing(url, state);
            if (!response.isOk()) {
                throw new RuntimeException(String.format("error updating state for validation %s to %s: %s: %s", validationId, state, response.responseCode, response.errorMessage));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
