package com.cordestitch.entity.customization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.IOException;
import java.util.List;

@Entity
@Table(name = "customization_data")
@Getter
@Setter
@ToString
public class CustomizationEntity {

    @Id
    @Column(name = "customization_id")
    private String customizationId;

    @Column(name = "pant_type")
    private String pantType;

    @Column(columnDefinition = "jsonb", name = "true_waist_measurement")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode trueWaistMeasurement;

    @Column(columnDefinition = "jsonb", name = "pant_in_seam_length")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode pantInSeamLength;

    @Column(columnDefinition = "jsonb", name = "pant_out_seam_length")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode pantOutSeamLength;

    @Column(columnDefinition = "jsonb", name = "fit_type")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode fitType;

    @Column(columnDefinition = "jsonb", name = "rise_type")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode riseType;

    @Column(columnDefinition = "jsonb", name = "pockets")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode pocketType;

    @Column(columnDefinition = "jsonb", name = "buttons")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode buttonType;

    @Column(columnDefinition = "jsonb", name = "fly_type")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode flyType;

    @Column(columnDefinition = "jsonb", name = "fabric_material")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode fabric;

    @Column(columnDefinition = "jsonb", name = "pant_cuff_type")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode pantCuffsType;

    @Column(columnDefinition = "jsonb", name = "pant_pleats_type")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode pantPleatsType;

    @Column(columnDefinition = "jsonb", name = "customization_image")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode customizationImage;

    @Transient
    private List<Integer> trueWaistMeasurements;

    @Transient
    private List<Integer> pantInSeamLengths;

    @Transient
    private List<Integer> pantOutSeamLengths;

    @Transient
    private List<CustomizationAttribute>fitTypes;

    @Transient
    private List<CustomizationAttribute> riseTypes;

    @Transient
    private PocketTypes pocketTypes;

    @Transient
    private ButtonTypes buttonTypes;

    @Transient
    private List<CustomizationAttribute> flyTypes;

    @Transient
    private List<CustomizationAttribute> pantCuffsTypes;

    @Transient
    private List<CustomizationAttribute> pantPleatsTypes;

    @Transient
    private List<Fabric> fabrics;

    @Transient
    private List<CustomizationAttribute> customizationImages;

    public void convertToJSON() {
        ObjectMapper mapper = new ObjectMapper();
        this.trueWaistMeasurement = mapper.valueToTree(trueWaistMeasurements);
        this.pantInSeamLength = mapper.valueToTree(pantInSeamLengths);
        this.pantOutSeamLength = mapper.valueToTree(pantOutSeamLengths);
        this.fitType = mapper.valueToTree(fitTypes);
        this.riseType = mapper.valueToTree(riseTypes);
        this.pocketType = mapper.valueToTree(pocketTypes);
        this.buttonType = mapper.valueToTree(buttonTypes);
        this.flyType = mapper.valueToTree(flyTypes);
        this.fabric = mapper.valueToTree(fabrics);
        this.pantCuffsType = mapper.valueToTree(pantCuffsTypes);
        this.pantPleatsType = mapper.valueToTree(pantPleatsTypes);
        this.customizationImage = mapper.valueToTree(customizationImages);
    }

    public void convertFromJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.trueWaistMeasurements = mapper.readValue(trueWaistMeasurement.traverse(), new TypeReference<>() {
        });
        this.pantInSeamLengths = mapper.readValue(pantInSeamLength.traverse(), new TypeReference<>() {
        });
        this.pantOutSeamLengths = mapper.readValue(pantOutSeamLength.traverse(), new TypeReference<>() {
        });
        this.fitTypes = mapper.readValue(fitType.traverse(), new TypeReference<>() {
        });
        this.riseTypes = mapper.readValue(riseType.traverse(), new TypeReference<>() {
        });
        this.pocketTypes = mapper.readValue(pocketType.traverse(), new TypeReference<>() {
        });
        this.buttonTypes = mapper.readValue(buttonType.traverse(), new TypeReference<>() {
        });
        this.flyTypes = mapper.readValue(flyType.traverse(), new TypeReference<>() {
        });
        this.fabrics = mapper.readValue(fabric.traverse(), new TypeReference<>() {
        });
        this.pantCuffsTypes = mapper.readValue(pantCuffsType.traverse(), new TypeReference<>() {
        });
        this.pantPleatsTypes = mapper.readValue(pantPleatsType.traverse(), new TypeReference<>() {
        });
        this.customizationImages = mapper.readValue(customizationImage.traverse(), new TypeReference<>() {
        });

    }
}
