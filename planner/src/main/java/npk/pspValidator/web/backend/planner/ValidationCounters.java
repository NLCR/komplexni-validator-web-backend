package npk.pspValidator.web.backend.planner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class ValidationCounters {
    public List<JSONObject> readyForExtraction = new ArrayList<>();
    public int extractionInProgress = 0;

    public List<JSONObject> readyForExecution = new ArrayList<>();
    public int executionInProgress = 0;

    public List<JSONObject> readyForArchivation = new ArrayList<>();
    public int archivationInProgress = 0;

    public List<JSONObject> readyForDeletion = new ArrayList<>();
    public int deletionInProgress = 0;

    public int deleted = 0;

    public ValidationCounters(JSONArray validations) {
        for (int i = 0; i < validations.length(); i++) {
            JSONObject validation = (JSONObject) validations.get(i);
            String state = (String) validation.get("state");
            switch (state) {
                case "READY_FOR_EXTRACTION":
                    readyForExtraction.add(validation);
                    break;
                case "TO_BE_EXTRACTED":
                case "EXTRACTING":
                    extractionInProgress++;
                    break;
                case "READY_FOR_EXECUTION":
                    readyForExecution.add(validation);
                    break;
                case "TO_BE_EXECUTED":
                case "EXECUTING":
                    executionInProgress++;
                    break;
                case "FINISHED":
                case "ERROR":
                case "CANCELED":
                    readyForArchivation.add(validation);
                    break;
                case "TO_BE_ARCHIVED":
                case "ARCHIVING":
                    archivationInProgress++;
                    break;
                case "ARCHIVED":
                    readyForDeletion.add(validation);
                    break;
                case "T0_BE_DELETED":
                case "DELETING":
                    deletionInProgress++;
                    break;
                case "DELETED":
                    deleted++;
                    break;
            }
        }
    }

    @Override
    public String toString() {
        return "ValidationCounters{" +
                "readyForExtraction=" + readyForExtraction.size() +
                ", extractionInProgress=" + extractionInProgress +
                ", readyForExecution=" + readyForExecution.size() +
                ", executionInProgress=" + executionInProgress +
                ", readyForArchivation=" + readyForArchivation.size() +
                ", archivationInProgress=" + archivationInProgress +
                ", readyForDeletion=" + readyForDeletion.size() +
                ", deletionInProgress=" + deletionInProgress +
                ", deleted=" + deleted +
                '}';
    }
}
