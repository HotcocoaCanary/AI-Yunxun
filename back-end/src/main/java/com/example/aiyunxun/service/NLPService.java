package com.example.aiyunxun.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface NLPService {
    ObjectNode answer(String question);
}
