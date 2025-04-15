package com.cordestitch.request.review;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {

    private String userId;

    @NotNull(message = "Order ID is mandatory")
    private String orderId;

    @NotNull(message = "Product ID is mandatory")
    private String productId;

    @NotNull(message = "Review ID is mandatory")
    private String reviewId;

    private Integer rating;

    private String comment;

    private String description;

    private List<MultipartFile> images;
    private List<MultipartFile> videos;
}
