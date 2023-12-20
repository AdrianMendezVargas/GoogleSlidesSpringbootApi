package com.google.slidesgenerationapi.models;

import java.util.List;


import java.util.ArrayList;

public class TemplateMetadata {

    private String id;

    private String name;

    private List<SlideMetadata> slides;

    public TemplateMetadata() {
        slides = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SlideMetadata> getSlides() {
        return slides;
    }

    public void setSlides(List<SlideMetadata> slides) {
        this.slides = slides;
    }
}
