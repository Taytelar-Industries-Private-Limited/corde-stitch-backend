package com.cordestitch.service.service.homepage;

import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.homepage.HomePageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface HomePageService {
    SuccessResponse uploadHomePageImages(String folderName, MultipartFile file, String description);

    HomePageResponse getAllHomePageImages();
}
