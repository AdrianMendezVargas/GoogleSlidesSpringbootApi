package com.google.slidesgenerationapi.services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.SlidesScopes;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class SlidesService {

    private static final String APPLICATION_NAME = "Your-Spring-Boot-App";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public Slides getService() throws IOException, GeneralSecurityException {


        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = GoogleCredentials.fromStream(getClass().getClassLoader().getResourceAsStream("service-account_veloci.json"))
                .createScoped(Collections.singleton(SlidesScopes.PRESENTATIONS));

        return new Slides.Builder(httpTransport , JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String createPresentation(String title) throws IOException, GeneralSecurityException {
        Slides slidesService = getService();

        Presentation presentation = new Presentation();
        presentation.setTitle(title);

        Presentation createdPresentation = slidesService.presentations().create(presentation).execute();
        

        return createdPresentation.getPresentationId();
    }
    

    // Use the 'getService' method to get the Slides service for further operations.
    // You can use this service to create, modify, or retrieve Google Slides presentations.
}


