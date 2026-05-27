package com.fjkhy.abnormal.application.flow;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.flow.*;
import com.fjkhy.abnormal.infrastructure.jdbc.DetectionConfigJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DetectionFlowConfigService {
    private final DetectionConfigJdbcRepository repository;
    private final Map<String, DetectionFlow> flows = new ConcurrentHashMap<>();
    private final Map<String, List<DetectionFlowNode>> nodes = new ConcurrentHashMap<>();
    private final Map<String, DetectionThreshold> thresholds = new ConcurrentHashMap<>();
    private final List<DetectionRuleView> rules = new ArrayList<>();

    public DetectionFlowConfigService(DetectionConfigJdbcRepository repository) {
        this.repository = repository;
        initDefaults();
        if (repository.configured()) {
            seedDefaultsIfEmpty();
        }
    }

    private void seedDefaultsIfEmpty() {
        try {
            if (repository.countFlows() > 0) {
                return;
            }
            flows.values().forEach(repository::upsertFlow);
            nodes.values().forEach(list -> list.forEach(repository::upsertNode));
            thresholds.values().forEach(repository::upsertThreshold);
            rules.forEach(repository::upsertRule);
        } catch (Exception ignored) {
        }
    }

    private void initDefaults() {
        addFlow(new DetectionFlow("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "车辆月度运行合规异常检测流程", ObjectType.VEHICLE,
                "任务级流程，包含轨迹数据质量、过站数据质量、长期无运行、轨迹规律性、轨迹与过站一致性等多个节点。", true, "V6.0"));
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "BASIC_DATA_QUALITY", "基础数据质量检查", DetectionFlowNodeType.DATA_QUALITY_CHECK, "BASIC_DATA_QUALITY_CHECKER", 10);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "TRACK_DATA_QUALITY", "轨迹数据质量检查", DetectionFlowNodeType.DATA_QUALITY_CHECK, "TRACK_DATA_QUALITY_CHECKER", 20);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "STATION_EVENT_DATA_QUALITY", "过站数据质量检查", DetectionFlowNodeType.DATA_QUALITY_CHECK, "STATION_EVENT_DATA_QUALITY_CHECKER", 30);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "LONG_TERM_NO_OPERATION", "长期无运行证据检测", DetectionFlowNodeType.BUSINESS_DETECTION, "VEHICLE_LONG_TERM_NO_OPERATION", 40);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "TRACK_REGULARITY", "轨迹规律性异常检测", DetectionFlowNodeType.BUSINESS_DETECTION, "VEHICLE_TRACK_REGULARITY", 50);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "SERVICE_RANGE_OUT_OF_SCOPE", "车辆跨乡镇/跨县运行辅助检测（服务范围外运行）", DetectionFlowNodeType.BUSINESS_DETECTION, "VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE", 60);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "TRACK_STATION_CONSISTENCY", "轨迹与过站记录一致性核验", DetectionFlowNodeType.BUSINESS_DETECTION, "VEHICLE_TRACK_STATION_EVENT_CONSISTENCY", 70);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "RISK_MAPPING", "疑似风险等级映射", DetectionFlowNodeType.RISK_MAPPING, null, 90);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "EVIDENCE_BINDING", "证据绑定", DetectionFlowNodeType.EVIDENCE_BINDING, null, 100);
        addNode("VEHICLE_MONTHLY_COMPLIANCE_FLOW", "RESULT_PERSISTENCE", "结果入库", DetectionFlowNodeType.RESULT_PERSISTENCE, null, 110);

        addFlow(new DetectionFlow("COUNTY_MONTHLY_INTEGRATED_FLOW", "区县月度运营合规综合检测流程", ObjectType.ALL,
                "支持多种异常按配置顺序执行，包括站点、车辆、线路和跨对象履约辅助分析。", true, "V6.0"));
        addNode("COUNTY_MONTHLY_INTEGRATED_FLOW", "BASIC_DATA_QUALITY", "基础数据质量检查", DetectionFlowNodeType.DATA_QUALITY_CHECK, "BASIC_DATA_QUALITY_CHECKER", 10);
        addNode("COUNTY_MONTHLY_INTEGRATED_FLOW", "STATION_SETTING", "站点设置条件异常检测", DetectionFlowNodeType.BUSINESS_DETECTION, "STATION_SETTING_CONDITION", 20);
        addNode("COUNTY_MONTHLY_INTEGRATED_FLOW", "VEHICLE_TRACK_REGULARITY", "车辆轨迹规律性异常检测", DetectionFlowNodeType.BUSINESS_DETECTION, "VEHICLE_TRACK_REGULARITY", 30);
        addNode("COUNTY_MONTHLY_INTEGRATED_FLOW", "VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE", "车辆服务范围外运行辅助检测", DetectionFlowNodeType.BUSINESS_DETECTION, "VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE", 40);
        addNode("COUNTY_MONTHLY_INTEGRATED_FLOW", "VEHICLE_TRACK_STATION_CONSISTENCY", "轨迹与过站一致性检测", DetectionFlowNodeType.BUSINESS_DETECTION, "VEHICLE_TRACK_STATION_EVENT_CONSISTENCY", 45);
        addNode("COUNTY_MONTHLY_INTEGRATED_FLOW", "LINE_STATION_COUNT", "线路站点数量异常检测", DetectionFlowNodeType.BUSINESS_DETECTION, "LINE_STATION_COUNT", 50);
        addNode("COUNTY_MONTHLY_INTEGRATED_FLOW", "FULFILLMENT_PACKAGE_NO_VEHICLE", "有件无车辅助核验", DetectionFlowNodeType.CROSS_OBJECT_ANALYSIS, "FULFILLMENT_PACKAGE_NO_VEHICLE", 60);
        addNode("COUNTY_MONTHLY_INTEGRATED_FLOW", "SUMMARY_GENERATION", "异常摘要生成", DetectionFlowNodeType.SUMMARY_GENERATION, null, 120);

        addThreshold(new DetectionThreshold("ARRIVAL_DISTANCE_M", "到站距离阈值", "车辆类阈值", "300", "米", "用于判断轨迹点是否到达站点附近。"));
        addThreshold(new DetectionThreshold("TRACK_GAP_MIN", "轨迹断点时间间隔", "车辆类阈值", "30", "分钟", "用于提示轨迹缺失和断点。"));
        addThreshold(new DetectionThreshold("SERVICE_RANGE_STATION_RADIUS_M", "绑定线路站点邻近阈值", "车辆类阈值", "500", "米", "用于判断车辆是否接近其绑定线路服务站点。"));
        addThreshold(new DetectionThreshold("SERVICE_RANGE_CORRIDOR_RADIUS_M", "绑定线路服务走廊阈值", "车辆类阈值", "1000", "米", "用于判断车辆是否在绑定线路站点序列形成的简化服务走廊附近。"));
        addThreshold(new DetectionThreshold("SERVICE_RANGE_OUTSIDE_RATIO", "服务范围外轨迹点比例阈值", "车辆类阈值", "0.60", "比例", "用于触发疑似线路服务范围外运行。"));
        addThreshold(new DetectionThreshold("SERVICE_RANGE_OUTSIDE_DURATION_MIN", "连续偏离最小时长", "车辆类阈值", "15", "分钟", "用于触发连续偏离绑定线路服务范围提示。"));
        addThreshold(new DetectionThreshold("NO_OPERATION_DAYS", "长期无运行证据天数", "车辆类阈值", "30", "天", "用于长期无轨迹、无过站或无运营记录提示。"));
        addThreshold(new DetectionThreshold("LINE_STATION_MAX", "线路站点数量上限", "线路类阈值", "16", "个", "用于线路站点数量异常检测。"));

        rules.add(new DetectionRuleView("VEHICLE_TRACK_REGULARITY_RULE", "车辆月度轨迹规律性异常规则", "VEHICLE", "车辆资质与运行合规异常", "运行轨迹无规律", "VEHICLE_TRACK_REGULARITY", true, "V6.0", "轨迹、线路、站点序列"));
        rules.add(new DetectionRuleView("VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE_RULE", "车辆疑似线路服务范围外运行规则", "VEHICLE", "车辆资质与运行合规异常", "疑似线路服务范围外运行", "VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE", true, "V7.0", "车辆绑定线路、线路站点序列、全量站点点位、轨迹、过站"));
        rules.add(new DetectionRuleView("VEHICLE_TRACK_STATION_CONSISTENCY_RULE", "轨迹与过站事件一致性规则", "VEHICLE", "车辆运行证据一致性异常", "轨迹与过站事件一致性异常", "VEHICLE_TRACK_STATION_EVENT_CONSISTENCY", true, "V6.0", "轨迹、过站、站点坐标"));
        rules.add(new DetectionRuleView("LINE_STATION_COUNT_RULE", "线路站点数量异常规则", "LINE", "线路配置与执行合规异常", "线路站点数量异常", "LINE_STATION_COUNT", true, "V6.0", "线路站点序列"));
    }

    private void addFlow(DetectionFlow flow) { flows.put(flow.getFlowCode(), flow); nodes.putIfAbsent(flow.getFlowCode(), new ArrayList<>()); }
    private void addNode(String flowCode, String nodeCode, String nodeName, DetectionFlowNodeType type, String detectorCode, int orderNo) {
        nodes.computeIfAbsent(flowCode, k -> new ArrayList<>()).add(new DetectionFlowNode(flowCode, nodeCode, nodeName, type, detectorCode,
                detectorCode == null ? null : detectorCode + "_RULE", orderNo, true, true, DetectionFailPolicy.CONTINUE));
    }
    private void addThreshold(DetectionThreshold t) { thresholds.put(t.getThresholdCode(), t); }

    public List<DetectionFlow> listFlows() {
        if (repository.configured()) {
            return repository.listFlows();
        }
        return flows.values().stream().sorted(Comparator.comparing(DetectionFlow::getFlowCode)).toList();
    }
    public DetectionFlow getFlow(String flowCode) {
        if (repository.configured()) {
            return repository.getFlow(flowCode);
        }
        return Optional.ofNullable(flows.get(flowCode)).orElseThrow(() -> new IllegalArgumentException("流程不存在：" + flowCode));
    }
    public List<DetectionFlowNode> listNodes(String flowCode) {
        if (repository.configured()) {
            return repository.listNodes(flowCode);
        }
        return nodes.getOrDefault(flowCode, List.of()).stream().sorted(Comparator.comparingInt(DetectionFlowNode::getOrderNo)).toList();
    }
    public List<DetectionRuleView> listRules() {
        if (repository.configured()) {
            return repository.listRules();
        }
        return rules;
    }
    public List<DetectionThreshold> listThresholds() {
        if (repository.configured()) {
            return repository.listThresholds();
        }
        return thresholds.values().stream().sorted(Comparator.comparing(DetectionThreshold::getThresholdCode)).toList();
    }
    public DetectionFlow createFlow(DetectionFlow flow) {
        flow.setEnabled(true);
        if (repository.configured()) {
            repository.upsertFlow(flow);
            return flow;
        }
        flows.put(flow.getFlowCode(), flow);
        nodes.putIfAbsent(flow.getFlowCode(), new ArrayList<>());
        return flow;
    }
    public DetectionFlow updateFlow(DetectionFlow flow) {
        if (repository.configured()) {
            repository.upsertFlow(flow);
            return flow;
        }
        flows.put(flow.getFlowCode(), flow);
        nodes.putIfAbsent(flow.getFlowCode(), new ArrayList<>());
        return flow;
    }
    public void deleteFlow(String flowCode) {
        if (repository.configured()) {
            repository.deleteFlow(flowCode);
            return;
        }
        flows.remove(flowCode);
        nodes.remove(flowCode);
    }
    public DetectionFlowNode addNode(DetectionFlowNode node) {
        if (repository.configured()) {
            repository.upsertNode(node);
            return node;
        }
        nodes.computeIfAbsent(node.getFlowCode(), k -> new ArrayList<>()).add(node);
        return node;
    }
    public DetectionFlowNode updateNode(DetectionFlowNode node) {
        if (repository.configured()) {
            repository.upsertNode(node);
            return node;
        }
        nodes.computeIfAbsent(node.getFlowCode(), k -> new ArrayList<>()).add(node);
        return node;
    }
    public void deleteNode(String flowCode, String nodeCode) {
        if (repository.configured()) {
            repository.deleteNode(flowCode, nodeCode);
            return;
        }
        listNodes(flowCode).stream().filter(n -> n.getNodeCode().equals(nodeCode)).findFirst().ifPresent(n -> nodes.get(flowCode).remove(n));
    }
    public void setNodeEnabled(String flowCode, String nodeCode, boolean enabled) {
        if (repository.configured()) {
            repository.updateNodeEnabled(flowCode, nodeCode, enabled);
            return;
        }
        listNodes(flowCode).stream().filter(n -> n.getNodeCode().equals(nodeCode)).findFirst().ifPresent(n -> n.setEnabled(enabled));
    }
    public void reorderNode(String flowCode, String nodeCode, int orderNo) {
        if (repository.configured()) {
            repository.updateNodeOrder(flowCode, nodeCode, orderNo);
            return;
        }
        listNodes(flowCode).stream().filter(n -> n.getNodeCode().equals(nodeCode)).findFirst().ifPresent(n -> n.setOrderNo(orderNo));
    }
    public DetectionThreshold upsertThreshold(DetectionThreshold threshold) {
        if (repository.configured()) {
            repository.upsertThreshold(threshold);
            return threshold;
        }
        thresholds.put(threshold.getThresholdCode(), threshold);
        return threshold;
    }
    public void deleteThreshold(String thresholdCode) {
        if (repository.configured()) {
            repository.deleteThreshold(thresholdCode);
            return;
        }
        thresholds.remove(thresholdCode);
    }
    public DetectionRuleView upsertRule(DetectionRuleView rule) {
        if (repository.configured()) {
            repository.upsertRule(rule);
            return rule;
        }
        rules.removeIf(r -> r.ruleCode().equals(rule.ruleCode()));
        rules.add(rule);
        return rule;
    }
    public void deleteRule(String ruleCode) {
        if (repository.configured()) {
            repository.deleteRule(ruleCode);
            return;
        }
        rules.removeIf(r -> r.ruleCode().equals(ruleCode));
    }
}
