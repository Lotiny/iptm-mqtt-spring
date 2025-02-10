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
import java.util.Random;

/**
 * @author Lotiny
 * @since 2/9/2025
 */
@SpringBootApplication
@EnableMongoRepositories
public class MqttApplication {

    public static Dotenv dotenv;
    public static String collectionPrefix;

    public static void main(String[] args) {
        dotenv = Dotenv.load();
        collectionPrefix = dotenv.get("MONGODB_COLLECTION_PREFIX");
        SpringApplication.run(MqttApplication.class, args);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        String mongoUri = dotenv.get("MONGODB_HOST");
        String database = dotenv.get("MONGODB_DATABASE");
        return new MongoTemplate(MongoClients.create(mongoUri), database);
    }

    @Bean
    public CommandLineRunner mqttListener(MongoTemplate mongoTemplate) {
        return args -> {
            String broker = "wss://" + dotenv.get("HIVEMQ_BROKER");
            String topic = "sensor/data";
            String clientId = "SpringBootClient";
            String username = dotenv.get("HIVEMQ_USER");
            String password = dotenv.get("HIVEMQ_PASSWORD");

            MqttClient client = new MqttClient(broker, clientId, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setCleanSession(true);

            client.connect(options);
            client.subscribe(topic, (t, message) -> {
                String collectionName = collectionPrefix + "sensor_" + new SimpleDateFormat("dd_MM_yy").format(new Date());
                String payload = new String(message.getPayload());
                System.out.println("Received MQTT Message: " + payload);
                Document doc = Document.parse(payload);
                doc.append("timestamp", System.currentTimeMillis());
                mongoTemplate.getCollection(collectionName).insertOne(doc);
            });
        };
    }
}

