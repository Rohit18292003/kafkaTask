package org.techpulse.consumer.exception;

import java.sql.SQLException;
import org.springframework.dao.DataIntegrityViolationException;

public final class UniqueConstraintViolationMapper {
    private UniqueConstraintViolationMapper() {
    }

    public static DuplicateEventException toDuplicateIfUniqueViolation(
            DataIntegrityViolationException exception, String entityName) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())) {
                return new DuplicateEventException(
                        "A " + entityName + " already uses this identifier or parent sequence number",
                        exception);
            }
            cause = cause.getCause();
        }
        throw exception;
    }
}
