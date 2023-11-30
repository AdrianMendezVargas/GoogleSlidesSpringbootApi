package com.google.slidesgenerationapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest;
import com.google.api.services.slides.v1.model.ReplaceAllShapesWithImageRequest;
import com.google.api.services.slides.v1.model.ReplaceAllTextRequest;
import com.google.api.services.slides.v1.model.Request;
import com.google.api.services.slides.v1.model.SubstringMatchCriteria;
import com.google.slidesgenerationapi.services.DriveService;
import com.google.slidesgenerationapi.services.SlidesService;

@SpringBootTest
class SlidesGenerationApiApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
    public void testCreatePresentation() throws IOException, GeneralSecurityException {
        SlidesService googleSlidesService = new SlidesService();

        // Replace with your desired title for the presentation
        String presentationTitle = "Test Presentation";

        String presentationId = googleSlidesService.createPresentation(presentationTitle);

        // Perform additional assertions or checks based on the expected behavior
        assert presentationId != null;
        System.out.println("Created presentation with ID: " + presentationId);

        Drive driveService = new DriveService().getService();

        // Define el permiso para el usuario con acceso de lectura
        Permission permiso = new Permission()
                .setType("user")
                .setRole("writer")
                .setEmailAddress("velociraptor088@gmail.com");

        // Comparte la presentación con el usuario especificado
        driveService.permissions().create(presentationId, permiso).execute();

    }

    //@Test
    public void testReplaceText() throws IOException, GeneralSecurityException {
        Slides slidesService = new SlidesService().getService();

                // Crea la solicitud de reemplazo de texto
        Request solicitud = new Request()
                .setReplaceAllText(new ReplaceAllTextRequest()
                        .setContainsText(new SubstringMatchCriteria().setText("{{TITLE}}").setMatchCase(true))
                        .setReplaceText("Nuevo titulo"));

        BatchUpdatePresentationRequest solicitudActualizacion = new BatchUpdatePresentationRequest().setRequests(Collections.singletonList(solicitud));

        // Envía la solicitud de reemplazo de texto
        slidesService.presentations().batchUpdate("1YI_Ht2EhjALZj86t8oPed2O4FXbKukjAZ5-8_EvduKw", solicitudActualizacion).execute();
    }

    //@Test
    public void sharePresentation() throws IOException, GeneralSecurityException{

        Drive driveService = new DriveService().getService();

        // Define el permiso para el usuario con acceso de lectura
        Permission permiso = new Permission()
                .setType("user")
                .setRole("writer")
                .setEmailAddress("velociraptor088@gmail.com");

        // Comparte la presentación con el usuario especificado
        driveService.permissions().create("1TawkiYzmWdI4AJOBAtHvcbLEWQTcRpfYENC5b_Zd6cY", permiso).execute();


    }

    //@Test
    public void replacePlaceholderWithImage() throws IOException, GeneralSecurityException {

        String presentationId = "";
        String placeholderId = "";
        String imageUrl = "";

        Slides slidesService = new SlidesService().getService();

        // Create a ReplaceAllShapesWithImageRequest to replace the placeholder with an image
        ReplaceAllShapesWithImageRequest request = new ReplaceAllShapesWithImageRequest()
                .setImageUrl(imageUrl)
                .setContainsText(new SubstringMatchCriteria().setText(placeholderId).setMatchCase(true));

        BatchUpdatePresentationRequest batchUpdateRequest = new BatchUpdatePresentationRequest()
                .setRequests(Collections.singletonList(new Request()
                        .setReplaceAllShapesWithImage(request)));

        // Execute the batch update to replace the placeholder with the image
        slidesService.presentations().batchUpdate(presentationId, batchUpdateRequest).execute();
    }

    @Test
    public void clonePresentation() throws IOException, GeneralSecurityException{

        Drive driveService = new DriveService().getService();

        String presentationId = "1YI_Ht2EhjALZj86t8oPed2O4FXbKukjAZ5-8_EvduKw";
        String copyTitle = "Copy presentacion";

        File file = new File();
        file.setName(copyTitle);

        File copiedFile = driveService.files().copy(presentationId, file).execute();
        String copiedPresentationId = copiedFile.getId();


        // compartir archivo
        Permission permiso = new Permission()
                .setType("user")
                .setRole("writer")
                .setEmailAddress("velociraptor088@gmail.com");

        // Comparte la presentación con el usuario especificado
        driveService.permissions().create(copiedPresentationId, permiso).execute();

    }

    @Test
    void testDemo() throws IOException, GeneralSecurityException{

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
        
        assertTrue(true);

    }

}
