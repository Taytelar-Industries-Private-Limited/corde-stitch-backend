package com.cordestitch.request.review;

import com.cordestitch.validation.reviews.ValidRating;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDataRequest {
    private String userId;

    @NotNull(message = "Order ID is mandatory")
    private String orderId;

    @NotNull(message = "Product ID is mandatory")
    private String productId;

    @NotNull(message = "Rating is mandatory")
    @ValidRating(message = "Rating must be between 1 and 5 and be a valid number")
    private String rating;

    @NotNull(message = "Comment is mandatory")
    private String comment;

    private String description;

    private List<MultipartFile> images;
    private List<MultipartFile> videos;
}
