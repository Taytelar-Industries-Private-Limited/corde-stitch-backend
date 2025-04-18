package com.cordestitch.controller.homepage;

import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.homepage.HomePageResponse;
import com.cordestitch.service.service.homepage.HomePageService;
import com.cordestitch.service.service.token.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomePageController.class)
class HomePageControllerTest {
    @MockBean
    public HomePageService homePageService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void testUploadHomePageImages() throws Exception{
        String folderName = "folder";
        MockMultipartFile file = new MockMultipartFile("file","image1.jpg", MediaType.IMAGE_JPEG_VALUE,"image content".getBytes());
        String description = "home page of cordestitch";
        when(homePageService.uploadHomePageImages(any(),any(),any())).thenReturn(new SuccessResponse());
        mockMvc.perform(multipart("/api/home/uploadHomePageImages")
                        .file(file)
                .param("folderName",folderName)
                        .param("description",description))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllHomePageImages() throws Exception{
        when(homePageService.getAllHomePageImages()).thenReturn(new HomePageResponse());
        mockMvc.perform(get("/api/home/getAllHomePageImages")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}