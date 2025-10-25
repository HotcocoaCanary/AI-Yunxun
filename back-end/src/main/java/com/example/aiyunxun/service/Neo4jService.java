package com.example.aiyunxun.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Neo4jService {

    ObjectNode getAll(int limit);

    void update();
}