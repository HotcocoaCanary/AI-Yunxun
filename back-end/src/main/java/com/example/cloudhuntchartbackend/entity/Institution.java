package com.example.cloudhuntchartbackend.entity;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Institution实体类
 */
@Node(labels = "Institution")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Institution {

    @Id
    @GeneratedValue // Id自增
    private Long id;

    @NonNull
    private String name;

    @Relationship(type = "AFFILIATED_WITH", direction = Relationship.Direction.INCOMING)
    private Set<Author> authorSet=new HashSet<>();
}
