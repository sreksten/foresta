package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.GestoreProgressione;
import com.threeamigos.foresta.motore.RegistroPersonaggi;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La dotazione di base delle classi giocanti e i compagni reclutati, allineati al livello del mondo.
 */
class EquipaggiamentoInizialeTest {

    @BeforeEach
    void mondoNuovo() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    @Test
    void ogniClasseGiocanteRiceveLaSuaDotazione() {
        for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRA,
                TipoPersonaggio.ELFO, TipoPersonaggio.CANTASTORIE, TipoPersonaggio.MAGA}) {
            Personaggio personaggio = FabbricaPersonaggi.crea(classe, 1);
            EquipaggiamentoIniziale.equipaggia(personaggio);
            Collection<TipoArtefatto> tipi = personaggio.getModelloDati().getArtefatti().stream().map(ArtefattoMD::getTipo)
                    .collect(Collectors.toList());
            assertEquals(EquipaggiamentoIniziale.perClasse(classe).size(), tipi.size(), classe + ": " + tipi);
            assertTrue(tipi.containsAll(EquipaggiamentoIniziale.perClasse(classe)), classe + ": " + tipi);
        }
    }

    @Test
    void ilCompagnoArrivaVicinoAlLivelloDelMondoConPezziChePuoUsare() {
        ModelloDati.getIstanza().getStatisticheMD().setLivello(5);
        for (int i = 0; i < 50; i++) {
            Personaggio compagno = RegistroPersonaggi.preparaCompagno(new Guerriero("Reginald", 1));
            assertTrue(compagno.getLivello() >= 3 && compagno.getLivello() <= 5, "livello " + compagno.getLivello());
            assertEquals("Reginald", compagno.getNomeProprio().orElse(null));
            assertFalse(compagno.isPNG());
            assertEquals(GestoreProgressione.getXpNecessariPerLivello(compagno.getLivello()), compagno.getEsperienza());
            assertEquals(compagno.getLivello() - 1, compagno.getPuntiAbilitaDisponibili());
            assertEquals(2, compagno.getModelloDati().getArtefatti().size());
            for (ArtefattoMD artefatto : compagno.getModelloDati().getArtefatti()) {
                assertTrue(artefatto.getLivello() >= 3 && artefatto.getLivello() <= compagno.getLivello(),
                        "artefatto di livello " + artefatto.getLivello() + " a un compagno di livello " + compagno.getLivello());
            }
        }
    }

    @Test
    void ilCompagnoReclutatoContinuaAGuadagnareEsperienza() {
        ModelloDati.getIstanza().getStatisticheMD().setLivello(5);
        Personaggio compagno = RegistroPersonaggi.preparaCompagno(new Guerriero("Reginald", 1));
        int prima = compagno.getEsperienza();
        compagno.addPuntiEsperienza(10);
        assertEquals(prima + 10, compagno.getEsperienza());
    }

    @Test
    void impostaLivelloDopoReclutamentoNonAbbassaIlLivello() {
        Personaggio personaggio = new Guerriero("Reginald", 1);
        personaggio.impostaLivelloDopoReclutamento(4);
        assertEquals(4, personaggio.getLivello());
        assertEquals(900, personaggio.getEsperienza());
        assertEquals(3, personaggio.getPuntiAbilitaDisponibili());
        personaggio.impostaLivelloDopoReclutamento(2);
        assertEquals(4, personaggio.getLivello());
        assertEquals(3, personaggio.getPuntiAbilitaDisponibili());
    }
}
