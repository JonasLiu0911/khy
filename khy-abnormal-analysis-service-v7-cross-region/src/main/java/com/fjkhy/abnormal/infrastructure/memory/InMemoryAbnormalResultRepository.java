package com.fjkhy.abnormal.infrastructure.memory;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.result.AbnormalResult;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryAbnormalResultRepository {
    private final List<AbnormalResult> store = new CopyOnWriteArrayList<>();

    public void saveAll(List<AbnormalResult> results) {
        store.addAll(results);
    }

    public List<AbnormalResult> list(ObjectType objectType, String countyName, String subtype) {
        return store.stream()
                .filter(r -> objectType == null || objectType == ObjectType.ALL || r.getObjectType() == objectType)
                .filter(r -> countyName == null || countyName.isBlank() || countyName.equals(r.getCountyName()))
                .filter(r -> subtype == null || subtype.isBlank() || r.getAbnormalSubtype().contains(subtype))
                .sorted(Comparator.comparing(AbnormalResult::getGeneratedTime).reversed())
                .toList();
    }

    public Optional<AbnormalResult> findById(String abnormalId) {
        return store.stream().filter(r -> r.getAbnormalId().equals(abnormalId)).findFirst();
    }

    public List<AbnormalResult> all() {
        return new ArrayList<>(store);
    }

    public void clear() {
        store.clear();
    }
}
