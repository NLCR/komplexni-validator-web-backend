package npk.pspValidator.web.backend.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static Config instance;

    public static void init() throws IOException {
        File propertiesFile = new File(System.getProperty("user.home") + File.separator + ".komplexni-validator" + File.separator + "/config.properties");
        instance = new Config(propertiesFile);
    }

    public static Config instanceOf() {
        return instance;
    }

    private final String validationMgrServiceUrl;
    private final String validationMgrServiceDbUrl;
    private final String validationMgrServiceDbLogin;
    private final String validationMgrServiceDbPassword;

    private final String quotasServiceUrl;
    private final String quotasServiceDbUrl;
    private final String quotasServiceDbLogin;
    private final String quotasServiceDbPassword;

    private final String usersServiceUrl;
    private final String usersServiceDbUrl;
    private final String usersServiceDbLogin;
    private final String usersServiceDbPassword;

    private final String uploadsServiceUrl;

    private final String jobsExecutionService;

    private final String resultsService;

    private final File validationsWorkingDir;

    private final String oauthGoogleClientId;

    public Config(File propertiesFile) throws IOException {
        InputStream input = new FileInputStream(propertiesFile);
        Properties prop = new Properties();
        prop.load(input);
        this.validationMgrServiceUrl = prop.getProperty("validation-manager-service.url");
        this.validationMgrServiceDbUrl = prop.getProperty("validation-manager-service.db.url");
        this.validationMgrServiceDbLogin = prop.getProperty("validation-manager-service.db.login");
        this.validationMgrServiceDbPassword = prop.getProperty("validation-manager-service.db.password");

        this.quotasServiceUrl = prop.getProperty("quotas-service.url");
        this.quotasServiceDbUrl = prop.getProperty("quotas-service.db.url");
        this.quotasServiceDbLogin = prop.getProperty("quotas-service.db.login");
        this.quotasServiceDbPassword = prop.getProperty("quotas-service.db.password");

        this.usersServiceUrl = prop.getProperty("users-service.url");
        this.usersServiceDbUrl = prop.getProperty("users-service.db.url");
        this.usersServiceDbLogin = prop.getProperty("users-service.db.login");
        this.usersServiceDbPassword = prop.getProperty("users-service.db.password");

        this.uploadsServiceUrl = prop.getProperty("uploads-service.url");
        this.jobsExecutionService = prop.getProperty("jobs-execution-service.url");
        this.resultsService = prop.getProperty("results-service.url");

        this.validationsWorkingDir = new File(prop.getProperty("validations-working-dir"));
        checkDir(this.validationsWorkingDir);
        this.oauthGoogleClientId = prop.getProperty("oauth.google.client-id");
    }

    private void checkDir(File dir) throws IOException {
        if (!dir.exists()) {
            throw new IOException("Directory doesn't exist: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir.getAbsolutePath());
        }
        if (!dir.canRead()) {
            throw new IOException("Cannot read directory: " + dir.getAbsolutePath());
        }
        if (!dir.canWrite()) {
            throw new IOException("Cannot write directory: " + dir.getAbsolutePath());
        }
    }

    public String getValidationMgrServiceUrl() {
        return validationMgrServiceUrl;
    }

    public String getValidationMgrServiceDbUrl() {
        return validationMgrServiceDbUrl;
    }

    public String getValidationMgrServiceDbLogin() {
        return validationMgrServiceDbLogin;
    }

    public String getValidationMgrServiceDbPassword() {
        return validationMgrServiceDbPassword;
    }

    public String getQuotasServiceUrl() {
        return quotasServiceUrl;
    }

    public String getQuotasServiceDbUrl() {
        return quotasServiceDbUrl;
    }

    public String getQuotasServiceDbLogin() {
        return quotasServiceDbLogin;
    }

    public String getQuotasServiceDbPassword() {
        return quotasServiceDbPassword;
    }

    public String getUsersServiceUrl() {
        return usersServiceUrl;
    }

    public String getUsersServiceDbUrl() {
        return usersServiceDbUrl;
    }

    public String getUsersServiceDbLogin() {
        return usersServiceDbLogin;
    }

    public String getUsersServiceDbPassword() {
        return usersServiceDbPassword;
    }

    public String getUploadsServiceUrl() {
        return uploadsServiceUrl;
    }

    public String getJobsExecutionService() {
        return jobsExecutionService;
    }

    public String getResultsService() {
        return resultsService;
    }

    public File getValidationsWorkingDir() {
        return validationsWorkingDir;
    }

    public String getOauthGoogleClientId() {
        return oauthGoogleClientId;
    }
}

