package nkp.pspValidator.web.backend.utils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {

    public static final String SYSTEM_USER_ID = "service@komplexni-validator";

    private static Config instance;

    private final long startTime = System.currentTimeMillis();

    public String getUptimeFormatted() {
        long ms = System.currentTimeMillis() - startTime;
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;
        return String.format("%02d:%02d:%02d", h, m % 60, s % 60);
    }

    public static void init() throws IOException {
        if (instance == null) {
            Logger.getLogger(Config.class.getName()).info("initializing ");
            File propertiesFile = new File(System.getProperty("user.home") + File.separator + ".komplexni-validator" + File.separator + "/config.properties");
            instance = new Config(propertiesFile);
            //System.out.println(instance);
        }
    }

    public static Config instanceOf() {
        return instance;
    }

    private final String validationMgrServiceUrl;
    private final String validationMgrServiceDbUrl;
    private final String validationMgrServiceDbLogin;
    private final String validationMgrServiceDbPassword;

    private final String quotaServiceUrl;
    private final String quotaServiceDbUrl;
    private final String quotaServiceDbLogin;
    private final String quotaServiceDbPassword;

    private final String userServiceUrl;
    private final String userServiceDbUrl;
    private final String userServiceDbLogin;
    private final String userServiceDbPassword;

    private final String uploadServiceUrl;

    private final String jobExecutionServiceUrl;
    private final String jobExecutionServiceValidatorJar;
    private final String jobExecutionServiceValidatorConfigDir;

    private final String resultServiceUrl;

    private final String notificationServiceUrl;
    private final String notificationServiceSenderEmail;
    private final String notificationServiceFrontendBaseUrl;
    private final String notificationServicePostmarkServerToken;
    private final boolean notificationServicePostmarkEnabled;

    private final String jwtLocalPrivateKey;
    private final String jwtLocalPublicKey;
    private final String jwGooglePublicKeysFile;

    private final String oauthGoogleClientId;

    private final File validationWorkingDir;


    public Config(File propertiesFile) throws IOException {
        InputStream input = new FileInputStream(propertiesFile);
        Properties prop = new Properties();
        prop.load(input);
        this.validationMgrServiceUrl = prop.getProperty("validation-manager-service.url");
        this.validationMgrServiceDbUrl = prop.getProperty("validation-manager-service.db.url");
        this.validationMgrServiceDbLogin = prop.getProperty("validation-manager-service.db.login");
        this.validationMgrServiceDbPassword = prop.getProperty("validation-manager-service.db.password");

        this.quotaServiceUrl = prop.getProperty("quota-service.url");
        this.quotaServiceDbUrl = prop.getProperty("quota-service.db.url");
        this.quotaServiceDbLogin = prop.getProperty("quota-service.db.login");
        this.quotaServiceDbPassword = prop.getProperty("quota-service.db.password");

        this.userServiceUrl = prop.getProperty("user-service.url");
        this.userServiceDbUrl = prop.getProperty("user-service.db.url");
        this.userServiceDbLogin = prop.getProperty("user-service.db.login");
        this.userServiceDbPassword = prop.getProperty("user-service.db.password");

        this.uploadServiceUrl = prop.getProperty("upload-service.url");
        this.jobExecutionServiceUrl = prop.getProperty("job-execution-service.url");
        this.jobExecutionServiceValidatorJar = prop.getProperty("job-execution-service.validator.jar");
        this.jobExecutionServiceValidatorConfigDir = prop.getProperty("job-execution-service.validator.configDir");

        this.resultServiceUrl = prop.getProperty("result-service.url");
        this.notificationServiceUrl = prop.getProperty("notification-service.url");
        this.notificationServiceSenderEmail = prop.getProperty("notification-service.senderEmail");
        this.notificationServiceFrontendBaseUrl = prop.getProperty("notification-service.frontendBaseUrl");
        this.notificationServicePostmarkServerToken = prop.getProperty("notification-service.postmark.serverToken");
        this.notificationServicePostmarkEnabled = Boolean.valueOf(prop.getProperty("notification-service.postmark.enabled"));

        this.jwtLocalPrivateKey = prop.getProperty("jwt.local.private-key");
        this.jwtLocalPublicKey = prop.getProperty("jwt.local.public-key");
        this.jwGooglePublicKeysFile = prop.getProperty("jwt.google.public-keys-file");
        this.oauthGoogleClientId = prop.getProperty("oauth.google.client-id");

        this.validationWorkingDir = new File(prop.getProperty("validation-working-dir"));
        checkDir(this.validationWorkingDir);
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

    public String getQuotaServiceUrl() {
        return quotaServiceUrl;
    }

    public String getQuotaServiceDbUrl() {
        return quotaServiceDbUrl;
    }

    public String getQuotaServiceDbLogin() {
        return quotaServiceDbLogin;
    }

    public String getQuotaServiceDbPassword() {
        return quotaServiceDbPassword;
    }

    public String getUserServiceUrl() {
        return userServiceUrl;
    }

    public String getUserServiceDbUrl() {
        return userServiceDbUrl;
    }

    public String getUserServiceDbLogin() {
        return userServiceDbLogin;
    }

    public String getUserServiceDbPassword() {
        return userServiceDbPassword;
    }

    public String getUploadServiceUrl() {
        return uploadServiceUrl;
    }

    public String getJobExecutionServiceUrl() {
        return jobExecutionServiceUrl;
    }


    public String getJobExecutionServiceValidatorJar() {
        return jobExecutionServiceValidatorJar;
    }

    public String getJobExecutionServiceValidatorConfigDir() {
        return jobExecutionServiceValidatorConfigDir;
    }

    public String getResultServiceUrl() {
        return resultServiceUrl;
    }

    public String getNotificationServiceUrl() {
        return notificationServiceUrl;
    }

    public String getNotificationServiceSenderEmail() {
        return notificationServiceSenderEmail;
    }

    public String getNotificationServiceFrontendBaseUrl() {
        return notificationServiceFrontendBaseUrl;
    }

    public String getNotificationServicePostmarkServerToken() {
        return notificationServicePostmarkServerToken;
    }

    public boolean isNotificationServicePostmarkEnabled() {
        return notificationServicePostmarkEnabled;
    }

    public String getJwtLocalPrivateKey() {
        return jwtLocalPrivateKey;
    }

    public String getJwtLocalPublicKey() {
        return jwtLocalPublicKey;
    }

    public String getJwGooglePublicKeysFile() {
        return jwGooglePublicKeysFile;
    }

    public String getOauthGoogleClientId() {
        return oauthGoogleClientId;
    }

    public File getValidationWorkingDir() {
        return validationWorkingDir;
    }


    @Override
    public String toString() {
        return "Config: {\n" +
                "validationMgrServiceUrl='" + validationMgrServiceUrl + '\'' +
                ",\nvalidationMgrServiceDbUrl='" + validationMgrServiceDbUrl + '\'' +
                ",\nvalidationMgrServiceDbLogin='" + validationMgrServiceDbLogin + '\'' +
                ",\nvalidationMgrServiceDbPassword='" + validationMgrServiceDbPassword + '\'' +
                ",\nquotaServiceUrl='" + quotaServiceUrl + '\'' +
                ",\nquotaServiceDbUrl='" + quotaServiceDbUrl + '\'' +
                ",\nquotaServiceDbLogin='" + quotaServiceDbLogin + '\'' +
                ",\nquotaServiceDbPassword='" + quotaServiceDbPassword + '\'' +
                ",\nuserServiceUrl='" + userServiceUrl + '\'' +
                ",\nuserServiceDbUrl='" + userServiceDbUrl + '\'' +
                ",\nuserServiceDbLogin='" + userServiceDbLogin + '\'' +
                ",\nuserServiceDbPassword='" + userServiceDbPassword + '\'' +
                ",\nuploadServiceUrl='" + uploadServiceUrl + '\'' +
                ",\njobExecutionServiceUrl='" + jobExecutionServiceUrl + '\'' +
                ",\nresultServiceUrl='" + resultServiceUrl + '\'' +
                ",\nnotificationServiceUrl='" + notificationServiceUrl + '\'' +
                ",\nnotificationServiceSenderEmail='" + notificationServiceSenderEmail + '\'' +
                ",\nnotificationServiceFrontendBaseUrl='" + notificationServiceFrontendBaseUrl + '\'' +
                ",\nnotificationServicePostmarkServerToken='" + notificationServicePostmarkServerToken + '\'' +
                ",\nvalidationWorkingDir=" + validationWorkingDir +
                ",\noauthGoogleClientId='" + oauthGoogleClientId + '\'' +
                "\n}";
    }

    public JSONObject getServicesUrls() {
        JSONObject services = new JSONObject();
        services.put("validation-manager-service", getValidationMgrServiceUrl());
        services.put("quota-service", getQuotaServiceUrl());
        services.put("user-service", getUserServiceUrl());
        services.put("upload-service", getUploadServiceUrl());
        services.put("job-execution-service", getJobExecutionServiceUrl());
        services.put("result-service", getResultServiceUrl());
        services.put("notification-service", getNotificationServiceUrl());
        return services;
    }
}

