package nkp.pspValidator.web.backend.notification_service;

import com.postmarkapp.postmark.Postmark;
import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.Message;
import com.postmarkapp.postmark.client.exception.PostmarkException;
import nkp.pspValidator.web.backend.utils.apiClient.ApiClientException;
import nkp.pspValidator.web.backend.utils.apiClient.QuotaServiceApi;

import java.io.IOException;

public class PostmarkHelper {

    private final QuotaServiceApi quotaServiceApi = new QuotaServiceApi();

    private final String serverToken;
    private final String emailFrom;
    private final String frontendBasUrl;

    private final boolean postmarkEnabled;

    public PostmarkHelper(String serverToken, String emailFrom, String frontendBasUrl, boolean postmarkEnabled) {
        this.serverToken = serverToken;
        this.emailFrom = emailFrom;
        this.frontendBasUrl = frontendBasUrl;
        this.postmarkEnabled = postmarkEnabled;
    }


    private String translateStatus(String status) {
        switch (status) {
            case "ERROR":
                return "CHYBA";
            case "FINISHED":
                return "DOKONČENO";
            default:
                return status;
        }
    }

    private void sendEmail(String recipientEmail, String subject, String bodyHtml) throws EmailSenderException {
        if (!postmarkEnabled) {
            System.out.println("SIMULATED EMAIL to: " + recipientEmail + ", subject: " + subject + ", body:\n" + bodyHtml);
        } else {
            try {
                ApiClient client = Postmark.getApiClient(serverToken);
                Message message = new Message(emailFrom, recipientEmail, subject, bodyHtml);
                client.deliverMessage(message);
            } catch (PostmarkException e) {
                throw new EmailSenderException(e);
            } catch (IOException e) {
                throw new EmailSenderException(e);
            }
        }
    }

    private String buildHtmlHeader(String title) {
        return "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>" + title + "</title>\n" +
                "</head>\n" +
                "<body>\n";
    }

    private String buildHtmlFooter() {
        return "<p>Děkujeme za využívání služeb Komplexního validátoru.</p>" +
                "</body>\n" +
                "</html>";
    }

    private String buildLinkToValidation(String validationId) {
        String url = this.frontendBasUrl + "/validations/" + validationId;
        return "<a href=\"" + url + "\">Zobrazit výsledky validace</a>\n";
    }

    public void sendValidationFinished(String recipientEmail, String validationId, String packageName, String status, Long durationS) throws EmailSenderException {
        String subject = "Validace dokončena";
        //System.out.println("durationS is null?" + (durationS == null));
        String durationFormatted = durationS == null ? null : durationS + " sekund";
        String html = buildHtmlHeader(subject)
                + "<p>Dobrý den,</p>\n"
                + "<p>Validace balíčku " + packageName + " byla dokončena.</p>\n"
                +
                (durationFormatted == null
                        ? ("<p>Validace skončila ve stavu <strong>" + translateStatus(status) + "</strong>.</p>\n")
                        : ("<p>Validace skončila ve stavu <strong>" + translateStatus(status) + "</strong>" + " a trvala <strong>" + durationFormatted + "</strong>.</p>\n")
                )
                + "<p>Výsledky validace si můžete zobrazit kliknutím na následující odkaz:</p>\n"
                + buildLinkToValidation(validationId)
                + buildHtmlFooter();
        sendEmail(recipientEmail, subject, html);
    }

    public void sendValidationArchived(String recipientEmail, String validationId, String packageName) throws EmailSenderException {
        String subject = "Validace archivována";
        Integer timeUntilDeletionH = null;
        try {
            timeUntilDeletionH = quotaServiceApi.getQuotas().timeToDeleteValidationH;
        } catch (ApiClientException e) {
            e.printStackTrace();
        }
        String timeUntilDeletion = formatToDaysAndHours(timeUntilDeletionH);

        String html = buildHtmlHeader(subject)
                + "<p>Dobrý den,</p>\n"
                + "<p>Validace balíčku " + packageName + " byla archivována.</p>\n"
                + "<p>Validace bude úplně smazána za " + timeUntilDeletion + ".</p>\n"
                + "<p>Do té doby si můžete výsledky validace zobrazit kliknutím na následující odkaz:</p>\n"
                + buildLinkToValidation(validationId)
                + buildHtmlFooter();
        sendEmail(recipientEmail, subject, html);
    }

    public void sendValidationDeleted(String recipientEmail, String validationId, String packageName) throws EmailSenderException {
        String subject = "Validace smazána";
        String html = buildHtmlHeader(subject)
                + "<p>Dobrý den,</p>\n"
                + "<p>Validace balíčku " + packageName + " byla smazána.</p>\n"
                + "<p>Výsledky validace nejsou nadále dostupné.</p>\n"
                + buildHtmlFooter();
        sendEmail(recipientEmail, subject, html);
    }

    private String formatToDaysAndHours(Integer hours) {
        if (hours == null) {
            return null;
        }
        int days = hours / 24;
        int hoursLeft = hours % 24;
        if (days > 0) {
            return hoursLeft == 0 ? formatDays(days) : formatDays(days) + " a " + formatHours(hoursLeft);
        } else {
            return formatHours(hours);
        }
    }

    private String formatHours(int hours) {
        if (hours == 1) {
            return "1 hodinu";
        } else if (hours >= 2 && hours <= 4) {
            return hours + " hodiny";
        } else {
            return hours + " hodin";
        }
    }

    private String formatDays(int days) {
        if (days == 1) {
            return "1 den";
        } else if (days >= 2 && days <= 4) {
            return days + " dny";
        } else {
            return days + " dní";
        }
    }


}
