package com.cordestitch.entity.homepage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "homepage_data")
@Getter
@Setter
@ToString
public class HomePageEntity {

    @Id
    @Column(name = "home_page_id")
    private String homePageId;

    @Column(name = "description")
    private String description;

    @Column(name = "home_page_image_url")
    private String homePageImageUrl;
}
