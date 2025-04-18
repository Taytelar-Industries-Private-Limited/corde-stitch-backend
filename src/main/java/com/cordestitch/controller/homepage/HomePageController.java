package com.cordestitch.controller.homepage;

import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.homepage.HomePageResponse;
import com.cordestitch.service.service.homepage.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomePageController {

    private final HomePageService homePageService;

    @PostMapping("/uploadHomePageImages")
    public ResponseEntity<SuccessResponse> uploadHomePageImages(@RequestParam("folderName") String folderName,
                                                                @RequestParam("file") MultipartFile file,
                                                                @RequestParam("description") String description) {
        SuccessResponse response = homePageService.uploadHomePageImages(folderName, file, description);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getAllHomePageImages")
    public ResponseEntity<HomePageResponse> getAllHomePageImages() {
        HomePageResponse response = homePageService.getAllHomePageImages();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
