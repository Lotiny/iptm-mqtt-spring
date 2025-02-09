package me.lotiny.mqtt.data;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    public List<Document> getSensorData() {
        return mongoTemplate.getCollection("sensorData")
                .find()
                .sort(new Document("timestamp", -1))
                .limit(50)
                .into(new java.util.ArrayList<>());
    }
}

