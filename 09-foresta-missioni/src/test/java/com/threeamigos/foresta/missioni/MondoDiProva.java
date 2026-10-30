package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.canti.CantiImparati;
import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.economia.MagazziniDeiNegozi;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.FabbricaIntermezzi;
import com.threeamigos.foresta.intermezzi.IntermezzoEremitaRompiscatole;
import com.threeamigos.foresta.intermezzi.IntermezzoUrloDellaBanshee;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CantiImparatiMD;
import com.threeamigos.foresta.modellodati.CassaMD;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.IntermezziMD;
import com.threeamigos.foresta.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.modellodati.MagazziniMD;
import com.threeamigos.foresta.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.modellodati.RegistroMissioniMD;
import com.threeamigos.foresta.modellodati.RegistroPersonaggiMD;
import com.threeamigos.foresta.modellodati.StatisticheMD;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Chierico;
import com.threeamigos.foresta.personaggi.Druido;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Il mondo delle prove delle missioni: i dati di ogni parte, nuovi a ogni prova, una Foresta di boschi con le quattro città, i cinque
 * castelli e qualche locanda, tempio e bottega, e un gruppo di quattro personaggi nella prima città.
 */
public final class MondoDiProva {

    public static final int LATO = 30;

    public static LineaTemporaleMD lineaTemporale;
    public static StatisticheMD statistiche;
    public static CassaMD cassa;
    public static RegistroMissioniMD missioni;

    private MondoDiProva() {
    }

    public static void nuovo(long seme) {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        Dado.impostaSeme(seme);
        GruppoGiocatoreMD datiGruppo = new GruppoGiocatoreMD();
        GruppoGiocatore.setFornitoreDiDati(() -> datiGruppo);
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        cassa = new CassaMD();
        Cassa.setFornitoreDiDati(() -> cassa);
        MagazziniMD magazzini = new MagazziniMD();
        MagazziniDeiNegozi.setFornitoreDiDati(() -> magazzini);
        statistiche = new StatisticheMD();
        Statistiche.setFornitoreDiDati(() -> statistiche);
        lineaTemporale = new LineaTemporaleMD();
        LineaTemporale.setFornitoreDiDati(() -> lineaTemporale);
        IntermezziMD intermezzi = new IntermezziMD();
        RegistroIntermezzi.setFornitoreDiDati(() -> intermezzi);
        FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_URLO_DELLA_BANSHEE, tipo -> new IntermezzoUrloDellaBanshee());
        FabbricaIntermezzi.registra(TipoIntermezzo.INTERMEZZO_EREMITA_ROMPISCATOLE, tipo -> new IntermezzoEremitaRompiscatole());
        try {
            java.lang.reflect.Field fonti = RegistroIntermezzi.class.getDeclaredField("FONTI");
            fonti.setAccessible(true);
            ((java.util.List<?>) fonti.get(null)).clear();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        RegistroIntermezzi.aggiungiFonte(IntermezziDelleMissioni.ISTANZA);
        CantiImparatiMD canti = new CantiImparatiMD();
        CantiImparati.setFornitoreDiDati(() -> canti);
        RegistroArtefattiMD artefatti = new RegistroArtefattiMD();
        RegistroArtefatti.setFornitoreDiDati(() -> artefatti);
        RegistroPersonaggiMD personaggi = new RegistroPersonaggiMD();
        RegistroPersonaggi.setFornitoreDiDati(() -> personaggi);
        missioni = new RegistroMissioniMD();
        RegistroMissioni.setFornitoreDiDati(() -> missioni);
        RegistroMissioni.setFornitoreLocazioneCompleta(() -> false);
        ForestaMD foresta = new ForestaMD();
        Foresta.setFornitoreDiDati(() -> foresta);
        Foresta.azzera(LATO, LATO);
        for (int x = 0; x < LATO; x++) {
            for (int y = 0; y < LATO; y++) {
                Foresta.impostaLocazione(new CoordinateMD(x, y), TipoLocazione.BOSCO);
            }
        }
        TipoLocazione[] citta = {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA, TipoLocazione.CITTA_FLEENA};
        for (int i = 0; i < citta.length; i++) {
            Foresta.costruisciLocazioneUnica(citta[i], new CoordinateMD(3 + 8 * i, 3 + 4 * (i % 2)), false);
        }
        TipoLocazione[] castelli = {TipoLocazione.CASTELLO_IDRA, TipoLocazione.CASTELLO_MINOTAURO, TipoLocazione.CASTELLO_LICH,
                TipoLocazione.CASTELLO_STREGA, TipoLocazione.CASTELLO_DRAGO};
        for (int i = 0; i < castelli.length; i++) {
            Foresta.costruisciLocazioneUnica(castelli[i], new CoordinateMD(2 + 6 * i, 22 + (i % 3)), false);
        }
        TipoLocazione[] altre = {TipoLocazione.LOCANDA, TipoLocazione.TEMPIO, TipoLocazione.ROVINE, TipoLocazione.GROTTA, TipoLocazione.PALUDE,
                TipoLocazione.RADURA, TipoLocazione.ALCHIMISTA, TipoLocazione.ARMAIOLO, TipoLocazione.VENDITORE_DI_PERGAMENE, TipoLocazione.INCANTATORE};
        for (int i = 0; i < 40; i++) {
            Foresta.impostaLocazione(new CoordinateMD(1 + (i * 7) % 28, 10 + (i * 3) % 10), altre[i % altre.length]);
        }
        // Come la Foresta vera: ogni locanda, e ogni città (che ne ha una), ha il suo nome, la sua recensione e il suo dialogo
        java.util.List<CoordinateMD> conLocanda = new java.util.ArrayList<>();
        for (int x = 0; x < LATO; x++) {
            for (int y = 0; y < LATO; y++) {
                TipoLocazione tipo = Foresta.getLocazione(x, y);
                if (tipo == TipoLocazione.LOCANDA || tipo.getCategoriaLocazione() == com.threeamigos.foresta.tipi.CategoriaLocazione.CITTA) {
                    conLocanda.add(new CoordinateMD(x, y));
                }
            }
        }
        com.threeamigos.foresta.testi.ProduttoriDiTesti.resetProduzioni();
        java.util.List<com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale.DatiLocanda> dati =
                com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale.getDatiLocanda(conLocanda.size());
        for (int i = 0; i < conLocanda.size(); i++) {
            com.threeamigos.foresta.modellodati.LocazioneMD md = Foresta.getLocazioneMD(conLocanda.get(i));
            com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale.DatiLocanda d = dati.get(i);
            if (md.getTipo() == TipoLocazione.LOCANDA) {
                md.setNome(d.getNome());
            } else {
                md.aggiungiProprieta(com.threeamigos.foresta.intermezzi.Locande.LOCANDA_NOME_IN_CITTA, d.getNome());
            }
            md.aggiungiProprieta(com.threeamigos.foresta.intermezzi.Locande.LOCANDA_IDENTIFICATIVO, d.getIdentificativo());
            md.aggiungiProprieta("LOCANDA_NOME_LOCANDIERE", d.getNomeLocandiere());
            md.aggiungiProprieta(com.threeamigos.foresta.intermezzi.Locande.LOCANDA_RECENSIONE, d.getRecensione());
            md.aggiungiProprieta(com.threeamigos.foresta.intermezzi.Locande.LOCANDA_DIALOGO, d.getDialogo());
        }
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        for (Personaggio p : new Personaggio[] {new Guerriero("Arsenio", 5), new Mago("Merlino", 5), new Chierico("Pio", 5), new Ladro("Zorro", 5)}) {
            p.getModelloDati().setPng(false);
            gruppo.aggiungiPersonaggio(p);
        }
        lineaTemporale.setOra(12);
    }

    /**
     * Come salvare la partita e ricaricarla: i dati delle missioni passano per la scrittura e la lettura, e il registro si ricostruisce.
     */
    public static void salvaERicarica() {
        try {
            java.io.StringWriter scritto = new java.io.StringWriter();
            java.io.PrintWriter stream = new java.io.PrintWriter(scritto);
            missioni.salva(stream);
            stream.flush();
            RegistroMissioniMD letto = new RegistroMissioniMD();
            letto.leggi(new java.io.BufferedReader(new java.io.StringReader(scritto.toString())));
            missioni = letto;
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        RegistroMissioni.aggiornaDopoRilettura();
    }

    public static void finito() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(null);
        Dado.ripristina();
    }
}
