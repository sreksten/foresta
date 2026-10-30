package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.AccessoAiPassi;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.MondoDiProva;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le chiavi dei custodi (le missioni figlie di MissioneATappe), costruite a mano: il custode che combatte e quello che vuole un oggetto,
 * la descrizione che cambia secondo il passo e la consegna nel posto del custode.
 */
class ChiaveDelCustodeTest {

    private final CoordinateMD posto = new CoordinateMD(6, 6);
    private final CoordinateMD postoDellOggetto = new CoordinateMD(9, 9);

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(111);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private ChiaveDelCustode conOggetto() {
        ChiaveDelCustode chiave = ChiaveDelCustode.cheVuoleUnOggetto(posto, "le rovine del custode", null, "la chiave d'osso", "Il custode ha la chiave.",
                "il medaglione", "Il medaglione sta altrove.", postoDellOggetto, "la grotta del medaglione", TipoPersonaggio.GOBLIN, 2);
        chiave.getModelloDati().setId("CHIAVE_CON_OGGETTO");
        RegistroMissioni.aggiungiMissioneSecondaria(chiave);
        return chiave;
    }

    @Test
    void laDescrizioneDelCustodeInScambioCambiaSecondoIlPasso() {
        ChiaveDelCustode chiave = conOggetto();
        chiave.aggiungiProprieta("PASSO_CORRENTE", "CUSTODE");
        String alCustode = chiave.getDescrizione();
        chiave.aggiungiProprieta("PASSO_CORRENTE", "CHIEDE");
        assertEquals(alCustode, chiave.getDescrizione());
        chiave.aggiungiProprieta("PASSO_CORRENTE", "CONSEGNA");
        assertTrue(chiave.getDescrizione().contains("Il medaglione sta altrove."), chiave.getDescrizione());
        ChiaveDelCustode combatte = ChiaveDelCustode.chePerCombattere(posto, "le rovine", TipoPersonaggio.LADRO, 3, "Gruk", "la chiave di ferro", "Il custode.");
        combatte.getModelloDati().setId("CHIAVE_CHE_COMBATTE");
        assertFalse(combatte.getDescrizione().contains("medaglione"));
    }

    @Test
    void laConsegnaFaSparireLeCoseACasualeNelPostoDelCustode() {
        ChiaveDelCustode chiave = conOggetto();
        chiave.aggiungiProprieta("PASSO_CORRENTE", "CONSEGNA");
        MissioneAPassi comePassi = chiave;
        Foresta.setPosizioneDelGruppo(new CoordinateMD(1, 1));
        AccessoAiPassi.eseguiAOgniControllo(comePassi, "CONSEGNA");
        assertFalse(RegistroMissioni.isDaSopprimere(posto));
        Foresta.setPosizioneDelGruppo(posto);
        AccessoAiPassi.eseguiAOgniControllo(comePassi, "CONSEGNA");
        assertTrue(RegistroMissioni.isDaSopprimere(posto), "nel posto del custode non salta fuori niente a caso");
    }
}
