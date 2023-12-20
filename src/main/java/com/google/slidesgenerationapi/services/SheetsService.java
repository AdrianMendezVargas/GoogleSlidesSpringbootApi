package com.google.slidesgenerationapi.services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class SheetsService {

    private static final String APLICACION = "Mi-Aplicacion-Spring-Boot";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    public Sheets getService() throws IOException, GeneralSecurityException {
        

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credenciales = GoogleCredentials.fromStream(getClass().getClassLoader().getResourceAsStream("service-account_veloci.json"))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credenciales))
                .setApplicationName(APLICACION)
                .build();
    }
    
}
