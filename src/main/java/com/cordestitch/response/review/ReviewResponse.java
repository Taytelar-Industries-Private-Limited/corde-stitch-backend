package com.cordestitch.response.review;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String reviewId;
    private String userName;
    private Integer rating;
    private String comment;
    private String description;
    private List<String> imagePaths;
    private List<String> videoPaths;
    private LocalDateTime createdAt;
}
