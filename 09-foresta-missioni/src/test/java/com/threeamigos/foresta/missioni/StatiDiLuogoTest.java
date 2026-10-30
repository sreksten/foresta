package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.incarichi.IlColpo;
import com.threeamigos.foresta.missioni.incarichi.IlNegozioDellAlchimista;
import com.threeamigos.foresta.missioni.incarichi.IlRituale;
import com.threeamigos.foresta.missioni.incarichi.IncaricoInCitta;
import com.threeamigos.foresta.missioni.incarichi.LIndagine;
import com.threeamigos.foresta.missioni.incarichi.LOggettoSmarrito;
import com.threeamigos.foresta.missioni.incarichi.SceltaDegliIncarichi;
import com.threeamigos.foresta.missioni.leggende.LaFalceDelTristoMietitore;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDelLocandiere;
import com.threeamigos.foresta.missioni.richieste.ColpoRichiesto;
import com.threeamigos.foresta.missioni.richieste.RitualeRichiesto;
import com.threeamigos.foresta.missioni.storie.LaBenedizione;
import com.threeamigos.foresta.missioni.storie.LaLealta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.Quadrante;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le domande che le missioni si fanno sul luogo (sono in una città? sono nel posto giusto?) e le loro risposte in un luogo che il gioco
 * di rado offre: una città distrutta, nessuna casella per il gruppo, rovine da costruire in un altro quadrante, boschi che mancano.
 * Dove la domanda è privata la si fa con la reflection: è la domanda che conta, non come si chiama.
 */
class StatiDiLuogoTest {

    private static final TipoLocazione[] CITTA = {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA,
            TipoLocazione.CITTA_FLEENA};

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(91);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
    }

    @AfterEach
    void ripulisce() {
        SceltaDegliIncarichi.setSorteggioAttivo(true);
        MondoDiProva.finito();
    }

    private static Object invoca(Object oggetto, String nome) throws Exception {
        for (Class<?> c = oggetto.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Method metodo = c.getDeclaredMethod(nome);
                metodo.setAccessible(true);
                try {
                    return metodo.invoke(oggetto);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof Exception) {
                        throw (Exception) e.getCause();
                    }
                    throw e;
                }
            } catch (NoSuchMethodException prossima) {
                // si cerca nella classe sopra
            }
        }
        throw new NoSuchMethodException(nome);
    }

    private static ForestaMD datiDellaForesta() throws ReflectiveOperationException {
        Method dati = Foresta.class.getDeclaredMethod("getForestaMD");
        dati.setAccessible(true);
        return (ForestaMD) dati.invoke(null);
    }

    private <M extends Missione> M registra(M m, String id) {
        m.getModelloDati().setId(id);
        RegistroMissioni.aggiungiMissioneSecondaria(m);
        return m;
    }

    // ---- Dove sta il gruppo

    @Test
    void inUnaCittaDistrutta() throws Exception {
        LaLeggendaDellArmaiolo armaiolo = registra(new LaLeggendaDellArmaiolo(), "ARMAIOLO");
        LaFalceDelTristoMietitore falce = registra(new LaFalceDelTristoMietitore(), "FALCE");
        IlNegozioDellAlchimista negozio = registra(new IlNegozioDellAlchimista(), "NEGOZIO");
        TipoLocazione citta = TipoLocazione.CITTA_RUUNA;
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(citta));
        assertTrue((Boolean) invoca(armaiolo, "isPostoDelRacconto"));
        assertTrue((Boolean) invoca(falce, "isInUnaCitta"));
        assertTrue((Boolean) invoca(negozio, "inUnaCitta"));
        assertNotNull(invoca(armaiolo, "getNarratoreConNome"));
        Foresta.setCittaDistrutta(citta);
        assertFalse((Boolean) invoca(armaiolo, "isPostoDelRacconto"));
        assertFalse((Boolean) invoca(falce, "isInUnaCitta"));
        assertFalse((Boolean) invoca(negozio, "inUnaCitta"));
        // Fuori da una città, il narratore è solo il suo mestiere
        Foresta.setPosizioneDelGruppo(new CoordinateMD(0, 0));
        assertEquals("l'armaiolo", invoca(armaiolo, "getNarratoreConNome"));
        assertFalse((Boolean) invoca(armaiolo, "isPostoDelRacconto"));
        assertFalse((Boolean) invoca(falce, "isInUnaCitta"));
        assertNotNull(new LaLeggendaDelLocandiere());
    }

    @Test
    void suUnaCasellaSenzaLocazioneNonSiEInUnaCitta() throws Exception {
        LaLeggendaDellArmaiolo armaiolo = registra(new LaLeggendaDellArmaiolo(), "ARMAIOLO");
        LaFalceDelTristoMietitore falce = registra(new LaFalceDelTristoMietitore(), "FALCE");
        IlNegozioDellAlchimista negozio = registra(new IlNegozioDellAlchimista(), "NEGOZIO");
        Foresta.azzera(MondoDiProva.LATO, MondoDiProva.LATO);
        assertNull(Foresta.getLocazioneDelGruppo(), "il gruppo sta in (0, 0), che è una casella vuota");
        assertFalse((Boolean) invoca(armaiolo, "isPostoDelRacconto"));
        assertFalse((Boolean) invoca(falce, "isInUnaCitta"));
        assertFalse((Boolean) invoca(negozio, "inUnaCitta"));
        assertEquals("l'armaiolo", invoca(armaiolo, "getNarratoreConNome"));
    }

    @Test
    void senzaUnaCasellaNessunPostoEGiusto() throws Exception {
        LaFalceDelTristoMietitore falce = registra(new LaFalceDelTristoMietitore(), "FALCE");
        LaLeggendaDelLocandiere locandiere = registra(new LaLeggendaDelLocandiere(), "LOCANDIERE");
        LaBenedizione benedizione = registra(new LaBenedizione(), "BENEDIZIONE");
        falce.aggiungiProprieta("POSTO", "3,3");
        locandiere.aggiungiProprieta("POSTO", "3,3");
        benedizione.aggiungiProprieta("LOCANDA", "3_3");
        Foresta.setPosizioneDelGruppo(new CoordinateMD(3, 3));
        assertTrue((Boolean) invoca(falce, "nelPosto"));
        assertTrue((Boolean) invoca(locandiere, "nelPosto"));
        assertTrue((Boolean) invoca(benedizione, "nellaLocanda"));
        datiDellaForesta().setPosizioneDelGruppo(null);
        assertFalse((Boolean) invoca(falce, "nelPosto"));
        assertFalse((Boolean) invoca(locandiere, "nelPosto"));
        assertFalse((Boolean) invoca(benedizione, "nellaLocanda"));
        datiDellaForesta().setPosizioneDelGruppo(new CoordinateMD(0, 0));
    }

    @Test
    void ilSorteggioDegliIncarichiSiSpegneESiRicorda() throws Exception {
        assertTrue(SceltaDegliIncarichi.isSorteggioAttivo());
        SceltaDegliIncarichi.setSorteggioAttivo(false);
        assertFalse(SceltaDegliIncarichi.isSorteggioAttivo());
        IlNegozioDellAlchimista negozio = registra(new IlNegozioDellAlchimista(), "NEGOZIO");
        Method tocca = SceltaDegliIncarichi.class.getDeclaredMethod("tocca", IncaricoInCitta.class);
        tocca.setAccessible(true);
        assertTrue((Boolean) tocca.invoke(null, negozio), "spento il sorteggio tocca sempre al primo pronto");
        SceltaDegliIncarichi.setSorteggioAttivo(true);
        Method visita = SceltaDegliIncarichi.class.getDeclaredMethod("visitaCorrente");
        visita.setAccessible(true);
        assertTrue(((String) visita.invoke(null)).endsWith("@" + Foresta.getPosizioneDelGruppo().getX() + "," + Foresta.getPosizioneDelGruppo().getY()));
        datiDellaForesta().setPosizioneDelGruppo(null);
        assertTrue(((String) visita.invoke(null)).endsWith("@"));
        datiDellaForesta().setPosizioneDelGruppo(new CoordinateMD(0, 0));
        assertNotNull(tocca.invoke(null, negozio));
        SceltaDegliIncarichi.dimentica();
    }

    // ---- Le rovine del negozio

    private static void soloBoschiNelQuadrante(Quadrante quadrante) {
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                CoordinateMD c = new CoordinateMD(x, y);
                TipoLocazione tipo = Foresta.getLocazione(c);
                if (tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA || tipo.getCategoriaLocazione() == CategoriaLocazione.CASTELLO) {
                    continue;
                }
                Foresta.impostaLocazione(c, quadrante.contiene(c) ? TipoLocazione.BOSCO : TipoLocazione.RADURA);
            }
        }
    }

    @Test
    void leRovineDelNegozioSiCostruisconoAltroveSeIlQuadranteNonHaPosto() throws Exception {
        for (Quadrante quadrante : Quadrante.values()) {
            MondoDiProva.nuovo(100 + quadrante.ordinal());
            RegistroMissioni.reimposta();
            soloBoschiNelQuadrante(quadrante);
            IlNegozioDellAlchimista negozio = registra(new IlNegozioDellAlchimista(), "NEGOZIO_" + quadrante);
            negozio.aggiungiProprieta("CITTA", TipoLocazione.CITTA_NYENA.name());
            invoca(negozio, "trovaLeRovine");
            assertNotNull(negozio.getCovo(), quadrante.name());
        }
    }

    @Test
    void senzaUnPostoPerLeRovineIlNegozioNonPuoOffrirsi() throws Exception {
        soloBoschiNelQuadrante(Quadrante.values()[0]);
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.BOSCO) {
                    Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.RADURA);
                }
            }
        }
        IlNegozioDellAlchimista negozio = registra(new IlNegozioDellAlchimista(), "NEGOZIO");
        negozio.aggiungiProprieta("CITTA", TipoLocazione.CITTA_NYENA.name());
        negozio.aggiungiProprieta("CITTA_ASSEGNATA", TipoLocazione.CITTA_NYENA.name());
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        assertFalse((Boolean) invoca(negozio, "isPossibileQui"), "nessuna rovina e nessun posto dove costruirla");
        assertThrows(IllegalStateException.class, () -> invoca(negozio, "trovaLeRovine"));
        // Con le rovine già scelte (il covo) il posto non si cerca più
        negozio.aggiungiProprieta("LOCAZIONE_OCCUPATA", "5,5,1");
        assertTrue((Boolean) invoca(negozio, "isPossibileQui"));
    }

    // ---- Il rituale e il colpo

    @Test
    void ilRitualeHaIlSuoCapoSoloSeLaRigaLoDice() {
        String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("RITUALE");
        while (!riga.contains("NEMICO=") || riga.contains("CAPO=")) {
            riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("RITUALE");
        }
        IlRituale senza = registra(new IlRituale(), "SENZA_CAPO");
        senza.aggiungiProprieta("PARAMETRO_RITUALE", riga);
        assertEquals("", senza.getCapo());
        assertNotNull(senza.getNemici());
        IlRituale con = registra(new IlRituale(), "CON_CAPO");
        con.aggiungiProprieta("PARAMETRO_RITUALE", riga + ";CAPO=SI");
        assertFalse(con.getCapo().isEmpty());
        assertNotNull(con.getNemici());
    }

    @Test
    void ilRitualeRinviatoRiparteSoloAllontanandosiDalPosto() throws Exception {
        IlRituale rituale = registra(new IlRituale(), "RITUALE");
        rituale.getRituale();
        rituale.aggiungiProprieta("POSTO", "4,4");
        Foresta.setPosizioneDelGruppo(new CoordinateMD(4, 4));
        MissioneAPassi comePassi = rituale;
        assertFalse(comePassi.costruisciPasso("RINVIO").isConcluso());
        Foresta.setPosizioneDelGruppo(new CoordinateMD(7, 7));
        assertTrue(comePassi.costruisciPasso("RINVIO").isConcluso());
    }

    @Test
    void leGuardieDelColpoSiNominanoUnaAUnaOInGruppo() throws Exception {
        String riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("COLPO");
        while (riga.contains("CAPO=")) {
            riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("COLPO");
        }
        // Nella grammatica le guardie sono almeno due: una sola, con o senza il capo, si prova con le righe cambiate a mano
        for (String variante : new String[] {riga, riga + ";CAPO=SI"}) {
            for (String numero : new String[] {"1", "3"}) {
                IlColpo missione = registra(new IlColpo(), "COLPO_" + variante.length() + "_" + numero);
                missione.aggiungiProprieta("PARAMETRO_COLPO", variante.replaceAll("NUMERO=\\d+", "NUMERO=" + numero));
                assertFalse(((String) invoca(missione, "getNomeDelleGuardie")).isEmpty());
            }
        }
        assertEquals(1, ColpoRichiesto.da(riga.replaceAll("NUMERO=\\d+", "NUMERO=1")).getNumero());
    }

    @Test
    void ilColpoSenzaPostoNonSiConclude() throws Exception {
        IlColpo colpo = registra(new IlColpo(), "COLPO");
        colpo.getColpo();
        MissioneAPassi comePassi = colpo;
        assertFalse(comePassi.costruisciPasso("COLPO").isConcluso());
    }

    // ---- L'indagine e l'oggetto smarrito

    @Test
    void l_indagineNonHaIndiziPrimaDiOffrirsi() {
        LIndagine indagine = registra(new LIndagine(), "INDAGINE");
        assertEquals(0, indagine.getIndiziTrovati());
        indagine.getIndagine();
        assertEquals(0, indagine.getIndiziTrovati(), "è scelta ma non ancora offerta");
        indagine.attivaMissione();
        assertEquals(indagine.getIndagine().getIndizi().size(), indagine.getIndiziTrovati());
        for (int n = 1; n <= 2; n++) {
            indagine.aggiungiProprieta("PASSO_CORRENTE", "TRACCIA_" + n);
            assertEquals(n - 1, indagine.getIndiziTrovati());
            indagine.aggiungiProprieta("PASSO_CORRENTE", "INDIZIO_" + n);
            assertEquals(n - 1, indagine.getIndiziTrovati());
        }
    }

    @Test
    void l_oggettoSmarritoSenzaPostiLoCercaInCittaESenzaNascondigli() throws Exception {
        // Una mappa di sole radure, tranne le caselle intorno alla città, che non sono nascondigli: l'oggetto sta nella città stessa
        for (int x = 0; x < MondoDiProva.LATO; x++) {
            for (int y = 0; y < MondoDiProva.LATO; y++) {
                TipoLocazione tipo = Foresta.getLocazione(x, y);
                if (tipo.getCategoriaLocazione() != CategoriaLocazione.CITTA && tipo.getCategoriaLocazione() != CategoriaLocazione.CASTELLO) {
                    Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.LOCANDA);
                }
            }
        }
        LOggettoSmarrito smarrito = registra(new LOggettoSmarrito(), "SMARRITO");
        smarrito.aggiungiProprieta("CITTA", TipoLocazione.CITTA_NYENA.name());
        smarrito.getSmarrito();
        invoca(smarrito, "scegliIlPosto");
        assertEquals(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA), smarrito.getPosto());
    }

    @Test
    void laLealtaEsclude_ilCapoEIPersonaggiATempo() {
        LaLealta lealta = registra(new LaLealta(), "LEALTA");
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio temporaneo = gruppo.getPersonaggiVivi().get(2);
        temporaneo.getModelloDati().setMotivoNelGruppo(com.threeamigos.foresta.tipi.TipoMotivoNelGruppo.AIUTO_GRATUITO);
        lealta.controllaPreLocazione();
        lealta.controllaInLocazione();
        temporaneo.getModelloDati().setMotivoNelGruppo(com.threeamigos.foresta.tipi.TipoMotivoNelGruppo.PERMANENTE);
        assertEquals(MissionePredefinita.LA_LEALTA, lealta.getClasse());
    }
}
