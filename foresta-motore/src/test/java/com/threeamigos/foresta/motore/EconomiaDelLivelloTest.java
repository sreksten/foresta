package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.IncaricoDiCombattimento;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le monete seguono il livello del mondo (vedi economia.md): si comincia con più monete, un prezioso vale quanto il
 * livello, e un incarico paga la sua paga base per il livello di quando si è offerto.
 */
class EconomiaDelLivelloTest {

    private static final String LADRI = "CHIAVE=LADRI_DI_PROVA;TIPO=RECUPERO;ASPETTO=QUALUNQUE;MANDANTE=il derubato;NEMICO=LADRO;"
            + "NUMERO=4;LUOGO=GROTTA;MONETE=20;TITOLO=Recupera il medaglione;RICHIESTA=Mi hanno rubato il medaglione.;"
            + "BATTUTA=Lo riavrai.;RISPOSTA=Grazie.;VITTORIA=I ladri sono a terra.;RINGRAZIAMENTO=Il mio medaglione!;"
            + "RICORDO=In questa grotta i ladri nascondevano il medaglione.";

    private static void impostaLivello(int livello) {
        ModelloDati.getIstanza().getStatisticheMD().setLivello(livello);
    }

    @Test
    void siComincianoConPiuMonete() {
        // Senza la modalità di prova, che nelle altre partite di test dà 9999 monete
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(33)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            assertEquals(150, CostantiPersonaggi.MONETE_INIZIALI);
            assertEquals(CostantiPersonaggi.MONETE_INIZIALI, partita.gruppo().getMonete());
            assertEquals(CostantiPersonaggi.PREZIOSI_INIZIALI, partita.gruppo().getPreziosi());
        }
    }

    @Test
    void unPreziosoValeQuantoIlLivelloDelMondo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(33)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            GruppoGiocatore gruppo = partita.gruppo();
            // Nel gruppo c'è solo un guerriero: nessun ladro che venda meglio
            impostaLivello(1);
            gruppo.subPreziosi(gruppo.getPreziosi());
            gruppo.addPreziosi(3);
            int monete = gruppo.getMonete();
            CommercioDelGruppo.vendePreziosi(gruppo);
            assertEquals(monete + 3, gruppo.getMonete(), "al primo livello vale una moneta");
            assertEquals(0, gruppo.getPreziosi());

            impostaLivello(6);
            gruppo.addPreziosi(3);
            monete = gruppo.getMonete();
            CommercioDelGruppo.vendePreziosi(gruppo);
            assertEquals(monete + 18, gruppo.getMonete(), "al sesto livello ne vale sei");
        }
    }

    @Test
    void unIncaricoPagaPerIlLivelloDiQuandoSiOffreEAnchePoiLaCifraNonCambia() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
                impostaLivello(3);
                partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA);
            });
            MissioneAPassi medaglione = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(IncaricoDiCombattimento.class::isInstance).map(MissioneAPassi.class::cast).findFirst()
                    .orElseThrow(AssertionError::new);
            medaglione.aggiungiProprieta("PARAMETRO_" + IncaricoDiCombattimento.INCARICO, LADRI);
            medaglione.controllaPreLocazione();
            medaglione.segnaIntermezzoPassoMostrato("INCARICO");
            medaglione.controllaInLocazione();
            assertTrue(medaglione.isAttiva());

            // Nel frattempo il livello sale, ma la cifra promessa è quella di quando l'incarico si è offerto
            impostaLivello(8);
            medaglione.aggiungiProprieta("PASSO_CORRENTE", "RITORNO");
            int monete = partita.gruppo().getMonete();
            medaglione.controllaPreLocazione();
            medaglione.segnaIntermezzoPassoMostrato("RITORNO");
            medaglione.controllaInLocazione();
            assertTrue(medaglione.isCompleta());
            assertEquals(monete + 20 * 3, partita.gruppo().getMonete());
        }
    }
}
