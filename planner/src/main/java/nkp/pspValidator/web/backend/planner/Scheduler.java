package nkp.pspValidator.web.backend.planner;

import nkp.pspValidator.web.backend.utils.apiClient.ApiClientException;
import nkp.pspValidator.web.backend.utils.apiClient.JobExecutionServiceApi;
import nkp.pspValidator.web.backend.utils.apiClient.QuotaServiceApi;
import nkp.pspValidator.web.backend.utils.apiClient.ValidationManagerServiceApi;
import nkp.pspValidator.web.backend.utils.dao.Quotas;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Scheduler {

    private QuotaServiceApi quotaServiceApi = new QuotaServiceApi();
    private ValidationManagerServiceApi validationManagerServiceApi = new ValidationManagerServiceApi();

    private JobExecutionServiceApi jobExecutionServiceApi = new JobExecutionServiceApi();

    StringBuilder builder;

    public String run() {
        clearLog();
        //log("Scheduler STARTED");
        try {

            //log("Fetching quotas ...");
            Quotas quotas = quotaServiceApi.getQuotas();
            //log("Quotas collected");

            //log("Fetching validations ...");
            JSONArray validationsAll = validationManagerServiceApi.getValidations();
            //log("Collected %d validations", validationsAll.length());
            ValidationCounters counters = new ValidationCounters(validationsAll, quotas);
            //System.out.println(counters);

            int activeJobs = counters.extractionInProgress + counters.executionInProgress + counters.archivationInProgress + counters.deletionInProgress;
            //log("Active jobs: %d", activeJobs);
            if (activeJobs < quotas.maxParallelJobs) {
                //log("Checking new jobs to be scheduled, %d slots available", quotas.maxParallelJobs - activeJobs);
            } else {
                //log("No slots for new jobs available");
            }

            if (activeJobs < quotas.maxParallelJobs) {
                if (counters.readyForDeletion.size() > 0 && counters.deletionInProgress < quotas.maxParallelDeletionJobs) {
                    int slots = Math.min(quotas.maxParallelJobs - activeJobs, quotas.maxParallelDeletionJobs);
                    List<JSONObject> validations = selectValidationsForDeletion(counters.readyForDeletion, slots);
                    for (JSONObject validation : validations) {
                        String validationId = validation.getString("id");
                        validationManagerServiceApi.updateValidationState(validationId, "T0_BE_DELETED");
                        log("scheduling DELETION job for " + validationId);
                        jobExecutionServiceApi.scheduleJob("deletion", validationId);
                        activeJobs++;
                    }
                }
            }

            if (activeJobs < quotas.maxParallelJobs) {
                if (counters.readyForArchivation.size() > 0 && counters.archivationInProgress < quotas.maxParallelArchivationJobs) {
                    int slots = Math.min(quotas.maxParallelJobs - activeJobs, quotas.maxParallelArchivationJobs);
                    //System.out.println("ready for archivation: " + counters.readyForArchivation.size());
                    List<JSONObject> validations = selectValidationsForArchivation(counters.readyForArchivation, slots);
                    for (JSONObject validation : validations) {
                        String validationId = validation.getString("id");
                        validationManagerServiceApi.updateValidationState(validationId, "TO_BE_ARCHIVED");
                        log("scheduling ARCHIVATION job for " + validationId);
                        jobExecutionServiceApi.scheduleJob("archivation", validationId);
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
                        validationManagerServiceApi.updateValidationState(validationId, "TO_BE_EXECUTED");
                        log("scheduling EXECUTION job for " + validationId);
                        jobExecutionServiceApi.scheduleJob("execution", validationId);
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
                        validationManagerServiceApi.updateValidationState(validationId, "TO_BE_EXTRACTED");
                        log("scheduling EXTRACTION job for " + validationId);
                        jobExecutionServiceApi.scheduleJob("extraction", validationId);
                        activeJobs++;
                    }
                }
            }
        } catch (ApiClientException e) {
            e.printStackTrace();
        }
        //log("Scheduler FINISHED");
        return getLog();
    }

    private List<JSONObject> selectValidationsForExtraction(List<JSONObject> readyForExtraction, int slots) {
        return readyForExtraction.subList(0, Math.min(slots, readyForExtraction.size()));
    }

    private List<JSONObject> selectValidationsForExecution(List<JSONObject> readyForExecution, int slots) {
        return readyForExecution.subList(0, Math.min(slots, readyForExecution.size()));
    }

    private List<JSONObject> selectValidationsForArchivation(List<JSONObject> readyForArchivation, int slots) {
        return readyForArchivation.subList(0, Math.min(slots, readyForArchivation.size()));
    }

    private List<JSONObject> selectValidationsForDeletion(List<JSONObject> readyForDeletion, int slots) {
        return readyForDeletion.subList(0, Math.min(slots, readyForDeletion.size()));
    }

    private void clearLog() {
        builder = new StringBuilder();
    }

    private void log(String message) {
        //System.out.println(message);
        builder.append(message).append('\n');
    }

    private void log(String template, Object... params) {
        //System.out.println(String.format(template, params));
        builder.append(String.format(template, params)).append('\n');
    }

    private String getLog() {
        return builder.toString();
    }
}
