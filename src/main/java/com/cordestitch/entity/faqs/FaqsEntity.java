package com.cordestitch.entity.faqs;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "faqs_data")
@Setter
@Getter
@ToString
public class FaqsEntity {

    @Id
    @Field(name = "faqs_id")
    private String faqsId;

    @Field(name = "question")
    private String question;

    @Field(name = "answer")
    private String answer;

}
