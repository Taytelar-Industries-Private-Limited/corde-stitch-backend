package com.cordestitch.service.serviceimplementation.homepage;

import com.cordestitch.entity.homepage.HomePageEntity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.ProductImage;
import com.cordestitch.entity.product.StockQuantity;
import com.cordestitch.entity.product.SubCategory;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.repository.homepage.HomePageRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.product.SubCategoryRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.homepage.HomePageLandscapeImage;
import com.cordestitch.response.homepage.HomePageResponse;
import com.cordestitch.response.homepage.HomePageTrendingProduct;
import com.cordestitch.service.service.homepage.HomePageService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomePageServiceImplementation implements HomePageService {

    private final HomePageRepository homePageRepository;

    private final ProductRepository productRepository;

    private final SubCategoryRepository subCategoryRepository;

    private final S3Client s3Client;

    private final Generator generator;

    private static final String TRENDING = "Trending";

    private static final String HOME_PAGE_COLLECTION = "Home Page Collection";

    @Value("${aws.buckets.productImages}")
    private String bucketName;

    @Transactional
    @Override
    public SuccessResponse uploadHomePageImages(String folderName, MultipartFile file, String description) {
        log.info("Upload HomePage Images request folder : {}, multipart file : {}, description :{}", folderName, file, description);

        String homePageImageUrl = uploadHomePageImageToS3Bucket(folderName, "images", file);
        log.info("HomePage ImageUrl : {}", homePageImageUrl);

        HomePageEntity homePageEntity = new HomePageEntity();
        homePageEntity.setHomePageId(generator.generateId(Constants.HOME_PAGE_ID));
        homePageEntity.setDescription(description);
        homePageEntity.setHomePageImageUrl(homePageImageUrl);
        homePageRepository.save(homePageEntity);
        log.info("Home Page Entity Data : {}", homePageEntity);

        SuccessResponse response = new SuccessResponse(Constants.HOME_PAGE_SUCCESS, HttpStatus.OK.value());
        log.info("Success Response: {}", response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productsCache", key = "'getAllHomePageImages'")
    public HomePageResponse getAllHomePageImages() {
        log.info("Cache miss getAllHomePageImages fetched from db {} ", LocalDateTime.now(ZoneId.of(Constants.ZONE)));

        List<SubCategory> subCategories = subCategoryRepository.findAll();
        List<String> subCategoryNames = subCategories.stream()
                .map(SubCategory::getSubCategoryName)
                .toList();
        log.info("Fetched SubCategory Names: {}", subCategoryNames);


        List<HomePageEntity> homePageEntityList = homePageRepository.findAll();
        log.info("List Of HomePageEntity: {}", homePageEntityList);

        List<HomePageLandscapeImage> homePageSubCategoryImages = new ArrayList<>();
        List<HomePageLandscapeImage> homePageLandscapeImages = new ArrayList<>();

        if (!homePageEntityList.isEmpty()) {
            homePageSubCategoryImages = homePageEntityList.stream()
                    .filter(homePageEntity -> subCategoryNames.contains(homePageEntity.getDescription()))
                    .map(homePageEntity -> new HomePageLandscapeImage(
                            homePageEntity.getDescription(),
                            homePageEntity.getHomePageImageUrl()
                    ))
                    .toList();

            homePageLandscapeImages = homePageEntityList.stream()
                    .filter(homePageEntity -> HOME_PAGE_COLLECTION.contains(homePageEntity.getDescription()))
                    .map(homePageEntity -> new HomePageLandscapeImage(
                            homePageEntity.getDescription(),
                            homePageEntity.getHomePageImageUrl()
                    ))
                    .toList();

        }

        Optional<List<Product>> optionalProducts = productRepository.findByProductStatus(TRENDING);
        log.info("List Of Products: {}", optionalProducts);

        List<HomePageTrendingProduct> homePageTrendingProducts = new ArrayList<>();
        if (optionalProducts.isPresent() && !optionalProducts.get().isEmpty()) {
            homePageTrendingProducts = optionalProducts.get().stream()
                    .map(product -> {

                        String firstColor = product.getProductImages().stream()
                                .map(ProductImage::getColorName)
                                .findFirst()
                                .orElse(null);

                        List<String> imageUrls = product.getProductImages().stream()
                                .filter(productImage -> productImage.getColorName().equalsIgnoreCase(firstColor))
                                .sorted(Comparator.comparingInt(ProductImage::getImagePriority))
                                .map(ProductImage::getImageUrl)
                                .filter(Objects::nonNull)
                                .limit(2)
                                .toList();

                    double price = product.getStockQuantities().stream()
                            .findFirst()
                            .map(StockQuantity::getProductPrice)
                            .orElse(0.0);

                        return new HomePageTrendingProduct(
                                product.getProductName(),
                                product.getProductMaterialType(),
                                imageUrls,
                                price
                        );
                    })
                    .toList();
        }

        HomePageResponse homePageResponse = new HomePageResponse(
                homePageLandscapeImages,
                homePageTrendingProducts,
                homePageSubCategoryImages
        );

        log.info("Home Page Response: {}", homePageResponse);
        return homePageResponse;
    }


    private String uploadHomePageImageToS3Bucket(String folderName, String fileType, MultipartFile file) {
        String slash = "/";
        String fileName = folderName + slash + fileType + slash + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build(),
                    RequestBody.fromInputStream(inputStream, file.getSize()));

            return s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build()).toExternalForm();
        } catch (IOException e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new S3UploadException(Constants.UPLOAD_ERROR);
        }
    }
}
