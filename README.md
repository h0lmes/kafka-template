# Running application

Run Zookeeper.

Run Kafka (version should be >= 2.5.1).

Run application:

    mvnw clean compile
    mvnw spring-boot:run
    
# Testing application

You should be able to test application with Kafka Tool (https://www.kafkatool.com/download.html)

1. Run Zookeeper and Kafka.
2. Run the application.
3. Run Kafka Tool.
4. Connect to your cluster.
5. In Kafka Tool Browser select: Topics - kt.incoming, Properties tab.
6. Set String for Key and Message and press Update.
7. In Kafka Tool Browser select: Topics - kt.incoming - Partitions - Partition 0.
8. Select Data tab.
9. Press "Add message" button - "Add single message".
10. Check "Enter manually [Text]" radio buttons.
11. In the "Message" text area input the following:
        
        {"event":"created","status":"ok", "payload":{"id":"11111111-1111-4000-1111-111111111111","value":"22222222-2222-4000-2222-222222222222"},"emitted_at":"2021-01-29T12:00:00.000Z","trace_id":"00000000-0000-4000-0000-000000000000"}
        
12. Check that message of type CREATED has been received in application logs and message of type LINKED has been sent.
13. Execute GET request to http://localhost:8080/api/status to see service status.