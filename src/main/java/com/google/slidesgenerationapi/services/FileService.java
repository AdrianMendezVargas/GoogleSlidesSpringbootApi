package com.google.slidesgenerationapi.services;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final ResourceLoader resourceLoader;

    public FileService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // List all files in the static folder
    public List<String> listFilesInStaticFolder() {
    try {
        // Use 'resourceLoader' to load resources
        Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:static/*");

        // Extract file names from resources
        return Arrays.stream(resources)
                .map(resource -> resource.getFilename())
                .collect(Collectors.toList());
    } catch (IOException e) {
        e.printStackTrace();
        // Handle the exception according to your needs
        return List.of("Error listing files in the static folder: " + e.getMessage());
    }
}
}

