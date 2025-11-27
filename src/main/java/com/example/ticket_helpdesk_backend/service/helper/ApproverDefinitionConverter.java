package com.example.ticket_helpdesk_backend.service.helper;

import com.example.ticket_helpdesk_backend.model.ApproverDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ApproverDefinitionConverter implements AttributeConverter<ApproverDefinition, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ApproverDefinition value) {
        try {
            if (value == null) return null;
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("JSON write error", e);
        }
    }

    @Override
    public ApproverDefinition convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return null;
            }
            return objectMapper.readValue(dbData, ApproverDefinition.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON read error: " + dbData, e);
        }
    }

}

