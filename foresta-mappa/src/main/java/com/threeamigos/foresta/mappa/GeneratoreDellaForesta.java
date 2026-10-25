package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale;

import java.util.List;

/**
 * Costruisce una Foresta nuova, con i nomi: le città (una per quadrante, in ordine casuale), le locande, i templi, le grotte, le paludi, le
 * rovine, le radure, e il bosco per tutto il resto. A chi la abita (residenti, magazzini, personaggi, artefatti) pensa il
 * {@link PopolatoreDellaForesta}. I castelli non si costruiscono qui: li rivendica ciascuno la propria missione.
 */
public final class GeneratoreDellaForesta {

    private GeneratoreDellaForesta() {
    }

    /**
     * Svuota la mappa e la ricostruisce delle dimensioni indicate.
     *
     * @param personaggiDisponibili quanti personaggi aspettano di essere messi in una locanda: le locande sono almeno altrettante
     */
    public static void genera(int dimensioneX, int dimensioneY, int personaggiDisponibili, PopolatoreDellaForesta popolatore) {
        Foresta.azzera(dimensioneX, dimensioneY);

        int numeroCitta = 0;
        for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
            if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
                numeroCitta++;
            }
        }
        int numeroLocandeMax = Math.max(personaggiDisponibili, (dimensioneX + dimensioneY) >> 2);
        List<ProduttoreDiLocandeCasuale.DatiLocanda> poolDatiLocanda = ProduttoreDiLocandeCasuale.getDatiLocanda(numeroCitta + numeroLocandeMax);

        costruisciLeCitta(poolDatiLocanda, popolatore);
        // Dopo le persone delle città: nessuno ha il nome di un altro
        popolatore.dopoLeCitta();
        costruisciLeLocande(poolDatiLocanda, popolatore);
        costruisciITempli(popolatore);

        int media = (dimensioneX + dimensioneY) / 2;
        costruisci(TipoLocazione.GROTTA, media >> 1);
        costruisci(TipoLocazione.PALUDE, media);
        costruisci(TipoLocazione.ROVINE, media);
        // Poche radure, quante le grotte: ci crescono alcuni ingredienti dell'alchimista
        costruisci(TipoLocazione.RADURA, media >> 1);

        // Il resto della Foresta è bosco. Va posato prima di sistemare il gruppo, che appena arriva si guarda intorno e ha bisogno di
        // caselle su cui farlo.
        for (int x = 0; x < dimensioneX; x++) {
            for (int y = 0; y < dimensioneY; y++) {
                CoordinateMD coordinate = new CoordinateMD(x, y);
                if (Foresta.getLocazione(coordinate) == null) {
                    Foresta.setLocazione(coordinate, TipoLocazione.BOSCO);
                    Foresta.impostaVarianteDelBoscoACaso(Foresta.getLocazioneMD(coordinate));
                }
            }
        }
    }

    /**
     * Le città, una per quadrante (in ordine casuale).
     */
    private static void costruisciLeCitta(List<ProduttoreDiLocandeCasuale.DatiLocanda> poolDatiLocanda, PopolatoreDellaForesta popolatore) {
        List<Quadrante> quadranti = Quadrante.inOrdineCasuale();
        for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
            if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
                CoordinateMD coordinate = Foresta.costruisciLocazioneUnica(tipoLocazione, getCoordinateLibere(quadranti.remove(0)), false);
                popolatore.impostaDatiLocanda(Foresta.getLocazioneMD(coordinate), poolDatiLocanda.remove(0));
                popolatore.cittaCostruita(tipoLocazione, coordinate);
            }
        }
    }

    /**
     * Le locande, una per ogni personaggio da piazzare e poi tante da arrivare a un quarto delle caselle per lato.
     */
    private static void costruisciLeLocande(List<ProduttoreDiLocandeCasuale.DatiLocanda> poolDatiLocanda, PopolatoreDellaForesta popolatore) {
        int locandeCostruite = 0;
        while (popolatore.prelevaPersonaggio()) {
            CoordinateMD coordinate = costruisciLaLocanda(poolDatiLocanda, popolatore);
            popolatore.locandaCostruita(coordinate);
            locandeCostruite++;
        }
        int media = (Foresta.getDimensioneX() + Foresta.getDimensioneY()) >> 2;
        for (int i = locandeCostruite; i < media; i++) {
            costruisciLaLocanda(poolDatiLocanda, popolatore);
        }
    }

    private static CoordinateMD costruisciLaLocanda(List<ProduttoreDiLocandeCasuale.DatiLocanda> poolDatiLocanda, PopolatoreDellaForesta popolatore) {
        ProduttoreDiLocandeCasuale.DatiLocanda datiLocanda = poolDatiLocanda.remove(0);
        CoordinateMD coordinate = Foresta.getCoordinateLibere();
        Foresta.setLocazione(coordinate, TipoLocazione.LOCANDA);
        popolatore.impostaDatiLocanda(Foresta.getLocazioneMD(coordinate), datiLocanda);
        return coordinate;
    }

    /**
     * I templi, uno per ogni artefatto da piazzare: solo la metà dei templi ne ha uno, gli altri restano liberi.
     */
    private static void costruisciITempli(PopolatoreDellaForesta popolatore) {
        int templiConArtefatto = 0;
        while (popolatore.prelevaArtefatto()) {
            CoordinateMD coordinate = Foresta.getCoordinateLibere();
            Foresta.setLocazione(coordinate, TipoLocazione.TEMPIO);
            popolatore.tempioCostruito(coordinate);
            templiConArtefatto++;
        }
        int media = (Foresta.getDimensioneX() + Foresta.getDimensioneY()) >> 2;
        int templiSenzaArtefatto = Math.max(templiConArtefatto, media - templiConArtefatto);
        costruisci(TipoLocazione.TEMPIO, templiSenzaArtefatto);
    }

    private static void costruisci(TipoLocazione tipoLocazione, int quantita) {
        Logger.log(() -> "Costruzione di " + quantita + " classiLocazione " + tipoLocazione);
        for (int i = 0; i < quantita; i++) {
            Foresta.setLocazione(Foresta.getCoordinateLibere(), tipoLocazione);
        }
    }

    /**
     * Come {@link Foresta#getCoordinateLibere()}, ma dentro il quadrante.
     */
    public static CoordinateMD getCoordinateLibere(Quadrante quadrante) {
        CoordinateMD coordinate;
        do {
            coordinate = quadrante.getCoordinateACaso();
        } while (!Foresta.isLibera(Foresta.getLocazione(coordinate)));
        return coordinate;
    }
}
