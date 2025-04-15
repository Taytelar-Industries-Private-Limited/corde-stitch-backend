package com.cordestitch.serviceimplementation.pdfs;

import com.cordestitch.emailhandler.EmailTransport;
import com.cordestitch.service.serviceimplementation.pdfs.ReturnOrderServiceImplementation;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.order.ReturnEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.order.ReturnRepository;
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

class ReturnOrderServiceImplementationTest {
    @InjectMocks
    private ReturnOrderServiceImplementation returnOrderServiceImplementation;
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EmailTransport emailTransport;

    @Mock
    private ReturnRepository returnRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        returnOrderServiceImplementation = new ReturnOrderServiceImplementation(orderRepository, emailTransport, returnRepository);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailPort", 465);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "emailUsername", "courses@seabed2crest.com");
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "emailPassword", "azng inwc nsyp gsml");
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailStartTlsRequired", true);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailStartTlsEnable", false);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailSocketFactoryClass", "javax.net.ssl.SSLSocketFactory");
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailDebug", false);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "toEmailAddress", "jayanthhp423@gmail.com");

    }
    @Test
    void sendReturnOrderEmail() throws Exception{
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        ReturnOrderServiceImplementation spyPdfService = Mockito.spy(returnOrderServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendReturnOrderEmail();
        assertNotNull(response);
        assertEquals("Email Sent Successfully", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    void sendReturnOrderEmail_Exception() throws Exception{
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailHost", null);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailPort", 0);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "emailUsername", null);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "emailPassword", null);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailStartTlsRequired", false);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailStartTlsEnable", false);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailSocketFactoryClass", null);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "mailDebug", false);
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "toEmailAddress", null);

        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        ReturnOrderServiceImplementation spyPdfService = Mockito.spy(returnOrderServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendReturnOrderEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void sendReturnOrderEmail_Failed_To_Send_Email_Exception() throws Exception{
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        ReturnOrderServiceImplementation spyPdfService = Mockito.spy(returnOrderServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        doThrow(new MessagingException("Failed to send email: " + new Throwable())).when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendReturnOrderEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void sendReturnOrderEmail_IO_Exception() throws Exception{
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        ReturnOrderServiceImplementation spyPdfService = Mockito.spy(returnOrderServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        doAnswer(invocation -> {throw new IOException("IO Error occurred while generating report: " + new Throwable());}).when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendReturnOrderEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void generateByteArray_When_Order_Entity_Is_Empty() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().setOrderItemEntities(new ArrayList<>());
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);
        ByteArrayOutputStream outputStream = returnOrderServiceImplementation.generateByteArray();
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
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.RETURNED);
        when(returnRepository.findAll()).thenReturn(List.of(getReturnEntity()));
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = returnOrderServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_When_Delivery_Status_Is_Delivered() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(returnRepository.findAll()).thenReturn(List.of(getReturnEntity()));
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = returnOrderServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_When_Delivery_Status_Is_Delivered_AND_Returned_In_List() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
        orderEntityList.add(getOrderEntity());
        orderEntityList.getLast().getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.PENDING);
        when(returnRepository.findAll()).thenReturn(List.of(getReturnEntity()));
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = returnOrderServiceImplementation.generateByteArray();
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
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().getPaymentEntity().setPaymentDate(null);
        orderEntityList.getFirst().getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.RETURNED);
        when(returnRepository.findAll()).thenReturn(List.of(getReturnEntity()));
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = returnOrderServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_With_PaymentDate_Is_Null_With_Delivery_Status_Delivered() throws Exception {
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(returnOrderServiceImplementation, "logoImageUrl", logoPath);
        List<OrderEntity> orderEntityList = getOrderEntityList();
        orderEntityList.getFirst().getPaymentEntity().setPaymentDate(null);
        orderEntityList.getFirst().getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(returnRepository.findAll()).thenReturn(List.of(getReturnEntity()));
        when(orderRepository.getReport(any(), any())).thenReturn(orderEntityList);

        ByteArrayOutputStream outputStream = returnOrderServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    private ReturnEntity getReturnEntity() {
        ReturnEntity returnEntity = new ReturnEntity();
        returnEntity.setReturnId("1");
        returnEntity.setReturnDate(LocalDateTime.now());
        returnEntity.setReturnStatus(ReturnStatus.REFUND_INITIATED);
        returnEntity.setOrderId("123");
        returnEntity.setOrderItemId("111");
        returnEntity.setUserId("1");
        returnEntity.setReturnReason("Damaged");
        returnEntity.setReturnType("Exchange");
        returnEntity.setReturnDate(LocalDateTime.now());
        return returnEntity;
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
        orderItemEntity.setReturnStatus(ReturnStatus.REFUND_INITIATED);
        orderItemEntity.setReturnReason("Damaged");
        orderItemEntity.setReturnReplacementOrderItemId("1");
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(5));
        orderItemEntities.add(orderItemEntity);
        return orderItemEntities;
    }

}