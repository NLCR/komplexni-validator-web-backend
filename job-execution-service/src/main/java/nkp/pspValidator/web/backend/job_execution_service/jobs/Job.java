package nkp.pspValidator.web.backend.job_execution_service.jobs;

import nkp.pspValidator.web.backend.utils.apiClient.ApiClientException;
import nkp.pspValidator.web.backend.utils.apiClient.NotificationServiceApi;
import nkp.pspValidator.web.backend.utils.apiClient.ValidationManagerServiceApi;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Job {

    private static final Logger logger = Logger.getLogger(Job.class.getName());

    final String validationId;

    private final ValidationManagerServiceApi validationManagerServiceApi = new ValidationManagerServiceApi();
    private final NotificationServiceApi notificationServiceApi = new NotificationServiceApi();

    Job(String validationId) {
        this.validationId = validationId;
    }

    public abstract void run();

    void updateValidationState(String status) {
        //zmena stavu je zavazna - jeji selhani se propaguje volajicimu
        try {
            //System.out.println("updating validation state to " + status);
            validationManagerServiceApi.updateValidationState(this.validationId, status);
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        }
        //notifikace je best-effort - jeji selhani nesmi zvratit uz provedenou zmenu stavu
        try {
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
        } catch (Exception e) {
            logger.warning("failed to send notification for validation " + this.validationId + " (state " + status + "): " + e.getMessage());
        }
    }

    /**
     * Best-effort varianta pro chybove cesty (catch bloky jobu): selhani zmeny stavu
     * uz nema jak napravit, jen ho zaloguje - nesmi zabit vlakno jobu dalsi vyjimkou.
     */
    void updateValidationStateQuietly(String status) {
        try {
            updateValidationState(status);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to update state of validation " + this.validationId + " to " + status, e);
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
