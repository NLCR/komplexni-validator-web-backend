package nkp.pspValidator.web.backend.utils.dao;

import org.json.JSONObject;

public class Quotas {

    //jobs
    public int maxParallelJobs;
    public int maxParallelExtractionJobs;
    public int maxParallelValidationJobs;
    public int maxParallelArchivationJobs;
    public int maxParallelDeletionJobs;

    //storage time
    public int timeToArchiveValidationH;
    public int timeToDeleteValidationH;

    //size
    public int maxUploadSizeMB;

    //number of jobs for user categories
    public int userVerifiedMaxActiveJobs;
    public int userVerifiedMaxInactiveJobs;
    public int userUnverifiedMaxActiveJobs;
    public int userUnverifiedMaxInactiveJobs;

    public Quotas(JSONObject json) {
        this.maxParallelJobs = json.getInt("maxParallelJobs");
        this.maxParallelExtractionJobs = json.getInt("maxParallelExtractionJobs");
        this.maxParallelValidationJobs = json.getInt("maxParallelValidationJobs");
        this.maxParallelArchivationJobs = json.getInt("maxParallelArchivationJobs");
        this.maxParallelDeletionJobs = json.getInt("maxParallelDeletionJobs");
        this.timeToArchiveValidationH = json.getInt("timeToArchiveValidationH");
        this.timeToDeleteValidationH = json.getInt("timeToDeleteValidationH");
        this.maxUploadSizeMB = json.getInt("maxUploadSizeMB");
        this.userVerifiedMaxActiveJobs = json.getInt("userVerifiedMaxActiveJobs");
        this.userVerifiedMaxInactiveJobs = json.getInt("userVerifiedMaxInactiveJobs");
        this.userUnverifiedMaxActiveJobs = json.getInt("userUnverifiedMaxActiveJobs");
        this.userUnverifiedMaxInactiveJobs = json.getInt("userUnverifiedMaxInactiveJobs");
    }
}
