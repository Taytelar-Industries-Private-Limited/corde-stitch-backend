package com.cordestitch.serviceimplementation.pdfs;

import com.cordestitch.emailhandler.EmailTransport;
import com.cordestitch.service.serviceimplementation.pdfs.PdfGenerateServiceImplementation;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PdfGenerateServiceImplementationTest {
    @InjectMocks
    private PdfGenerateServiceImplementation pdfGenerateServiceImplementation;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EmailTransport emailTransport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pdfGenerateServiceImplementation = new PdfGenerateServiceImplementation(orderRepository, emailTransport);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailPort", 465);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "emailUsername", "courses@seabed2crest.com");
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "emailPassword", "azng inwc nsyp gsml");
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailStartTlsRequired", true);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailStartTlsEnable", false);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailSocketFactoryClass", "javax.net.ssl.SSLSocketFactory");
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailDebug", false);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "toEmailAddress", "jayanthhp423@gmail.com");

    }


    @Test
    void sendReportToEmail_Success() throws Exception {
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        PdfGenerateServiceImplementation spyPdfService = Mockito.spy(pdfGenerateServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray(anyInt());
        doNothing().when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendReportToEmail(1);
        assertNotNull(response);
        assertEquals("Email Sent Successfully", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    void sendReportToEmail_Exception() throws Exception {
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailHost", null);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailPort", 0);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "emailUsername", null);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "emailPassword", null);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailStartTlsRequired", false);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailStartTlsEnable", false);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailSocketFactoryClass", null);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "mailDebug", false);
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "toEmailAddress", null);
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        PdfGenerateServiceImplementation spyPdfService = Mockito.spy(pdfGenerateServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray(anyInt());
        doNothing().when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendReportToEmail(anyInt());
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void sendReportToEmail_Failed_To_Send_Email_Exception() throws Exception {
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        PdfGenerateServiceImplementation spyPdfService = Mockito.spy(pdfGenerateServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray(anyInt());
        doNothing().when(emailTransport).send(any());
        doThrow(new MessagingException("Failed to send email: " + new Throwable())).when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendReportToEmail(anyInt());
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void sendReportToEmail_IO_Exception() throws Exception {
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        PdfGenerateServiceImplementation spyPdfService = Mockito.spy(pdfGenerateServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray(anyInt());
        doNothing().when(emailTransport).send(any());
        doAnswer(invocation -> {
            throw new IOException("IO Error occurred while generating report: ");
        }).when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendReportToEmail(anyInt());
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void generateByteArray_When_Order_Entity_Empty_And_Report_Type_1() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "logoImageUrl", logoPath);
        when(orderRepository.getReport(any(), any())).thenReturn(Collections.emptyList());

        ByteArrayOutputStream outputStream = pdfGenerateServiceImplementation.generateByteArray(1);
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_And_Report_Type_2() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = pdfGenerateServiceImplementation.generateByteArray(2);
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_And_Report_Type_3() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().getPaymentEntity().setPaymentDate(null);
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = pdfGenerateServiceImplementation.generateByteArray(3);
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_And_Report_Type_Default() throws IOException {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(pdfGenerateServiceImplementation, "logoImageUrl", logoPath);
        int reportType = 4;
        List<OrderEntity> orderEntityList = getOrderEntityList();
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,()->pdfGenerateServiceImplementation.generateByteArray(reportType));
        assertEquals("Invalid report type: "+ reportType, exception.getMessage());
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
        orderItemEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderItemEntity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(5));
        orderItemEntities.add(orderItemEntity);
        return orderItemEntities;
    }
}
