package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.abilita.*;
import com.threeamigos.foresta.incantesimi.AlbaSacra;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.LuceDivina;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chierico e Chierica, Druido e Druida: le classi (copie di Guerriero e Bardo con valori propri), le armi che usano e le abilità innate, che non
 * consumano pergamene (vedi il pacchetto {@code abilita}).
 */
class ChiericoEDruidoTest {

    private Gruppo compagni;
    private GruppoAvversario avversari;

    @BeforeEach
    void preparaGruppi() {
        ModelloDati.setIstanza(new ModelloDati());
        GruppoAvversario.azzeraIstanza();
        avversari = GruppoAvversario.getIstanza();
        compagni = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return true;
            }
        };
    }

    private static void fissa(PersonaggioBase personaggio, TipoAttributo attributo, int valore) {
        personaggio.addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.QUANTITA_ASSOLUTA, valore));
    }

    private static Chierico chierico() {
        Chierico chierico = new Chierico("Ambrogio", 5);
        chierico.addMagia(1000);
        chierico.addSalute(10000);
        return chierico;
    }

    private static Druido druido() {
        Druido druido = new Druido("Quercia", 5);
        druido.addMagia(1000);
        druido.addSalute(10000);
        return druido;
    }

    // --- Le classi

    @Test
    void sonoClassiGiocantiMaschioEFemmina() {
        Personaggio chierico = new Chierico("Ambrogio", 1);
        Personaggio chierica = new Chierica("Chiara", 1);
        Personaggio druido = new Druido("Quercia", 1);
        Personaggio druida = new Druida("Edera", 1);
        assertEquals(TipoPersonaggio.CHIERICO, chierico.getClasse());
        assertEquals(TipoPersonaggio.CHIERICA, chierica.getClasse());
        assertEquals(TipoPersonaggio.DRUIDO, druido.getClasse());
        assertEquals(TipoPersonaggio.DRUIDA, druida.getClasse());
        assertEquals(Personaggio.Sesso.MASCHIO, chierico.getSesso());
        assertEquals(Personaggio.Sesso.FEMMINA, chierica.getSesso());
        assertEquals(Personaggio.Sesso.MASCHIO, druido.getSesso());
        assertEquals(Personaggio.Sesso.FEMMINA, druida.getSesso());
        assertEquals("Chierici", chierico.getNomePlurale());
        assertEquals("Chierice", chierica.getNomePlurale());
        assertEquals("Druidi", druido.getNomePlurale());
        assertEquals("Druide", druida.getNomePlurale());
        assertEquals("Chierica", TipoPersonaggio.CHIERICA.getNome());
        assertEquals("Druido", TipoPersonaggio.DRUIDO.getNome());
        for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.CHIERICO, TipoPersonaggio.CHIERICA, TipoPersonaggio.DRUIDO, TipoPersonaggio.DRUIDA)) {
            assertEquals(classe, FabbricaPersonaggi.crea(classe, 1).getClasse());
            assertTrue(classe.isSoloDiProva(), classe.name());
            assertTrue(classe.isDellaCategoria(CategoriaPersonaggio.UMANOIDE));
        }
        assertFalse(TipoPersonaggio.SACERDOTE.isSoloDiProva());
        assertFalse(TipoPersonaggio.BARDO.isSoloDiProva());
    }

    @Test
    void ilFemminileHaLoStessoRuoloEGliStessiValori() {
        assertTrue(TipoPersonaggio.CHIERICO.stessoRuoloDi(TipoPersonaggio.CHIERICA));
        assertTrue(TipoPersonaggio.DRUIDO.stessoRuoloDi(TipoPersonaggio.DRUIDA));
        assertFalse(TipoPersonaggio.CHIERICO.stessoRuoloDi(TipoPersonaggio.DRUIDO));
        Personaggio chierico = FabbricaPersonaggi.modello(TipoPersonaggio.CHIERICO);
        Personaggio chierica = FabbricaPersonaggi.modello(TipoPersonaggio.CHIERICA);
        assertEquals(chierico.getMoltiplicatoreParata(), chierica.getMoltiplicatoreParata());
        assertEquals(chierico.getMoltiplicatoreDanniFisici(), chierica.getMoltiplicatoreDanniFisici());
        assertEquals(((PersonaggioBase) chierico).getSaluteBase(), ((PersonaggioBase) chierica).getSaluteBase());
        Personaggio druido = FabbricaPersonaggi.modello(TipoPersonaggio.DRUIDO);
        Personaggio druida = FabbricaPersonaggi.modello(TipoPersonaggio.DRUIDA);
        assertEquals(druido.getMoltiplicatoreDanniMagici(), druida.getMoltiplicatoreDanniMagici());
        assertEquals(druido.getMoltiplicatorePercezione(), druida.getMoltiplicatorePercezione());
    }

    @Test
    void ilChiericoStaFraIlSacerdoteEIlGuerriero() {
        Personaggio chierico = FabbricaPersonaggi.modello(TipoPersonaggio.CHIERICO);
        Personaggio sacerdote = FabbricaPersonaggi.modello(TipoPersonaggio.SACERDOTE);
        Personaggio guerriero = FabbricaPersonaggi.modello(TipoPersonaggio.GUERRIERO);
        assertTrue(chierico.getMoltiplicatoreParata() > sacerdote.getMoltiplicatoreParata());
        assertTrue(chierico.getMoltiplicatoreParata() < guerriero.getMoltiplicatoreParata());
        assertTrue(chierico.getMoltiplicatoreDanniFisici() > sacerdote.getMoltiplicatoreDanniFisici());
        assertTrue(chierico.getMoltiplicatoreDanniFisici() < guerriero.getMoltiplicatoreDanniFisici());
        assertTrue(chierico.getMoltiplicatoreDanniMagici() < sacerdote.getMoltiplicatoreDanniMagici());
        assertTrue(chierico.getMoltiplicatoreDanniMagici() > guerriero.getMoltiplicatoreDanniMagici());
        assertTrue(chierico.getMoltiplicatoreRecuperoMagico() < sacerdote.getMoltiplicatoreRecuperoMagico());
    }

    @Test
    void resistenzeDiClasse() {
        assertEquals(0.75d, TipoPersonaggio.CHIERICO.getMoltiplicatoreDannoSubito(TipoDanno.NECROTICO), 1e-9);
        assertEquals(0.75d, TipoPersonaggio.CHIERICA.getMoltiplicatoreDannoSubito(TipoDanno.MALEDIZIONE), 1e-9);
        assertEquals(1.0d, TipoPersonaggio.SACERDOTE.getMoltiplicatoreDannoSubito(TipoDanno.NECROTICO), 1e-9);
        assertEquals(0.75d, TipoPersonaggio.DRUIDO.getMoltiplicatoreDannoSubito(TipoDanno.TERRA), 1e-9);
        assertEquals(0.75d, TipoPersonaggio.DRUIDA.getMoltiplicatoreDannoSubito(TipoDanno.VELENO), 1e-9);
        assertEquals(1.25d, TipoPersonaggio.DRUIDO.getMoltiplicatoreDannoSubito(TipoDanno.FUOCO), 1e-9);
        assertEquals(1.1d, TipoPersonaggio.DRUIDA.getMoltiplicatoreDannoSubito(TipoDanno.TAGLIENTE), 1e-9);
    }

    @Test
    void usanoLeArmiDellaLoroClasse() {
        for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.CHIERICO, TipoPersonaggio.CHIERICA)) {
            for (TipoArtefatto tipo : Arrays.asList(TipoArtefatto.MAZZA, TipoArtefatto.SPADA, TipoArtefatto.SCUDO, TipoArtefatto.ARMATURA)) {
                assertTrue(RegoleEquipaggiamento.puoUsare(classe, tipo), classe + " " + tipo);
            }
            for (TipoArtefatto tipo : Arrays.asList(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.LIBRO_MAGICO, TipoArtefatto.ASCIA, TipoArtefatto.SPADONE)) {
                assertFalse(RegoleEquipaggiamento.puoUsare(classe, tipo), classe + " " + tipo);
            }
            assertFalse(RegoleEquipaggiamento.puoImpugnareDueArmi(classe));
        }
        for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.DRUIDO, TipoPersonaggio.DRUIDA)) {
            for (TipoArtefatto tipo : Arrays.asList(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.LANCIA, TipoArtefatto.LIBRO_MAGICO)) {
                assertTrue(RegoleEquipaggiamento.puoUsare(classe, tipo), classe + " " + tipo);
            }
            // Niente metallo e nessun arco (nel gioco non ce ne sono)
            for (TipoArtefatto tipo : Arrays.asList(TipoArtefatto.SCUDO, TipoArtefatto.ARMATURA, TipoArtefatto.SPADA, TipoArtefatto.MAZZA)) {
                assertFalse(RegoleEquipaggiamento.puoUsare(classe, tipo), classe + " " + tipo);
            }
            assertFalse(RegoleEquipaggiamento.puoImpugnareDueArmi(classe));
        }
    }

    // --- Alba sacra e luce divina del chierico

    @Test
    void ilChiericoHaInnateAlbaSacraELuceDivina() {
        for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.CHIERICO, TipoPersonaggio.CHIERICA)) {
            assertTrue(AlbaSacra.conosciutaDa(classe), classe.name());
            assertTrue(LuceDivina.conosciutaDa(classe), classe.name());
            assertFalse(DardoArcano.conosciutoDa(classe), classe.name());
        }
        assertTrue(AlbaSacra.puoLanciarlaInnata(chierico()));
        assertTrue(LuceDivina.puoLanciarlaInnata(chierico()));
        assertFalse(AlbaSacra.conosciutaDa(TipoPersonaggio.DRUIDO));
        assertFalse(LuceDivina.conosciutaDa(TipoPersonaggio.DRUIDA));
    }

    // --- Chi ha le abilità

    @Test
    void ognunaHaIlSuoComandoELeAbilitaSonoDiUnaSolaClasse() {
        for (TipoAbilita tipo : TipoAbilita.values()) {
            assertSame(tipo, TipoAbilita.ofComando(tipo.getComando()), tipo.name());
            assertNotNull(tipo.getNome());
            assertNotNull(tipo.getDescrizione());
            assertNotNull(TipoAzione.valueOf(tipo.name()), "ha un'azione con lo stesso nome");
            int classi = 0;
            for (TipoPersonaggio classe : TipoPersonaggio.values()) {
                if (AbilitaInnate.conosciutaDa(classe, tipo)) {
                    classi++;
                }
            }
            assertEquals(2, classi, tipo.name() + ": maschile e femminile");
        }
        assertNull(TipoAbilita.ofComando(Comando.CANTO_CURATIVO));
        assertNull(TipoAbilita.ofComando(Comando.ANNULLA));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.CHIERICA, TipoAbilita.SCACCIARE_I_NON_MORTI));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.CHIERICO, TipoAbilita.IMPOSIZIONE_DELLE_MANI));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.CHIERICO, TipoAbilita.RADICI));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.DRUIDA, TipoAbilita.SCIAME));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.DRUIDO, TipoAbilita.IMPOSIZIONE_DELLE_MANI));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.SACERDOTE));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.BARDO));
    }

    @Test
    void servonoLaMagiaEUnaVoceLibera() {
        Chierico chierico = chierico();
        assertEquals(Arrays.asList(TipoAbilita.SCACCIARE_I_NON_MORTI, TipoAbilita.IMPOSIZIONE_DELLE_MANI), AbilitaInnate.abilitaPossibili(chierico));
        Druido druido = druido();
        assertEquals(Arrays.asList(TipoAbilita.RADICI, TipoAbilita.SCIAME, TipoAbilita.PELLE_DI_CORTECCIA, TipoAbilita.RIGENERAZIONE),
                AbilitaInnate.abilitaPossibili(druido));
        assertTrue(AbilitaInnate.abilitaPossibili(new Mago("Merlino", 5)).isEmpty());

        // Con poca magia si fa solo quello che costa poco
        druido.subMagia(druido.getMagia() - Costanti.ABILITA_RADICI_COSTO_LANCIO);
        assertTrue(AbilitaInnate.puoUsare(druido, TipoAbilita.RADICI));
        assertFalse(AbilitaInnate.puoUsare(druido, TipoAbilita.PELLE_DI_CORTECCIA));
        druido.subMagia(druido.getMagia());
        assertTrue(AbilitaInnate.abilitaPossibili(druido).isEmpty());

        Druido silenziato = druido();
        silenziato.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertTrue(AbilitaInnate.abilitaPossibili(silenziato).isEmpty());
        Chierico morto = chierico();
        morto.muore("per una preghiera sbagliata");
        assertTrue(AbilitaInnate.abilitaPossibili(morto).isEmpty());
    }

    @Test
    void ognunaCostaLaSuaMagia() {
        Chierico chierico = chierico();
        Druido druido = druido();
        assertEquals(Costanti.ABILITA_SCACCIARE_I_NON_MORTI_COSTO_LANCIO, FabbricaAbilita.crea(TipoAbilita.SCACCIARE_I_NON_MORTI, chierico).getCostoLancio());
        assertEquals(Costanti.ABILITA_IMPOSIZIONE_DELLE_MANI_COSTO_LANCIO, FabbricaAbilita.crea(TipoAbilita.IMPOSIZIONE_DELLE_MANI, chierico).getCostoLancio());
        assertEquals(Costanti.ABILITA_RADICI_COSTO_LANCIO, FabbricaAbilita.crea(TipoAbilita.RADICI, druido).getCostoLancio());
        assertEquals(Costanti.ABILITA_SCIAME_COSTO_LANCIO, FabbricaAbilita.crea(TipoAbilita.SCIAME, druido).getCostoLancio());
        assertEquals(Costanti.ABILITA_PELLE_DI_CORTECCIA_COSTO_LANCIO, FabbricaAbilita.crea(TipoAbilita.PELLE_DI_CORTECCIA, druido).getCostoLancio());
        assertEquals(Costanti.ABILITA_RIGENERAZIONE_COSTO_LANCIO, FabbricaAbilita.crea(TipoAbilita.RIGENERAZIONE, druido).getCostoLancio());
        for (TipoAbilita tipo : TipoAbilita.values()) {
            Personaggio chi = AbilitaInnate.conosciutaDa(TipoPersonaggio.CHIERICO, tipo) ? chierico : druido;
            assertSame(tipo, FabbricaAbilita.crea(tipo, chi).getTipo());
        }
    }

    // --- Scacciare i non morti

    @Test
    void scacciareFaPauraAiNonMortiMaNonAgliAltri() {
        Chierico chierico = chierico();
        fissa(chierico, TipoAttributo.SOGGEZIONE, 1000);
        compagni.aggiungiPersonaggio(chierico);
        Spettro spettro = new Spettro(5);
        Goblin goblin = new Goblin(5);
        avversari.aggiungiPersonaggio(goblin);
        // Solo un goblin: non serve, e non parte
        assertFalse(AbilitaInnate.serve(TipoAbilita.SCACCIARE_I_NON_MORTI, compagni, avversari));
        int magia = chierico.getMagia();
        assertFalse(FabbricaAbilita.crea(TipoAbilita.SCACCIARE_I_NON_MORTI, chierico).usa(chierico, compagni, avversari));
        assertEquals(magia, chierico.getMagia());

        avversari.aggiungiPersonaggio(spettro);
        fissa(spettro, TipoAttributo.CORAGGIO, 0);
        assertTrue(AbilitaInnate.serve(TipoAbilita.SCACCIARE_I_NON_MORTI, compagni, avversari));
        assertEquals(100.0d, ScacciareINonMorti.probabilitaDiCedere(chierico, spettro), 1e-9);
        assertTrue(FabbricaAbilita.crea(TipoAbilita.SCACCIARE_I_NON_MORTI, chierico).usa(chierico, compagni, avversari));
        assertEquals(magia - Costanti.ABILITA_SCACCIARE_I_NON_MORTI_COSTO_LANCIO, chierico.getMagia());
        assertTrue(spettro.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO));
        assertFalse(goblin.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO));
        // Ora è già spaventato: non serve più
        assertFalse(AbilitaInnate.serve(TipoAbilita.SCACCIARE_I_NON_MORTI, compagni, avversari));
    }

    @Test
    void loScheletroELichSonoImmuniAlloSpavento() {
        Chierico chierico = chierico();
        compagni.aggiungiPersonaggio(chierico);
        avversari.aggiungiPersonaggio(new Scheletro(5));
        avversari.aggiungiPersonaggio(new Lich(5));
        assertFalse(AbilitaInnate.serve(TipoAbilita.SCACCIARE_I_NON_MORTI, compagni, avversari));
    }

    @Test
    void conUnaSoggezioneNullaIlNonMortoNonCede() {
        Chierico chierico = chierico();
        fissa(chierico, TipoAttributo.SOGGEZIONE, 0);
        Spettro spettro = new Spettro(5);
        fissa(spettro, TipoAttributo.CORAGGIO, 100);
        assertEquals(0.0d, ScacciareINonMorti.probabilitaDiCedere(chierico, spettro), 1e-9);
    }

    // --- Imposizione delle mani e rigenerazione

    @Test
    void imposizioneCuraIlPiuFeritoEMaiOltreIlMassimo() {
        Chierico chierico = chierico();
        Guerriero lieve = new Guerriero("Lieve", 5);
        Guerriero grave = new Guerriero("Grave", 5);
        compagni.aggiungiPersonaggio(chierico);
        compagni.aggiungiPersonaggio(lieve);
        compagni.aggiungiPersonaggio(grave);
        lieve.subSalute(lieve.getSaluteMassima() / 10, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        grave.subSalute(grave.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        int saluteLieve = lieve.getSalute();
        int saluteGrave = grave.getSalute();
        int magia = chierico.getMagia();
        ImposizioneDelleMani imposizione = (ImposizioneDelleMani) FabbricaAbilita.crea(TipoAbilita.IMPOSIZIONE_DELLE_MANI, chierico);

        assertTrue(imposizione.usa(chierico, compagni, avversari));

        assertEquals(Math.min(grave.getSaluteMassima(), saluteGrave + imposizione.getCura()), grave.getSalute());
        assertEquals(saluteLieve, lieve.getSalute(), "ne cura uno solo");
        assertEquals(magia - Costanti.ABILITA_IMPOSIZIONE_DELLE_MANI_COSTO_LANCIO, chierico.getMagia());
    }

    @Test
    void imposizioneCuraMenoDelCantoCurativoECresceConIlLivello() {
        Chierico chierico = new Chierico("Ambrogio", 5);
        Bardo bardo = new Bardo("Orfeo", 5);
        assertTrue(new ImposizioneDelleMani(chierico).getCura() < new com.threeamigos.foresta.canti.CantoCurativo(bardo).getCura());
        assertTrue(new ImposizioneDelleMani(new Chierico("Alto", 9)).getCura() > new ImposizioneDelleMani(new Chierico("Basso", 1)).getCura());
    }

    @Test
    void senzaFeritiNonSiImponeNeSiPagaMagia() {
        Chierico chierico = chierico();
        compagni.aggiungiPersonaggio(chierico);
        chierico.addSalute(10000);
        int magia = chierico.getMagia();
        assertFalse(FabbricaAbilita.crea(TipoAbilita.IMPOSIZIONE_DELLE_MANI, chierico).usa(chierico, compagni, avversari));
        assertEquals(magia, chierico.getMagia());
    }

    @Test
    void laRigenerazioneGuariscePocoAOgniTurno() {
        Druido druido = druido();
        Guerriero ferito = new Guerriero("Ferito", 5);
        compagni.aggiungiPersonaggio(druido);
        compagni.aggiungiPersonaggio(ferito);
        ferito.subSalute(ferito.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        Rigenerazione rigenerazione = (Rigenerazione) FabbricaAbilita.crea(TipoAbilita.RIGENERAZIONE, druido);
        int cura = rigenerazione.getCuraPerTurno(ferito);
        assertTrue(cura > 0);
        int salute = ferito.getSalute();
        int magia = druido.getMagia();

        assertTrue(rigenerazione.usa(druido, compagni, avversari));

        assertEquals(magia - Costanti.ABILITA_RIGENERAZIONE_COSTO_LANCIO, druido.getMagia());
        assertTrue(ferito.hasEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE));
        assertEquals(salute, ferito.getSalute(), "non guarisce subito");
        ferito.applicaDanniDaEffettiDiStato();
        assertEquals(salute + cura, ferito.getSalute());
        // L'effetto dura qualche turno e poi finisce
        for (int turno = 1; turno < Costanti.ABILITA_RIGENERAZIONE_DURATA; turno++) {
            ferito.riduciEffettiDiStato();
            assertTrue(ferito.hasEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE));
        }
        ferito.riduciEffettiDiStato();
        assertFalse(ferito.hasEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE));
        // Chi si rigenera già non è più da rigenerare
        ferito.addEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE, 3, 0, cura);
        assertFalse(AbilitaInnate.daRigenerare(compagni).isPresent());
        assertTrue(TipoEffettoDiStato.RIGENERAZIONE.isBenefico());
    }

    // --- Radici e sciame

    @Test
    void radiciESciameFannoDannoACheNonEImmune() {
        for (TipoAbilita tipo : Arrays.asList(TipoAbilita.RADICI, TipoAbilita.SCIAME)) {
            avversari = azzeraAvversari();
            Druido druido = druido();
            fissa(druido, TipoAttributo.PRECISIONE, 1000);
            compagni = new Gruppo() {
                @Override
                public boolean isGruppoGiocatore() {
                    return true;
                }
            };
            compagni.aggiungiPersonaggio(druido);
            Goblin goblin = new Goblin(5);
            avversari.aggiungiPersonaggio(goblin);
            int salute = goblin.getSalute();
            int magia = druido.getMagia();
            Abilita abilita = FabbricaAbilita.crea(tipo, druido);

            boolean fatto = false;
            for (int i = 0; i < 40 && goblin.isVivo() && goblin.getSalute() == salute; i++) {
                assertTrue(abilita.usa(druido, compagni, avversari));
                fatto = true;
            }
            assertTrue(fatto);
            assertTrue(goblin.getSalute() < salute || !goblin.isVivo(), tipo + " dovrebbe aver ferito il goblin");
            assertTrue(druido.getMagia() < magia);
            assertEquals(0, (magia - druido.getMagia()) % (tipo == TipoAbilita.RADICI ? Costanti.ABILITA_RADICI_COSTO_LANCIO : Costanti.ABILITA_SCIAME_COSTO_LANCIO));
        }
    }

    @Test
    void ilGargoyleDiPietraNonSenteIlVeleno() {
        Druido druido = druido();
        fissa(druido, TipoAttributo.PRECISIONE, 1000);
        compagni.aggiungiPersonaggio(druido);
        Gargoyle gargoyle = new Gargoyle(5);
        avversari.aggiungiPersonaggio(gargoyle);
        int salute = gargoyle.getSalute();
        for (int i = 0; i < 10; i++) {
            FabbricaAbilita.crea(TipoAbilita.SCIAME, druido).usa(druido, compagni, avversari);
        }
        assertEquals(salute, gargoyle.getSalute());
    }

    @Test
    void senzaAvversariLeRadiciNonPartono() {
        Druido druido = druido();
        compagni.aggiungiPersonaggio(druido);
        int magia = druido.getMagia();
        assertFalse(FabbricaAbilita.crea(TipoAbilita.RADICI, druido).usa(druido, compagni, avversari));
        assertEquals(magia, druido.getMagia());
        assertTrue(AbilitaInnate.abilitaUtili(druido, compagni, avversari).isEmpty());
    }

    // --- Pelle di corteccia

    @Test
    void laPelleDiCortecciaIlGruppoPerQualcheTurnoENonSiToglieConLAlbaSacra() {
        Druido druido = druido();
        Guerriero guerriero = new Guerriero("Rocco", 5);
        compagni.aggiungiPersonaggio(druido);
        compagni.aggiungiPersonaggio(guerriero);
        avversari.aggiungiPersonaggio(new Goblin(5));
        int parata = guerriero.getParata();
        int magia = druido.getMagia();

        assertTrue(FabbricaAbilita.crea(TipoAbilita.PELLE_DI_CORTECCIA, druido).usa(druido, compagni, avversari));

        assertEquals(magia - Costanti.ABILITA_PELLE_DI_CORTECCIA_COSTO_LANCIO, druido.getMagia());
        assertTrue(guerriero.getParata() > parata);
        for (Personaggio personaggio : compagni.getPersonaggi()) {
            assertEquals(2, personaggio.getModificatoriTemporanei().size());
            for (ModificatoreAttributoMD modificatore : personaggio.getModificatoriTemporanei()) {
                assertEquals(PelleDiCorteccia.NOTA, modificatore.getNote());
                assertEquals(UnitaDurataModificatore.TURNI, modificatore.getUnitaDurata());
                assertEquals(Costanti.ABILITA_PELLE_DI_CORTECCIA_DURATA, modificatore.getDurata());
            }
        }
        guerriero.rimuoviIModificatoriTemporaneiRimovibili();
        assertEquals(2, guerriero.getModificatoriTemporanei().size());
        // Usarla di nuovo non raddoppia l'effetto
        FabbricaAbilita.crea(TipoAbilita.PELLE_DI_CORTECCIA, druido).usa(druido, compagni, avversari);
        assertEquals(2, guerriero.getModificatoriTemporanei().size());
        // Passano i turni e finisce
        for (int turno = 0; turno < Costanti.ABILITA_PELLE_DI_CORTECCIA_DURATA; turno++) {
            guerriero.riduciModificatori(UnitaDurataModificatore.TURNI);
        }
        assertTrue(guerriero.getModificatoriTemporanei().isEmpty());
        assertEquals(parata, guerriero.getParata());
    }

    private static GruppoAvversario azzeraAvversari() {
        GruppoAvversario.azzeraIstanza();
        return GruppoAvversario.getIstanza();
    }

    @Test
    void ilMenuDelleAbilitaSegueQuelloCheServe() {
        Chierico chierico = chierico();
        compagni.aggiungiPersonaggio(chierico);
        avversari.aggiungiPersonaggio(new Goblin(5));
        // Nessun ferito e nessun non morto: non c'è niente da fare
        assertEquals(Collections.emptyList(), AbilitaInnate.abilitaUtili(chierico, compagni, avversari));
        chierico.subSalute(chierico.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertEquals(Collections.singletonList(TipoAbilita.IMPOSIZIONE_DELLE_MANI), AbilitaInnate.abilitaUtili(chierico, compagni, avversari));
        avversari.aggiungiPersonaggio(new Spettro(5));
        List<TipoAbilita> utili = AbilitaInnate.abilitaUtili(chierico, compagni, avversari);
        assertEquals(Arrays.asList(TipoAbilita.SCACCIARE_I_NON_MORTI, TipoAbilita.IMPOSIZIONE_DELLE_MANI), utili);
    }

    // --- La scelta iniziale: solo in modalità di prova

    @Test
    void inModalitaDiProvaSiScelgonoAncheChiericoEDruido() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(21)) {
            partita.comando(Comando.PERGAMENA);
            partita.testo("Elia");
            partita.comando(Comando.MASCHIO);
            assertTrue(partita.comandiDisponibili().containsAll(Arrays.asList(Comando.CHIERICO, Comando.DRUIDO)), partita.comandiDisponibili().toString());
            assertFalse(partita.comandiDisponibili().contains(Comando.CHIERICA));
        }
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.comando(Comando.PERGAMENA);
            partita.testo("Giulia");
            partita.comando(Comando.FEMMINA);
            assertTrue(partita.comandiDisponibili().containsAll(Arrays.asList(Comando.CHIERICA, Comando.DRUIDA)), partita.comandiDisponibili().toString());
            assertFalse(partita.comandiDisponibili().contains(Comando.DRUIDO));
        }
    }

    @Test
    void sceltiInProvaSonoIlCapoDelGruppo() {
        for (Object[] scelta : new Object[][]{{Comando.MASCHIO, Comando.CHIERICO, TipoPersonaggio.CHIERICO},
                {Comando.FEMMINA, Comando.CHIERICA, TipoPersonaggio.CHIERICA}, {Comando.MASCHIO, Comando.DRUIDO, TipoPersonaggio.DRUIDO},
                {Comando.FEMMINA, Comando.DRUIDA, TipoPersonaggio.DRUIDA}}) {
            try (PartitaDiTest partita = PartitaDiTest.nuova(23)) {
                partita.iniziaCon("Prova", (Comando) scelta[0], (Comando) scelta[1], () -> { });
                assertEquals(scelta[2], partita.gruppo().getCapo().getClasse());
            }
        }
    }

    @Test
    void senzaLaModalitaDiProvaNonSiScelgono() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(24)) {
            partita.comando(Comando.PERGAMENA);
            partita.testo("Elia");
            partita.comando(Comando.MASCHIO);
            assertEquals(6, partita.comandiDisponibili().size(), partita.comandiDisponibili().toString());
            assertFalse(partita.comandiDisponibili().contains(Comando.CHIERICO));
            assertFalse(partita.comandiDisponibili().contains(Comando.DRUIDO));
            // E se il comando arriva lo stesso, si rifiuta
            assertThrows(AssertionError.class, () -> partita.comando(Comando.CHIERICO));
        }
    }

    @Test
    void nonCompaionoFraIChampioniDellaSfidaSenzaProva() {
        boolean prova = ModalitaDiProva.isAttiva();
        try {
            ModalitaDiProva.setAttiva(false);
            assertFalse(com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni.classiAmichevoli().contains(TipoPersonaggio.CHIERICO));
            assertFalse(com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni.classiAmichevoli().contains(TipoPersonaggio.DRUIDA));
            assertTrue(com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni.classiAmichevoli().contains(TipoPersonaggio.SACERDOTE));
            ModalitaDiProva.setAttiva(true);
            assertTrue(com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni.classiAmichevoli().contains(TipoPersonaggio.CHIERICO));
        } finally {
            ModalitaDiProva.setAttiva(prova);
        }
    }
}
