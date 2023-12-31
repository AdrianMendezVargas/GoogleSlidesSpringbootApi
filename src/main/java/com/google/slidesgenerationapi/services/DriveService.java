package com.google.slidesgenerationapi.services;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.stereotype.Service;

@Service
public class DriveService {

    private static final String APLICACION = "Mi-Aplicacion-Spring-Boot";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    public Drive getService() throws IOException, GeneralSecurityException {
        

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credenciales = GoogleCredentials.fromStream(getClass().getClassLoader().getResourceAsStream("service-account_veloci.json"))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        return new Drive.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credenciales))
                .setApplicationName(APLICACION)
                .build();
    }
}
