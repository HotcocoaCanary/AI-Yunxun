package yunxun.ai.canary.backend.data.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data_resources")
public class DataResourceDoc {

    @Id
    private String id;

    private Long ownerId;

    private String type;

    private String title;

    private String visibility; // private | public

    private List<Long> allowedNodes; // chat_session ids

    private Map<String, Object> meta;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
