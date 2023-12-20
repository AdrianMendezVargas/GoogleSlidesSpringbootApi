package com.google.slidesgenerationapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class SlideMetadata {

    private String id;

    private int index;

    private boolean removable;

    @JsonIgnore
    private TemplateMetadata templateMetadata;

    private List<PlaceholderMetadata> placeholders;

    public SlideMetadata() {
        placeholders = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isRemovable() {
        return removable;
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
    }

    public TemplateMetadata getTemplate() {
        return templateMetadata;
    }

    public void setTemplate(TemplateMetadata template) {
        this.templateMetadata = template;
    }

    public List<PlaceholderMetadata> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(List<PlaceholderMetadata> placeholders) {
        this.placeholders = placeholders;
    }
}
