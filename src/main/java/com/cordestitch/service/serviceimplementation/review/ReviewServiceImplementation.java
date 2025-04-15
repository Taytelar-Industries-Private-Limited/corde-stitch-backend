package com.cordestitch.service.serviceimplementation.review;

import com.cordestitch.entity.review.ProductReview;
import com.cordestitch.entity.review.Review;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.exception.review.UnauthorizedActionException;
import com.cordestitch.repository.review.ReviewRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.review.FilterReviewRequest;
import com.cordestitch.request.review.ReviewDataRequest;
import com.cordestitch.request.review.ReviewRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.review.ProductReviewResponse;
import com.cordestitch.response.review.ReviewResponse;
import com.cordestitch.service.service.review.ReviewService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImplementation implements ReviewService {

    private final ReviewRepository reviewRepository;

    private final UserRepository userRepository;

    private final Generator generator;
    private final ModelMapper modelMapper = new ModelMapper();

    private final S3Client s3Client;
    @Value("${aws.buckets.productReview}")
    private String bucketName;

    @Value("${aws.region}")
    private String awsRegion;

    private static final String IMAGES = "images";
    private static final String VIDEOS = "videos";

    @Transactional
    @Override
    public SuccessResponse createReview(ReviewDataRequest reviewDataRequest) {
        log.info("Create Review Request : {}", reviewDataRequest);

        UserEntity user = getUserEntity(reviewDataRequest.getUserId());

        Optional<ProductReview> optionalProductReview = reviewRepository.findByProductIdAndUserIdAndOrderId(reviewDataRequest.getUserId(), reviewDataRequest.getProductId(), reviewDataRequest.getOrderId());
        if(optionalProductReview.isPresent()) {
            log.info("Product Review Data : {}", optionalProductReview.get());
            log.info(Constants.ALREADY_REVIEW_PROVIDED_FOR_PRODUCT, HttpStatus.BAD_REQUEST.value());
            return new SuccessResponse(Constants.ALREADY_REVIEW_PROVIDED_FOR_PRODUCT, HttpStatus.BAD_REQUEST.value());
        }

        ProductReview productReview = reviewRepository.findByProductId(reviewDataRequest.getProductId());
        if (productReview == null) {
            productReview = new ProductReview();
            productReview.setProductId(reviewDataRequest.getProductId());
        }

        Review review = new Review();
        review.setReviewId(generator.generateId(Constants.REVIEW_ID));
        review.setUserId(reviewDataRequest.getUserId());
        review.setOrderId(reviewDataRequest.getOrderId());
        review.setDescription(reviewDataRequest.getDescription());
        review.setRating(Integer.parseInt(reviewDataRequest.getRating()));
        review.setUserName(user.getFirstName() + " " + user.getLastName());
        review.setComment(reviewDataRequest.getComment());
        review.setCreatedAt(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        review.setImagePaths(new ArrayList<>());
        review.setVideoPaths(new ArrayList<>());


        String productId = reviewDataRequest.getProductId();
        String userId = reviewDataRequest.getUserId();
        String orderId = reviewDataRequest.getOrderId();

        if (reviewDataRequest.getImages() != null && !reviewDataRequest.getImages().isEmpty()) {
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile image : reviewDataRequest.getImages()) {
                String imagePath = uploadFileToS3(image, IMAGES, productId, userId, orderId);
                imagePaths.add(imagePath);
            }
            review.setImagePaths(imagePaths);
        }

        if (reviewDataRequest.getVideos() != null && !reviewDataRequest.getVideos().isEmpty()) {
            List<String> videoPaths = new ArrayList<>();
            for (MultipartFile video : reviewDataRequest.getVideos()) {
                String videoPath = uploadFileToS3(video, VIDEOS, productId, userId, orderId);
                videoPaths.add(videoPath);
            }
            review.setVideoPaths(videoPaths);
        }

        productReview.getReviews().add(review);
        reviewRepository.save(productReview);

        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage(Constants.REVIEW_CREATED_SUCCESSFULLY);
        successResponse.setStatusCode(HttpStatus.OK.value());

        log.info("Create Review Response : {}", successResponse);
        return successResponse;

    }

    @Transactional
    @Override
    public ReviewResponse updateReview(ReviewRequest reviewDataRequest) {
        log.info("Update Review Request : {}", reviewDataRequest);
        ProductReview productReview = reviewRepository.findByProductId(reviewDataRequest.getProductId());
        if (productReview == null) {
            throw new ResourceNotFoundException(Constants.PRODUCT_NOT_FOUND);
        }

        Review review = productReview.getReviews().stream()
                .filter(r -> r.getReviewId().equals(reviewDataRequest.getReviewId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(Constants.REVIEW_NOT_FOUND));

        if (!review.getUserId().equals(reviewDataRequest.getUserId())) {
            throw new UnauthorizedActionException(Constants.UNAUTHORIZED_ACTION);
        }

        String productId = reviewDataRequest.getProductId();
        String userId = reviewDataRequest.getUserId();
        String orderId = reviewDataRequest.getOrderId();

        review.setRating(reviewDataRequest.getRating() != null ? reviewDataRequest.getRating() : review.getRating());
        review.setComment(reviewDataRequest.getComment() != null ? reviewDataRequest.getComment() : review.getComment());
        review.setDescription(reviewDataRequest.getDescription() != null ? reviewDataRequest.getDescription() : review.getDescription());
        review.setImagePaths(reviewDataRequest.getImages()!= null? mapToFilePaths(reviewDataRequest.getImages(), IMAGES, productId, userId, orderId, review) : review.getImagePaths());
        review.setVideoPaths(reviewDataRequest.getVideos()!= null? mapToFilePaths(reviewDataRequest.getVideos(),VIDEOS, productId, userId, orderId, review) : review.getVideoPaths());

        reviewRepository.save(productReview);

        ReviewResponse reviewResponse = modelMapper.map(review, ReviewResponse.class);
        log.info("Update review response : {}", reviewResponse);
        return reviewResponse;
    }

    @Override
    public SuccessResponse deleteReview(String reviewId, String productId, String userId) {
        log.info("Delete Review Request : reviewId={}, productId={}", reviewId, productId);
        ProductReview productReview = reviewRepository.findByProductId(productId);

        if (productReview == null) {
            throw new ResourceNotFoundException(Constants.PRODUCT_NOT_FOUND);
        }

        List<Review> reviews = productReview.getReviews();
        Review reviewToDelete = reviews.stream()
                .filter(review -> review.getReviewId().equals(reviewId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(Constants.REVIEW_NOT_FOUND));

        if (!reviewToDelete.getUserId().equals(userId)) {
            throw new UnauthorizedActionException(Constants.UNAUTHORIZED_ACTION);
        }

        reviews.remove(reviewToDelete);
        reviewRepository.save(productReview);

        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage(Constants.REVIEW_DELETED_SUCCESSFULLY);
        successResponse.setStatusCode(HttpStatus.OK.value());
        log.info("Delete review response : {}", successResponse);
        return successResponse;
    }


    @Override
    public ProductReviewResponse getProductReviews(String productId) {
        ProductReview productReview = reviewRepository.findByProductId(productId);
        log.info("Product Review data : {}", productReview);

        if (isNull(productReview)) {
            return new ProductReviewResponse(productId, new ArrayList<>(), 0.0, 0);
        }
        var response = modelMapper.map(productReview, ProductReviewResponse.class);

        if (productReview.getReviews() != null && !productReview.getReviews().isEmpty()) {
            List<Review> reviews = productReview.getReviews();
            int totalReviews = reviews.size();
            double averageRating = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);

            response.setTotalNumberOfReviews(totalReviews);
            response.setReviewAverage(Math.round(averageRating * 10.0) / 10.0);
        } else {
            response.setTotalNumberOfReviews(0);
            response.setReviewAverage(0.0);
        }

        log.info("Get Product Reviews response : {}", response);
        return response;

    }

    @Override
    public SuccessResponse validateUserPurchase(String userId, String productId) {
        log.info("Validate user purchase request : useId={}, productId={} ", userId, productId);
        UserEntity user = getUserEntity(userId);
        if (user == null) {
            throw new ResourceNotFoundException(Constants.USER_NOT_FOUND);
        }

        log.info("User orders data : {}", user.getOrderEntities());

        boolean hasPurchased = user.getOrderEntities().stream()
                .flatMap(order -> order.getOrderItemEntities().stream())
                .anyMatch(orderItem -> orderItem.getProductId().equals(productId));

        if (hasPurchased) {
            log.info(Constants.USER_PURCHASED_MSG);
            return new SuccessResponse(Constants.USER_PURCHASED_MSG, HttpStatus.OK.value());
        } else {
            log.info(Constants.USER_NOT_PURCHASED_MSG);
            return new SuccessResponse(Constants.USER_NOT_PURCHASED_MSG, HttpStatus.FORBIDDEN.value());
        }
    }

    @Override
    public List<Review> getFilteredReviews(FilterReviewRequest filterReviewRequest) {
        ProductReview productReview = reviewRepository.findByProductId(filterReviewRequest.getProductId());
        if (productReview == null) {
            throw new ResourceNotFoundException(Constants.PRODUCT_NOT_FOUND);
        }
        return  productReview.getReviews().stream()
                .sorted(getComparator(filterReviewRequest.getSortBy(), filterReviewRequest.getSortOrder()))
                .toList();
    }

    @Override
    public ReviewResponse getReviewById(String productId, String orderId, String userId) {
        Optional<ProductReview> productReview = reviewRepository.findByProductIdAndUserIdAndOrderId(productId, userId, orderId);
        if (productReview.isPresent()) {
            Review review = productReview.get().getReviews().stream()
                   .filter(r -> r.getUserId().equals(userId))
                   .findFirst()
                   .orElseThrow(() -> new ResourceNotFoundException(Constants.REVIEW_NOT_FOUND));

            ReviewResponse response = modelMapper.map(review, ReviewResponse.class);
            log.info("Get Review by Id response : {}", response);
            return response;
        } else {
            return new ReviewResponse();
        }
    }

    private Comparator<Review> getComparator(String sortBy, String sortOrder) {
        Comparator<Review> comparator =switch (sortBy) {
            case Constants.MOST_RECENT-> Comparator.comparing(Review::getCreatedAt).reversed();
            case Constants.OLDEST_FIRST-> Comparator.comparing(Review::getCreatedAt);
            case Constants.HIGHEST_RATED-> Comparator.comparing(Review::getRating).reversed();
            case Constants.LOWEST_RATED-> Comparator.comparing(Review::getRating);
            default -> Comparator.comparing(Review::getReviewId, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private UserEntity getUserEntity(String userId) {
        return userRepository.findUserByUserId(userId);
    }

    private String uploadFileToS3(MultipartFile file, String fileType, String productId, String userId, String orderId) {
        String slash = "/";
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()){
            throw new S3UploadException("Invalid File: File name is null or empty.");
        }

        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
        encodedFileName = encodedFileName.replace("+", " ");

        String fileName = productId + slash + userId + slash + orderId + slash + fileType + slash + encodedFileName;
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

    private List<String> mapToFilePaths(List<MultipartFile> files, String fileType, String productId, String userId, String orderId, Review review) {
        List<String> existingFiles = fileType.equals(IMAGES) ? new ArrayList<>(review.getImagePaths()) : new ArrayList<>(review.getVideoPaths());

        List<String> alreadyExistingFiles = new ArrayList<>();
        List<String> removedExistingFiles = new ArrayList<>();

        List<String> requestFilePaths = files != null ? files.stream()
                .map(MultipartFile::getOriginalFilename)
                .filter(fileName -> fileName!=null && !fileName.isEmpty())
                .toList() : new ArrayList<>();

        for (String existingFilePath : existingFiles) {
            String fileName = existingFilePath.substring(existingFilePath.lastIndexOf("/") + 1);

            boolean isAlreadyExisting = requestFilePaths.stream()
                    .anyMatch(fileName::equals);

            if (isAlreadyExisting) {
                alreadyExistingFiles.add(existingFilePath);
            } else {
                removedExistingFiles.add(existingFilePath);
            }
        }

        List<String> newAddedFiles = createNewFiles(files, productId, userId, orderId, alreadyExistingFiles, fileType);


        for (String removedFile : removedExistingFiles) {
            deleteFileFromS3(removedFile);
        }

        List<String> updatedFilePaths = new ArrayList<>(alreadyExistingFiles);
        updatedFilePaths.addAll(newAddedFiles);

        return updatedFilePaths;
    }

    private List<String> createNewFiles(List<MultipartFile> files, String productId, String userId, String orderId, List<String> alreadyExistingFiles, String fileType) {
        List<String> newAddedFiles = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                String originalFilename = file != null ? file.getOriginalFilename() : null;
                if (originalFilename != null && !originalFilename.isEmpty()) {
                    boolean isNewFile = alreadyExistingFiles.stream()
                            .noneMatch(existing -> existing.endsWith(originalFilename));

                    if (isNewFile) {
                        String filePath = uploadFileToS3(file, fileType, productId, userId, orderId);
                        newAddedFiles.add(filePath);
                    }
                }
            }
        }
        return newAddedFiles;
    }

    private void deleteFileFromS3(String removedFile) {
        try {

            String key = extractS3KeyFromUrl(removedFile);

            if (key.isEmpty()) {
                log.error("S3 key is null or empty for the URL: {}", removedFile);
                throw new S3UploadException("Invalid S3 key extracted from URL: " + removedFile);
            }

            String decodeKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
            log.info("Deleting file from S3 with key: {}", decodeKey);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(decodeKey)
                    .build());
            log.info("File successfully deleted from S3. Key: {}", decodeKey);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage(), e);
            throw new S3UploadException(Constants.S3_DELETE_ERROR);
        }
    }

    private String extractS3KeyFromUrl(String removedFile) {
        String bucketBaseUrl = "https://" + bucketName + ".s3." + awsRegion + ".amazonaws.com/";
        if(removedFile.startsWith(bucketBaseUrl)) {
            return removedFile.substring(bucketBaseUrl.length());
        }
        throw new S3UploadException("Invalid S3 URL: " + removedFile);
    }
}

