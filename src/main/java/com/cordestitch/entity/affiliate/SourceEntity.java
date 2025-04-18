package com.cordestitch.entity.affiliate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SourceEntity {

    @Field(name = "platform")
    private String platform;

    @Field(name = "count")
    private Integer count;
}
