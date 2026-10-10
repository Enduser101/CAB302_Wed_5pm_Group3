package com.ecotwin.model;

/**
 * Holds temporary household values for a sustainability scenario.
 * These values are kept separate from the household's real resource records.
 */
public class SustainabilityScenario {

    private double energyKwh;
    private Double solarGenerationKwh;
    private double waterLitres;

    private double generalWasteKg;
    private double recycledWasteKg;
    private double compostKg;

    private double publicTransportTripsPerWeek;
    private double flightsPerYear;

    public SustainabilityScenario(double energyKwh,
                                  Double solarGenerationKwh,
                                  double waterLitres,
                                  double generalWasteKg,
                                  double recycledWasteKg,
                                  double compostKg,
                                  double publicTransportTripsPerWeek,
                                  double flightsPerYear) {
        this.energyKwh = energyKwh;
        this.solarGenerationKwh = solarGenerationKwh;
        this.waterLitres = waterLitres;
        this.generalWasteKg = generalWasteKg;
        this.recycledWasteKg = recycledWasteKg;
        this.compostKg = compostKg;
        this.publicTransportTripsPerWeek = publicTransportTripsPerWeek;
        this.flightsPerYear = flightsPerYear;
    }

    /** An independent copy: changing it leaves this scenario's values as they are. */
    public SustainabilityScenario copy() {
        return new SustainabilityScenario(
                energyKwh,
                solarGenerationKwh,
                waterLitres,
                generalWasteKg,
                recycledWasteKg,
                compostKg,
                publicTransportTripsPerWeek,
                flightsPerYear
        );
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(double energyKwh) {
        this.energyKwh = energyKwh;
    }

    public Double getSolarGenerationKwh() {
        return solarGenerationKwh;
    }

    public void setSolarGenerationKwh(Double solarGenerationKwh) {
        this.solarGenerationKwh = solarGenerationKwh;
    }

    public double getWaterLitres() {
        return waterLitres;
    }

    public void setWaterLitres(double waterLitres) {
        this.waterLitres = waterLitres;
    }

    public double getGeneralWasteKg() {
        return generalWasteKg;
    }

    public void setGeneralWasteKg(double generalWasteKg) {
        this.generalWasteKg = generalWasteKg;
    }

    public double getRecycledWasteKg() {
        return recycledWasteKg;
    }

    public void setRecycledWasteKg(double recycledWasteKg) {
        this.recycledWasteKg = recycledWasteKg;
    }

    public double getCompostKg() {
        return compostKg;
    }

    public void setCompostKg(double compostKg) {
        this.compostKg = compostKg;
    }

    public double getPublicTransportTripsPerWeek() {
        return publicTransportTripsPerWeek;
    }

    public void setPublicTransportTripsPerWeek(double publicTransportTripsPerWeek) {
        this.publicTransportTripsPerWeek = publicTransportTripsPerWeek;
    }

    public double getFlightsPerYear() {
        return flightsPerYear;
    }

    public void setFlightsPerYear(double flightsPerYear) {
        this.flightsPerYear = flightsPerYear;
    }
}
