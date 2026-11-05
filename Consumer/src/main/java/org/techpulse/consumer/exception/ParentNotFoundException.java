package org.techpulse.consumer.exception;

import java.util.UUID;

public class ParentNotFoundException extends RuntimeException {
    public ParentNotFoundException(String parentType, UUID parentId) {
        super(parentType + " not found: " + parentId);
    }
}
