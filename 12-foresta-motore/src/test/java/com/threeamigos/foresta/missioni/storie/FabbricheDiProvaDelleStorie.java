package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Per i test: costruisce le storie dalle loro fabbriche, che sono visibili solo al loro pacchetto.
 */
public final class FabbricheDiProvaDelleStorie {

    private FabbricheDiProvaDelleStorie() {
    }

    public static MissioneAPassi ilCampione() {
        return IlCampione.di(TipoPersonaggio.GUERRIERO, "Gervasio", TipoLocazione.BOSCO);
    }

    /**
     * Un favore di una benedizione che non sia di combattimento e senza un capo da nominare: comporre i nomi dei nemici e dei capi
     * disturba i test che li aspettano nell'ordine (ScenarioDuelliTest).
     */
    public static MissioneAPassi ilFavore() {
        for (int tentativo = 0; tentativo < 200; tentativo++) {
            com.threeamigos.foresta.missioni.richieste.BenedizioneRichiesta benedizione = com.threeamigos.foresta.missioni.richieste.BenedizioneRichiesta
                    .da(com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale.rigaDiMissioni("BENEDIZIONE"));
            com.threeamigos.foresta.missioni.richieste.FavoreRichiesto.Tipo tipo = benedizione.getFavore().getTipo();
            if (!benedizione.getFavore().isConCapo() && tipo != com.threeamigos.foresta.missioni.richieste.FavoreRichiesto.Tipo.COMBATTIMENTO) {
                return IlFavore.per(benedizione);
            }
        }
        throw new IllegalStateException("nessuna benedizione con un favore senza capo");
    }

    /** Il viandante già accettato, con i suoi parametri (il giorno, la richiesta, la quantità e la città). */
    public static MissioneAPassi ilViandante() {
        IlViandante viandante = new IlViandante();
        viandante.aggiungiProprieta("PARAMETRO_GIORNO", "3");
        viandante.aggiungiProprieta("PARAMETRO_RIGA", com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale.rigaDiMissioni("VIANDANTE"));
        viandante.aggiungiProprieta("PARAMETRO_QUANTITA", String.valueOf(viandante.getRichiesta().getQuantitaMinima()));
        viandante.aggiungiProprieta("PARAMETRO_CITTA", TipoLocazione.CITTA_FLEENA.name());
        viandante.attivaMissione();
        return viandante;
    }

    /** Il viaggio di Olaf già accettato: Olaf è con il gruppo, il leggendario non ancora scelto. */
    public static MissioneAPassi ilViaggioDiOlaf() {
        IlViaggioDiOlaf olaf = new IlViaggioDiOlaf();
        olaf.attivaMissione();
        return olaf;
    }
}
