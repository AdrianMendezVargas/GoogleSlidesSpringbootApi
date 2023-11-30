package com.google.slidesgenerationapi.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest;
import com.google.api.services.slides.v1.model.ReplaceAllTextRequest;
import com.google.api.services.slides.v1.model.Request;
import com.google.api.services.slides.v1.model.SubstringMatchCriteria;
import com.google.slidesgenerationapi.services.DriveService;
import com.google.slidesgenerationapi.services.SlidesService;

@RestController
@RequestMapping("/api/slides")
public class SlidesController {


    @GetMapping()
    public String get(){
        return "hello 29-11-2023";
    }

    @GetMapping("/demo")
    public void runDemo() throws IOException, GeneralSecurityException{

        //1. Clonar
        Drive driveService = new DriveService().getService();

        String presentationId = "1YI_Ht2EhjALZj86t8oPed2O4FXbKukjAZ5-8_EvduKw";
        String copyTitle = "Presentacion JAVA";

        File file = new File();
        file.setName(copyTitle);

        File copiedFile = driveService.files().copy(presentationId, file).execute();
        String copiedPresentationId = copiedFile.getId();


        //2. Reemplazar texto
        Slides slidesService = new SlidesService().getService();

        Request solicitud = new Request()
                .setReplaceAllText(new ReplaceAllTextRequest()
                        .setContainsText(new SubstringMatchCriteria().setText("{{TITLE}}").setMatchCase(true))
                        .setReplaceText("New presentation from JAVA 😎"));

        BatchUpdatePresentationRequest solicitudActualizacion = new BatchUpdatePresentationRequest().setRequests(Collections.singletonList(solicitud));

        slidesService.presentations().batchUpdate(copiedPresentationId, solicitudActualizacion).execute();


        //3. Compartir archivo
        Permission permiso = new Permission()
                .setType("user")
                .setRole("writer")
                .setEmailAddress("velociraptor088@gmail.com");

        driveService.permissions().create(copiedPresentationId, permiso).execute();

    }
    
}
