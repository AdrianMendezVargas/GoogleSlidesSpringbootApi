package com.google.slidesgenerationapi.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.slides.v1.SlidesScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class GmailService {

    private static final String APLICACION = "Mi-Aplicacion-Spring-Boot";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String USER = "me"; // 'me' refers to the authenticated user

    private Gmail gmailService;

    public GmailService() throws GeneralSecurityException, IOException {
        //this.gmailService = createGmailService();
    }

    public Gmail createGmailService() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credenciales = GoogleCredentials.fromStream(getClass().getClassLoader().getResourceAsStream("service-account_veloci.json"))
                .createScoped(SlidesScopes.PRESENTATIONS, SlidesScopes.SPREADSHEETS, GmailScopes.GMAIL_COMPOSE);

        return new Gmail.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credenciales))
                .setApplicationName(APLICACION)
                .build();
    }

    public String createDraft(String to, String subject, String body) throws IOException {
        Message draftMessage = createDraftMessage(to, subject, body);
        Draft draft = new Draft().setMessage(draftMessage);

        Draft createdDraft = gmailService.users().drafts().create(USER, draft).execute();
        return createdDraft.getId();
    }

    public void sendEmail(Message emailMessage) throws IOException {
        gmailService.users().messages().send(USER, emailMessage).execute();
    }

    private Message createDraftMessage(String to, String subject, String body) throws IOException {
        Message message = createMessage(body);

        MessagePartHeader toHeader = new MessagePartHeader().setName("To").setValue(to);
        MessagePartHeader subjectHeader = new MessagePartHeader().setName("Subject").setValue(subject);

        MessagePart messagePart = new MessagePart()
                .setBody(new MessagePartBody()
                        .setData(Base64.getUrlEncoder().encodeToString(message.toPrettyString().getBytes())))
                .setHeaders(Arrays.asList(toHeader, subjectHeader));

        return new Message().setPayload(messagePart);
    }

    private Message createMessage(String body) {
        Message message = new Message();
        message.setRaw(body); // Assuming you want plain text for the body
        return message;
    }
}