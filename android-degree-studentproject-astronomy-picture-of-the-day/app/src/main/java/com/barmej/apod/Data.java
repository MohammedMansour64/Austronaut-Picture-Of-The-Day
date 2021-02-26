package com.barmej.apod;

public class Data {
    private String title;
    private String description;
    private String imageOrVideo;
    private String mediaType;


    public Data(String title, String description, String imageOrVideo, String mediaType) {
        this.title = title;
        this.description = description;
        this.imageOrVideo = imageOrVideo;
        this.mediaType = mediaType;
    }


    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }


    public String getImageOrVideo() {
        return imageOrVideo;
    }

    public void setImageOrVideo(String image) {
        this.imageOrVideo = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
