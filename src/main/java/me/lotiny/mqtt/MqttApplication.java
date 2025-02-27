package me.lotiny.mqtt;

import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lotiny
 * @since 2/9/2025
 */
@SpringBootApplication // Indicates this is a Spring Boot application
@EnableMongoRepositories // Enables Spring Data MongoDB repositories
public class MqttApplication {

    public static Dotenv dotenv; // Static variable to hold the Dotenv instance for environment variables
    public static String collectionPrefix; // Static variable to hold the MongoDB collection prefix

    public static void main(String[] args) {
        // Load environment variables from .env file
        dotenv = Dotenv.load();
        // Get the collection prefix from environment variables
        collectionPrefix = dotenv.get("MONGODB_COLLECTION_PREFIX");
        // Start the Spring Boot application
        SpringApplication.run(MqttApplication.class, args);
    }

    /**
     * Creates and configures a MongoTemplate bean.
     * This bean is used to interact with MongoDB.
     *
     * @return A configured MongoTemplate instance.
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        // Retrieve MongoDB connection URI from environment variables
        String mongoUri = dotenv.get("MONGODB_HOST");
        // Retrieve database name from environment variables
        String database = dotenv.get("MONGODB_DATABASE");
        // Create and return a new MongoTemplate instance
        return new MongoTemplate(MongoClients.create(mongoUri), database);
    }

    /**
     * Creates a CommandLineRunner bean that listens for MQTT messages.
     * This runner executes after the application starts.
     *
     * @param mongoTemplate The MongoTemplate instance to use for database operations.
     * @return A CommandLineRunner that subscribes to an MQTT topic.
     */
    @Bean
    public CommandLineRunner mqttListener(MongoTemplate mongoTemplate) {
        return args -> {
            // Retrieve MQTT broker address from environment variables
            String broker = dotenv.get("MQTT_BROKER");
            // Retrieve MQTT topic to subscribe to from environment variables
            String topic = dotenv.get("MQTT_TOPIC");
            // Retrieve MQTT client ID from environment variables
            String clientId = dotenv.get("MQTT_CLIENT_ID");
            // Retrieve MQTT username from environment variables
            String username = dotenv.get("MQTT_USER");
            // Retrieve MQTT password from environment variables
            String password = dotenv.get("MQTT_PASSWORD");

            // Try to connect to the MQTT broker and subscribe to the topic
            try (MqttClient client = new MqttClient(broker, clientId, null)) {
                // Configure MQTT connection options
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                options.setCleanSession(true); // Clean session: start with a fresh session

                // Connect to the MQTT broker
                client.connect(options);
                // Subscribe to the specified MQTT topic
                client.subscribe(topic, (t, message) -> {
                    // Define the collection name based on the current date
                    String collectionName = collectionPrefix + "sensor_" + new SimpleDateFormat("dd_MM_yy").format(new Date());
                    // Get the message payload as a string
                    String payload = new String(message.getPayload());
                    // Print the received message to the console
                    System.out.println("Received MQTT Message: \n" + payload);
                    // Parse the payload into a MongoDB Document
                    Document document = Document.parse(payload);
                    // Add a timestamp to the document
                    document.append("timestamp", System.currentTimeMillis());
                    // Insert the document into the specified collection
                    mongoTemplate.getCollection(collectionName).insertOne(document);
                });
            } catch (Exception e) {
                // Handle any exceptions that occur during MQTT operations
                e.fillInStackTrace();
            }
        };
    }
}