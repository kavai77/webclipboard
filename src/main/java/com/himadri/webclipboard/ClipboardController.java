package com.himadri.webclipboard;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.himadri.webclipboard.entity.Clipboard;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ClipboardController {
    public static final String X_AUTHORIZATION_FIREBASE = "X-Authorization-Firebase";
    private static final ConcurrentHashMap<String, AESKey> memoryKeyStore = new ConcurrentHashMap<>();
    private static final String CLAIM_KEY = "key";
    private static final String CLAIM_IV = "iv";

    @Autowired
    private CipherEngine cipherEngine;

    @Autowired
    private ResourceHash resourceHash;

    @Autowired
    private DynamoDbRepository dynamoDbRepository;

    @PostConstruct
    public void init() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(getClass().getResourceAsStream("/webclipboard-firebase-admin-service.json"));
        FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(googleCredentials)
            .setProjectId("webclipboard")
            .setStorageBucket("webclipboard.appspot.com")
            .setDatabaseUrl("https://webclipboard-7ac72.firebaseio.com")
            .build();
        FirebaseApp.initializeApp(options);
    }

    @RequestMapping(value = "/")
    public String index(Model model) {
        model.addAttribute("mainjshash", resourceHash.getResourceHash(ResourceHash.Resource.MAIN_JS));
        model.addAttribute("indexcsshash", resourceHash.getResourceHash(ResourceHash.Resource.INDEX_CSS));
        return "index";
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/text")
    public ResponseEntity<Object> putText(
        @RequestParam String text,
        @RequestHeader(X_AUTHORIZATION_FIREBASE) String firebaseToken
    ) throws FirebaseAuthException {
        if (text.length() > 1024 * 1024) {
            throw new PayloadTooLargeException();
        }
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
        byte[] encrypted = cipherEngine.encrypt(getAesKey(decodedToken), text);
        Clipboard clipboard = new Clipboard(decodedToken.getUid(), encrypted, System.currentTimeMillis());
        dynamoDbRepository.save(clipboard);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/text")
    @ResponseBody
    public String getText(@RequestHeader(X_AUTHORIZATION_FIREBASE) String firebaseToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
        byte[] encryptedText = dynamoDbRepository.getEncryptedText(decodedToken.getUid());
        if (encryptedText == null) {
            return "";
        }
        return cipherEngine.decrypt(getAesKey(decodedToken), encryptedText);
    }

    private AESKey getAesKey(FirebaseToken decodedToken) {
        return memoryKeyStore.computeIfAbsent(decodedToken.getUid(), it -> {
                Map<String, Object> tokenClaims = decodedToken.getClaims();
                Object key = tokenClaims.get(CLAIM_KEY);
                Object iv = tokenClaims.get(CLAIM_IV);
                if (key == null || iv == null) {
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    Map<String, Object> customClaims;
                    try {
                        customClaims = firebaseAuth.getUser(decodedToken.getUid()).getCustomClaims();
                    } catch (FirebaseAuthException e) {
                        throw new IllegalArgumentException(e);
                    }
                    key = customClaims.get(CLAIM_KEY);
                    iv = customClaims.get(CLAIM_IV);
                    if (key == null || iv == null) {
                        key = Base64.encodeBase64String(RandomUtils.nextBytes(32));
                        iv = Base64.encodeBase64String(RandomUtils.nextBytes(16));
                        firebaseAuth.setCustomUserClaimsAsync(decodedToken.getUid(), Map.of(
                            CLAIM_KEY, key,
                            CLAIM_IV, iv
                        ));
                    }
                }
                return new AESKey(
                    Base64.decodeBase64((String) key),
                    Base64.decodeBase64((String) iv)
                );

        });
    }

    @RequestMapping(method = RequestMethod.GET, value = "/_ah/warmup")
    @ResponseBody
    public ResponseEntity<Object> warmup() {
        return ResponseEntity.ok().build();
    }

    @ResponseStatus(value= HttpStatus.PAYLOAD_TOO_LARGE)
    public static class PayloadTooLargeException extends RuntimeException{}
}
