package nkp.pspValidator.web.backend.notification_service;

import org.junit.Test;

import java.io.IOException;

public class PostmarkHelperTest {

    @Test
    public void sendValidationFinishedEmail() throws IOException, EmailSenderException {
        /*
        Config.init();
        PostmarkHelper postmarkHelper = new PostmarkHelper(
                Config.instanceOf().getNotificationServicePostmarkServerToken(),
                Config.instanceOf().getNotificationServiceSenderEmail(),
                Config.instanceOf().getNotificationServiceFrontendBaseUrl()
        );
        String validationId = "380bfae5-a243-4bd3-9b81-7d30d262558a";
        String packageName = "ex001-0003fn";
        String status = "FINISHED";
        String recipient = "martin.rehanek@gmail.com";
        postmarkHelper.sendValidationFinished(recipient, validationId, packageName, status, 13);
        */
    }
}
