package com.example.ticket_helpdesk_backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        try {
            Field startField = value.getClass().getDeclaredField("start");
            Field endField = value.getClass().getDeclaredField("end");

            startField.setAccessible(true);
            endField.setAccessible(true);

            Object startObj = startField.get(value);
            Object endObj = endField.get(value);

            if (startObj == null || endObj == null) return true;

            if (startObj instanceof LocalDateTime && endObj instanceof LocalDateTime) {
                LocalDateTime start = (LocalDateTime) startObj;
                LocalDateTime end = (LocalDateTime) endObj;

                // Nếu start >= end thì gắn lỗi vào field "end"
                if (!start.isBefore(end)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("End time must be after start time")
                            .addPropertyNode("end") //
                            .addConstraintViolation();
                    return false;
                }
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Nếu không có field start/end thì bỏ qua
            return true;
        }

        return true;
    }
}
