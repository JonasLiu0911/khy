package com.fjkhy.abnormal.application.flow;

import com.fjkhy.abnormal.domain.flow.DetectionStepLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DetectionStepLogService {
    private final List<DetectionStepLog> logs = new CopyOnWriteArrayList<>();
    public void save(DetectionStepLog log) { logs.add(log); }
    public List<DetectionStepLog> listByTask(String taskId) { return logs.stream().filter(l -> l.getTaskId().equals(taskId)).toList(); }
    public List<DetectionStepLog> all() { return List.copyOf(logs); }
}
