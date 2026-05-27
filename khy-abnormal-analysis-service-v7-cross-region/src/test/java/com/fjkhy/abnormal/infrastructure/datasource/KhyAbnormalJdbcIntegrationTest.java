package com.fjkhy.abnormal.infrastructure.datasource;

import com.fjkhy.abnormal.domain.business.vehicle.servicerange.VehicleServiceRangeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "KHY_DB_IT", matches = "1")
class KhyAbnormalJdbcIntegrationTest {
    @Autowired
    @Qualifier("khyJdbcTemplate")
    private ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    @Autowired
    private VehicleServiceRangeRepository repository;

    @Test
    void viewsAndTablesAreQueryable() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        assertNotNull(jdbcTemplate, "khyJdbcTemplate should be available when KHY_DB_URL is set");
        assertTrue(repository.configured(), "repository should be configured when datasource exists");

        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        System.out.println("SELECT 1 => " + one);

        System.out.println("v_khy_algo_vehicle_line_station => " +
                jdbcTemplate.queryForList("SELECT vehicle_id FROM v_khy_algo_vehicle_line_station LIMIT 1"));

        System.out.println("v_khy_algo_all_station_point => " +
                jdbcTemplate.queryForList("SELECT station_id FROM v_khy_algo_all_station_point LIMIT 1"));

        System.out.println("vehicle_gps_points => " +
                jdbcTemplate.queryForList("SELECT vehicle_id FROM vehicle_gps_points LIMIT 1"));

        System.out.println("v_khy_algo_vehicle_station_pass => " +
                jdbcTemplate.queryForList("SELECT vehicle_id FROM v_khy_algo_vehicle_station_pass LIMIT 1"));
    }
}
