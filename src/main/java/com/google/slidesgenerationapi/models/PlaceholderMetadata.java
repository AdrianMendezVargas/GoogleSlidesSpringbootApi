package com.google.slidesgenerationapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class PlaceholderMetadata {

    private int id;

    private String name;

    private String type;

    private int maxLength;

    private boolean editable;

    private boolean removable;

    private String bindedTo;

    @JsonIgnore
    private SlideMetadata slideMetadata;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isRemovable() {
        return removable;
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
    }

    public String getBindedTo() {
        return bindedTo;
    }

    public void setBindedTo(String bindedTo) {
        this.bindedTo = bindedTo;
    }

    public SlideMetadata getSlide() {
        return slideMetadata;
    }

    public void setSlide(SlideMetadata slide) {
        this.slideMetadata = slide;
    }
}

