package yunxun.ai.canary.backend.repository.graph.impl;

import org.junit.jupiter.api.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;
import yunxun.ai.canary.backend.repository.graph.GraphRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GraphRepositoryImplTest {

    @Autowired
    private GraphRepository graphRepository;

    @Autowired
    private Driver driver;

    // 定义简单的节点与关系类用于测试
    static class UserNode extends BaseNode {
        public UserNode(String id, String name) {
            super("User");
            this.id = id;
            this.addProperty("name", name);
        }
    }

    static class ProductNode extends BaseNode {
        public ProductNode(String id, String name) {
            super("Product");
            this.id = id;
            this.addProperty("name", name);
        }
    }

    static class BoughtRelationship extends BaseRelationship {
        public BoughtRelationship(String id, BaseNode start, BaseNode end, int quantity) {
            super("BOUGHT", start, end);
            this.id = id;
            this.addProperty("quantity", quantity);
        }
    }

    private final UserNode user = new UserNode("u1", "Alice");
    private final ProductNode product = new ProductNode("p1", "Apple");
    private final BoughtRelationship relationship = new BoughtRelationship("r1", user, product, 2);

    @BeforeEach
    void clearDatabase() {
        try (Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    @Test
    @Order(1)
    void testAddNode() {
        graphRepository.addNode(user);
        try (Session session = driver.session()) {
            var result = session.run("MATCH (n:User {id: 'u1'}) RETURN n.name AS name").single();
            assertThat(result.get("name").asString()).isEqualTo("Alice");
        }
    }

    @Test
    @Order(2)
    void testAddRelationship() {
        graphRepository.addRelationship(relationship);
        try (Session session = driver.session()) {
            var result = session.run(
                    "MATCH (a:User {id: 'u1'})-[r:BOUGHT]->(b:Product {id: 'p1'}) RETURN r.quantity AS q"
            ).single();
            assertThat(result.get("q").asInt()).isEqualTo(2);
        }
    }

    @Test
    @Order(3)
    void testUpdateNodeProperties() {
        graphRepository.addNode(user);
        user.getProperties().put("name", "Alice Updated");
        graphRepository.updateNodeProperties("u1", user);
        try (Session session = driver.session()) {
            var result = session.run("MATCH (n:User {id: 'u1'}) RETURN n.name AS name").single();
            assertThat(result.get("name").asString()).isEqualTo("Alice Updated");
        }
    }

    @Test
    @Order(4)
    void testUpdateRelationshipProperties() {
        graphRepository.addRelationship(relationship);
        relationship.getProperties().put("quantity", 5);
        graphRepository.updateRelationshipProperties("r1", relationship);
        try (Session session = driver.session()) {
            var result = session.run(
                    "MATCH ()-[r:BOUGHT {id: 'r1'}]->() RETURN r.quantity AS q"
            ).single();
            assertThat(result.get("q").asInt()).isEqualTo(5);
        }
    }

    @Test
    @Order(5)
    void testDeleteRelationship() {
        graphRepository.addRelationship(relationship);
        graphRepository.deleteRelationship("r1");
        try (Session session = driver.session()) {
            var count = session.run("MATCH ()-[r:BOUGHT {id: 'r1'}]->() RETURN COUNT(r) AS c")
                    .single().get("c").asInt();
            assertThat(count).isEqualTo(0);
        }
    }

    @Test
    @Order(6)
    void testDeleteNode() {
        graphRepository.addRelationship(relationship);
        graphRepository.deleteNode("u1");
        try (Session session = driver.session()) {
            var count = session.run("MATCH (n {id: 'u1'}) RETURN COUNT(n) AS c")
                    .single().get("c").asInt();
            assertThat(count).isEqualTo(0);
        }
    }
}
