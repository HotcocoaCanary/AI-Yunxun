package com.example.cloudhuntchartbackend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node(labels = "Country")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Country {

    @Id
    @GeneratedValue // Id自增
    private Long id;

    @NonNull
    private String name;

    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.INCOMING)
    private Set<Institution> institutionSet=new HashSet<>();

}