package com.cordestitch.serviceimplementation.pdfs;

import com.cordestitch.emailhandler.EmailTransport;
import com.cordestitch.service.serviceimplementation.pdfs.OrderConfirmedServiceImplementation;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.util.Constants;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderConfirmedServiceImplementationTest {
    @InjectMocks
    private OrderConfirmedServiceImplementation orderConfirmedServiceImplementation;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EmailTransport emailTransport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderConfirmedServiceImplementation = new OrderConfirmedServiceImplementation(orderRepository, emailTransport);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailPort", 465);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "emailUsername", "courses@seabed2crest.com");
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "emailPassword", "azng inwc nsyp gsml");
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailStartTlsRequired", true);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailStartTlsEnable", false);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailSocketFactoryClass", "javax.net.ssl.SSLSocketFactory");
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailDebug", false);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "toEmailAddress", "jayanthhp423@gmail.com");

    }
    @Test
    void sendOrderConfirmedEmail_Success() throws Exception{
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        OrderConfirmedServiceImplementation spyPdfService = Mockito.spy(orderConfirmedServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendOrderConfirmedEmail();
        assertNotNull(response);
        assertEquals("Email Sent Successfully", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    void sendOrderConfirmedEmail_Exception() throws Exception{
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailHost", null);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailPort", 0);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "emailUsername", null);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "emailPassword", null);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailStartTlsRequired", false);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailStartTlsEnable", false);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailSocketFactoryClass", null);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "mailDebug", false);
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "toEmailAddress", null);

        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        OrderConfirmedServiceImplementation spyPdfService = Mockito.spy(orderConfirmedServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendOrderConfirmedEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void sendOrderConfirmedEmail_Failed_To_Send_Email_Exception() throws Exception{
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        OrderConfirmedServiceImplementation spyPdfService = Mockito.spy(orderConfirmedServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        doThrow(new MessagingException("Failed to send email: " + new Throwable())).when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendOrderConfirmedEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void sendOrderConfirmedEmail_IO_Exception() throws Exception{
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        OrderConfirmedServiceImplementation spyPdfService = Mockito.spy(orderConfirmedServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        doAnswer(invocation -> {throw new IOException("IO Error occurred while generating report: " + new Throwable());}).when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendOrderConfirmedEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void generateByteArray_When_Order_Entity_Is_Empty() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().setOrderItemEntities(new ArrayList<>());
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);
        ByteArrayOutputStream outputStream = orderConfirmedServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().getOrderItemEntities().getFirst().setOrderStatus(OrderStatus.CONFIRMED);
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = orderConfirmedServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_With_PaymentDate_Is_Null() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(orderConfirmedServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().getPaymentEntity().setPaymentDate(null);
        orderEntityList.getFirst().getOrderItemEntities().getFirst().setOrderStatus(OrderStatus.CONFIRMED);
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = orderConfirmedServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    private List<OrderEntity> getOrderEntityList() {
        List<OrderEntity> orderEntityList = new ArrayList<>();
        orderEntityList.add(getOrderEntity());
        return orderEntityList;
    }

    private OrderEntity getOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId("123");
        orderEntity.setUserEntity(new UserEntity());
        orderEntity.setOrderDate(LocalDateTime.now());
        orderEntity.setPaymentEntity(getPaymentEntity());
        orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderEntity.setPaymentMethod(Constants.RAZORPAY);
        orderEntity.setAddressEntity(new AddressEntity());
        orderEntity.setTotalAmount(150.0);
        List<OrderItemEntity> orderItemEntities = getListOfOrderItemEntities(orderEntity);
        orderEntity.setOrderItemEntities(orderItemEntities);
        return orderEntity;
    }

    private PaymentEntity getPaymentEntity() {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderEntity(new OrderEntity());
        paymentEntity.setPaymentDate(LocalDateTime.now());
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setPaymentMethod(Constants.RAZORPAY);
        paymentEntity.setUserId("1");
        paymentEntity.setRazorPayOrderId("razorpay123");
        paymentEntity.setPaymentId("pay123");
        paymentEntity.setRazorPayPaymentId("rpayment123");
        paymentEntity.setTotalAmount(150.0);
        paymentEntity.setRefundEntities(getListRefundEntities(paymentEntity));
        return paymentEntity;
    }

    private List<RefundEntity> getListRefundEntities(PaymentEntity paymentEntity) {
        List<RefundEntity> refundEntities = new ArrayList<>();
        RefundEntity refundEntity = new RefundEntity();
        refundEntity.setRefundId("r123");
        refundEntity.setRefundDate(LocalDateTime.now());
        refundEntity.setRefundStatus(RefundStatus.NOT_REQUESTED);
        refundEntity.setRefundAmount(10.0);
        refundEntity.setRefundIdOrPayoutId("pout_35gfev2j21njk");
        refundEntity.setRefundType(RefundType.PRODUCT);
        refundEntity.setPaymentEntity(paymentEntity);
        refundEntity.setOrderItemEntity(new OrderItemEntity());
        refundEntities.add(refundEntity);
        return refundEntities;
    }

    private List<OrderItemEntity> getListOfOrderItemEntities(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(orderEntity);
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P1");
        orderItemEntity.setUnitPrice(100.0);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setReturnDaysPolicy(7);
        orderItemEntity.setTotalAmount(150.0);
        orderItemEntity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(5));
        orderItemEntities.add(orderItemEntity);
        return orderItemEntities;
    }

}