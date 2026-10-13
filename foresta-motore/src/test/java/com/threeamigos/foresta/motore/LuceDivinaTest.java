package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.LuceDivina;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La Luce Divina: l'incantesimo sacro, innato per Sacerdote e Sacerdotessa, che brucia i non morti e non risparmia lo
 * Scheletro che combatte con il gruppo.
 */
class LuceDivinaTest {

    private static final String FRASE_SULLO_SCHELETRO = "La luce divina investe anche lo Scheletro.";

    @Test
    void ECaratterizzataComeIlFulmineMaSacraEAPiuBersagli() {
        Incantesimo luce = FabbricaIncantesimi.crea(ClasseIncantesimo.LUCE_DIVINA, 5);
        assertEquals(PortataIncantesimo.MULTIPLO, ClasseIncantesimo.LUCE_DIVINA.getPortata());
        assertEquals(TipoDanno.SACRO, ClasseIncantesimo.LUCE_DIVINA.getTipoDanno());
        assertEquals(Comando.LUCE_DIVINA, ClasseIncantesimo.LUCE_DIVINA.getComandoDiAttivazione());
        assertEquals(Costanti.INCANTESIMO_FULMINE_DANNI, Costanti.INCANTESIMO_LUCE_DIVINA_DANNI);
        assertEquals(Costanti.INCANTESIMO_FULMINE_COSTO_LANCIO, luce.getCostoLancio());
        assertEquals(FabbricaIncantesimi.costoAcquisto(ClasseIncantesimo.FULMINE),
                FabbricaIncantesimi.costoAcquisto(ClasseIncantesimo.LUCE_DIVINA));
    }

    @Test
    void ELInnataSoloDeiSacerdotiConLaMagiaSufficiente() {
        Personaggio sacerdote = FabbricaPersonaggi.crea(TipoPersonaggio.SACERDOTE, 5);
        Personaggio sacerdotessa = FabbricaPersonaggi.crea(TipoPersonaggio.SACERDOTESSA, 5);
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 5);
        for (Personaggio p : new Personaggio[]{sacerdote, sacerdotessa, guerriero}) {
            p.getModelloDati().set(TipoAttributo.MAGIA, Costanti.INCANTESIMO_LUCE_DIVINA_COSTO_LANCIO);
        }
        assertTrue(LuceDivina.puoLanciarlaInnata(sacerdote));
        assertTrue(LuceDivina.puoLanciarlaInnata(sacerdotessa));
        assertFalse(LuceDivina.puoLanciarlaInnata(guerriero), "gli altri hanno bisogno di una pergamena");

        sacerdote.getModelloDati().set(TipoAttributo.MAGIA, Costanti.INCANTESIMO_LUCE_DIVINA_COSTO_LANCIO - 1);
        assertFalse(LuceDivina.puoLanciarlaInnata(sacerdote));
        sacerdote.getModelloDati().set(TipoAttributo.MAGIA, Costanti.INCANTESIMO_LUCE_DIVINA_COSTO_LANCIO);
        sacerdote.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertFalse(LuceDivina.puoLanciarlaInnata(sacerdote), "il silenzio impedisce di lanciarla");
    }

    private static Personaggio scheletroAlleato(PartitaDiTest partita) {
        Personaggio scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        scheletro.setMotivoNelGruppo(com.threeamigos.foresta.tipi.TipoMotivoNelGruppo.MIETITURA);
        partita.gruppo().aggiungiPersonaggio(scheletro);
        return scheletro;
    }

    private static List<String> frasi(PartitaDiTest partita) {
        return partita.eventi().tutti(NotificaTestoFrase.class).stream().map(NotificaTestoFrase::getMessaggio)
                .collect(Collectors.toList());
    }

    @Test
    void ColpisceAncheLoScheletroAlleatoDiChiLaLancia() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(301)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio sacerdote = new Sacerdote("Fra Bartolomeo", 5);
            partita.gruppo().aggiungiPersonaggio(sacerdote);
            scheletroAlleato(partita);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            avversari.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 5));
            partita.eventi().ascolta(NotificaTestoFrase.class);
            int magiaPrima = sacerdote.getMagia();

            FabbricaIncantesimi.crea(ClasseIncantesimo.LUCE_DIVINA, sacerdote.getLivello()).formula(sacerdote, null, avversari);

            assertTrue(frasi(partita).contains(FRASE_SULLO_SCHELETRO), frasi(partita).toString());
            assertEquals(magiaPrima - Costanti.INCANTESIMO_LUCE_DIVINA_COSTO_LANCIO, sacerdote.getMagia());
        }
    }

    @Test
    void ChiNonEDelGruppoDelGiocatoreNonColpisceLoScheletroAlleato() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(302)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            scheletroAlleato(partita);
            Personaggio sacerdoteAvversario = new Sacerdote("Fra Cattivo", 5);
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            avversari.aggiungiPersonaggio(sacerdoteAvversario);
            partita.eventi().ascolta(NotificaTestoFrase.class);

            FabbricaIncantesimi.crea(ClasseIncantesimo.LUCE_DIVINA, 5).formula(sacerdoteAvversario, null, partita.gruppo());

            assertFalse(frasi(partita).contains(FRASE_SULLO_SCHELETRO), frasi(partita).toString());
        }
    }

    @Test
    void IMostriNonLaLanciano() {
        for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.LICH, TipoPersonaggio.STREGA, TipoPersonaggio.DRAGO}) {
            assertFalse(FabbricaPersonaggi.crea(classe, 5).getRepertorioIncantesimi().contains(ClasseIncantesimo.LUCE_DIVINA), classe.name());
        }
    }

    @Test
    void IlSacroFaIlDoppioAiNonMortiDelGruppoDiChiLoSubisce() {
        // La regola dei non morti (deboli al sacro) è quella di sempre: qui si controlla che valga per la Luce Divina
        assertEquals(2.0d, TipoPersonaggio.SCHELETRO.getMoltiplicatoreDannoSubito(TipoDanno.SACRO));
        assertEquals(2.0d, TipoPersonaggio.LICH.getMoltiplicatoreDannoSubito(TipoDanno.SACRO));
        assertEquals(1.0d, TipoPersonaggio.GOBLIN.getMoltiplicatoreDannoSubito(TipoDanno.SACRO));
    }
}
