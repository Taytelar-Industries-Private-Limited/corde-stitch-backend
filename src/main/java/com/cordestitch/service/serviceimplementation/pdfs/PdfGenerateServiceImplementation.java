package com.cordestitch.service.serviceimplementation.pdfs;

import com.itextpdf.commons.actions.IEvent;
import com.itextpdf.commons.actions.IEventHandler;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.*;
import com.cordestitch.emailhandler.EmailTransport;
import com.cordestitch.emailhandler.SessionProvider;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.pdfs.PdfGenerateService;
import com.cordestitch.util.Constants;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
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
public class PdfGenerateServiceImplementation implements PdfGenerateService {

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

    private static final String ZONE = "Asia/Kolkata";

    @Override
    public SuccessResponse sendReportToEmail(int reportType) {
        try {
            ByteArrayOutputStream outputStream = generateByteArray(reportType);

            String fileName = switch (reportType){
                case 1 -> "Daily_Orders_Report.pdf";
                case 2 -> "Weekly_Orders_Report.pdf";
                case 3 -> "Monthly_Orders_Report.pdf";
                default -> throw new IllegalArgumentException("Invalid report type");
            };

            Session session = SessionProvider.createSession(mailHost, mailPort, emailUsername, emailPassword,
                    mailStartTlsRequired, mailStartTlsEnable, mailSocketFactoryClass, mailDebug);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddress));
            message.setSubject("Report");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Please find the report attached.");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(outputStream.toByteArray(), "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(fileName);

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

    public ByteArrayOutputStream generateByteArray(int reportType) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC);
        Document doc = new Document(pdfDoc).setFont(font);

        ImageData imageData = ImageDataFactory.create(logoImageUrl);
        Image logoImage = new Image(imageData);
        logoImage.setFixedPosition(pdfDoc.getDefaultPageSize().getWidth()/2-230,pdfDoc.getDefaultPageSize().getHeight()/2-230);
        logoImage.setOpacity(0.28f).setWidth(UnitValue.createPercentValue(120)).setHeight(UnitValue.createPercentValue(80));
        logoImage.setRotationAngle(Math.toRadians(45));

        Color borderColor = new DeviceRgb(0, 0, 0);
        float borderWidth = 2f;

        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new HeaderFooterEventHandler(borderColor, borderWidth));
        String reportLabel;

        LocalDateTime startDate;
        LocalDateTime endDate = switch (reportType) {
            case 1 -> {
                reportLabel = "Daily Order Report";
                startDate = LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1).toLocalDate().atStartOfDay().atZone(ZoneId.of(ZONE)).toLocalDateTime();
                yield LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of(ZONE)).toLocalDateTime();
            }
            case 2 -> {
                reportLabel = "Weekly Order Report";
                startDate = LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusWeeks(1).toLocalDate().atStartOfDay().atZone(ZoneId.of(ZONE)).toLocalDateTime();
                yield LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of(ZONE)).toLocalDateTime();
            }
            case 3 -> {
                reportLabel = "Monthly Order Report";
                startDate = LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusMonths(1).toLocalDate().atStartOfDay().atZone(ZoneId.of(ZONE)).toLocalDateTime();
                yield LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1).toLocalDate().atTime(23, 59, 59).atZone(ZoneId.of(ZONE)).toLocalDateTime();
            }
            default -> throw new IllegalArgumentException("Invalid report type: " + reportType);
        };

        List<OrderEntity> orderEntityList = orderRepository.getReport(startDate, endDate);
        Table companyDetailsTable = new Table(UnitValue.createPercentArray(new float[]{2, 4}));
        PdfFont companyFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC);
        Color fontColor = new DeviceRgb(77,76,76);

        Image watermarkImage = new Image(ImageDataFactory.create(logoImageUrl));
        watermarkImage.setWidth(UnitValue.createPointValue(140)).setHeight(UnitValue.createPercentValue(80)).setHorizontalAlignment(HorizontalAlignment.LEFT);
        companyDetailsTable.addHeaderCell(new Cell().add(watermarkImage)
                .setVerticalAlignment(VerticalAlignment.TOP).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));

        Paragraph companyParagraph = new Paragraph()
                .add(new Text("Cordestitch INDUSTRIES PVT LTD").setFontColor(fontColor).setFont(companyFont).setFontSize(20)).setPaddingLeft(25f)
                .add("\n")
                .add(new Text("Rajanukunte, Bengaluru, 560064 \n").setFontColor(fontColor).setFont(companyFont).setFontSize(12)).setPaddingLeft(25f)
                .add(new Text("GST No: xxxxxxxxx \n\n")).setFontColor(fontColor).setFont(companyFont).setPaddingLeft(25f);
        companyDetailsTable.addHeaderCell(new Cell().add(companyParagraph).setTextAlignment(TextAlignment.RIGHT).setHorizontalAlignment(HorizontalAlignment.RIGHT).setBorder(Border.NO_BORDER));

        companyDetailsTable.setWidth(UnitValue.createPercentValue(100));
        doc.add(logoImage);
        doc.add(companyDetailsTable);

        float[] orderTable = {1,1,1,1,1};
        Table dataTable = new Table(orderTable);
        dataTable.setWidth(500);

        long confirmedOrdersCount = orderEntityList.stream()
                .flatMap(order -> order.getOrderItemEntities().stream())
                .filter(orders -> Optional.ofNullable(orders.getOrderStatus())
                        .map(status -> status.equals(OrderStatus.CONFIRMED))
                        .orElse(false))
                .count();
        long cancelledOrdersCount = orderEntityList.stream()
                .flatMap(order -> order.getOrderItemEntities().stream())
                .filter(orders -> Optional.ofNullable(orders.getOrderStatus())
                        .map(status -> status.equals(OrderStatus.CANCELED))
                        .orElse(false))
                .count();
        long pendingOrdersCount = orderEntityList.stream()
                .flatMap(order -> order.getOrderItemEntities().stream())
                .filter(orders -> Optional.ofNullable(orders.getOrderStatus())
                        .map(status -> status.equals(OrderStatus.PENDING))
                        .orElse(false))
                .count();

        long deliveredOrdersCount = orderEntityList.stream()
                .flatMap(order -> order.getOrderItemEntities().stream())
                .filter(orders -> Optional.ofNullable(orders.getOrderStatus())
                        .map(status -> status.equals(OrderStatus.DELIVERED))
                        .orElse(false))
                .count();
        long returnedOrdersCount = orderEntityList.stream()
                .flatMap(order -> order.getOrderItemEntities().stream())
                .filter(orders -> Optional.ofNullable(orders.getOrderStatus())
                        .map(status -> status.equals(OrderStatus.RETURNED))
                        .orElse(false))
                .count();
        dataTable.addCell(new Cell().add((createStyledCell("Confirmed",font))).setPadding(4f));
        dataTable.addCell(new Cell().add((createStyledCell("Cancelled",font))).setPadding(4f));
        dataTable.addCell(new Cell().add((createStyledCell("Pending",font))).setPadding(4f));
        dataTable.addCell(new Cell().add((createStyledCell("Delivered",font))).setPadding(4f));
        dataTable.addCell(new Cell().add((createStyledCell("Returned",font))).setPadding(4f));

        dataTable.addCell(new Cell().add((createStyledCell(String.valueOf(confirmedOrdersCount),font))).setPadding(4f));
        dataTable.addCell(new Cell().add((createStyledCell(String.valueOf(cancelledOrdersCount),font))).setPadding(4f));
        dataTable.addCell(new Cell().add((createStyledCell(String.valueOf(pendingOrdersCount),font))).setPadding(4f));
        dataTable.addCell(new Cell().add((createStyledCell(String.valueOf(deliveredOrdersCount),font))).setPadding(4f));
        dataTable.addCell(new Cell().add((createStyledCell(String.valueOf(returnedOrdersCount),font))).setPadding(4f));
        dataTable.setTextAlignment(TextAlignment.LEFT)
                .setBorder(Border.NO_BORDER).setHorizontalAlignment(HorizontalAlignment.RIGHT);

        Table dateTable = new Table(1);
        dateTable.setWidth(200);

        Paragraph dateParagraph = new Paragraph();
        dateParagraph.add("\n\n");
        dateParagraph.add("Report Name: ").setFont(companyFont);
        dateParagraph.add(reportLabel).setFont(companyFont);
        dateParagraph.add("\n");

        if (!orderEntityList.isEmpty()) {
            dateParagraph.add("Report Date: ").setFont(companyFont);
            dateParagraph.add(format.format(LocalDateTime.now(ZoneId.of(Constants.ZONE)).atZone(ZoneId.of(ZONE)))).setFont(companyFont);
            dateParagraph.add("\n");
            dateParagraph.add("\n");
            String dateText = String.format("Report Start Date: %s and End Date: %s", startDate, endDate);
            dateParagraph.add(dateText).setFont(companyFont);
            dateParagraph.add("\n");
            dateParagraph.add("\n");
            doc.add(new Cell().add(dateParagraph)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER)
                    .setMarginTop(-10f));

            float orderTableWidth = 260f;
            float dataTableWidth = 50f;
            float tableSpacing = 5f;
            float marginLeft = 36;

            dateTable.setFixedPosition(marginLeft, pdfDoc.getDefaultPageSize().getHeight() - 100, orderTableWidth);
            dataTable.setFixedPosition(marginLeft + orderTableWidth + tableSpacing,
                    pdfDoc.getDefaultPageSize().getHeight() - 200, dataTableWidth);


            doc.add(dateTable);
            doc.add(dataTable);


            float[] bookingInfoColumnWidths = {1, 1, 1, 1, 1, 1, 1, 1, 1};
            Table bookingTable = new Table(bookingInfoColumnWidths);
            bookingTable.setTextAlignment(TextAlignment.CENTER);
            bookingTable.setWidth(UnitValue.createPercentValue(100));
            Color customGray = new DeviceRgb(150,150,150);
            bookingTable.addCell(new Cell().add(createStyledCell("Order ID", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Order Date", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("OrderItem ID", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Order Status", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Total Amount", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Payment Method", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Payment Status", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Payment Date", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            bookingTable.addCell(new Cell().add(createStyledCell("Delivery Status", font)).setBackgroundColor(customGray).setOpacity(0.8f));



            for (OrderEntity entity : orderEntityList) {

                List<OrderItemEntity> orderItemEntity = entity.getOrderItemEntities();
                for(OrderItemEntity orderItem : orderItemEntity) {
                    bookingTable.addCell(new Cell().add(createStyledCell(entity.getOrderId(),font)));
                    bookingTable.addCell(new Cell().add(createStyledCell(format.format(entity.getOrderDate()),font)));
                    bookingTable.addCell(new Cell().add(createStyledCell(String.valueOf(orderItem.getOrderItemId()), font)));
                    bookingTable.addCell(new Cell().add(createStyledCell(String.valueOf(orderItem.getOrderStatus()), font)));
                    bookingTable.addCell(new Cell().add(createStyledCell(String.valueOf(orderItem.getTotalAmount()),font)));
                    bookingTable.addCell(new Cell().add(createStyledCell(entity.getPaymentEntity().getPaymentMethod(),font)));
                    bookingTable.addCell(new Cell().add(createStyledCell(String.valueOf(entity.getPaymentEntity().getPaymentStatus()),font)));
                    bookingTable.addCell(new Cell().add(createStyledCell(entity.getPaymentEntity().getPaymentDate() != null ? format.format(entity.getPaymentEntity().getPaymentDate()) : "N/A",font)));
                    bookingTable.addCell(new Cell().add(createStyledCell(String.valueOf(orderItem.getDeliveryStatus()),font)));

                }
            }

            doc.add(bookingTable);
        } else {
            dateParagraph.add("\n There is no Order Details found on " + format.format(LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1)));
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
        cell.setFontSize(10f);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        return cell;
    }

    public static class HeaderFooterEventHandler extends AbstractPdfDocumentEventHandler implements IEventHandler {
        private final Color borderColor;
        private final float borderWidth;

        public HeaderFooterEventHandler(Color borderColor, float borderWidth) {
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
        }

        @Override
        public void onEvent(IEvent iEvent) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) iEvent;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            if (page == null) {
                return;
            }

            PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
            Rectangle pageSize = page.getPageSize();
            float leftX = pageSize.getLeft() + 10 + borderWidth / 2;
            float rightX = pageSize.getRight() - 10 - borderWidth / 2;
            float topY = pageSize.getTop() - 25 - borderWidth / 2;
            float bottomY = pageSize.getBottom() + 25 + borderWidth / 2;

            canvas.setStrokeColor(borderColor);
            canvas.setLineWidth(borderWidth);
            canvas.moveTo(leftX, topY);
            canvas.lineTo(rightX, topY);
            canvas.stroke();

            canvas.moveTo(leftX, topY);
            canvas.lineTo(leftX, bottomY);
            canvas.stroke();

            canvas.moveTo(rightX, topY);
            canvas.lineTo(rightX, bottomY);
            canvas.stroke();

            canvas.moveTo(leftX, bottomY);
            canvas.lineTo(rightX, bottomY);
            canvas.stroke();
        }

        @Override
        protected void onAcceptedEvent(AbstractPdfDocumentEvent abstractPdfDocumentEvent) {
            log.info("onAcceptedEvent: " + abstractPdfDocumentEvent);
        }

    }
}
