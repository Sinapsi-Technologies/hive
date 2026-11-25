package io.sinapsi.hive.mcp.monitor;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public final class OperationRegistry {
    private static final int MAX_RECORDS = 200;
    private final Deque<OperationRecord> records = new ConcurrentLinkedDeque<>();

    public String start(String tool, String projectRoot) {
        String id = UUID.randomUUID().toString();
        records.addFirst(new OperationRecord(id, tool, projectRoot, "RUNNING", Instant.now(), null, 0, ""));
        trim();
        return id;
    }

    public void complete(String id, String status, long durationMillis, String error) {
        for (OperationRecord current : records) {
            if (current.id().equals(id)) {
                records.remove(current);
                records.addFirst(new OperationRecord(current.id(), current.tool(), current.projectRoot(),
                        status, current.startedAt(), Instant.now(), durationMillis, error == null ? "" : error));
                return;
            }
        }
    }

    public void rename(String id, String tool) {
        if (tool == null || tool.isBlank()) {
            return;
        }
        for (OperationRecord current : records) {
            if (current.id().equals(id)) {
                records.remove(current);
                records.addFirst(new OperationRecord(current.id(), tool, current.projectRoot(), current.status(),
                        current.startedAt(), current.finishedAt(), current.durationMillis(), current.error()));
                return;
            }
        }
    }

    public List<OperationRecord> recent() {
        return List.copyOf(new ArrayList<>(records));
    }

    private void trim() {
        while (records.size() > MAX_RECORDS) {
            records.removeLast();
        }
    }
}
