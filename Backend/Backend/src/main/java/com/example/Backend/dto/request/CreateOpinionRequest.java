package com.example.Backend.dto.request;

import com.example.Backend.enums.OpinionPosition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CreateOpinionRequest {

    @NotNull
    private OpinionPosition position;

    @NotBlank
    private String content;

    private List<SourceRequest> sources = new ArrayList<>();

    public OpinionPosition getPosition() { return position; }
    public void setPosition(OpinionPosition position) { this.position = position; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<SourceRequest> getSources() { return sources; }
    public void setSources(List<SourceRequest> sources) { this.sources = sources; }

    public static class SourceRequest {
        private String url;
        private String title;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
