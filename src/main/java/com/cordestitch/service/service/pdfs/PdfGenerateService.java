package com.cordestitch.service.service.pdfs;

import com.cordestitch.response.SuccessResponse;

public interface PdfGenerateService {

    SuccessResponse sendReportToEmail(int reportType) ;
}
