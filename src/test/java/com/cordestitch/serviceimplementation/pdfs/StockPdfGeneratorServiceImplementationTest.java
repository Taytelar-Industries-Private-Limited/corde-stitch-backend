package com.cordestitch.serviceimplementation.pdfs;

import com.cordestitch.emailhandler.EmailTransport;
import com.cordestitch.service.serviceimplementation.pdfs.StockPdfGeneratorServiceImplementation;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.cordestitch.entity.product.ColorQuantity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.StockQuantity;
import com.cordestitch.entity.product.SubCategory;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.response.SuccessResponse;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockPdfGeneratorServiceImplementationTest {
    @InjectMocks
    private StockPdfGeneratorServiceImplementation stockPdfGeneratorServiceImplementation;
    @Mock
    private ProductRepository productRepository;

    @Mock
    private EmailTransport emailTransport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        stockPdfGeneratorServiceImplementation = new StockPdfGeneratorServiceImplementation(emailTransport,productRepository);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailPort", 465);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "emailUsername", "courses@seabed2crest.com");
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "emailPassword", "azng inwc nsyp gsml");
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailStartTlsRequired", true);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailStartTlsEnable", false);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailSocketFactoryClass", "javax.net.ssl.SSLSocketFactory");
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailDebug", false);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "toEmailAddress", "jayanthhp423@gmail.com");

    }


    @Test
    void sendStockPdfReportToEmail_Success() throws Exception {
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        StockPdfGeneratorServiceImplementation spyPdfService = Mockito.spy(stockPdfGeneratorServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendStockPdfReportToEmail();
        assertNotNull(response);
        assertEquals("Email Sent Successfully", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }



    @Test
    void sendStockPdfReportToEmail_Exception() throws Exception {
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailHost", null);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailPort", 0);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "emailUsername", null);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "emailPassword", null);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailStartTlsRequired", false);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailStartTlsEnable", false);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailSocketFactoryClass", null);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "mailDebug", false);
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "toEmailAddress", null);

        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        StockPdfGeneratorServiceImplementation spyPdfService = Mockito.spy(stockPdfGeneratorServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendStockPdfReportToEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void sendStockPdfReportToEmail_Failed_To_Send_Email_Exception() throws Exception {
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        StockPdfGeneratorServiceImplementation spyPdfService = Mockito.spy(stockPdfGeneratorServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        doThrow(new MessagingException("Failed to send email: " + new Throwable())).when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendStockPdfReportToEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void sendStockPdfReportToEmail_IO_Exception() throws Exception {
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        mockOutputStream.write("Mock PDF content".getBytes());
        StockPdfGeneratorServiceImplementation spyPdfService = Mockito.spy(stockPdfGeneratorServiceImplementation);
        doReturn(mockOutputStream).when(spyPdfService).generateByteArray();
        doNothing().when(emailTransport).send(any());
        doAnswer(invocation -> {throw new IOException("IO Error occurred while generating report: " + new Throwable());}).when(emailTransport).send(any());
        SuccessResponse response = spyPdfService.sendStockPdfReportToEmail();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
    }

    @Test
    void generateByteArray() throws Exception{
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "logoImageUrl", logoPath);
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        ByteArrayOutputStream outputStream = stockPdfGeneratorServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty() throws Exception{
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "logoImageUrl", logoPath);
        Product product = getProduct();
        product.setStockQuantities(null);
        when(productRepository.findAll()).thenReturn(List.of(product));

        ByteArrayOutputStream outputStream = stockPdfGeneratorServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_With_StockQuantity_Empty() throws Exception{
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "logoImageUrl", logoPath);
        StockQuantity stockQuantity = getStockQuantity();
        stockQuantity.setColorQuantities(List.of(getColorQuantityA(),new ColorQuantity()));
        Product product = getProduct();
        product.setStockQuantities(List.of(getStockQuantity(), stockQuantity));
        when(productRepository.findAll()).thenReturn(List.of(product));

        ByteArrayOutputStream outputStream = stockPdfGeneratorServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    @Test
    void generateByteArray_When_Order_Entity_Not_Empty_With_StockQuantity() throws Exception{
        Path tempFile = Files.createTempFile("test-logo", ".png");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", tempFile.toFile());

        String logoPath = tempFile.toUri().toURL().toString();
        ReflectionTestUtils.setField(stockPdfGeneratorServiceImplementation, "logoImageUrl", logoPath);
        Product product = getProduct();
        when(productRepository.findAll()).thenReturn(List.of(product));

        ByteArrayOutputStream outputStream = stockPdfGeneratorServiceImplementation.generateByteArray();
        assertNotNull(outputStream);

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(outputStream.toByteArray())));
        assertEquals(1, pdfDoc.getNumberOfPages());
        pdfDoc.close();
    }

    private Product getProduct() {
        Product product = new Product();
        product.setProductId("1");
        product.setProductName("Pant");
        product.setProductStatus("Pending");
        product.setProductDescription("Formal pant");
        product.setProductPattern("plain");
        product.setProductMaterialType("polyester");
        product.setProductOfferPercentage(5.0);
        product.setSubCategory(new SubCategory());
        product.setStockQuantities(List.of(getStockQuantity()));
        return product;
    }

    private StockQuantity getStockQuantity() {
        StockQuantity stockQuantity = new StockQuantity();
        stockQuantity.setSize(32);
        stockQuantity.setProductPrice(100.0);
        stockQuantity.setProduct(new Product());
        stockQuantity.setStockId("11");
        stockQuantity.setColorQuantities(getColorQuantity());
        return stockQuantity;
    }

    private List<ColorQuantity> getColorQuantity() {
        List<ColorQuantity> list = new ArrayList<>();
        ColorQuantity colorQuantity = new ColorQuantity();
        colorQuantity.setColorQuantityId("1");
        colorQuantity.setStockQuantity(new StockQuantity());
        colorQuantity.setColor("white");
        colorQuantity.setColorCode("#ffffff");
        colorQuantity.setQuantity(5);
        list.add(colorQuantity);
        return list;
    }

    private ColorQuantity getColorQuantityA() {
        ColorQuantity colorQuantity = new ColorQuantity();
        colorQuantity.setColorQuantityId("1");
        colorQuantity.setStockQuantity(new StockQuantity());
        colorQuantity.setColor("white");
        colorQuantity.setColorCode("#ffffff");
        colorQuantity.setQuantity(5);
        return colorQuantity;
    }
}