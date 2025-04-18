package com.cordestitch.service.serviceimplementation.product;

import com.cordestitch.entity.product.*;
import com.cordestitch.exception.order.CategoryNotFoundException;
import com.cordestitch.exception.order.SubCategoryNotFoundException;
import com.cordestitch.exception.product.ProductNotFoundException;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.repository.product.CategoryRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.product.SubCategoryRepository;
import com.cordestitch.request.product.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.product.*;
import com.cordestitch.response.review.ProductReviewResponse;
import com.cordestitch.service.service.product.ProductService;
import com.cordestitch.service.service.review.ReviewService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImplementation implements ProductService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final Generator generator;
    private final ReviewService reviewService;
    private final CacheManager cacheManager;

    @PersistenceContext
    private final EntityManager entityManager;

    private static final String PRODUCTS_CACHE_NAME = "productsCache";
    private static final String PRODUCT_CACHE_KEY = "listAllProduct";

    private static final String PRODUCT_TYPE = "PRODUCT TYPE";
    private static final String FABRIC_TYPE = "FABRIC TYPE";
    private static final String STRETCH = "STRETCH";
    private static final String SIZE = "SIZE";
    private static final String COLORS = "COLOR";

    @Value("${aws.buckets.productImages}")
    private String bucketName;

    @Transactional
    @Override
    public AddProductResponse addProduct(AddProductRequest request) {
        log.info("Add product request : {}", request);

        Category category = getOrCreateCategory(request.getCategoryName(), request.getCategoryDescription());
        SubCategory subCategory = getOrCreateSubCategory(request.getSubCategoryName(), request.getSubCategoryDescription(), category);

        Product product = createProduct(request, subCategory);
        log.info("Product : {}", product);

        product = entityManager.merge(product);
        for (StockQuantity stockQuantity : product.getStockQuantities()) {
            stockQuantity = entityManager.merge(stockQuantity);
            for (ColorQuantity colorQuantity : stockQuantity.getColorQuantities()) {
                entityManager.merge(colorQuantity);
            }
        }
        productRepository.save(product);

        return new AddProductResponse(product.getProductId(), Constants.PRODUCT_ADDED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse uploadProductFiles(String productId, String colorName, MultipartFile[] images, Integer[] imagePriorities, MultipartFile video) {
        Optional<Product> productOptional = productRepository.findByProductId(productId);
        if (productOptional.isEmpty()) {
            throw new ProductNotFoundException(Constants.PRODUCT_NOT_FOUND);
        }

        Product product = productOptional.get();
        List<ProductImage> productImages  = uploadFilesToS3(imagePriorities, images, productId, colorName, product);
        String videoUrl = uploadFileToS3(video, "video", productId);
        log.info("Image URLs: {} and Video URL: {}", productImages, videoUrl);

        if (product.getProductImages() == null) {
            product.setProductImages(new ArrayList<>());
        }
        product.getProductImages().addAll(productImages);
        product.setVideoUrl(videoUrl);

        productRepository.save(product);

        log.info("Images URL: {} and Video URL: {}", productImages.stream().map(ProductImage::getImageUrl).toList(), videoUrl);
        return new SuccessResponse(Constants.IMAGES_AND_VIDEO_ADDED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    public ProductDataResponse getProductByProductId(String productId) {

        Cache cache = cacheManager.getCache(PRODUCTS_CACHE_NAME);
        if (!isNull(cache)) {

            ProductResponse cachedProductResponse = cache.get(PRODUCT_CACHE_KEY, ProductResponse.class);

            if (!isNull(cachedProductResponse)) {
                Optional<ProductDataResponse> optionalProduct = cachedProductResponse.getCategoryResponses().stream()
                        .flatMap(category -> category.getSubCategoryResponses().stream())
                        .flatMap(subCategory -> subCategory.getProductDataResponses().stream())
                        .filter(product -> product.getProductId().equals(productId))
                        .findFirst();
                if (optionalProduct.isPresent())
                    return convertCacheDataToResponse(optionalProduct.get());
            }
        }
        log.info("Cache miss getProductByProductId fetched from db {} ", LocalDateTime.now(ZoneId.of(Constants.ZONE)));

        return productRepository.findByProductId(productId)
                .map(this::convertProductToResponse)
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found", productId);
                    return new ProductNotFoundException(Constants.PRODUCT_NOT_FOUND);
                });
    }

    @Transactional
    @Override
    public SuccessResponse updateProduct(UpdateProductRequest request) {
        Optional<Category> categoryOptional = categoryRepository.findByCategoryId(request.getCategoryId());
        if (categoryOptional.isEmpty()) {
            throw new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND + request.getAddProductRequest().getCategoryName());
        }
        Category category = categoryOptional.get();
        category.setCategoryName(request.getAddProductRequest().getCategoryName());
        String categoryDescription = request.getAddProductRequest().getCategoryDescription();
        if (categoryDescription != null && !categoryDescription.isBlank()) {
            category.setCategoryDescription(categoryDescription);
        }
        categoryRepository.save(category);

        Optional<SubCategory> subCategoryOptional = subCategoryRepository.findBySubCategoryId(request.getSubCategoryId());
        if (subCategoryOptional.isEmpty()) {
            throw new SubCategoryNotFoundException(Constants.SUBCATEGORY_NOT_FOUND + request.getAddProductRequest().getSubCategoryName());
        }
        SubCategory subCategory = subCategoryOptional.get();
        String subCategoryDescription = request.getAddProductRequest().getSubCategoryDescription();
        if (subCategoryDescription != null && !subCategoryDescription.isBlank()) {
            subCategory.setSubCategoryDescription(subCategoryDescription);
        }
        subCategory.setSubCategoryDescription(request.getAddProductRequest().getSubCategoryDescription());
        subCategory.setCategory(category);
        subCategoryRepository.save(subCategory);

        Optional<Product> productOptional = productRepository.findByProductId(request.getProductId());
        if (productOptional.isEmpty()) {
            throw new ProductNotFoundException(Constants.PRODUCT_NOT_FOUND);
        }

        Product product = productOptional.get();
        product.setProductName(request.getAddProductRequest().getProductName());
        product.setProductDescription(request.getAddProductRequest().getProductDescription());
        product.setProductStatus(request.getAddProductRequest().getProductStatus());
        product.setProductMaterialType(request.getAddProductRequest().getProductMaterialType());
        product.setProductPattern(request.getAddProductRequest().getProductPattern());
        product.setProductStretchType(request.getAddProductRequest().getProductStretchType());
        product.setProductOfferPercentage(request.getAddProductRequest().getProductOfferPercentage());
        updateOrAddStockQuantities(product, product.getStockQuantities(), request.getAddProductRequest().getStockQuantities());

        product.setSubCategory(subCategory);
        log.info("Product: {}", product);
        productRepository.save(product);

        return new SuccessResponse(Constants.PRODUCT_UPDATED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    @Cacheable(value = "productsCache", key = "'listAllProduct'")
    @Transactional
    public ProductResponse getAllProducts() {

        log.info("Cache miss getAllProducts fetched from db {} ", LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        List<Category> categories = categoryRepository.findAll();
        log.info("Category Data : {}", categories);

        List<CategoryResponse> categoryResponses = categories.stream()
                .map(this::convertCategoryToResponse)
                .toList();

        log.info("Category Data Response : {}", categoryResponses);
        return new ProductResponse(categoryResponses);
    }

    @Override
    public ProductResponse filterProductData(ProductFilterRequest request) {

        request.convertEmptyToNull();
        List<ProductFilterResponse> filteredProducts = productRepository.findProductsByFilters(
                request.getProductStretchType(),
                request.getProductSubCategory(),
                request.getProductMaterialType(),
                request.getSize(),
                request.getColorCode()
        );

        List<CategoryResponse> categoryResponses = filteredProducts.stream()
                .map(ProductFilterResponse::getCategory)
                .distinct()
                .map(category -> mapToCategoryResponse(category, filteredProducts))
                .toList();

        return new ProductResponse(categoryResponses);
    }

    @Override
    @Cacheable(value = "productsCache", key = "'getAllFilterCondition'")
    public List<FilterConditionResponse> getAllFilterCondition() {

        log.info("Cache miss getAllFilterCondition fetched from db {} ", LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        List<FilterTypesResponse> filterTypes = productRepository.fetchProductFilterTypes();

        List<String> fieldOrder = Arrays.asList(
                PRODUCT_TYPE,
                FABRIC_TYPE,
                STRETCH,
                SIZE
        );

        List<FilterConditionResponse> filtersResponse = new ArrayList<>();

        Map<String, Function<FilterTypesResponse, String>> fieldMappings = getFieldMappings();

        fieldOrder.forEach(fieldName -> {
            Function<FilterTypesResponse, String> getter = fieldMappings.get(fieldName);
            if (!isNull(getter)) {
                List<String> options = getDistinctValues(filterTypes, getter);
                filtersResponse.add(new FilterConditionResponse(fieldName,getFieldTitle(fieldName),options));
            }
        });

        List<FilterItem> colorItems = filterTypes.stream()
                .map(response -> new FilterItem(response.getColorCode(), response.getColor()))
                .filter(item -> !(isNull(item.getColorCode())) && !(isNull(item.getColorName())))
                .distinct()
                .toList();
        filtersResponse.add(new FilterConditionResponse(COLORS, COLORS.toLowerCase(),new ArrayList<>(), colorItems));

        log.info("Product Filter Conditions : {}", filtersResponse);
        return filtersResponse;
    }

    @Override
    public List<FilterDataResponse> getProductFilterData(String productId) {
        ProductDataResponse response = getProductByProductId(productId);
        if (response == null || response.getStockQuantityResponseList() == null || response.getStockQuantityResponseList().isEmpty()) {
            return Collections.emptyList();
        }

        List<FilterDataResponse> filterResponse = new ArrayList<>();
        for (StockQuantityResponse stock : response.getStockQuantityResponseList()) {
            filterResponse.add(createFilterDataResponse(stock, response.getProductImageResponses()));
        }
        return filterResponse;
    }

    private Map<String, Function<FilterTypesResponse, String>> getFieldMappings() {
        return Map.of(
                PRODUCT_TYPE, FilterTypesResponse::getSubCategoryName,
                FABRIC_TYPE, FilterTypesResponse::getProductMaterialType,
                STRETCH, FilterTypesResponse::getProductStretchType,
                SIZE, filterType -> String.valueOf(filterType.getSize())
        );
    }

    private <T> List<T> getDistinctValues(List<FilterTypesResponse> response, Function<FilterTypesResponse, T> mapper) {
        return response.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private String getFieldTitle(String key) {
        return switch (key) {
            case PRODUCT_TYPE -> "productSubCategory";
            case FABRIC_TYPE -> "productMaterialType";
            case STRETCH -> "productStretchType";
            case SIZE -> "size";
            default -> key.toUpperCase();
        };
    }

    private Category getOrCreateCategory(String categoryName, String categoryDescription) {
        Optional<Category> optionalCategory = categoryRepository.findByCategoryName(categoryName);

        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.get();
            log.info("Category already exists: {}", category);
            return category;
        } else {
            Category newCategory = new Category();
            newCategory.setCategoryId(generator.generateId(Constants.CATEGORY_ID));
            newCategory.setCategoryName(categoryName);
            newCategory.setCategoryDescription(categoryDescription);
            categoryRepository.save(newCategory);
            log.info("Created new Category: {}", newCategory);
            return newCategory;
        }
    }

    private SubCategory getOrCreateSubCategory(String subCategoryName, String subCategoryDescription, Category category) {
        Optional<SubCategory> optionalSubCategory = subCategoryRepository.findBySubCategoryName(subCategoryName);
        if (optionalSubCategory.isPresent()) {
            SubCategory subCategory = optionalSubCategory.get();
            log.info("SubCategory already exist: {}", subCategory);
            return subCategory;

        } else {
            SubCategory newSubCategory = new SubCategory();
            newSubCategory.setSubCategoryId(generator.generateId(Constants.SUB_CATEGORY_ID));
            newSubCategory.setSubCategoryName(subCategoryName);
            newSubCategory.setSubCategoryDescription(subCategoryDescription);
            newSubCategory.setCategory(category);
            subCategoryRepository.save(newSubCategory);
            log.info("SubCategory: {}", newSubCategory);
            return newSubCategory;
        }
    }

    private Product createProduct(AddProductRequest request, SubCategory subCategory) {
        log.info("Product Request :{}", request);
        Product product = new Product();
        product.setProductId(generator.generateId(Constants.PRODUCT_ID));
        product.setProductName(request.getProductName());
        product.setProductStatus(request.getProductStatus());
        product.setProductDescription(request.getProductDescription());
        product.setProductMaterialType(request.getProductMaterialType());
        product.setProductPattern(request.getProductPattern());
        product.setProductStretchType(request.getProductStretchType());
        product.setProductOfferPercentage(request.getProductOfferPercentage());
        product.setStockQuantities(mapStockQuantities(request.getStockQuantities(), product));
        product.setSubCategory(subCategory);
        return product;
    }

    private List<StockQuantity> mapStockQuantities(List<StockQuantityRequest> stockQuantities, Product product) {
        List<StockQuantity> result = new ArrayList<>();
        for (StockQuantityRequest stockQuantity : stockQuantities) {
            StockQuantity quantity = new StockQuantity();
            quantity.setStockId(generator.generateId(Constants.STOCK_ID));
            quantity.setSize(stockQuantity.getSize());
            quantity.setProductPrice(stockQuantity.getProductPrice());
            quantity.setProduct(product);
            quantity.setColorQuantities(mapColorQuantities(stockQuantity.getColorQuantities(), quantity));
            result.add(quantity);
        }
        return result;
    }

    private List<ColorQuantity> mapColorQuantities(List<ColorQuantityRequest> colorQuantities, StockQuantity quantity) {
        List<ColorQuantity> result = new ArrayList<>();
        for (ColorQuantityRequest colorQuantity : colorQuantities) {
            ColorQuantity quantity1 = new ColorQuantity();
            quantity1.setColorQuantityId(generator.generateId(Constants.COLOR_QUANTITY_ID));
            quantity1.setColor(colorQuantity.getColor());
            quantity1.setColorCode(colorQuantity.getColorCode());
            quantity1.setQuantity(colorQuantity.getQuantity());
            quantity1.setStockQuantity(quantity);
            result.add(quantity1);
        }
        return result;
    }

    private List<ProductImage> uploadFilesToS3(Integer[] imagePriorities, MultipartFile[] imageFiles, String productId, String colorName, Product product) {
        List<ProductImage> productImages = new ArrayList<>();

        for (int i = 0; i < imageFiles.length; i++) {
            MultipartFile file = imageFiles[i];
            Integer priority = imagePriorities[i];

            String fileUrl = uploadFileToS3(file, "images", productId, colorName);
            ProductImage productImage = new ProductImage();
            productImage.setProductImageId(generator.generateId(Constants.PRODUCT_IMAGE_ID));
            productImage.setColorName(colorName);
            productImage.setImageUrl(fileUrl);
            productImage.setImagePriority(priority);
            productImage.setProduct(product);

            productImages.add(productImage);
        }
        log.info("Uploaded images for color {}: {}", colorName, productImages);
        return productImages;
    }


    private String uploadFileToS3(MultipartFile file, String fileType, String productId) {
        String slash = "/";
        String fileName = productId + slash + fileType + slash + file.getOriginalFilename();
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

    private CategoryResponse convertCategoryToResponse(Category category) {
        List<SubCategory> subCategories = subCategoryRepository.findByCategory(category);
        log.info("SubCategory Data : {}", subCategories);

        List<SubCategoryResponse> subCategoryResponses = subCategories.stream()
                .map(this::convertSubCategoryToResponse)
                .toList();

        log.info("SubCategory Data Response : {}", subCategoryResponses);
        return new CategoryResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryDescription(),
                subCategoryResponses
        );
    }

    private SubCategoryResponse convertSubCategoryToResponse(SubCategory subCategory) {
        List<Product> products = productRepository.findBySubCategory(subCategory);
        log.info("Product Data : {}", products);

        List<ProductDataResponse> productDataResponses = products.stream()
                .map(this::convertProductToResponse)
                .toList();

        log.info("Product Data Response : {}", productDataResponses);
        return new SubCategoryResponse(
                subCategory.getSubCategoryId(),
                subCategory.getSubCategoryName(),
                subCategory.getSubCategoryDescription(),
                productDataResponses
        );
    }

    private ProductDataResponse convertProductToResponse(Product product) {
        ProductReviewResponse productReviewResponse = getProductReviews(product.getProductId());
        List<StockQuantityResponse> stockQuantityResponses = Collections.emptyList();
        if (product.getStockQuantities() != null) {
            stockQuantityResponses = product.getStockQuantities().stream()
                    .sorted(Comparator.comparing(StockQuantity::getSize))
                    .map(stock -> {
                        List<ColorQuantityResponse> colorQuantityResponses = Collections.emptyList();
                        if (stock.getColorQuantities() != null) {
                            colorQuantityResponses = stock.getColorQuantities().stream()
                                    .sorted(Comparator.comparing(ColorQuantity::getColor))
                                    .map(colorQuantity -> {
                                        Map<String, Integer> imageMap = Collections.emptyMap();
                                        if (product.getProductImages() != null) {
                                            imageMap = product.getProductImages().stream()
                                                    .filter(image -> image.getColorName().equals(colorQuantity.getColor()))
                                                    .filter(image -> image.getImagePriority() != null)
                                                    .sorted(Comparator.comparing(
                                                            ProductImage::getImagePriority,
                                                            Comparator.nullsLast(Comparator.naturalOrder())
                                                    ))
                                                    .collect(Collectors.toMap(
                                                            ProductImage::getImageUrl,
                                                            ProductImage::getImagePriority,
                                                            (existing, replacement) -> existing
                                                    ));
                                        }
                                        return new ColorQuantityResponse(
                                                colorQuantity.getColor(),
                                                colorQuantity.getColorCode(),
                                                colorQuantity.getQuantity(),
                                                imageMap
                                        );
                                    })
                                    .toList();
                        }
                        return new StockQuantityResponse(
                                stock.getSize(),
                                stock.getProductPrice(),
                                colorQuantityResponses
                        );
                    })
                    .toList();
        }

        List<ProductImageResponse> productImageResponses = product.getProductImages() != null
                ? product.getProductImages().stream()
                .sorted(Comparator.comparing(ProductImage::getColorName))
                .map(productImage -> new ProductImageResponse(
                        productImage.getColorName(),
                        productImage.getImageUrl(),
                        productImage.getImagePriority()
                ))
                .toList()
                : Collections.emptyList();

        return new ProductDataResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductStatus(),
                product.getProductDescription(),
                product.getProductMaterialType(),
                product.getProductPattern(),
                product.getProductStretchType(),
                product.getProductOfferPercentage(),
                stockQuantityResponses,
                productImageResponses,
                product.getVideoUrl(),
                productReviewResponse
        );
    }

    private ProductDataResponse convertCacheDataToResponse(ProductDataResponse product) {
        ProductReviewResponse productReviewResponse = getProductReviews(product.getProductId());
        List<StockQuantityResponse> stockQuantityResponses = Collections.emptyList();
        if (product.getStockQuantityResponseList() != null) {
            stockQuantityResponses = product.getStockQuantityResponseList().stream()
                    .sorted(Comparator.comparing(StockQuantityResponse::getSize))
                    .map(stock -> {
                        List<ColorQuantityResponse> colorQuantityResponses = Collections.emptyList();
                        if (stock.getColorQuantityResponses() != null) {
                            colorQuantityResponses = stock.getColorQuantityResponses().stream()
                                    .sorted(Comparator.comparing(ColorQuantityResponse::getColor))
                                    .map(colorQuantity -> {
                                        Map<String, Integer> imageMap = Collections.emptyMap();
                                        if (product.getProductImageResponses() != null) {
                                            imageMap = product.getProductImageResponses().stream()
                                                    .filter(image -> image.getColorName().equals(colorQuantity.getColor()))
                                                    .filter(image -> image.getImagePriority() != null)
                                                    .sorted(Comparator.comparing(
                                                            ProductImageResponse::getImagePriority,
                                                            Comparator.nullsLast(Comparator.naturalOrder())
                                                    ))
                                                    .collect(Collectors.toMap(
                                                            ProductImageResponse::getImageUrl,
                                                            ProductImageResponse::getImagePriority,
                                                            (existing, replacement) -> existing
                                                    ));
                                        }
                                        return new ColorQuantityResponse(
                                                colorQuantity.getColor(),
                                                colorQuantity.getColorCode(),
                                                colorQuantity.getQuantity(),
                                                imageMap
                                        );
                                    })
                                    .toList();
                        }
                        return new StockQuantityResponse(
                                stock.getSize(),
                                stock.getProductPrice(),
                                colorQuantityResponses
                        );
                    })
                    .toList();
        }

        return new ProductDataResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductStatus(),
                product.getProductDescription(),
                product.getProductMaterialType(),
                product.getProductPattern(),
                product.getProductStretchType(),
                product.getProductOfferPercentage(),
                stockQuantityResponses,
                new ArrayList<>(),
                product.getVideo(),
                productReviewResponse
        );
    }

    private void updateOrAddStockQuantities(Product product, List<StockQuantity> existingStockQuantities, List<StockQuantityRequest> stockQuantityRequests) {
        existingStockQuantities.removeIf(existingStockQuantity ->
                stockQuantityRequests.stream()
                        .noneMatch(stockQuantityRequest -> stockQuantityRequest.getSize().equals(existingStockQuantity.getSize()))
        );

        for (StockQuantityRequest stockQuantityRequest : stockQuantityRequests) {
            Optional<StockQuantity> existingStockQuantityOpt = existingStockQuantities.stream()
                    .filter(stockQuantity -> stockQuantity.getSize().equals(stockQuantityRequest.getSize()))
                    .findFirst();

            if (existingStockQuantityOpt.isPresent()) {
                StockQuantity existingStockQuantity = existingStockQuantityOpt.get();
                existingStockQuantity.setProductPrice(stockQuantityRequest.getProductPrice());

                updateOrAddColorQuantities(existingStockQuantity.getColorQuantities(), stockQuantityRequest.getColorQuantities(), existingStockQuantity);
            } else {
                StockQuantity newStockQuantity = new StockQuantity();
                newStockQuantity.setStockId(generator.generateId(Constants.STOCK_ID));
                newStockQuantity.setSize(stockQuantityRequest.getSize());
                newStockQuantity.setProductPrice(stockQuantityRequest.getProductPrice());

                List<ColorQuantity> newColorQuantities = new ArrayList<>();
                for (ColorQuantityRequest colorQuantityRequest : stockQuantityRequest.getColorQuantities()) {
                    ColorQuantity newColorQuantity = getNewColorQuantity(colorQuantityRequest, newStockQuantity);
                    newColorQuantities.add(newColorQuantity);
                }
                newStockQuantity.setColorQuantities(newColorQuantities);
                newStockQuantity.setProduct(product);

                existingStockQuantities.add(newStockQuantity);
            }
        }
    }


    private void updateOrAddColorQuantities(List<ColorQuantity> existingColorQuantities, List<ColorQuantityRequest> colorQuantityRequests, StockQuantity existingStockQuantity) {
        existingColorQuantities.removeIf(existingColorQuantity ->
                colorQuantityRequests.stream()
                        .noneMatch(colorQuantityRequest -> colorQuantityRequest.getColor().equals(existingColorQuantity.getColor()))
        );

        for (ColorQuantityRequest colorQuantityRequest : colorQuantityRequests) {
            Optional<ColorQuantity> existingColorQuantityOpt = existingColorQuantities.stream()
                    .filter(colorQuantity -> colorQuantity.getColor().equals(colorQuantityRequest.getColor()))
                    .findFirst();

            if (existingColorQuantityOpt.isPresent()) {
                ColorQuantity existingColorQuantity = existingColorQuantityOpt.get();
                existingColorQuantity.setColorCode(colorQuantityRequest.getColorCode());
                existingColorQuantity.setQuantity(existingColorQuantity.getQuantity() + colorQuantityRequest.getQuantity());
            } else {
                ColorQuantity newColorQuantity = getNewColorQuantity(colorQuantityRequest, existingStockQuantity);
                existingColorQuantities.add(newColorQuantity);
            }
        }
    }

    private ColorQuantity getNewColorQuantity(ColorQuantityRequest colorQuantityRequest, StockQuantity stockQuantity) {
        ColorQuantity newColorQuantity = new ColorQuantity();
        newColorQuantity.setColorQuantityId(generator.generateId(Constants.COLOR_QUANTITY_ID));
        newColorQuantity.setColor(colorQuantityRequest.getColor());
        newColorQuantity.setColorCode(colorQuantityRequest.getColorCode());
        newColorQuantity.setQuantity(colorQuantityRequest.getQuantity());
        newColorQuantity.setStockQuantity(stockQuantity);
        return newColorQuantity;
    }

    private CategoryResponse mapToCategoryResponse(Category category, List<ProductFilterResponse> filterResponses) {
        List<ProductFilterResponse> categoryProducts = filterResponses.stream()
                .filter(dto -> dto.getCategory().getCategoryId().equals(category.getCategoryId()))
                .toList();

        List<SubCategoryResponse> subCategoryResponses = categoryProducts.stream()
                .map(ProductFilterResponse::getSubCategory)
                .distinct()
                .map(subCategory -> mapToSubCategoryResponse(subCategory, categoryProducts))
                .toList();

        return new CategoryResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryDescription(),
                subCategoryResponses
        );
    }

    private SubCategoryResponse mapToSubCategoryResponse(SubCategory subCategory, List<ProductFilterResponse> categoryProducts) {
        List<ProductFilterResponse> subCategoryProducts = categoryProducts.stream()
                .filter(dto -> dto.getSubCategory().getSubCategoryId().equals(subCategory.getSubCategoryId()))
                .toList();

        List<ProductDataResponse> productResponses = subCategoryProducts.stream()
                .map(ProductFilterResponse::getProduct)
                .distinct()
                .map(product -> mapToProductDataResponse(product, subCategoryProducts))
                .toList();

        return new SubCategoryResponse(
                subCategory.getSubCategoryId(),
                subCategory.getSubCategoryName(),
                subCategory.getSubCategoryDescription(),
                productResponses
        );
    }

    private ProductDataResponse mapToProductDataResponse(Product product, List<ProductFilterResponse> subCategoryProducts) {
        List<ProductFilterResponse> productFilterResponses = subCategoryProducts.stream()
                .filter(dto -> dto.getProduct().getProductId().equals(product.getProductId()))
                .toList();

        ProductReviewResponse productReviewResponse = getProductReviews(product.getProductId());

        List<ProductImageResponse> productImageResponses = !isNull(product.getProductImages())
                ? product.getProductImages().stream()
                .map(productImage -> new ProductImageResponse(
                        productImage.getColorName(),
                        productImage.getImageUrl(),
                        productImage.getImagePriority()
                ))
                .toList()
                : Collections.emptyList();

        return new ProductDataResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductStatus(),
                product.getProductDescription(),
                product.getProductMaterialType(),
                product.getProductPattern(),
                product.getProductStretchType(),
                product.getProductOfferPercentage(),
                productFilterResponses.stream()
                        .map(ProductFilterResponse::getStockQuantity)
                        .distinct()
                        .map(stock -> new StockQuantityResponse(
                                stock.getSize(),
                                stock.getProductPrice(),
                                productFilterResponses.stream()
                                        .filter(dto -> dto.getStockQuantity().getStockId().equals(stock.getStockId()))
                                        .map(ProductFilterResponse::getColorQuantity)
                                        .distinct()
                                        .map(colorQuantity -> new ColorQuantityResponse(
                                                colorQuantity.getColor(),
                                                colorQuantity.getColorCode(),
                                                colorQuantity.getQuantity(),
                                                product.getProductImages().stream()
                                                        .filter(image -> image.getColorName().equals(colorQuantity.getColor()))
                                                        .filter(image -> image.getImagePriority() != null)
                                                        .sorted(Comparator.comparing(ProductImage::getImagePriority, Comparator.nullsLast(Comparator.naturalOrder())))
                                                        .collect(Collectors.toMap(
                                                                ProductImage::getImageUrl,
                                                                ProductImage::getImagePriority,
                                                                (existing, replacement) -> existing
                                                        ))
                                        ))
                                        .toList()
                        ))
                        .toList(),
                productImageResponses,
                product.getVideoUrl(),
                productReviewResponse
        );
    }

    private ProductReviewResponse getProductReviews(String productId) {
        ProductReviewResponse productReviewResponse;
        try {
            productReviewResponse = reviewService.getProductReviews(productId);
        } catch (ResourceNotFoundException e) {
            log.info("No reviews found for product: {}", productId);
            throw new ResourceNotFoundException(Constants.REVIEW_NOT_FOUND);
        }
        log.info("Product Review Response : {}", productReviewResponse);

        return productReviewResponse;
    }

    private String uploadFileToS3(MultipartFile file, String fileType, String productId, String colorName) {
        String slash = "/";
        String fileName = productId + slash + colorName + slash + fileType + slash + file.getOriginalFilename();
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

    private FilterDataResponse createFilterDataResponse(StockQuantityResponse stock, List<ProductImageResponse> productImages) {
        FilterDataResponse dataResponse = new FilterDataResponse();
        dataResponse.setSize(stock.getSize());
        dataResponse.setAmount(stock.getProductPrice());

        List<ColorDataResponse> colorDataResponses = new ArrayList<>();
        if (stock.getColorQuantityResponses() != null) {
            for (ColorQuantityResponse color : stock.getColorQuantityResponses()) {
                colorDataResponses.add(createColorDataResponse(color, productImages));
            }
        }
        dataResponse.setColorDataResponses(colorDataResponses);
        return dataResponse;
    }

    private ColorDataResponse createColorDataResponse(ColorQuantityResponse color, List<ProductImageResponse> productImages) {
        ColorDataResponse colorMap = new ColorDataResponse();
        Map<String, String> colorWithColorCode = new HashMap<>();
        colorWithColorCode.put(color.getColor(), color.getColorCode());
        colorMap.setColorWithColorCodes(colorWithColorCode);

        colorMap.setColorStockAvailable(color.getQuantity() != 0);
        colorMap.setAvailableStock(color.getQuantity());

        colorMap.setImagesWithPriority(getImagesForColor(color.getColor(), productImages));
        return colorMap;
    }

    private Map<Integer, String> getImagesForColor(String color, List<ProductImageResponse> productImages) {
        return productImages.stream()
                .filter(image -> image.getColorName().equalsIgnoreCase(color))
                .collect(Collectors.toMap(ProductImageResponse::getImagePriority, ProductImageResponse::getImageUrl));
    }

}
