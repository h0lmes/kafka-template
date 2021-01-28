# Running application

Run Zookeeper.

Run Kafka (version should be >= 2.5.1).

Run application:

    mvnw clean compile
    mvnw spring-boot:run
    
# Testing application

You should be able to test application with Kafka Tool (https://www.kafkatool.com/download.html)

For test payload see ``KafkaProducerAndConsumerTest.java``