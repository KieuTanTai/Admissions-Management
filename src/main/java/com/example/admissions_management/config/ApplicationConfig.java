package com.example.admissions_management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình cho ứng dụng hybrid (Swing + Web MVC)
 */
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig {

    private SwingConfig swing = new SwingConfig();

    public SwingConfig getSwing() {
        return swing;
    }

    public void setSwing(SwingConfig swing) {
        this.swing = swing;
    }

    public static class SwingConfig {
        private boolean enabled = false;
        private String title = "Admissions Admin Console";
        private int width = 860;
        private int height = 520;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}

