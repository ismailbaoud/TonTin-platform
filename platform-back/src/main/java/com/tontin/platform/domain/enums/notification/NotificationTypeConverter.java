package com.tontin.platform.domain.enums.notification;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists NotificationType using the API value (e.g. dar_invitation)
 * so the database check constraint notifications_type_check is satisfied.
 */
@Converter(autoApply = false)
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {

    @Override
    public String convertToDatabaseColumn(NotificationType attribute) {
        if (attribute == null) return null;
        return attribute.getApiValue();
    }

    @Override
    public NotificationType convertToEntityAttribute(String dbData) {
        return NotificationType.fromApiValue(dbData);
    }
}
