package com.google.slidesgenerationapi.models;

public class TemplateInfoResponse {
    private String id;
    private String name;

    public TemplateInfoResponse(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Agrega getters y setters según sea necesario
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
