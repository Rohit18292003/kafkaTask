package org.techpulse.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class ConsumerApplicationTests {

    @Test
    void applicationEntryPointIsConfigured() {
        org.junit.jupiter.api.Assertions.assertTrue(
                ConsumerApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }

}
