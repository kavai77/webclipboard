package com.himadri.webclipboard;

import com.google.appengine.api.datastore.Text;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.googlecode.objectify.NotFoundException;
import com.himadri.webclipboard.entity.Clipboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Controller
public class ClipboardController {
    public static final String X_AUTHORIZATION_FIREBASE = "X-Authorization-Firebase";

    @Autowired
    private Application.GoogleCloudRuntime runtime;

    @PostConstruct
    public void init() throws IOException {
        GoogleCredentials googleCredentials;
        if (runtime == Application.GoogleCloudRuntime.LOCAL) {
            try {
                googleCredentials = GoogleCredentials.fromStream(new FileInputStream(Application.LOCAL_APPLICATION_CREDENTIALS));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            googleCredentials = GoogleCredentials.getApplicationDefault();
        }
        FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(googleCredentials)
            .setProjectId("webclipboard")
            .setStorageBucket("webclipboard.appspot.com")
            .setDatabaseUrl("https://webclipboard-7ac72.firebaseio.com")
            .build();
        FirebaseApp.initializeApp(options);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/copy")
    @ResponseBody
    public String copy(
        @RequestParam String text,
        @RequestHeader(X_AUTHORIZATION_FIREBASE) String firebaseToken
    ) throws FirebaseAuthException {
        if (text.length() > 1024 * 1024) {
            throw new PayloadTooLargeException();
        }
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
        ofy().save().entity(new Clipboard(decodedToken.getUid(), new Text(text), System.currentTimeMillis())).now();
        return "OK";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/paste")
    @ResponseBody
    public String paste(@RequestHeader(X_AUTHORIZATION_FIREBASE) String firebaseToken) throws FirebaseAuthException {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            return ofy().load().type(Clipboard.class).id(decodedToken.getUid()).safe().getText().getValue();
        } catch (NotFoundException e) {
            return "";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/_ah/warmup")
    @ResponseBody
    public String warmup() {
        return "OK";
    }

    @ResponseStatus(value= HttpStatus.PAYLOAD_TOO_LARGE)
    public static class PayloadTooLargeException extends RuntimeException{}
}
