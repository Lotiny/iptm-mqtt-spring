package me.lotiny.mqtt.data;

import com.mongodb.client.MongoCollection;
import me.lotiny.mqtt.MqttApplication;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lotiny
 * @since 2/9/2025
 */
@RestController // Indicates that this class is a REST controller
@RequestMapping("/api/sensor") // Base path for all request mappings in this controller
@CrossOrigin(origins = "http://localhost:5173") // Allows cross-origin requests from the specified origin
class SensorDataController {

    private final MongoTemplate mongoTemplate; // Instance of MongoTemplate for database operations

    /**
     * Constructor for SensorDataController.
     *
     * @param mongoTemplate The MongoTemplate instance to use for database operations.
     */
    public SensorDataController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Retrieves sensor data for a specific date.
     *
     * @param date The date for which to retrieve sensor data (format: dd_MM_yy).
     * @return A list of MongoDB Documents containing the sensor data.
     */
    @GetMapping("/{date}") // Handles GET requests to /api/sensor/{date}
    public List<Document> getSensorDataDate(@PathVariable String date) {
        // Construct the collection name based on the provided date
        String collectionName = MqttApplication.collectionPrefix + "sensor_" + date;
        // Check if the collection exists
        if (!mongoTemplate.collectionExists(collectionName)) {
            // If the collection doesn't exist, return an empty list
            return new ArrayList<>();
        }

        // Get the collection from MongoDB
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        // Retrieve all documents from the collection, sorted by timestamp in descending order
        return collection.find()
                .sort(new Document("timestamp", -1)) // Sort by timestamp, descending (-1)
                .into(new ArrayList<>()); // Convert the result to a list
    }
}