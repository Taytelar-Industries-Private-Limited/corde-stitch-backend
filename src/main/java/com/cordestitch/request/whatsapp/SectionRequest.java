package com.cordestitch.request.whatsapp;

import lombok.Data;

import java.util.List;

@Data
public class SectionRequest {

    private String title;

    private List<RowRequest> rows;
}