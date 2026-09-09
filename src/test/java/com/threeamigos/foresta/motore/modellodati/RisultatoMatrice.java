package com.threeamigos.foresta.motore.modellodati;

public class RisultatoMatrice {

    public final double winRatePg;
    public final double loseRatePg;
    public final double stalloRate;
    public final double mediaTurni;
    public final double mediaStanchezzaFinale;
    public final double tassoColpirePg;
    public final double tassoColpireMostro;
    public final double dannoMedioPg;
    public final double dannoMedioMostro;

    public RisultatoMatrice(double winRatePg, double loseRatePg, double stalloRate,
                             double mediaTurni, double mediaStanchezzaFinale,
                             double tassoColpirePg, double tassoColpireMostro,
                             double dannoMedioPg, double dannoMedioMostro) {
        this.winRatePg = winRatePg;
        this.loseRatePg = loseRatePg;
        this.stalloRate = stalloRate;
        this.mediaTurni = mediaTurni;
        this.mediaStanchezzaFinale = mediaStanchezzaFinale;
        this.tassoColpirePg = tassoColpirePg;
        this.tassoColpireMostro = tassoColpireMostro;
        this.dannoMedioPg = dannoMedioPg;
        this.dannoMedioMostro = dannoMedioMostro;
    }
}
