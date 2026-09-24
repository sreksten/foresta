package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVenditaArtefatto;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoNegozio;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NegoziTest {

    private static final CoordinateMD CITTA = new CoordinateMD(5, 6);

    private GruppoGiocatore gruppo;

    @BeforeEach
    void nuovoMondo() {
        ModelloDati.setIstanza(new ModelloDati());
        gruppo = new GruppoGiocatore();
        gruppo.subMonete(gruppo.getMonete());
    }

    @Test
    void ilVenditoreTrattaSoloPergameneLArmaioloTuttoIlResto() {
        assertTrue(TipoNegozio.VENDITORE_DI_PERGAMENE.tratta(TipoArtefatto.INCANTAMENTO));
        assertFalse(TipoNegozio.VENDITORE_DI_PERGAMENE.tratta(TipoArtefatto.SPADA));
        assertFalse(TipoNegozio.ARMAIOLO.tratta(TipoArtefatto.INCANTAMENTO));
        assertTrue(TipoNegozio.ARMAIOLO.tratta(TipoArtefatto.SPADA));
        assertTrue(TipoNegozio.ARMAIOLO.tratta(TipoArtefatto.ANELLO));
    }

    @Test
    void lArmaioloNonCompraPergamene() {
        Artefatto pergamena = artefatto(TipoArtefatto.INCANTAMENTO, 30);
        gruppo.addArtefatto(pergamena);
        ScambiatoreArtefatti armaiolo = RegistroArtefatti.getScambiatorePerNegozio(CITTA, TipoNegozio.ARMAIOLO);

        assertFalse(gruppo.vende(new ComandoVenditaArtefatto(gruppo, armaiolo, pergamena)));

        assertEquals(0, gruppo.getMonete());
        assertTrue(nelGruppo(pergamena));
        assertTrue(armaiolo.getInventario().isEmpty());
    }

    @Test
    void ilVenditoreDiPergameneCompraSoloPergamene() {
        Artefatto spada = artefatto(TipoArtefatto.SPADA, 20);
        Artefatto pergamena = artefatto(TipoArtefatto.INCANTAMENTO, 30);
        gruppo.addArtefatto(spada);
        gruppo.addArtefatto(pergamena);
        ScambiatoreArtefatti venditore = RegistroArtefatti.getScambiatorePerNegozio(CITTA, TipoNegozio.VENDITORE_DI_PERGAMENE);

        assertFalse(gruppo.vende(new ComandoVenditaArtefatto(gruppo, venditore, spada)));
        assertTrue(gruppo.vende(new ComandoVenditaArtefatto(gruppo, venditore, pergamena)));

        assertEquals(30, gruppo.getMonete());
        assertTrue(nelGruppo(spada));
        assertFalse(nelGruppo(pergamena));
        assertEquals(1, venditore.getInventario().size());
    }

    @Test
    void iMagazziniDellaCittaSiRiempionoOgnunoConLaSuaMerce() {
        RegistroArtefatti.riempiMagazzini(CITTA, GeneratoreArtefatti.istanza());

        Collection<Artefatto> armaiolo = RegistroArtefatti.getScambiatorePerNegozio(CITTA, TipoNegozio.ARMAIOLO).getInventario();
        Collection<Artefatto> venditore = RegistroArtefatti.getScambiatorePerNegozio(CITTA, TipoNegozio.VENDITORE_DI_PERGAMENE).getInventario();
        assertEquals(Costanti.MAGAZZINO_ARTEFATTI_ARMAIOLO, armaiolo.size());
        assertEquals(Costanti.MAGAZZINO_PERGAMENE, venditore.size());
        assertTrue(armaiolo.stream().allMatch(a -> TipoNegozio.ARMAIOLO.tratta(a.getTipo())));
        assertTrue(venditore.stream().allMatch(a -> a.getTipo() == TipoArtefatto.INCANTAMENTO));
        // Livelli a rotazione da 1 al massimo
        Set<Integer> livelli = venditore.stream().map(a -> a.getModelloDati().getLivello()).collect(Collectors.toSet());
        assertEquals(Costanti.MAGAZZINO_LIVELLO_MASSIMO, livelli.size());
        // Un'altra città ha i magazzini vuoti
        assertTrue(RegistroArtefatti.getScambiatorePerNegozio(new CoordinateMD(1, 1), TipoNegozio.ARMAIOLO).getInventario().isEmpty());
    }

    private boolean nelGruppo(Artefatto artefatto) {
        return gruppo.getInventario().stream().anyMatch(a -> a.getModelloDati() == artefatto.getModelloDati());
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int costo) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(1);
        md.setPeso(1);
        md.setCostoAcquisto(costo);
        return Artefatto.di(md);
    }
}
