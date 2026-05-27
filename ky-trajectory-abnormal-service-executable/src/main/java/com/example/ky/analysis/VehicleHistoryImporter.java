package com.example.ky.analysis;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class VehicleHistoryImporter {

    // =========================
    // 1. 基础配置
//            "闽JDB2087", "闽J05558D", "闽J09837D", "闽J09865D", "闽JY5783", "闽J05000D", "闽JDB7627", "闽JDB3965",
//                    "闽JDB8373", "闽JDB7037", "闽JDB2967", "闽JDD8907", "闽JDD7037", "闽JDB8930", "闽JDC8547", "闽JD09651",
//                    "闽JDC5454", "闽JDB9283", "闽JDD9853", "闽JDB1387", "闽JDD7137", "闽JDD8107", "闽JDB7680", "闽JD06573",
//                    "闽JDB0537", "闽JDB7183", "闽JDD8957", "闽JD18301", "闽JDD8153", "闽JDD7252", "闽JDB0815", "闽JDC3747"
    // =========================
    private static final String BASE_URL = "http://120.35.30.240:9999/gps-web/api/get_gps_h_plate.jsp";
    private static final List<String> CAR_PLATES = Arrays.asList(
            "闽JY0669", "闽JY7396", "闽JY7687", "闽JY7389", "闽JY7878", "闽JY0390", "闽JY7695", "闽JY0690",
            "闽JY7593", "闽JY5882", "闽JY0356", "闽JY5588", "闽JY0203", "闽JY7578", "闽J02323D", "闽J09875D",
            "闽J09790D", "闽J05859D", "闽JDB1737", "闽JDD7136", "闽JDD7859", "闽JDD7039", "闽JDD8909", "闽JDD6733",
            "闽JDD7068", "闽JDD7167", "闽JDD7237"
    );
    private static final String SESSION_ID = "swTwAmY7NGK1MHZ0JWYzEmMkV2ZhZGMvBWYvE2N58FZflDa4MGNfFzXn1kN4pzNmxmYFVjTzIlMGNTM3YEN5NmQ4cmYflzXxQGO0djJkR2YglTcyMnOoJmJnVmYjhiNvBmbxIGZoAGaix0YzcjYgt2MUhza3oXNbNWdhdkMY";

    // =========================
    // 2. 数据库配置
    // =========================
    private static final String DB_URL = "jdbc:mysql://116.63.183.174:3306/khy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "KHY@777!FJ512";

    private static final java.time.format.DateTimeFormatter DT_FMT =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String RANGE_START = "20260331000000"; // 20260331000000
    private static final String RANGE_END = "20260504000000";

    public static void main(String[] args) {
        try {
            for (String carPlate : CAR_PLATES) {
                System.out.println("==== 当前车牌号: " + carPlate + " ====");
                java.time.LocalDateTime start = java.time.LocalDateTime.parse(RANGE_START, DT_FMT);
                java.time.LocalDateTime end = java.time.LocalDateTime.parse(RANGE_END, DT_FMT);

                while (start.isBefore(end)) {
                    java.time.LocalDateTime next = start.plusDays(1);
                    if (next.isAfter(end)) next = end;

                    String startStr = start.format(DT_FMT);
                    String endStr = next.format(DT_FMT);

                    System.out.println("==== 拉取区间: " + startStr + " ~ " + endStr + " ====");
                    String json = requestVehicleHistory(carPlate, startStr, endStr);
                    if (json == null || json.isBlank()) {
                        System.out.println("未获取到有效响应。");
                    } else {
                        Object parsed = Json.parse(json);
                        List<TrackPoint> points = TrackPointExtractor.extract(parsed);

                        System.out.println("共提取到轨迹点数量：" + points.size());
                        if (!points.isEmpty()) {
                            System.out.println("前 5 条轨迹点：");
                            points.stream().limit(5).forEach(System.out::println);
                        }

                        insertToMySQL(carPlate, points);
                        System.out.println("写入数据库完成。");
                    }

                    start = next;
                }
            }
        } catch (Exception e) {
            System.err.println("执行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================
    // 3. 请求接口
    // =========================
    private static String requestVehicleHistory(String carPlate, String startTime, String endTime) throws IOException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("carPlate", carPlate);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("sessionId", SESSION_ID);
        params.put("maxSpeed", "100");
        params.put("stopLong", "5");
        params.put("queryAlarm", "false");
        params.put("filter0", "true");
        params.put("filterInvalid", "true");
        params.put("filterAppend", "true");

        String query = buildQuery(params);
        String urlStr = BASE_URL + "?" + query;

        System.out.println("请求地址：");
        System.out.println(urlStr);

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");

        int status = conn.getResponseCode();
        System.out.println("状态码：" + status);

        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String buildQuery(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    // =========================
    // 4. 写入 MySQL
    // =========================
    private static void insertToMySQL(String carPlate, List<TrackPoint> points) throws SQLException {
        String sqlCar = "SELECT id FROM khy_car WHERE license_plate_num = ?";
        String sqlInsert = "INSERT INTO vehicle_gps_points_511 " +
                "(vehicle_id, gps_time, lng, lat, glng, glat, speed, direction) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement psCar = conn.prepareStatement(sqlCar);
             PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {

            psCar.setString(1, carPlate);
            try (ResultSet rs = psCar.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("未找到车牌对应的车辆ID: " + carPlate);
                }
                long vehicleId = rs.getLong("id");
                for (TrackPoint p : points) {
                    psInsert.setLong(1, vehicleId);
                    if (p.time == null || p.time.isBlank()) {
                        psInsert.setNull(2, Types.TIMESTAMP);
                    } else {
                        psInsert.setString(2, p.time);
                    }

                    if (p.lng != null) psInsert.setBigDecimal(3, p.lng); else psInsert.setNull(3, Types.DECIMAL);
                    if (p.lat != null) psInsert.setBigDecimal(4, p.lat); else psInsert.setNull(4, Types.DECIMAL);
                    if (p.glng != null) psInsert.setBigDecimal(5, p.glng); else psInsert.setNull(5, Types.DECIMAL);
                    if (p.glat != null) psInsert.setBigDecimal(6, p.glat); else psInsert.setNull(6, Types.DECIMAL);

                    if (p.speed != null) psInsert.setBigDecimal(7, p.speed); else psInsert.setNull(7, Types.DECIMAL);
                    if (p.direction != null) psInsert.setBigDecimal(8, p.direction); else psInsert.setNull(8, Types.DECIMAL);

                    psInsert.executeUpdate();
                }
            }
        }
    }

    // =========================
    // 5. 轨迹点提取
    // =========================
    static class TrackPoint {
        String time;
        java.math.BigDecimal lng;
        java.math.BigDecimal lat;
        java.math.BigDecimal glng;
        java.math.BigDecimal glat;
        java.math.BigDecimal speed;
        java.math.BigDecimal direction;

        @Override
        public String toString() {
            return "TrackPoint{time='" + time + "', lng=" + lng + ", lat=" + lat +
                    ", glng=" + glng + ", glat=" + glat +
                    ", speed=" + speed + ", direction=" + direction + '}';
        }
    }

    static class TrackPointExtractor {
        private static final List<String> LNG_KEYS = List.of("lng", "lon", "longitude", "baiduLng", "mapLng");
        private static final List<String> LAT_KEYS = List.of("lat", "latitude", "baiduLat", "mapLat");
        private static final List<String> GLNG_KEYS = List.of("glng");
        private static final List<String> GLAT_KEYS = List.of("glat");
        private static final List<String> TIME_KEYS = List.of("time", "gpsTime", "gt", "dateTime", "recvTime", "createTime");

        static List<TrackPoint> extract(Object data) {
            List<TrackPoint> points = new ArrayList<>();
            walk(data, points);
            return points;
        }

        private static void walk(Object obj, List<TrackPoint> points) {
            if (obj == null) return;

            if (obj instanceof List<?> list) {
                for (Object item : list) walk(item, points);
                return;
            }

            if (obj instanceof Map<?, ?> map) {
                String lngKey = findKey(map, LNG_KEYS);
                String latKey = findKey(map, LAT_KEYS);
                String glngKey = findKey(map, GLNG_KEYS);
                String glatKey = findKey(map, GLAT_KEYS);

                if (lngKey != null && latKey != null) {
                    TrackPoint p = new TrackPoint();
                    String timeKey = findKey(map, TIME_KEYS);

                    p.time = timeKey == null ? null : String.valueOf(map.get(timeKey));
                    p.lng = toDecimal(map.get(lngKey));
                    p.lat = toDecimal(map.get(latKey));
                    p.glng = glngKey == null ? null : toDecimal(map.get(glngKey));
                    p.glat = glatKey == null ? null : toDecimal(map.get(glatKey));
                    p.speed = toDecimal(map.get("speed"));
                    Object dir = map.containsKey("direction") ? map.get("direction") : map.get("direct");
                    p.direction = toDecimal(dir);

                    if (p.lng != null && p.lat != null) {
                        points.add(p);
                    }
                }

                for (Object v : map.values()) walk(v, points);
            }
        }

        private static String findKey(Map<?, ?> map, List<String> keys) {
            for (String k : keys) {
                if (map.containsKey(k)) return k;
            }
            return null;
        }

        private static java.math.BigDecimal toDecimal(Object val) {
            if (val == null) return null;
            try {
                return new java.math.BigDecimal(String.valueOf(val));
            } catch (Exception e) {
                return null;
            }
        }
    }

    // =========================
    // 6. 轻量 JSON 解析器（仅支持对象/数组/数字/字符串/布尔/null）
    // =========================
    static class Json {
        private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");

        static Object parse(String json) {
            return new Parser(json).parseValue();
        }

        static class Parser {
            private final String s;
            private int i = 0;

            Parser(String s) { this.s = s.trim(); }

            Object parseValue() {
                skipWs();
                if (peek() == '{') return parseObject();
                if (peek() == '[') return parseArray();
                if (peek() == '"') return parseString();
                if (startsWith("true")) { i += 4; return Boolean.TRUE; }
                if (startsWith("false")) { i += 5; return Boolean.FALSE; }
                if (startsWith("null")) { i += 4; return null; }
                return parseNumber();
            }

            Map<String, Object> parseObject() {
                Map<String, Object> map = new LinkedHashMap<>();
                expect('{');
                skipWs();
                if (peek() == '}') { i++; return map; }

                while (true) {
                    skipWs();
                    String key = parseString();
                    skipWs();
                    expect(':');
                    skipWs();
                    Object val = parseValue();
                    map.put(key, val);
                    skipWs();
                    if (peek() == ',') { i++; continue; }
                    if (peek() == '}') { i++; break; }
                }
                return map;
            }

            List<Object> parseArray() {
                List<Object> list = new ArrayList<>();
                expect('[');
                skipWs();
                if (peek() == ']') { i++; return list; }

                while (true) {
                    skipWs();
                    list.add(parseValue());
                    skipWs();
                    if (peek() == ',') { i++; continue; }
                    if (peek() == ']') { i++; break; }
                }
                return list;
            }

            String parseString() {
                expect('"');
                StringBuilder sb = new StringBuilder();
                while (i < s.length()) {
                    char c = s.charAt(i++);
                    if (c == '"') break;
                    if (c == '\\') {
                        char n = s.charAt(i++);
                        switch (n) {
                            case '"': sb.append('"'); break;
                            case '\\': sb.append('\\'); break;
                            case '/': sb.append('/'); break;
                            case 'b': sb.append('\b'); break;
                            case 'f': sb.append('\f'); break;
                            case 'n': sb.append('\n'); break;
                            case 'r': sb.append('\r'); break;
                            case 't': sb.append('\t'); break;
                            case 'u':
                                String hex = s.substring(i, i + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                                break;
                            default: sb.append(n);
                        }
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }

            Object parseNumber() {
                int start = i;
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if ("0123456789-+.eE".indexOf(c) >= 0) i++;
                    else break;
                }
                String num = s.substring(start, i);
                if (!NUMBER.matcher(num).matches()) return null;
                if (num.contains(".") || num.contains("e") || num.contains("E")) return Double.parseDouble(num);
                try {
                    return Long.parseLong(num);
                } catch (NumberFormatException e) {
                    return Double.parseDouble(num);
                }
            }

            void skipWs() {
                while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            }

            char peek() {
                return i < s.length() ? s.charAt(i) : '\0';
            }

            boolean startsWith(String x) {
                return s.startsWith(x, i);
            }

            void expect(char c) {
                if (peek() != c) throw new RuntimeException("Expected '" + c + "' at " + i);
                i++;
            }
        }
    }
}