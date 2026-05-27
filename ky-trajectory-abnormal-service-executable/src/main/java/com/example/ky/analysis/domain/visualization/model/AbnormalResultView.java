package com.example.ky.analysis.domain.visualization.model;

import com.example.ky.analysis.domain.common.AbnormalLevel;

/**
 * 异常检测结果摘要展示对象。
 */
public record AbnormalResultView(
        AbnormalLevel abnormalLevel,
        double trackScore,
        double eventScore,
        double finalScore,
        boolean conflictFlag,
        String evidenceSummary,
        String suggestion,
        String algorithmVersion
) {
}
