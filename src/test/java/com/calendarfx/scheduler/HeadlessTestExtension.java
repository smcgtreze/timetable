package com.calendarfx.scheduler;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extension to configure JavaFX for headless testing environments
 */
public class HeadlessTestExtension implements BeforeAllCallback {
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("prism.useSoftwareRenderer", "true");
        System.setProperty("prism.order", "sw");
    }
}
