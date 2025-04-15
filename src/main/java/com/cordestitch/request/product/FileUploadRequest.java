package com.cordestitch.request.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadRequest {

    @NotNull(message = "Product Id is required")
    private String productId;

    @NotNull(message = "At least one image must be provided")
    @Size(min = 1, message = "At least one image file and priority is required")
    private List<MultipartFile> images;

    @NotNull(message = "Image priorities are required")
    private Map<String, Integer> imagePriorities;

    @NotNull(message = "Video file is required")
    private MultipartFile video;
}
