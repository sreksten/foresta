package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.comandigiocatore.*;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.eventi.richieste.*;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import java.util.Date;
import java.util.stream.Collectors;

/**
 *
 * @author Stefano Reksten
 */
public class SnifferBusEventi {

    public SnifferBusEventi() {
        BusEventi.iscriviti(NotificaAggiornamentoStatoMissione.class, this::onEventoAggiornamentoStatoMissione);
        BusEventi.iscriviti(NotificaAggiuntaModificatorePersonaggio.class, this::onEventoAggiuntaModificatore);
        BusEventi.iscriviti(NotificaApprovazioneAcquistoArtefatto.class, this::onEventoApprovazioneAcquistoArtefatto);
        BusEventi.iscriviti(NotificaApprovazioneAcquistoConsumabile.class, this::onEventoApprovazioneAcquistoConsumabile);
        BusEventi.iscriviti(NotificaApprovazionePrelievoArtefatto.class, this::onEventoApprovazionePrelievoArtefatto);
        // EventoApprovazioneSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(NotificaApprovazioneStoccaggioArtefatto.class, this::onEventoApprovazioneStoccaggioArtefatto);
        BusEventi.iscriviti(NotificaApprovazioneVenditaArtefatto.class, this::onEventoApprovazioneVenditaArtefatto);
        BusEventi.iscriviti(NotificaAumentoLivelloMondo.class, this::onEventoAumentoLivelloMondo);
        BusEventi.iscriviti(NotificaAumentoLivelloPersonaggio.class, this::onEventoAumentoLivelloPersonaggio);
        // EventoBase è classe astratta
        BusEventi.iscriviti(InternoAggiornamentoComandiDisponibili.class, this::onEventoComandiDisponibili);
        BusEventi.iscriviti(ComandoDiGioco.class, this::onEventoComandoDiGioco);
        BusEventi.iscriviti(NotificaInizioCombattimentoPersonaggio.class, this::onEventoCombattimento);
        BusEventi.iscriviti(NotificaConsumoPuntoAbilitaPersonaggio.class, this::onEventoConsumoPuntoAbilita);
        BusEventi.iscriviti(InternoCreazionePersonaggio.class, this::onEventoCreazionePersonaggio);
        BusEventi.iscriviti(InternoCreazioneSpriteAnnuncioGlobale.class, this::onEventoCreazioneSpriteAnnuncioGlobale);
        BusEventi.iscriviti(InternoCreazioneSpriteATempo.class, this::onEventoCreazioneSpriteATempo);
        BusEventi.iscriviti(InternoCreazioneSpriteEffettoDiStato.class, this::onEventoCreazioneSpriteEffetto);
        BusEventi.iscriviti(InternoCreazioneSpriteFumettoATempo.class, this::onEventoCreazioneSpriteFumetto);
        BusEventi.iscriviti(InternoCreazioneSpriteInDissolvenza.class, this::onEventoCreazioneSpriteInDissolvenza);
        BusEventi.iscriviti(NotificaErroreCaricamento.class, this::onEventoErroreCaricamento);
        BusEventi.iscriviti(InternoErrore.class, this::onEventoErroreInterno);
        BusEventi.iscriviti(InternoException.class, this::onEventoException);
        BusEventi.iscriviti(NotificaFineGioco.class, this::onEventoFineGioco);
        BusEventi.iscriviti(InternoNotificaViaFumettoATempo.class, this::onEventoFumetto);
        BusEventi.iscriviti(NotificaInterazioneElementalePersonaggio.class, this::onEventoInterazioneElementale);
        BusEventi.iscriviti(InternoInterfacciaUtentePronta.class, this::onEventoInterfacciaUtentePronta);
        BusEventi.iscriviti(ComandoInvioTesto.class, this::onEventoInvioTesto);
        BusEventi.iscriviti(NotificaTestoFrase.class, this::onEventoMessaggio);
        BusEventi.iscriviti(InternoMessaggio.class, this::onEventoMessaggioInterno);
        BusEventi.iscriviti(InternoPortaInPrimoPiano.class, this::onEventoMostraFinestra);
        BusEventi.iscriviti(NotificaMostraPunteggiMigliori.class, this::onEventoMostraPunteggi);
        BusEventi.iscriviti(InternoMostraSchermataGioco.class, this::onEventoMostraSchermataGioco);
        BusEventi.iscriviti(NotificaMostraStatisticheFineGioco.class, this::onEventoMostraStatistiche);
        BusEventi.iscriviti(NotificaGlobale.class, this::onEventoNotificaGlobale);
        BusEventi.iscriviti(NotificaTestoParagrafo.class, this::onEventoParagrafo);
        // EventoPersonaggio è classe astratta
        BusEventi.iscriviti(InternoPreparazioneLocazione.class, this::onEventoPreparazioneLocazione);
        BusEventi.iscriviti(InternoPuliziaCacheDinamicaImmagini.class, this::onEventoPuliziaCacheDinamicaImmagini);
        BusEventi.iscriviti(NotificaRaccoltaOggetti.class, this::onEventoRaccoltaOggetti);
        BusEventi.iscriviti(ComandoAcquistoArtefatto.class, this::onEventoRichiestaAcquistoArtefatto);
        BusEventi.iscriviti(ComandoAcquistoConsumabile.class, this::onEventoRichiestaAcquistoConsumabile);
        BusEventi.iscriviti(InternoRichiestaAperturaFinestraCombattimento.class, this::onEventoRichiestaAperturaFinestraCombattimento);
        BusEventi.iscriviti(ComandoAperturaInventarioCommerciante.class, this::onEventoRichiestaAperturaInventarioCommerciante);
        BusEventi.iscriviti(ComandoAperturaInventarioFornitore.class, this::onEventoRichiestaAperturaInventarioFornitore);
        BusEventi.iscriviti(ComandoAperturaInventarioGruppo.class, this::onEventoRichiestaAperturaInventarioGruppo);
        BusEventi.iscriviti(InternoRichiestaChiusuraFinestraCombattimento.class, this::onEventoRichiestaChiusuraFinestraCombattimento);
        BusEventi.iscriviti(ComandoPrelievoArtefatto.class, this::onEventoRichiestaPrelievoArtefatto);
        BusEventi.iscriviti(InternoRichiestaRefreshUI.class, this::onEventoRichiestaRefreshUI);
        BusEventi.iscriviti(InternoRichiestaReinizializzazioneUI.class, this::onEventoRichiestaReinizializzazioneUI);
        // EventoRichiestaSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(RichiestaSelezioneSlotPerRilettura.class, this::onEventoRichiestaSelezioneSlotPerRilettura);
        BusEventi.iscriviti(RichiestaSelezioneSlotPerSalvataggio.class, this::onEventoRichiestaSelezioneSlotPerSalvataggio);
        BusEventi.iscriviti(ComandoStoccaggioArtefatto.class, this::onEventoRichiestaStoccaggioArtefatto);
        BusEventi.iscriviti(RichiestaTesto.class, this::onEventoRichiestaTesto);
        BusEventi.iscriviti(ComandoVenditaArtefatto.class, this::onEventoRichiestaVenditaArtefatto);
        BusEventi.iscriviti(ComandoVenditaArtefatto.class, this::onEventoRichiestaVenditaArtefatto);
        BusEventi.iscriviti(ComandoVisualizzazioneMappa.class, this::onEventoRichiestaVisualizzazioneMappa);
        BusEventi.iscriviti(NotificaRifiutoAcquistoArtefatto.class, this::onEventoRifiutoAcquistoArtefatto);
        BusEventi.iscriviti(NotificaRifiutoAcquistoConsumabile.class, this::onEventoRifiutoAcquistoConsumabile);
        BusEventi.iscriviti(NotificaRifiutoPrelievoArtefatto.class, this::onEventoRifiutoPrelievoArtefatto);
        // EventoRifiutoSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(NotificaRifiutoStoccaggioArtefatto.class, this::onEventoRifiutoStoccaggioArtefatto);
        BusEventi.iscriviti(NotificaRifiutoVenditaArtefatto.class, this::onEventoRifiutoVenditaArtefatto);
        BusEventi.iscriviti(RichiestaUscitaDalGioco.class, this::onEventoSelezioneConfermaUscita);
        BusEventi.iscriviti(RichiestaSelezioneDirezione.class, this::onEventoSelezioneDirezione);
        BusEventi.iscriviti(RichiestaSelezioneIncantesimoDaLanciare.class, this::onEventoSelezioneIncantesimoDaLanciare);
        BusEventi.iscriviti(RichiestaSelezioneSiNo.class, this::onEventoSelezioneSiNo);
        BusEventi.iscriviti(InternoStatoDiGioco.class, this::onEventoStatoDiGioco);
        BusEventi.iscriviti(InternoRisultatoValutazionePersonaggioAttaccante.class, this::onEventoValutazioneAttaccante);
        BusEventi.iscriviti(NotificaVariazioneEffettoDiStatoPersonaggio.class, this::onEventoVariazioneEffettoDiStato);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaGemme.class, this::onEventoVariazioneGemme);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaIncantesimi.class, this::onEventoVariazioneIncantesimi);
        BusEventi.iscriviti(NotificaVariazioneConoscenzaMappa.class, this::onEventoVariazioneMappa);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaMonete.class, this::onEventoVariazioneMonete);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniMagia.class, this::onEventoVariazionePozioniMagia);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniMagiaGrandi.class, this::onEventoVariazionePozioniMagiaGrandi);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSalute.class, this::onEventoVariazionePozioniSalute);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSaluteGrandi.class, this::onEventoVariazionePozioniSaluteGrandi);
        BusEventi.iscriviti(NotificaVariazionePunteggio.class, this::onEventoVariazionePunti);
        BusEventi.iscriviti(NotificaVariazionePuntiEsperienzaPersonaggio.class, this::onEventoVariazionePuntiEsperienza);
        BusEventi.iscriviti(NotificaVariazioneStatistichePersonaggio.class, this::onEventoVariazioneStatistichePersonaggio);
        BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, this::onEventoVariazioneStatoVitalePersonaggio);
    }

    private void onEventoAggiornamentoStatoMissione(NotificaAggiornamentoStatoMissione evento) {
        Logger.log(headerEvento(evento) + "Missione " + evento.getMissione().getNome() + " - " +
                evento.getEtichetta() + " - " + evento.getDescrizione());
    }

    private void onEventoAggiuntaModificatore(NotificaAggiuntaModificatorePersonaggio evento) {
        ModificatoreAttributo modificatore = evento.getModificatore();
        Logger.log(headerEvento(evento) + formattaModificatoreAttributo(modificatore) +
                formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoApprovazioneAcquistoArtefatto(NotificaApprovazioneAcquistoArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamentoArtefatto().getUuid());
    }

    private void onEventoApprovazioneAcquistoConsumabile(NotificaApprovazioneAcquistoConsumabile evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaAcquistoConsumabile().getUuid());
    }

    private void onEventoApprovazionePrelievoArtefatto(NotificaApprovazionePrelievoArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamentoArtefatto().getUuid());
    }

    private void onEventoApprovazioneStoccaggioArtefatto(NotificaApprovazioneStoccaggioArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamentoArtefatto().getUuid());
    }

    private void onEventoApprovazioneVenditaArtefatto(NotificaApprovazioneVenditaArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamentoArtefatto().getUuid());
    }

    private void onEventoAumentoLivelloMondo(NotificaAumentoLivelloMondo evento) {
        Logger.log(headerEvento(evento) + "Mondo aumenta di livello a " + evento.getLivello());
    }

    private void onEventoAumentoLivelloPersonaggio(NotificaAumentoLivelloPersonaggio evento) {
        Logger.log(headerEvento(evento) + "Aumento livello " + evento.getLivelloPrecedente() + " -> " +
                evento.getLivelloAttuale() + " - " + formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoComandiDisponibili(InternoAggiornamentoComandiDisponibili evento) {
        Logger.log(headerEvento(evento) + "Comandi disponibili: " + evento.getPossibilita());
    }

    private void onEventoComandoDiGioco(ComandoDiGioco evento) {
        Comando comando = evento.getComando();
        Logger.log(headerEvento(evento) + "Comando: " + comando);
    }

    private void onEventoCombattimento(NotificaInizioCombattimentoPersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Personaggio bersaglio = evento.getBersaglio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) +
                formattaStatistichePersonaggio(bersaglio) + evento.formattaRisultatoCombattimento());
    }

    private void onEventoConsumoPuntoAbilita(NotificaConsumoPuntoAbilitaPersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoAttributo());
    }

    private void onEventoCreazionePersonaggio(InternoCreazionePersonaggio evento) {
        Personaggio p  = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p));
    }

    private void onEventoCreazioneSpriteAnnuncioGlobale(InternoCreazioneSpriteAnnuncioGlobale evento) {
        Logger.log(headerEvento(evento) + "Titolo: " + evento.getSprite().getTitolo() +
                ", Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoCreazioneSpriteATempo(InternoCreazioneSpriteATempo evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoCreazioneSpriteEffetto(InternoCreazioneSpriteEffettoDiStato evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getTesto());
    }

    private void onEventoCreazioneSpriteFumetto(InternoCreazioneSpriteFumettoATempo evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getTesto());
    }

    private void onEventoCreazioneSpriteInDissolvenza(InternoCreazioneSpriteInDissolvenza evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoErroreCaricamento(NotificaErroreCaricamento evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getException().getMessage()));
        Logger.log(evento.getException());
    }

    private void onEventoErroreInterno(InternoErrore evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoException(InternoException evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getException().getMessage()));
        Logger.log(evento.getException());
    }

    private void onEventoFineGioco(NotificaFineGioco evento) {
        Logger.log(String.format("%s - %s - Completato con successo: %s", new Date(), evento.getTipoEvento(), evento.isCompletatoConSuccesso()));
    }

    private void onEventoFumetto(InternoNotificaViaFumettoATempo evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getTesto()));
    }

    private void onEventoInterazioneElementale(NotificaInterazioneElementalePersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoInterazioneElementale());
    }

    private void onEventoInterfacciaUtentePronta(InternoInterfacciaUtentePronta evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoInvioTesto(ComandoInvioTesto evento) {
        Logger.log(headerEvento(evento) + "Ricevuto testo: " + evento.getTesto());
    }

    private void onEventoMessaggio(NotificaTestoFrase evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoMessaggioInterno(InternoMessaggio evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggioInterno()));
    }

    private void onEventoMostraFinestra(InternoPortaInPrimoPiano evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getFinestre()));
    }

    private void onEventoMostraPunteggi(NotificaMostraPunteggiMigliori evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMostraSchermataGioco(InternoMostraSchermataGioco evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMostraStatistiche(NotificaMostraStatisticheFineGioco evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoNotificaGlobale(NotificaGlobale evento) {
        Logger.log(String.format("%s - %s - %s - %s - %s", new Date(), evento.getTipoEvento(), evento.getEtichetta(), evento.getEtichetta(), evento.getMessaggio()));
    }

    private void onEventoParagrafo(NotificaTestoParagrafo evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoPreparazioneLocazione(InternoPreparazioneLocazione evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoPuliziaCacheDinamicaImmagini(InternoPuliziaCacheDinamicaImmagini evento) {
        int prima = evento.getElementiPrima();
        int dopo = evento.getElementiDopo();
        Logger.log(headerEvento(evento) + "Eliminate " + (prima - dopo) + " immagini dalla cache. Rimanenti: " + dopo);
    }

    private void onEventoRaccoltaOggetti(NotificaRaccoltaOggetti evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaAcquistoArtefatto(ComandoAcquistoArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaAcquistoConsumabile(ComandoAcquistoConsumabile evento) {
        ClasseIncantesimo incantesimo = evento.getClasseIncantesimo();
        Logger.log(headerEvento(evento) + "Gruppo richiede di acquistare " + evento.getTipoConsumabile() +
                (incantesimo != null ? (" " + incantesimo) : "") + ", Costo: " + evento.getPrezzo());
    }

    private void onEventoRichiestaAperturaFinestraCombattimento(InternoRichiestaAperturaFinestraCombattimento evento) {
        String notifica = "Attiva - " + formattaStatistichePersonaggio(evento.getPersonaggio()) + " vs " +
                formattaStatistichePersonaggio(evento.getAvversario());
        Logger.log(headerEvento(evento) + notifica);
    }

    private void onEventoRichiestaAperturaInventarioCommerciante(ComandoAperturaInventarioCommerciante evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaAperturaInventarioFornitore(ComandoAperturaInventarioFornitore evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaAperturaInventarioGruppo(ComandoAperturaInventarioGruppo evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaChiusuraFinestraCombattimento(InternoRichiestaChiusuraFinestraCombattimento evento) {
        Logger.log(headerEvento(evento) + "Chiusa");
    }

    private void onEventoRichiestaPrelievoArtefatto(ComandoPrelievoArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaRefreshUI(InternoRichiestaRefreshUI evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaReinizializzazioneUI(InternoRichiestaReinizializzazioneUI evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaSelezioneSlotPerRilettura(RichiestaSelezioneSlotPerRilettura evento) {
        Logger.log(headerEvento(evento) + "Salvataggi disponibili: " + evento.getSalvataggiDisponibili()
                .stream().map(TestataSalvataggio::getId).map(Comando::name).collect(Collectors.joining(", ")));
    }

    private void onEventoRichiestaSelezioneSlotPerSalvataggio(RichiestaSelezioneSlotPerSalvataggio evento) {
        Logger.log(headerEvento(evento) + "Salvataggi disponibili: " + evento.getSalvataggiDisponibili()
                .stream().map(TestataSalvataggio::getId).map(Comando::name).collect(Collectors.joining(", ")));
    }

    private void onEventoRichiestaVisualizzazioneMappa(ComandoVisualizzazioneMappa evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaStoccaggioArtefatto(ComandoStoccaggioArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaTesto(RichiestaTesto evento) {
        Logger.log(headerEvento(evento) + "Richiesta: " + evento.getRichiesta());
    }

    private void onEventoRichiestaVenditaArtefatto(ComandoVenditaArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRifiutoAcquistoArtefatto(NotificaRifiutoAcquistoArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamento().getUuid());
    }

    private void onEventoRifiutoAcquistoConsumabile(NotificaRifiutoAcquistoConsumabile evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaAcquistoConsumabile().getUuid());
    }

    private void onEventoRifiutoPrelievoArtefatto(NotificaRifiutoPrelievoArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamento().getUuid());
    }

    private void onEventoRifiutoStoccaggioArtefatto(NotificaRifiutoStoccaggioArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamento().getUuid());
    }

    private void onEventoRifiutoVenditaArtefatto(NotificaRifiutoVenditaArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamento().getUuid());
    }

    private void onEventoSelezioneConfermaUscita(RichiestaUscitaDalGioco evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoSelezioneDirezione(RichiestaSelezioneDirezione evento) {
        Logger.log(headerEvento(evento) + evento.getPossibilita());
    }

    private void onEventoSelezioneIncantesimoDaLanciare(RichiestaSelezioneIncantesimoDaLanciare evento) {
        Logger.log(headerEvento(evento) + evento.getPossibilita());
    }

    private void onEventoSelezioneSiNo(RichiestaSelezioneSiNo evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoStatoDiGioco(InternoStatoDiGioco evento) {
        Logger.log(headerEvento(evento) + evento.getStato().toString());
    }

    private void onEventoValutazioneAttaccante(InternoRisultatoValutazionePersonaggioAttaccante evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getRisultatoValutazione());
    }

    private void onEventoVariazioneEffettoDiStato(NotificaVariazioneEffettoDiStatoPersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Tipo: %s, EffettoDiStato: %s - ",
                evento.getTipo(), evento.getEffetto()) + formattaStatistichePersonaggio(p));
    }

    public void onEventoVariazioneGemme(NotificaVariazioneDisponibilitaGemme evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazioneIncantesimi(NotificaVariazioneDisponibilitaIncantesimi evento) {
        Logger.log(headerEvento(evento) + String.format("Tipo: %s, Variazione: %d -> %d",
                evento.getClasseIncantesimo(), evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazioneMappa(NotificaVariazioneConoscenzaMappa evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: (%d, %d) -> (%d, %d)",
                evento.getDaX(), evento.getDaY(), evento.getaX(), evento.getaY()));
    }

    public void onEventoVariazioneMonete(NotificaVariazioneDisponibilitaMonete evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniMagia(NotificaVariazioneDisponibilitaPozioniMagia evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniMagiaGrandi(NotificaVariazioneDisponibilitaPozioniMagiaGrandi evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniSalute(NotificaVariazioneDisponibilitaPozioniSalute evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniSaluteGrandi(NotificaVariazioneDisponibilitaPozioniSaluteGrandi evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePunti(NotificaVariazionePunteggio evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePuntiEsperienza(NotificaVariazionePuntiEsperienzaPersonaggio evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    private void onEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Attributo: %s, Variazione: %7.2f -> %7.2f - ",
                evento.getTipoAttributo(), evento.getValorePrecedente(), evento.getNuovoValore()) +
                formattaStatistichePersonaggio(p));
    }

    private void onEventoVariazioneStatoVitalePersonaggio(NotificaVariazioneStatoVitalePersonaggio evento) {
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
