package com.google.slidesgenerationapi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddChartRequest;
import com.google.api.services.sheets.v4.model.BasicChartDomain;
import com.google.api.services.sheets.v4.model.BasicChartSeries;
import com.google.api.services.sheets.v4.model.BasicChartSpec;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ChartData;
import com.google.api.services.sheets.v4.model.ChartSourceRange;
import com.google.api.services.sheets.v4.model.ChartSpec;
import com.google.api.services.sheets.v4.model.EmbeddedChart;
import com.google.api.services.sheets.v4.model.EmbeddedObjectPosition;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.OverlayPosition;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.Slides.Presentations.Pages.GetThumbnail;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.api.services.slides.v1.model.ReplaceAllShapesWithImageRequest;
import com.google.api.services.slides.v1.model.ReplaceAllTextRequest;
import com.google.api.services.slides.v1.model.Request;
import com.google.api.services.slides.v1.model.SubstringMatchCriteria;
import com.google.api.services.slides.v1.model.Thumbnail;
import com.google.api.services.slides.v1.model.UpdateSlidesPositionRequest;
import com.google.slidesgenerationapi.models.TemplateInfoResponse;
import com.google.slidesgenerationapi.services.DriveService;
import com.google.slidesgenerationapi.services.SheetsService;
import com.google.slidesgenerationapi.services.SlidesService;

@SpringBootTest
class SlidesGenerationApiApplicationTests {

    private final ResourcePatternResolver resourcePatternResolver;

    @Autowired
    public SlidesGenerationApiApplicationTests(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);

    }

    // // @BeforeAll
    // public void setUp() {

    // }

    // //@AfterAll
    // public void tearDown() {
    // // Realiza cualquier limpieza necesaria después de cada prueba
    // }

    // @Test
    void contextLoads() {
    }

    // @Test
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

    // @Test
    public void testReplaceText() throws IOException, GeneralSecurityException {
        Slides slidesService = new SlidesService().getService();

        // Crea la solicitud de reemplazo de texto
        Request solicitud = new Request()
                .setReplaceAllText(new ReplaceAllTextRequest()
                        .setContainsText(new SubstringMatchCriteria().setText("{{TITLE}}").setMatchCase(true))
                        .setReplaceText("Nuevo titulo"));

        BatchUpdatePresentationRequest solicitudActualizacion = new BatchUpdatePresentationRequest()
                .setRequests(Collections.singletonList(solicitud));

        // Envía la solicitud de reemplazo de texto
        slidesService.presentations()
                .batchUpdate("1YI_Ht2EhjALZj86t8oPed2O4FXbKukjAZ5-8_EvduKw", solicitudActualizacion).execute();
    }

    // @Test
    public void sharePresentation() throws IOException, GeneralSecurityException {

        Drive driveService = new DriveService().getService();

        // Define el permiso para el usuario con acceso de lectura
        Permission permiso = new Permission()
                .setType("user")
                .setRole("writer")
                .setEmailAddress("velociraptor088@gmail.com");

        // Comparte la presentación con el usuario especificado
        driveService.permissions().create("1TawkiYzmWdI4AJOBAtHvcbLEWQTcRpfYENC5b_Zd6cY", permiso).execute();

    }

    // @Test
    public void replacePlaceholderWithImage() throws IOException, GeneralSecurityException {

        String presentationId = "";
        String placeholderId = "";
        String imageUrl = "";

        Slides slidesService = new SlidesService().getService();

        // Create a ReplaceAllShapesWithImageRequest to replace the placeholder with an
        // image
        ReplaceAllShapesWithImageRequest request = new ReplaceAllShapesWithImageRequest()
                .setImageUrl(imageUrl)
                .setContainsText(new SubstringMatchCriteria().setText(placeholderId).setMatchCase(true));

        BatchUpdatePresentationRequest batchUpdateRequest = new BatchUpdatePresentationRequest()
                .setRequests(Collections.singletonList(new Request()
                        .setReplaceAllShapesWithImage(request)));

        // Execute the batch update to replace the placeholder with the image
        slidesService.presentations().batchUpdate(presentationId, batchUpdateRequest).execute();
    }

    // @Test
    public void clonePresentation() throws IOException, GeneralSecurityException {

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

    // @Test
    void testDemo() throws IOException, GeneralSecurityException {

        // 1. Clonar
        Drive driveService = new DriveService().getService();

        String presentationId = "1YI_Ht2EhjALZj86t8oPed2O4FXbKukjAZ5-8_EvduKw";
        String copyTitle = "Presentacion JAVA";

        File file = new File();
        file.setName(copyTitle);

        File copiedFile = driveService.files().copy(presentationId, file).execute();
        String copiedPresentationId = copiedFile.getId();

        // 2. Reemplazar texto
        Slides slidesService = new SlidesService().getService();

        Request solicitud = new Request()
                .setReplaceAllText(new ReplaceAllTextRequest()
                        .setContainsText(new SubstringMatchCriteria().setText("{{TITLE}}").setMatchCase(true))
                        .setReplaceText("New presentation from JAVA 😎"));

        BatchUpdatePresentationRequest solicitudActualizacion = new BatchUpdatePresentationRequest()
                .setRequests(Collections.singletonList(solicitud));

        slidesService.presentations().batchUpdate(copiedPresentationId, solicitudActualizacion).execute();

        // 3. Compartir archivo
        Permission permiso = new Permission()
                .setType("user")
                .setRole("writer")
                .setEmailAddress("velociraptor088@gmail.com");

        driveService.permissions().create(copiedPresentationId, permiso).execute();

        assertTrue(true);

    }

    // @Test
    void addMarketingSlides() {

        Map<String, String> marketingPresentationIdsToAdd = new HashMap<String, String>();
        Map<String, Integer> slidesOrder = new HashMap<String, Integer>();

        String targetPresentationId = "1HGssIuAf4ugNCVmd92OYOJe7Inp9c5Hqo0iOF3XbD9c";
        marketingPresentationIdsToAdd.put("gcb9a0b074_1_0", "1OtEIuzTnE0Hm1qAmM2UxssKPIp1bXX-I52HAZdjLK6c");
        // slidesOrder.put("", 0);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (Map.Entry<String, String> marketingPresentationKeyValue : marketingPresentationIdsToAdd
                    .entrySet()) {
                String url = "https://script.google.com/macros/s/AKfycbzCqVDHp9rXYKHemTzdut0_FuhQP6T4q8SQeI-_b3WO8zXzdXj6mNQemwXpqPpXt_eW/exec?srcId="
                        + marketingPresentationKeyValue.getValue() + "&dstId=" + targetPresentationId + "&srcPage="
                        + marketingPresentationKeyValue.getKey();

                HttpGet request = new HttpGet(url);

                org.apache.http.HttpResponse response = client.execute(request);

                if ((response).getStatusLine().getStatusCode() == 200) {
                    String newSlideId = EntityUtils.toString((response).getEntity());

                    if (slidesOrder.containsKey(marketingPresentationKeyValue.getKey())) {
                        int slideIndex = slidesOrder.get(marketingPresentationKeyValue.getKey());
                        slidesOrder.remove(marketingPresentationKeyValue.getKey());
                        slidesOrder.put(newSlideId, slideIndex);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(true);
    }

    // @Test
    void getTemplates() throws IOException, GeneralSecurityException {

        Drive driveService = new DriveService().getService();

        // Define los parámetros de la solicitud.
        Drive.Files.List listRequest = driveService.files().list();
        listRequest.setQ(
                "mimeType='application/vnd.google-apps.presentation' and name starts with 'STemplate' and trashed = false");
        listRequest.setFields("nextPageToken, files(id, name)");

        // Lista los archivos.
        FileList fileList = listRequest.execute();
        List<File> files = fileList.getFiles();

        // Imprimir la información de los archivos
        if (files != null) {
            for (File file : files) {
                System.out.println("ID: " + file.getId() + ", Nombre: " + file.getName());
            }
        } else {
            System.out.println("No se encontraron archivos.");
        }

    }

    public ResponseEntity<List<String>> generateSlideThumbnails() throws IOException, GeneralSecurityException {

        String presentationId = "";
        String size = "SMALL";

        Slides slidesService = new SlidesService().getService();

        // HostingEnvironment hostingEnvironment;

        if (!List.of("SMALL", "MEDIUM", "LARGE").contains(size)) {
            return ResponseEntity.badRequest().body(List.of("Invalid size. Use SMALL, MEDIUM, or LARGE"));
        }

        try {
            // Obtener la presentación
            Presentation presentation = slidesService.presentations().get(presentationId).execute();

            List<String> thumbnailUrls = new ArrayList<>();

            // Crear una ruta de directorio para las miniaturas
            Path thumbnailsFolderPath = Paths.get("classpath:static/", "thumbnails", presentationId, size);

            // Crear el tamaño de miniatura basado en el parámetro 'size'
            GetThumbnail thumbnailRequest;
            switch (size) {
                case "SMALL":
                    thumbnailRequest = slidesService.presentations().pages().getThumbnail(presentationId, "SMALL");
                    break;
                case "MEDIUM":
                    thumbnailRequest = slidesService.presentations().pages().getThumbnail(presentationId, "MEDIUM");
                    break;
                case "LARGE":
                    thumbnailRequest = slidesService.presentations().pages().getThumbnail(presentationId, "LARGE");
                    break;
                default:
                    thumbnailRequest = slidesService.presentations().pages().getThumbnail(presentationId, "MEDIUM");
                    break;
            }

            // Crear el directorio si no existe
            Files.createDirectories(thumbnailsFolderPath);

            // Crear HttpClient instance
            HttpClient httpClient = HttpClient.newHttpClient();
            // Generar miniaturas para cada diapositiva
            for (int i = 0; i < presentation.getSlides().size(); i++) {
                Page slide = presentation.getSlides().get(i);

                // Configurar la solicitud de miniatura
                thumbnailRequest.setPageObjectId(slide.getObjectId());
                thumbnailRequest.setThumbnailPropertiesMimeType("PNG");

                // Ejecutar la solicitud y obtener la URL de la imagen de miniatura
                Thumbnail thumbnailResponse = thumbnailRequest.execute();
                String thumbnailUrl = thumbnailResponse.getContentUrl();

                // Generar un nombre de archivo único para la miniatura
                String thumbnailFilename = slide.getObjectId() + ".png";
                Path thumbnailFilePath = thumbnailsFolderPath.resolve(thumbnailFilename);

                // Descargar la imagen de miniatura usando HttpClient
                HttpResponse<InputStream> response = httpClient.send(
                        HttpRequest.newBuilder(URI.create(thumbnailUrl)).build(),
                        HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == HttpStatus.SC_OK) {
                    // Guardar la imagen de miniatura en el archivo
                    try (FileOutputStream fileOutputStream = new FileOutputStream(thumbnailFilePath.toFile())) {
                        IOUtils.copy(response.body(), fileOutputStream);
                    }
                }

                // Agregar la ruta del archivo de miniatura a la lista
                String thumbnailUrlPath = "/thumbnails/" + presentationId + "/" + size + "/" + thumbnailFilename;
                thumbnailUrls.add(thumbnailUrlPath);
            }

            return ResponseEntity.ok(thumbnailUrls);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(List.of("Error al generar miniaturas."));
        }
    }

    @Test
    public void readFileFromStaticFolder() {

        List<String> files;

        try {

            // Using ResourcePatternResolver
            ClassPathResource resource = new ClassPathResource("static/");
            java.io.File file = resource.getFile();
            java.io.File[] listFiles = file.listFiles();
            System.out.println(listFiles);

            // Extract file names from resources

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            files = List.of("Error listing files in the static folder: " + e.getMessage());
            System.out.println(files);

        }

    }

    @Test
    public void readSheets() throws IOException, GeneralSecurityException {

        Sheets sheetsService = new SheetsService().getService();

        // ID of the Google Sheet (you can find it in the URL)
        String spreadsheetId = "1J7sP682rkpLtGiRXcxJVkeCGKhCd8iUNktRomp2iEM0";

        // Fetch the spreadsheet
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();

        // Get the title (name) of the spreadsheet
        String spreadsheetTitle = spreadsheet.getProperties().getTitle();

        System.out.println("Spreadsheet Name: " + spreadsheetTitle);

    }

    @Test
    public void reorderSlides() throws IOException, GeneralSecurityException {
        Slides slidesService = new SlidesService().getService();

        List<Request> requests = new ArrayList<>();
        Map<String, Integer> slidesOrder = new HashMap<>();

        slidesOrder.put("SLIDES_API354122686_0", 3);
        // slidesOrder.put("slideId2", 2);

        String copyPresentationId = "1HGssIuAf4ugNCVmd92OYOJe7Inp9c5Hqo0iOF3XbD9c";

        for (Map.Entry<String, Integer> sOrder : slidesOrder.entrySet()) {
            UpdateSlidesPositionRequest updateSlidesPositionRequest = new UpdateSlidesPositionRequest()
                    .setSlideObjectIds(List.of(sOrder.getKey()))
                    .setInsertionIndex(sOrder.getValue());

            Request request = new Request().setUpdateSlidesPosition(updateSlidesPositionRequest);
            requests.add(request);
        }

        BatchUpdatePresentationRequest batchUpdateRequest = new BatchUpdatePresentationRequest()
                .setRequests(requests);

        slidesService.presentations().batchUpdate(copyPresentationId, batchUpdateRequest).execute();

        assertTrue(true);
    }

    @Test
    void testCreateChart() {
        assertDoesNotThrow(() -> createChart("1J7sP682rkpLtGiRXcxJVkeCGKhCd8iUNktRomp2iEM0", "Sheet1"));
    }

    // Replace this method with your actual implementation
    private void createChart(String spreadsheetId, String sheetName) throws IOException, GeneralSecurityException {
        // Set up the Sheets service
        Sheets sheetsService = new SheetsService().getService();

        // Specify the data for the chart
        List<RowData> rowData = new ArrayList<>();
        List<CellData> values = new ArrayList<>();

        // Assuming data for the chart is in cells A1:B5
        values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Category")));
        values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Value")));
        rowData.add(new RowData().setValues(values));

        values = new ArrayList<>();
        values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("A")));
        values.add(new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue(10d)));
        rowData.add(new RowData().setValues(values));

        values = new ArrayList<>();
        values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("B")));
        values.add(new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue(20d)));
        rowData.add(new RowData().setValues(values));

        values = new ArrayList<>();
        values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("C")));
        values.add(new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue(30d)));
        rowData.add(new RowData().setValues(values));

        // Create the chart embedded in cell E1
        EmbeddedChart chart = new EmbeddedChart()
                .setSpec(new ChartSpec()
                        .setTitle("My Chart")
                        .setBasicChart(new BasicChartSpec()
                                .setChartType("LINE")
                                .setSeries(Arrays.asList(
                                        new BasicChartSeries()
                                                .setSeries(new ChartData()
                                                        .setSourceRange(new ChartSourceRange()
                                                                .setSources(Arrays.asList(
                                                                        new GridRange()
                                                                                .setEndColumnIndex(2)
                                                                                .setEndRowIndex(4)
                                                                                .setStartColumnIndex(1)
                                                                                .setStartRowIndex(0)))))))
                                .setDomains(Arrays.asList(
                                        new BasicChartDomain()
                                                .setDomain(new ChartData()
                                                        .setSourceRange(new ChartSourceRange()
                                                                .setSources(Arrays.asList(
                                                                        new GridRange()
                                                                                .setEndColumnIndex(1)
                                                                                .setEndRowIndex(4)
                                                                                .setStartColumnIndex(0)
                                                                                .setStartRowIndex(0)))))))
                                .setHeaderCount(1)
                                .setLegendPosition("BOTTOM_LEGEND")
                                .setAxis(new ArrayList<>())))
                .setPosition(new EmbeddedObjectPosition()
                        .setOverlayPosition(new OverlayPosition()
                                .setOffsetXPixels(50)
                                .setOffsetYPixels(50)
                                .setWidthPixels(500)
                                .setHeightPixels(350)));

        List<com.google.api.services.sheets.v4.model.Request> requests = new ArrayList<>();
        requests.add(new com.google.api.services.sheets.v4.model.Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate().setSheetId(0).setRowIndex(0).setColumnIndex(0))
                        .setRows(rowData)
                        .setFields("*")));
        requests.add(new com.google.api.services.sheets.v4.model.Request()
                .setAddChart(new AddChartRequest()
                        .setChart(chart)));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);

        sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
    }

}
