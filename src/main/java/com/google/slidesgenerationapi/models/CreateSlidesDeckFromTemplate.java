package com.google.slidesgenerationapi.models;

import java.util.Map;

public class CreateSlidesDeckFromTemplate {
    private String templateId;
    private String presentationName;
    private Map<String, String> textPlaceholders;
    private Map<String, String> imagePlaceholders;
    private Map<String, ChartInfo> chartPlaceholders;
    private String[] slidesToRemove;
    private String reciverEmail;
    private Map<String, String> marketingSlidesIdsToAdd = null;
    private Map<String, Integer> slidesOrder;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public Map<String, String> getTextPlaceholders() {
        return textPlaceholders;
    }

    public void setTextPlaceholders(Map<String, String> textPlaceholders) {
        this.textPlaceholders = textPlaceholders;
    }

    public Map<String, String> getImagePlaceholders() {
        return imagePlaceholders;
    }

    public void setImagePlaceholders(Map<String, String> imagePlaceholders) {
        this.imagePlaceholders = imagePlaceholders;
    }

    public Map<String, ChartInfo> getChartPlaceholders() {
        return chartPlaceholders;
    }

    public void setChartPlaceholders(Map<String, ChartInfo> chartPlaceholders) {
        this.chartPlaceholders = chartPlaceholders;
    }

    public String[] getSlidesToRemove() {
        return slidesToRemove;
    }

    public void setSlidesToRemove(String[] slidesToRemove) {
        this.slidesToRemove = slidesToRemove;
    }

    public String getReciverEmail() {
        return reciverEmail;
    }

    public void setReciverEmail(String receiverEmail) {
        this.reciverEmail = receiverEmail;
    }

    public Map<String, String> getMarketingSlidesIdsToAdd() {
        return marketingSlidesIdsToAdd;
    }

    public void setMarketingSlidesIdsToAdd(Map<String, String> marketingSlidesIdsToAdd) {
        this.marketingSlidesIdsToAdd = marketingSlidesIdsToAdd;
    }

    public Map<String, Integer> getSlidesOrder() {
        return slidesOrder;
    }

    public void setSlidesOrder(Map<String, Integer> slidesOrder) {
        this.slidesOrder = slidesOrder;
    }
}

