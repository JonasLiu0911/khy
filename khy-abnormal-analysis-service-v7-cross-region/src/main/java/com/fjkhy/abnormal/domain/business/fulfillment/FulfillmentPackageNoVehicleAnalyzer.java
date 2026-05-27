package com.fjkhy.abnormal.domain.business.fulfillment;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentPackageNoVehicleAnalyzer extends AbstractDemoDetector {
    @Override public String detectorCode() { return "FULFILLMENT_PACKAGE_NO_VEHICLE"; }
    @Override public String detectorName() { return "有件无车辅助核验"; }
    @Override public String objectTypeName() { return "跨对象履约"; }
    @Override public String abnormalCategory() { return "跨对象服务履约辅助分析"; }
    @Override public String abnormalSubtype() { return "有件无车"; }
    @Override protected ObjectType objectType() { return ObjectType.FULFILLMENT; }
}
