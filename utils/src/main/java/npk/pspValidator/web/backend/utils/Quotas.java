package npk.pspValidator.web.backend.utils;

import org.json.JSONObject;

public class Quotas {

    public int maxParallelJobs;
    public int maxParallelExtractionJobs;
    public int maxParallelValidationJobs;
    public int maxParallelArchivationJobs;
    public int maxParallelDeletionJobs;
    public int timeToArchiveValidationH;
    public int timeToDeleteValidationH;

    public Quotas(JSONObject json) {
        this.maxParallelJobs = json.getInt("maxParallelJobs");
        this.maxParallelExtractionJobs = json.getInt("maxParallelExtractionJobs");
        this.maxParallelValidationJobs = json.getInt("maxParallelValidationJobs");
        this.maxParallelArchivationJobs = json.getInt("maxParallelArchivationJobs");
        this.maxParallelDeletionJobs = json.getInt("maxParallelDeletionJobs");
        this.timeToArchiveValidationH = json.getInt("timeToArchiveValidationH");
        this.timeToDeleteValidationH = json.getInt("timeToDeleteValidationH");
    }
}
