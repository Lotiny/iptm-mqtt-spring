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
@RestController
@RequestMapping("/api/sensor")
@CrossOrigin(origins = "http://localhost:5173")
class SensorDataController {

    private final MongoTemplate mongoTemplate;

    public SensorDataController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/{date}")
    public List<Document> getSensorDataDate(@PathVariable String date) {
        String collectionName = MqttApplication.collectionPrefix + "sensor_" + date;
        if (!mongoTemplate.collectionExists(collectionName)) {
            return new ArrayList<>();
        }

        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        return collection.find()
                .sort(new Document("timestamp", -1))
                .into(new ArrayList<>());
    }
}
