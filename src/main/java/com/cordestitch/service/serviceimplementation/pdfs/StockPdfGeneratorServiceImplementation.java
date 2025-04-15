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
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.cordestitch.emailhandler.EmailTransport;
import com.cordestitch.emailhandler.SessionProvider;
import com.cordestitch.entity.product.ColorQuantity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.StockQuantity;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.pdfs.StockPdfGeneratorService;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class StockPdfGeneratorServiceImplementation implements StockPdfGeneratorService {

    private final EmailTransport emailTransport;

    private final ProductRepository productRepository;

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
    @Transactional
    public SuccessResponse sendStockPdfReportToEmail()  {
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
            attachmentPart.setFileName("Product_Stock_Report.pdf");

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

        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new HeaderFooterEventHandler(borderColor, borderWidth));
        List<Product> productsData = productRepository.findAll();

        Table companyDetailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 5}));
        PdfFont companyFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC);
        Color fontColor = new DeviceRgb(77,76,76);

        Image watermarkImage = new Image(ImageDataFactory.create(logoImageUrl));
        watermarkImage.setWidth(UnitValue.createPointValue(140)).setHeight(UnitValue.createPercentValue(80)).setHorizontalAlignment(HorizontalAlignment.LEFT);
        companyDetailsTable.addHeaderCell(new Cell().add(watermarkImage).setVerticalAlignment(VerticalAlignment.TOP).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
        Paragraph companyParagraph = new Paragraph()
                .add(new Text("cordestitch INDUSTRIES PVT LTD").setFontColor(fontColor).setFont(companyFont).setFontSize(20))
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
        dateParagraph.add("Product Stock report").setFont(companyFont);
        dateParagraph.add("\n");
        dateParagraph.add("Time: ");
        dateParagraph.add(String.valueOf(LocalTime.now()));
        dateParagraph.add("\n");
        if (!productsData.isEmpty()) {
            dateParagraph.add("Report Date: ");
            dateParagraph.add(format.format(LocalDateTime.now(ZoneId.of(Constants.ZONE))));
            dateParagraph.add("\n");

            dateTable.addCell(new Cell().add(dateParagraph)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER));

            doc.add(dateTable);

            float[] productInfoColumnWidths = {130, 150, 150, 130, 140, 160, 130};
            Table productTable = new Table(productInfoColumnWidths);
            productTable.setTextAlignment(TextAlignment.CENTER);

            Color customGray = new DeviceRgb(150,150,150);
            productTable.addCell(new Cell().add(createStyledCell("Product ID", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            productTable.addCell(new Cell().add(createStyledCell("Product Name", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            productTable.addCell(new Cell().add(createStyledCell("Stock ID", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            productTable.addCell(new Cell().add(createStyledCell("Product Size", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            productTable.addCell(new Cell().add(createStyledCell("Product Price", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            productTable.addCell(new Cell().add(createStyledCell("Color ", font)).setBackgroundColor(customGray).setOpacity(0.8f));
            productTable.addCell(new Cell().add(createStyledCell("Color Quantities", font)).setBackgroundColor(customGray).setOpacity(0.8f));

            createProductStockCell(productTable, productsData, font);

            doc.add(productTable);
        } else {
            dateParagraph.add("\n There is no Product Stock Details found on " + format.format(LocalDateTime.now(ZoneId.of(Constants.ZONE)).minusDays(1)));
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

    private void createProductStockCell(Table productTable, List<Product> productsData, PdfFont font) {
        for (Product entity : productsData) {
            addProductCells(productTable, entity, font);
        }
    }

    private void addProductCells(Table productTable, Product entity, PdfFont font) {
        boolean isFirstStockQuantity = true;
        if (entity.getStockQuantities() != null) {
            for (StockQuantity stockQuantity : entity.getStockQuantities()) {
                isFirstStockQuantity = addStockCells(productTable, entity, stockQuantity, font, isFirstStockQuantity);
            }
        }
    }

    private boolean addStockCells(Table productTable, Product entity, StockQuantity stockQuantity, PdfFont font, boolean isFirstStockQuantity) {
        boolean isFirstColorQuantity = true;
        for (ColorQuantity colorQuantity : stockQuantity.getColorQuantities()) {
            if (isFirstStockQuantity) {
                productTable.addCell(createStyledCell(String.valueOf(entity.getProductId()), font));
                productTable.addCell(createStyledCell(String.valueOf(entity.getProductName()), font));
                isFirstStockQuantity = false;
            } else {
                addEmptyProductCells(productTable, font);
            }
            if (isFirstColorQuantity) {
                productTable.addCell(createStyledCell(stockQuantity.getStockId(), font));
                productTable.addCell(createStyledCell(String.valueOf(stockQuantity.getSize()), font));
                productTable.addCell(createStyledCell(String.valueOf(stockQuantity.getProductPrice()), font));
                isFirstColorQuantity = false;
            } else {
                addEmptyStockCells(productTable, font);
            }

            productTable.addCell(createStyledCell(String.valueOf(colorQuantity.getColor()), font));
            productTable.addCell(createStyledCell(String.valueOf(colorQuantity.getQuantity()), font));
        }
        return isFirstStockQuantity;
    }


    private void addEmptyProductCells(Table productTable, PdfFont font) {
        productTable.addCell(createStyledCell("", font));
        productTable.addCell(createStyledCell("", font));
    }

    private void addEmptyStockCells(Table productTable, PdfFont font) {
        productTable.addCell(createStyledCell("", font));
        productTable.addCell(createStyledCell("", font));
        productTable.addCell(createStyledCell("", font));
    }


    private Cell createStyledCell(String text, PdfFont font) {
        Cell cell = new Cell().add(new Paragraph(text).setFont(font));
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setPadding(5);
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
