package com.ecotwin.service;

import com.ecotwin.model.Recommendation.Domain;
import com.ecotwin.model.SustainabilityScenario;

import java.util.List;

/** US-24: the rules, each judged against the benchmarks the scorers use. */
final class RecommendationRules {

    private RecommendationRules() {
    }

    static List<RecommendationRule> all() {
        return List.of(new AddSolar(), new CutElectricity(), new CutWater(),
                new CompostScraps(), new RecycleMore(), new FlyLess(), new UsePublicTransport());
    }

    private static double solar(SustainabilityScenario v) {
        return v.getSolarGenerationKwh() == null ? 0 : v.getSolarGenerationKwh();
    }

    private static String whole(double value) {
        return String.format("%,d", Math.round(value));
    }

    static final class AddSolar implements RecommendationRule {
        public Domain domain() { return Domain.ENERGY; }
        public boolean applies(SustainabilityScenario v, int people) { return solar(v) == 0 && v.getEnergyKwh() > 0; }
        public void apply(SustainabilityScenario c, int people) { c.setSolarGenerationKwh(c.getEnergyKwh() * 0.5); }
        public String title() { return "Add rooftop solar"; }
        public String detail(SustainabilityScenario v, int people) {
            return "You generate no solar. A system covering half your " + whole(v.getEnergyKwh())
                    + " kWh a month would offset a large share of your electricity.";
        }
    }

    static final class CutElectricity implements RecommendationRule {
        public Domain domain() { return Domain.ENERGY; }
        public boolean applies(SustainabilityScenario v, int people) {
            return (v.getEnergyKwh() - solar(v)) / people > EnergyScorer.KWH_BENCHMARK;
        }
        public void apply(SustainabilityScenario c, int people) { c.setEnergyKwh(c.getEnergyKwh() * 0.85); }
        public String title() { return "Cut electricity use by 15%"; }
        public String detail(SustainabilityScenario v, int people) {
            return "You use " + whole((v.getEnergyKwh() - solar(v)) / people) + " kWh per person a month against a "
                    + whole(EnergyScorer.KWH_BENCHMARK) + " kWh benchmark.";
        }
    }

    static final class CutWater implements RecommendationRule {
        public Domain domain() { return Domain.WATER; }
        public boolean applies(SustainabilityScenario v, int people) {
            return v.getWaterLitres() / people > WaterScorer.LITRES_BENCHMARK;
        }
        public void apply(SustainabilityScenario c, int people) { c.setWaterLitres(c.getWaterLitres() * 0.8); }
        public String title() { return "Install low-flow shower heads"; }
        public String detail(SustainabilityScenario v, int people) {
            return "You use " + whole(v.getWaterLitres()) + " L a month, " + whole(v.getWaterLitres() / people)
                    + " L per person against a " + whole(WaterScorer.LITRES_BENCHMARK) + " L benchmark.";
        }
    }

    static final class CompostScraps implements RecommendationRule {
        public Domain domain() { return Domain.WASTE; }
        public boolean applies(SustainabilityScenario v, int people) {
            return v.getGeneralWasteKg() / people > WasteScorer.LANDFILL_KG_BENCHMARK / 2;
        }
        public void apply(SustainabilityScenario c, int people) {
            double moved = c.getGeneralWasteKg() * 0.3;
            c.setGeneralWasteKg(c.getGeneralWasteKg() - moved);
            c.setCompostKg(c.getCompostKg() + moved);
        }
        public String title() { return "Compost food scraps"; }
        public String detail(SustainabilityScenario v, int people) {
            return whole(v.getGeneralWasteKg()) + " kg a month goes to landfill. Food scraps are usually about a third of that.";
        }
    }

    static final class RecycleMore implements RecommendationRule {
        public Domain domain() { return Domain.WASTE; }
        public boolean applies(SustainabilityScenario v, int people) {
            double total = v.getGeneralWasteKg() + v.getRecycledWasteKg() + v.getCompostKg();
            return total > 0 && v.getRecycledWasteKg() / total < 0.4;
        }
        public void apply(SustainabilityScenario c, int people) {
            double moved = c.getGeneralWasteKg() * 0.2;
            c.setGeneralWasteKg(c.getGeneralWasteKg() - moved);
            c.setRecycledWasteKg(c.getRecycledWasteKg() + moved);
        }
        public String title() { return "Recycle more of your general waste"; }
        public String detail(SustainabilityScenario v, int people) {
            double total = v.getGeneralWasteKg() + v.getRecycledWasteKg() + v.getCompostKg();
            return "Only " + whole(100 * v.getRecycledWasteKg() / total) + "% of your waste is recycled.";
        }
    }

    static final class FlyLess implements RecommendationRule {
        public Domain domain() { return Domain.TRANSPORT; }
        public boolean applies(SustainabilityScenario v, int people) {
            return v.getFlightsPerYear() / people > TransportScorer.FLIGHTS_PER_YEAR_BENCHMARK;
        }
        public void apply(SustainabilityScenario c, int people) { c.setFlightsPerYear(c.getFlightsPerYear() - people); }
        public String title() { return "Take one fewer flight each per year"; }
        public String detail(SustainabilityScenario v, int people) {
            return "Your household takes " + whole(v.getFlightsPerYear()) + " flights a year, "
                    + whole(v.getFlightsPerYear() / people) + " per person against a benchmark of "
                    + whole(TransportScorer.FLIGHTS_PER_YEAR_BENCHMARK) + ".";
        }
    }

    static final class UsePublicTransport implements RecommendationRule {
        public Domain domain() { return Domain.TRANSPORT; }
        public boolean applies(SustainabilityScenario v, int people) {
            return v.getPublicTransportTripsPerWeek() / people < TransportScorer.PUBLIC_TRANSPORT_TRIPS_PER_WEEK_TARGET;
        }
        public void apply(SustainabilityScenario c, int people) {
            c.setPublicTransportTripsPerWeek(c.getPublicTransportTripsPerWeek() + 2.0 * people);
        }
        public String title() { return "Replace one weekly commute with public transport"; }
        public String detail(SustainabilityScenario v, int people) {
            return "Your household makes " + whole(v.getPublicTransportTripsPerWeek())
                    + " public transport trips a week. One return commute each adds " + (2 * people) + ".";
        }
    }
}