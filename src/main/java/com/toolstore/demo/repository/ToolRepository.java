package com.toolstore.demo.repository;

import com.toolstore.demo.model.Tool;
import com.toolstore.demo.model.ToolType;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ToolRepository {

    private final Map<String, Tool> tools = new HashMap<>();

    public ToolRepository() {
        // Pre-populate with the 4 tools from specification
        tools.put("CHNS", new Tool("CHNS", ToolType.CHAINSAW, "Stihl"));
        tools.put("LADW", new Tool("LADW", ToolType.LADDER, "Werner"));
        tools.put("JAKD", new Tool("JAKD", ToolType.JACKHAMMER, "DeWalt"));
        tools.put("JAKR", new Tool("JAKR", ToolType.JACKHAMMER, "Ridgid"));
    }

    public Optional<Tool> findByCode(String code) {
        return Optional.ofNullable(tools.get(code));
    }

    // Could be useful?
//    public Map<String, Tool> findAll() {
//        return new HashMap<>(tools);
//    }
}