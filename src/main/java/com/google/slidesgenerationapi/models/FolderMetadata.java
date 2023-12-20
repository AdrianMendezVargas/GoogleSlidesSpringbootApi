package com.google.slidesgenerationapi.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FolderMetadata {

    private String id;
    private String name;
    private List<FolderMetadata> subfolders;
    private List<SlideItem> slideItems;

    public FolderMetadata() {
        this.subfolders = new ArrayList<>();
        this.slideItems = new ArrayList<>();
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

    public List<FolderMetadata> getSubfolders() {
        return subfolders;
    }

    public void setSubfolders(List<FolderMetadata> subfolders) {
        this.subfolders = subfolders;
    }

    public List<SlideItem> getSlideItems() {
        return slideItems;
    }

    public void setSlideItems(List<SlideItem> slideItems) {
        this.slideItems = slideItems;
    }

    public static class SlideItem {

        private String id = "m" + UUID.randomUUID().toString();
        private String slideId;
        private String presentationId;
        private String presentationName;
        private String name;
        private String thumbnailUrl;

        public String getId() {
            return id;
        }

        public String getSlideId() {
            return slideId;
        }

        public void setSlideId(String slideId) {
            this.slideId = slideId;
        }

        public String getPresentationId() {
            return presentationId;
        }

        public void setPresentationId(String presentationId) {
            this.presentationId = presentationId;
        }

        public String getPresentationName() {
            return presentationName;
        }

        public void setPresentationName(String presentationName) {
            this.presentationName = presentationName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}

