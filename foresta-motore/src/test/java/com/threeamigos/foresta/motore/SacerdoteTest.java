package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.AlbaSacra;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.personaggi.Sacerdotessa;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sacerdote e Sacerdotessa: giocanti come Mago e Maga, ma con l'alba sacra al posto del dardo arcano, un incantesimo
 * innato che non consuma pergamene.
 */
class SacerdoteTest {

    @BeforeEach
    void preparaModello() {
        com.threeamigos.foresta.modellodati.ModelloDati.setIstanza(new com.threeamigos.foresta.modellodati.ModelloDati());
    }

    @Test
    void sonoClassiGiocantiMaschioEFemmina() {
        Personaggio sacerdote = new Sacerdote("Elia", 1);
        Personaggio sacerdotessa = new Sacerdotessa("Giuditta", 1);
        assertEquals(TipoPersonaggio.SACERDOTE, sacerdote.getClasse());
        assertEquals(TipoPersonaggio.SACERDOTESSA, sacerdotessa.getClasse());
        assertEquals(Personaggio.Sesso.MASCHIO, sacerdote.getSesso());
        assertEquals(Personaggio.Sesso.FEMMINA, sacerdotessa.getSesso());
        assertEquals("Sacerdote", sacerdote.getNomeSingolare());
        assertEquals("Sacerdotesse", sacerdotessa.getNomePlurale());
        assertTrue(sacerdote.isMagico());
    }

    @Test
    void nonHannoIlDardoArcano() {
        assertFalse(DardoArcano.conosciutoDa(TipoPersonaggio.SACERDOTE));
        assertFalse(DardoArcano.conosciutoDa(TipoPersonaggio.SACERDOTESSA));
        assertFalse(AlbaSacra.conosciutaDa(TipoPersonaggio.MAGO));
        assertFalse(AlbaSacra.conosciutaDa(TipoPersonaggio.ELFA));
        assertTrue(AlbaSacra.conosciutaDa(TipoPersonaggio.SACERDOTE));
        assertTrue(AlbaSacra.conosciutaDa(TipoPersonaggio.SACERDOTESSA));
    }

    @Test
    void serveAbbastanzaMagiaEBisognaPoterParlare() {
        Personaggio sacerdote = new Sacerdote("Elia", 1);
        assertTrue(AlbaSacra.puoLanciarlaInnata(sacerdote));
        sacerdote.subMagia(sacerdote.getMagia() - (Costanti.INCANTESIMO_ALBA_SACRA_COSTO_LANCIO - 1));
        assertFalse(AlbaSacra.puoLanciarlaInnata(sacerdote));

        Personaggio muta = new Sacerdotessa("Giuditta", 1);
        assertTrue(AlbaSacra.puoLanciarlaInnata(muta));
        muta.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertFalse(AlbaSacra.puoLanciarlaInnata(muta));

        assertFalse(AlbaSacra.puoLanciarlaInnata(new Mago("Merlino", 1)));
    }

    @Test
    void siScelgonoDallaSchermataDellaClasse() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
            partita.comando(Comando.PERGAMENA);
            partita.testo("Elia");
            partita.comando(Comando.MASCHIO);
            partita.assertComandoDisponibile(Comando.SACERDOTE);
            partita.assertComandoDisponibile(Comando.MAGO);
            partita.comando(Comando.SACERDOTE);
            assertEquals(TipoPersonaggio.SACERDOTE, partita.gruppo().getCapo().getClasse());
        }
        try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
            partita.comando(Comando.PERGAMENA);
            partita.testo("Giuditta");
            partita.comando(Comando.FEMMINA);
            partita.assertComandoDisponibile(Comando.SACERDOTESSA);
            partita.comando(Comando.SACERDOTESSA);
            assertEquals(TipoPersonaggio.SACERDOTESSA, partita.gruppo().getCapo().getClasse());
        }
    }

    @Test
    void laLanciaSenzaConsumarePergamene() {
        boolean provato = false;
        for (long seme = 1; seme <= 40 && !provato; seme++) {
            try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
                partita.iniziaCon("Elia", Comando.MASCHIO, Comando.SACERDOTE, () -> { });
                if (!partita.comandiDisponibili().contains(Comando.INCANTESIMO)) {
                    continue;
                }
                provato = true;
                // In modalità di prova il gruppo ha tante pergamene di ogni incantesimo: l'alba sacra è comunque una sola
                // scelta, e lanciarla non ne consuma
                Personaggio sacerdote = partita.gruppo().getCapo();
                int pergamene = partita.gruppo().getIncantesimi(ClasseIncantesimo.ALBA_SACRA);
                assertTrue(pergamene > 0);
                partita.comando(Comando.INCANTESIMO);
                Collection<Comando> scelte = partita.comandiDisponibili();
                assertEquals(1, scelte.stream().filter(c -> c == Comando.ALBA_SACRA).count(), scelte.toString());
                assertFalse(scelte.contains(Comando.DARDO_ARCANO));
                int magiaPrima = sacerdote.getMagia();
                partita.comando(Comando.ALBA_SACRA);
                assertEquals(magiaPrima - Costanti.INCANTESIMO_ALBA_SACRA_COSTO_LANCIO, sacerdote.getMagia());
                assertEquals(pergamene, partita.gruppo().getIncantesimi(ClasseIncantesimo.ALBA_SACRA));
            }
        }
        assertTrue(provato, "nessuna delle partite provate aveva avversari da affrontare all'inizio");
    }

    @Test
    void iSacerdotiDaReclutareSiIncontrano() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            // I compagni da reclutare, sistemati nelle città e nelle locande, ci sono tutti e due...
            java.util.Set<String> nomi = new java.util.TreeSet<>();
            while (RegistroPersonaggi.getNumeroPersonaggiDisponibili() > 0) {
                nomi.add(RegistroPersonaggi.getPersonaggioDisponibile().getNomeProprio().orElse("?"));
            }
            // ...salvo quelli già sistemati nella Foresta: li cerchiamo lì
            for (com.threeamigos.foresta.modellodati.CoordinateMD coordinate : com.threeamigos.foresta.modellodati.ModelloDati.getIstanza()
                    .getRegistroPersonaggiMD().getUbicazioniPersonaggi()) {
                nomi.add(RegistroPersonaggi.getPersonaggioInLocazione(coordinate).getNomeProprio().orElse("?"));
            }
            assertTrue(nomi.contains("Fra' Stornato"), nomi.toString());
            assertTrue(nomi.contains("Sorella Intronata"), nomi.toString());
        }
    }

    @Test
    void ilNomeConSpaziEApostrofoSopravviveAlSalvataggio() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(12)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            partita.gruppo().aggiungiPersonaggio(new Sacerdote("Fra' Stornato", 1));
            partita.gruppo().aggiungiPersonaggio(new Sacerdotessa("Sorella Intronata", 1));
            assertTrue(partita.salva(Comando.NUMERO_2));
            assertTrue(partita.leggi(Comando.NUMERO_2));
            java.util.List<String> nomi = new java.util.ArrayList<>();
            for (Personaggio personaggio : partita.gruppo().getPersonaggi()) {
                nomi.add(personaggio.getNomeProprio().orElse("?") + ":" + personaggio.getClasse());
            }
            assertTrue(nomi.contains("Fra' Stornato:SACERDOTE"), nomi.toString());
            assertTrue(nomi.contains("Sorella Intronata:SACERDOTESSA"), nomi.toString());
        }
    }

    @Test
    void ilSacerdoteSedutoInLocandaEUnPersonaggioDelSuoTipo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(13)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.SACERDOTE, TipoPersonaggio.SACERDOTESSA}) {
                com.threeamigos.foresta.intermezzi.ImmagineIntermezzo immagine = com.threeamigos.foresta.intermezzi.ScenaInLocanda
                        .conSacerdote(classe).getPagine().get(0).getElemento("locandiere").getImmagine();
                assertEquals(com.threeamigos.foresta.intermezzi.ImmagineIntermezzo.Tipo.PERSONAGGIO, immagine.getTipo());
                assertEquals(classe, immagine.getClassePersonaggio());
            }
            // Entrambi guardano verso sinistra, da dove arriva il gruppo (se serve, la UI specchia l'immagine)
            for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.SACERDOTE, TipoPersonaggio.SACERDOTESSA}) {
                assertEquals(com.threeamigos.foresta.intermezzi.Verso.SINISTRA, com.threeamigos.foresta.intermezzi.ScenaInLocanda
                        .conSacerdote(classe).getPagine().get(0).getElemento("locandiere").getStatoAl(0).getVerso());
            }
            assertThrows(IllegalArgumentException.class,
                    () -> com.threeamigos.foresta.intermezzi.ScenaInLocanda.conSacerdote(TipoPersonaggio.MAGO));
        }
    }

    @Test
    void rimuovendoTuttiGliEffettiOgniNotificaDiceQualeEffettoEFinito() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(14)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            partita.eventi().ascolta(com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio.class);
            Personaggio sacerdote = partita.gruppo().getCapo();
            sacerdote.addEffettoDiStato(TipoEffettoDiStato.BRUCIATO, 3, 0);
            sacerdote.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 0);
            partita.eventi().svuota();
            // Come fa l'alba sacra: toglie tutto, e la UI deve sapere cosa
            sacerdote.rimuoviTuttiGliEffettiDiStato();
            java.util.Set<TipoEffettoDiStato> rimossi = new java.util.HashSet<>();
            for (com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio evento
                    : partita.eventi().tutti(com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio.class)) {
                assertNotNull(evento.getEffetto(), "l'evento di rimozione non dice quale effetto");
                rimossi.add(evento.getEffetto());
            }
            assertEquals(new java.util.HashSet<>(java.util.Arrays.asList(TipoEffettoDiStato.BRUCIATO, TipoEffettoDiStato.AVVELENATO)), rimossi);
        }
    }
}
