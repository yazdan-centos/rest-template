package org.mapnaom.resttemplate.config;

import org.mapnaom.resttemplate.entity.Employee;
import org.mapnaom.resttemplate.entity.Post;
import org.mapnaom.resttemplate.entity.WorkLocation;
import org.mapnaom.resttemplate.repository.EmployeeRepository;
import org.mapnaom.resttemplate.repository.PostRepository;
import org.mapnaom.resttemplate.repository.WorkLocationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final DateTimeFormatter SEED_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.n]");

    private final JsonMapper objectMapper;
    private final EmployeeRepository employeeRepository;
    private final PostRepository postRepository;
    private final WorkLocationRepository workLocationRepository;

    public DataInitializer(JsonMapper objectMapper,
                           EmployeeRepository employeeRepository,
                           PostRepository postRepository,
                           WorkLocationRepository workLocationRepository) {
        this.objectMapper = objectMapper;
        this.employeeRepository = employeeRepository;
        this.postRepository = postRepository;
        this.workLocationRepository = workLocationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        // Employees reference both lookup tables, so delete them first.
        employeeRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        workLocationRepository.deleteAllInBatch();

        Map<Long, Post> posts = loadPosts();
        Map<Long, WorkLocation> locations = loadWorkLocations();
        loadEmployees(posts, locations);
    }

    private Map<Long, Post> loadPosts() throws IOException {
        Map<Long, Post> posts = new HashMap<>();
        for (JsonNode node : readArray("post.json")) {
            Post post = new Post();
            post.setName(required(node, "name"));
            post.setDescription(text(node, "description"));
            post.setCreatedAt(timestamp(node, "created_at"));
            Post saved = postRepository.save(post);
            posts.put(node.get("id").asLong(), saved);
        }
        return posts;
    }

    private Map<Long, WorkLocation> loadWorkLocations() throws IOException {
        Map<Long, WorkLocation> locations = new HashMap<>();
        for (JsonNode node : readArray("work_location.json")) {
            WorkLocation location = new WorkLocation();
            location.setName(required(node, "name"));
            location.setDescription(text(node, "description"));
            location.setCreatedAt(timestamp(node, "created_at"));
            WorkLocation saved = workLocationRepository.save(location);
            locations.put(node.get("id").asLong(), saved);
        }
        return locations;
    }

    private void loadEmployees(Map<Long, Post> posts, Map<Long, WorkLocation> locations) throws IOException {
        for (JsonNode node : readArray("employee.json")) {
            Employee employee = new Employee();
            employee.setPersonnelCode(node.get("personnel_code").asLong());
            employee.setFirstName(required(node, "first_name"));
            employee.setLastName(required(node, "last_name"));
            employee.setFullName(required(node, "full_name"));
            employee.setGender(text(node, "gender"));
            employee.setPost(reference(posts, node, "post_id"));
            employee.setWorkLocation(reference(locations, node, "work_location_id"));
            employee.setCreatedAt(timestamp(node, "created_at"));
            employeeRepository.save(employee);
        }
    }

    private JsonNode readArray(String filename) throws IOException {
        Resource resource = new PathMatchingResourcePatternResolver()
                .getResource("classpath:/static/" + filename);
        return objectMapper.readTree(resource.getInputStream());
    }

    private static String required(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required seed field '" + field + "'");
        }
        return value;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static Instant timestamp(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(value, SEED_TIMESTAMP).toInstant(ZoneOffset.UTC);
        }
    }

    private static <T> T reference(Map<Long, T> references, JsonNode node, String field) {
        JsonNode id = node.get(field);
        if (id == null || id.isNull()) return null;
        T reference = references.get(id.asLong());
        if (reference == null) {
            throw new IllegalStateException("Unknown " + field + " " + id.asLong() + " in employee seed");
        }
        return reference;
    }
}
