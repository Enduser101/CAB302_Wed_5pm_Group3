package com.ecotwin.service;

import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.ScoreBreakdown;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * A household's current values and scores, taken from its latest reading in each domain.
 * Used by the dashboard
 */
public class HouseholdScoreService {

    private final EnergyService energyService;
    private final WaterService waterService;
    private final WasteService wasteService;
    private final TransportService transportService;
    private final ScoreCalculator scoreCalculator;

    public HouseholdScoreService(EnergyService energyService, WaterService waterService,
                                 WasteService wasteService, TransportService transportService,
                                 ScoreCalculator scoreCalculator) {
        this.energyService = energyService;
        this.waterService = waterService;
        this.wasteService = wasteService;
        this.transportService = transportService;
        this.scoreCalculator = scoreCalculator;
    }

    /** @return empty until the household has at least one reading in every domain */
    public Optional<SustainabilityScenario> currentValues(Household household) {
        Optional<EnergyEntry> energy = latest(energyService.findEntriesForHousehold(household),
                EnergyEntry::getPeriod, EnergyEntry::getId);
        Optional<WaterEntry> water = latest(waterService.findEntriesForHousehold(household),
                WaterEntry::getPeriod, WaterEntry::getId);
        Optional<WasteEntry> waste = latest(wasteService.findEntriesForHousehold(household),
                WasteEntry::getPeriod, WasteEntry::getId);
        Optional<TransportEntry> transport = latest(transportService.findEntriesForHousehold(household),
                TransportEntry::getPeriod, TransportEntry::getId);

        if (energy.isEmpty() || water.isEmpty() || waste.isEmpty() || transport.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new SustainabilityScenario(
                energy.get().getElectricityKwh(),
                energy.get().getSolarGenerationKwh(),
                water.get().getLitres(),
                waste.get().getGeneralKg(),
                waste.get().getRecycledKg(),
                waste.get().getCompostKg(),
                transport.get().getPublicTransportTripsPerWeek(),
                transport.get().getFlightsPerYear()
        ));
    }

    public Optional<ScoreBreakdown> currentScores(Household household) {
        return currentValues(household)
                .map(values -> scoreCalculator.breakdown(values, household.getOccupants()));
    }

    /** Newest period wins; within the same period, the most recently added entry wins. */
    private static <T> Optional<T> latest(List<T> entries, Function<T, String> period, ToLongFunction<T> id) {
        return entries.stream().max(Comparator.comparing(period).thenComparingLong(id));
    }
}