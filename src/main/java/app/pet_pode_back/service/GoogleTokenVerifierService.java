package app.pet_pode_back.service;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleTokenVerifierService {

    private static final String CLIENT_ID = "646686829695-psiqebgq6ia23nrvahilgo422p6d08nb.apps.googleusercontent.com"; // Não use o do Android, precisa ser o WEB client ID

    public Payload verify(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new Exception("ID token inválido");
        }
    }
}
