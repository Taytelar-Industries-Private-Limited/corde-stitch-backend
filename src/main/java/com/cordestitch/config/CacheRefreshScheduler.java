package com.cordestitch.config;

import com.cordestitch.service.service.homepage.HomePageService;
import com.cordestitch.service.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheRefreshScheduler {

    private final CacheManager cacheManager;

    private final ProductService productService;

    private final HomePageService homePageService;

    private static final String PRODUCT_CACHE = "productsCache";

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshGetAllProductCache() {
        long startTime =System.currentTimeMillis();
        refreshCache(PRODUCT_CACHE,
                "listAllProduct",
                productService::getAllProducts);
        long endTime = System.currentTimeMillis();
        log.info("Product cache refresh completed in {} ms", endTime - startTime);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshGetAllFilterConditionCache() {
        refreshCache(PRODUCT_CACHE,
                "getAllFilterCondition",
                productService::getAllFilterCondition);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshGetAllHomePageImagesCache() {
        refreshCache(PRODUCT_CACHE,
                "getAllHomePageImages",
                homePageService::getAllHomePageImages);
    }

    public <T> void refreshCache(String cacheName, String cacheKey, Supplier<T> dataProvider) {
        log.info("Starting cache refresh for cache: {}, key: {} at {}", cacheName, cacheKey, LocalDateTime.now());

        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (isNull(cache)) {
                log.warn("Cache '{}' not found. Skipping refresh.", cacheName);
                return;
            }
            cache.evict(cacheKey);
            log.debug("Cache entry evicted for cache: {}, key: {}", cacheName, cacheKey);

            dataProvider.get();

            log.info("Cache refreshed successfully for cache: {}, key: {} at {}", cacheName, cacheKey,LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error refreshing cache: {}, key: {} - Error: {}", cacheName, cacheKey, e.getMessage(), e);
        }
    }
}