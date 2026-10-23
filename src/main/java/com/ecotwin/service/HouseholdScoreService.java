package com.ecotwin.service;

import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.Recommendation.Domain;
import com.ecotwin.model.ScoreBreakdown;
import com.ecotwin.model.ScoreChange;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import java.util.AbstractMap;
import com.ecotwin.model.ScorePoint;
import java.util.Map;
import java.util.TreeSet;


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

        return Optional.of(scenarioFrom(energy.get(), water.get(), waste.get(), transport.get()));
    }

    public Optional<ScoreBreakdown> currentScores(Household household) {
        return currentValues(household)
                .map(values -> scoreCalculator.breakdown(values, household.getOccupants()));
    }

    /** US-23: run an update and report the household's scores before and after it. */
    public Optional<ScoreChange> trackChange(Household household, Domain domain, Runnable update) {
        Optional<ScoreBreakdown> before = currentScores(household);
        update.run();
        return currentScores(household)
                .map(after -> new ScoreChange(before.orElse(null), after, domain));
    }

    /** Newest period wins; within the same period, the most recently added entry wins. */
    private static <T> Optional<T> latest(List<T> entries, Function<T, String> period, ToLongFunction<T> id) {
        return entries.stream().max(Comparator.comparing(period).thenComparingLong(id));
    }

    /** US-23: one score per period with readings in every domain, oldest first. */
    public List<ScorePoint> scoreHistory(Household household) {
        List<EnergyEntry> energy = energyService.findEntriesForHousehold(household);
        List<WaterEntry> water = waterService.findEntriesForHousehold(household);
        List<WasteEntry> waste = wasteService.findEntriesForHousehold(household);
        List<TransportEntry> transport = transportService.findEntriesForHousehold(household);

        return allPeriods(energy, water, waste, transport).stream()
                .map(period -> scenarioFor(period, energy, water, waste, transport))
                .flatMap(Optional::stream)
                .map(e -> new ScorePoint(e.getKey(),
                        scoreCalculator.breakdown(e.getValue(), household.getOccupants())))
                .toList();
    }

    /** Every period that appears in any domain, oldest first. */
    private static List<String> allPeriods(List<EnergyEntry> energy, List<WaterEntry> water,
                                           List<WasteEntry> waste, List<TransportEntry> transport) {
        TreeSet<String> periods = new TreeSet<>();
        energy.forEach(e -> periods.add(e.getPeriod()));
        water.forEach(e -> periods.add(e.getPeriod()));
        waste.forEach(e -> periods.add(e.getPeriod()));
        transport.forEach(e -> periods.add(e.getPeriod()));
        return List.copyOf(periods);
    }

    /** A scenario for one period, or empty if any domain has no reading that period. */
    private static Optional<Map.Entry<String, SustainabilityScenario>> scenarioFor(
            String period, List<EnergyEntry> energy, List<WaterEntry> water,
            List<WasteEntry> waste, List<TransportEntry> transport) {

        Optional<EnergyEntry> e = inPeriod(energy, EnergyEntry::getPeriod, EnergyEntry::getId, period);
        Optional<WaterEntry> w = inPeriod(water, WaterEntry::getPeriod, WaterEntry::getId, period);
        Optional<WasteEntry> s = inPeriod(waste, WasteEntry::getPeriod, WasteEntry::getId, period);
        Optional<TransportEntry> t = inPeriod(transport, TransportEntry::getPeriod, TransportEntry::getId, period);

        if (e.isEmpty() || w.isEmpty() || s.isEmpty() || t.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new AbstractMap.SimpleEntry<>(period,
                scenarioFrom(e.get(), w.get(), s.get(), t.get())));
    }

    /** The last entry recorded for a period. */
    private static <T> Optional<T> inPeriod(List<T> entries, Function<T, String> period,
                                            ToLongFunction<T> id, String wanted) {
        return entries.stream()
                .filter(entry -> wanted.equals(period.apply(entry)))
                .max(Comparator.comparingLong(id));
    }

    /** Builds scenario values from one reading in each domain. */
    private static SustainabilityScenario scenarioFrom(EnergyEntry energy, WaterEntry water,
                                                       WasteEntry waste, TransportEntry transport) {
        return new SustainabilityScenario(
                energy.getElectricityKwh(),
                energy.getSolarGenerationKwh(),
                water.getLitres(),
                waste.getGeneralKg(),
                waste.getRecycledKg(),
                waste.getCompostKg(),
                transport.getPublicTransportTripsPerWeek(),
                transport.getFlightsPerYear());
    }
}