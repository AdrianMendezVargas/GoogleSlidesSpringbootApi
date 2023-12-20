package com.google.slidesgenerationapi.models;

import java.util.Map;

public class ChartInfo {
    private int id = 0;
    private String type = "COLUMN";
    private String stackedType = "NOT_STACKED";
    private String title = "";
    private String subtitle = "";
    private String leftAxisName;
    private String bottomAxisName;
    private String legendPosition = "BOTTOM_LEGEND";
    private Map<String, String[]> domains;
    private Map<String, String[]> series;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStackedType() {
        return stackedType;
    }

    public void setStackedType(String stackedType) {
        this.stackedType = stackedType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getLeftAxisName() {
        return leftAxisName;
    }

    public void setLeftAxisName(String leftAxisName) {
        this.leftAxisName = leftAxisName;
    }

    public String getBottomAxisName() {
        return bottomAxisName;
    }

    public void setBottomAxisName(String bottomAxisName) {
        this.bottomAxisName = bottomAxisName;
    }

    public String getLegendPosition() {
        return legendPosition;
    }

    public void setLegendPosition(String legendPosition) {
        this.legendPosition = legendPosition;
    }

    public Map<String, String[]> getDomains() {
        return domains;
    }

    public void setDomains(Map<String, String[]> domains) {
        this.domains = domains;
    }

    public Map<String, String[]> getSeries() {
        return series;
    }

    public void setSeries(Map<String, String[]> series) {
        this.series = series;
    }
}

