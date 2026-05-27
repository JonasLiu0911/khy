package com.fjkhy.abnormal.application.flow;

import com.fjkhy.abnormal.domain.flow.DetectionStepLog;
import com.fjkhy.abnormal.domain.result.AbnormalResult;
import com.fjkhy.abnormal.domain.task.DetectionTaskRun;

import java.util.List;

public record DetectionFlowRunOutput(
        DetectionTaskRun task,
        List<DetectionStepLog> stepLogs,
        List<AbnormalResult> abnormalResults
) {}
