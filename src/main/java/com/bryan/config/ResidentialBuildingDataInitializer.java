package com.bryan.config;

import com.bryan.entity.ResidentialBuilding;
import com.bryan.repository.ResidentialBuildingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResidentialBuildingDataInitializer implements ApplicationRunner {

    private final ResidentialBuildingRepository repository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, ResidentialBuilding> existingByCode = repository.findAll().stream()
                .collect(Collectors.toMap(ResidentialBuilding::getCode, Function.identity()));

        List<ResidentialBuilding> buildingsToSave = buildings().stream()
                .map(seed -> applySeed(existingByCode.get(seed.code()), seed))
                .toList();

        repository.saveAll(buildingsToSave);
        log.info("Seeded {} Vinhomes Grand Park residential buildings", buildingsToSave.size());
    }

    static List<BuildingSeed> buildings() {
        List<BuildingSeed> buildings = new ArrayList<>();

        addTowers(buildings, "The Rainbow", 1000,
                "S1.01 S1.02 S1.03 S1.05 S1.06 S2.01 S2.02 S2.03 S2.05 S2.06 "
                        + "S3.01 S3.02 S3.03 S3.05 S3.06 S5.01 S5.02");
        addTowers(buildings, "The Origami", 2000,
                "S6.01 S6.02 S6.03 S6.04 S6.05 S7.01 S7.02 S7.03 S7.04 S7.05 "
                        + "S8.01 S8.02 S8.03 S8.04 S8.05 S8.06 S9.01 S9.02 S9.03 S9.04 S9.05");
        addTowers(buildings, "The Beverly", 3000,
                "BE1 BE2 BE3 BE5 BE6 BE7 BE8 BE9 BE10 BE11");
        addTowers(buildings, "The Beverly Solari", 4000,
                "BS1 BS2 BS3 BS5 BS6 BS7 BS8 BS9 BS10 BS11 BS12 BS15 BS16");
        addTowers(buildings, "Glory Heights", 5000,
                "GH1 GH2 GH3 GH5 GH6");

        addNamedBuilding(buildings, "LUMIERE-A", "Toa A - Lumiere Boulevard", "Lumiere Boulevard", 6001);
        addNamedBuilding(buildings, "LUMIERE-B", "Toa B - Lumiere Boulevard", "Lumiere Boulevard", 6002);
        addNamedBuilding(buildings, "LUMIERE-C", "Toa C - Lumiere Boulevard", "Lumiere Boulevard", 6003);
        addNamedBuilding(buildings, "LUMIERE-D", "Toa D - Lumiere Boulevard", "Lumiere Boulevard", 6004);
        addNamedBuilding(buildings, "LUMIERE-E", "Toa E - Lumiere Boulevard", "Lumiere Boulevard", 6005);

        addNamedBuilding(buildings, "MANHATTAN", "The Manhattan", "Khu thap tang", 7001);
        addNamedBuilding(buildings, "MANHATTAN-GLORY", "The Manhattan Glory", "Khu thap tang", 7002);
        addNamedBuilding(buildings, "OPUS-ONE", "The Opus One", "Phan khu moi", 8001);

        return List.copyOf(buildings);
    }

    private static void addTowers(List<BuildingSeed> buildings, String subdivision, int startingOrder, String codes) {
        String[] towerCodes = codes.split(" ");
        for (int index = 0; index < towerCodes.length; index++) {
            String code = towerCodes[index];
            addNamedBuilding(buildings, code, "Toa " + code + " - " + subdivision, subdivision, startingOrder + index + 1);
        }
    }

    private static void addNamedBuilding(
            List<BuildingSeed> buildings,
            String code,
            String name,
            String subdivision,
            int displayOrder
    ) {
        buildings.add(new BuildingSeed(
                code,
                name,
                subdivision + ", Vinhomes Grand Park, TP Thu Duc",
                displayOrder
        ));
    }

    private static ResidentialBuilding applySeed(ResidentialBuilding existing, BuildingSeed seed) {
        ResidentialBuilding building = existing != null
                ? existing
                : new ResidentialBuilding(seed.code(), seed.name(), seed.description(), seed.displayOrder());

        building.setName(seed.name());
        building.setDescription(seed.description());
        building.setDisplayOrder(seed.displayOrder());
        return building;
    }

    record BuildingSeed(String code, String name, String description, int displayOrder) {
    }
}
