package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.motore.ModalitaDiProva;
import com.threeamigos.foresta.oggetti.ArmaNaturale;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.leggendari.RegoleSetLeggendari;
import com.threeamigos.foresta.personaggi.OmbraFiamma;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.strumenti.Logger;
import com.threeamigos.foresta.tipi.*;

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
     * Il risultato di un colpo pieno con quell'arma: il danno, gli stati alterati e le reazioni che ne seguono.
     */
    public static DannoRisultante calcolaDannoRisultante(Personaggio attaccante, Personaggio difensore, Arma arma) {
        return calcolaDannoRisultante(attaccante, difensore, arma, 1.0d);
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
                    + attaccante.getIntelligenza() * incantamento.getCoefficienteScala() * attaccante.getMoltiplicatoreDanniMagici();
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

    private static double dannoAtteso(Personaggio attaccante, Personaggio difensore, Arma arma, double fattoreDellaFase) {
        double probabilita = calcolaProbabilitaDiColpire(attaccante, difensore, arma.getTipoDanno().getSuperTipo()) / 100.0d;
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
     * mano secondaria, al 40% (Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA).
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
                .ifPresent(arma -> fasi.add(new FaseDiAttacco(arma, Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA)));
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
        boolean dannoNonFisico = tipoDanno.getSuperTipo() == CategoriaDanno.ELEMENTALE || tipoDanno.getSuperTipo() == CategoriaDanno.MAGICO;
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
            dannoBaseArma = (int) Math.round(dannoBaseArma * Costanti.INCANTESIMO_FATTORE_DANNI);
        }

        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "dannoBaseArma = danniBase %d * livello arma %d = %d", arma.getDanni(), arma.getLivello(), dannoBaseArma));
        }

        // Rapporto di Efficacia dell'Arma per evitare exploit di armi liv. 1 su campioni liv. 20: fino a
        // TOLLERANZA_LIVELLO_ARMA livelli sotto quello di chi la impugna l'arma rende ancora tutta la parte dell'eroe
        double rapportoEfficacia = (double) (arma.getLivello() + Costanti.TOLLERANZA_LIVELLO_ARMA) / (double) attaccante.getLivello();
        if (rapportoEfficacia > 1.0d) {
            rapportoEfficacia = 1.0d;
        }
        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "rapportoEfficacia ((livello arma %d + %d) / livello attaccante %d) = %f", arma.getLivello(),
	                Costanti.TOLLERANZA_LIVELLO_ARMA, attaccante.getLivello(), rapportoEfficacia));
        }

        double contributoEroe = statOffensiva * livelloDiCombattimento(attaccante.getLivello()) / 5.0d;
        if (Logger.isAttivo()) {
        	Logger.log(String.format(Locale.ROOT, "contributoEroe (statOffensiva %d * livello attaccante %d / 5 = %f", statOffensiva, attaccante.getLivello(), contributoEroe));
        }
        // Il carattere della classe: il guerriero picchia, il mago incanta (Costanti.*_MOLTIPLICATORE_DANNI_*)
        double moltiplicatoreClasse = dannoNonFisico ? attaccante.getMoltiplicatoreDanniMagici() : attaccante.getMoltiplicatoreDanniFisici();
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
                switch (interazione) {
                    // Cambiano solo il danno, e questo l'ha già fatto moltiplicatoreDelleInterazioni
                    case ELETTROCUZIONE:
                    case ALIMENTAZIONE_FIAMMA:
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case REAZIONE_TOSSICA:
                        Logger.log("tipo danno ACIDO: Reazione Tossica, +15% danno");
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case DILUIZIONE_EMATICA:
                        Logger.log("tipo danno ACQUA: Diluizione Ematica, riduce il danno periodico da sanguinamento");
                        dannoRisultante.setDiluizioneEmaticaAttiva(true);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case CONGELAMENTO:
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.CONGELATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.CONGELATO),
                                calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.CONGELATO));
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case VAPORIZZAZIONE:
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case ESTINZIONE:
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case SCIOGLIMENTO_TERMICO:
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.BAGNATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.BAGNATO),
                                calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.BAGNATO));
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case ESPLOSIONE_DI_GAS:
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.AVVELENATO),
                                calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.AVVELENATO));
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case VAMPATA_TOSSICA:
                        Logger.log("tipo danno FUOCO: Vampata Tossica");
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.AVVELENATO);
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.BRUCIATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.BRUCIATO),
                                calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.BRUCIATO));
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case COAGULAZIONE_FORZATA:
                        Logger.log("tipo danno GELO: Coagulazione Forzata, rimuove SANGUINAMENTO e applica RALLENTATO");
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO);
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.RALLENTATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.RALLENTATO),
                                calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.RALLENTATO));
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case FRANTUMAZIONE_DEL_GHIACCIO:
                    case DISGELO_VIOLENTO:
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.CONGELATO);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case SUPERCONDUZIONE:
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case MIETITURA:
                        // La SAGGEZZA del difensore riduce l'efficacia dei danni NECROTICO su un bersaglio MALEDETTO
                        // (il moltiplicatore sta in moltiplicatoreDelleInterazioni)
                        dannoRisultante.addInterazione(interazione);
                        dannoRisultante.setMietituraAttiva(true);
                        break;

                    case RIGETTO:
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.MALEDETTO);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case COLLASSO_ENTROPICO:
                        Logger.log("tipo danno VUOTO: Collasso Entropico, rimuove MALEDETTO e brucia il 25% dei MP massimi come danno aggiuntivo");
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.MALEDETTO);
                        dannoRisultante.setCollassoEntropicoAttivo(true);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case PURIFICAZIONE:
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.INFETTATO);
                        dannoRisultante.addInterazione(interazione);
                        // La cura ad area (50% del danno applicato) va calcolata più avanti, quando il danno finale è noto
                        purificazioneAttiva = true;
                        break;

                    case SIFONE_VITALE:
                        // Sifone Vitale: il 30% del danno grezzo (pre-mitigazione) va all'attaccante come MP, l'eccedenza oltre
                        // il massimo di MP va in HP
                        dannoRisultante.setSifoneVitale((int) Math.round(dannoOffensivoGrezzo * 0.3d));
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case TOSSICITA_SETTICA:
                        // Tossicità Settica: l'AVVELENATO applicato su un bersaglio già INFETTATO raddoppia i danni periodici
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.AVVELENATO),
                                calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.AVVELENATO) * 2);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case COLPO_DI_GRAZIA:
                        Logger.log("tipo danno TAGLIENTE o PERFORANTE, critico automatico");
                        criticoAutomatico = true;
                        dannoRisultante.setColpoDiGrazia(true);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case SCHIACCIAMENTO:
                        Logger.log("Con difensore già ATTERRATO, tipo danno CONTUNDENTE: Schiacciamento");
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.ATTERRATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.ATTERRATO) + 1, 0);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case INCIAMPO:
                        Logger.log("Con difensore già RALLENTATO, tipo danno TERRA o CONTUNDENTE: Inciampo");
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.ATTERRATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.ATTERRATO), 0);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case INCENDIO_LIBERATORIO:
                        Logger.log("tipo danno FUOCO: Incendio Liberatorio, distrugge l'immobilizzazione all'istante");
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.IMMOBILIZZATO);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case IMPATTO_RIGIDO:
                        Logger.log("tipo danno CONTUNDENTE o TERRA: Impatto Rigido");
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case SOVRACCARICO_MENTALE:
                        Logger.log("tipo danno ARCANO: Sovraccarico Mentale, trasforma lo stato in STORDITO pesante");
                        dannoRisultante.rimuoviEffettoDiStato(statoDiPaura(difensore));
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.STORDITO, Dado.tira(2), 0);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case SHOCK_DI_REALTA:
                        Logger.log("danno FISICO: Shock di Realtà, interrompe subito lo stato");
                        dannoRisultante.rimuoviEffettoDiStato(statoDiPaura(difensore));
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case FOLLIA_COSMICA:
                        Logger.log("tipo danno VUOTO: Follia Cosmica, rimuove CONFUSO e applica MENTE_FRATTURATA");
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.CONFUSO);
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.MENTE_FRATTURATA,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.MENTE_FRATTURATA), 0);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case DISPERSIONE:
                        Logger.log("tipo danno ARIA: Dispersione, rimuove ACCECATO");
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.ACCECATO);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case FANGO:
                        Logger.log("tipo danno ACQUA: Fango, applica RALLENTATO severo");
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.RALLENTATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.RALLENTATO) + 2,
                                calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.RALLENTATO));
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case DISORIENTAMENTO:
                        Logger.log("Con difensore già ASSORDATO, tipo danno SONICO: Disorientamento, trasforma in STORDITO");
                        dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.ASSORDATO);
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case RISONANZA_SIGILLATA:
                        Logger.log("tipo danno ARCANO: Risonanza Sigillata, +30% danno Puro che ignora le difese");
                        dannoPuroBonus = dannoPuroBonus + dannoOffensivoGrezzo * 0.3d;
                        dannoRisultante.addInterazione(interazione);
                        break;

                    case ISOLAMENTO_SENSORIALE:
                        Logger.log("tipo danno PSICHICO: Isolamento Sensoriale, +50% danno ed estende anche ACCECATO");
                        dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.ACCECATO,
                                calcolaDurataStato(difensore, TipoEffettoDiStato.ACCECATO), 0);
                        dannoRisultante.addInterazione(interazione);
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
                    dannoRisultante.addInterazione(TipoInterazioneConEffettiDiStato.VAPORIZZAZIONE);
                }

                // Un incantamento NECROTICO su un bersaglio MALEDETTO miete come un'arma necrotica: moltiplica il suo
                // danno (non quello dell'arma) e, se il bersaglio è un minion quasi morto, lo fa risvegliare scheletro
                if (elementoMagico == TipoDanno.NECROTICO && tipoDanno != TipoDanno.NECROTICO
                        && difensore.hasEffettoDiStato(TipoEffettoDiStato.MALEDETTO)) {
                    dannoQuestoIncantamento = dannoQuestoIncantamento * moltiplicatoreMietitura(difensore);
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
                double dannoLibro = dannoIncantamento(attaccante, difensore, bonusLibroMagico(libro.get(), tipoDanno),
                        libro.get().getLivello(), fattoreLibroMagico(attaccante), attaccante.getMoltiplicatoreDanniMagici());
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
                double probStatoFisico = ((dannoMitigato * 100.0d) / difensore.getForza()) + (attaccante.getFuria() * 2.0d);
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
                        double probStatoMagico = ((dannoQuestoIncantamentoProc * 100.0d) / difensore.getForza()) + contributoMagico;
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
                    double probModificatore = difensore.getForza() > 0
                            ? ((dannoTotaleCombinato * 100.0d) / difensore.getForza()) + contributoMagico
                            : 100.0d;
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
     * Di quanto la Mietitura moltiplica il danno necrotico su un bersaglio MALEDETTO: la SAGGEZZA del difensore ne
     * riduce l'efficacia.
     */
    private static double moltiplicatoreMietitura(Personaggio difensore) {
        return Math.max(1.0d, 2.0d - (difensore.getSaggezza() / 100.0));
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
                (attaccante.getIntelligenza() * incantamento.getCoefficienteScala() * attaccante.getMoltiplicatoreDanniMagici())) * fattore;
        double mitigazione = 100.0d / (100.0d + difesaContro(difensore, incantamento.getTipoDannoElementale()));
        double danno = Math.floor(dannoGrezzo * mitigazione);
        if (incantamento.getTipoDannoElementale() == TipoDanno.FUOCO && difensore.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
            danno = danno * 0.5d;
        }
        return danno;
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
        return interazioniAttive(difensore, tipoDanno, Collections.emptySet());
    }

    /**
     * Come {@link #interazioniAttive(Personaggio, TipoDanno)}, ma il difensore ha anche gli stati indicati (quelli che un'azione
     * precedente può avergli causato): per le tattiche, che guardano oltre l'azione di adesso.
     */
    static List<TipoInterazioneConEffettiDiStato> interazioniAttive(Personaggio difensore, TipoDanno tipoDanno,
                                                                   Set<TipoEffettoDiStato> statiFuturi) {
        if (difensore.isImmuneATipoDanno(tipoDanno)) {
            return Collections.emptyList();
        }
        List<TipoInterazioneConEffettiDiStato> attive = new ArrayList<>();
        for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
            if (interazione.getInnescanti().contains(tipoDanno)
                    && interazione.getPrecondizioni().stream().anyMatch(stato -> haLoStato(difensore, statiFuturi, stato))) {
                attive.add(interazione);
            }
        }
        return attive;
    }

    private static boolean haLoStato(Personaggio difensore, Set<TipoEffettoDiStato> statiFuturi, TipoEffettoDiStato stato) {
        return difensore.hasEffettoDiStato(stato) || statiFuturi.contains(stato);
    }

    /**
     * Gli effetti di stato che un'arma (o un incantesimo) può causare a un difensore: quelli nativi del suo tipo di danno e
     * quelli dei tipi elementali dei suoi incantamenti, escludendo i tipi di danno a cui il difensore è immune e gli stati a cui
     * è immune. Non cambia niente e non tira i dadi; non dice con che probabilità scattano (vedi {@link #fattoreEffetti}).
     */
    public static Set<TipoEffettoDiStato> statiCausabili(Personaggio difensore, Arma arma) {
        Set<TipoEffettoDiStato> stati = EnumSet.noneOf(TipoEffettoDiStato.class);
        aggiungiStatiCausabili(stati, difensore, arma.getTipoDanno());
        if (arma.isIncantata()) {
            for (IncantamentoMD incantamento : arma.getIncantamenti()) {
                aggiungiStatiCausabili(stati, difensore, incantamento.getTipoDannoElementale());
            }
        }
        return stati;
    }

    private static void aggiungiStatiCausabili(Set<TipoEffettoDiStato> stati, Personaggio difensore, TipoDanno tipoDanno) {
        if (difensore.isImmuneATipoDanno(tipoDanno)) {
            return;
        }
        for (TipoEffettoDiStato stato : tipoDanno.getEffetti()) {
            if (!difensore.isImmuneAEffetto(stato)) {
                stati.add(stato);
            }
        }
    }

    /**
     * Lo stato di SPAVENTATO o CONFUSO che conta per le interazioni: se li ha tutti e due, lo SPAVENTATO.
     */
    private static TipoEffettoDiStato statoDiPaura(Personaggio difensore) {
        return difensore.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO) ? TipoEffettoDiStato.SPAVENTATO : TipoEffettoDiStato.CONFUSO;
    }

    /**
     * Per quanto il difensore moltiplica il danno di quel tipo: quello della sua classe (debolezza, resistenza o immunità: vedi
     * Personaggio.getMoltiplicatoreDannoSubito) per i moltiplicatori delle interazioni attive (vedi {@link #interazioniAttive};
     * TipoInterazioneConEffettiDiStato.getMoltiplicatore, più la MIETITURA, che dipende dalla SAGGEZZA del difensore). Meno di 1
     * vuol dire che il difensore resiste, più di 1 che è debole.
     */
    public static double moltiplicatoreDelleInterazioni(Personaggio difensore, TipoDanno tipoDanno,
                                                        List<TipoInterazioneConEffettiDiStato> interazioni) {
        double moltiplicatore = difensore.getMoltiplicatoreDannoSubito(tipoDanno);
        for (TipoInterazioneConEffettiDiStato interazione : interazioni) {
            moltiplicatore = moltiplicatore * (interazione == TipoInterazioneConEffettiDiStato.MIETITURA ?
                    moltiplicatoreMietitura(difensore) : interazione.getMoltiplicatore());
        }
        return moltiplicatore;
    }

    /**
     * Di quanto gli stati del difensore cambiano la sua difesa contro quel danno: un CONGELATO ha un guscio di ghiaccio che
     * para i colpi di lama e di punta (×1,5), ma la SUPERCONDUZIONE la riduce (×0,7); 1 se non cambia.
     */
    static double fattoreDifesaDaStati(Personaggio difensore, TipoDanno tipoDanno, List<TipoInterazioneConEffettiDiStato> interazioni) {
        return fattoreDifesaDaStati(difensore, tipoDanno, interazioni, Collections.emptySet());
    }

    private static double fattoreDifesaDaStati(Personaggio difensore, TipoDanno tipoDanno, List<TipoInterazioneConEffettiDiStato> interazioni,
                                               Set<TipoEffettoDiStato> statiFuturi) {
        if (interazioni.contains(TipoInterazioneConEffettiDiStato.SUPERCONDUZIONE)) {
            return 0.7d;
        }
        if (haLoStato(difensore, statiFuturi, TipoEffettoDiStato.CONGELATO) && tipoDanno.getSuperTipo() == CategoriaDanno.FISICO
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
     * a quel tipo di danno (vedi {@link #fattoreEffetti}): conta le reazioni come se scattassero.
     */
    public static double moltiplicatoreAtteso(Personaggio difensore, TipoDanno tipoDanno) {
        return moltiplicatoreAtteso(difensore, tipoDanno, Collections.emptySet());
    }

    /**
     * Come {@link #moltiplicatoreAtteso(Personaggio, TipoDanno)}, ma per un colpo che arriva <b>dopo</b> un'azione che ha causato al
     * difensore gli stati indicati (vedi {@link #statiCausabili}): il Gelo che prima bagna e poi congela, il fulmine che colpisce
     * un BAGNATO. Gli stati si contano come se fossero scattati (come le reazioni, vedi sopra). Non cambia niente e non tira i dadi.
     */
    public static double moltiplicatoreAttesoDopo(Personaggio difensore, TipoDanno tipoDanno, Set<TipoEffettoDiStato> statiFuturi) {
        return moltiplicatoreAtteso(difensore, tipoDanno, statiFuturi);
    }

    /**
     * Il fattore del colpo di tipo <code>tipoDanno</code> che segue un'azione fatta con l'arma indicata, contando anche gli stati che
     * l'arma può causare al difensore ({@link #statiCausabili}). Per scegliere il bersaglio o l'azione guardando oltre il colpo di adesso.
     */
    public static double moltiplicatoreAttesoDopo(Personaggio difensore, TipoDanno tipoDanno, Arma azione) {
        return moltiplicatoreAtteso(difensore, tipoDanno, statiCausabili(difensore, azione));
    }

    private static double moltiplicatoreAtteso(Personaggio difensore, TipoDanno tipoDanno, Set<TipoEffettoDiStato> statiFuturi) {
        if (difensore.isImmuneATipoDanno(tipoDanno)) {
            return 0.0d;
        }
        List<TipoInterazioneConEffettiDiStato> interazioni = interazioniAttive(difensore, tipoDanno, statiFuturi);
        double difesa = difesaContro(difensore, tipoDanno);
        if (tipoDanno.getSuperTipo() != CategoriaDanno.FISICO && haLoStato(difensore, statiFuturi, TipoEffettoDiStato.MENTE_FRATTURATA)) {
            difesa = difesa * 0.8d;
        }
        difesa = difesa * fattoreDifesaDaStati(difensore, tipoDanno, interazioni, statiFuturi);
        return 100.0d / (100.0d + difesa) * moltiplicatoreDelleInterazioni(difensore, tipoDanno, interazioni);
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
                    percentuale += incantamento.getCoefficienteScala() * Costanti.RESISTENZA_FATTORE_PERCENTUALE * moltiplicatoreSet;
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
        boolean fisico = tipoDanno.getSuperTipo() == CategoriaDanno.FISICO;
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
        return arma instanceof IncantesimoMalefico || arma instanceof DardoArcano;
    }

    /**
     * Quanto del bonus del libro magico ha chi lo porta: tutto, tranne l'elfo, che non lo usa bene come il mago.
     */
    static double fattoreLibroMagico(Personaggio personaggio) {
        TipoPersonaggio classe = personaggio.getClasse();
        return classe == TipoPersonaggio.ELFO || classe == TipoPersonaggio.ELFA ? Costanti.LIBRO_MAGICO_FATTORE_ELFO : 1.0d;
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
        double maggiorazione = 1.0d + Costanti.LIBRO_MAGICO_MAGGIORAZIONE;
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
