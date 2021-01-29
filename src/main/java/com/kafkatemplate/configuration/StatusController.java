package com.kafkatemplate.configuration;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kafkatemplate.model.message.Message;
import com.kafkatemplate.model.message.StatusCheck;

@RestController
@RequestMapping("api/status")
public class StatusController {

    private final Logger log = LoggerFactory.getLogger(StatusController.class);
    private final BuildProperties properties;
    private final KafkaTemplate<String, Message> kafkaTemplate;
    public static final String SERVER_NAME = InetAddress.getLoopbackAddress().getCanonicalHostName();

    @Value("${spring.kafka.consumer.topic}")
    private String topic;

    public StatusController(BuildProperties properties, KafkaTemplate<String, Message> kafkaTemplate) {
        this.properties = properties;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @GetMapping
    public Status getStatus() {
        return new Status(isKafkaOk(), properties.getName(), SERVER_NAME, getUpTime(), properties.getVersion());
    }

    private String getUpTime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long millis = uptime % 1000;
        long seconds = (uptime / 1000) % 60;
        long minutes = (uptime / (1000 * 60)) % 60;
        long hours = (uptime / (1000 * 60 * 60)) % 24;
        long days = uptime / (1000 * 60 * 60 * 24);
        return String.format("%d.%02d:%02d:%02d:%03d", days, hours, minutes, seconds, millis);
    }

    private String isKafkaOk() {
        try {
            kafkaTemplate.send(topic, new StatusCheck()).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Exception during kafka health check: ", e);
            return "Exception during kafka health check: " + e.toString();
        }
        return "OK";
    }
    
    public static class Status {
        String applicationState;
        String applicationName;
        String serverName;
        String uptime;
        String buildNumber;

        public Status(String applicationState, String applicationName, String serverName, String uptime, String buildNumber) {
            this.applicationState = applicationState;
            this.applicationName = applicationName;
            this.serverName = serverName;
            this.uptime = uptime;
            this.buildNumber = buildNumber;
        }

        public String getApplicationState() {
            return applicationState;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public String getServerName() {
            return serverName;
        }

        public String getUptime() {
            return uptime;
        }

        public String getBuildNumber() {
            return buildNumber;
        }
    }

}