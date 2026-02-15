package com.tontin.platform.config;

import com.tontin.platform.domain.enums.dart.DartStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Custom converter to handle case-insensitive conversion of String to DartStatus enum.
 *
 * <p>
 * This converter allows API clients to send status values in any case (e.g., "active", "ACTIVE", "Active")
 * and correctly converts them to the corresponding enum value.
 * </p>
 */
@Component
public class StringToDartStatusConverter implements Converter<String, DartStatus> {

    @Override
    public DartStatus convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        try {
            // Convert to uppercase to match enum constant names
            return DartStatus.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // If conversion fails, throw a more descriptive exception
            throw new IllegalArgumentException(
                String.format(
                    "Invalid dart status: '%s'. Valid values are: PENDING, ACTIVE, FINISHED",
                    source
                ),
                e
            );
        }
    }
}
