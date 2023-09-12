package nkp.pspValidator.web.backend.planner;

import nkp.pspValidator.web.backend.utils.dao.Quotas;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public ValidationCounters(JSONArray validations, Quotas quotas) {
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
                    if (isTimeForAction(LocalDateTime.parse(validation.getString("tsEnded")), quotas.timeToArchiveValidationH)) {
                        readyForArchivation.add(validation);
                        //System.out.println("time to archive FINISHED validation");
                    } else {
                        //System.out.println("not time to archive FINISHED validation yet");
                    }
                    break;
                case "ERROR":
                case "CANCELED": {
                    LocalDateTime ended = null;
                    if (validation.has("tsEnded")) {
                        ended = LocalDateTime.parse(validation.getString("tsEnded"));
                    } //mozna jine timepstampy?
                    if (ended != null) {
                        if (isTimeForAction(ended, quotas.timeToArchiveValidationH)) {
                            readyForArchivation.add(validation);
                            //System.out.println("time to archive ERROR validation");
                        } else {
                            //System.out.println("not time to archive ERROR validation yet");
                        }
                    } else {
                        System.out.println("no final timestamp for ERROR found for validation: " + validation.getString("id"));
                    }
                }
                break;
                case "TO_BE_ARCHIVED":
                case "ARCHIVING":
                    archivationInProgress++;
                    break;
                case "ARCHIVED": {
                    LocalDateTime ended = null;
                    if (validation.has("tsEnded")) {
                        ended = LocalDateTime.parse(validation.getString("tsEnded"));
                    } //mozna jine timepstampy?
                    if (ended != null) {
                        if (isTimeForAction(ended, quotas.timeToArchiveValidationH + quotas.timeToDeleteValidationH)) {
                            readyForDeletion.add(validation);
                            //System.out.println("time to delete archived validation");
                        } else {
                            //System.out.println("not time to delete archived validation yet");
                        }
                    } else {
                        System.out.println("no final timestamp for ERROR found for validation: " + validation.getString("id"));
                    }
                }
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
        this.readyForExtraction = sort(this.readyForExtraction);
        this.readyForExecution = sort(this.readyForExecution);
        this.readyForDeletion = sort(this.readyForDeletion);
        this.readyForArchivation = sort(this.readyForArchivation);
    }

    private List<JSONObject> sort(List<JSONObject> validations) {
        return validations.stream().sorted((o1, o2) -> {
            int p1 = o1.getInt("priority");
            int p2 = o2.getInt("priority");
            if (p1 < p2) {
                return -1;
            } else if (p1 > p2) {
                return 1;
            } else {
                LocalDateTime ts1 = LocalDateTime.parse(o1.getString("tsCreated"));
                LocalDateTime ts2 = LocalDateTime.parse(o2.getString("tsCreated"));
                return ts1.compareTo(ts2);
            }
        }).collect(Collectors.toList());
    }

    private boolean isTimeForAction(LocalDateTime dateTime, int minHoursSince) {
        LocalDateTime now = LocalDateTime.now();
        long hours = Duration.between(dateTime, now).toHours();
        //System.out.println("hourse since: " + hours);
        return hours > minHoursSince;
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
