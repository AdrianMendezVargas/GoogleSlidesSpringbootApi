package com.google.slidesgenerationapi.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.slides.v1.model.*;
import com.google.api.services.slides.v1.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

public class SlidesService {

    private static final String APPLICATION_NAME = "Your-Spring-Boot-App";
    //private static final String CREDENTIALS_FILE_PATH = "images\\service-account_veloci.json";

    public Slides getService() throws IOException, GeneralSecurityException {

        //ClassPathResource classPathResource = new ClassPathResource("classpath:service-account_veloci.json");

        GoogleCredential credential = GoogleCredential.fromStream(getClass().getClassLoader().getResourceAsStream("service-account_veloci.json"))
                .createScoped(Collections.singleton(SlidesScopes.PRESENTATIONS));
        return new Slides.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
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


