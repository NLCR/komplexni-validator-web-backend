package nkp.pspValidator.web.backend.job_execution_service.jobs;

import nkp.pspValidator.web.backend.utils.apiClient.ApiClientException;
import nkp.pspValidator.web.backend.utils.apiClient.NotificationServiceApi;
import nkp.pspValidator.web.backend.utils.apiClient.ValidationManagerServiceApi;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Job {

    final String validationId;

    private final ValidationManagerServiceApi validationManagerServiceApi = new ValidationManagerServiceApi();
    private final NotificationServiceApi notificationServiceApi = new NotificationServiceApi();

    Job(String validationId) {
        this.validationId = validationId;
    }

    public abstract void run();

    void updateValidationState(String status) {
        try {
            //System.out.println("updating validation state to " + status);
            validationManagerServiceApi.updateValidationState(this.validationId, status);
            JSONObject validation = validationManagerServiceApi.getValidation(this.validationId);
            //System.out.println(validation.toString(2));
            String packageName = validation.getString("packageName");
            String recipient = validation.getString("ownerId");

            Long durationS = null;
            if (validation.has("tsStarted") && validation.has("tsEnded")) {
                LocalDateTime started = LocalDateTime.parse(validation.getString("tsStarted"));
                LocalDateTime ended = LocalDateTime.parse(validation.getString("tsEnded"));
                durationS = Duration.between(started, ended).getSeconds();
            }
            switch (status) {
                case "FINISHED":
                case "ERROR":
                    notificationServiceApi.notifyValidationFinished(this.validationId, recipient, packageName, status, durationS);
                    break;
                case "ARCHIVED":
                    notificationServiceApi.notifyValidationArchived(this.validationId, recipient, packageName);
                    break;
                case "DELETED":
                    notificationServiceApi.notifyValidationDeleted(this.validationId, recipient, packageName);
                    break;
            }
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        }
    }

    //zaznam z DB nemazeme nikdy
    /*void deleteValidationFromDb() {
        try {
            validationManagerServiceApi.deleteValidation(this.validationId);
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        }
    }*/
}
