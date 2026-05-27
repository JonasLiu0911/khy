package com.example.ky.analysis.domain.task.port;

import com.example.ky.analysis.domain.task.AnalysisTaskLog;

import java.util.List;

public interface AnalysisTaskLogPort {
    void save(AnalysisTaskLog log);
    void update(AnalysisTaskLog log);
    List<AnalysisTaskLog> queryAll();
}
