package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.artefatti.leggendari.RegoleSetLeggendari;
import com.threeamigos.foresta.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.tipi.GradoIncantamento;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.CategoriaDanno;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.personaggi.ArmaNaturale;
import com.threeamigos.foresta.personaggi.OmbraFiamma;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.*;

/**
 * Le regole del combattimento: se un colpo va a segno, quanto danno fa e quali stati alterati provoca, e le stime che le tattiche usano per
 * scegliere cosa fare.
 * <p>
 * La probabilità di colpire confronta un attacco e una difesa. Per i danni fisici contano la coordinazione di chi attacca (precisione e destrezza)
 * contro la schivata di chi si difende (velocità, destrezza e parata); per quelli magici l'intelligenza e la precisione contro la resistenza
 * magica e la saggezza. Stanchezza e stati alterati (confuso, accecato, rallentato, spaventato) cambiano i due lati, e il risultato ha dei limiti
 * minimo e massimo. Il danno tiene conto dell'arma e degli incantamenti, del livello, dei critici, di debolezze e resistenze della classe, delle
 * interazioni fra tipo di danno e stati (vedi interazioni_effetti_di_stato.md) e dell'equipaggiamento difensivo del bersaglio.
 * <p>
 * Il risultato è un {@link DannoRisultante}, che il personaggio colpito applica a sé con {@code applicaRisultatoCombattimento}.
 *
 * @author Stefano Reksten
 */
public class CalcolatoreCombattimento {


    /**
     * La probabilità, in percentuale, che l'attaccante colpisca il difensore con quel genere di danno (fisico o magico). Alcuni stati decidono da soli
     * l'esito; altrimenti si confronta l'attacco con la difesa, e il risultato resta fra un minimo e un massimo.
     */
    public static int calcolaProbabilitaDiColpire(Personaggio attaccante, Personaggio difensore, CategoriaDanno tipoDanno) {

        // DEBUG: in modalità di prova l'OmbraFiamma va sempre a segno, per poter testare
        // che il gioco sia completabile senza dover contare sulla fortuna dei tiri.
        if (ModalitaDiProva.isAttiva() && attaccante instanceof OmbraFiamma) {
            Logger.log("DEBUG: attaccante OmbraFiamma in modalità di prova, colpisce sempre.");
            return 100;
        }

        // 0. CONTROLLO EFFETTI DI STATO CHE DETERMINANO AUTOMATICAMENTE LA RIUSCITA
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            Logger.log("L'attaccante è STORDITO e non può colpire.");
            return 0;
        }
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            Logger.log("Il difensore è ATTERRATO/CONGELATO/STORDITO e viene colpito automaticamente.");
            return 100;
        }

        double attaccoTotale = 0.0d;
        double difesaTotale = 0.0d;

        // BIVIO LOGICO DI BILANCIAMENTO IN BASE AL TIPO DI DANNO
        if (tipoDanno == CategoriaDanno.FISICO) {
            // --- CALCOLO ASSE FISICO ---
            // Attaccante usa la coordinazione occhio-mano (Precisione + Destrezza)
            attaccoTotale = attaccante.getPrecisione() + attaccante.getDestrezza();
            if (Logger.isAttivo()) {
            	Logger.log(String.format(Locale.ROOT, "[FISICO] Attacco totale (PRECISIONE %d + DESTREZZA %d): %f",
	                    attaccante.getPrecisione(), attaccante.getDestrezza(), attaccoTotale));
            }

            // Il difensore contrasta fisicamente (Velocità + Destrezza) modificata dalla PARATA
            difesaTotale = difensore.getVelocita() + difensore.getDestrezza() + (difensore.getParata() * 0.5d);
            if (Logger.isAttivo()) {
            	Logger.log(String.format(Locale.ROOT, "[FISICO] Difesa totale (VELOCITA %d + DESTREZZA %d + 50%% PARATA %d): %f",
	                    difensore.getVelocita(), difensore.getDestrezza(), difensore.getParata(), difesaTotale));
            }

        } else {
            // --- CALCOLO ASSE MAGICO/ELEMENTALE ---
            // Chi lancia magie si affida al controllo mentale e alla potenza magica (Intelligenza + Precisione)
            attaccoTotale = attaccante.getPrecisione() + attaccante.getIntelligenza();
            if (Logger.isAttivo()) {
            	Logger.log(String.format(Locale.ROOT, "[MAGICO] Attacco totale (PRECISIONE %d + INTELLIGENZA %d): %f",
	                    attaccante.getPrecisione(), attaccante.getIntelligenza(), attaccoTotale));
            }

            // Chi subisce magie si affida al filtro difensivo mistico e alla stabilità mentale (Resistenza Magica + Saggezza)
            difesaTotale = difensore.getResistenzaMagica() + difensore.getSaggezza();
            if (Logger.isAttivo()) {
            	Logger.log(String.format(Locale.ROOT, "[MAGICO] Difesa totale (RESISTENZA_MAGICA %d + SAGGEZZA %d): %f",
	                    difensore.getResistenzaMagica(), difensore.getSaggezza(), difesaTotale));
            }
        }

        // 3. CONTROLLO EFFETTI DI STATO SULL'ATTACCANTE (CONFUSO / ACCECATO / STANCHEZZA)
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)) {
            attaccoTotale = attaccoTotale * (8.0d + Math.min(2, attaccante.getSaggezza() / 20.0d)) / 10.0d;
            if (Logger.isAttivo()) {
            	Logger.log(String.format(Locale.ROOT, "Attacco dopo effetto CONFUSO (mitigato da SAGGEZZA %d): %f", attaccante.getSaggezza(), attaccoTotale));
            }
        }

        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.ACCECATO)) {
            if (tipoDanno == CategoriaDanno.FISICO) {
                // La cecità devasta la mira fisica
                double penalitaAccecato = (attaccoTotale / 2.0d) * (1.0d - Math.min(1.0d, attaccante.getPercezione() / 100.0d));
                attaccoTotale -= penalitaAccecato;
            } else {
                // Per le magie, la cecità influisce meno perché il mago si guida con la Percezione dei flussi magici
                double penalitaAccecatoMagico = (attaccoTotale / 4.0d) * (1.0d - Math.min(1.0d, attaccante.getPercezione() / 100.0d));
                attaccoTotale -= penalitaAccecatoMagico;
            }
            if (Logger.isAttivo()) {
            	Logger.log(String.format(Locale.ROOT, "Attacco dopo effetto ACCECATO (mitigato da PERCEZIONE %d): %f", attaccante.getPercezione(), attaccoTotale));
            }
        }

        // Penalità di STANCHEZZA sull'attaccante
        attaccoTotale -= attaccante.getStanchezza() * 2.0d;
        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "Attacco totale dopo stanchezza (STANCHEZZA %d): %f", attaccante.getStanchezza(), attaccoTotale));
        }


        // 4. APPLICAZIONE DEI MODIFICATORI DI STATO AL DIFENSORE
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.RALLENTATO)) {
            if (tipoDanno == CategoriaDanno.FISICO) {
                difesaTotale = difesaTotale / 2.0d; // Rallentato distrugge la schivata fisica
            } else {
                // Rallentato influisce pochissimo sulla barriera mistica passiva (Resistenza Magica)
                difesaTotale = difesaTotale * 0.9d;
            }
            if (Logger.isAttivo()) {
            	Logger.log("Difesa totale dopo effetto RALLENTATO: " + difesaTotale);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)) {
            // Penalità base del 20%: SAGGEZZA e CORAGGIO (portato ×10 sulla scala dei primari, come per la
            // durata) ne recuperano al massimo metà, così chi è spaventato si difende sempre peggio
            // (moltiplicatore fra 0.8 e 0.9, mai sopra).
            double mitigazioneSpavento = (Math.min(1.0d, difensore.getSaggezza() / 40.0d)
                    + Math.min(1.0d, difensore.getCoraggio() * 10.0d / 40.0d)) / 2.0d;
            difesaTotale = difesaTotale * (0.8d + 0.1d * mitigazioneSpavento);
            if (Logger.isAttivo()) {
            	Logger.log(String.format(Locale.ROOT, "Difesa totale dopo effetto SPAVENTATO (mitigato da SAGGEZZA %d e CORAGGIO %d): %f", difensore.getSaggezza(), difensore.getCoraggio(), difesaTotale));
            }
        }

        // Penalità di STANCHEZZA sul difensore (La fatica logora sia i riflessi fisici che la concentrazione magica)
        difesaTotale -= difensore.getStanchezza() * 2.0d;
        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "Difesa totale dopo stanchezza (STANCHEZZA %d): %f", difensore.getStanchezza(), difesaTotale));
        }

        // 5. CALCOLO DELLA PROBABILITÀ FINALE DI COLPIRE
        int probabilitaFinale = 75 + (int) ((attaccoTotale - difesaTotale) * 2.0d);
        if (Logger.isAttivo()) {
        	Logger.log("Probabilità finale di colpire: " + probabilitaFinale);
        }

        // Limiti minimi e massimi (Cap) per il bilanciamento
        if (probabilitaFinale < 5) {
            probabilitaFinale = 5;
        } else if (probabilitaFinale > 95) {
            probabilitaFinale = 95;
        }
        if (Logger.isAttivo()) {
        	Logger.log("Probabilità finale di colpire dopo applicazione dei limiti: " + probabilitaFinale);
        }
        return probabilitaFinale;
    }

    /**
     * Tira il dado e dice se il colpo va a segno, in base alla {@link #calcolaProbabilitaDiColpire probabilità di colpire}.
     */
    public static boolean colpisce(Personaggio attaccante, Personaggio difensore, CategoriaDanno tipoDanno) {

        int probabilitaFinale = calcolaProbabilitaDiColpire(attaccante, difensore, tipoDanno);

        // 6. TIRO DEL DADO (Generazione numero casuale da 1 a 100)
        int tiroDado = Dado.tira(100);
        Logger.log(() -> "Risultato sul COLPIRE (tiro del dado = " + tiroDado + "): " + (tiroDado <= probabilitaFinale));

        return tiroDado <= probabilitaFinale;
    }

    /**
     * La quota del danno che subisce un bersaglio di un incantesimo (o di un'abilità di danno) a più bersagli, secondo la sua posizione:
     * 0 per il principale, che lo subisce intero, poi un quarto in meno a ogni posizione, fino a un quarto del danno
     * (vedi CostantiIncantesimi.INCANTESIMO_MULTIPLO_RIDUZIONE_PER_POSIZIONE). Lo applicano sia il lancio sia la valutazione delle tattiche, perché
     * scelgano per il danno vero.
     *
     * @param posizione la posizione del bersaglio fra i colpiti, dal principale (0)
     */
    public static double quotaDelDannoPerPosizione(int posizione) {
        return Math.max(CostantiIncantesimi.INCANTESIMO_MULTIPLO_QUOTA_MINIMA,
                1.0d - CostantiIncantesimi.INCANTESIMO_MULTIPLO_RIDUZIONE_PER_POSIZIONE * Math.max(0, posizione));
    }

    /**
     * Il risultato di un colpo pieno con quell'arma: il danno, gli stati alterati e le reazioni che ne seguono.
     */
    public static DannoRisultante calcolaDannoRisultante(Personaggio attaccante, Personaggio difensore, Arma arma) {
        return calcolaDannoRisultante(attaccante, difensore, arma, 1.0d);
    }

    /**
     * Il colpo che una reazione ad area (DannoRisultante.getDanniAdArea) porta a un vicino del bersaglio. Non tira per colpire né
     * per il critico e non guarda l'arma: parte dalla quota del danno grezzo del colpo che l'ha scatenata e passa dalla difesa
     * del vicino contro quel tipo di danno, dalla sua classe (un immune non subisce niente) e dalle reazioni con i suoi stati,
     * di cui contano il danno e gli stati tolti e messi; gli altri effetti di quelle reazioni no, e nemmeno un altro danno ad
     * area: le reazioni ad area non fanno catena. Gli stati nativi del tipo di danno non si tirano.
     */
    public static DannoRisultante calcolaDannoAdArea(Personaggio attaccante, Personaggio vicino, DannoRisultante.DannoAdArea area) {
        TipoDanno tipoDanno = area.getTipoDanno();
        DannoRisultante risultato = new DannoRisultante(attaccante, vicino);
        risultato.setTipoDanno(tipoDanno);
        risultato.setColpoAdArea(true);
        risultato.addReazioneDiClasse(tipoDanno, vicino.getMoltiplicatoreDannoSubito(tipoDanno));
        if (vicino.isImmuneATipoDanno(tipoDanno)) {
            return risultato;
        }
        List<TipoInterazioneConEffettiDiStato> interazioni = interazioniCheScattano(interazioniAttive(vicino, tipoDanno),
                fattoreEffetti(vicino, tipoDanno));
        double difesa = difesaContro(vicino, tipoDanno);
        risultato.setFattoreEquipaggiamento((100.0d + difesaBaseContro(vicino, tipoDanno)) / (100.0d + difesa));
        if (tipoDanno.getCategoriaDanno() != CategoriaDanno.FISICO && vicino.hasEffettoDiStato(TipoEffettoDiStato.MENTE_FRATTURATA)) {
            difesa = difesa * 0.8d;
        }
        difesa = difesa * fattoreDifesaDaStati(vicino, tipoDanno, interazioni);
        for (TipoInterazioneConEffettiDiStato interazione : interazioni) {
            applicaGliStatiDellInterazione(interazione, attaccante, vicino, risultato);
            risultato.addInterazione(interazione);
        }
        double danno = Math.floor(area.getDannoGrezzo() * 100.0d / (100.0d + difesa)
                * moltiplicatoreDelleInterazioni(vicino, tipoDanno, interazioni));
        risultato.setDanno(Math.max(1, (int) danno));
        Logger.log(() -> area.getInterazione() + " investe " + vicino.getNome() + " con " + risultato.getDanno() + " danni " + tipoDanno);
        return risultato;
    }

    /**
     * Chi una reazione ad area investe, nella fila del gruppo del bersaglio (chi è fuori combattimento non conta e non
     * separa): chi sta subito prima e subito dopo di lui; per l'ELETTROCUZIONE, invece, i BAGNATI attaccati a lui da una
     * parte e dall'altra, finché la pozza non si interrompe.
     */
    public static List<Personaggio> viciniInvestiti(List<Personaggio> fila, Personaggio bersaglio, TipoInterazioneConEffettiDiStato interazione) {
        List<Personaggio> inCampo = new ArrayList<>();
        for (Personaggio personaggio : fila) {
            if (personaggio == bersaglio || !personaggio.isFuoriCombattimento()) {
                inCampo.add(personaggio);
            }
        }
        int posizione = inCampo.indexOf(bersaglio);
        List<Personaggio> vicini = new ArrayList<>();
        if (posizione < 0) {
            return vicini;
        }
        boolean nellaPozza = interazione == TipoInterazioneConEffettiDiStato.ELETTROCUZIONE;
        for (int verso : new int[]{-1, 1}) {
            for (int i = posizione + verso; i >= 0 && i < inCampo.size(); i += verso) {
                Personaggio vicino = inCampo.get(i);
                if (nellaPozza && !vicino.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
                    break;
                }
                vicini.add(vicino);
                if (!nellaPozza) {
                    break;
                }
            }
        }
        return vicini;
    }

    /**
     * Uno degli effetti di stato che quel tipo di danno può provocare, a caso; null se non ne provoca
     */
    private static TipoEffettoDiStato effettoDiStatoCasuale(TipoDanno tipoDanno) {
        TipoEffettoDiStato[] effetti = tipoDanno.getEffetti();
        if (effetti.length == 0) {
            Logger.log(() -> "effettoDiStatoCasuale(" + tipoDanno + "): nessun effetto collaterale");
            return null;
        }
        TipoEffettoDiStato tipoEffetto = effetti[Dado.tiraAncheAUnaFaccia(effetti.length) - 1];
        Logger.log(() -> "effettoDiStatoCasuale(" + tipoDanno + "): effetto casuale: " + tipoEffetto);
        return tipoEffetto;
    }

    /**
     * Quanto pesa un livello nel danno, per tutti: la sua radice quadrata, come per la salute massima (vedi
     * PersonaggioBase.calcolaSaluteMassima), così uno scontro alla pari dura uguale a ogni livello
     */
    static double livelloDiCombattimento(int livello) {
        return Math.sqrt(livello);
    }

    /**
     * Quanto vale un'arma per chi la impugna, senza dadi, critici né bersaglio (come contro un difensore neutro, e al pieno del
     * danno anche se è la seconda arma): danni base per il livello di combattimento dell'arma, più per ogni incantamento il bonus fisso
     * e la quota dell'INTELLIGENZA. Serve a confrontare due armi (il cambio d'arma con la doppia arma); non guarda gli effetti di stato
     * né i modificatori che l'arma applica.
     */
    public static double dannoAttesoDellArma(Personaggio attaccante, Arma arma) {
        double danno = arma.getDanni() * livelloDiCombattimento(arma.getLivello());
        for (IncantamentoMD incantamento : arma.getIncantamenti()) {
            danno += incantamento.getDannoBonusFisso() * arma.getLivello()
                    + attaccante.getIntelligenza() * incantamento.getCoefficienteScala() * moltiplicatoreDanniMagici(attaccante);
        }
        return danno;
    }

    /**
     * Il danno che un colpo con quest'arma farebbe in media a quel difensore, senza dadi: la probabilità di colpire per il danno base
     * dell'arma ({@link #dannoAttesoDellArma}) per il fattore di danno atteso contro quel difensore ({@link #moltiplicatoreAtteso}: difesa,
     * debolezze e resistenze di classe e di stati). 0 se è immune a quel tipo di danno. Lo usano le tattiche per confrontare le azioni.
     */
    public static double dannoAtteso(Personaggio attaccante, Personaggio difensore, Arma arma) {
        return dannoAtteso(attaccante, difensore, arma, 1.0d);
    }

    /**
     * Come {@link #dannoAtteso(Personaggio, Personaggio, Arma)} per la quota del danno che l'arma fa in quella fase, o che un bersaglio
     * subisce in una posizione di un incantesimo a più bersagli (vedi {@link #quotaDelDannoPerPosizione}).
     */
    public static double dannoAtteso(Personaggio attaccante, Personaggio difensore, Arma arma, double fattoreDellaFase) {
        double probabilita = calcolaProbabilitaDiColpire(attaccante, difensore, arma.getTipoDanno().getCategoriaDanno()) / 100.0d;
        return probabilita * dannoAttesoDellArma(attaccante, arma) * fattoreDellaFase * moltiplicatoreAtteso(difensore, arma.getTipoDanno());
    }

    /**
     * Il danno medio di un attacco con le armi (tutte le fasi, la seconda arma compresa) contro quel difensore, senza dadi
     */
    public static double dannoAttesoDellaMischia(Personaggio attaccante, Personaggio difensore) {
        double totale = 0;
        for (FaseDiAttacco fase : fasiDiAttacco(attaccante, difensore)) {
            totale += dannoAtteso(attaccante, difensore, fase.getArma(), fase.getFattore());
        }
        return totale;
    }

    /**
     * Le fasi di attacco di un turno: l'arma principale e, per chi combatte con due armi, quella nella
     * mano secondaria, al 40% (CostantiPersonaggi.DOPPIA_ARMA_FATTORE_SECONDA_ARMA).
     */
    public static List<FaseDiAttacco> fasiDiAttacco(Personaggio attaccante) {
        return fasiDiAttacco(attaccante, null);
    }

    /**
     * Come {@link #fasiDiAttacco(Personaggio)}, ma contro un difensore preciso: se l'attaccante combatte con la sua
     * arma naturale, questa può cambiare in base a chi ha davanti (vedi ArmaNaturale.controDifensore).
     */
    public static List<FaseDiAttacco> fasiDiAttacco(Personaggio attaccante, Personaggio difensore) {
        List<FaseDiAttacco> fasi = new ArrayList<>();
        Arma armaPrincipale = attaccante.getArmaEquipaggiata();
        if (difensore != null && armaPrincipale instanceof ArmaNaturale) {
            armaPrincipale = ((ArmaNaturale) armaPrincipale).controDifensore(difensore);
        }
        fasi.add(new FaseDiAttacco(armaPrincipale, 1.0d));
        attaccante.getArmaSecondaria()
                .ifPresent(arma -> fasi.add(new FaseDiAttacco(arma, CostantiPersonaggi.DOPPIA_ARMA_FATTORE_SECONDA_ARMA)));
        return fasi;
    }

    /**
     * @param fattore la quota del danno (base e incantamenti) che l'arma fa in questa fase: 1 per l'arma
     *                principale, meno per la seconda arma
     */
    public static DannoRisultante calcolaDannoRisultante(Personaggio attaccante, Personaggio difensore, Arma arma, double fattore) {

        DannoRisultante dannoRisultante = new DannoRisultante(attaccante, difensore);
        TipoDanno tipoDanno = arma.getTipoDanno();
        dannoRisultante.setTipoDanno(tipoDanno);

        boolean immuneATipoDannoBase = difensore.isImmuneATipoDanno(tipoDanno);
        dannoRisultante.addReazioneDiClasse(tipoDanno, difensore.getMoltiplicatoreDannoSubito(tipoDanno));
        if (immuneATipoDannoBase) {
            BusEventi.pubblica(new InternoMessaggio(difensore.getNome() + " è immune al tipo di danno " + tipoDanno));
        }

        // 1. CALCOLO STATISTICHE EFFETTIVE
        // Determina se l'attacco scala su FORZA (Fisico) o INTELLIGENZA (Magico/Elementale)
        int statOffensiva;
        boolean dannoNonFisico = tipoDanno.getCategoriaDanno() == CategoriaDanno.ELEMENTALE || tipoDanno.getCategoriaDanno() == CategoriaDanno.MAGICO;
        Logger.log(() -> "tipoDanno = " + tipoDanno + ", dannoNonFisico = " + dannoNonFisico);
        if (dannoNonFisico) {
            statOffensiva = attaccante.getIntelligenza();
            if (Logger.isAttivo()) {
            	Logger.log("statOffensiva (INTELLIGENZA) = " + statOffensiva);
            }
            // La SAGGEZZA potenzia i danni SACRO
            if (tipoDanno == TipoDanno.SACRO) {
                statOffensiva += attaccante.getSaggezza() / 2;
                if (Logger.isAttivo()) {
                	Logger.log(String.format(Locale.ROOT, "statOffensiva modificata da SACRO (%d) = %d", attaccante.getSaggezza(), statOffensiva));
                }
            }
        } else {
            statOffensiva = attaccante.getForza();
            if (Logger.isAttivo()) {
            	Logger.log("statOffensiva (FORZA) = " + statOffensiva);
            }
        }

        if (dannoNonFisico && attaccante.hasEffettoDiStato(TipoEffettoDiStato.MENTE_FRATTURATA)) {
            statOffensiva = (int) Math.round(statOffensiva * 0.8d);
            if (Logger.isAttivo()) {
            	Logger.log("statOffensiva penalizzata dal 20% da MENTE_FRATTURATA = " + statOffensiva);
            }
        }

        // Determina la difesa del bersaglio (COSTITUZIONE + PARATA per Fisico, RESISTENZA_MAGICA per Magico/Elementale),
        // con le resistenze di elmo, scudo, armatura e schinieri contro questo tipo di danno
        double statDifensiva = difesaContro(difensore, tipoDanno);
        // Quanto l'equipaggiamento difensivo ha cambiato il danno rispetto a un difensore senza: lo vedono tutti (StatoDelCombattimento)
        dannoRisultante.setFattoreEquipaggiamento((100.0d + difesaBaseContro(difensore, tipoDanno)) / (100.0d + statDifensiva));

        if (dannoNonFisico && difensore.hasEffettoDiStato(TipoEffettoDiStato.MENTE_FRATTURATA)) {
            statDifensiva = statDifensiva * 0.8d;
            if (Logger.isAttivo()) {
            	Logger.log("statDifensiva penalizzata dal 20% da MENTE_FRATTURATA = " + statDifensiva);
            }
        }

        // 2. MATEMATICA DI BASE DEL DANNO (Con fattore di scala livello arma, che pesa con la sua radice quadrata)
        int dannoBaseArma = (int) Math.round(arma.getDanni() * livelloDiCombattimento(arma.getLivello()));
        if (isIncantesimo(arma)) {
            // Il danno proprio dell'incantesimo: il resto lo fanno l'INTELLIGENZA e il moltiplicatore magico di chi la lancia
            dannoBaseArma = (int) Math.round(dannoBaseArma * CostantiIncantesimi.INCANTESIMO_FATTORE_DANNI);
        }

        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "dannoBaseArma = danniBase %d * livello arma %d = %d", arma.getDanni(), arma.getLivello(), dannoBaseArma));
        }

        // Rapporto di Efficacia dell'Arma per evitare exploit di armi liv. 1 su campioni liv. 20: fino a
        // TOLLERANZA_LIVELLO_ARMA livelli sotto quello di chi la impugna l'arma rende ancora tutta la parte dell'eroe
        double rapportoEfficacia = (double) (arma.getLivello() + CostantiPersonaggi.TOLLERANZA_LIVELLO_ARMA) / (double) attaccante.getLivello();
        if (rapportoEfficacia > 1.0d) {
            rapportoEfficacia = 1.0d;
        }
        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "rapportoEfficacia ((livello arma %d + %d) / livello attaccante %d) = %f", arma.getLivello(),
	                CostantiPersonaggi.TOLLERANZA_LIVELLO_ARMA, attaccante.getLivello(), rapportoEfficacia));
        }

        double contributoEroe = statOffensiva * livelloDiCombattimento(attaccante.getLivello()) / 5.0d;
        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "contributoEroe (statOffensiva %d * livello attaccante %d / 5 = %f", statOffensiva, attaccante.getLivello(), contributoEroe));
        }
        // Il carattere della classe: il guerriero picchia, il mago incanta (Costanti.*_MOLTIPLICATORE_DANNI_*)
        double moltiplicatoreClasse = dannoNonFisico ? moltiplicatoreDanniMagici(attaccante) : attaccante.getMoltiplicatoreDanniFisici();
        double dannoOffensivoGrezzo = (dannoBaseArma + Math.floor(contributoEroe * rapportoEfficacia)) * fattore * moltiplicatoreClasse;
        if (Logger.isAttivo()) {
        	Logger.log("dannoOffensivoGrezzo = " + dannoOffensivoGrezzo + " (fattore " + fattore + ", moltiplicatore di classe " + moltiplicatoreClasse + ")");
        }
        if (isIncantesimo(arma)) {
            // Bastoni e libri magici aumentano la potenza degli incantesimi (POTERE_MAGICO)
            dannoOffensivoGrezzo = attaccante.getModificaDanniMagia(dannoOffensivoGrezzo);
            if (Logger.isAttivo()) {
            	Logger.log("dannoOffensivoGrezzo dopo POTERE_MAGICO = " + dannoOffensivoGrezzo);
            }
        }

        // 2.5 APPLICAZIONE DEL BONUS BERSERK (Esclusivo ai Guerrieri con FURIA)
        // Solo sul danno fisico (vedi TipoEffettoDiStato.BERSERK): la condizione era negata al
        // contrario, e il bonus valeva solo per gli attacchi elementali e magici.
        if (!dannoNonFisico && attaccante.hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
            // Il danno fisico scala con la salute persa: più è ferito, più forte colpisce
            double percentualeSalutePerduta = 1.0d - ((double) attaccante.getSalute() / (double) attaccante.getSaluteMassima());
            dannoOffensivoGrezzo = dannoOffensivoGrezzo * (1.0d + percentualeSalutePerduta * 0.5d);
            if (Logger.isAttivo()) {
            	Logger.log("dannoOffensivoGrezzo dopo BERSERK = " + dannoOffensivoGrezzo);
            }
        }

        // 3. APPLICAZIONE INTERAZIONI ELEMENTALI E STATI DEL DIFENSORE
        double moltiplicatoreDannoStato = 1.0d;
        double dannoPuroBonus = 0.0d;
        boolean criticoAutomatico = false;
        boolean purificazioneAttiva = false;

        if (!immuneATipoDannoBase) {

            // Quali interazioni scattano, e quanto moltiplicano il danno e cambiano la difesa, lo dicono le stesse funzioni
            // che usano le tattiche (interazioniAttive, moltiplicatoreDelleInterazioni, fattoreDifesaDaStati): qui si
            // applicano solo i loro effetti, nell'ordine in cui scattano
            List<TipoInterazioneConEffettiDiStato> interazioni = interazioniAttive(difensore, tipoDanno);
            // Chi resiste a questo tipo di danno subisce meno anche le reazioni con i suoi stati: ognuna scatta con quella probabilità
            interazioni = interazioniCheScattano(interazioni, fattoreEffetti(difensore, tipoDanno));
            moltiplicatoreDannoStato = moltiplicatoreDelleInterazioni(difensore, tipoDanno, interazioni);
            statDifensiva = statDifensiva * fattoreDifesaDaStati(difensore, tipoDanno, interazioni);

            for (TipoInterazioneConEffettiDiStato interazione : interazioni) {
                // Gli stati che la reazione toglie e mette sono scritti nell'interazione (TipoInterazioneConEffettiDiStato):
                // qui sotto restano solo gli effetti che non sono stati
                applicaGliStatiDellInterazione(interazione, attaccante, difensore, dannoRisultante);
                dannoRisultante.addInterazione(interazione);
                switch (interazione) {
                    // Investono anche i vicini del difensore, con metà del danno grezzo di questo colpo (PersonaggioBase lo
                    // distribuisce, calcolaDannoAdArea lo calcola per ciascuno)
                    case ELETTROCUZIONE:
                    case ESPLOSIONE_DI_GAS:
                    case RIGETTO:
                        dannoRisultante.addDannoAdArea(new DannoRisultante.DannoAdArea(interazione,
                                dannoOffensivoGrezzo * CostantiPersonaggi.DANNO_AD_AREA_QUOTA));
                        break;

                    // Cambiano solo il danno (l'ha già fatto moltiplicatoreDelleInterazioni) o gli stati (qui sopra)
                    case ALIMENTAZIONE_FIAMMA:
                    case REAZIONE_TOSSICA:
                    case CONGELAMENTO:
                    case VAPORIZZAZIONE:
                    case ESTINZIONE:
                    case SCIOGLIMENTO_TERMICO:
                    case VAMPATA_TOSSICA:
                    case COAGULAZIONE_FORZATA:
                    case DISGELO_VIOLENTO:
                    case SUPERCONDUZIONE:
                    case TOSSICITA_SETTICA:
                    case SCHIACCIAMENTO:
                    case INCIAMPO:
                    case INCENDIO_LIBERATORIO:
                    case IMPATTO_RIGIDO:
                    case SOVRACCARICO_MENTALE:
                    case SHOCK_DI_REALTA:
                    case FOLLIA_COSMICA:
                    case DISPERSIONE:
                    case FANGO:
                    case DISORIENTAMENTO:
                    case ISOLAMENTO_SENSORIALE:
                        if (Logger.isAttivo()) {
                            Logger.log("Interazione " + interazione + " con " + tipoDanno + " su " + difensore.getNome());
                        }
                        break;

                    case DILUIZIONE_EMATICA:
                        Logger.log("tipo danno ACQUA: Diluizione Ematica, riduce il danno periodico da sanguinamento");
                        dannoRisultante.setDiluizioneEmaticaAttiva(true);
                        break;

                    case MIETITURA:
                        // Il danno raddoppia (l'ha già fatto moltiplicatoreDelleInterazioni); il minion quasi morto si
                        // risveglia scheletro (PersonaggioBase.applicaRisultatoCombattimento)
                        dannoRisultante.setMietituraAttiva(true);
                        break;

                    case COLLASSO_ENTROPICO:
                        Logger.log("tipo danno VUOTO: Collasso Entropico, brucia il 25% dei MP massimi come danno aggiuntivo");
                        dannoRisultante.setCollassoEntropicoAttivo(true);
                        break;

                    case PURIFICAZIONE:
                        // La cura ad area (50% del danno applicato) va calcolata più avanti, quando il danno finale è noto
                        purificazioneAttiva = true;
                        break;

                    case SIFONE_VITALE:
                        // Sifone Vitale: il 30% del danno grezzo (pre-mitigazione) va all'attaccante come MP, l'eccedenza oltre
                        // il massimo di MP va in HP
                        dannoRisultante.setSifoneVitale((int) Math.round(dannoOffensivoGrezzo * 0.3d));
                        break;

                    case COLPO_DI_GRAZIA:
                        Logger.log("tipo danno TAGLIENTE o PERFORANTE, critico automatico");
                        criticoAutomatico = true;
                        dannoRisultante.setColpoDiGrazia(true);
                        break;

                    case FRANTUMAZIONE_DEL_GHIACCIO:
                        // Il ghiaccio va in pezzi: critico automatico, che non si somma a un critico vero
                        Logger.log("tipo danno CONTUNDENTE su un CONGELATO, critico automatico");
                        criticoAutomatico = true;
                        break;

                    case RISONANZA_SIGILLATA:
                        Logger.log("tipo danno ARCANO: Risonanza Sigillata, +30% danno Puro che ignora le difese");
                        dannoPuroBonus = dannoPuroBonus + dannoOffensivoGrezzo * 0.3d;
                        break;

                    default:
                        throw new IllegalStateException("Interazione non gestita: " + interazione);
                }
            }
        }

        // 4. MITIGAZIONE DELLA DIFESA DEL DIFENSORE (Formula Diminishing Returns)
        double fattoreMitigazione = 100.0d / (100.0d + statDifensiva);
        if (Logger.isAttivo()) {
        	Logger.log("fattoreMitigazione: " + fattoreMitigazione + ", moltiplicatoreDannoStato = " + moltiplicatoreDannoStato);
        }
        double dannoMitigato = immuneATipoDannoBase ? 0.0d :
                Math.floor(dannoOffensivoGrezzo * fattoreMitigazione * moltiplicatoreDannoStato);
        Logger.log(() -> "dannoMitigato: " + dannoMitigato);

        // 4.2 --- COMPONENTE ELEMENTALE INCANTATA CUMULATIVA ---
        double dannoElementaleFinale = 0.0d;

        if (arma.isIncantata()) {
            double moltiplicatoreSet = moltiplicatoreSet(attaccante, arma);
            for (IncantamentoMD inc : arma.getIncantamenti()) {
                TipoDanno elementoMagico = inc.getTipoDannoElementale();

                double moltiplicatoreDannoSubito = difensore.getMoltiplicatoreDannoSubito(elementoMagico);
                dannoRisultante.addReazioneDiClasse(elementoMagico, moltiplicatoreDannoSubito);
                if (difensore.isImmuneATipoDanno(elementoMagico)) {
                    Logger.log(() -> String.format(Locale.ROOT, "[INCANTAMENTO] %s è immune a %s: incantamento senza effetto",
                            difensore.getNome(), elementoMagico));
                    continue;
                }

                double dannoQuestoIncantamento = dannoIncantamento(attaccante, difensore, inc, arma.getLivello(),
                        fattore * moltiplicatoreSet, 1.0d);

                // La debolezza o resistenza della classe a questo elemento
                dannoQuestoIncantamento = dannoQuestoIncantamento * moltiplicatoreDannoSubito;

                // Se il bersaglio era BAGNATO e la spada è di FUOCO, si attiva l'interazione Vaporizzazione
                if (elementoMagico == TipoDanno.FUOCO && difensore.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
                    applicaGliStatiDellInterazione(TipoInterazioneConEffettiDiStato.VAPORIZZAZIONE, attaccante, difensore, dannoRisultante);
                    dannoRisultante.addInterazione(TipoInterazioneConEffettiDiStato.VAPORIZZAZIONE);
                }

                // Un incantamento NECROTICO su un bersaglio MALEDETTO miete come un'arma necrotica: moltiplica il suo
                // danno (non quello dell'arma) e, se il bersaglio è un minion quasi morto, lo fa risvegliare scheletro
                if (elementoMagico == TipoDanno.NECROTICO && tipoDanno != TipoDanno.NECROTICO
                        && difensore.hasEffettoDiStato(TipoEffettoDiStato.MALEDETTO)) {
                    dannoQuestoIncantamento = dannoQuestoIncantamento * TipoInterazioneConEffettiDiStato.MIETITURA.getMoltiplicatore();
                    dannoRisultante.addInterazione(TipoInterazioneConEffettiDiStato.MIETITURA);
                    dannoRisultante.setMietituraAttiva(true);
                }

                // SOMMA i danni invece di sovrascriverli
                dannoElementaleFinale += dannoQuestoIncantamento;
                if (Logger.isAttivo()) {
                	Logger.log(String.format(Locale.ROOT, "[INCANTAMENTO] Danno bonus accumulato da %s: %.2f", elementoMagico, dannoQuestoIncantamento));
                }
            }
        }

        // 4.3 --- LIBRO MAGICO: bonus al danno degli incantesimi di chi lo porta ---
        if (isIncantesimo(arma) && !immuneATipoDannoBase) {
            Optional<Artefatto> libro = libroMagico(attaccante);
            if (libro.isPresent()) {
                // Il bonus del libro è danno dell'incantesimo: prende tutto il moltiplicatore magico della classe.
                // Un elfo ne ha solo una parte
                // Con più bersagli il bonus del libro si ripartisce come il resto del danno (fattore è la quota del bersaglio)
                double dannoLibro = dannoIncantamento(attaccante, difensore, bonusLibroMagico(libro.get(), tipoDanno),
                        libro.get().getLivello(), fattoreLibroMagico(attaccante) * fattore, moltiplicatoreDanniMagici(attaccante));
                dannoElementaleFinale += dannoLibro;
                Logger.log(() -> String.format(Locale.ROOT, "[LIBRO MAGICO] Danno bonus: %.2f", dannoLibro));
            }
        }

        // Il danno totale combinato dell'attacco (il bonus Puro di Risonanza Sigillata ignora la mitigazione)
        double dannoTotaleCombinato = dannoMitigato + dannoElementaleFinale + dannoPuroBonus;

        // 5. DETERMINAZIONE DEL COLPO CRITICO (Come capire se il colpo raddoppia)
        // Formula di base: 5% fisso + 1% per ogni punto statistica CRITICO dell'attaccante,
        // contrastata dalla FORTUNA del difensore.

        int intuitoCritico = attaccante.getCritico();
        int contromisuraCritico = difensore.getFortuna();

        double probabilitaCritico = Math.max(0.0d, 5.0d + intuitoCritico - contromisuraCritico);
        Logger.log(() -> String.format(Locale.ROOT, "probabilitaCritico: %f = MAX(0, 5 + CRITICO attaccante %d - FORTUNA difensore %d)", probabilitaCritico, intuitoCritico, contromisuraCritico));

        double tiroDadoCritico = Dado.tira(100);
        Logger.log(() -> "tiroDadoCritico: " + tiroDadoCritico);
        if (tiroDadoCritico <= probabilitaCritico || criticoAutomatico) {
            // Il colpo critico raddoppia il danno finale calcolato
            dannoTotaleCombinato = dannoTotaleCombinato * 2.0d;
            if (Logger.isAttivo()) {
            	Logger.log("dannoTotaleCombinato raddoppiato per CRITICO: " + dannoTotaleCombinato);
            }
        }

        // 6. APPLICAZIONE DEI NUOVI STATI NATIVI (Proc Rate)
        if (dannoTotaleCombinato > 0.0d) {
            Logger.log("Applicazione stati nativi per arma ibrida");

            // --- TRAGUARDO 1: Stato dell'arma Principale (es. TAGLIENTE ➔ SANGUINAMENTO) ---
            if (!immuneATipoDannoBase && tipoDanno.hasEffettiDiStato()) {
                double probStatoFisico = percentualeControForza(dannoMitigato, difensore) + (attaccante.getFuria() * 2.0d);
                if (Dado.tira(100) <= probStatoFisico * fattoreEffetti(difensore, tipoDanno)) {
                    TipoEffettoDiStato effettoFisico = effettoDiStatoCasuale(tipoDanno);
                    if (effettoFisico != null) {
                        dannoRisultante.addEffettoDiStato(effettoFisico, calcolaDurataStato(difensore, effettoFisico),
                                calcolaDannoPeriodico(attaccante, difensore, effettoFisico));
                        Logger.log(() -> "Arma applica stato fisico: " + effettoFisico);
                    }
                }
            }

            // --- TRAGUARDO 2: Stati degli Incantamenti (Multipli e Indipendenti) ---
            if (arma.isIncantata()) {
                double moltiplicatoreSet = moltiplicatoreSet(attaccante, arma);
                for (IncantamentoMD incantamento : arma.getIncantamenti()) {
                    if (difensore.isImmuneATipoDanno(incantamento.getTipoDannoElementale())) {
                        continue;
                    }
                    if (incantamento.getTipoDannoElementale().hasEffettiDiStato()) {
                        TipoDanno elemento = incantamento.getTipoDannoElementale();

                        // Ricalcola il danno specifico di QUESTO incantamento per un Proc Rate preciso
                        double dannoQuestoIncantamentoProc = dannoIncantamento(attaccante, difensore, incantamento,
                                arma.getLivello(), fattore * moltiplicatoreSet, 1.0d);

                        // Probabilità (su 100) che l'incantamento applichi il suo effetto di stato: una parte dipende da
                        // quanto è forte il colpo di QUESTO elemento rispetto alla FORZA del difensore, l'altra da chi
                        // colpisce, cioè la sua INTELLIGENZA pesata dal POTERE_MAGICO (100 = nessun bastone o libro).
                        // La riserva di MAGIA non conta: un guerriero senza mana con INTELLIGENZA 10 aggiunge 10 punti,
                        // un mago con INTELLIGENZA 20 e un bastone +20% ne aggiunge 24.
                        double contributoMagico = attaccante.getIntelligenza() * attaccante.getPotereMagico() / 100.0d;
                        double probStatoMagico = percentualeControForza(dannoQuestoIncantamentoProc, difensore) + contributoMagico;
                        if (Dado.tira(100) <= probStatoMagico * fattoreEffetti(difensore, elemento)) {
                            TipoEffettoDiStato effettoMagico = effettoDiStatoCasuale(elemento);
                            if (effettoMagico != null) {
                                dannoRisultante.addEffettoDiStato(effettoMagico, calcolaDurataStato(difensore, effettoMagico),
                                        calcolaDannoPeriodico(attaccante, difensore, effettoMagico));
                                Logger.log(() -> "L'incantamento applica stato magico: " + effettoMagico.name());
                            }
                        }
                    }
                }
            }

            // --- TRAGUARDO 3: Modificatori dell'arma sul bersaglio (es. FORTUNA porta a 0 per qualche turno) ---
            // Un bersaglio immune al tipo di danno dell'arma lo è anche ai suoi modificatori. Per gli altri vale la
            // probabilità degli stati degli incantamenti: il colpo contro la FORZA del difensore, più l'INTELLIGENZA
            // di chi colpisce pesata dal POTERE_MAGICO
            if (!immuneATipoDannoBase) {
                for (ModificatoreAttributoMD modificatore : arma.getModificatoriSulBersaglio()) {
                    double contributoMagico = attaccante.getIntelligenza() * attaccante.getPotereMagico() / 100.0d;
                    double probModificatore = percentualeControForza(dannoTotaleCombinato, difensore) + contributoMagico;
                    if (Dado.tira(100) <= probModificatore * fattoreEffetti(difensore, tipoDanno)) {
                        dannoRisultante.addModificatoreDaAggiungere(modificatore.suPortatore());
                        Logger.log(() -> "L'arma applica il modificatore: " + modificatore.getTipoAttributo() + " " + modificatore.getQuantita());
                    }
                }
            }

            int dannoFinale = Math.max(1, (int) dannoTotaleCombinato);
            dannoRisultante.setDanno(dannoFinale);

            if (purificazioneAttiva) {
                // Cura ad area: 50% del danno applicato (dannoFinale, già mitigato ed eventualmente raddoppiato dal critico)
                dannoRisultante.setCuraAdArea(dannoFinale / 2);
            }
        }

        // DEBUG: in modalità di prova l'OmbraFiamma fa sempre almeno 100 danni, anche contro
        // bersagli immuni, per poter testare che il gioco sia completabile.
        if (ModalitaDiProva.isAttiva() && attaccante instanceof OmbraFiamma && dannoRisultante.getDanno() < 100) {
            Logger.log("DEBUG: attaccante OmbraFiamma in modalità di prova, danno minimo forzato a 100.");
            dannoRisultante.setDanno(100);
        }

        return dannoRisultante;
    }

    /**
     * Il danno di un incantamento, mitigato dalla difesa del bersaglio contro il suo tipo di danno.
     * La parte fissa scala con il livello dell'oggetto (e per moltiplicatoreParteFissa), quella percentuale
     * con l'INTELLIGENZA e il moltiplicatore di danno magico di chi colpisce: una spada di fuoco rende di più
     * in mano a un elfo che a un guerriero. Contro un bersaglio BAGNATO il fuoco fa la metà.
     */
    private static double dannoIncantamento(Personaggio attaccante, Personaggio difensore, IncantamentoMD incantamento,
                                            int livelloOggetto, double fattore, double moltiplicatoreParteFissa) {
        double dannoGrezzo = ((incantamento.getDannoBonusFisso() * livelloOggetto * moltiplicatoreParteFissa) +
                (attaccante.getIntelligenza() * incantamento.getCoefficienteScala() * moltiplicatoreDanniMagici(attaccante))) * fattore;
        double mitigazione = 100.0d / (100.0d + difesaContro(difensore, incantamento.getTipoDannoElementale()));
        double danno = Math.floor(dannoGrezzo * mitigazione);
        if (incantamento.getTipoDannoElementale() == TipoDanno.FUOCO && difensore.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
            danno = danno * 0.5d;
        }
        return danno;
    }

    /**
     * Il colpo in percentuale della FORZA del difensore: la base della probabilità che scattino stati e modificatori. Un difensore
     * senza FORZA non regge niente, quindi vale 100 (e non una divisione per zero).
     */
    private static double percentualeControForza(double danno, Personaggio difensore) {
        return difensore.getForza() > 0 ? (danno * 100.0d) / difensore.getForza() : 100.0d;
    }

    /**
     * Quanto reggono gli effetti di un tipo di danno (stati, modificatori, reazioni con gli stati) contro un difensore: il
     * moltiplicatore del danno subito (vedi Personaggio.getMoltiplicatoreDannoSubito) se è minore di 1, cioè se il difensore
     * resiste, altrimenti 1. Chi è debole subisce più danno ma non più effetti. Per chi è immune (0) non scatta niente, ma
     * l'immunità l'ha già esclusa il calcolo del danno.
     */
    public static double fattoreEffetti(Personaggio difensore, TipoDanno tipoDanno) {
        return Math.min(1.0d, difensore.getMoltiplicatoreDannoSubito(tipoDanno));
    }

    /**
     * Le interazioni che scattano davvero: ognuna scatta con probabilità <code>fattoreEffetti</code>. Con 1 (nessuna resistenza)
     * le tiene tutte senza tirare i dadi.
     */
    private static List<TipoInterazioneConEffettiDiStato> interazioniCheScattano(List<TipoInterazioneConEffettiDiStato> interazioni,
                                                                                double fattoreEffetti) {
        if (fattoreEffetti >= 1.0d) {
            return interazioni;
        }
        List<TipoInterazioneConEffettiDiStato> scattate = new ArrayList<>();
        for (TipoInterazioneConEffettiDiStato interazione : interazioni) {
            if (Dado.tira(100) <= fattoreEffetti * 100.0d) {
                scattate.add(interazione);
            }
        }
        return scattate;
    }

    /**
     * Le interazioni fra il tipo di danno e gli stati che il difensore ha già che scattano, nell'ordine di dichiarazione di
     * TipoInterazioneConEffettiDiStato (che è quello in cui il calcolo del danno le applica): ognuna scatta se il difensore ha
     * uno dei suoi stati e riceve uno dei suoi tipi di danno. (La debolezza o resistenza della sua classe a quel danno non è
     * un'interazione: vedi Personaggio.getMoltiplicatoreDannoSubito.)
     * Non cambia niente e non tira i dadi; un difensore immune a quel danno non ne ha.
     * <p>
     * È l'unico punto che decide quando un'interazione scatta: il calcolo del danno vero e le tattiche (per sapere se un
     * avversario è debole a un elemento) usano questo.
     */
    public static List<TipoInterazioneConEffettiDiStato> interazioniAttive(Personaggio difensore, TipoDanno tipoDanno) {
        return interazioniAttive(difensore, tipoDanno, StatiPrevisti.NESSUNO);
    }

    /**
     * Come {@link #interazioniAttive(Personaggio, TipoDanno)}, ma con gli stati che un'azione precedente può avergli messo o tolto:
     * per le tattiche, che guardano oltre l'azione di adesso.
     */
    static List<TipoInterazioneConEffettiDiStato> interazioniAttive(Personaggio difensore, TipoDanno tipoDanno,
                                                                   StatiPrevisti previsti) {
        if (difensore.isImmuneATipoDanno(tipoDanno)) {
            return Collections.emptyList();
        }
        List<TipoInterazioneConEffettiDiStato> attive = new ArrayList<>();
        for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
            if (interazione.getInnescanti().contains(tipoDanno)
                    && interazione.getPrecondizioni().stream().anyMatch(stato -> previsti.ha(difensore, stato))) {
                attive.add(interazione);
            }
        }
        return attive;
    }

    /**
     * Gli stati che un'azione cambierà al difensore, per le stime: quelli che gli metterà e quelli che gli toglierà. Quelli
     * tolti vincono, come nel calcolo vero, in cui chi subisce il colpo toglie gli stati dopo averli messi.
     */
    static final class StatiPrevisti {

        static final StatiPrevisti NESSUNO = new StatiPrevisti(EnumSet.noneOf(TipoEffettoDiStato.class),
                EnumSet.noneOf(TipoEffettoDiStato.class));

        private final Set<TipoEffettoDiStato> messi;
        private final Set<TipoEffettoDiStato> tolti;

        StatiPrevisti(Set<TipoEffettoDiStato> messi, Set<TipoEffettoDiStato> tolti) {
            this.messi = messi;
            this.tolti = tolti;
        }

        /**
         * Se il difensore, dopo l'azione, avrà quello stato.
         */
        boolean ha(Personaggio difensore, TipoEffettoDiStato stato) {
            return !tolti.contains(stato) && (difensore.hasEffettoDiStato(stato) || messi.contains(stato));
        }
    }

    /**
     * Toglie e mette gli stati che la reazione dichiara (TipoInterazioneConEffettiDiStato.getStatiRimossi e getStatiApplicati),
     * con la durata e il danno periodico normali, salvo le eccezioni di {@link #durataDelloStatoMesso} e
     * {@link #dannoDelloStatoMesso}. Chi applica il danno toglie gli stati dopo averli messi (vedi PersonaggioBase).
     */
    private static void applicaGliStatiDellInterazione(TipoInterazioneConEffettiDiStato interazione, Personaggio attaccante,
                                                       Personaggio difensore, DannoRisultante dannoRisultante) {
        for (TipoEffettoDiStato stato : interazione.getStatiRimossi()) {
            dannoRisultante.rimuoviEffettoDiStato(stato);
        }
        for (TipoEffettoDiStato stato : interazione.getStatiApplicati()) {
            int durata = durataDelloStatoMesso(interazione, difensore, stato);
            dannoRisultante.addEffettoDiStato(stato, durata, dannoDelloStatoMesso(interazione, attaccante, difensore, stato));
        }
    }

    /**
     * Quanto dura uno stato messo da una reazione: quanto dura di solito, salvo l'ALIMENTAZIONE_FIAMMA (il vento allunga il
     * BRUCIATO: a quel che resta si somma una durata normale), lo SCHIACCIAMENTO (un turno in più a terra),
     * il FANGO (un RALLENTATO severo, due turni in più), il SOVRACCARICO_MENTALE (uno STORDITO pesante, 1-2 turni) e il
     * DISORIENTAMENTO (uno STORDITO di un turno).
     */
    private static int durataDelloStatoMesso(TipoInterazioneConEffettiDiStato interazione, Personaggio difensore,
                                             TipoEffettoDiStato stato) {
        switch (interazione) {
            case ALIMENTAZIONE_FIAMMA:
                return durataResidua(difensore, stato) + calcolaDurataStato(difensore, stato);
            case SCHIACCIAMENTO:
                return calcolaDurataStato(difensore, stato) + 1;
            case FANGO:
                return calcolaDurataStato(difensore, stato) + 2;
            case SOVRACCARICO_MENTALE:
                return Dado.tira(2);
            case DISORIENTAMENTO:
                return 1;
            default:
                return calcolaDurataStato(difensore, stato);
        }
    }

    /**
     * I turni che restano allo stato del difensore, 0 se non ce l'ha.
     */
    private static int durataResidua(Personaggio difensore, TipoEffettoDiStato stato) {
        return difensore.getEffettiDiStato().stream().filter(e -> e.getTipoEffettoDiStato() == stato)
                .mapToInt(EffettoDiStatoMD::getDurata).max().orElse(0);
    }

    /**
     * Il danno periodico di uno stato messo da una reazione: quello di solito, doppio per la TOSSICITA_SETTICA (il veleno su
     * un bersaglio già INFETTATO).
     */
    private static int dannoDelloStatoMesso(TipoInterazioneConEffettiDiStato interazione, Personaggio attaccante,
                                            Personaggio difensore, TipoEffettoDiStato stato) {
        int danno = calcolaDannoPeriodico(attaccante, difensore, stato);
        return interazione == TipoInterazioneConEffettiDiStato.TOSSICITA_SETTICA ? danno * 2 : danno;
    }

    /**
     * Gli effetti di stato che un'arma (o un incantesimo) può causare a un difensore: quelli nativi del suo tipo di danno e
     * dei tipi elementali dei suoi incantamenti, e quelli che mettono le reazioni con gli stati che il difensore ha già
     * (l'INCIAMPO atterra un RALLENTATO), escludendo i tipi di danno e gli stati a cui è immune e gli stati che le reazioni
     * gli tolgono (vedi {@link #statiTolti}). Non cambia niente e non tira i dadi; conta le reazioni come se scattassero e
     * non dice con che probabilità scattano gli stati (vedi {@link #fattoreEffetti}).
     */
    public static Set<TipoEffettoDiStato> statiCausabili(Personaggio difensore, Arma arma) {
        return Collections.unmodifiableSet(previsione(difensore, arma).messi);
    }

    /**
     * Gli effetti di stato che un'arma (o un incantesimo) toglie a un difensore con le reazioni che scatenano: il FUOCO
     * scioglie un CONGELATO, l'ACQUA spegne un BRUCIATO. Come per {@link #statiCausabili}, le reazioni si contano come se
     * scattassero.
     */
    public static Set<TipoEffettoDiStato> statiTolti(Personaggio difensore, Arma arma) {
        return Collections.unmodifiableSet(previsione(difensore, arma).tolti);
    }

    private static StatiPrevisti previsione(Personaggio difensore, Arma arma) {
        Set<TipoEffettoDiStato> messi = EnumSet.noneOf(TipoEffettoDiStato.class);
        Set<TipoEffettoDiStato> tolti = EnumSet.noneOf(TipoEffettoDiStato.class);
        aggiungiStatiCausabili(messi, tolti, difensore, arma.getTipoDanno());
        if (arma.isIncantata()) {
            for (IncantamentoMD incantamento : arma.getIncantamenti()) {
                aggiungiStatiCausabili(messi, tolti, difensore, incantamento.getTipoDannoElementale());
            }
        }
        // Chi subisce il colpo toglie gli stati dopo averli messi: uno stato messo e tolto non resta
        messi.removeAll(tolti);
        return new StatiPrevisti(messi, tolti);
    }

    private static void aggiungiStatiCausabili(Set<TipoEffettoDiStato> messi, Set<TipoEffettoDiStato> tolti, Personaggio difensore,
                                               TipoDanno tipoDanno) {
        if (difensore.isImmuneATipoDanno(tipoDanno)) {
            return;
        }
        for (TipoEffettoDiStato stato : tipoDanno.getEffetti()) {
            if (!difensore.isImmuneAEffetto(stato)) {
                messi.add(stato);
            }
        }
        for (TipoInterazioneConEffettiDiStato interazione : interazioniAttive(difensore, tipoDanno)) {
            for (TipoEffettoDiStato stato : interazione.getStatiApplicati()) {
                if (!difensore.isImmuneAEffetto(stato)) {
                    messi.add(stato);
                }
            }
            tolti.addAll(interazione.getStatiRimossi());
        }
    }

    /**
     * Per quanto il difensore moltiplica il danno di quel tipo: quello della sua classe (debolezza, resistenza o immunità: vedi
     * Personaggio.getMoltiplicatoreDannoSubito) per i moltiplicatori delle interazioni attive (vedi {@link #interazioniAttive};
     * TipoInterazioneConEffettiDiStato.getMoltiplicatore; il critico automatico del COLPO_DI_GRAZIA e della
     * FRANTUMAZIONE_DEL_GHIACCIO non c'è, perché si decide con il critico: lo aggiunge moltiplicatoreAtteso). Meno di 1
     * vuol dire che il difensore resiste, più di 1 che è debole.
     */
    public static double moltiplicatoreDelleInterazioni(Personaggio difensore, TipoDanno tipoDanno,
                                                        List<TipoInterazioneConEffettiDiStato> interazioni) {
        double moltiplicatore = difensore.getMoltiplicatoreDannoSubito(tipoDanno);
        for (TipoInterazioneConEffettiDiStato interazione : interazioni) {
            moltiplicatore = moltiplicatore * interazione.getMoltiplicatore();
        }
        return moltiplicatore;
    }

    /**
     * Di quanto gli stati del difensore cambiano la sua difesa contro quel danno: un CONGELATO ha un guscio di ghiaccio che
     * para i colpi di lama e di punta (×1,5), ma la SUPERCONDUZIONE la riduce (×0,7); 1 se non cambia.
     */
    static double fattoreDifesaDaStati(Personaggio difensore, TipoDanno tipoDanno, List<TipoInterazioneConEffettiDiStato> interazioni) {
        return fattoreDifesaDaStati(difensore, tipoDanno, interazioni, StatiPrevisti.NESSUNO);
    }

    private static double fattoreDifesaDaStati(Personaggio difensore, TipoDanno tipoDanno, List<TipoInterazioneConEffettiDiStato> interazioni,
                                               StatiPrevisti previsti) {
        if (interazioni.contains(TipoInterazioneConEffettiDiStato.SUPERCONDUZIONE)) {
            return 0.7d;
        }
        if (previsti.ha(difensore, TipoEffettoDiStato.CONGELATO) && tipoDanno.getCategoriaDanno() == CategoriaDanno.FISICO
                && tipoDanno != TipoDanno.CONTUNDENTE) {
            return 1.5d;
        }
        return 1.0d;
    }

    /**
     * Il fattore del danno atteso contro un difensore, senza tirare i dadi e senza cambiare niente: 0 se è immune a quel
     * tipo di danno; altrimenti la mitigazione della sua difesa, <code>100 / (100 + difesa)</code>, per il moltiplicatore delle
     * interazioni attive (la debolezza della classe e quelle degli stati: vedi {@link #interazioniAttive}). Più di quello di un
     * altro tipo di danno vuol dire che il difensore è più debole a questo. Lo usano le tattiche; il calcolo del danno
     * vero usa le stesse funzioni.
     * <p>
     * Non tiene conto della probabilità ridotta con cui scattano le reazioni con gli stati e gli stati nativi contro chi resiste
     * a quel tipo di danno (vedi {@link #fattoreEffetti}): conta le reazioni come se scattassero. Il critico automatico
     * (TipoInterazioneConEffettiDiStato.isCriticoAutomatico) conta ×2; il critico a caso no.
     */
    public static double moltiplicatoreAtteso(Personaggio difensore, TipoDanno tipoDanno) {
        return moltiplicatoreAtteso(difensore, tipoDanno, StatiPrevisti.NESSUNO);
    }

    /**
     * Come {@link #moltiplicatoreAtteso(Personaggio, TipoDanno)}, ma per un colpo che arriva <b>dopo</b> un'azione che ha causato al
     * difensore gli stati indicati (vedi {@link #statiCausabili}): il Gelo che prima bagna e poi congela, il fulmine che colpisce
     * un BAGNATO. Gli stati si contano come se fossero scattati (come le reazioni, vedi sopra). Non cambia niente e non tira i dadi.
     */
    public static double moltiplicatoreAttesoDopo(Personaggio difensore, TipoDanno tipoDanno, Set<TipoEffettoDiStato> statiFuturi) {
        return moltiplicatoreAtteso(difensore, tipoDanno, new StatiPrevisti(statiFuturi, EnumSet.noneOf(TipoEffettoDiStato.class)));
    }

    /**
     * Il fattore del colpo di tipo <code>tipoDanno</code> che segue un'azione fatta con l'arma indicata, contando anche gli stati che
     * l'arma può causare al difensore ({@link #statiCausabili}) e senza quelli che gli toglie ({@link #statiTolti}). Per scegliere il
     * bersaglio o l'azione guardando oltre il colpo di adesso.
     */
    public static double moltiplicatoreAttesoDopo(Personaggio difensore, TipoDanno tipoDanno, Arma azione) {
        return moltiplicatoreAtteso(difensore, tipoDanno, previsione(difensore, azione));
    }

    private static double moltiplicatoreAtteso(Personaggio difensore, TipoDanno tipoDanno, StatiPrevisti previsti) {
        if (difensore.isImmuneATipoDanno(tipoDanno)) {
            return 0.0d;
        }
        List<TipoInterazioneConEffettiDiStato> interazioni = interazioniAttive(difensore, tipoDanno, previsti);
        double difesa = difesaContro(difensore, tipoDanno);
        if (tipoDanno.getCategoriaDanno() != CategoriaDanno.FISICO && previsti.ha(difensore, TipoEffettoDiStato.MENTE_FRATTURATA)) {
            difesa = difesa * 0.8d;
        }
        difesa = difesa * fattoreDifesaDaStati(difensore, tipoDanno, interazioni, previsti);
        // Il critico automatico (colpo di grazia, frantumazione) raddoppia il colpo: non è fra i moltiplicatori perché il
        // calcolo vero lo decide insieme al critico
        double critico = interazioni.stream().anyMatch(TipoInterazioneConEffettiDiStato::isCriticoAutomatico) ? 2.0d : 1.0d;
        return 100.0d / (100.0d + difesa) * moltiplicatoreDelleInterazioni(difensore, tipoDanno, interazioni) * critico;
    }

    /**
     * La difesa del bersaglio contro un tipo di danno T:
     * (difesa base + Σ fisso_T × livello del pezzo) × (1 + Σ percentuale_T / 2), sugli incantamenti di tipo T
     * di elmo, scudo, armatura e schinieri. La difesa base è COSTITUZIONE + PARATA per il danno fisico,
     * RESISTENZA_MAGICA per quello elementale o magico.
     */
    public static double difesaContro(Personaggio difensore, TipoDanno tipoDanno) {
        double difesaBase = difesaBaseContro(difensore, tipoDanno);
        double fisso = 0.0d;
        double percentuale = 0.0d;
        for (Artefatto pezzo : difensore.getInventario()) {
            if (!isPezzoDifensivo(pezzo)) {
                continue;
            }
            double moltiplicatoreSet = RegoleSetLeggendari.moltiplicatore(difensore.getModelloDati().getArtefatti(), pezzo.getModelloDati());
            for (IncantamentoMD incantamento : pezzo.getIncantamenti()) {
                if (incantamento.getTipoDannoElementale() == tipoDanno) {
                    fisso += incantamento.getDannoBonusFisso() * pezzo.getLivello() * moltiplicatoreSet;
                    percentuale += incantamento.getCoefficienteScala() * CostantiPersonaggi.RESISTENZA_FATTORE_PERCENTUALE * moltiplicatoreSet;
                }
            }
        }
        double difesa = (difesaBase + fisso) * (1.0d + percentuale);
        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "difesa contro %s = (base %.0f + fisso %.0f) x (1 + %.3f) = %.2f",
	                tipoDanno, difesaBase, fisso, percentuale, difesa));
        }
        return difesa;
    }

    /**
     * La difesa del personaggio senza i pezzi dell'equipaggiamento: COSTITUZIONE + PARATA per il fisico, RESISTENZA_MAGICA per il resto
     */
    public static double difesaBaseContro(Personaggio difensore, TipoDanno tipoDanno) {
        boolean fisico = tipoDanno.getCategoriaDanno() == CategoriaDanno.FISICO;
        return fisico ? difensore.getCostituzione() + difensore.getParata() : difensore.getResistenzaMagica();
    }

    /**
     * Il moltiplicatore del set leggendario dell'arma, se è un artefatto e chi la impugna ha il set completo (vedi
     * RegoleSetLeggendari); 1 per le armi naturali e gli incantesimi.
     */
    private static double moltiplicatoreSet(Personaggio attaccante, Arma arma) {
        if (!(arma instanceof Artefatto)) {
            return 1.0d;
        }
        return RegoleSetLeggendari.moltiplicatore(attaccante.getModelloDati().getArtefatti(), ((Artefatto) arma).getModelloDati());
    }

    private static boolean isPezzoDifensivo(Artefatto artefatto) {
        CategoriaArtefatto supertipo = artefatto.getTipo().getCategoria();
        return supertipo == CategoriaArtefatto.SCUDO || supertipo == CategoriaArtefatto.ELMO
                || supertipo == CategoriaArtefatto.ARMATURA || supertipo == CategoriaArtefatto.SCHINIERI;
    }

    /**
     * Le pergamene di incantesimi e il dardo arcano: il loro danno segue le regole degli incantesimi
     * (INCANTESIMO_FATTORE_DANNI, bonus del libro magico)
     */
    private static boolean isIncantesimo(Arma arma) {
        return arma.isMagica();
    }

    /**
     * Quanto del bonus del libro magico ha chi lo porta: tutto, tranne l'elfo, che non lo usa bene come il mago.
     */
    static double fattoreLibroMagico(Personaggio personaggio) {
        TipoPersonaggio classe = personaggio.getClasse();
        return classe == TipoPersonaggio.ELFO || classe == TipoPersonaggio.ELFA ? CostantiPersonaggi.LIBRO_MAGICO_FATTORE_ELFO : 1.0d;
    }

    /**
     * Il moltiplicatore dei danni magici di chi attacca: quello della sua classe, per quanto lo moltiplica il libro magico che
     * porta, se lo moltiplica (il Manuale di Unix System V li raddoppia: vedi LibroTecnico).
     */
    static double moltiplicatoreDanniMagici(Personaggio personaggio) {
        double moltiplicatoreLibro = libroMagico(personaggio).flatMap(LibroTecnico::di)
                .map(LibroTecnico::getMoltiplicatoreDanniMagici).orElse(1).doubleValue();
        return personaggio.getMoltiplicatoreDanniMagici() * moltiplicatoreLibro;
    }

    private static Optional<Artefatto> libroMagico(Personaggio personaggio) {
        return personaggio.getInventario().stream()
                .filter(a -> a.getTipo() == TipoArtefatto.LIBRO_MAGICO)
                .findFirst();
    }

    /**
     * Il bonus del libro magico come un incantamento del tipo di danno dell'incantesimo: parte fissa e
     * percentuale del grado adatto al livello del libro, più il 25%.
     */
    static IncantamentoMD bonusLibroMagico(Artefatto libro, TipoDanno tipoDanno) {
        GradoIncantamento grado = GradoIncantamento.perLivello(libro.getLivello());
        double maggiorazione = 1.0d + CostantiPersonaggi.LIBRO_MAGICO_MAGGIORAZIONE;
        return new IncantamentoMD("Libro magico", tipoDanno,
                (int) Math.round(grado.getBonusFisso() * maggiorazione), grado.getCoefficiente() * maggiorazione);
    }

    /**
     * La durata in turni di uno stato alterato appena causato al difensore: dipende dal tipo di stato e da quanto il difensore lo contrasta con i suoi
     * attributi. Atterrato dura un turno, e il berserk (che finisce da solo lasciando la locazione) vale uno.
     */
    public static int calcolaDurataStato(Personaggio difensore, TipoEffettoDiStato stato) {
        // Gestione immediata dell'unico stato a 1 turno fisso (Azione per rialzarsi)
        if (stato == TipoEffettoDiStato.ATTERRATO) {
            return 1;
        }

        // Lo stato speciale BERSERK non ha turni, termina al cambio mappa
        if (stato == TipoEffettoDiStato.BERSERK) {
            return 1;
        }

        int durataBase;
        int riduzione = 0;

        // BIVIO LOGICO DI BILANCIAMENTO GENERALE
        switch (stato) {

            case STORDITO:
                // --- CATEGORIA: TRAUMA FISICO (Base 2 turni, contrasta COSTITUZIONE) ---
                durataBase = 2;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getCostituzione()) / 3.0d);
                break;

            case CONGELATO:
                // --- CATEGORIA: BLOCCO MISTICO (Base 3 turni, contrasta SAGGEZZA) ---
                durataBase = 3;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getSaggezza()) / 3.0d);
                break;

            case RALLENTATO:
                // --- CATEGORIA: INTRALCIO MOTORIO (Base 3 turni, contrasta DESTREZZA) ---
                durataBase = 3;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getDestrezza()) / 3.0d);
                break;

            case IMMOBILIZZATO:
                // --- CATEGORIA: MORSA FISICA/TERRESTRE (Base 3 turni, contrasta FORZA) ---
                durataBase = 3;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getForza()) / 3.0d);
                break;

            case SPAVENTATO:
                // --- CATEGORIA: ATTACCO AL MORALE (Base 3 turni, contrasta CORAGGIO secondario) ---
                durataBase = 3;
                // Moltiplichiamo il moltiplicatore di Coraggio * 10 per portarlo sulla stessa scala degli attributi primari
                riduzione = (int) Math.floor(Math.sqrt(difensore.getCoraggio() * 10.0d) / 3.0d);
                break;

            case SANGUINAMENTO:
            case AVVELENATO:
            case INFETTATO:
                // --- CATEGORIA: TOSSINE E DEGENERAZIONI ORGANICHE (Base 4 turni, contrasta COSTITUZIONE) ---
                durataBase = 4;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getCostituzione()) / 3.0d);
                break;

            case BRUCIATO:
                // --- CATEGORIA: INCENDIO ELEMENTALE (Base 4 turni, contrasta SAGGEZZA/Controllo Energetico) ---
                durataBase = 4;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getSaggezza()) / 3.0d);
                break;

            case CONFUSO:
            case ACCECATO:
            case ASSORDATO:
            case SILENZIATO:
            case MALEDETTO:
            case BAGNATO:
            default:
                // --- CATEGORIA: DEBILITAZIONI MENTALI E SENSORIALI BASE (Base 3 turni, contrasta SAGGEZZA) ---
                durataBase = 3;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getSaggezza()) / 3.0d);
                break;
        }

        // CALCOLO FINALE E COMPUTAZIONE DEI LIMITI (CAP)
        int durataFinale = durataBase - riduzione;

        // BINDAGGIO ANTIPANICO: Qualsiasi stato applicato con successo deve durare
        // almeno 1 turno (il turno corrente in cui viene consumato/subito)
        // e non può mai superare la sua durata base naturale per evitare il perma-block.
        return Math.max(1, Math.min(durataBase, durataFinale));
    }

    /**
     * Calcola il danno ad ogni tick
     */
    public static int calcolaDannoPeriodico(Personaggio attaccante, Personaggio difensore,
                                            TipoEffettoDiStato stato) {

        double dannoGrezzo = 0.0d;
        double difesaFiltro = 0.0d;
        int livelloAttaccante = attaccante.getLivello();

        switch (stato) {
            case SANGUINAMENTO:
                // Danno fisico: scala su FORZA dell'attaccante e ignora metà COSTITUZIONE
                dannoGrezzo = (attaccante.getForza() * livelloAttaccante) / 10.0d;
                difesaFiltro = difensore.getCostituzione() / 4.0d;
                if (Logger.isAttivo()) {
                	Logger.log(String.format(Locale.ROOT, "[DoT] Elaborazione SANGUINAMENTO su %s. Attacco: %.2f, Difesa Filtro: %.2f",
	                        difensore.getNome(), dannoGrezzo, difesaFiltro));
                }
                break;

            case BRUCIATO:
                // Danno elementale: scala su INTELLIGENZA dell'attaccante, contrasta RESISTENZA MAGICA
                dannoGrezzo = (attaccante.getIntelligenza() * livelloAttaccante) / 10.0d;
                difesaFiltro = difensore.getResistenzaMagica() / 2.0d;
                if (Logger.isAttivo()) {
                	Logger.log(String.format(Locale.ROOT, "[DoT] Elaborazione BRUCIATO su %s. Attacco: %.2f, Difesa Filtro: %.2f",
	                        difensore.getNome(), dannoGrezzo, difesaFiltro));
                }
                break;

            case AVVELENATO:
                // Danno elementale: scala su INTELLIGENZA dell'attaccante, contrasta COSTITUZIONE
                dannoGrezzo = (attaccante.getIntelligenza() * livelloAttaccante) / 10.0d;
                difesaFiltro = difensore.getCostituzione() / 2.0d;
                Logger.log(() -> String.format(Locale.ROOT, "[DoT] Elaborazione AVVELENATO su %s.", difensore.getNome()));
                break;

            case INFETTATO:
                // Danno necrotico: scala su INTELLIGENZA, contrasta RESISTENZA MAGICA
                dannoGrezzo = (attaccante.getIntelligenza() * livelloAttaccante) / 12.0d; // Leggermente più lento ma blocca le cure
                difesaFiltro = difensore.getResistenzaMagica() / 2.0d;
                Logger.log(() -> String.format(Locale.ROOT, "[DoT] Elaborazione INFETTATO su %s.", difensore.getNome()));
                break;

            default:
                return 0;
        }

        // Calcolo finale del danno del singolo tick del timer
        return (int) Math.max(1, Math.floor(dannoGrezzo - difesaFiltro));
    }
}
