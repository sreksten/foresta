package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Date;

/**
 *
 * @author Stefano Reksten
 */
public class SnifferBusEventi {

    public SnifferBusEventi() {
        BusEventi.iscriviti(EventoAggiuntaModificatore.class, this::onEventoAggiuntaModificatore);
        BusEventi.iscriviti(EventoApprovazioneAcquistoArtefatto.class, this::onEventoApprovazioneAcquisto);
        BusEventi.iscriviti(EventoApprovazionePrelievoArtefatto.class, this::onEventoApprovazionePrelievo);
        // EventoApprovazioneSpostamento è classe astratta
        BusEventi.iscriviti(EventoApprovazioneStoccaggioArtefatto.class, this::onEventoApprovazioneStoccaggio);
        BusEventi.iscriviti(EventoApprovazioneVenditaArtefatto.class, this::onEventoApprovazioneVendita);
        BusEventi.iscriviti(EventoAumentoLivelloMondo.class, this::onEventoAumentoLivelloMondo);
        BusEventi.iscriviti(EventoAumentoLivelloPersonaggio.class, this::onEventoAumentoLivelloPersonaggio);
        // EventoBase è classe astratta
        BusEventi.iscriviti(EventoComandiDisponibili.class, this::onEventoComandiDisponibili);
        BusEventi.iscriviti(EventoComandoDiGioco.class, this::onEventoComandoDiGioco);
        BusEventi.iscriviti(EventoCombattimento.class, this::onEventoCombattimento);
        BusEventi.iscriviti(EventoConsumoPuntoAbilita.class, this::onEventoConsumoPuntoAbilita);
        BusEventi.iscriviti(EventoCreazionePersonaggio.class, this::onEventoCreazionePersonaggio);
        BusEventi.iscriviti(EventoCreazioneSpriteAnnuncioGlobale.class, this::onEventoCreazioneSpriteAnnuncioGlobale);
        BusEventi.iscriviti(EventoCreazioneSpriteATempo.class, this::onEventoCreazioneSpriteATempo);
        BusEventi.iscriviti(EventoCreazioneSpriteEffetto.class, this::onEventoCreazioneSpriteEffetto);
        BusEventi.iscriviti(EventoCreazioneSpriteFumetto.class, this::onEventoCreazioneSpriteFumetto);
        BusEventi.iscriviti(EventoCreazioneSpriteInDissolvenza.class, this::onEventoCreazioneSpriteInDissolvenza);
        BusEventi.iscriviti(EventoErroreInterno.class, this::onEventoErroreInterno);
        BusEventi.iscriviti(EventoException.class, this::onEventoException);
        BusEventi.iscriviti(EventoFumetto.class, this::onEventoFumetto);
        BusEventi.iscriviti(EventoInterazioneElementale.class, this::onEventoInterazioneElementale);
        BusEventi.iscriviti(EventoInterfacciaUtentePronta.class, this::onEventoInterfacciaUtentePronta);
        BusEventi.iscriviti(EventoMessaggio.class, this::onEventoMessaggio);
        BusEventi.iscriviti(EventoMessaggioInterno.class, this::onEventoMessaggioInterno);
        BusEventi.iscriviti(EventoNotificaGlobale.class, this::onEventoNotificaGlobale);
        BusEventi.iscriviti(EventoParagrafo.class, this::onEventoParagrafo);
        // EventoPersonaggio è classe astratta
        BusEventi.iscriviti(EventoPuliziaCacheDinamicaImmagini.class, this::onEventoPuliziaCacheDinamicaImmagini);
        BusEventi.iscriviti(EventoRichiestaAcquistoArtefatto.class, this::onEventoRichiestaAcquisto);
        BusEventi.iscriviti(EventoRichiestaPrelievoArtefatto.class, this::onEventoRichiestaPrelievo);
        // EventoRichiestaSpostamento è classe astratta
        BusEventi.iscriviti(EventoRichiestaStoccaggioArtefatto.class, this::onEventoRichiestaStoccaggio);
        BusEventi.iscriviti(EventoRichiestaTesto.class, this::onEventoRichiestaTesto);
        BusEventi.iscriviti(EventoRichiestaVenditaArtefatto.class, this::onEventoRichiestaVendita);
        BusEventi.iscriviti(EventoRifiutoAcquistoArtefatto.class, this::onEventoRifiutoAcquisto);
        BusEventi.iscriviti(EventoRifiutoPrelievoArtefatto.class, this::onEventoRifiutoPrelievo);
        // EventoRifiutoSpostamento è classe astratta
        BusEventi.iscriviti(EventoRifiutoStoccaggioArtefatto.class, this::onEventoRifiutoStoccaggio);
        BusEventi.iscriviti(EventoRifiutoVenditaArtefatto.class, this::onEventoRifiutoVendita);
        BusEventi.iscriviti(EventoStatoDiGioco.class, this::onEventoStatoDiGioco);
        BusEventi.iscriviti(EventoTestoDisponibile.class, this::onEventoTestoDisponibile);
        BusEventi.iscriviti(EventoValutazioneAttaccante.class, this::onEventoValutazioneAttaccante);
        BusEventi.iscriviti(EventoVariazioneEffettoDiStato.class, this::onEventoVariazioneEffettoDiStato);
        BusEventi.iscriviti(EventoVariazioneGemme.class, this::onEventoVariazioneGemme);
        BusEventi.iscriviti(EventoVariazioneIncantesimi.class, this::onEventoVariazioneIncantesimi);
        BusEventi.iscriviti(EventoVariazioneMappa.class, this::onEventoVariazioneMappa);
        BusEventi.iscriviti(EventoVariazioneMonete.class, this::onEventoVariazioneMonete);
        BusEventi.iscriviti(EventoVariazionePozioniMagia.class, this::onEventoVariazionePozioniMagia);
        BusEventi.iscriviti(EventoVariazionePozioniMagiaGrandi.class, this::onEventoVariazionePozioniMagiaGrandi);
        BusEventi.iscriviti(EventoVariazionePozioniSalute.class, this::onEventoVariazionePozioniSalute);
        BusEventi.iscriviti(EventoVariazionePozioniSaluteGrandi.class, this::onEventoVariazionePozioniSaluteGrandi);
        BusEventi.iscriviti(EventoVariazionePozioniMagia.class, this::onEventoVariazionePozioniMagia);
        BusEventi.iscriviti(EventoVariazionePunti.class, this::onEventoVariazionePunti);
        BusEventi.iscriviti(EventoVariazionePuntiEsperienza.class, this::onEventoVariazionePuntiEsperienza);
        BusEventi.iscriviti(EventoVariazioneStatistichePersonaggio.class, this::onEventoVariazioneStatistichePersonaggio);
        BusEventi.iscriviti(EventoVariazioneStatoVitalePersonaggio.class, this::onEventoVariazioneStatoVitalePersonaggio);
    }

    private void onEventoAggiuntaModificatore(EventoAggiuntaModificatore evento) {
        ModificatoreAttributo modificatore = evento.getModificatore();
        Logger.log(headerEvento(evento) + formattaModificatoreAttributo(modificatore) +
                formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoApprovazioneAcquisto(EventoApprovazioneAcquistoArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamentoArtefatto();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " acquista "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazionePrelievo(EventoApprovazionePrelievoArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamentoArtefatto();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " preleva "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazioneStoccaggio(EventoApprovazioneStoccaggioArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamentoArtefatto();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " stocca "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazioneVendita(EventoApprovazioneVenditaArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamentoArtefatto();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " vende "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoAumentoLivelloMondo(EventoAumentoLivelloMondo evento) {
        Logger.log(headerEvento(evento) + "Mondo aumenta di livello a " + evento.getLivello());
    }

    private void onEventoAumentoLivelloPersonaggio(EventoAumentoLivelloPersonaggio evento) {
        Logger.log(headerEvento(evento) + "Aumento livello " + evento.getLivelloPrecedente() + " -> " +
                evento.getLivelloAttuale() + " - " + formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoComandiDisponibili(EventoComandiDisponibili evento) {
        Logger.log(headerEvento(evento) + "Comandi disponibili: " + evento.getComandiDisponibili());
    }

    private void onEventoComandoDiGioco(EventoComandoDiGioco evento) {
        Comando comando = evento.getComando();
        Logger.log(headerEvento(evento) + "Comando: " + comando);
    }

    private void onEventoCombattimento(EventoCombattimento evento) {
        Personaggio p = evento.getPersonaggio();
        Personaggio bersaglio = evento.getBersaglio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) +
                formattaStatistichePersonaggio(bersaglio) + evento.formattaRisultatoCombattimento());
    }

    private void onEventoConsumoPuntoAbilita(EventoConsumoPuntoAbilita evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoAttributo());
    }

    private void onEventoCreazionePersonaggio(EventoCreazionePersonaggio evento) {
        Personaggio p  = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p));
    }

    private void onEventoCreazioneSpriteAnnuncioGlobale(EventoCreazioneSpriteAnnuncioGlobale evento) {
        Logger.log(headerEvento(evento) + "Titolo: " + evento.getSprite().getTitolo() +
                ", Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoCreazioneSpriteATempo(EventoCreazioneSpriteATempo evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoCreazioneSpriteEffetto(EventoCreazioneSpriteEffetto evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getTesto());
    }

    private void onEventoCreazioneSpriteFumetto(EventoCreazioneSpriteFumetto evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getTesto());
    }

    private void onEventoCreazioneSpriteInDissolvenza(EventoCreazioneSpriteInDissolvenza evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoErroreInterno(EventoErroreInterno evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoException(EventoException evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getException().getMessage()));
        Logger.log(evento.getException());
    }

    private void onEventoFumetto(EventoFumetto evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getTesto()));
    }

    private void onEventoInterazioneElementale(EventoInterazioneElementale evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoInterazioneElementale());
    }

    private void onEventoInterfacciaUtentePronta(EventoInterfacciaUtentePronta evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMessaggio(EventoMessaggio evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoMessaggioInterno(EventoMessaggioInterno evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggioInterno()));
    }

    private void onEventoNotificaGlobale(EventoNotificaGlobale evento) {
        Logger.log(String.format("%s - %s - %s - %s - %s", new Date(), evento.getTipoEvento(), evento.getEtichetta(), evento.getEtichetta(), evento.getMessaggio()));
    }

    private void onEventoParagrafo(EventoParagrafo evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoPuliziaCacheDinamicaImmagini(EventoPuliziaCacheDinamicaImmagini evento) {
        int prima = evento.getElementiPrima();
        int dopo = evento.getElementiDopo();
        Logger.log(headerEvento(evento) + "Eliminate " + (prima - dopo) + " immagini dalla cache. Rimanenti: " + dopo);
    }

    private void onEventoRichiestaAcquisto(EventoRichiestaAcquistoArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaPrelievo(EventoRichiestaPrelievoArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaStoccaggio(EventoRichiestaStoccaggioArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaTesto(EventoRichiestaTesto evento) {
        Logger.log(headerEvento(evento) + "Richiesta: " + evento.getRichiesta());
    }

    private void onEventoRichiestaVendita(EventoRichiestaVenditaArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRifiutoAcquisto(EventoRifiutoAcquistoArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(richiesta.getParteRemota()) + ": fondi insufficienti");
    }

    private void onEventoRifiutoPrelievo(EventoRifiutoPrelievoArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(richiesta.getParteRemota()) + ": carico eccessivo");
    }

    private void onEventoRifiutoStoccaggio(EventoRifiutoStoccaggioArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoRifiutoVendita(EventoRifiutoVenditaArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoStatoDiGioco(EventoStatoDiGioco evento) {
        Logger.log(headerEvento(evento) + evento.getStato().toString());
    }

    private void onEventoTestoDisponibile(EventoTestoDisponibile evento) {
        Logger.log(headerEvento(evento) + "Ricevuto testo: " + evento.getTesto());
    }

    private void onEventoValutazioneAttaccante(EventoValutazioneAttaccante evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getRisultatoValutazione());
    }

    private void onEventoVariazioneEffettoDiStato(EventoVariazioneEffettoDiStato evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Tipo: %s, EffettoDiStato: %s - ",
                evento.getTipo(), evento.getEffetto()) + formattaStatistichePersonaggio(p));
    }

    public void onEventoVariazioneGemme(EventoVariazioneGemme evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazioneIncantesimi(EventoVariazioneIncantesimi evento) {
        Logger.log(headerEvento(evento) + String.format("Tipo: %s, Variazione: %d -> %d",
                evento.getClasseIncantesimo(), evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazioneMappa(EventoVariazioneMappa evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: (%d, %d) -> (%d, %d)",
                evento.getDaX(), evento.getDaY(), evento.getaX(), evento.getaY()));
    }

    public void onEventoVariazioneMonete(EventoVariazioneMonete evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniMagia(EventoVariazionePozioniMagia evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniMagiaGrandi(EventoVariazionePozioniMagiaGrandi evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniSalute(EventoVariazionePozioniSalute evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniSaluteGrandi(EventoVariazionePozioniSaluteGrandi evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePunti(EventoVariazionePunti evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePuntiEsperienza(EventoVariazionePuntiEsperienza evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    private void onEventoVariazioneStatistichePersonaggio(EventoVariazioneStatistichePersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Attributo: %s, Variazione: %7.2f -> %7.2f - ",
                evento.getTipoAttributo(), evento.getValorePrecedente(), evento.getNuovoValore()) +
                formattaStatistichePersonaggio(p));
    }

    private void onEventoVariazioneStatoVitalePersonaggio(EventoVariazioneStatoVitalePersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Stato: -> %s, Causa trapasso: -> %s - ",
                p.isVivo() ? "Vivo" : "Morto", p.getCausaTrapasso()) + formattaStatistichePersonaggio(p));
    }

    //--- Metodi generali di utilità

    private String formattaStatistichePersonaggio(Personaggio p) {
        return String.format("UUID: %s,Nome: %17s, Livello: %2d, Salute: %3d/%3d; Magia: %3d/%3d; Forza: %3d; Destrezza: %3d; Costituzione: %3d; Intelligenza: %3d; Saggezza: %3d; Carisma: %3d; Fortuna: %3d",
                p.getModelloDati().getUuid(), p.getNomeSingolare(), p.getLivello(), p.getSalute(), p.getSaluteMassima(), p.getMagia(), p.getMagiaMassima(),
                p.getForza(), p.getDestrezza(), p.getCostituzione(), p.getIntelligenza(), p.getSaggezza(), p.getCarisma(), p.getFortuna());
    }

    private String formattaModificatoreAttributo(ModificatoreAttributo modificatore) {
        return String.format("TipoAttributo: %s, TipoModificatore: %s, Quantità: %5f, Note: %s - ",
                modificatore.getTipoModificatoreAttributo(), modificatore.getTipoAttributo(), modificatore.getQuantita(), modificatore.getNote());
    }

    private String headerEvento(EventoBase evento) {
        return String.format("%s - %s - ", new Date(), evento.getTipoEvento());
    }

    private String formattaParte(ScambiatoreArtefatti parte) {
        if (parte instanceof Personaggio) {
            return ((Personaggio) parte).getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
        }
        if (parte instanceof GruppoGiocatore) {
            return "il gruppo";
        }
        return parte.getClass().getSimpleName();
    }

    private String nomeOggetto(Object oggetto) {
        return oggetto instanceof Artefatto ? ((Artefatto) oggetto).getNome() : String.valueOf(oggetto);
    }

}
