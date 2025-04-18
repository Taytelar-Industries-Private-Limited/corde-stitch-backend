package com.cordestitch.service.serviceimplementation.pdfs;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.cordestitch.emailhandler.EmailTransport;
import com.cordestitch.emailhandler.SessionProvider;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.pdfs.OrderConfirmedService;
import com.cordestitch.util.Constants;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmedServiceImplementation implements OrderConfirmedService {
    private final OrderRepository orderRepository;

    private final EmailTransport emailTransport;

    @Value("${spring.mail.username}")
    private String emailUsername;

    @Value("${spring.mail.password}")
    private String emailPassword;

    @Value("${spring.mail.to.username}")
    private String toEmailAddress;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.properties.mail.smtp.starttls.required}")
    private boolean mailStartTlsRequired;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean mailStartTlsEnable;

    @Value("${spring.mail.properties.mail.smtp.socketFactory.class}")
    private String mailSocketFactoryClass;

    @Value("${spring.mail.properties.mail.debug}")
    private boolean mailDebug;

    @Value("${logo.image.url}")
    private String logoImageUrl;

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yy");

    @Override
    public SuccessResponse sendOrderConfirmedEmail() {
        try {
            ByteArrayOutputStream outputStream = generateByteArray();

            Session session = SessionProvider.createSession(mailHost, mailPort, emailUsername, emailPassword,
                    mailStartTlsRequired, mailStartTlsEnable, mailSocketFactoryClass, mailDebug);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddress));
            message.setSubject("Report");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Please find the report attached.");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            jakarta.activation.DataSource source = new jakarta.mail.util.ByteArrayDataSource(outputStream.toByteArray(), "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("Orders_Confirmed_Report.pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            emailTransport.send(message);

            return new SuccessResponse("Email Sent Successfully", HttpStatus.OK.value());
        } catch (MessagingException e) {
            return new SuccessResponse("Failed to send email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (IOException e) {
            return new SuccessResponse("IO Error occurred while generating report: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            return new SuccessResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public ByteArrayOutputStream generateByteArray() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
        Document doc = new Document(pdfDoc).setFont(font);

        ImageData imageData = ImageDataFactory.create(logoImageUrl);
        Image logoImage = new Image(imageData);
        logoImage.setFixedPosition(pdfDoc.getDefaultPageSize().getWidth()/2-120,pdfDoc.getDefaultPageSize().getHeight()/2-130);
        logoImage.setOpacity(0.28f).setWidth(UnitValue.createPercentValue(80)).setHeight(UnitValue.createPercentValue(50));
        logoImage.setRotationAngle(Math.toRadians(45));

        Color borderColor = new DeviceRgb(0, 0, 0);
        float borderWidth = 2f;

        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new PdfGenerateServiceImplementation.HeaderFooterEventHandler(borderColor, borderWidth));
        LocalDateTime startDate;
        LocalDateTime endDate;
        startDate = LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1).toLocalDate().atStartOfDay();
        endDate = LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1).toLocalDate().atTime(23,59,59);

        List<OrderEntity> orderEntityList = orderRepository.getReport(startDate, endDate);
        List<OrderItemEntity> confirmedOrderData = orderEntityList.stream()
                .flatMap(order -> order.getOrderItemEntities().stream())
                .filter(orders -> Optional.ofNullable(orders.getOrderStatus())
                        .map(status -> status.equals(OrderStatus.CONFIRMED))
                        .orElse(false))
                .toList();
        Table companyDetailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 5}));
        PdfFont companyFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC);
        Color fontColor = new DeviceRgb(77,76,76);

        Image watermarkImage = new Image(ImageDataFactory.create(logoImageUrl));
        watermarkImage.setWidth(UnitValue.createPointValue(140)).setHeight(UnitValue.createPercentValue(80)).setHorizontalAlignment(HorizontalAlignment.LEFT);
        companyDetailsTable.addHeaderCell(new Cell().add(watermarkImage).setVerticalAlignment(VerticalAlignment.TOP).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
        Paragraph companyParagraph = new Paragraph()
                .add(new Text("Cordestitch INDUSTRIES PVT LTD").setFontColor(fontColor).setFont(companyFont).setFontSize(20))
                .add("\n")
                .add(new Text("Rajanukunte, Bengaluru, 560064 \n").setFontColor(fontColor).setFont(companyFont).setFontSize(12))
                .add(new Text("GST No: xxxxxxxxx \n\n")).setFontColor(fontColor).setFont(companyFont).setPaddingLeft(25f);

        companyDetailsTable.addHeaderCell(new Cell().add(companyParagraph).setTextAlignment(TextAlignment.RIGHT).setHorizontalAlignment(HorizontalAlignment.RIGHT).setBorder(Border.NO_BORDER));
        companyDetailsTable.setWidth(UnitValue.createPercentValue(100));
        doc.add(logoImage);
        doc.add(companyDetailsTable);
        Table dateTable = new Table(1);
        dateTable.setWidth(400);

        Paragraph dateParagraph = new Paragraph();
        dateParagraph.add("\n\n");
        dateParagraph.add("Report Name : ").setFont(companyFont);
        dateParagraph.add("Confirmed Orders Report").setFont(companyFont);
        dateParagraph.add("\n");
        if (!confirmedOrderData.isEmpty()) {
            dateParagraph.add("Report Date: ");
            dateParagraph.add(format.format(orderEntityList.getFirst().getOrderDate()));
            dateParagraph.add("\n");

            dateTable.addCell(new Cell().add(dateParagraph)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER));

            doc.add(dateTable);


            float[] bookingInfoColumnWidths = {120, 120, 150, 145, 120, 150, 130, 120};
            Table bookingTable = new Table(bookingInfoColumnWidths);
            bookingTable.setTextAlignment(TextAlignment.CENTER);
            Color customGray = new DeviceRgb(150,150,150);
            bookingTable.addCell(new Cell().add(createStyledCell("Order ID", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("OrderItem ID", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Order Date", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Order Status", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Total Amount", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Payment Method", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Payment Status", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Payment Date", font)).setBackgroundColor(customGray).setOpacity(0.8f));


            for (OrderItemEntity orderItemEntity : confirmedOrderData) {

                OrderEntity entity = orderItemEntity.getOrderEntity();

                bookingTable.addCell(new Cell().add(createStyledCell(entity.getOrderId(),font)));
                bookingTable.addCell(new Cell().add(createStyledCell(orderItemEntity.getOrderItemId(),font)));
                bookingTable.addCell(new Cell().add(createStyledCell(format.format(entity.getOrderDate()),font)));
                bookingTable.addCell(new Cell().add(createStyledCell(String.valueOf(orderItemEntity.getOrderStatus()),font)));
                bookingTable.addCell(new Cell().add(createStyledCell(String.valueOf(entity.getTotalAmount()),font)));
                bookingTable.addCell(new Cell().add(createStyledCell(entity.getPaymentEntity().getPaymentMethod(),font)));
                bookingTable.addCell(new Cell().add(createStyledCell(String.valueOf(entity.getPaymentEntity().getPaymentStatus()),font)));
                bookingTable.addCell(new Cell().add(createStyledCell(entity.getPaymentEntity().getPaymentDate() != null ? format.format(entity.getPaymentEntity().getPaymentDate()) : "N/A",font)));

            }

            doc.add(bookingTable);
        } else {
            dateParagraph.add("\n There is no Confirmed Order Details found on " + format.format(LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1)));
            dateTable.addCell(new Cell().add(dateParagraph)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER));

            doc.add(dateTable);
        }

        doc.close();
        pdfDoc.close();
        writer.close();

        log.info("PDF generated successfully.");
        return outputStream;
    }

    private Cell createStyledCell(String text, PdfFont font) {
        Cell cell = new Cell().add(new Paragraph(text).setFont(font));
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setPadding(2);
        return cell;
    }
}
