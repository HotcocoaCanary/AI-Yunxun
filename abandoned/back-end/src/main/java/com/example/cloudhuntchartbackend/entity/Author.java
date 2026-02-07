package com.example.cloudhuntchartbackend.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Author实体类
 */
@Node(labels = "Author")
@Data
@RequiredArgsConstructor
public class Author {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String name;

    @Property("Nationality")
    private String nationality;

    @Relationship(type = "AUTHORED", direction = Relationship.Direction.OUTGOING)
    private Set<Paper> paperSet=new HashSet<>();
}
