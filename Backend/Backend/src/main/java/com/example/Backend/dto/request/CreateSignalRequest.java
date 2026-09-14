package com.example.Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class CreateSignalRequest {

    @NotBlank
    @Size(max = 300)
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String category;

    private List<String> tags = new ArrayList<>();

    private List<SourceRequest> sources = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<SourceRequest> getSources() { return sources; }
    public void setSources(List<SourceRequest> sources) { this.sources = sources; }

    public static class SourceRequest {
        private String url;
        private String title;
        private String description;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
