package nkp.pspValidator.web.backend.utils;

import nkp.pspValidator.web.backend.utils.auth.KeyBuilder;
import org.junit.Test;

public class KeyBuilderTest {

    @Test
    public void testKeyBuilder() throws Exception {
        KeyBuilder keyBuilder = new KeyBuilder();
        String[] keys = keyBuilder.buildPublicPrivateKey();
        System.out.println("publicKey: \n" + keys[0]);
        System.out.println("privateKey: \n" + keys[1]);
    }
}
