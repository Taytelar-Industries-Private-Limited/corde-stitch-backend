package com.cordestitch.exception;

import com.cordestitch.exception.affiliate.FailedToSendOtpException;
import com.cordestitch.exception.cart.CartItemNotFoundException;
import com.cordestitch.exception.customization.*;
import com.cordestitch.exception.filter.EncryptionException;
import com.cordestitch.exception.order.*;
import com.cordestitch.exception.otp.OtpNotFoundException;
import com.cordestitch.exception.otp.UnknownUserTypeException;
import com.cordestitch.exception.payment.CardNotFoundException;
import com.cordestitch.exception.payment.PaymentNotFoundException;
import com.cordestitch.exception.payment.RefundProcessException;
import com.cordestitch.exception.product.ProductNotFoundException;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.exception.review.UnauthorizedActionException;
import com.cordestitch.exception.token.InvalidRefreshTokenException;
import com.cordestitch.exception.token.JwtProcessingException;
import com.cordestitch.exception.user.*;
import com.cordestitch.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private BindingResult bindingResult;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUserNotFoundException() {
        String expectedMessage = "User not found";
        UserNotFoundException exception = new UserNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUserNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testUserDetailsMissMatchException() {
        String expectedMessage = "user details miss match";
        UserDetailsMissMatchException exception = new UserDetailsMissMatchException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUserDetailsMissMatchException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void UserAccountNotExistException() {
        String expectedMessage = "User account not exist";
        UserAccountNotExistException exception = new UserAccountNotExistException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUserAccountNotExistException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testUserAccountAlreadyExistException() {
        String expectedMessage = "User account already exist";
        UserAccountAlreadyExistException exception = new UserAccountAlreadyExistException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUserAccountAlreadyExistException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testAddressNotFoundException() {
        String expectedMessage = "Address not found";
        AddressNotFoundException exception = new AddressNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAddressNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());

    }

    @Test
    void testAddressAlreadyExistsException() {
        String expectedMessage = "Address already exists";
        AddressAlreadyExistsException exception = new AddressAlreadyExistsException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAddressAlreadyExistsException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testProductNotFoundException() {
        String expectedMessage = "Product not found";
        ProductNotFoundException exception = new ProductNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleProductNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testS3UploadException() {
        String expectedMessage = "Failed to upload to S3";
        S3UploadException exception = new S3UploadException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleS3UploadException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testPaymentNotFoundException() {
        String expectedMessage = "Payment not found";
        PaymentNotFoundException exception = new PaymentNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handlePaymentNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testRefundProcessException() {
        String expectedMessage = "Failed to process refund";
        RefundProcessException exception = new RefundProcessException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleRefundProcessException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testOtpNotFoundException() {
        String expectedMessage = "otp not found";
        OtpNotFoundException exception = new OtpNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleOtpNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testUnknownUserTypeException() {
        String expectedMessage = "unknown user type";
        UnknownUserTypeException exception = new UnknownUserTypeException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUnknownUserTypeException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testCategoryNotFoundException() {
        String expectedMessage = "Category not found";
        CategoryNotFoundException exception = new CategoryNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleCategoryNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testColorNotFoundException() {
        String expectedMessage = "Color not found";
        ColorNotFoundException exception = new ColorNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleColorNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testInsufficientStockException() {
        String expectedMessage = "Insufficient stock";
        InsufficientStockException exception = new InsufficientStockException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleInsufficientStockException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testOrderCancellationException() {
        String expectedMessage = "Order cancellation failed";
        OrderCancellationException exception = new OrderCancellationException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleOrderCancellationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testOrderNotFoundException() {
        String expectedMessage = "Order not found";
        OrderNotFoundException exception = new OrderNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleOrderNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testOrderPlacingException() {
        String expectedMessage = "Failed to place order";
        OrderPlacingException exception = new OrderPlacingException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleOrderPlacingException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testReturnException() {
        String expectedMessage = "Return failed";
        ReturnException exception = new ReturnException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleReturnException(exception);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testStockNotFoundException() {
        String expectedMessage = "Stock not found";
        StockNotFoundException exception = new StockNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleStockNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testSubCategoryNotFoundException() {
        String expectedMessage = "Subcategory not found";
        SubCategoryNotFoundException exception = new SubCategoryNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleSubCategoryNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testCartItemNotFoundException() {
        String expectedMessage = "Cart item not found";
        CartItemNotFoundException exception = new CartItemNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleCartItemNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testFailedToSendOtpException() {
        String expectedMessage = "Failed to send otp";
        FailedToSendOtpException exception = new FailedToSendOtpException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleFailedToSendOtpException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testCustomizationTypeAlreadyExistException() {
        String expectedMessage = "CustomizationType Already Exist";
        CustomizationTypeAlreadyExistException exception = new CustomizationTypeAlreadyExistException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleCustomizationTypeAlreadyExistException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testConvertToJsonException() {
        String expectedMessage = "Convert To Json";
        ConvertToJsonException exception = new ConvertToJsonException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleConvertToJsonException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testConvertFromJsonException() {
        String expectedMessage = "Convert From Json";
        ConvertFromJsonException exception = new ConvertFromJsonException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleConvertFromJsonException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testCustomizationNotFoundException() {
        String expectedMessage = "Customization Not Found";
        CustomizationNotFoundException exception = new CustomizationNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleCustomizationTypeNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testFabricNotFoundException() {
        String expectedMessage = "Fabric Not Found";
        FabricNotFoundException exception = new FabricNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleFabricNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testCardNotFoundException() {
        String expectedMessage = "Card Not Found";
        CardNotFoundException exception = new CardNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleCardNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testDataCheckReflectionException() {
        String expectedMessage = "Data Check Reflection";
        DataCheckReflectionException exception = new DataCheckReflectionException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleDataCheckReflectionException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testEncryptionException() {
        String expectedMessage = "Encryption";
        Throwable cause = new Throwable("Exception");
        EncryptionException exception = new EncryptionException(expectedMessage,cause);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleEncryptionException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testInvalidRefreshTokenException() {
        String expectedMessage = "Invalid Refresh Token";
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleInvalidRefreshTokenException(exception);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getBody().getStatusCode());
    }
    @Test
    void testJwtProcessingException() {
        String expectedMessage = "JwtProcessing Exception";
        JwtProcessingException exception = new JwtProcessingException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleJwtProcessingException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatusCode());
    }
    @Test
    void testResourceNotFoundException() {
        String expectedMessage = "Resource Not Found";
        ResourceNotFoundException exception = new ResourceNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleResourceNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatusCode());
    }

    @Test
    void testUnauthorizedActionException() {
        String expectedMessage = "Unauthorized Action";
        UnauthorizedActionException exception = new UnauthorizedActionException(expectedMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUnauthorizedActionException(exception);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals(expectedMessage, Objects.requireNonNull(responseEntity.getBody()).getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getBody().getStatusCode());
    }
    @Test
    void testMethodArgumentNotValidException() {
        String objectName = "test object";
        String errorMessage = "test error";
        ObjectError objectError = new ObjectError(objectName, errorMessage);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(objectError));
        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);
        ResponseEntity<Map<String, String>> responseEntity = globalExceptionHandler.handleValidationExceptions(exception);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Map<String, String> responseBody = responseEntity.getBody();
        assert responseBody != null;
        assertEquals(1, responseBody.size());
        assertEquals(errorMessage, responseBody.get(objectName));
    }

}
