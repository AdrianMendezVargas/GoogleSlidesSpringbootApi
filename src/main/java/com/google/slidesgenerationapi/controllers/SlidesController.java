package com.google.slidesgenerationapi.controllers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Permissions.Create;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddChartRequest;
import com.google.api.services.sheets.v4.model.BasicChartAxis;
import com.google.api.services.sheets.v4.model.BasicChartDomain;
import com.google.api.services.sheets.v4.model.BasicChartSeries;
import com.google.api.services.sheets.v4.model.BasicChartSpec;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ChartData;
import com.google.api.services.sheets.v4.model.ChartSourceRange;
import com.google.api.services.sheets.v4.model.ChartSpec;
import com.google.api.services.sheets.v4.model.EmbeddedChart;
import com.google.api.services.sheets.v4.model.EmbeddedObjectPosition;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.OverlayPosition;
import com.google.api.services.sheets.v4.model.PieChartSpec;
import com.google.api.services.sheets.v4.model.Response;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.Slides.Presentations;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest;
import com.google.api.services.slides.v1.model.DeleteObjectRequest;
import com.google.api.services.slides.v1.model.InsertTableRowsRequest;
import com.google.api.services.slides.v1.model.InsertTextRequest;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.PageElement;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.api.services.slides.v1.model.ReplaceAllShapesWithImageRequest;
import com.google.api.services.slides.v1.model.ReplaceAllShapesWithSheetsChartRequest;
import com.google.api.services.slides.v1.model.ReplaceAllTextRequest;
import com.google.api.services.slides.v1.model.Request;
import com.google.api.services.slides.v1.model.SubstringMatchCriteria;
import com.google.api.services.slides.v1.model.Table;
import com.google.api.services.slides.v1.model.TableCell;
import com.google.api.services.slides.v1.model.TableCellLocation;
import com.google.api.services.slides.v1.model.TableRow;
import com.google.api.services.slides.v1.model.TextContent;
import com.google.api.services.slides.v1.model.TextElement;
import com.google.api.services.slides.v1.model.TextRun;
import com.google.api.services.slides.v1.model.UpdateSlidesPositionRequest;
import com.google.slidesgenerationapi.models.ChartInfo;
import com.google.slidesgenerationapi.models.CreateSlidesDeckFromTemplate;
import com.google.slidesgenerationapi.models.FolderMetadata;
import com.google.slidesgenerationapi.models.PlaceholderMetadata;
import com.google.slidesgenerationapi.models.SlideMetadata;
import com.google.slidesgenerationapi.models.TemplateInfoResponse;
import com.google.slidesgenerationapi.models.TemplateMetadata;
import com.google.slidesgenerationapi.services.DriveService;
import com.google.slidesgenerationapi.services.SheetsService;
import com.google.slidesgenerationapi.services.SlidesService;

@RestController
@RequestMapping("/api/slidesControler")
public class SlidesController {

    @Value("${external.images.directory}")
    private String externalImagesDirectory;

    private final Drive driveService;
    private final Slides slidesService;
    private final Sheets sheetsService;

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public SlidesController(
            DriveService driveService,
            SlidesService slidesService,
            SheetsService sheetsService) throws IOException, GeneralSecurityException {

        this.sheetsService = sheetsService.getService();
        this.driveService = driveService.getService();
        this.slidesService = slidesService.getService();
    }

    @GetMapping()
    public String get() {
        return "hello 07-12-2023";
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFilesInStaticFolder() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/");
        java.io.File folder = resource.getFile();
        java.io.File[] listFiles = folder.listFiles();

        return ResponseEntity.ok(Arrays.stream(listFiles)
                .map(file -> file.getName())
                .collect(Collectors.toList()));
    }

    @GetMapping("/download")
    public ResponseEntity<String> downloadImage() {
        try {

            createExternalDirectoryIfNotExists();

            String imageUrl = "https://static.vecteezy.com/system/resources/previews/021/608/795/non_2x/chatgpt-logo-chat-gpt-icon-on-green-background-free-vector.jpg";
            // Download image from URL
            URL url = new URL(imageUrl);
            try (InputStream in = url.openStream()) {

                // Save image to external directory
                Path imagePath = Paths.get(externalImagesDirectory, "image.jpg");
                try (FileOutputStream fos = new FileOutputStream(imagePath.toString())) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

            }

            return ResponseEntity.ok("Image downloaded and saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to download and save image");
        }
    }

    private void createExternalDirectoryIfNotExists() throws IOException {
        Path directoryPath = Paths.get(externalImagesDirectory);
        if (Files.notExists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
    }

    @GetMapping("/demo")
    public void runDemo() throws IOException {

        String presentationId = "1YI_Ht2EhjALZj86t8oPed2O4FXbKukjAZ5-8_EvduKw";
        String copyTitle = "Presentacion JAVA";

        File file = new File();
        file.setName(copyTitle);

        File copiedFile = driveService.files().copy(presentationId, file).execute();
        String copiedPresentationId = copiedFile.getId();

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

    }

    @GetMapping("/dir")
    public String getDirectory() {

        return ("Current Working Directory: " + System.getProperty("user.dir"));

    }

    @GetMapping("/path")
    public String getJsonPath() throws FileNotFoundException {

        String filePath = getClass().getClassLoader().getResource("service-account_veloci.json").getPath();
        java.io.File file = ResourceUtils.getFile("classpath:service-account_veloci.json");
        file.getPath();
        return ("File Path: " + file.getPath());

    }

    @GetMapping("/exists/filePath={filePath}")
    public ResponseEntity<Boolean> checkFileExists(@PathVariable String filePath) {
        java.io.File file = new java.io.File(filePath);
        boolean exists = file.exists();
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    public ResponseEntity<String> createFromTemplate(@RequestBody CreateSlidesDeckFromTemplate req) throws IOException {

        System.out.println("Objeto Peticion: " + new ObjectMapper().writeValueAsString(req));

        File copyPresentation = new File();
        copyPresentation.setName(req.getPresentationName());

        Drive.Files.Copy copyRequest = driveService.files().copy(req.getTemplateId(), copyPresentation);
        String copyPresentationId = copyRequest.execute().getId();

        try {
            Permission permission = new Permission();
            permission.setType("user");
            permission.setRole("writer");
            permission.setEmailAddress(req.getReciverEmail());

            // Create the permission request and execute it to share the file
            Create request = driveService.permissions().create(copyPresentationId, permission);
            request.setSendNotificationEmail(true);
            request.execute();

            Presentation slidesPresentation = slidesService.presentations().get(copyPresentationId).execute();

            BatchUpdatePresentationRequest batchRequest = new BatchUpdatePresentationRequest();
            List<Request> requests = new ArrayList<>();

            if (req.getTextPlaceholders() != null && !req.getTextPlaceholders().isEmpty())
                requests.addAll(getTextPlaceholdersRequests(req.getTextPlaceholders()));

            if (req.getImagePlaceholders() != null && !req.getImagePlaceholders().isEmpty())
                requests.addAll(getImagePlaceholdersRequests(req.getImagePlaceholders()));

            if (req.getChartPlaceholders() != null && !req.getChartPlaceholders().isEmpty())
                requests.addAll(getChartPlaceholdersRequest(req.getChartPlaceholders()));

            requests.addAll(getRemoveSlidesRequest(req.getSlidesToRemove(), slidesPresentation));

            if (req.getMarketingSlidesIdsToAdd() != null && !req.getMarketingSlidesIdsToAdd().isEmpty()) {
                addMarketingSlides(req.getMarketingSlidesIdsToAdd(), copyPresentationId, req.getSlidesOrder());
            }

            if (!req.getSlidesOrder().isEmpty()) {
                reorderSlides(req.getSlidesOrder(), copyPresentationId);
            }

            batchRequest.setRequests(requests);

            slidesService.presentations()
                    .batchUpdate(copyPresentationId, batchRequest).execute();

            // TODO: Delete this!!!
            if ("18p-VPv_8w31vLB4IBQfv9FWvkk0rLBvk1j079kHVrFQ".equals(req.getTemplateId())) {

                // Adding sample data to tables

                // Current projects
                addRowToTable(copyPresentationId, "g1e7dd5128d1_1_24",
                        new String[] { "Project 2", "Description 2", "Focus 2", "Owner 2", "Status 2" });
                addRowToTable(copyPresentationId, "g1e7dd5128d1_1_24",
                        new String[] { "Project 3", "Description 3", "Focus 3", "Owner 3", "Status 3" });
                addRowToTable(copyPresentationId, "g1e7dd5128d1_1_24",
                        new String[] { "Project 4", "Description 4", "Focus 4", "Owner 4", "Status 4" });

                // SPOC List
                for (int i = 0; i < 10; i++) {
                    addRowToTable(copyPresentationId, "g1e7dd5128d1_1_232",
                            new String[] { "Name " + i, "Phone " + i, "Email " + i, "✔", "✔", "✔", "✔", "✔", "✔" });
                }

                for (int i = 0; i < 5; i++) {
                    addRowToTable(copyPresentationId, "g1e7dd5128d1_1_46",
                            new String[] { "Name " + i, "Phone " + i, "Email " + i, "✔", "✔", "✔", "✔", "✔", "✔" });
                }

                // My Business
                for (int i = 0; i < 5; i++) {
                    addRowToTable(copyPresentationId, "g1e7dd5128d1_1_52", new String[] { "Role " + i, "✔", " " });
                }
            }

        } catch (Exception e) {
            driveService.files().delete(copyPresentationId).execute();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }

        return ResponseEntity.ok("https://docs.google.com/presentation/d/" + copyPresentationId + "/edit");
    }

    private void addRowToTable(String presentationId, String tableId, String[] cellTexts) throws IOException {
        // Create a new row with cells for each column
        TableRow newRow = new TableRow();
        newRow.setTableCells(Arrays.stream(cellTexts)
                .map(text -> new TableCell()
                        .setText(new TextContent()
                                .setTextElements(Collections.singletonList(new TextElement()
                                        .setTextRun(new TextRun()
                                                .setContent(text))))))
                .collect(Collectors.toList()));

        // Retrieve the table from the presentation
        Table table = getTable(presentationId, tableId);

        // Create a batch update request to add the new row to the table
        BatchUpdatePresentationRequest batchUpdateRequest = new BatchUpdatePresentationRequest()
                .setRequests(Collections.singletonList(new Request()
                        .setInsertTableRows(new InsertTableRowsRequest()
                                .setTableObjectId(tableId)
                                .setInsertBelow(true)
                                .setNumber(1)
                                .setCellLocation(new TableCellLocation()
                                        .setRowIndex(table.getRows() - 1)
                                        .setColumnIndex(0)))));

        // Execute the batch update request
        slidesService.presentations().batchUpdate(presentationId, batchUpdateRequest).execute();

        // Now, you can add the data to the new row using InsertText requests
        int rowNumber = table.getRows();

        List<Request> insertTextRequests = IntStream.range(0, cellTexts.length)
                .mapToObj(index -> new Request()
                        .setInsertText(new InsertTextRequest()
                                .setObjectId(tableId)
                                .setText(cellTexts[index])
                                .setCellLocation(new TableCellLocation()
                                        .setRowIndex(rowNumber)
                                        .setColumnIndex(index))))
                .collect(Collectors.toList());

        // Create another batch update request to add text to the cells
        BatchUpdatePresentationRequest insertTextBatchUpdateRequest = new BatchUpdatePresentationRequest()
                .setRequests(insertTextRequests);

        // Execute the batch update request to add text to the cells
        slidesService.presentations().batchUpdate(presentationId, insertTextBatchUpdateRequest).execute();
    }

    private Table getTable(String presentationId, String tableId) throws IOException {
        Presentations presentations = slidesService.presentations();
        Presentation presentation = presentations.get(presentationId).execute();

        for (Page page : presentation.getSlides()) {
            for (PageElement element : page.getPageElements()) {
                if (element.getObjectId().equals(tableId)) {
                    return element.getTable();
                }
            }
        }

        throw new IllegalArgumentException("Table with ID " + tableId + " not found in presentation.");
    }

    @GetMapping("/TemplateList")
    public ResponseEntity<TemplateInfoResponse[]> getTemplates() throws IOException {
        // Define parameters of request.
        Drive.Files.List listRequest = driveService.files().list();
        listRequest.setQ(
                "mimeType='application/vnd.google-apps.presentation' and name starts with 'STemplate' and trashed = false");
        listRequest.setFields("nextPageToken, files(id, name)");

        // List files.
        List<File> files = listRequest.execute().getFiles();

        List<TemplateInfoResponse> filesInfo = new ArrayList<>();
        for (File file : files) {
            filesInfo.add(new TemplateInfoResponse(file.getId(), file.getName()));
        }

        TemplateInfoResponse[] responseArray = filesInfo.toArray(new TemplateInfoResponse[0]);
        return ResponseEntity.ok(responseArray);
    }

    @GetMapping("/Metadata/{templateId}")
    public ResponseEntity<TemplateMetadata> getMetadata(@PathVariable String templateId) {
        Presentation presentation;
        try {
            presentation = slidesService.presentations().get(templateId).execute();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }

        // TemplateMetadata savedTemplateMetadata =
        // templateService.getTemplateById(templateId);
        // if (savedTemplateMetadata != null) {
        // return ResponseEntity.ok(savedTemplateMetadata);
        // }

        String placeholderPattern = "\\{\\{.[^\\{\\}]{0,}\\}\\}";

        TemplateMetadata templateMetadata = new TemplateMetadata();
        templateMetadata.setId(presentation.getPresentationId());
        templateMetadata.setName(presentation.getTitle());

        for (int i = 0; i < presentation.getSlides().size(); i++) {
            Page slide = presentation.getSlides().get(i);

            SlideMetadata slideMetadata = new SlideMetadata();
            slideMetadata.setId(slide.getObjectId());
            slideMetadata.setIndex(i);
            slideMetadata.setRemovable(true);

            if (slide.getPageElements() != null) {
                for (var element : slide.getPageElements()) {
                    PlaceholderMetadata placeholderMetadata = new PlaceholderMetadata();

                    if (element.getShape() != null && element.getShape().getText() != null &&
                            element.getShape().getText().getTextElements() != null) {

                        String content = element.getShape().getText().getTextElements().stream()
                                .filter(t -> t.getTextRun() != null)
                                .map(t -> t.getTextRun().getContent())
                                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                                .toString()
                                .replace("\n", "");

                        Matcher matchResult = Pattern.compile(placeholderPattern).matcher(content);

                        if (matchResult.find()) {
                            placeholderMetadata.setName(removeMetadataText(content));

                            if (content.contains("IMAGE"))
                                placeholderMetadata.setType("IMAGE");
                            else if (content.contains("CHART"))
                                placeholderMetadata.setType("CHART");
                            else
                                placeholderMetadata.setType("TEXT");

                            placeholderMetadata.setMaxLength(extractNumberFromMax(content));
                            placeholderMetadata.setEditable(content.contains("EDITABLE"));
                            placeholderMetadata.setRemovable(content.contains("REMOVABLE"));

                            slideMetadata.getPlaceholders().add(placeholderMetadata);
                        }
                    }
                }
            }

            templateMetadata.getSlides().add(slideMetadata);
        }

        // templateService.saveTemplate(templateMetadata);

        return ResponseEntity.ok(templateMetadata);
    }

    private String removeMetadataText(String input) {
        int colonIndex = input.indexOf(':');
        if (colonIndex != -1) {
            return input.substring(0, colonIndex) + "}}"; // Add curly brackets at the end
        }
        return input; // If colon not found, return the original input
    }

    private int extractNumberFromMax(String input) {
        Matcher match = Pattern.compile("\\BMAX([0-9]{0,})").matcher(input);

        if (match.find()) {
            String numberString = match.group(1);
            try {
                return Integer.parseInt(numberString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return -1; // Default value if no number found or parsing fails
    }

    @GetMapping("folder/{folderId}")
    public ResponseEntity<FolderMetadata> getMarketingFolders(@PathVariable String folderId) {
        try {
            FolderMetadata folderMetadata = getFolderWithSubfolders(folderId);
            return ResponseEntity.ok(folderMetadata);
        } catch (Exception e) {
            // Handle exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private FolderMetadata getFolderWithSubfolders(String folderId) throws IOException {
        FolderMetadata folderMetadata = new FolderMetadata();

        // Create the request to retrieve the files and folders within the specified
        // folder
        Drive.Files.List request = driveService.files().list()
                .setQ(String.format("'%s' in parents and mimeType = 'application/vnd.google-apps.folder'", folderId))
                .setFields("files(id, name)")
                .setPageSize(1000); // Adjust the page size as needed

        do {
            // Retrieve the files and folders
            FileList fileList = request.execute();

            for (com.google.api.services.drive.model.File folder : fileList.getFiles()) {
                FolderMetadata subfolder = new FolderMetadata();
                subfolder.setId(folder.getId());
                subfolder.setName(folder.getName());

                getAllSlidesInFolder(subfolder);
                // Recursively get subfolders
                subfolder.setSubfolders(getSubfolders(folder.getId()));

                folderMetadata.getSubfolders().add(subfolder);
            }

            request.setPageToken(fileList.getNextPageToken());
        } while (request.getPageToken() != null && !request.getPageToken().isEmpty());

        return folderMetadata;
    }

    private List<FolderMetadata> getSubfolders(String parentFolderId) throws IOException {
        List<FolderMetadata> subfolders = new ArrayList<>();

        Drive.Files.List request = driveService.files().list()
                .setQ(String.format("'%s' in parents and mimeType = 'application/vnd.google-apps.folder'",
                        parentFolderId))
                .setFields("files(id, name)")
                .setPageSize(1000); // Adjust the page size as needed

        do {
            FileList fileList = request.execute();

            for (com.google.api.services.drive.model.File folder : fileList.getFiles()) {
                FolderMetadata subfolder = new FolderMetadata();
                subfolder.setId(folder.getId());
                subfolder.setName(folder.getName());

                getGooglePresentationsInFolder(subfolder);
                // Recursively get subfolders
                subfolder.setSubfolders(getSubfolders(folder.getId()));

                subfolders.add(subfolder);
            }

            request.setPageToken(fileList.getNextPageToken());
        } while (request.getPageToken() != null && !request.getPageToken().isEmpty());

        return subfolders;
    }

    private void getGooglePresentationsInFolder(FolderMetadata folder) throws IOException {
        Drive.Files.List request = driveService.files().list()
                .setQ(String.format("'%s' in parents and mimeType = 'application/vnd.google-apps.presentation'",
                        folder.getId()))
                .setFields("files(*)");
        FileList response = request.execute();
        List<com.google.api.services.drive.model.File> presentations = response.getFiles();

        for (com.google.api.services.drive.model.File presentation : presentations) {
            Presentation slidePresentation = slidesService.presentations().get(presentation.getId()).execute();

            for (int i = 0; i < slidePresentation.getSlides().size(); i++) {
                Page slide = slidePresentation.getSlides().get(i);
                FolderMetadata.SlideItem slideItem = new FolderMetadata.SlideItem();
                slideItem.setSlideId(slide.getObjectId());
                slideItem.setPresentationId(presentation.getId());
                slideItem.setPresentationName(presentation.getName());
                slideItem.setName(getSlideTitle(slidePresentation, slide.getObjectId()));
                slideItem.setThumbnailUrl(
                        String.format("/thumbnails/%s/MEDIUM/%s.png", presentation.getId(), slide.getObjectId()));

                folder.getSlideItems().add(slideItem);
            }
        }
    }

    private void getAllSlidesInFolder(FolderMetadata folder) throws IOException {
        Drive.Files.List request = driveService.files().list()
                .setQ(String.format("'%s' in parents and mimeType = 'application/vnd.google-apps.presentation'",
                        folder.getId()))
                .setFields("files(*)");
        FileList response = request.execute();
        List<com.google.api.services.drive.model.File> presentations = response.getFiles();

        for (com.google.api.services.drive.model.File presentation : presentations) {
            Presentation slidePresentation = slidesService.presentations().get(presentation.getId()).execute();

            for (int i = 0; i < slidePresentation.getSlides().size(); i++) {
                Page slide = slidePresentation.getSlides().get(i);
                FolderMetadata.SlideItem slideItem = new FolderMetadata.SlideItem();
                slideItem.setSlideId(slide.getObjectId());
                slideItem.setPresentationId(presentation.getId());
                slideItem.setPresentationName(presentation.getName());
                slideItem.setName(getSlideTitle(slidePresentation, slide.getObjectId()));
                slideItem.setThumbnailUrl(
                        String.format("/thumbnails/%s/MEDIUM/%s.png", presentation.getId(), slide.getObjectId()));

                folder.getSlideItems().add(slideItem);
            }
        }
    }

    private String getSlideTitle(Presentation presentation, String slideId) {
        // Find the slide with the specified slideId
        Page slide = presentation.getSlides().stream()
                .filter(s -> Objects.equals(s.getObjectId(), slideId))
                .findFirst()
                .orElse(null);

        if (slide != null) {
            // Find the page element of type 'TITLE'
            PageElement titleElement = slide.getPageElements().stream()
                    .filter(element -> element.getShape() != null &&
                            element.getShape().getText() != null &&
                            element.getShape().getText().getTextElements() != null &&
                            element.getShape().getText().getTextElements().stream()
                                    .anyMatch(x -> x.getTextRun() != null && x.getTextRun().getContent() != null))
                    .findFirst()
                    .orElse(null);

            if (titleElement != null) {
                // Return the title of the slide
                return titleElement.getShape().getText().getTextElements().stream()
                        .filter(x -> x.getTextRun() != null && x.getTextRun().getContent() != null)
                        .findFirst()
                        .map(x -> x.getTextRun().getContent().replace("\n", ""))
                        .orElse(null);
            }
        }

        // Slide or title not found
        return null;
    }

    private List<Request> getTextPlaceholdersRequests(Map<String, String> textPlaceholders) {
        List<Request> requests = new ArrayList<>();

        for (Map.Entry<String, String> placeholder : textPlaceholders.entrySet()) {
            requests.add(new Request()
                    .setReplaceAllText(new ReplaceAllTextRequest()
                            .setContainsText(
                                    new SubstringMatchCriteria().setMatchCase(true).setText(placeholder.getKey()))
                            .setReplaceText(placeholder.getValue())));
        }

        return requests;
    }

    private List<Request> getImagePlaceholdersRequests(Map<String, String> imagePlaceholders) {
        List<Request> requests = new ArrayList<>();

        for (Map.Entry<String, String> placeholder : imagePlaceholders.entrySet()) {
            requests.add(new Request()
                    .setReplaceAllShapesWithImage(new ReplaceAllShapesWithImageRequest()
                            .setContainsText(
                                    new SubstringMatchCriteria().setMatchCase(true).setText(placeholder.getKey()))
                            .setImageUrl(placeholder.getValue())
                            .setImageReplaceMethod("CENTER_INSIDE")));
        }

        return requests;
    }

    public List<Request> getChartPlaceholdersRequest(Map<String, ChartInfo> chartPlaceholders) throws IOException {
        List<Request> requests = new ArrayList<>();

        BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateSpreadsheetRequest.setRequests(new ArrayList<>());

        for (Map.Entry<String, ChartInfo> chartPlaceholder : chartPlaceholders.entrySet()) {

            List<ValueRange> valueRanges = new ArrayList<ValueRange>();

            ChartSpec chartSpec = new ChartSpec();
            chartSpec.setTitle(chartPlaceholder.getValue().getTitle());

            if (chartPlaceholder.getValue().getType().equals("PIE")
                    || chartPlaceholder.getValue().getType().equals("DOUGHNUT")) {
                chartSpec.setPieChart(new PieChartSpec());
                chartSpec.getPieChart().setThreeDimensional(false);
                chartSpec.getPieChart().setPieHole(chartPlaceholder.getValue().getType().equals("PIE") ? 0 : 0.5);

                chartSpec.getPieChart().setDomain(new ChartData()
                        .setSourceRange(new ChartSourceRange()
                                .setSources(List.of(new GridRange()
                                        .setEndColumnIndex(1)
                                        .setEndRowIndex(Integer.MAX_VALUE)
                                        .setStartColumnIndex(0)
                                        .setStartRowIndex(0)))));

                chartSpec.getPieChart().setSeries(new ChartData()
                        .setSourceRange(new ChartSourceRange()
                                .setSources(List.of(new GridRange()
                                        .setEndColumnIndex(2)
                                        .setEndRowIndex(Integer.MAX_VALUE)
                                        .setStartColumnIndex(1)
                                        .setStartRowIndex(0)))));

                // Logic for data in Column A
                List<List<Object>> columnDataA = new ArrayList<>();

                Entry<String, String[]> domain = chartPlaceholder.getValue().getDomains().entrySet().iterator().next();

                // add header of column A
                columnDataA.add(List.of(domain.getKey()));

                for (var dataCell : domain.getValue()) {
                    columnDataA.add(List.of(dataCell));
                }

                // Create data range for Column A
                var dataRangeA = new ValueRange()
                        .setRange("A:A")
                        .setValues(columnDataA);

                valueRanges.add(dataRangeA);

                // Logic for data in Column B
                List<List<Object>> columnDataB = new ArrayList<>();

                Entry<String, String[]> series = chartPlaceholder.getValue().getSeries().entrySet().iterator().next();

                // add header of column B
                columnDataB.add(List.of(series.getKey()));

                for (var dataCell : series.getValue()) {
                    columnDataB.add(List.of(dataCell));
                }

                // Create data range for Column B
                var dataRangeB = new ValueRange()
                        .setRange("B:B")
                        .setValues(columnDataB);

                valueRanges.add(dataRangeB);

            } else {
                chartSpec.setBasicChart(new BasicChartSpec());
                chartSpec.getBasicChart().setChartType(chartPlaceholder.getValue().getType());
                chartSpec.getBasicChart().setStackedType(chartPlaceholder.getValue().getStackedType());
                chartSpec.getBasicChart().setHeaderCount(1);
                chartSpec.getBasicChart().setLegendPosition(chartPlaceholder.getValue().getLegendPosition());

                chartSpec.getBasicChart().setAxis(List.of(
                        new BasicChartAxis().setPosition("BOTTOM_AXIS")
                                .setTitle(chartPlaceholder.getValue().getBottomAxisName()),
                        new BasicChartAxis().setPosition("LEFT_AXIS")
                                .setTitle(chartPlaceholder.getValue().getLeftAxisName())));

                // Logic for domains and series
                // Logic for domains
                for (int i = 0; i < chartPlaceholder.getValue().getDomains().size(); i++) {
                    Entry<String, String[]> domain = (Entry<String, String[]>)chartPlaceholder.getValue().getDomains().entrySet().toArray()[i];

                    chartSpec.getBasicChart().setDomains(new ArrayList<>());
                    chartSpec.getBasicChart().getDomains().add(new BasicChartDomain()
                            .setDomain(new ChartData()
                                    .setSourceRange(new ChartSourceRange()
                                            .setSources(List.of(new GridRange()
                                                    .setEndColumnIndex(i + 1)
                                                    .setEndRowIndex(Integer.MAX_VALUE)
                                                    .setStartColumnIndex(0)
                                                    .setStartRowIndex(0))))));

                    // Add data for the domain
                    var columnData = new ArrayList<List<Object>>();

                    // add header of the column
                    columnData.add(List.of((domain).getKey()));

                    for (var dataCell : domain.getValue()) {
                        columnData.add(List.of(dataCell));
                    }

                    // determine the column letter based on the index
                    var columnLetter = getColumnLetter(i);

                    // Create data range for the domain
                    var dataRange = new ValueRange()
                            .setRange(columnLetter + ":" + columnLetter)
                            .setValues(columnData);

                    valueRanges.add(dataRange);
                }

                // Logic for series
                for (int i = 0; i < chartPlaceholder.getValue().getSeries().size(); i++) {
                    Entry<String, String[]> series = (Entry<String, String[]>)chartPlaceholder.getValue().getSeries().entrySet().toArray()[i];

                    chartSpec.getBasicChart().setSeries(new ArrayList<>());
                    chartSpec.getBasicChart().getSeries().add(new BasicChartSeries()
                            .setSeries(new ChartData()
                                    .setSourceRange(new ChartSourceRange()
                                            .setSources(List.of(new GridRange()
                                                    .setEndColumnIndex(
                                                            i + (chartPlaceholder.getValue().getDomains().size()) + 1)
                                                    .setEndRowIndex(Integer.MAX_VALUE)
                                                    .setStartColumnIndex(
                                                            (i + (chartPlaceholder.getValue().getDomains().size()) + 1)
                                                                    - 1)
                                                    .setStartRowIndex(0))))));

                    // Add data for the series
                    var columnData = new ArrayList<List<Object>>();

                    // add header of the column
                    columnData.add(List.of(series.getKey()));

                    for (var dataCell : series.getValue()) {
                        columnData.add(List.of(dataCell));
                    }

                    // determine the column letter based on the index
                    var columnLetter = getColumnLetter(i + chartPlaceholder.getValue().getDomains().size());

                    // Create data range for the series
                    var dataRange = new ValueRange()
                            .setRange(columnLetter + ":" + columnLetter)
                            .setValues(columnData);

                    valueRanges.add(dataRange);
                }

            }

            // Update the values on sheet
            sheetsService.spreadsheets().values()
                    .batchUpdate("1J7sP682rkpLtGiRXcxJVkeCGKhCd8iUNktRomp2iEM0", new BatchUpdateValuesRequest()
                            .setData(valueRanges)
                            .setValueInputOption("USER_ENTERED"))
                    .execute();

            // Logic to add ChartSpec to the batch update spreadsheet request
            // Logic to add ChartSpec to the batch update spreadsheet request
            var addChartRequest = new AddChartRequest()
                    .setChart(new EmbeddedChart()
                            .setSpec(chartSpec)
                            .setPosition(new EmbeddedObjectPosition()
                                    .setOverlayPosition(new OverlayPosition()
                                            .setOffsetXPixels(50)
                                            .setOffsetYPixels(50)
                                            .setWidthPixels(500)
                                            .setHeightPixels(350))));

            // Add the chart request to the batch update spreadsheet request
            batchUpdateSpreadsheetRequest.getRequests().add(new com.google.api.services.sheets.v4.model.Request()
                    .setAddChart(addChartRequest));

            // requests.add(new Request()); // Replace with the actual Request instance
        }

        Sheets.Spreadsheets.BatchUpdate batchUpdate;
        batchUpdate = sheetsService.spreadsheets().batchUpdate("1J7sP682rkpLtGiRXcxJVkeCGKhCd8iUNktRomp2iEM0",
                batchUpdateSpreadsheetRequest);
        BatchUpdateSpreadsheetResponse response = batchUpdate.execute();

        for (int i = 0; i < response.getReplies().size(); i++) {
            Response res = response.getReplies().get(i);

            if (res.getAddChart() != null) {

                requests.add(new Request()
                        .setReplaceAllShapesWithSheetsChart(new ReplaceAllShapesWithSheetsChartRequest()
                                .setSpreadsheetId("1J7sP682rkpLtGiRXcxJVkeCGKhCd8iUNktRomp2iEM0")
                                .setChartId(response.getReplies().get(i).getAddChart().getChart().getChartId())
                                .setLinkingMode("NOT_LINKED_IMAGE")
                                .setContainsText(new SubstringMatchCriteria()
                                        .setMatchCase(true)
                                        .setText(((Map.Entry<String, ChartInfo>) chartPlaceholders.entrySet()
                                                .toArray()[i]).getKey()))));
            }
        }

        return requests;
    }

    private void addMarketingSlides(Map<String, String> marketingPresentationIdsToAdd, String targetPresentationId,
            Map<String, Integer> slidesOrder) {

        for (Map.Entry<String, String> marketingPresentationKeyValue : marketingPresentationIdsToAdd.entrySet()) {
            String url = "https://script.google.com/macros/s/AKfycbzCqVDHp9rXYKHemTzdut0_FuhQP6T4q8SQeI-_b3WO8zXzdXj6mNQemwXpqPpXt_eW/exec?srcId="
                    + marketingPresentationKeyValue.getValue() + "&dstId=" + targetPresentationId + "&srcPage="
                    + marketingPresentationKeyValue.getKey();

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {

                    String newSlideId = response.body();

                    if (slidesOrder.containsKey(marketingPresentationKeyValue.getKey())) {
                        int slideIndex = slidesOrder.get(marketingPresentationKeyValue.getKey());
                        slidesOrder.remove(marketingPresentationKeyValue.getKey());

                        slidesOrder.put(newSlideId, slideIndex);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void reorderSlides(Map<String, Integer> slidesOrder, String copyPresentationId)
            throws IOException {

        List<Request> requests = new ArrayList<>();

        for (Map.Entry<String, Integer> sOrder : slidesOrder.entrySet()) {
            UpdateSlidesPositionRequest request = new UpdateSlidesPositionRequest();
            request.setSlideObjectIds(List.of(sOrder.getKey()));
            request.setInsertionIndex(sOrder.getValue());

            requests.add(new Request().setUpdateSlidesPosition(request));
        }

        BatchUpdatePresentationRequest batchUpdateRequest = new BatchUpdatePresentationRequest();
        batchUpdateRequest.setRequests(requests);

        Slides.Presentations.BatchUpdate updateRequest = slidesService.presentations()
                .batchUpdate(copyPresentationId, batchUpdateRequest);

        updateRequest.execute();
    }

    private List<Request> getRemoveSlidesRequest(String[] slidesToRemoveIds, Presentation slidesPresentation) {
        List<Request> requests = new ArrayList<>();

        for (String slideToRemoveId : slidesToRemoveIds) {
            requests.add(new Request()
                    .setDeleteObject(new DeleteObjectRequest()
                            .setObjectId(slideToRemoveId)));
        }

        return requests;
    }

    private String getColumnLetter(int columnIndex) {
        String columnLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder columnLetter = new StringBuilder();
        while (columnIndex >= 0) {
            int letterIndex = (columnIndex % 26);
            columnLetter.insert(0, columnLetters.charAt(letterIndex));
            columnIndex = (columnIndex / 26) - 1;
        }
        return columnLetter.toString();
    }

}
