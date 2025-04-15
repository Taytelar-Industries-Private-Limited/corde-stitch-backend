package com.cordestitch.response.homepage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomePageLandscapeImage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String description;

    private String imageUrl;

}
