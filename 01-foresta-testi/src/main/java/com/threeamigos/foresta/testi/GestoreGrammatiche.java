package com.threeamigos.foresta.testi;


import com.threeamigos.foresta.tipi.TipoModalitaProduzioneTesti;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.Maiuscole;
import com.threeamigos.foresta.strumenti.logger.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Legge un file di grammatica in formato testuale e lo usa per generare ("produrre") testo
 * in modo casuale, espandendo ricorsivamente i riferimenti alle produzioni.
 * <p>
 * <b>Come orientarsi in questa classe</b> (è la più lunga del modulo; i nomi dei metodi sono in inglese, i commenti in
 * italiano). Il lavoro ha due tempi:
 * <ol>
 *     <li><b>Il caricamento</b>, una volta sola, nel costruttore ({@link #inizializza(InputStream, InputStream)}): si sostituiscono
 *     le inclusioni ({@link #conInclusioni}), si leggono le righe e si costruisce la mappa delle produzioni
 *     ({@link #leggiSorgenteECreaProduzioni}, che per ogni riga chiama {@link #leggiIntestazione} o
 *     {@link #leggiFigli}), si controlla che ogni riferimento punti a qualcosa che esiste
 *     ({@link #controllaLeProduzioni}), si calcolano i pesi ({@link #aumentaIPesiPerIDiscendenti}) e si legge il file
 *     delle sostituzioni finali ({@link #leggiPostProduzione}).</li>
 *     <li><b>La produzione</b>, a ogni {@link #produci()}: {@link #produciDalNodo(String, Map, Map, String)} cerca la prima
 *     parentesi quadra del testo, la sostituisce con il suo valore ({@link #risolviElemento}, che sceglie un'alternativa con
 *     {@link #scegliAlternativa}) e ricomincia, finché di parentesi non ne restano; alla fine {@link #postProduci} sistema le
 *     preposizioni e gli spazi.</li>
 * </ol>
 * I messaggi d'errore (delle {@link GrammaticaMalformataException} e delle eccezioni di produzione) sono in inglese, per
 * scelta: li legge chi scrive una grammatica, che può non sapere l'italiano (vedi GestoreGrammatiche.md §9).
 * <p>
 * Un esempio minimo di grammatica e del suo uso:
 * <pre>{@code
 * SALUTO
 *     [CHI] ti saluta | [CHI] ti fa un cenno
 * CHI
 *     il fabbro | la locandiera
 *
 * new GestoreGrammatiche(testo).produci()   // per esempio ["la locandiera ti fa un cenno"]
 * }</pre>
 * <p>
 * Sintassi del file di grammatica:
 * <ul>
 *     <li>Una riga che inizia alla colonna 0 dichiara una <b>produzione</b>: il suo nome.</li>
 *     <li>Le righe rientrate con un TAB o con uno spazio sono i <b>figli</b> della produzione
 *     (le alternative): il testo che può essere scelto a caso quando la produzione viene
 *     espansa. Più alternative possono stare sulla stessa riga, separate da
 *     {@code |}.</li>
 *     <li>Un figlio può riferirsi a un'altra produzione racchiudendone il nome fra parentesi
 *     quadre, ad esempio {@code [AltraProduzione]}; all'espansione il riferimento viene
 *     sostituito da un'alternativa di {@code AltraProduzione} scelta a caso (e a sua volta
 *     espansa ricorsivamente).</li>
 *     <li>Un figlio può contenere un <b>gruppo di alternative in linea</b>,
 *     {@code {opz1|opz2|...}}: al caricamento viene trasformato in una produzione nuova, con un
 *     nome generato automaticamente (vedi {@link #espandiAlternativeInLinea}), e il gruppo è
 *     sostituito da un riferimento a essa, così da quel momento si comporta esattamente come
 *     un normale riferimento {@code [Nome]}. Esempio:
 *     <pre>{@code
 *     FATTO_PROEMIO
 *         Il {vecchio|giovane|misterioso} viandante bussò alla porta
 *     }</pre></li>
 *     <li>Un tratto delimitato da virgolette doppie ({@code "..."}) è un <b>tratto letterale
 *     grezzo</b>: {@code {}}, {@code }}} e {@code |} al suo interno sono trattati come testo
 *     semplice invece che come sintassi dei gruppi di alternative in linea, e {@code \"} al
 *     suo interno è una virgoletta letterale con l'escape, che non chiude il tratto. Un
 *     tratto lasciato aperto a fine riga (o a fine contenuto di un gruppo di alternative in
 *     linea) è un errore. Si noti che questo protegge solo {@code {}}, {@code }}} e
 *     {@code |}: un testo letterale che somigli a {@code [Nome]} dentro un tratto verrebbe
 *     comunque interpretato come riferimento a una produzione.</li>
 *     <li>Un nome di produzione che termina con {@code $} è <b>monouso</b>: una volta scelta una
 *     delle sue alternative durante un ciclo di produzione, quell'alternativa (e, se era
 *     l'ultima rimasta, l'intera produzione e ogni riferimento a essa) viene rimossa per il
 *     resto del ciclo.</li>
 *     <li>Un riferimento preceduto da {@code *} (ad esempio {@code [*Nome]}) è una
 *     <b>produzione fissata globalmente</b>: la prima volta che viene espansa nel corso di una
 *     chiamata a {@link #produci()}, il risultato viene messo in cache e riusato per ogni
 *     occorrenza successiva, in qualsiasi punto del testo prodotto.</li>
 *     <li>Un riferimento preceduto da {@code !} (ad esempio {@code [!Nome]}) è una
 *     <b>produzione fissata localmente</b>: come quella globale, ma il valore in cache viene
 *     riusato solo all'interno dello stesso sottoalbero di produzione (cioè non trapela nei
 *     rami fratelli prodotti in modo indipendente). È visibile a tutto ciò che sta annidato
 *     sotto il punto che l'ha fissato, a qualsiasi profondità, così un {@code [#Nome]} dentro
 *     una produzione referenziata trova comunque il valore fissato da chi l'ha chiamata.</li>
 *     <li>Un riferimento della forma {@code [chiave=valore]} <b>assegna</b> un valore
 *     a {@code chiave} nella stessa cache globale usata dai riferimenti {@code *}, per
 *     recuperarlo poi con un riferimento {@code #} in qualsiasi punto del testo prodotto.
 *     {@code valore} può a sua volta contenere elementi {@code [...]} (ad esempio
 *     {@code [chiave=[AltraProduzione]]}), che vengono risolti completamente prima che il
 *     risultato sia memorizzato.</li>
 *     <li>Un riferimento preceduto da {@code #} (ad esempio {@code [#chiave]}) recupera il
 *     valore assegnato in precedenza a {@code chiave} con un elemento {@code chiave=valore}.</li>
 *     <li>Un riferimento di qualsiasi tipo può avere un <b>valore predefinito</b>, scritto dopo il
 *     nome referenziato come {@code ?} seguito da {@code |} e dal testo di ripiego:
 *     {@code [*EQUIPAGGIAMENTO? | fagotto]}. Gli spazi attorno a {@code ?} e a {@code |} sono
 *     ignorati, e {@code [Nome?]} da solo è la forma abbreviata per un predefinito vuoto. Il
 *     ripiego è normale testo di grammatica: un letterale, un {@code [Riferimento]}, un gruppo
 *     in linea, o un misto. Si usa ogni volta che in quel punto non c'è alcun valore
 *     disponibile, perché il nome non è mai stato dichiarato, perché il {@code [chiave=valore]}
 *     che lo avrebbe riempito non è ancora stato eseguito, o perché una produzione monouso è
 *     esaurita: è ciò che permette di scrivere una grammatica senza dichiarare una produzione
 *     segnaposto solo per superare la validazione. Il {@code ?} dà tolleranza su <i>quando</i>
 *     compare un valore, non sul fatto che il nome abbia un significato: il nome deve comunque
 *     essere una produzione dichiarata o una chiave assegnata da qualche parte nella
 *     grammatica, così un errore di battitura viene rifiutato comunque al caricamento (vedi
 *     {@link #controllaElemento}). Un nome di produzione quindi non può terminare con
 *     {@code ?}. Il valore di un'assegnazione è testo libero, perciò {@code ?} e {@code |}
 *     dentro {@code [chiave=valore]} restano testo semplice.</li>
 *     <li>Una riga che inizia con {@code //} è un commento a riga intera e viene ignorata, così
 *     come le righe vuote.</li>
 *     <li>Un'alternativa può iniziare con un elemento {@code [^N]} ({@code N} un numero
 *     positivo, ad esempio {@code 10} o {@code 2.5}) per darle un <b>peso</b> di selezione: con
 *     {@link TipoModalitaProduzioneTesti#CASUALE} viene scelta {@code N} volte più spesso di
 *     un'alternativa col peso predefinito di 1.
 *     L'elemento è risolto e tolto al caricamento della grammatica (a differenza di
 *     {@code [chiave=valore]}, che è risolto in modo pigro durante la produzione), perciò non
 *     compare mai nel testo prodotto. Un'alternativa senza {@code [^N]} ha peso 1,
 *     esattamente come la selezione uniforme che ogni alternativa aveva prima che questa
 *     funzione esistesse, comprese le produzioni con nome automatico create dai gruppi di
 *     alternative in linea, le cui opzioni possono avere a loro volta un elemento {@code [^N]}
 *     ma non richiedono alcun trattamento speciale per restare uniformemente casuali.</li>
 *     <li>Una volta che l'intera grammatica è stata analizzata e validata, il peso di ogni
 *     alternativa viene aumentato automaticamente di {@link #FATTORE_DI_SCALA} volte il peso
 *     aggregato di ogni produzione che essa referenzia (ricorsivamente, dato che il peso di
 *     una produzione referenziata è a quel punto già stato aumentato da ciò che a sua volta
 *     referenzia), così le alternative che si espandono in sottoalberi strutturalmente più
 *     ricchi vengono scelte proporzionalmente più spesso con
 *     {@link TipoModalitaProduzioneTesti#CASUALE}, senza bisogno di scrivere a mano un
 *     elemento {@code [^N]}. Una produzione referenziata più di una volta nella stessa
 *     alternativa contribuisce all'aumento una volta per ogni occorrenza. Un riferimento con
 *     un predefinito conta o la produzione referenziata o ciò che il suo ripiego referenzia,
 *     mai entrambi, perché al momento della produzione se ne prende solo uno.
 *     Questo aggiustamento è calcolato una sola volta, al caricamento della grammatica, e
 *     incorporato nei pesi usati per tutta la vita del {@code GestoreGrammatiche}. I cicli di
 *     riferimenti (produzioni ricorsive su se stesse o a vicenda) sono gestiti usando il peso
 *     originale, non ancora aumentato, di una produzione ogni volta che la si reincontra
 *     mentre il suo stesso aumento è ancora in calcolo. Due membri strutturalmente identici
 *     di un ciclo quindi non finiscono con lo stesso peso: quello <b>dichiarato per primo nel
 *     file</b> viene aumentato per primo, e finisce per essere il più pesante dei due (vedi
 *     {@link #aumentaIPesiPerIDiscendenti}).</li>
 *     <li>Un riferimento di qualsiasi tipo ({@code [Nome]}, {@code [*Nome]}, {@code [!Nome]} o
 *     {@code [#chiave]}) preceduto da {@code ^} immediatamente alla sua sinistra, <b>fuori</b>
 *     dalle parentesi quadre (ad esempio {@code ^[Nome]}), ha la prima lettera del valore
 *     risolto in maiuscolo. A differenza dell'elemento di peso {@code [^N]}, che sta subito
 *     dentro le parentesi, all'inizio del testo di un'alternativa, ed è risolto al caricamento
 *     della grammatica, questo marcatore è risolto al momento della produzione, via via che
 *     ogni riferimento viene espanso, perciò funziona in modo uniforme su qualunque cosa
 *     produca quello specifico riferimento. Non esiste una sequenza di escape per un
 *     {@code ^} letterale immediatamente prima di un riferimento, come per gli altri
 *     marcatori del file.</li>
 * </ul>
 * Entrambi i file sorgente sono decodificati come <b>UTF-8</b>, in modo esplicito e non
 * tramite il charset predefinito della JVM, così le lettere accentate di una grammatica
 * sopravvivono qualunque sia la piattaforma su cui gira il gioco. Anche una {@code String}
 * passata ai costruttori testuali è codificata in UTF-8 prima di essere riletta.
 * <p>
 * Un secondo file facoltativo, di post-produzione, elenca sostituzioni letterali di testo
 * ({@code prima:dopo}) applicate al testo finale prodotto, per correggere i problemi di lingua
 * naturale che nascono dalla concatenazione meccanica (ad esempio, in italiano, {@code "a il"}
 * che diventa {@code "al"}). Il {@code dopo} di una regola non può contenere il proprio
 * {@code prima} alla lettera, dato che ogni regola viene applicata finché il suo {@code prima}
 * non compare più (vedi {@link #leggiPostProduzione} e {@link #postProduci}).
 * <p>
 * Ci si aspetta che ogni produzione produca una stringa senza spazi ai bordi (vedi
 * {@link #risolviSemplice} e {@link #risolviFissata}): gli spazi iniziali o
 * finali rimasti dopo che l'alternativa scelta di una produzione è stata espansa del tutto
 * vengono tolti prima che il risultato sia inserito in ciò che l'ha referenziata. Conta in
 * particolare per i gruppi di alternative in linea, dove la foglia vuota di un gruppo
 * annidato, scelta accanto a testo letterale, lascerebbe altrimenti uno spazio spurio.
 * <p>
 * Questa rimozione protegge solo i bordi dell'espansione di un singolo riferimento: un
 * riferimento a una produzione che risolve intenzionalmente in una stringa vuota (ad esempio
 * un'alternativa pensata per rappresentare "nessun testo in più qui") lascia comunque gli
 * spazi o i tab letterali che il testo del modello metteva ai due lati del riferimento,
 * producendo una sequenza di due o più caratteri di spaziatura o, se il riferimento sta
 * subito prima di un segno di punteggiatura a fine frase, un singolo spazio o tab spurio prima
 * di quella punteggiatura. Per rendere utilizzabili queste alternative volutamente vuote
 * senza obbligare ogni modello a trattarle come caso speciale, ogni sequenza di due o più
 * spazi e/o tab rimasta nel testo prodotto finale viene ridotta automaticamente a un solo
 * spazio, e ogni spazio o tab rimasto subito prima di un segno di punteggiatura viene tolto,
 * una volta sola, dopo che tutte le sostituzioni di post-produzione sono state applicate (vedi
 * {@link #postProduci}).
 * <p>
 * <b>Le produzioni monouso rendono con stato una sequenza di chiamate a {@code produci()},
 * e il chiamante decide dove finisce una sequenza e dove comincia la successiva chiamando
 * {@link #ripristina()}.</b> Non esiste un'unica politica giusta, perché le due cose che una
 * grammatica può volere sono opposte:
 * <ul>
 *     <li><b>Risultati indipendenti.</b> Ogni chiamata deve reggersi da sola e attingere
 *     all'insieme completo delle alternative: una fiaba per ogni visita alla locanda, per
 *     dire. Si chiama {@link #ripristina()} dopo ogni {@code produci()}. Senza di esso i serbatoi
 *     monouso continuano a svuotarsi di chiamata in chiamata, e i risultati diventano via via
 *     più poveri fino a fallire del tutto.</li>
 *     <li><b>Una serie di risultati volutamente distinti.</b> Generare, ad esempio, cinque
 *     personaggi diversi che non devono ripetersi è esattamente lo scopo di una produzione
 *     monouso: <b>non</b> si fa il reset fra l'uno e l'altro, o potrebbero ripetersi. Si fa il
 *     reset solo quando si inizia una nuova serie.</li>
 * </ul>
 * Il modo in cui il secondo caso fallisce merita di essere conosciuto, perché arriva tardi e
 * il suo messaggio nomina una produzione innocente: una volta consumata l'ultima alternativa
 * di una produzione monouso, la produzione viene rimossa e la cascata in
 * {@link #rimuoviProduzione} elimina ogni alternativa che la referenzia, rimuovendo
 * ricorsivamente tutto ciò che resta vuoto. Una chiamata successiva muore allora con
 * {@code IllegalArgumentException: Production X is empty!}, dove {@code X} può trovarsi
 * parecchi livelli sopra il serbatoio che si è davvero esaurito. La lunghezza di una serie è
 * quindi limitata dal più piccolo serbatoio monouso da cui attinge: vedi
 * {@link #MARCATORE_MONOUSO} e {@link #scegliAlternativa}.
 * <p>
 * <b>Questa classe non è thread-safe.</b> Un'istanza deve restare confinata a un singolo
 * thread. Il punto delicato è che {@link #produci()} sembra una lettura ma non lo è: consuma
 * le alternative monouso da {@link #produzioniCorrenti} (a cascata attraverso
 * {@link #rimuoviProduzione}), riempie e poi svuota {@link #fissateGlobali}, e
 * attinge dalla sorgente condivisa {@link Dado#sorgente()}. Due thread che chiamano
 * {@code produci()} sulla stessa istanza possono quindi perdersi a vicenda i valori fissati,
 * consumare due volte la stessa alternativa monouso, o lasciare la mappa delle produzioni
 * potata a metà. {@link #ripristina()}, {@link #fissaProduzione}, {@link #setRadice} e
 * {@link #setModalita} modificano lo stesso stato e sono ugualmente non protetti.
 * <p>
 * Ciò che <i>è</i> sicuro è condividere il <i>sorgente</i> della grammatica: tutto ciò che
 * viene analizzato al caricamento ({@link #mappaProduzioni}, compresi i pesi incorporati da
 * {@link #aumentaIPesiPerIDiscendenti}, e {@link #sostituzioniFinali}) non viene più scritto
 * dopo che il costruttore è ritornato. Quindi il modo di produrre testo da più di un thread
 * è dare a ciascun thread il proprio {@code GestoreGrammatiche} costruito dallo stesso file, non
 * sincronizzare l'accesso a una sola istanza condivisa.
 */
public class GestoreGrammatiche {

	/**
	 * Apre il testo di un file incluso con {@code #include <file>}. La grammatica conosce solo il nome scritto nella
	 * direttiva, relativo alla cartella del file che lo include; dove sia quella cartella, e come si legga un file, lo
	 * decide il risolutore (vedi {@link #daRisorsa}).
	 */
	@FunctionalInterface
	public interface RisolutoreDelleInclusioni {
		/**
		 * @param path il percorso del file, relativo alla cartella della grammatica principale: usa {@code /} come separatore
		 *             e non inizia mai con esso, né contiene segmenti {@code .}, {@code ..} o vuoti
		 * @return il testo del file, o {@code null} se non esiste
		 * @throws IOException se il file esiste ma non si può aprire
		 */
		InputStream apri(String percorso) throws IOException;

		/**
		 * Un nome che distingue questo file da ogni altro in tutto il programma, così che {@code #include_static} possa
		 * condividerne le produzioni fra le grammatiche che lo includono. Il valore predefinito è null: il risolutore non
		 * supporta le sezioni statiche. {@link #daRisorsa} dà l'URL della risorsa.
		 * @param path lo stesso percorso di {@link #apri}
		 * @return l'identità del file, o null se il risolutore non ne può dare una
		 */
		default String identita(String percorso) {
			return null;
		}
	}

	private static final String PRODUZIONE = "Production ";
	private static final String RIGA = "Line ";
	/**
	 * Prefisso che segna un commento a riga intera nei file sorgente di grammatica e di post-produzione.
	 */
	private static final String PREFISSO_COMMENTO = "//";
	/**
	 * Con cosa inizia una riga di direttiva, nella prima colonna. Una riga che inizia così e non è una direttiva nota
	 * è un errore: un commento inizia con {@link #PREFISSO_COMMENTO}.
	 */
	private static final String PREFISSO_DIRETTIVA = "#";
	/**
	 * Direttiva che include un altro file, in stile C: {@code #include <file>} su una riga a sé, a partire dalla
	 * prima colonna. Vedi {@link RisolutoreDelleInclusioni}.
	 */
	private static final String DIRETTIVA_INCLUDE = "#include";
	/**
	 * Direttiva che include un file come <i>sezione statica</i>: {@code #include_static <file>}. Le sue produzioni sono
	 * condivise da ogni grammatica che la include (vedi {@link #ripristinaLeSezioniStatiche()}).
	 */
	private static final String DIRETTIVA_INCLUDE_STATIC = "#include_static";
	/**
	 * Quanti file possono includersi a vicenda, uno dentro l'altro: ferma le inclusioni senza fine.
	 */
	private static final int MASSIMA_PROFONDITA_INCLUSIONI = 10;
	/**
	 * Suffisso che segna una produzione come monouso (rimossa una volta usata nel corso di un ciclo). Un solo
	 * carattere anziché {@code {1}}, per evitare confusione visiva con i gruppi di alternative in linea
	 * ({@link #GRAFFA_APERTA}/{@link #GRAFFA_CHIUSA}), che usano le stesse parentesi graffe.
	 */
	private static final String MARCATORE_MONOUSO = "$";
	/**
	 * Marcatore per le produzioni che derivano i loro valori possibili anche da altre produzioni.
	 */
	private static final String MARCATORE_DERIVATA = "=";

	private static final String QUADRA_APERTA = "[";
	private static final String QUADRA_CHIUSA = "]";
	/**
	 * Caratteri che delimitano un gruppo di alternative in linea in una foglia ({@code {opz1|opz2|...}}).
	 */
	private static final char GRAFFA_APERTA = '{';
	private static final char GRAFFA_CHIUSA = '}';
	/**
	 * Carattere che delimita un tratto letterale grezzo in una foglia ({@code "..."}), dentro il quale
	 * {@link #GRAFFA_APERTA}, {@link #GRAFFA_CHIUSA} e {@code |} sono trattati come testo semplice.
	 */
	private static final char VIRGOLETTA = '"';
	/**
	 * Carattere che mette in escape un {@link #VIRGOLETTA}, un {@link #QUADRA_APERTA} o un
	 * {@link #QUADRA_CHIUSA} dentro un tratto letterale grezzo, così che sia trattato come testo semplice.
	 */
	private static final char CARATTERE_ESCAPE = '\\';
	/**
	 * Caratteri che fanno le veci di un {@code [} e un {@code ]} letterali, scritti {@code \[} e
	 * {@code \]} dentro un tratto letterale grezzo. Sono messi al loro posto da {@link #risolviVirgolette} al
	 * caricamento della grammatica e riconvertiti in vere parentesi da {@link #postProduci}, una volta che ogni
	 * elemento è stato espanso: è ciò che rende una parentesi in escape invisibile agli scanner degli elementi
	 * ({@link #controllaElementi}, {@link #raccogliProduzioniCitate},
	 * {@link #produciDalNodo(String, Map, Map, String)}), che cercano tutti una {@code [} nuda.
	 * Si usano caratteri dell'area a uso privato, così che nessun testo plausibile di grammatica possa
	 * scontrarsi con essi; un file sorgente che ne contenga uno viene rifiutato senza appello (vedi
	 * {@link #leggiSorgenteECreaProduzioni}). Devono restare sopra {@code U+0020},
	 * perché l'espansione di ogni produzione passa per {@link String#trim()}, che altrimenti
	 * toglierebbe un segnaposto posto a uno dei suoi bordi.
	 */
	private static final char SEGNAPOSTO_QUADRA_APERTA = '\uE000';
	private static final char SEGNAPOSTO_QUADRA_CHIUSA = '\uE001';
	/**
	 * Marcatore che, trovato come ultimo carattere di una riga, significa che la riga non è
	 * ancora finita: viene tolto e al suo posto si accoda la riga successiva.
	 */
	private static final String MARCATORE_CONTINUAZIONE = "\\";
	/**
	 * Prefisso usato per dare il nome alle produzioni generate automaticamente dai gruppi di alternative in linea.
	 */
	private static final String PREFISSO_PRODUZIONE_IN_LINEA = "PROD_";
	/**
	 * Marcatore di una produzione il cui risultato è fissato globalmente, per l'intera chiamata a produce().
	 */
	private static final String MARCATORE_FISSATA_GLOBALE = "*";
	/**
	 * Marcatore di una produzione il cui risultato è fissato localmente, all'interno dell'albero di produzione corrente.
	 */
	private static final String MARCATORE_FISSATA_LOCALE = "!";
	/**
	 * Marcatore che assegna un valore letterale a una chiave nella cache globale delle produzioni
	 * fissate (chiave=valore).
	 */
	private static final String MARCATORE_ASSEGNAZIONE = "=";
	/**
	 * Marcatore che referenzia il valore di una chiave assegnata in precedenza nella cache globale
	 * delle produzioni fissate.
	 */
	private static final String MARCATORE_RIFERIMENTO = "#";
	/**
	 * Marcatore che precede l'elemento di peso {@code [^N]} iniziale di un'alternativa (ad esempio
	 * {@code [^10] del testo} o {@code [^2.5] del testo}), usato per orientare la scelta casuale
	 * verso di essa; vedi {@link #leggiPeso}. Risolto e tolto al caricamento della grammatica,
	 * a differenza di {@link #MARCATORE_ASSEGNAZIONE}/{@link #MARCATORE_RIFERIMENTO}, che sono risolti
	 * in modo pigro al momento della produzione.
	 */
	private static final String MARCATORE_PESO = "^";
	/**
	 * Marcatore che mette in maiuscolo la prima lettera del valore risolto di un riferimento quando si trova
	 * immediatamente prima della sua parentesi di apertura, fuori da essa (ad esempio {@code ^[Nome]}). Stesso
	 * carattere di {@link #MARCATORE_PESO} ma in posizione speculare (prima della parentesi
	 * anziché subito dentro), così i due non occorrono mai nella stessa posizione di carattere
	 * e non si possono confondere. Risolto al momento della produzione, in
	 * {@link #produciDalNodo(String, Map, Map, String)}.
	 */
	private static final String MARCATORE_MAIUSCOLA = "^";
	/**
	 * Marcatore che rende un riferimento tollerante a un valore mancante: scritto subito dopo il nome
	 * referenziato e seguito da {@code |} e dal valore di ripiego
	 * ({@code [Nome? | ripiego]}), oppure da solo per un ripiego vuoto ({@code [Nome?]}). Risolto
	 * al momento della produzione, in {@link #risolviElemento}. Diverso per posizione sia da
	 * {@link #MARCATORE_PESO} (che segue la parentesi di apertura, all'inizio di
	 * un'alternativa) sia da {@link #MARCATORE_MAIUSCOLA} (che precede la parentesi di apertura), così i
	 * tre non occupano mai la stessa posizione di carattere. Un nome di produzione quindi non può
	 * terminare con questo carattere (vedi {@link #leggiIntestazione}).
	 */
	private static final String MARCATORE_PREDEFINITO = "?";
	/**
	 * Fattore di scala applicato al peso aggregato di una produzione referenziata quando si aumenta il
	 * peso di un'alternativa che la referenzia; vedi {@link #aumentaIPesiPerIDiscendenti}.
	 */
	private static final double FATTORE_DI_SCALA = 0.33;
	/**
	 * Separatore fra il testo di pre-produzione e quello di post-produzione nel file di post-produzione.
	 */
	private static final String SEPARATORE_POST_PRODUZIONE = ":";
	/**
	 * Espressione regolare che divide il testo prodotto in singole righe.
	 */
	private static final String REGEX_A_CAPO = "\\n";
	/**
	 * Espressione regolare che trova una sequenza di due o più spazi e/o tab, ridotta a un solo spazio da
	 * {@link #postProduci}.
	 */
	private static final String REGEX_SPAZI_MULTIPLI = "[ \t]{2,}";
	/**
	 * Espressione regolare che trova uno o più spazi e/o tab immediatamente prima di un segno di punteggiatura
	 * di frase/proposizione, tolti da {@link #postProduci}.
	 */
	private static final String REGEX_SPAZIO_PRIMA_DELLA_PUNTEGGIATURA = "[ \t]+(?=[.,;:!?])";
	/**
	 * Produzione radice da cui parte il processo di generazione.
	 */
	private String radice = null;
	/**
	 * Mappa di tutte le produzioni disponibili. Mantiene l'ordine di inserimento, così che iterarla segua
	 * l'ordine in cui le produzioni sono state dichiarate nel file di grammatica (con ogni produzione
	 * generata automaticamente da un gruppo di alternative in linea collocata dove è stata creata). Tre
	 * cose dipendono da questo: quale membro di un ciclo di riferimenti ottiene per primo l'aumento del peso dei
	 * discendenti (vedi {@link #aumentaIPesiPerIDiscendenti}), quale riferimento errato viene segnalato per primo
	 * quando una grammatica ne ha diversi (vedi {@link #controllaLeProduzioni}), e
	 * l'ordine in cui la cascata di una produzione monouso esaurita elimina le altre produzioni
	 * (vedi {@link #rimuoviProduzione}): con una {@link HashMap} tutte e tre dipenderebbero dall'ordine
	 * dell'hash, impossibile da prevedere leggendo il file.
	 */
	private final Map<String, List<AlternativaPesata>> mappaProduzioni = new LinkedHashMap<>();
	/**
	 * Mappa di tutte le produzioni disponibili da riusare in un ciclo di produzioni. Mantiene l'ordine di inserimento
	 * come {@link #mappaProduzioni}, da cui {@link #ripristina()} la copia, così la cascata di rimozione monouso
	 * percorre le produzioni nell'ordine di dichiarazione.
	 */
	private final Map<String, List<AlternativaPesata>> produzioniCorrenti = new ProduzioniCorrenti();
	/**
	 * Le sezioni statiche che questa grammatica include con {@code #include_static}, nell'ordine delle direttive.
	 */
	private final List<SezioneStatica> sezioniStatiche = new ArrayList<>();
	/**
	 * Vero quando questa istanza è quella che carica una sezione statica: un sorgente simile non può includere altre
	 * sezioni statiche.
	 */
	private boolean sorgenteStatica;
	/**
	 * Prefisso dei nomi dati alle produzioni delle alternative in linea: una sezione statica ha il proprio, così che
	 * i suoi nomi non incontrino mai quelli della grammatica che la include.
	 */
	private String prefissoInLinea = PREFISSO_PRODUZIONE_IN_LINEA;
	/**
	 * I nomi delle produzioni statiche già esaurite, le cui alternative sono state potate da
	 * {@link #produzioniCorrenti}.
	 */
	private final Set<String> nomiStaticiPotati = new HashSet<>();
	/**
	 * Quanto valevano {@link #epocaStatica} e {@link #versioneStatica} l'ultima volta che questa grammatica ha guardato le sezioni statiche.
	 */
	private long epocaVista;
	private long versioneVista;
	/**
	 * Mappa delle produzioni fissate
	 */
	private final Map<String, String> fissateGlobali = new HashMap<>();
	/**
	 * Mappa di tutte le produzioni che avvengono dopo che la produzione principale è terminata, per aggiustare il risultato correggendo i problemi di grammatica della lingua naturale
	 * (ad esempio, in italiano, 'a il' viene trasformato in 'al').
	 * <p>
	 * Mantiene l'ordine di inserimento, così {@link #postProduci} applica le regole nell'ordine in cui
	 * sono scritte nel file di post-produzione. Conta ogni volta che due regole possono corrispondere allo stesso
	 * tratto di testo (un {@code prima} più corto che è prefisso di uno più lungo, per dire), perché
	 * quella eseguita per prima lo consuma. Con una {@link HashMap} quell'esito
	 * dipenderebbe dall'ordine dell'hash, e un file non potrebbe esprimere una propria precedenza.
	 */
	private final Map<String, String> sostituzioniFinali = new LinkedHashMap<>();
	/**
	 * Per scegliere a caso una produzione: l'unica sorgente casuale del gioco, {@link Dado#sorgente()},
	 * così che un seme impostato con {@link Dado#impostaSeme} renda ripetibili anche i testi prodotti, e
	 * gli artefatti generati da una grammatica. Attingere direttamente da essa non consuma i
	 * tiri truccati con {@link Dado#trucca}, che restano riservati alle decisioni di gioco.
	 */
	private final Random caso = Dado.sorgente();
	/**
	 * Controlla come viene scelta l'alternativa di una produzione; vedi {@link TipoModalitaProduzioneTesti}.
	 * Il valore predefinito è {@link TipoModalitaProduzioneTesti#CASUALE}. Impostarlo a {@link TipoModalitaProduzioneTesti#PRIMO}
	 * o {@link TipoModalitaProduzioneTesti#ULTIMO} rende {@link #produci()}/{@link #produci(String)} del tutto
	 * deterministici, il che serve soprattutto nei test.
	 */
	private TipoModalitaProduzioneTesti modalita = TipoModalitaProduzioneTesti.CASUALE;
	/**
	 * Produzioni che devono essere rimosse una volta usate nel corso di un ciclo
	 */
	private final Set<String> produzioniMonouso = new HashSet<>();
	/**
	 * Contatore progressivo usato per dare il nome alle produzioni generate automaticamente dai gruppi di
	 * alternative in linea ({@link #PREFISSO_PRODUZIONE_IN_LINEA} + questo contatore).
	 */
	private int contatoreInLinea = 0;

	private final Map<String, Collection<String>> produzioniDerivate = new LinkedHashMap<>();

	/**
	 * Un frame per ogni chiamata ricorsiva attiva (non ancora ritornata) a
	 * {@link #produciDalNodo(String, Map, Map, String)}: {@code etichetta} nomina ciò che viene
	 * espanso (il nome di una produzione, o l'elemento iniziale {@code [radice]}) e
	 * {@code passi} contiene i valori successivi della variabile {@code produzione} propria
	 * di quella chiamata, uno per ogni parentesi risolta. Usato da {@link #descriviLaTraccia()} per
	 * ricostruire il percorso seguito dal motore della grammatica, nel caso venga lanciata un'eccezione
	 * durante la produzione; vedi {@link #produci()}.
	 */
	private static final class TrattoDellaTraccia {
		private final String etichetta;
		private final List<String> passi = new ArrayList<>();

		private TrattoDellaTraccia(String etichetta, String testoIniziale) {
			this.etichetta = etichetta;
			passi.add(testoIniziale);
		}
	}

	/**
	 * Il frame più interno per ultimo, così che iterare in ordine di inserimento (prima l'esterno)
	 * dia il percorso dalla radice dell'espansione fino al punto in cui si trova ora
	 * (o si trovava, quando un'eccezione l'ha interrotta). Non thread-safe, come il resto di
	 * questa classe.
	 */
	private final Deque<TrattoDellaTraccia> traccia = new ArrayDeque<>();

	/**
	 * Il testo di un'alternativa insieme al suo peso di selezione (vedi {@link #MARCATORE_PESO},
	 * {@link #leggiPeso}). Immutabile; le istanze sono condivise fra {@link #mappaProduzioni}
	 * e le copie di {@link #produzioniCorrenti} fatte da {@link #ripristina()}.
	 */
	static final class AlternativaPesata {
		private final String testo;
		private final double peso;

		private AlternativaPesata(String testo, double peso) {
			this.testo = testo;
			this.peso = peso;
		}

		String getTesto() {
			return testo;
		}
	}

	/**
	 * Da dove si leggono i file delle direttive {@code #include}; null se la grammatica è stata costruita senza.
	 */
	private RisolutoreDelleInclusioni risolutore;
	/**
	 * Per ogni riga del sorgente dopo le inclusioni, il file e la riga da cui proviene ("nomi.txt:12"); null se
	 * il sorgente non ha {@code #include}, così che i messaggi continuino a dire solo "Line N".
	 */
	private List<String> originiDelleRighe;

	/**
	 * Costruisce una grammatica da una risorsa, risolvendo allo stesso modo le sue direttive {@code #include}: un nome scritto in una
	 * direttiva è un file nella cartella della risorsa che lo contiene, o in una sua sottocartella.
	 * @param anchor classe usata per trovare le risorse, come per {@link Class#getResourceAsStream}
	 * @param risorsaGrammatica la risorsa con la grammatica, ad esempio {@code "/com/mio/pacchetto/locande.txt"}
	 * @param risorsaPostProduzione la risorsa con le sostituzioni di post-produzione, o null
	 * @throws GrammaticaMalformataException se una risorsa manca o la grammatica non è valida
	 * @throws IOException se una risorsa non si può leggere
	 */
	public static GestoreGrammatiche daRisorsa(Class<?> ancora, String risorsaGrammatica, String risorsaPostProduzione)
			throws GrammaticaMalformataException, IOException {
		String cartella = risorsaGrammatica.substring(0, risorsaGrammatica.lastIndexOf('/') + 1);
		try (InputStream grammatica = ancora.getResourceAsStream(risorsaGrammatica);
			 InputStream postProduzione = risorsaPostProduzione == null ? null : ancora.getResourceAsStream(risorsaPostProduzione)) {
			if (grammatica == null) {
				throw new GrammaticaMalformataException("Grammar resource not found: " + risorsaGrammatica);
			}
			if (risorsaPostProduzione != null && postProduzione == null) {
				throw new GrammaticaMalformataException("Post-production resource not found: " + risorsaPostProduzione);
			}
			return new GestoreGrammatiche(grammatica, postProduzione, new RisolutoreDaRisorse(ancora, cartella));
		}
	}

	/**
	 * Legge i file inclusi dal class path, nella cartella della risorsa della grammatica principale.
	 */
	private static final class RisolutoreDaRisorse implements RisolutoreDelleInclusioni {
		private final Class<?> ancora;
		private final String cartella;

		private RisolutoreDaRisorse(Class<?> ancora, String cartella) {
			this.ancora = ancora;
			this.cartella = cartella;
		}

		@Override
		public InputStream apri(String percorso) {
			return ancora.getResourceAsStream(cartella + percorso);
		}

		@Override
		public String identita(String percorso) {
			java.net.URL indirizzo = ancora.getResource(cartella + percorso);
			return indirizzo == null ? null : indirizzo.toString();
		}
	}

	/**
	 * Costruisce una grammatica da una stringa sorgente, senza sostituzioni di post-produzione.
	 * @param grammar testo sorgente della grammatica, per la sintassi vedi la documentazione della classe
	 * @throws GrammaticaMalformataException se il testo non descrive una grammatica valida
	 * @throws IOException se il testo non si può leggere
	 */
	public GestoreGrammatiche(String grammatica) throws GrammaticaMalformataException, IOException {
		if (grammatica == null) {
			throw new GrammaticaMalformataException("Grammar text cannot be null");
		}
		inizializza(grammatica, null);
	}

	/**
	 * Costruisce una grammatica da una stringa sorgente e da una stringa facoltativa di post-produzione.
	 * @param grammar testo sorgente della grammatica, per la sintassi vedi la documentazione della classe
	 * @param postProduzione il testo facoltativo (può essere {@code null}) di post-produzione, che elenca
	 *                       sostituzioni letterali {@code prima:dopo} applicate al testo prodotto
	 * @throws GrammaticaMalformataException se uno dei due testi non descrive una grammatica valida
	 * @throws IOException se uno dei due testi non si può leggere
	 */
	public GestoreGrammatiche(String grammatica, String postProduzione) throws GrammaticaMalformataException, IOException {
		if (grammatica == null) {
			throw new GrammaticaMalformataException("Grammar text cannot be null");
		}
		inizializza(grammatica, postProduzione == null ? null
				: new ByteArrayInputStream(postProduzione.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Costruisce una grammatica da un file sorgente e da un file facoltativo di post-produzione.
	 * @param grammar file sorgente della grammatica, per la sintassi vedi la documentazione della classe
	 * @param postProduzione il file facoltativo (può essere {@code null}) di post-produzione, che elenca
	 *                        sostituzioni letterali {@code prima:dopo} applicate al testo prodotto
	 * @throws GrammaticaMalformataException se uno dei due file non descrive una grammatica valida
	 * @throws IOException se uno dei due file non si può leggere
	 */
	public GestoreGrammatiche(InputStream grammatica, InputStream postProduzione) throws GrammaticaMalformataException, IOException {
		if (grammatica == null) {
			throw new GrammaticaMalformataException("Grammar source file cannot be null");
		}
		inizializza(grammatica, postProduzione);
	}

	private void inizializza(String grammatica, InputStream postProduzione) throws GrammaticaMalformataException, IOException {
		inizializza(new ByteArrayInputStream(grammatica.getBytes(StandardCharsets.UTF_8)), postProduzione);
	}

	/**
	 * Come {@link #GestoreGrammatiche(InputStream, InputStream)}, ma la grammatica può includere altri file con
	 * {@code #include <file>}: li apre il risolutore (vedi {@link RisolutoreDelleInclusioni}).
	 * @param risolutore da dove si leggono i file inclusi; null se la grammatica non ha {@code #include}
	 */
	public GestoreGrammatiche(InputStream grammatica, InputStream postProduzione, RisolutoreDelleInclusioni risolutore)
			throws GrammaticaMalformataException, IOException {
		if (grammatica == null) {
			throw new GrammaticaMalformataException("Grammar source file cannot be null");
		}
		this.risolutore = risolutore;
		inizializza(grammatica, postProduzione);
	}

	/**
	 * Costruisce la grammatica di una sezione statica (vedi {@link SezioneStatica}).
	 */
	private GestoreGrammatiche(InputStream grammatica, RisolutoreDelleInclusioni risolutore, String prefissoInLinea)
			throws GrammaticaMalformataException, IOException {
		this.sorgenteStatica = true;
		this.risolutore = risolutore;
		this.prefissoInLinea = prefissoInLinea;
		inizializza(grammatica, null);
	}

	private void inizializza(InputStream grammatica, InputStream postProduzione) throws GrammaticaMalformataException, IOException {
		leggiSorgente(grammatica);
		leggiPostProduzione(postProduzione);
		ripristina();
	}

	/**
	 * Legge e analizza il file di grammatica.
	 */
	private void leggiSorgente(InputStream flussoDiIngresso) throws GrammaticaMalformataException, IOException {
		leggiSorgenteECreaProduzioni(flussoDiIngresso);
		controllaNomiStatici();
		controllaLeProduzioni();
		aumentaIPesiPerIDiscendenti();
	}

	/**
	 * Il sorgente della grammatica con ogni {@code #include <file>} sostituito, su una riga a sé, dalle righe di quel
	 * file (che può includerne altri, fino a {@link #MASSIMA_PROFONDITA_INCLUSIONI} livelli), e ogni {@code #include_static <file>}
	 * rimosso dopo aver registrato la sua sezione statica (vedi {@link SezioneStatica}). Un file viene incluso una sola volta, ovunque
	 * lo si richieda di nuovo, così due file possono includere lo stesso terzo file e un file non può includere se stesso. Un sorgente
	 * senza direttive viene restituito com'è, e {@link #originiDelleRighe} resta null.
	 */
	private InputStream conInclusioni(InputStream flussoDiIngresso) throws IOException, GrammaticaMalformataException {
		List<String> righe = leggiRighe(flussoDiIngresso);
		boolean serveEspandere = false;
		for (String riga : righe) {
			if (riga.startsWith(PREFISSO_DIRETTIVA)) {
				serveEspandere = true;
				break;
			}
		}
		originiDelleRighe = null;
		List<String> espanse = righe;
		if (serveEspandere) {
			espanse = new ArrayList<>();
			List<String> origini = new ArrayList<>();
			espandiInclusioni(righe, "", "source", 0, new HashSet<>(), espanse, origini);
			originiDelleRighe = origini;
		}
		StringBuilder testo = new StringBuilder();
		for (String riga : espanse) {
			testo.append(riga).append('\n');
		}
		return new ByteArrayInputStream(testo.toString().getBytes(StandardCharsets.UTF_8));
	}

	private static List<String> leggiRighe(InputStream flussoDiIngresso) throws IOException {
		List<String> righe = new ArrayList<>();
		BufferedReader lettore = new BufferedReader(new InputStreamReader(flussoDiIngresso, StandardCharsets.UTF_8));
		String riga;
		while ((riga = lettore.readLine()) != null) {
			righe.add(riga);
		}
		return righe;
	}

	/**
	 * Dice se la riga è un commento a riga intera: inizia con {@link #PREFISSO_COMMENTO}.
	 */
	private static boolean isCommento(String riga) {
		return riga.startsWith(PREFISSO_COMMENTO);
	}

	/**
	 * La direttiva che una riga rappresenta, {@code "include"} o {@code "include_static"}, o null. Il nome deve essere l'intera parola
	 * dopo il {@code #}: {@code #includes} o {@code # include} non sono direttive.
	 */
	private static String direttiva(String riga) {
		if (!riga.startsWith(PREFISSO_DIRETTIVA)) {
			return null;
		}
		int fine = 1;
		while (fine < riga.length() && (Character.isLetter(riga.charAt(fine)) || riga.charAt(fine) == '_')) {
			fine++;
		}
		String nome = riga.substring(1, fine);
		return nome.equals("include") || nome.equals("include_static") ? nome : null;
	}

	/**
	 * Copia le righe in {@code uscita}, sostituendo le direttive di inclusione con le righe dei file che nominano.
	 * @param folder la cartella di questo file, relativa alla grammatica principale ("" o che termina con "/")
	 * @param name come si chiama questo file nei messaggi
	 */
	private void espandiInclusioni(List<String> righe, String cartella, String nome, int profondita, Set<String> giaIncluse,
								List<String> uscita, List<String> origini) throws IOException, GrammaticaMalformataException {
		boolean continua = false;
		for (int i = 0; i < righe.size(); i++) {
			String riga = righe.get(i);
			String dove = nome + ":" + (i + 1);
			// Una riga che continua la precedente (vedi MARCATORE_CONTINUAZIONE) non è mai una direttiva
			String direttiva = continua ? null : direttiva(riga);
			if (direttiva != null) {
				String percorso = percorsoDellInclusione(riga, direttiva, dove);
				if (direttiva.equals("include")) {
					includiFile(percorso, cartella, dove, profondita, giaIncluse, uscita, origini);
				} else {
					includiStatica(percorso, cartella, dove);
				}
			} else {
				if (!continua && riga.startsWith(PREFISSO_DIRETTIVA)) {
					throw new GrammaticaMalformataException(dove + ": Unknown directive: comments start with " + PREFISSO_COMMENTO
							+ ", and a line starting with " + PREFISSO_DIRETTIVA + " must be " + DIRETTIVA_INCLUDE
							+ " or " + DIRETTIVA_INCLUDE_STATIC);
				}
				uscita.add(riga);
				origini.add(dove);
			}
			if (continua) {
				continua = riga.endsWith(MARCATORE_CONTINUAZIONE);
			} else {
				continua = !riga.isEmpty() && !isCommento(riga) && riga.endsWith(MARCATORE_CONTINUAZIONE);
			}
		}
		if (continua) {
			throw new GrammaticaMalformataException(nome + ": Line continuation marker '" + MARCATORE_CONTINUAZIONE
					+ "' at end of file");
		}
	}

	/**
	 * Il nome fra {@code <} e {@code >} in una direttiva di inclusione.
	 */
	private static String percorsoDellInclusione(String riga, String direttiva, String dove) throws GrammaticaMalformataException {
		String direttivaScritta = PREFISSO_DIRETTIVA + direttiva;
		String resto = riga.substring(direttivaScritta.length()).trim();
		if (!resto.startsWith("<") || !resto.endsWith(">") || resto.length() < 3) {
			throw new GrammaticaMalformataException(dove + ": Malformed " + direttivaScritta + ", expected " + direttivaScritta + " <file>");
		}
		String percorso = resto.substring(1, resto.length() - 1).trim();
		if (percorso.isEmpty() || percorso.startsWith("/") || percorso.contains("\\")) {
			throw new GrammaticaMalformataException(dove + ": Invalid file name in " + direttivaScritta + ": '" + percorso
					+ "' (it must be relative, with / as separator)");
		}
		for (String segmento : percorso.split("/", -1)) {
			if (segmento.isEmpty() || segmento.equals(".") || segmento.equals("..")) {
				throw new GrammaticaMalformataException(dove + ": Invalid file name in " + direttivaScritta + ": '" + percorso
						+ "' (only the folder of the file that includes it, or its subfolders)");
			}
		}
		return percorso;
	}

	private void includiFile(String percorso, String cartella, String dove, int profondita, Set<String> giaIncluse,
							 List<String> uscita, List<String> origini) throws IOException, GrammaticaMalformataException {
		if (risolutore == null) {
			throw new GrammaticaMalformataException(dove + ": " + DIRETTIVA_INCLUDE
					+ " cannot be resolved: the grammar was built without a RisolutoreDelleInclusioni (see GestoreGrammatiche.daRisorsa)");
		}
		String percorsoCompleto = cartella + percorso;
		if (!giaIncluse.add(percorsoCompleto)) {
			return;
		}
		if (profondita >= MASSIMA_PROFONDITA_INCLUSIONI) {
			throw new GrammaticaMalformataException(dove + ": Too many nested " + DIRETTIVA_INCLUDE + " (more than "
					+ MASSIMA_PROFONDITA_INCLUSIONI + ")");
		}
		List<String> righeIncluse;
		try (InputStream inclusa = risolutore.apri(percorsoCompleto)) {
			if (inclusa == null) {
				throw new GrammaticaMalformataException(dove + ": Included file not found: " + percorsoCompleto);
			}
			righeIncluse = leggiRighe(inclusa);
		}
		if (!righeIncluse.isEmpty() && righeIncluse.get(0).startsWith("\uFEFF")) {
			righeIncluse.set(0, righeIncluse.get(0).substring(1));
		}
		espandiInclusioni(righeIncluse, percorsoCompleto.substring(0, percorsoCompleto.lastIndexOf('/') + 1), percorsoCompleto, profondita + 1,
				giaIncluse, uscita, origini);
	}

	/**
	 * Registra la sezione statica di {@code #include_static <percorso>}, caricandola la prima volta che qualcuno la chiede.
	 * Non aggiunge nulla al testo di questa grammatica: le sue produzioni vengono cercate nella sezione condivisa.
	 */
	private void includiStatica(String percorso, String cartella, String dove) throws IOException, GrammaticaMalformataException {
		if (sorgenteStatica) {
			throw new GrammaticaMalformataException(dove + ": " + DIRETTIVA_INCLUDE_STATIC
					+ " cannot be used inside a static section");
		}
		if (risolutore == null) {
			throw new GrammaticaMalformataException(dove + ": " + DIRETTIVA_INCLUDE_STATIC
					+ " cannot be resolved: the grammar was built without a RisolutoreDelleInclusioni (see GestoreGrammatiche.daRisorsa)");
		}
		String percorsoCompleto = cartella + percorso;
		String chiave = risolutore.identita(percorsoCompleto);
		if (chiave == null) {
			try (InputStream prova = risolutore.apri(percorsoCompleto)) {
				if (prova == null) {
					throw new GrammaticaMalformataException(dove + ": Included file not found: " + percorsoCompleto);
				}
			}
			throw new GrammaticaMalformataException(dove + ": " + DIRETTIVA_INCLUDE_STATIC
					+ " needs a resolver that identifies its files (see GestoreGrammatiche.daRisorsa)");
		}
		SezioneStatica sezione = caricaSezioneStatica(chiave, percorsoCompleto, dove);
		if (sezioniStatiche.contains(sezione)) {
			return;
		}
		for (String produzione : sezione.produzioni.keySet()) {
			for (SezioneStatica altra : sezioniStatiche) {
				if (altra.produzioni.containsKey(produzione)) {
					throw new GrammaticaMalformataException(dove + ": Production " + produzione + " is defined by two static sections ("
							+ altra.chiave + " and " + sezione.chiave + ")");
				}
			}
		}
		sezioniStatiche.add(sezione);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Sezioni statiche: #include_static <file>
	// ---------------------------------------------------------------------------------------------------------

	/**
	 * Protegge tutto ciò che è condiviso fra le grammatiche: {@link #SEZIONI_STATICHE}, le produzioni consumabili di ogni
	 * {@link SezioneStatica}, {@link #epocaStatica} e {@link #versioneStatica}. Un unico lock, così che due thread che
	 * producono da due grammatiche diverse non prendano mai due lock in ordine diverso.
	 */
	private static final Object LUCCHETTO_STATICO = new Object();
	/**
	 * Prefisso dei nomi delle produzioni in linea di una sezione statica, seguito dal suo numero: {@code PROD_S1_0}.
	 */
	private static final String PREFISSO_IN_LINEA_STATICO = "PROD_S";
	/**
	 * Le sezioni statiche caricate, per identità del file (vedi {@link RisolutoreDelleInclusioni#identita}).
	 */
	private static final Map<String, SezioneStatica> SEZIONI_STATICHE = new HashMap<>();
	/**
	 * Cambia con {@link #ripristinaLeSezioniStatiche()}: una grammatica che ne vede uno nuovo ricostruisce le proprie produzioni.
	 */
	private static long epocaStatica = 0;
	/**
	 * Cambia ogni volta che una produzione statica si esaurisce (o a un reset): una grammatica che ne vede uno nuovo pota ciò che
	 * dipendeva da essa.
	 */
	private static long versioneStatica = 0;

	/**
	 * Un file incluso con {@code #include_static}: analizzato una sola volta, per tutto il programma, come grammatica a sé (così non
	 * può riferirsi alle produzioni della grammatica che lo include, che invece può riferirsi alle sue). Le sue
	 * produzioni sono <b>condivise</b>: una produzione monouso consumata tramite una grammatica è consumata per ogni altra
	 * grammatica che include lo stesso file, finché non si chiama {@link GestoreGrammatiche#ripristinaLeSezioniStatiche()}.
	 * <p>
	 * Non c'è alcun rifornimento automatico: una produzione esaurita resta esaurita, e le alternative che dipendevano da essa
	 * sono rimosse, esattamente come per una normale produzione monouso (vedi {@link #rimuoviProduzione}).
	 */
	private static final class SezioneStatica {
		final String chiave;
		/**
		 * Le produzioni come caricate, pesi compresi: mai modificate dopo.
		 */
		final Map<String, List<AlternativaPesata>> produzioni;
		final Set<String> monouso;
		/**
		 * Ciò che si può ancora produrre: la copia condivisa e consumabile di {@link #produzioni}.
		 */
		final Map<String, List<AlternativaPesata>> corrente = new LinkedHashMap<>();

		SezioneStatica(String chiave, Map<String, List<AlternativaPesata>> produzioni, Set<String> monouso) {
			this.chiave = chiave;
			this.produzioni = produzioni;
			this.monouso = monouso;
			ripristinaSezione();
		}

		void ripristinaSezione() {
			corrente.clear();
			for (Map.Entry<String, List<AlternativaPesata>> voce : produzioni.entrySet()) {
				corrente.put(voce.getKey(), new ArrayList<>(voce.getValue()));
			}
		}
	}

	/**
	 * Le produzioni che una grammatica può produrre ora: le proprie, consumate man mano che la grammatica produce, e, per un nome che non è
	 * suo, le produzioni condivise delle sue sezioni statiche.
	 */
	private final class ProduzioniCorrenti extends LinkedHashMap<String, List<AlternativaPesata>> {
		private static final long serialVersionUID = 1L;

		@Override
		public List<AlternativaPesata> get(Object chiave) {
			List<AlternativaPesata> proprie = super.get(chiave);
			if (proprie != null || sezioniStatiche.isEmpty()) {
				return proprie;
			}
			synchronized (LUCCHETTO_STATICO) {
				for (SezioneStatica sezione : sezioniStatiche) {
					List<AlternativaPesata> condivise = sezione.corrente.get(chiave);
					if (condivise != null) {
						return condivise;
					}
				}
			}
			return null;
		}

		@Override
		public boolean containsKey(Object chiave) {
			return get(chiave) != null;
		}
	}

	/**
	 * Ripristina le produzioni condivise di ogni sezione statica caricata finora: ciò che era stato consumato tramite una qualsiasi grammatica
	 * torna disponibile. Ogni grammatica che include una sezione statica ricostruisce inoltre le proprie produzioni la volta
	 * successiva che viene usata (comprese quelle monouso proprie), così che nulla resti potato per un nome che è tornato. Si chiama
	 * all'inizio di una partita (vedi {@code ProduttoriDiTesti.resetProduzioni}).
	 */
	public static void ripristinaLeSezioniStatiche() {
		synchronized (LUCCHETTO_STATICO) {
			for (SezioneStatica sezione : SEZIONI_STATICHE.values()) {
				sezione.ripristinaSezione();
			}
			epocaStatica++;
			versioneStatica++;
		}
	}

	/**
	 * La sezione statica di quel file, analizzata la prima volta e condivisa poi.
	 */
	private SezioneStatica caricaSezioneStatica(String chiave, String percorsoCompleto, String dove) throws IOException, GrammaticaMalformataException {
		synchronized (LUCCHETTO_STATICO) {
			SezioneStatica caricata = SEZIONI_STATICHE.get(chiave);
			if (caricata != null) {
				return caricata;
			}
			String cartella = percorsoCompleto.substring(0, percorsoCompleto.lastIndexOf('/') + 1);
			RisolutoreDelleInclusioni risolutoreDellaSezione = risolutore;
			GestoreGrammatiche grammaticaDellaSezione;
			try (InputStream flusso = risolutore.apri(percorsoCompleto)) {
				if (flusso == null) {
					throw new GrammaticaMalformataException(dove + ": Included file not found: " + percorsoCompleto);
				}
				grammaticaDellaSezione = new GestoreGrammatiche(flusso, percorso -> risolutoreDellaSezione.apri(cartella + percorso),
						PREFISSO_IN_LINEA_STATICO + (SEZIONI_STATICHE.size() + 1) + "_");
			} catch (GrammaticaMalformataException e) {
				if (e.getMessage().startsWith(dove)) {
					throw e;
				}
				throw new GrammaticaMalformataException(dove + ": In the static section " + percorsoCompleto + ": " + e.getMessage());
			}
			SezioneStatica sezione = new SezioneStatica(chiave, grammaticaDellaSezione.mappaProduzioni, grammaticaDellaSezione.produzioniMonouso);
			SEZIONI_STATICHE.put(chiave, sezione);
			return sezione;
		}
	}

	/**
	 * Una grammatica non può definire una produzione che una delle sue sezioni statiche definisce già.
	 */
	private void controllaNomiStatici() throws GrammaticaMalformataException {
		if (sezioniStatiche.isEmpty()) {
			return;
		}
		for (String nome : mappaProduzioni.keySet()) {
			for (SezioneStatica sezione : sezioniStatiche) {
				if (sezione.produzioni.containsKey(nome)) {
					throw new GrammaticaMalformataException(PRODUZIONE + nome + " is repeated: it is also defined by the static section "
							+ sezione.chiave);
				}
			}
		}
	}

	private boolean isNomeStatico(String nome) {
		return sezioneStaticaDi(nome) != null;
	}

	private SezioneStatica sezioneStaticaDi(String nome) {
		for (SezioneStatica sezione : sezioniStatiche) {
			if (sezione.produzioni.containsKey(nome)) {
				return sezione;
			}
		}
		return null;
	}

	/**
	 * Dice se esiste una produzione con quel nome, in questa grammatica o in una delle sue sezioni statiche.
	 */
	private boolean isDefinita(String nome) {
		return mappaProduzioni.containsKey(nome) || isNomeStatico(nome);
	}

	/**
	 * Sceglie un'alternativa di una produzione statica, consumandola se la produzione è monouso. Quando la produzione è
	 * esaurita viene rimossa dalle produzioni condivise insieme ai suoi dipendenti, e questa grammatica pota le proprie
	 * alternative che la usavano. Va chiamato tenendo {@link #LUCCHETTO_STATICO}.
	 */
	String produzioneStatica(String produzione) {
		SezioneStatica sezione = sezioneStaticaDi(produzione);
		List<AlternativaPesata> alternative = sezione.corrente.get(produzione);
		// Una produzione esaurita viene tolta dalle condivise (vedi sotto): se un altro thread la esaurisce fra il controllo di
		// chi ci chiama e questo punto, la lista non c'è più
		if (alternative == null) {
			throw new IllegalArgumentException(PRODUZIONE + produzione + " is empty!");
		}
		AlternativaPesata scelta = scegli(alternative);
		if (sezione.monouso.contains(produzione)) {
			alternative.remove(scelta);
			if (alternative.isEmpty()) {
				rimuoviProduzione(sezione.corrente, produzione);
				versioneStatica++;
				allineaStatiche();
				versioneVista = versioneStatica;
			}
		}
		return scelta.testo;
	}

	/**
	 * Toglie dalle produzioni proprie di questa grammatica le alternative che richiedono una produzione statica non più
	 * disponibile, insieme a ciò che dipendeva da esse (vedi {@link #rimuoviProduzione}). Va chiamato tenendo
	 * {@link #LUCCHETTO_STATICO}.
	 */
	private void allineaStatiche() {
		for (SezioneStatica sezione : sezioniStatiche) {
			for (String nome : sezione.produzioni.keySet()) {
				if (!sezione.corrente.containsKey(nome) && nomiStaticiPotati.add(nome)) {
					rimuoviProduzione(produzioniCorrenti, nome);
				}
			}
		}
	}

	/**
	 * Aggiorna questa grammatica rispetto alle sezioni statiche, prima di produrre: se sono state resettate ricostruisce le proprie
	 * produzioni, e se qualcosa è stato esaurito da un'altra grammatica pota ciò che ne dipendeva.
	 */
	private void aggiornaStatiche() {
		if (sezioniStatiche.isEmpty()) {
			return;
		}
		synchronized (LUCCHETTO_STATICO) {
			if (epocaVista != epocaStatica) {
				ricostruisciProduzioniCorrenti();
			} else if (versioneVista != versioneStatica) {
				allineaStatiche();
				versioneVista = versioneStatica;
			}
		}
	}

	/**
	 * Dove si trova una riga del sorgente, per i messaggi di errore: "Line N", più il file e la riga da cui proviene quando il
	 * sorgente include altri file ("Line 120 (nomi.txt:12)").
	 */
	String posizione(int numeroDiRiga) {
		if (originiDelleRighe == null || numeroDiRiga < 1 || numeroDiRiga > originiDelleRighe.size()) {
			return RIGA + numeroDiRiga;
		}
		return RIGA + numeroDiRiga + " (" + originiDelleRighe.get(numeroDiRiga - 1) + ")";
	}

	/**
	 * Legge il file di grammatica riga per riga, popolando {@link #mappaProduzioni}: ogni riga
	 * non rientrata inizia una nuova produzione (vedi {@link #leggiIntestazione}), ogni riga
	 * rientrata con TAB/spazio aggiunge figli alla produzione corrente (vedi
	 * {@link #leggiFigli}). I commenti e le righe vuote vengono saltati. Una riga che termina con
	 * {@link #MARCATORE_CONTINUAZIONE} non è ancora finita: il marcatore viene tolto e
	 * la riga seguente viene accodata al suo posto, ripetendo finché non si trova una riga che non
	 * termina con il marcatore; raggiungere la fine del file mentre si attende ancora
	 * una continuazione è un errore.
	 * @throws GrammaticaMalformataException se il file termina con un marcatore di continuazione di riga pendente
	 */
	private void leggiSorgenteECreaProduzioni(InputStream flussoDiIngresso) throws IOException, GrammaticaMalformataException {
		try (BufferedReader lettore = new BufferedReader(new InputStreamReader(conInclusioni(flussoDiIngresso), StandardCharsets.UTF_8))) {
			String produzioneCorrente = null;
			int numeroDiRiga = 0;
			String riga;
			while ((riga = lettore.readLine()) != null) {
				numeroDiRiga++;

				if (riga.isEmpty() || isCommento(riga)) {
					continue;
				}

				while (riga.endsWith(MARCATORE_CONTINUAZIONE)) {
					String rigaSeguente = lettore.readLine();
					if (rigaSeguente == null) {
						throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Line continuation marker '" + MARCATORE_CONTINUAZIONE + "' at end of file");
					}
					numeroDiRiga++;
					riga = riga.substring(0, riga.length() - MARCATORE_CONTINUAZIONE.length()) + rigaSeguente;
				}

				if (riga.indexOf(SEGNAPOSTO_QUADRA_APERTA) >= 0
						|| riga.indexOf(SEGNAPOSTO_QUADRA_CHIUSA) >= 0) {
					throw new GrammaticaMalformataException(posizione(numeroDiRiga)
							+ ": Reserved placeholder character found; use '" + CARATTERE_ESCAPE + QUADRA_APERTA
							+ "' inside a quoted span for a literal bracket");
				}

				boolean isIntestazione = !riga.startsWith("\t") && !riga.startsWith(" ");

				if (isIntestazione) {
					controllaProduzionePrecedente(numeroDiRiga, produzioneCorrente);
					produzioneCorrente = leggiIntestazione(numeroDiRiga, riga);
				} else {
					if (produzioneCorrente == null) {
						throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Missing parent production. Lines beginning with TAB or space must be preceded by a production.");
					}
					leggiFigli(riga, numeroDiRiga, produzioneCorrente);
				}
			}
			controllaProduzionePrecedente(numeroDiRiga, produzioneCorrente);
		}

		risolviDerivate();
	}

	/**
	 * Dà a ogni produzione derivata ({@code A = B C}) le alternative delle produzioni da cui deriva, oltre alle sue. Le
	 * derivazioni possono fare catena ({@code A = B} e {@code B = C}): prima di copiare le alternative di {@code B} in
	 * {@code A} si risolve {@code B}, così {@code A} riceve anche quelle di {@code C}, in qualunque ordine siano scritte nel
	 * file. Un ciclo ({@code A = B} e {@code B = A}) non avrebbe fine ed è un errore.
	 */
	private void risolviDerivate() throws GrammaticaMalformataException {
		Set<String> risolto = new HashSet<>();
		for (String derivata : produzioniDerivate.keySet()) {
			risolviDerivata(derivata, risolto, new LinkedHashSet<>());
		}
	}

	// inCorso: le derivate che si stanno risolvendo, dalla prima a quella corrente (per riconoscere un ciclo e per dirlo)
	private void risolviDerivata(String derivata, Set<String> risolto, LinkedHashSet<String> inCorso)
			throws GrammaticaMalformataException {
		if (risolto.contains(derivata)) {
			return;
		}
		if (!inCorso.add(derivata)) {
			List<String> ciclo = new ArrayList<>(inCorso);
			ciclo = ciclo.subList(ciclo.indexOf(derivata), ciclo.size());
			throw new GrammaticaMalformataException("Productions " + String.join(", ", ciclo) + " are mutually derived: "
					+ String.join(" -> ", ciclo) + " -> " + derivata);
		}
		for (String sorgenteDerivata : produzioniDerivate.get(derivata)) {
			List<AlternativaPesata> alternativeDellaSorgente = mappaProduzioni.get(sorgenteDerivata);
			if (alternativeDellaSorgente == null) {
				throw new GrammaticaMalformataException("Production " + derivata + " refers to an undeclared production: " + sorgenteDerivata);
			}
			if (produzioniDerivate.containsKey(sorgenteDerivata)) {
				risolviDerivata(sorgenteDerivata, risolto, inCorso);
			}
			mappaProduzioni.get(derivata).addAll(alternativeDellaSorgente);
		}
		inCorso.remove(derivata);
		risolto.add(derivata);
	}

	/**
	 * Verifica che la produzione appena chiusa dall'arrivo di una nuova intestazione di produzione
	 * (o dalla fine del file) abbia almeno un figlio, dato che una produzione che non produce
	 * nulla farebbe fallire la generazione più avanti. Viene chiamato con la produzione che era
	 * ancora in corso di riempimento, così che ogni produzione del file sia controllata esattamente
	 * una volta: quando si legge l'intestazione che la chiude oppure, per l'ultima, quando si
	 * raggiunge la fine del file.
	 * @throws GrammaticaMalformataException se {@code produzionePrecedente} non ha alcun figlio
	 */
	private void controllaProduzionePrecedente(int numeroDiRiga, String produzionePrecedente) throws GrammaticaMalformataException {
		if (produzionePrecedente != null) {
			List<AlternativaPesata> figliPrecedenti = mappaProduzioni.get(produzionePrecedente);
			if (figliPrecedenti.isEmpty() && produzioniDerivate.get(produzionePrecedente) == null) {
				throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Production " + produzionePrecedente + " has no children.");
			}
		}
	}

	/**
	 * Registra una nuova riga di intestazione di produzione: toglie il suffisso {@link #MARCATORE_MONOUSO}
	 * se presente (segnando la produzione come monouso), la imposta come
	 * {@link #radice} se è la prima produzione trovata, e la aggiunge (inizialmente
	 * senza figli) a {@link #mappaProduzioni}. Se la produzione deriva da altre,
	 * controlla che non ci siano derivazioni da sé, reciproche o ripetute.
	 *
	 * @return il nome semplice della produzione (senza il marcatore monouso)
	 * @throws GrammaticaMalformataException se una produzione con lo stesso nome è già stata dichiarata
	 */
	private String leggiIntestazione(int numeroDiRiga, String riga) throws GrammaticaMalformataException {
		String produzioneCorrente = riga.trim();
		if (produzioneCorrente.contains(MARCATORE_DERIVATA)) {
			String sorgentiScritte = produzioneCorrente.substring(produzioneCorrente.indexOf(MARCATORE_DERIVATA) + MARCATORE_DERIVATA.length());
			// Ripulisce le produzioni da cui si deriva
			sorgentiScritte = sorgentiScritte.replaceAll("\\s+", " ").trim();

			produzioneCorrente = produzioneCorrente.substring(0, produzioneCorrente.indexOf(MARCATORE_DERIVATA)).trim();

			if (!sorgentiScritte.isEmpty()) {
				Collection<String> sorgentiDellaDerivata = produzioniDerivate.computeIfAbsent(produzioneCorrente, k -> new LinkedHashSet<>());
				for (String sorgenteDellaDerivata : sorgentiScritte.split(" ")) {
					if (sorgenteDellaDerivata.equals(produzioneCorrente)) {
						throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Production " + produzioneCorrente + " derives from itself");
					}
					if (sorgentiDellaDerivata.contains(sorgenteDellaDerivata)) {
						throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Production " + sorgenteDellaDerivata + " is repeated more than once");
					}
					sorgentiDellaDerivata.add(sorgenteDellaDerivata);
				}
			}
		}

		if (produzioneCorrente.endsWith(MARCATORE_MONOUSO)) {
			produzioneCorrente = produzioneCorrente.substring(0, produzioneCorrente.length() - MARCATORE_MONOUSO.length());
			if (Logger.isAttivo()) {
				Logger.log("One-shot production: " + produzioneCorrente);
			}
			produzioniMonouso.add(produzioneCorrente);
		}
		if (produzioneCorrente.endsWith(MARCATORE_PREDEFINITO)) {
			throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Production name " + produzioneCorrente
					+ " may not end with '" + MARCATORE_PREDEFINITO + "', which marks a reference's default value");
		}
		if (radice == null) {
			radice = produzioneCorrente;
		}
		if (mappaProduzioni.containsKey(produzioneCorrente)) {
			throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Production " + produzioneCorrente + " is repeated.");
		}
		mappaProduzioni.put(produzioneCorrente, new ArrayList<>());
		return produzioneCorrente;
	}

	/**
	 * Aggiunge le alternative trovate su una riga rientrata come figli di {@code produzioneCorrente}.
	 * La riga viene prima divisa in alternative di primo livello su {@code |}
	 * (ignorando ogni {@code |} che si trovi dentro un gruppo di alternative in linea {@code {...}}, vedi
	 * {@link #dividiAlternative}); poi, per ogni alternativa, si analizza e si toglie l'eventuale
	 * elemento di peso {@code [^N]} iniziale (vedi {@link #leggiPeso}) prima che i suoi gruppi di
	 * alternative in linea siano espansi in produzioni generate automaticamente (vedi {@link #espandiAlternativeInLinea}).
	 * @throws GrammaticaMalformataException se la riga non è preceduta da un'intestazione di produzione,
	 *                                  contiene un gruppo di alternative in linea malformato o un
	 *                                  elemento di peso malformato
	 */
	private void leggiFigli(String riga, int rigaCorrente, String produzioneCorrente) throws GrammaticaMalformataException {
		List<AlternativaPesata> figliDellaProduzione = mappaProduzioni.get(produzioneCorrente);
		for (String figlio : dividiAlternative(riga, rigaCorrente)) {
			AlternativaPesata pesata = leggiPeso(figlio.trim(), rigaCorrente);
			figliDellaProduzione.add(new AlternativaPesata(espandiAlternativeInLinea(pesata.testo, rigaCorrente), pesata.peso));
		}
	}

	/**
	 * Analizza e toglie da {@code figlioRipulito} un eventuale elemento di peso {@code [^N]} iniziale,
	 * in modo che questa alternativa sia scelta {@code N} volte più spesso di una con il
	 * peso predefinito di 1 (vedi {@link #scegliAlternativa}). {@code N} può essere frazionario (ad esempio
	 * {@code [^2.5]}). Un'alternativa senza tale elemento ha peso 1.
	 * @return una {@link AlternativaPesata} con il testo rimanente e il peso analizzato
	 *         (oppure peso 1 e il testo intatto, se non c'è alcun elemento di peso)
	 * @throws GrammaticaMalformataException se un elemento di peso è aperto ma non chiuso, o il suo
	 *                                  valore non è un numero positivo
	 */
	private AlternativaPesata leggiPeso(String figlioRipulito, int numeroDiRiga) throws GrammaticaMalformataException {
		String inizioDelPeso = QUADRA_APERTA + MARCATORE_PESO;
		if (!figlioRipulito.startsWith(inizioDelPeso)) {
			return new AlternativaPesata(figlioRipulito, 1);
		}
		int indiceQuadraChiusa = figlioRipulito.indexOf(QUADRA_CHIUSA);
		if (indiceQuadraChiusa < 0) {
			throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Missing '" + QUADRA_CHIUSA + "' for weight token");
		}
		String testoDelPeso = figlioRipulito.substring(inizioDelPeso.length(), indiceQuadraChiusa);
		double peso;
		try {
			peso = Double.parseDouble(testoDelPeso);
		} catch (NumberFormatException e) {
			throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Invalid weight '" + testoDelPeso + "': must be a positive number");
		}
		if (!(peso > 0) || Double.isInfinite(peso)) {
			throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Invalid weight " + testoDelPeso + ": must be a positive number");
		}
		return new AlternativaPesata(figlioRipulito.substring(indiceQuadraChiusa + 1).trim(), peso);
	}

	/**
	 * Divide una riga rientrata nelle sue alternative di primo livello su {@code |},
	 * trattando ogni {@code |} che si trovi fra una graffa di apertura e la corrispondente graffa di chiusura come parte di
	 * un gruppo di alternative in linea e non come separatore di alternative, così che una riga come
	 * <code>A {a|b} | C</code> sia divisa in due pezzi (<code>A {a|b} </code> e <code> C</code>),
	 * non in quattro. Allo stesso modo un {@code |} fra una parentesi quadra di apertura e la corrispondente
	 * parentesi di chiusura fa parte di un elemento e non è un separatore, ed è ciò che permette a un elemento di portare un
	 * valore predefinito (<code>[Nome? | ripiego]</code>, vedi {@link #dividiPredefinito}) o
	 * un'assegnazione il cui valore contiene un {@code |} letterale. Un {@code |} (o {@code {}}/{@code }}})
	 * dentro un tratto letterale grezzo ({@code "..."}) non è mai trattato nemmeno come separatore;
	 * virgolette ed escape qui sono lasciate intatte (vengono risolte più tardi, in
	 * {@link #espandiAlternativeInLinea}).
	 * @throws GrammaticaMalformataException se un tratto letterale grezzo è aperto ma mai chiuso
	 */
	private List<String> dividiAlternative(String riga, int numeroDiRiga) throws GrammaticaMalformataException {
		List<String> alternative = new ArrayList<>();
		int inizio = 0;
		Scansione scansione = new Scansione(riga, 0);
		while (scansione.avanza()) {
			if (scansione.isGraffaSpaiata()) {
				throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": '" + GRAFFA_CHIUSA + "' without a matching '"
						+ GRAFFA_APERTA + "': quote it (\"" + GRAFFA_CHIUSA + "\") to emit it as literal text");
			}
			if (scansione.isAlPrimoLivello() && scansione.carattere() == '|') {
				alternative.add(riga.substring(inizio, scansione.indice()));
				inizio = scansione.indice() + 1;
			}
		}
		if (scansione.isFraVirgolette()) {
			throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Missing closing '" + VIRGOLETTA + "'");
		}
		alternative.add(riga.substring(inizio));
		return alternative;
	}

	/**
	 * Espande ogni gruppo di alternative in linea di primo livello (<code>{opz1|opz2|...}</code>) trovato in
	 * {@code figlio} in un riferimento a una produzione nuova con nome generato automaticamente (vedi
	 * {@link #creaProduzioneInLinea}), così che il testo restituito possa da lì in poi essere trattato esattamente come
	 * ogni altra foglia. Il contenuto di un gruppo può a sua volta contenere altri gruppi annidati (ad esempio
	 * <code>{a|{b|c}}</code>); questi sono espansi ricorsivamente da {@link #creaProduzioneInLinea} nelle
	 * loro produzioni al volo prima che il gruppo esterno sia registrato.
	 * @throws GrammaticaMalformataException se una graffa di apertura non ha la corrispondente di chiusura,
	 *                                  se un tratto letterale grezzo è aperto ma mai chiuso, o se la
	 *                                  produzione in linea risultante sarebbe vuota
	 */
	private String espandiAlternativeInLinea(String figlio, int numeroDiRiga) throws GrammaticaMalformataException {
		StringBuilder risultato = new StringBuilder();
		int indice = 0;
		int indiceGraffaAperta;
		while ((indiceGraffaAperta = trovaGraffaApertaFuoriDalleVirgolette(figlio, indice)) >= 0) {
			int indiceGraffaChiusa = trovaGraffaChiusa(figlio, indiceGraffaAperta, numeroDiRiga);
			risultato.append(risolviVirgolette(figlio.substring(indice, indiceGraffaAperta)));
			String testoDelGruppo = figlio.substring(indiceGraffaAperta + 1, indiceGraffaChiusa);
			String nomeInLinea = creaProduzioneInLinea(testoDelGruppo, numeroDiRiga);
			risultato.append(QUADRA_APERTA).append(nomeInLinea).append(QUADRA_CHIUSA);
			indice = indiceGraffaChiusa + 1;
		}
		risultato.append(risolviVirgolette(figlio.substring(indice)));
		return risultato.toString();
	}

	/**
	 * Scorre un testo di grammatica un carattere alla volta e dice, per ognuno, se è sintassi o testo. Un carattere è testo
	 * se sta dentro un tratto letterale grezzo ({@code "..."}, virgolette comprese, con i suoi escape {@code \"},
	 * {@code \[} e {@code \]}); altrimenti è sintassi, e la scansione tiene il conto delle graffe ({@code {...}}, i gruppi
	 * di alternative in linea) e delle quadre ({@code [...]}, i riferimenti) aperte. Così ogni metodo che cerca un
	 * separatore o una parentesi segue le stesse regole.
	 * <p>
	 * Si usa così: {@code while (scansione.avanza()) { ... scansione.carattere() ... }}. Dopo {@link #avanza()} i conti
	 * comprendono già il carattere corrente: su una graffa di apertura, le graffe aperte sono già una di più.
	 */
	private static final class Scansione {
		private final String testo;
		// Il carattere corrente, e quello da cui riparte avanza() (due più in là dopo un escape)
		private int indice;
		private int prossimo;
		private boolean fraVirgolette;
		private boolean letterale;
		private int graffe;
		private int quadre;
		private boolean graffaSpaiata;

		Scansione(String testo, int da) {
			this.testo = testo;
			this.prossimo = da;
		}

		/**
		 * Passa al carattere successivo; false a fine testo.
		 */
		boolean avanza() {
			if (prossimo >= testo.length()) {
				return false;
			}
			indice = prossimo;
			prossimo = indice + 1;
			graffaSpaiata = false;
			char c = testo.charAt(indice);
			if (fraVirgolette) {
				letterale = true;
				if (eLEscapeDi(testo, indice, VIRGOLETTA) || eLEscapeDi(testo, indice, QUADRA_APERTA.charAt(0))
						|| eLEscapeDi(testo, indice, QUADRA_CHIUSA.charAt(0))) {
					// L'escape e il carattere che protegge sono un solo carattere di testo
					prossimo++;
				} else if (c == VIRGOLETTA) {
					fraVirgolette = false;
				}
				return true;
			}
			if (c == VIRGOLETTA) {
				fraVirgolette = true;
				letterale = true;
				return true;
			}
			letterale = false;
			if (c == GRAFFA_APERTA) {
				graffe++;
			} else if (c == GRAFFA_CHIUSA) {
				if (graffe == 0) {
					graffaSpaiata = true;
				} else {
					graffe--;
				}
			} else if (c == QUADRA_APERTA.charAt(0)) {
				quadre++;
			} else if (c == QUADRA_CHIUSA.charAt(0)) {
				quadre = Math.max(0, quadre - 1);
			}
			return true;
		}

		/** La posizione del carattere corrente nel testo. */
		int indice() {
			return indice;
		}

		/** Il carattere corrente. */
		char carattere() {
			return testo.charAt(indice);
		}

		/** Se il carattere corrente è sintassi, cioè non sta in un tratto letterale grezzo. */
		boolean isSintassi() {
			return !letterale;
		}

		/** Se il carattere corrente è sintassi e sta fuori da ogni graffa e da ogni quadra. */
		boolean isAlPrimoLivello() {
			return !letterale && graffe == 0 && quadre == 0;
		}

		/** Quante graffe sono aperte, compresa quella del carattere corrente. */
		int graffeAperte() {
			return graffe;
		}

		/** Se il carattere corrente è una graffa di chiusura senza una di apertura prima. */
		boolean isGraffaSpaiata() {
			return graffaSpaiata;
		}

		/** Se un tratto letterale grezzo è ancora aperto (a fine testo vuol dire che non è stato chiuso). */
		boolean isFraVirgolette() {
			return fraVirgolette;
		}
	}

	/**
	 * Dice se in {@code testo[i]} c'è un escape: un {@link #CARATTERE_ESCAPE} seguito da un altro carattere (come ultimo carattere
	 * del testo è un carattere qualunque).
	 */
	static boolean eUnEscape(String testo, int i) {
		return testo.charAt(i) == CARATTERE_ESCAPE && i + 1 < testo.length();
	}

	/**
	 * Dice se in {@code testo[i]} c'è l'escape proprio di {@code protetto}, per esempio {@code \"} o {@code \[}.
	 */
	static boolean eLEscapeDi(String testo, int i, char protetto) {
		return eUnEscape(testo, i) && testo.charAt(i + 1) == protetto;
	}

	/**
	 * Trova l'indice della prossima {@link #GRAFFA_APERTA} a partire da {@code daIndice} (compreso) che non si trovi
	 * dentro un tratto letterale grezzo ({@code "..."}), così che ad esempio in <code>"a {b} c"</code> l'intero
	 * testo (graffa compresa) sia trattato come letterale, non come gruppo di alternative in linea.
	 * @return l'indice della prossima graffa di apertura non fra virgolette, o {@code -1} se non ce n'è
	 */
	private int trovaGraffaApertaFuoriDalleVirgolette(String figlio, int daIndice) {
		Scansione scansione = new Scansione(figlio, daIndice);
		while (scansione.avanza()) {
			if (scansione.isSintassi() && scansione.carattere() == GRAFFA_APERTA) {
				return scansione.indice();
			}
		}
		return -1;
	}

	/**
	 * Toglie i delimitatori {@link #VIRGOLETTA} di ogni tratto letterale grezzo trovato in {@code testo},
	 * trasformando un escape {@code \"} in una {@code "} letterale e un escape {@code \[}/{@code \]} nel
	 * segnaposto che {@link #postProduci} riconvertirà in una parentesi letterale. È l'unico punto in cui i marcatori
	 * di un tratto letterale grezzo sono effettivamente risolti: le scansioni precedenti ({@link #dividiAlternative},
	 * {@link #trovaGraffaChiusa}, {@link #trovaGraffaApertaFuoriDalleVirgolette}) devono solo sapere
	 * dove si trova un tratto, non toglierlo, dato che farlo prima che il riconoscimento dei gruppi sia finito renderebbe una
	 * {@link #GRAFFA_APERTA}/{@link #GRAFFA_CHIUSA} protetta dalle virgolette indistinguibile dalla sintassi vera.
	 */
	private String risolviVirgolette(String testo) {
		StringBuilder risultato = new StringBuilder();
		boolean fraVirgolette = false;
		for (int i = 0; i < testo.length(); i++) {
			char c = testo.charAt(i);
			if (fraVirgolette) {
				if (eLEscapeDi(testo, i, VIRGOLETTA)) {
					risultato.append(VIRGOLETTA);
					i++;
				} else if (eLEscapeDi(testo, i, QUADRA_APERTA.charAt(0))) {
					risultato.append(SEGNAPOSTO_QUADRA_APERTA);
					i++;
				} else if (eLEscapeDi(testo, i, QUADRA_CHIUSA.charAt(0))) {
					risultato.append(SEGNAPOSTO_QUADRA_CHIUSA);
					i++;
				} else if (c == VIRGOLETTA) {
					fraVirgolette = false;
				} else {
					risultato.append(c);
				}
			} else if (c == VIRGOLETTA) {
				fraVirgolette = true;
			} else {
				risultato.append(c);
			}
		}
		return risultato.toString();
	}

	/**
	 * Trova la graffa di chiusura corrispondente a quella di apertura in {@code indiceGraffaAperta}, rispettando
	 * il livello di annidamento, così che ad esempio in <code>{a|{b|c}}</code> la corrispondente del gruppo esterno sia proprio l'ultima
	 * <code>}</code>, non la prima incontrata.
	 * Una {@link #GRAFFA_APERTA}/{@link #GRAFFA_CHIUSA} dentro un tratto letterale grezzo
	 * ({@code "..."}) non influisce sul conteggio della profondità, così il testo di un gruppo può contenere
	 * graffe letterali senza compromettere l'appaiamento.
	 * @throws GrammaticaMalformataException se non esiste una graffa di chiusura corrispondente
	 */
	private int trovaGraffaChiusa(String figlio, int indiceGraffaAperta, int numeroDiRiga) throws GrammaticaMalformataException {
		// La scansione parte dalla graffa di apertura: quella che riporta il conto a zero è la sua chiusura
		Scansione scansione = new Scansione(figlio, indiceGraffaAperta);
		while (scansione.avanza()) {
			if (scansione.isSintassi() && scansione.carattere() == GRAFFA_CHIUSA && !scansione.isGraffaSpaiata()
					&& scansione.graffeAperte() == 0) {
				return scansione.indice();
			}
		}
		throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Missing '" + GRAFFA_CHIUSA
				+ "' for inline alternation started at '" + figlio.substring(indiceGraffaAperta) + "'");
	}

	/**
	 * Crea una nuova produzione dal contenuto di un gruppo di alternative in linea, con nome
	 * {@link #PREFISSO_PRODUZIONE_IN_LINEA} seguito da un numero progressivo univoco, e
	 * la registra in {@link #mappaProduzioni}. Le alternative sono divise con
	 * {@link #dividiAlternative} (non con una divisione ingenua per espressione regolare), così che i separatori
	 * <code>|</code> di un gruppo annidato non siano scambiati per quelli di primo livello, e ogni alternativa viene passata
	 * di nuovo a {@link #espandiAlternativeInLinea}, così che ogni gruppo annidato che contiene diventi la
	 * propria produzione al volo prima che questa sia registrata. Un'alternativa vuota (ad esempio
	 * l'ultima opzione vuota in {@code {grande|feroce|}}) è conservata come foglia a stringa vuota, esattamente come
	 * un {@code |} finale sulla riga dei figli di una produzione normale: è ciò che permette a un gruppo in linea
	 * di esprimere un elemento facoltativo (ad esempio un aggettivo facoltativo). Ogni opzione può anche iniziare
	 * con un elemento di peso {@code [^N]} (eventualmente frazionario; vedi {@link #leggiPeso}), che permette
	 * di pesare una produzione al volo, benché sia del tutto facoltativo: un'opzione senza tale
	 * elemento ha il peso predefinito di 1, perciò i gruppi in linea non richiedono alcun trattamento speciale per restare
	 * uniformemente casuali.
	 * @param testoDelGruppo il testo grezzo fra la graffa di apertura e quella di chiusura
	 * @return il nome della produzione appena creata
	 * @throws GrammaticaMalformataException se un gruppo di alternative in linea annidato è malformato, o
	 *                                  l'elemento di peso di un'opzione è malformato
	 */
	private String creaProduzioneInLinea(String testoDelGruppo, int numeroDiRiga) throws GrammaticaMalformataException {
		List<String> opzioniScritte = dividiAlternative(testoDelGruppo, numeroDiRiga);
		if (opzioniScritte.size() < 2) {
			throw new GrammaticaMalformataException(posizione(numeroDiRiga) + ": Inline alternation group '"
					+ GRAFFA_APERTA + testoDelGruppo + GRAFFA_CHIUSA + "' has no '|': a group with a single"
					+ " option only strips its own braces. Remove them, or quote them (\"" + GRAFFA_APERTA
					+ "\" ... \"" + GRAFFA_CHIUSA + "\") to emit them as literal text");
		}
		List<AlternativaPesata> opzioni = new ArrayList<>();
		for (String opzione : opzioniScritte) {
			AlternativaPesata pesata = leggiPeso(opzione.trim(), numeroDiRiga);
			opzioni.add(new AlternativaPesata(espandiAlternativeInLinea(pesata.testo, numeroDiRiga), pesata.peso));
		}
		String nome;
		do {
			nome = prefissoInLinea + contatoreInLinea++;
		} while (mappaProduzioni.containsKey(nome));
		mappaProduzioni.put(nome, opzioni);
		return nome;
	}

	/**
	 * Scorre ogni figlio di ogni produzione cercando i riferimenti {@code [Nome]} e
	 * convalida ciascuno con {@link #controllaElemento}. Viene chiamato una volta, subito dopo che
	 * l'intero file di grammatica (comprese le produzioni generate dai gruppi di alternative in linea)
	 * è stato analizzato.
	 * <p>Una chiave assegnata in un punto qualsiasi con {@code [chiave=valore]} è un bersaglio valido per un
	 * riferimento {@code *}/{@code !}/semplice altrove, anche se non esiste alcuna produzione di nome
	 * {@code chiave}, dato che a runtime un riferimento simile è soddisfatto dalla cache delle
	 * produzioni fissate e non dalla mappa delle produzioni. Serve quindi un
	 * primo passaggio ({@link #raccogliChiaviAssegnate()}) su tutta la grammatica prima del
	 * passaggio di convalida vero e proprio, dato che un'assegnazione può comparire nel testo dopo il
	 * riferimento che ne dipende.
	 * @throws GrammaticaMalformataException se un riferimento è malformato o punta a una produzione non definita
	 */
	private void controllaLeProduzioni() throws GrammaticaMalformataException {
		Set<String> chiaviAssegnate = raccogliChiaviAssegnate();
		for (String chiave : mappaProduzioni.keySet()) {
			for (AlternativaPesata alternativa : mappaProduzioni.get(chiave)) {
				controllaElementi(alternativa.testo, chiaviAssegnate);
			}
		}
	}

	/**
	 * Scorre ogni figlio di ogni produzione raccogliendo le chiavi assegnate con
	 * {@code [chiave=valore]}, comprese le assegnazioni annidate nel valore di un'altra
	 * assegnazione (ad esempio {@code [chiave=[AltraProduzione]]}).
	 */
	private Set<String> raccogliChiaviAssegnate() throws GrammaticaMalformataException {
		Set<String> chiaviAssegnate = new HashSet<>();
		for (List<AlternativaPesata> alternative : mappaProduzioni.values()) {
			for (AlternativaPesata alternativa : alternative) {
				raccogliChiaviAssegnate(alternativa.testo, chiaviAssegnate);
			}
		}
		return chiaviAssegnate;
	}

	private void raccogliChiaviAssegnate(String testo, Set<String> chiaviAssegnate) throws GrammaticaMalformataException {
		int indiceQuadraAperta;
		while ((indiceQuadraAperta = testo.indexOf(QUADRA_APERTA)) >= 0) {
			int indiceQuadraChiusa = trovaQuadraChiusaOErrore(testo, indiceQuadraAperta);
			String corpo = testo.substring(indiceQuadraAperta + 1, indiceQuadraChiusa);
			ElementoConPredefinito conPredefinito = dividiPredefinito(corpo);
			if (conPredefinito.ripiego != null) {
				// un elemento con predefinito legge soltanto; il suo ripiego, però, può benissimo assegnare
				raccogliChiaviAssegnate(conPredefinito.ripiego, chiaviAssegnate);
			} else {
				int posizioneUguale = corpo.indexOf(MARCATORE_ASSEGNAZIONE);
				if (posizioneUguale > 0) {
					chiaviAssegnate.add(corpo.substring(0, posizioneUguale));
					raccogliChiaviAssegnate(corpo.substring(posizioneUguale + MARCATORE_ASSEGNAZIONE.length()), chiaviAssegnate);
				}
			}
			testo = testo.substring(indiceQuadraChiusa + 1);
		}
	}

	/**
	 * Scorre ogni elemento {@code [...]} in {@code testo} (tenendo conto dell'annidamento, così un elemento come
	 * {@code [chiave=[AltraProduzione]]} è trattato come un unico elemento esterno il cui valore è
	 * a sua volta convalidato ricorsivamente) e convalida ciascuno con {@link #controllaElemento}.
	 */
	private void controllaElementi(String testo, Set<String> chiaviAssegnate) throws GrammaticaMalformataException {
		int indiceQuadraAperta;
		while ((indiceQuadraAperta = testo.indexOf(QUADRA_APERTA)) >= 0) {
			int indiceQuadraChiusa = trovaQuadraChiusaOErrore(testo, indiceQuadraAperta);
			controllaElemento(testo.substring(indiceQuadraAperta + 1, indiceQuadraChiusa), chiaviAssegnate);
			testo = testo.substring(indiceQuadraChiusa + 1);
		}
	}

	/**
	 * Convalida il corpo di un singolo riferimento {@code [...]} (un marcatore di produzione fissata,
	 * un'assegnazione, un riferimento a valore, o un semplice nome di produzione).
	 * @throws GrammaticaMalformataException se un marcatore di produzione fissata non ha un nome dopo di sé,
	 *                                  un marcatore di assegnazione non ha una chiave prima di sé, o
	 *                                  l'elemento è un semplice nome che non è una produzione definita
	 *                                  e non è stato assegnato in nessun punto della grammatica
	 */
	private void controllaElemento(String elemento, Set<String> chiaviAssegnate) throws GrammaticaMalformataException {
		ElementoConPredefinito conPredefinito = dividiPredefinito(elemento);
		if (conPredefinito.ripiego != null) {
			controllaElementi(conPredefinito.ripiego, chiaviAssegnate);
			String nome = conPredefinito.nome;
			if (nome.startsWith(MARCATORE_FISSATA_GLOBALE) || nome.startsWith(MARCATORE_FISSATA_LOCALE)
					|| nome.startsWith(MARCATORE_RIFERIMENTO)) {
				nome = nome.substring(1);
			}
			if (nome.isEmpty()) {
				throw new GrammaticaMalformataException(MARCATORE_PREDEFINITO + " marker must be preceded by a node name");
			}
			if (mappaProduzioni.get(nome) == null && !chiaviAssegnate.contains(nome) && !isNomeStatico(nome)) {
				throw new GrammaticaMalformataException(PRODUZIONE + nome + " is not defined: a '" + MARCATORE_PREDEFINITO
						+ "' default covers the order in which an assignment runs, not the existence of the name");
			}
			return;
		}
		boolean fissata = elemento.startsWith(MARCATORE_FISSATA_GLOBALE) || elemento.startsWith(MARCATORE_FISSATA_LOCALE);
		if (fissata) {
			elemento = elemento.substring(1);
			if (elemento.isEmpty()) {
				throw new GrammaticaMalformataException("Empty fixed production present (" + MARCATORE_FISSATA_GLOBALE
						+ " or " + MARCATORE_FISSATA_LOCALE + " alone)");
			}
		}
		if (elemento.startsWith(MARCATORE_ASSEGNAZIONE)) {
			throw new GrammaticaMalformataException(MARCATORE_ASSEGNAZIONE + " symbol must be preceded by a node name");
		}
		int posizioneUguale = elemento.indexOf(MARCATORE_ASSEGNAZIONE);
		if (posizioneUguale >= 0) {
			controllaElementi(elemento.substring(posizioneUguale + MARCATORE_ASSEGNAZIONE.length()), chiaviAssegnate);
			return;
		}
		// senza marcatore [chiave] cerca solo fra le produzioni: una chiave solo assegnata si legge con [#chiave], [*chiave] o [!chiave]
		if (mappaProduzioni.get(elemento) == null && !(fissata && chiaviAssegnate.contains(elemento))
				&& !elemento.startsWith(MARCATORE_RIFERIMENTO)
				&& !isNomeStatico(elemento)) {
			throw new GrammaticaMalformataException(PRODUZIONE + elemento + " is not defined");
		}
	}

	/**
	 * Come {@link #trovaQuadraChiusa}, ma segnala una parentesi non appaiata come
	 * {@link GrammaticaMalformataException} invece che come {@link IllegalArgumentException},
	 * per l'uso durante la convalida al caricamento della grammatica.
	 */
	private int trovaQuadraChiusaOErrore(String testo, int indiceQuadraAperta) throws GrammaticaMalformataException {
		try {
			return trovaQuadraChiusa(testo, indiceQuadraAperta);
		} catch (IllegalArgumentException e) {
			throw new GrammaticaMalformataException("Missing '" + QUADRA_CHIUSA + "' element after token " + testo);
		}
	}

	/**
	 * Aumenta il peso di ogni alternativa di {@link #FATTORE_DI_SCALA} volte il peso aggregato (aggiustato
	 * ricorsivamente) di ogni produzione che referenzia (vedi
	 * {@link #trovaProduzioniCitate}), così che le alternative che si espandono in sottoalberi
	 * strutturalmente più ricchi siano scelte proporzionalmente più spesso con
	 * {@link TipoModalitaProduzioneTesti#CASUALE}, senza bisogno di un elemento {@code [^N]}
	 * scritto a mano su ogni alternativa. Viene chiamato una volta, subito dopo che l'intera grammatica (comprese
	 * le produzioni dei gruppi di alternative in linea) è stata analizzata e convalidata, e prima che
	 * {@link #ripristina()} copi mai {@link #mappaProduzioni} in
	 * {@link #produzioniCorrenti}.
	 * <p>
	 * Le produzioni sono visitate nell'ordine di dichiarazione, ed è ciò che rende l'esito per
	 * i cicli di riferimenti prevedibile dal file di grammatica; vedi
	 * {@link #calcolaPesoComplessivo}.
	 */
	private void aumentaIPesiPerIDiscendenti() {
		Map<String, Double> pesiCalcolati = new HashMap<>();
		Set<String> produzioniInCorso = new HashSet<>();
		for (String nome : mappaProduzioni.keySet()) {
			calcolaPesoComplessivo(nome, pesiCalcolati, produzioniInCorso);
		}
	}

	/**
	 * Restituisce il peso aggregato (somma di quelli di tutte le alternative) della produzione {@code nome}
	 * dopo aver aumentato ricorsivamente ciascuna delle sue alternative tramite
	 * {@link #aumentaIPesiPerIDiscendenti}, sostituendo come effetto collaterale la sua voce in {@link #mappaProduzioni}
	 * con le alternative aumentate. I risultati sono memorizzati in
	 * {@code pesiCalcolati}.
	 * <p>
	 * Sicuro rispetto ai cicli: i riferimenti fra produzioni possono formare cicli (produzioni ricorsive su se stesse
	 * o a vicenda). Una produzione ancora in calcolo più in alto nello stack delle chiamate (tracciata in
	 * {@code produzioniInCorso}) non viene rientrata; si sommano invece direttamente le sue alternative
	 * attuali, non ancora aumentate, rompendo il ciclo con il peso base originale di quella produzione. Per un ciclo che attraversa più di una produzione,
	 * il membro raggiunto per primo finisce con un aumento maggiore degli altri; dato che {@link #mappaProduzioni} mantiene
	 * l'ordine di inserimento, è il membro dichiarato per primo nel file di grammatica, perciò
	 * l'asimmetria si può prevedere (e riordinare) leggendo il file.
	 */
	private double calcolaPesoComplessivo(String nome, Map<String, Double> pesiCalcolati, Set<String> produzioniInCorso) {
		Double memorizzato = pesiCalcolati.get(nome);
		if (memorizzato != null) {
			return memorizzato;
		}
		if (!mappaProduzioni.containsKey(nome)) {
			// Una produzione di una sezione statica: i suoi pesi sono stati aggiustati quando la sezione è stata caricata
			double totaleDellaSezione = 0;
			for (AlternativaPesata alternativa : sezioneStaticaDi(nome).produzioni.get(nome)) {
				totaleDellaSezione += alternativa.peso;
			}
			return totaleDellaSezione;
		}
		List<AlternativaPesata> alternative = mappaProduzioni.get(nome);
		if (!produzioniInCorso.add(nome)) {
			double sommaBase = 0;
			for (AlternativaPesata alternativa : alternative) {
				sommaBase += alternativa.peso;
			}
			return sommaBase;
		}
		List<AlternativaPesata> aumentate = new ArrayList<>(alternative.size());
		double totale = 0;
		for (AlternativaPesata alternativa : alternative) {
			double aumento = 0;
			for (String citata : trovaProduzioniCitate(alternativa.testo)) {
				aumento += calcolaPesoComplessivo(citata, pesiCalcolati, produzioniInCorso);
			}
			double pesoAumentato = alternativa.peso + FATTORE_DI_SCALA * aumento;
			aumentate.add(new AlternativaPesata(alternativa.testo, pesoAumentato));
			totale += pesoAumentato;
		}
		produzioniInCorso.remove(nome);
		mappaProduzioni.put(nome, aumentate);
		pesiCalcolati.put(nome, totale);
		return totale;
	}

	/**
	 * Restituisce ogni nome di produzione referenziato (come {@code [Nome]}, {@code [*Nome]} o
	 * {@code [!Nome]}) direttamente in {@code testo}, in ordine di occorrenza, mantenendo i
	 * duplicati: una produzione referenziata due volte nella stessa alternativa contribuisce
	 * due volte con il proprio peso aggregato all'aumento del peso dei discendenti di quell'alternativa (vedi
	 * {@link #aumentaIPesiPerIDiscendenti}). Rispecchia il modo in cui {@link #controllaElemento}
	 * toglie i marcatori, così gli elementi {@code [chiave=valore]}/{@code [#chiave]}, che non
	 * referenziano una produzione, sono saltati, salvo che il valore di un'assegnazione viene a sua volta
	 * cercato ricorsivamente (così {@code [chiave=[AltraProduzione]]} conta comunque
	 * {@code AltraProduzione} come referenziata). Anche un nome {@code *}/{@code !}/semplice che non sia
	 * una vera produzione (cioè che viene popolato solo tramite {@code =} altrove) è saltato,
	 * dato che non ha un proprio peso aggregato. Presuppone che {@code testo} sia già
	 * noto come ben formato (viene chiamato solo dopo che {@link #controllaLeProduzioni} è
	 * riuscito).
	 */
	private List<String> trovaProduzioniCitate(String testo) {
		List<String> citate = new ArrayList<>();
		raccogliProduzioniCitate(testo, citate);
		return citate;
	}

	private void raccogliProduzioniCitate(String testo, List<String> citate) {
		String restante = testo;
		int indiceQuadraAperta;
		while ((indiceQuadraAperta = restante.indexOf(QUADRA_APERTA)) >= 0) {
			int indiceQuadraChiusa = trovaQuadraChiusa(restante, indiceQuadraAperta);
			String corpo = restante.substring(indiceQuadraAperta + 1, indiceQuadraChiusa);
			ElementoConPredefinito conPredefinito = dividiPredefinito(corpo);
			if (conPredefinito.ripiego != null) {
				// dei due rami se ne prende esattamente uno al momento della produzione, quindi contarli
				// entrambi gonfierebbe il peso dell'alternativa: si preferisce il nome quando è una vera
				// produzione, e ci si ripiega su ciò che il predefinito stesso referenzia
				String nome = conPredefinito.nome;
				if (nome.startsWith(MARCATORE_FISSATA_GLOBALE) || nome.startsWith(MARCATORE_FISSATA_LOCALE)) {
					nome = nome.substring(1);
				}
				if (!nome.startsWith(MARCATORE_RIFERIMENTO) && isDefinita(nome)) {
					citate.add(nome);
				} else {
					raccogliProduzioniCitate(conPredefinito.ripiego, citate);
				}
				restante = restante.substring(indiceQuadraChiusa + 1);
				continue;
			}
			if (corpo.startsWith(MARCATORE_FISSATA_GLOBALE) || corpo.startsWith(MARCATORE_FISSATA_LOCALE)) {
				corpo = corpo.substring(1);
			}
			int posizioneUguale = corpo.indexOf(MARCATORE_ASSEGNAZIONE);
			if (posizioneUguale >= 0) {
				raccogliProduzioniCitate(corpo.substring(posizioneUguale + MARCATORE_ASSEGNAZIONE.length()), citate);
			} else if (!corpo.startsWith(MARCATORE_RIFERIMENTO) && isDefinita(corpo)) {
				citate.add(corpo);
			}
			restante = restante.substring(indiceQuadraChiusa + 1);
		}
	}

	/**
	 * Legge il file facoltativo di post-produzione in {@link #sostituzioniFinali}. Ogni riga
	 * deve avere la forma {@code prima:dopo}; un file mancante (uno stream {@code null}) comporta semplicemente
	 * nessuna sostituzione di post-produzione.
	 * <p>
	 * Una riga che inizia con {@link #PREFISSO_COMMENTO} è un commento a riga intera e una riga vuota è
	 * ignorata, esattamente come nel file di grammatica: una regola di sostituzione come {@code [,:[} ha bisogno
	 * di una parola di spiegazione accanto molto più di una riga di grammatica. Commenti e righe vuote
	 * contano comunque per il numero di riga riportato in un messaggio di errore, così che indichi
	 * la riga reale del file. Si noti che una riga è considerata vuota quando è
	 * <i>fatta di soli spazi</i>, non solo quando è vuota: a differenza di un file di grammatica, dove una riga
	 * rientrata e vuota è un'alternativa vuota significativa, qui non potrebbe mai esprimere una regola. Una regola il cui
	 * {@code prima} inizia con {@code #} non è di conseguenza esprimibile, lo stesso compromesso che fa il
	 * file di grammatica.
	 * <p>
	 * Una regola il cui {@code dopo} contiene alla lettera il proprio {@code prima} viene rifiutata qui,
	 * perché {@link #postProduci} riesamina il testo che ha appena riscritto e
	 * continuerebbe quindi a ritrovare lo stesso {@code prima} dentro il {@code dopo} appena scritto,
	 * ciclando all'infinito. Rifiutarla al caricamento lascia il ciclo di sostituzione in
	 * {@link #postProduci} esattamente com'è (applicazione ripetuta per regola, così che
	 * le occorrenze sovrapposte siano tutte riscritte) invece di rinunciare a quel comportamento
	 * a favore di una sostituzione in un solo passaggio.
	 * @throws GrammaticaMalformataException se in una riga manca il testo di pre- o post-produzione,
	 *                                  o il suo testo di post-produzione contiene il proprio
	 *                                  testo di pre-produzione
	 */
	private void leggiPostProduzione(InputStream flussoDiIngresso) throws GrammaticaMalformataException, IOException {
		if (flussoDiIngresso == null) {
			return;
		}
		try (BufferedReader lettore = new BufferedReader(new InputStreamReader(flussoDiIngresso, StandardCharsets.UTF_8))) {
			String riga;
			String prima;
			String dopo;
			int rigaCorrente = 0;
			while ((riga = lettore.readLine()) != null) {
				rigaCorrente++;
				if (riga.trim().isEmpty() || isCommento(riga)) {
					continue;
				}
				String[] parti = riga.split(SEPARATORE_POST_PRODUZIONE, 2);
				prima = parti[0];
				if (prima.trim().isEmpty()) {
					throw new GrammaticaMalformataException(RIGA + rigaCorrente + ": No pre production");
				}
				if (parti.length < 2) {
					throw new GrammaticaMalformataException(RIGA + rigaCorrente + ": No post production");
				}
				dopo = parti[1];
				if (dopo.trim().isEmpty()) {
					throw new GrammaticaMalformataException(RIGA + rigaCorrente + ": Empty post production");
				}
				if (dopo.contains(prima)) {
					throw new GrammaticaMalformataException(RIGA + rigaCorrente + ": Post production '" + dopo
							+ "' contains its own pre production '" + prima
							+ "': the substitution would never terminate");
				}
				sostituzioniFinali.put(prima, dopo);
			}
		}
	}

	/**
	 * Cambia la produzione da cui parte {@link #produci()}.
	 * @param radice il nome semplice (senza parentesi) di una produzione esistente
	 * @throws IllegalArgumentException se {@code radice} non è una produzione definita
	 */
	public void setRadice(String radice) {
		if (mappaProduzioni.get(radice) == null && !isNomeStatico(radice)) {
			throw new IllegalArgumentException(radice + ": not a valid production");
		}
		this.radice = radice;
	}

	/**
	 * @return il nome semplice (senza parentesi) della produzione da cui {@link #produci()} parte attualmente
	 */
	public String getRadice() {
		return radice;
	}

	/**
	 * Dice se la produzione può ancora produrre qualcosa nel ciclo corrente: esiste e ha
	 * ancora alternative. Una produzione monouso ({@code NOME$}) smette di poterlo fare quando la sua ultima
	 * alternativa è consumata, e così ogni produzione che referenziava solo lei (la cascata di
	 * rimozione, vedi {@link #rimuoviProduzione}), fino al prossimo {@link #ripristina()}. È il modo in cui un chiamante
	 * che estrae una serie di risultati unici scopre che la serie è finita, invece di aspettare che
	 * {@link #produci(String)} lanci un'eccezione.
	 * <p>
	 * Non guarda dentro le alternative: una che referenzia due volte la stessa produzione monouso esaurita
	 * ({@code [X] [X]}) conta comunque, e fallisce solo quando viene prodotta.
	 * @param production il nome semplice (senza parentesi) di una produzione
	 * @return false anche per una produzione mai definita
	 */
	public boolean puoProdurre(String produzione) {
		aggiornaStatiche();
		List<AlternativaPesata> alternative = produzioniCorrenti.get(produzione);
		return alternative != null;
	}

	List<AlternativaPesata> getAlternative(String produzioneRadice) {
		return mappaProduzioni.get(produzioneRadice);
	}

	/**
	 * @return la modalità corrente di scelta dell'alternativa (vedi {@link TipoModalitaProduzioneTesti})
	 */
	public TipoModalitaProduzioneTesti getModalita() {
		return modalita;
	}

	/**
	 * Imposta come viene scelta l'alternativa di una produzione.
	 * @param modalita la nuova modalità; vedi {@link TipoModalitaProduzioneTesti}
	 */
	public void setModalita(TipoModalitaProduzioneTesti modalita) {
		this.modalita = modalita;
	}

	/**
	 * Ripristina {@link #produzioniCorrenti} a una copia fresca di {@link #mappaProduzioni} e
	 * svuota {@link #fissateGlobali}, così che ogni alternativa monouso consumata da un
	 * ciclo di produzione precedente torni disponibile e nessun valore fissato vecchio trapeli nel ciclo
	 * successivo.
	 * <p>
	 * È la chiamata che traccia il confine fra una serie di produzioni e la successiva, quindi
	 * <b>quando</b> farla è una scelta, non una formalità:
	 * <ul>
	 *     <li>dopo ogni {@link #produci()}, quando ogni risultato deve essere indipendente dagli
	 *     altri e può riusare le stesse alternative;</li>
	 *     <li>solo all'inizio di una serie, quando lo scopo della serie è che i suoi risultati
	 *     <b>non</b> si ripetano a vicenda: fare il reset nel mezzo vanificherebbe le produzioni
	 *     monouso su cui la grammatica conta a tal fine.</li>
	 * </ul>
	 * Una grammatica senza produzioni monouso e senza riferimenti {@code [*Nome]} non ha nulla da
	 * ripristinare, quindi chiamarlo è innocuo ma inutile. Vedi la documentazione della classe per cosa
	 * succede a una serie che sopravvive al suo più piccolo serbatoio monouso.
	 */
	public void ripristina() {
		ricostruisciProduzioniCorrenti();
		fissateGlobali.clear();
	}

	/**
	 * Copia {@link #mappaProduzioni} in {@link #produzioniCorrenti} e pota ciò che le sezioni statiche hanno
	 * già esaurito. Non tocca le sezioni statiche: lo fa solo {@link #ripristinaLeSezioniStatiche()}.
	 */
	private void ricostruisciProduzioniCorrenti() {
		synchronized (LUCCHETTO_STATICO) {
			produzioniCorrenti.clear();
			for (Map.Entry<String, List<AlternativaPesata>> voce : mappaProduzioni.entrySet()) {
				produzioniCorrenti.put(voce.getKey(), new ArrayList<>(voce.getValue()));
			}
			nomiStaticiPotati.clear();
			epocaVista = epocaStatica;
			allineaStatiche();
			versioneVista = versioneStatica;
		}
	}

	/**
	 * Preimposta il valore che sarà restituito per un riferimento a produzione fissata globalmente
	 * ({@code [*nome]}), prima di chiamare {@link #produci()}.
	 */
	public void fissaProduzione(String nome, String valore) {
		fissateGlobali.put(nome, valore);
	}

	/**
	 * Produce un testo casuale a partire da {@link #radice}.
	 * <p>
	 * Se la grammatica ha produzioni monouso, se questa chiamata debba essere seguita da
	 * {@link #ripristina()} dipende da cosa vuole il chiamante: reset per rendere ogni chiamata
	 * indipendente, niente reset per costruire una serie di risultati che non devono ripetersi a vicenda.
	 * Vedi la documentazione della classe.
	 * @return il testo prodotto, diviso in una voce per riga
	 */
	public List<String> produci() {
		return produci(radice);
	}

	/**
	 * Produce un testo casuale a partire da una produzione data esplicitamente, senza
	 * cambiare {@link #radice}. Come {@link #produci()}, la cache globale delle produzioni fissate
	 * viene svuotata una volta prodotto il testo, così che un riferimento {@code [*Nome]}
	 * resti fissato all'interno di una singola chiamata invece di restare congelato per tutte
	 * le successive. Le produzioni monouso, invece, restano consumate finché non si chiama
	 * {@link #ripristina()}; vedi la documentazione della classe per quando è ciò che si vuole.
	 * @param radice il nome semplice (senza parentesi) di una produzione esistente
	 * @return il testo prodotto, diviso in una voce per riga
	 */
	public List<String> produci(String radice) {
		traccia.clear();
		try {
			return produciDalNodo(QUADRA_APERTA + radice + QUADRA_CHIUSA);
		} catch (RuntimeException e) {
			Logger.log(() -> "Grammar production failed. Trace:\n" + descriviLaTraccia());
			throw e;
		} finally {
			fissateGlobali.clear();
		}
	}

	/**
	 * Espande {@code nodoIniziale} (già racchiuso fra parentesi da {@link #produci()} o
	 * {@link #produci(String)}) nel suo testo finale, poi applica le sostituzioni
	 * di post-produzione e divide il risultato in righe.
	 * <p>
	 * Visibile solo nel package di proposito: prende un elemento già fra parentesi ed espone l'ordine
	 * interno delle fasi di produzione, quindi non fa parte della superficie pubblica della classe.
	 * È visibile solo perché i test di questo package possano pilotare direttamente il passaggio finale
	 * di divisione in righe, che nessun file di grammatica può raggiungere (nessuno dei due file
	 * sorgente può incorporare un a capo letterale dentro una singola voce).
	 * @param nodoIniziale un singolo elemento {@code [Nome]} da espandere
	 * @return il testo prodotto, diviso in una voce per riga
	 */
	List<String> produciDalNodo(String nodoIniziale) {
		aggiornaStatiche();
		String primoRisultato = produciDalNodo(nodoIniziale, produzioniCorrenti, fissateGlobali, nodoIniziale);
		String risultatoIntermedio = postProduci(primoRisultato);
        return new ArrayList<>(Arrays.asList(risultatoIntermedio.split(REGEX_A_CAPO)));
	}

	/**
	 * Descrive {@link #traccia} così com'è al momento, una riga per frame attivo
	 * (prima il più esterno), ciascuna con i valori successivi attraversati dalla propria espansione.
	 * Pensato per essere chiamato subito dopo che un'eccezione interrompe {@link #produci()}/
	 * {@link #produci(String)}, quando i frame rimasti in {@link #traccia} sono esattamente
	 * il percorso seguito dal motore della grammatica prima di fallire; chiamarlo in qualsiasi altro momento (ad esempio
	 * dopo una produzione riuscita) restituisce una stringa vuota, dato che ogni frame viene tolto
	 * al ritorno normale.
	 * @return la traccia, o una stringa vuota se non ce n'è di attiva
	 */
	String descriviLaTraccia() {
		StringBuilder sb = new StringBuilder();
		for (TrattoDellaTraccia tratto : traccia) {
			sb.append(tratto.etichetta).append(": ").append(String.join(" -> ", tratto.passi)).append('\n');
		}
		return sb.toString();
	}

	/**
	 * Trova ripetutamente l'elemento {@code [...]} più a sinistra in {@code produzione} e lo sostituisce
	 * con il suo valore risolto (vedi {@link #risolviElemento}) finché non ne restano.
	 * @param production il testo da espandere, in genere all'inizio un unico elemento {@code [Nome]}
	 * @param produzioniEreditate la mappa delle produzioni rispetto a cui risolvere i riferimenti semplici/fissati
	 *                            (quella dell'intera grammatica, o una sua copia potata dei monouso)
	 * @param fissateEreditate la cache delle produzioni fissate ereditata dal chiamante
	 *                              (la cache globale per una chiamata di primo livello, o la cache locale di un sottoalbero)
	 * @param label nomina ciò che viene espanso, per {@link #traccia}: un nome di produzione,
	 *              o l'elemento iniziale {@code [radice]} per la chiamata più esterna
	 * @return il testo completamente espanso, senza più elementi {@code [...]}
	 */
	private String produciDalNodo(String produzione, Map<String, List<AlternativaPesata>> produzioniEreditate, Map<String, String> fissateEreditate, String etichetta) {
		// Inizializzata con la cache del chiamante e non vuota, così che un valore fissato da un
		// [!Nome] più in alto sia visibile a un [#Nome] (o a un altro [!Nome]) in qualsiasi punto sotto
		// di esso nello stesso sottoalbero, a qualsiasi profondità. La copia è ciò che mantiene
		// indipendenti i rami fratelli: ognuno eredita lo stesso punto di partenza ma scrive solo nella
		// propria mappa, così nulla di ciò che un ramo fissa trapela verso l'alto o di lato.
		Map<String, String> fissateLocali = new HashMap<>(fissateEreditate);
		TrattoDellaTraccia tratto = new TrattoDellaTraccia(etichetta, produzione);
		traccia.addLast(tratto);
		int indiceQuadraAperta;
		while ((indiceQuadraAperta = produzione.indexOf(QUADRA_APERTA)) >= 0) {
			boolean maiuscola = indiceQuadraAperta > 0
					&& produzione.charAt(indiceQuadraAperta - 1) == MARCATORE_MAIUSCOLA.charAt(0);
			int fineDelPrefisso = maiuscola ? indiceQuadraAperta - 1 : indiceQuadraAperta;
			int indiceQuadraChiusa = trovaQuadraChiusa(produzione, indiceQuadraAperta);
			String prefisso = produzione.substring(0, fineDelPrefisso);
			String corpo = produzione.substring(indiceQuadraAperta + 1, indiceQuadraChiusa);
			String suffisso = produzione.substring(indiceQuadraChiusa + 1);
			String risolto = risolviElemento(corpo, produzioniEreditate, fissateEreditate, fissateLocali);
			if (maiuscola) {
				risolto = Maiuscole.inizialeMaiuscola(risolto);
			}
			produzione = prefisso + risolto + suffisso;
			tratto.passi.add(produzione);
		}
		// Solo sul percorso di ritorno normale: se un'eccezione si propaga fuori da questa chiamata
		// (o da una annidata in essa), questo frame viene lasciato di proposito in traccia,
		// così che descriviLaTraccia() possa ancora riportarlo.
		traccia.removeLast();
		return produzione;
	}

	/**
	 * Il corpo di un elemento diviso nel nome che referenzia e nel valore di ripiego da usare quando quel
	 * nome non ha alcun valore disponibile. {@link #ripiego} è {@code null} per un elemento senza
	 * {@link #MARCATORE_PREDEFINITO}, ed è ciò che dice a ogni chiamante di seguire il percorso ordinario.
	 */
	private static final class ElementoConPredefinito {
		private final String nome;
		private final String ripiego;

		private ElementoConPredefinito(String nome, String ripiego) {
			this.nome = nome;
			this.ripiego = ripiego;
		}
	}

	/**
	 * Trova il {@code |} che separa il nome referenziato da un elemento dal suo valore predefinito: il
	 * primo a profondità di annidamento zero (contano parentesi quadre, graffe e tratti letterali grezzi)
	 * il cui carattere non bianco immediatamente precedente è un {@link #MARCATORE_PREDEFINITO}.
	 * <p>
	 * Restituisce {@code -1} quando non ce n'è nessuno, e anche quando si trova prima un {@code =} a profondità zero:
	 * ciò rende l'elemento un'assegnazione ({@code [chiave=valore]}), il cui valore può liberamente
	 * contenere {@code ?} e {@code |} come testo semplice. Decide il primissimo {@code |} a profondità zero:
	 * se non è preceduto da un {@link #MARCATORE_PREDEFINITO} l'elemento semplicemente non ha
	 * predefinito, invece di proseguire la scansione a caccia di un candidato successivo.
	 */
	private int trovaSeparatorePredefinito(String corpo) {
		Scansione scansione = new Scansione(corpo, 0);
		while (scansione.avanza()) {
			if (!scansione.isAlPrimoLivello()) {
				continue;
			}
			if (scansione.carattere() == MARCATORE_ASSEGNAZIONE.charAt(0)) {
				return -1;
			}
			if (scansione.carattere() == '|') {
				return finisceColMarcatorePredefinito(corpo.substring(0, scansione.indice())) ? scansione.indice() : -1;
			}
		}
		return -1;
	}

	/**
	 * @return se {@code testo}, ignorando gli spazi finali, termina con il
	 *         {@link #MARCATORE_PREDEFINITO}
	 */
	private static boolean finisceColMarcatorePredefinito(String testo) {
		String ripulito = senzaSpaziFinali(testo);
		return ripulito.endsWith(MARCATORE_PREDEFINITO);
	}

	private static String senzaSpaziFinali(String testo) {
		int fine = testo.length();
		while (fine > 0 && Character.isWhitespace(testo.charAt(fine - 1))) {
			fine--;
		}
		return testo.substring(0, fine);
	}

	/**
	 * Divide il corpo di un elemento nel nome che referenzia e nel suo valore predefinito, riconoscendo sia
	 * {@code Nome? | ripiego} sia la forma abbreviata {@code Nome?} (un ripiego vuoto). Gli spazi
	 * attorno al {@link #MARCATORE_PREDEFINITO} e al {@code |} sono ignorati; il ripiego stesso viene
	 * restituito alla lettera, per essere espanso e ripulito da chi ne ha bisogno.
	 * @return un {@link ElementoConPredefinito} il cui {@code ripiego} è {@code null} quando l'elemento
	 *         non porta alcun predefinito, nel qual caso il suo {@code nome} è {@code corpo}
	 *         intatto
	 */
	private ElementoConPredefinito dividiPredefinito(String corpo) {
		int indiceSeparatore = trovaSeparatorePredefinito(corpo);
		if (indiceSeparatore >= 0) {
			String primaDelSeparatore = senzaSpaziFinali(corpo.substring(0, indiceSeparatore));
			String nome = primaDelSeparatore.substring(0, primaDelSeparatore.length() - MARCATORE_PREDEFINITO.length()).trim();
			return new ElementoConPredefinito(nome, corpo.substring(indiceSeparatore + 1));
		}
		if (finisceColMarcatorePredefinito(corpo) && !haAssegnazioneAlPrimoLivello(corpo)) {
			String ripulito = senzaSpaziFinali(corpo);
			return new ElementoConPredefinito(ripulito.substring(0, ripulito.length() - MARCATORE_PREDEFINITO.length()).trim(), "");
		}
		return new ElementoConPredefinito(corpo, null);
	}

	/**
	 * @return se {@code corpo} contiene un {@link #MARCATORE_ASSEGNAZIONE} a profondità di annidamento
	 *         zero, cioè se è un'assegnazione e non un riferimento
	 */
	private boolean haAssegnazioneAlPrimoLivello(String corpo) {
		Scansione scansione = new Scansione(corpo, 0);
		while (scansione.avanza()) {
			if (scansione.isAlPrimoLivello() && scansione.carattere() == MARCATORE_ASSEGNAZIONE.charAt(0)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Trova la {@code ]} corrispondente alla {@code [} in {@code indiceQuadraAperta}, rispettando
	 * l'annidamento, così che ad esempio in {@code [chiave=[AltraProduzione]]} l'elemento esterno sia appaiato
	 * con l'ultima {@code ]} esterna e non con la prima incontrata.
	 * @throws IllegalArgumentException se {@code produzione} non ha alcuna {@code ]} corrispondente dopo {@code indiceQuadraAperta}
	 */
	private int trovaQuadraChiusa(String produzione, int indiceQuadraAperta) {
		int profondita = 1;
		for (int indice = indiceQuadraAperta + 1; indice < produzione.length(); indice++) {
			char c = produzione.charAt(indice);
			if (c == QUADRA_APERTA.charAt(0)) {
				profondita++;
			} else if (c == QUADRA_CHIUSA.charAt(0)) {
				profondita--;
				if (profondita == 0) {
					return indice;
				}
			}
		}
		throw new IllegalArgumentException("Missing '" + QUADRA_CHIUSA + "' element after token " + produzione);
	}

	/**
	 * Risolve il contenuto di un singolo [elemento], passando al gestore appropriato
	 * in base al suo marcatore ({@code *}, {@code !}, {@code =} o {@code #}).
	 */
	private String risolviElemento(String corpo, Map<String, List<AlternativaPesata>> mappaProduzioni, Map<String, String> fissateEreditate, Map<String, String> fissateLocali) {
		ElementoConPredefinito conPredefinito = dividiPredefinito(corpo);
		if (conPredefinito.ripiego != null) {
			String risolto = risolviElementoONull(conPredefinito.nome, mappaProduzioni, fissateEreditate, fissateLocali);
			if (risolto != null) {
				return risolto;
			}
			return produciDalNodo(conPredefinito.ripiego, mappaProduzioni, fissateLocali, conPredefinito.nome + " (default)").trim();
		}
		if (corpo.startsWith(MARCATORE_FISSATA_GLOBALE)) {
			String nome = corpo.substring(MARCATORE_FISSATA_GLOBALE.length());
			return risolviFissata(nome, fissateGlobali, mappaProduzioni, fissateEreditate);
		}
		if (corpo.startsWith(MARCATORE_FISSATA_LOCALE)) {
			String nome = corpo.substring(MARCATORE_FISSATA_LOCALE.length());
			return risolviFissata(nome, fissateLocali, mappaProduzioni, fissateEreditate);
		}
		if (corpo.indexOf(MARCATORE_ASSEGNAZIONE) >= 0) {
			assegnaProduzioneFissata(corpo, mappaProduzioni, fissateEreditate);
			return "";
		}
		if (corpo.startsWith(MARCATORE_RIFERIMENTO)) {
			return risolviRiferimento(corpo, fissateLocali);
		}
		return risolviSemplice(corpo, mappaProduzioni, fissateLocali);
	}

	/**
	 * Risolve un riferimento come fa {@link #risolviElemento}, ma risponde {@code null}
	 * invece di lanciare un'eccezione quando non c'è alcun valore disponibile, così che il chiamante possa ripiegare sul
	 * predefinito dell'elemento. "Nessun valore disponibile" copre volutamente tutti i motivi insieme:
	 * <ul>
	 *     <li>il nome non è mai stato dichiarato come produzione e non è mai stato assegnato;</li>
	 *     <li>è una chiave che qualche {@code [chiave=valore]} assegna, ma quell'assegnazione non è ancora
	 *     stata eseguita a questo punto dell'espansione;</li>
	 *     <li>nomina una produzione monouso le cui alternative sono state tutte consumate, dato che
	 *     {@code mappaProduzioni} qui è la {@link #produzioniCorrenti} potata e una produzione
	 *     esaurita ne è stata rimossa.</li>
	 * </ul>
	 * Un elemento di assegnazione non arriva mai a questo metodo: {@link #trovaSeparatorePredefinito} si rifiuta di
	 * vedere un predefinito in un elemento che assegna.
	 */
	private String risolviElementoONull(String corpo, Map<String, List<AlternativaPesata>> mappaProduzioni, Map<String, String> fissateEreditate, Map<String, String> fissateLocali) {
		if (corpo.startsWith(MARCATORE_FISSATA_GLOBALE)) {
			return risolviFissataONull(corpo.substring(MARCATORE_FISSATA_GLOBALE.length()),
					fissateGlobali, mappaProduzioni, fissateEreditate);
		}
		if (corpo.startsWith(MARCATORE_FISSATA_LOCALE)) {
			return risolviFissataONull(corpo.substring(MARCATORE_FISSATA_LOCALE.length()),
					fissateLocali, mappaProduzioni, fissateEreditate);
		}
		if (corpo.startsWith(MARCATORE_RIFERIMENTO)) {
			String chiave = corpo.substring(MARCATORE_RIFERIMENTO.length());
			String valore = fissateLocali.get(chiave);
			return valore != null ? valore : fissateGlobali.get(chiave);
		}
		if (!mappaProduzioni.containsKey(corpo)) {
			return null;
		}
		return risolviSemplice(corpo, mappaProduzioni, fissateLocali);
	}

	/**
	 * La controparte nullable di {@link #risolviFissata}: prima la cache, poi la
	 * mappa delle produzioni, poi {@code null}.
	 */
	private String risolviFissataONull(String nome, Map<String, String> fissate, Map<String, List<AlternativaPesata>> mappaProduzioni, Map<String, String> fissateEreditate) {
		String memorizzato = fissate.get(nome);
		if (memorizzato != null) {
			return memorizzato;
		}
		if (!mappaProduzioni.containsKey(nome)) {
			return null;
		}
		return risolviFissata(nome, fissate, mappaProduzioni, fissateEreditate);
	}

	/**
	 * Risolve una produzione il cui risultato è messo in cache (globalmente o localmente, a seconda di
	 * {@code fissate}) così che i riferimenti successivi allo stesso nome riusino
	 * il valore generato la prima volta. Come {@link #risolviSemplice}, il risultato viene
	 * ripulito dagli spazi ai bordi: ci si aspetta che ogni produzione produca una stringa senza spazi ai bordi.
	 */
	private String risolviFissata(String nome, Map<String, String> fissate, Map<String, List<AlternativaPesata>> mappaProduzioni, Map<String, String> fissateEreditate) {
		String memorizzato = fissate.get(nome);
		if (memorizzato != null) {
			return memorizzato;
		}
		List<AlternativaPesata> produzioni = mappaProduzioni.get(nome);
		if (produzioni == null) {
			throw new IllegalArgumentException(PRODUZIONE + nome + " is empty!");
		}
		String risultato = produciDalNodo(scegliAlternativa(mappaProduzioni, nome), mappaProduzioni, fissateEreditate, nome).trim();
		fissate.put(nome, risultato);
		return risultato;
	}

	/**
	 * Gestisce un elemento {@code [chiave=valore]} memorizzando {@code valore} sotto {@code chiave} in
	 * {@link #fissateGlobali}, per recuperarlo poi con un riferimento {@code [#chiave]}
	 * in qualsiasi punto del testo prodotto (non solo all'interno del sottoalbero di produzione corrente).
	 * Se {@code valore} contiene a sua volta elementi {@code [...]} (ad esempio {@code [chiave=[AltraProduzione]]}),
	 * questi sono risolti completamente prima che il risultato sia memorizzato, così che il valore in cache sia sempre
	 * testo semplice.
	 */
	private void assegnaProduzioneFissata(String corpo, Map<String, List<AlternativaPesata>> mappaProduzioni, Map<String, String> fissateEreditate) {
		int posizioneUguale = corpo.indexOf(MARCATORE_ASSEGNAZIONE);
		String chiave = corpo.substring(0, posizioneUguale);
		String valore = corpo.substring(posizioneUguale + MARCATORE_ASSEGNAZIONE.length());
		String valoreRisolto = produciDalNodo(valore, mappaProduzioni, fissateEreditate, chiave);
		fissateGlobali.put(chiave, valoreRisolto);
	}

	/**
	 * Gestisce un elemento {@code [#chiave]} cercando il valore assegnato in precedenza a
	 * {@code chiave}: prima in {@code fissateLocali} (che un {@code [!chiave]} nello
	 * stesso frame di chiamata, o in uno qualsiasi che lo racchiuda nello stesso sottoalbero, può aver
	 * popolato), poi in {@link #fissateGlobali} (che
	 * {@link #assegnaProduzioneFissata} popola).
	 * @throws IllegalArgumentException se {@code chiave} non è mai stata assegnata
	 */
	private String risolviRiferimento(String corpo, Map<String, String> fissateLocali) {
		String chiave = corpo.substring(MARCATORE_RIFERIMENTO.length());
		String valore = fissateLocali.get(chiave);
		if (valore == null) {
			valore = fissateGlobali.get(chiave);
		}
		if (valore == null) {
			throw new IllegalArgumentException(PRODUZIONE + corpo + " not yet defined!");
		}
		return valore;
	}

	/**
	 * Risolve un semplice riferimento {@code [Nome]}: sceglie un'alternativa di {@code nome}
	 * (vedi {@link #scegliAlternativa}) e la espande ricorsivamente. Il risultato viene ripulito dagli spazi ai bordi: ci si aspetta che ogni
	 * produzione produca una stringa senza spazi ai bordi, così che ad esempio la foglia scelta da un gruppo di alternative in linea,
	 * se vuota, non lasci uno spazio spurio dovuto al testo letterale
	 * che circonda il riferimento a essa.
	 * @throws IllegalArgumentException se {@code nome} non è una produzione (attualmente disponibile)
	 */
	private String risolviSemplice(String nome, Map<String, List<AlternativaPesata>> mappaProduzioni, Map<String, String> fissateLocali) {
		List<AlternativaPesata> produzioni = mappaProduzioni.get(nome);
		if (produzioni == null) {
			throw new IllegalArgumentException(PRODUZIONE + nome + " is empty!");
		}
		return produciDalNodo(scegliAlternativa(mappaProduzioni, nome), mappaProduzioni, fissateLocali, nome).trim();
	}

	/**
	 * Sceglie un'alternativa di {@code produzione} da {@code produzioniLocali}, secondo
	 * {@link #modalita}: sempre la prima, sempre l'ultima, o una scelta a caso pesata
	 * (vedi {@link #scegliPesata}, che pesa ogni alternativa col suo {@link AlternativaPesata#peso}
	 * così che ad esempio un'alternativa di peso 10 sia scelta dieci volte più spesso di una col peso predefinito
	 * di 1). Se {@code produzione} è monouso, l'alternativa scelta (e, se era
	 * l'ultima, l'intera produzione, a cascata tramite {@link #rimuoviProduzione}) viene rimossa, così che non
	 * possa essere scelta di nuovo nello stesso ciclo di produzione.
	 */
	private String scegliAlternativa(Map<String, List<AlternativaPesata>> produzioniLocali, String produzione) {
		if (isNomeStatico(produzione)) {
			synchronized (LUCCHETTO_STATICO) {
				return produzioneStatica(produzione);
			}
		}
		List<AlternativaPesata> produzioni = produzioniLocali.get(produzione);
		AlternativaPesata scelta = scegli(produzioni);
		if (produzioniMonouso.contains(produzione)) {
			produzioni.remove(scelta);
			if (produzioni.isEmpty()) {
				rimuoviProduzione(produzioniLocali, produzione);
			}
		}
		return scelta.testo;
	}

	/**
	 * Sceglie un'alternativa secondo {@link #modalita}: la prima, l'ultima, o una scelta a caso pesata.
	 */
	private AlternativaPesata scegli(List<AlternativaPesata> produzioni) {
		switch (modalita) {
			case PRIMO:
				return produzioni.get(0);
			case ULTIMO:
				return produzioni.get(produzioni.size() - 1);
			default:
				return scegliPesata(produzioni);
		}
	}

	/**
	 * Sceglie a caso un'alternativa fra {@code produzioni}, pesando ciascuna col suo
	 * peso (eventualmente frazionario) {@link AlternativaPesata#peso}: si somma il peso totale, si estrae un
	 * valore casuale in {@code [0, pesoTotale)}, e si restituisce l'alternativa il cui intervallo di
	 * peso cumulato lo contiene. Se tutte le alternative hanno lo
	 * stesso peso, la scelta è uniforme.
	 */
	private AlternativaPesata scegliPesata(List<AlternativaPesata> produzioni) {
		double pesoTotale = 0;
		for (AlternativaPesata alternativa : produzioni) {
			pesoTotale += alternativa.peso;
		}
		// Si immaginano le alternative una dopo l'altra su un righello lungo quanto il peso totale, ognuna larga quanto il
		// suo peso: si sceglie un punto a caso del righello e si prende l'alternativa che lo contiene
		double r = caso.nextDouble() * pesoTotale;
		double pesoCumulato = 0;
		// L'ultima alternativa non si confronta: se le precedenti non bastano, r cade nel suo intervallo
		for (int i = 0; i < produzioni.size() - 1; i++) {
			AlternativaPesata alternativa = produzioni.get(i);
			pesoCumulato += alternativa.peso;
			if (r < pesoCumulato) {
				return alternativa;
			}
		}
		return produzioni.get(produzioni.size() - 1);
	}

	/**
	 * Rimuove da {@code produzioniLocali} una produzione monouso esaurita, e procede a cascata:
	 * toglie ogni riferimento {@code [produzione]}/{@code [*produzione]}/{@code [!produzione]} a essa
	 * dalle alternative di ogni altra produzione, rimuovendo ricorsivamente ogni produzione che
	 * resti così senza alternative proprie.
	 */
	private void rimuoviProduzione(Map<String, List<AlternativaPesata>> produzioniLocali, String produzione) {
		produzioniLocali.remove(produzione);
		String riferimentoSemplice = QUADRA_APERTA + produzione + QUADRA_CHIUSA;
		String riferimentoGlobale = QUADRA_APERTA + MARCATORE_FISSATA_GLOBALE + produzione + QUADRA_CHIUSA;
		String riferimentoLocale = QUADRA_APERTA + MARCATORE_FISSATA_LOCALE + produzione + QUADRA_CHIUSA;
		List<String> produzioniDaRimuovere = new ArrayList<>();
		for (Map.Entry<String, List<AlternativaPesata>> voce : produzioniLocali.entrySet()) {
			List<AlternativaPesata> opzioni = voce.getValue();
			opzioni.removeIf(opzione -> opzione.testo.contains(riferimentoSemplice) || opzione.testo.contains(riferimentoGlobale) || opzione.testo.contains(riferimentoLocale));
			if (opzioni.isEmpty()) {
				produzioniDaRimuovere.add(voce.getKey());
			}
		}
		for (String daRimuovere : produzioniDaRimuovere) {
			rimuoviProduzione(produzioniLocali, daRimuovere);
		}
	}

	/**
	 * Applica ogni sostituzione {@code prima:dopo} di {@link #sostituzioniFinali} al testo
	 * completamente espanso, correggendo i problemi di lingua naturale nati dalla concatenazione
	 * meccanica delle alternative di produzione, poi riduce a un solo spazio ogni sequenza di due o più
	 * spazi e/o tab rimasta (tipicamente per un'alternativa volutamente vuota), e infine toglie
	 * ogni spazio o tab rimasto subito prima di un segno di punteggiatura
	 * (tipicamente lasciato quando un riferimento proprio a fine frase si espande
	 * in una stringa vuota).
	 * <p>
	 * Ogni escape {@code \[}/{@code \]} scritto dentro un tratto letterale grezzo viene prima riconvertito in una
	 * vera parentesi, prima che le sostituzioni siano eseguite: ogni elemento è già stato espanso
	 * a quel punto, quindi una parentesi ripristinata qui non può più essere scambiata per un riferimento, e le
	 * regole di post-produzione e la ripulitura degli spazi vedono entrambe il testo finale.
	 * <p>
	 * Le regole sono applicate nell'ordine in cui compaiono nel file di post-produzione, così che un file che elenchi
	 * un {@code prima} più lungo prima di uno più corto che ne è il prefisso ottenga prima la corrispondenza più lunga.
	 * <p>
	 * Ogni regola è applicata ripetutamente finché il suo {@code prima} non compare più, riesaminando
	 * il testo dall'inizio ogni volta, così che le occorrenze sovrapposte
	 * siano tutte riscritte. Termina perché {@link #leggiPostProduzione} rifiuta
	 * ogni regola il cui {@code dopo} contenga il proprio {@code prima}: senza questa garanzia una
	 * regola continuerebbe a trovare il testo che aveva appena scritto.
	 */
	private String postProduci(String testoIntermedio) {
		testoIntermedio = testoIntermedio
				.replace(SEGNAPOSTO_QUADRA_APERTA, QUADRA_APERTA.charAt(0))
				.replace(SEGNAPOSTO_QUADRA_CHIUSA, QUADRA_CHIUSA.charAt(0));
		for (Map.Entry<String, String> voce : sostituzioniFinali.entrySet()) {
			String prima = voce.getKey();
			String dopo = voce.getValue();
			int punto;
			while ((punto = testoIntermedio.indexOf(prima)) >= 0) {
				StringBuilder sb = new StringBuilder(testoIntermedio.substring(0, punto)).append(dopo);
				int l = punto + prima.length();
				if (l < testoIntermedio.length()) {
					sb.append(testoIntermedio.substring(l));
				}
				testoIntermedio = sb.toString();
			}
		}
		testoIntermedio = testoIntermedio.replaceAll(REGEX_SPAZI_MULTIPLI, " ");
		return testoIntermedio.replaceAll(REGEX_SPAZIO_PRIMA_DELLA_PUNTEGGIATURA, "");
	}

	/**
	 * La grammatica (o la sua postproduzione) non è valida o non si può leggere: il messaggio dice dove e perché.
	 */
	public static class GrammaticaMalformataException extends Exception {
		private static final long serialVersionUID = 1L;

		/**
		 * @param error cosa non va nella grammatica
		 */
		public GrammaticaMalformataException(String errore) {
			super(errore);
		}
	}
}
