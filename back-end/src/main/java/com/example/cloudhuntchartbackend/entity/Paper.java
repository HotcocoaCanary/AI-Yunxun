package com.example.cloudhuntchartbackend.entity;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.*;

/**
 * Paper实体类
 */
@Node(labels = "Paper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Paper {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private Integer pmid;

    @Property("Title")
    private String title;

    @Property("Date")
    private String date;

    @Property("Keyword")
    private String keyword;

    @Property("Journal")
    private String journal;

    @Relationship(type = "CITES", direction = Relationship.Direction.OUTGOING)
    private Set<Paper> paperSet=new HashSet<>();
}
