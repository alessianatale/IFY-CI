package it.unisa.di.is.gc1.ify.domandaTirocinio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unisa.di.is.gc1.ify.Studente.OperazioneNonAutorizzataException;
import it.unisa.di.is.gc1.ify.Studente.Studente;
import it.unisa.di.is.gc1.ify.convenzioni.DelegatoAziendale;
import it.unisa.di.is.gc1.ify.responsabileUfficioTirocini.ResponsabileUfficioTirocini;
import it.unisa.di.is.gc1.ify.utenza.MailSingletonSender;
import it.unisa.di.is.gc1.ify.utenza.Utente;
import it.unisa.di.is.gc1.ify.utenza.UtenzaService;

/**
 * La classe fornisce i metodi per la logica di business per la gestione delle
 * richieste d'iscrizione
 * 
 * @author Giusy Castaldo Geremia Cavezza Carmine Ferrara
 */

@Service
public class DomandaTirocinioService {
	@Autowired
	DomandaTirocinioRepository domandaTirocinioRepository;

	@Autowired
	UtenzaService utenzaService;

	@Autowired
	MailSingletonSender mailSingletonSender;

	@Transactional(rollbackFor = Exception.class)
	public String controllaStatoStudente() throws OperazioneNonAutorizzataException {
		try {
			Studente s = (Studente) utenzaService.getUtenteAutenticato();
			List<DomandaTirocinio> domandeTirocinio = domandaTirocinioRepository
					.findAllByStato(DomandaTirocinio.IN_ATTESA);
			for (DomandaTirocinio x : domandeTirocinio) {
				if (x.getStudente().getId() == s.getId()) {
					return "Hai già una domanda di tirocinio in attesa, non puoi inviarne un'altra al momento.";
				}
			}
			return "";
		} catch (Exception e) {
			throw new OperazioneNonAutorizzataException();
		}
	}

	/**
	 * Il metodo fornisce la funzionalità di salvataggio di una domanda di tirocinio
	 * pervenuta da uno studente e posta in stato di attesa
	 * 
	 * @param domandaTirocinio
	 * 
	 * @return DomandaTirocinio domandaTirocinio
	 * @pre domandaTirocinio != null
	 * @post domandaTirocinio != null
	 */
	@Transactional(rollbackFor = Exception.class)
	public DomandaTirocinio salvaDomandaTirocinio(DomandaTirocinio domandaTirocinio) {
		domandaTirocinio.setStato(DomandaTirocinio.IN_ATTESA);
		domandaTirocinio = domandaTirocinioRepository.save(domandaTirocinio);

		return domandaTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalità di accettazione aziendale di una domanda
	 * di tirocinio
	 * 
	 * 
	 * @param idDomanda
	 * @return Oggetto {@link DomandaTirocinio} che rappresenta la domanda di
	 *         tirocinio approvata
	 * @throws OperazioneNonAutorizzataException
	 */

	@Transactional(rollbackFor = Exception.class)
	public DomandaTirocinio accettaDomandaTirocinio(Long idDomanda) throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il delegati aziendale dell'azienda corrispondente può accettare una
		// domanda di tirocinio in attesa pervenuta all'azienda stessa
		if (!(utente instanceof DelegatoAziendale)) {
			throw new OperazioneNonAutorizzataException();
		}

		DelegatoAziendale delegatoAziendale = (DelegatoAziendale) utente;

		DomandaTirocinio domandaTirocinio = domandaTirocinioRepository.findById(idDomanda);
		if (!(delegatoAziendale.getAzienda().getpIva().equals(domandaTirocinio.getAzienda().getpIva()))) {
			throw new OperazioneNonAutorizzataException();
		}

		if (!domandaTirocinio.getStato().equals(DomandaTirocinio.IN_ATTESA)) {
			throw new OperazioneNonAutorizzataException("Impossibile accettare questa domanda");
		}

		domandaTirocinio.setStato(DomandaTirocinio.ACCETTATA);
		domandaTirocinio = domandaTirocinioRepository.save(domandaTirocinio);

		mailSingletonSender.sendEmail(domandaTirocinio, domandaTirocinio.getStudente().getEmail());

		return domandaTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalita' di rifiuto aziendale di una domanda di
	 * tirocinio
	 * 
	 * 
	 * @param idDomanda
	 * @return Oggetto {@link DomandaTirocinio} che rappresenta la domanda di
	 *         tirocinio rifiutata
	 * @throws OperazioneNonAutorizzataException
	 */

	@Transactional(rollbackFor = Exception.class)
	public DomandaTirocinio rifiutoDomandaTirocinio(Long idDomanda) throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il delegati aziendale dell'azienda corrispondente può rifiutare una
		// domanda di tirocinio in attesa pervenuta all'azienda stessa
		if (!(utente instanceof DelegatoAziendale)) {
			throw new OperazioneNonAutorizzataException();
		}

		DelegatoAziendale delegatoAziendale = (DelegatoAziendale) utente;

		DomandaTirocinio domandaTirocinio = domandaTirocinioRepository.findById(idDomanda);
		if (!(delegatoAziendale.getAzienda().getpIva().equals(domandaTirocinio.getAzienda().getpIva()))) {
			throw new OperazioneNonAutorizzataException();
		}

		if (!domandaTirocinio.getStato().equals(DomandaTirocinio.IN_ATTESA)) {
			throw new OperazioneNonAutorizzataException("Impossibile rifiutare questa domanda");
		}

		domandaTirocinio.setStato(DomandaTirocinio.RIFIUTATA);
		domandaTirocinio = domandaTirocinioRepository.save(domandaTirocinio);

		mailSingletonSender.sendEmail(domandaTirocinio, domandaTirocinio.getStudente().getEmail());

		return domandaTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalita' di approvazione dell'ufficio tirocini di
	 * una domanda di tirocinio
	 * 
	 * 
	 * @param idDomanda
	 * @return Oggetto {@link DomandaTirocinio} che rappresenta la domanda di
	 *         tirocinio rifiutata
	 * @throws OperazioneNonAutorizzataException
	 */

	@Transactional(rollbackFor = Exception.class)
	public DomandaTirocinio approvazioneDomandaTirocinio(Long idDomanda) throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il responsabile dell'ufficio tirocini può approvare definitivamente una
		// domanda di tirocinio pervenuta dopo l'accettazione aziendale
		if (!(utente instanceof ResponsabileUfficioTirocini)) {
			throw new OperazioneNonAutorizzataException();
		}

		DomandaTirocinio domandaTirocinio = domandaTirocinioRepository.findById(idDomanda);

		if (!domandaTirocinio.getStato().equals(DomandaTirocinio.ACCETTATA)) {
			throw new OperazioneNonAutorizzataException("Impossibile rifiutare questa domanda");
		}

		domandaTirocinio.setStato(DomandaTirocinio.APPROVATA);
		domandaTirocinio = domandaTirocinioRepository.save(domandaTirocinio);

		mailSingletonSender.sendEmail(domandaTirocinio, domandaTirocinio.getStudente().getEmail());

		return domandaTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalita' di respinta dell'ufficio tirocini di una
	 * domanda di tirocinio
	 * 
	 * 
	 * @param idDomanda
	 * @return Oggetto {@link DomandaTirocinio} che rappresenta la domanda di
	 *         tirocinio rifiutata
	 * 
	 * @throws OperazioneNonAutorizzataException
	 */

	@Transactional(rollbackFor = Exception.class)
	public DomandaTirocinio respintaDomandaTirocinio(Long idDomanda) throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il responsabile dell'ufficio tirocini può respingere definitivamente una
		// domanda di tirocinio pervenuta dopo l'accettazione aziendale
		if (!(utente instanceof ResponsabileUfficioTirocini)) {
			throw new OperazioneNonAutorizzataException();
		}

		DomandaTirocinio domandaTirocinio = domandaTirocinioRepository.findById(idDomanda);

		if (!domandaTirocinio.getStato().equals(DomandaTirocinio.ACCETTATA)) {
			throw new OperazioneNonAutorizzataException("Impossibile rifiutare questa domanda");
		}

		domandaTirocinio.setStato(DomandaTirocinio.RESPINTA);
		domandaTirocinio = domandaTirocinioRepository.save(domandaTirocinio);

		mailSingletonSender.sendEmail(domandaTirocinio, domandaTirocinio.getStudente().getEmail());

		return domandaTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalita' di visualizzazione delle domande di
	 * tirocinio in attesa
	 * 
	 * @param piva
	 * @return Lista di {@link DomandaTirocinio} che rappresenta la lista delle
	 *         domande di tirocinio <b>Può essere vuota</b> se nel database non sono
	 *         presenti domande di tirocinio di quella determinata azienda
	 *         
	 * @throws OperazioneNonAutorizzataException
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DomandaTirocinio> visualizzaDomandeTirocinioInAttesaAzienda(String piva)
			throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il delegato aziendale può visualizzare le domande di tirocinio in attesa
		// dell'azienda
		if (!(utente instanceof DelegatoAziendale)) {
			throw new OperazioneNonAutorizzataException();
		}
		DelegatoAziendale delegatoAziendale = (DelegatoAziendale) utente;

		// Le domande di tirocinio di un'azienda possono essere visualizzate solo dal
		// delegato aziendale dell'azienda stessa
		if (!(delegatoAziendale.getAzienda().getpIva().equals(piva))) {
			throw new OperazioneNonAutorizzataException();
		}

		List<DomandaTirocinio> domandeTirocinio = domandaTirocinioRepository.findAllByAziendaPIvaAndStato(piva,
				DomandaTirocinio.IN_ATTESA);

		return domandeTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalita' di visualizzazione delle domande di
	 * tirocinio inoltrate
	 * 
	 * @param piva
	 * @return Lista di {@link DomandaTirocinio} che rappresenta la lista delle
	 *         domande di tirocinio <b>Può essere vuota</b> se nel database non sono
	 *         presenti domande di tirocinio di quella determinata azienda
	 *         
	 * @throws OperazioneNonAutorizzataException
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DomandaTirocinio> visualizzaDomandeTirocinioInoltrateAzienda(String piva)
			throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il delegato aziendale può visualizzare le domande di tirocinio inoltrate
		// dell'azienda
		if (!(utente instanceof DelegatoAziendale)) {
			throw new OperazioneNonAutorizzataException();
		}
		DelegatoAziendale delegatoAziendale = (DelegatoAziendale) utente;

		// Le domande di tirocinio di un'azienda possono essere visualizzate solo dal
		// delegato aziendale dell'azienda stessa
		if (!(delegatoAziendale.getAzienda().getpIva().equals(piva))) {
			throw new OperazioneNonAutorizzataException();
		}

		List<DomandaTirocinio> domandeTirocinio = new ArrayList<DomandaTirocinio>();
		domandeTirocinio
				.addAll(domandaTirocinioRepository.findAllByAziendaPIvaAndStato(piva, DomandaTirocinio.ACCETTATA));
		domandeTirocinio
				.addAll(domandaTirocinioRepository.findAllByAziendaPIvaAndStato(piva, DomandaTirocinio.APPROVATA));
		domandeTirocinio
				.addAll(domandaTirocinioRepository.findAllByAziendaPIvaAndStato(piva, DomandaTirocinio.RESPINTA));

		return domandeTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalita' di visualizzazione dei tirocini in corso
	 * dell'azienda
	 * 
	 * @param piva
	 * @return Lista di {@link DomandaTirocinio} che rappresenta la lista delle
	 *         domande di tirocinio <b>Può essere vuota</b> se nel database non sono
	 *         presenti domande di tirocinio di quella determinata azienda e in
	 *         corso
	 *         
	 * @throws OperazioneNonAutorizzataException
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DomandaTirocinio> visualizzaTirociniInCorsoAzienda(String piva)
			throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// L'azienda può visualizzare i suoi tirocini in corso
		if (!(utente instanceof DelegatoAziendale)) {
			throw new OperazioneNonAutorizzataException();
		}
		DelegatoAziendale delegatoAziendale = (DelegatoAziendale) utente;

		// I tirocini in corso di un'azienda possono essere visualizzate dall'azienda
		// stessa
		if (!(delegatoAziendale.getAzienda().getpIva().equals(piva))) {
			throw new OperazioneNonAutorizzataException();
		}

		List<DomandaTirocinio> domandeTirocinio = domandaTirocinioRepository.findAllByAziendaPIvaAndStato(piva,
				DomandaTirocinio.APPROVATA);
		List<DomandaTirocinio> tirociniInCorso = new ArrayList<DomandaTirocinio>();

		for (DomandaTirocinio d : domandeTirocinio) {
			if (d.getDataInizio().isBefore(LocalDate.now()) && d.getDataFine().isAfter(LocalDate.now())) {
				tirociniInCorso.add(d);
			}
		}
		return tirociniInCorso;
	}

	/**
	 * Il metodo fornisce la funzionalita' di visualizzazione delle domande di
	 * tirocinio inoltrate da uno studente
	 * 
	 * @param id
	 * @return Lista di {@link DomandaTirocinio} che rappresenta la lista delle
	 *         domande di tirocinio <b>Può essere vuota</b> se nel database non sono
	 *         presenti domande di tirocinio di quel determinato studente
	 * @throws OperazioneNonAutorizzataException
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DomandaTirocinio> visualizzaDomandeTirocinioInoltrateStudente(Long id)
			throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Lo studente può visualizzare le domande di tirocinio da lui inoltrate
		if (!(utente instanceof Studente)) {
			throw new OperazioneNonAutorizzataException();
		}
		Studente studente = (Studente) utente;

		// Le domande di tirocinio di uno studente possono essere visualizzate dallo
		// studente stesso
		if (!(studente.getId() == id)) {
			throw new OperazioneNonAutorizzataException();
		}

		List<DomandaTirocinio> domandeTirocinio = domandaTirocinioRepository.findAllByStudenteId(id);

		return domandeTirocinio;
	}
	
	/**
	 *  Il metodo fornisce la funzionalita' di visualizzazione dei tirocini in corso
	 * di uno studente
	 * 
	 * @param id
	 * @return Lista di {@link DomandaTirocinio} che rappresenta la lista delle
	 *         domande di tirocinio <b>Può essere vuota</b> se nel database non sono
	 *         presenti domande di tirocinio di quel determinato studente e in corso
	 * @throws OperazioneNonAutorizzataException
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DomandaTirocinio> visualizzaTirociniInCorsoStudente(Long id) throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Lo studente può visualizzare le domande di tirocinio da lui inoltrate
		if (!(utente instanceof Studente)) {
			throw new OperazioneNonAutorizzataException();
		}
		Studente studente = (Studente) utente;

		// I tirocini in corso di uno studente possono essere visualizzate dallo
		// studente stesso
		if (!(studente.getId() == id)) {
			throw new OperazioneNonAutorizzataException();
		}

		List<DomandaTirocinio> domandeTirocinio = domandaTirocinioRepository.findAllByStudenteIdAndStato(id,
				DomandaTirocinio.APPROVATA);
		List<DomandaTirocinio> tirociniInCorso = new ArrayList<DomandaTirocinio>();

		for (DomandaTirocinio d : domandeTirocinio) {
			if (d.getDataInizio().isBefore(LocalDate.now().plusDays(1L)) && d.getDataFine().isAfter(LocalDate.now())) {
				tirociniInCorso.add(d);
			}
		}
		return tirociniInCorso;
	}

	/**
	 * Il metodo fornisce la funzionalita' di visualizzazione delle domande di
	 * tirocinio in attesa dell'ufficio
	 * 
	 * @return Lista di {@link DomandaTirocinio} che rappresenta la lista delle
	 *         domande di tirocinio <b>Può essere vuota</b> se nel database non sono
	 *         presenti domande di tirocinio in attesa dell'ufficio
	 * @throws OperazioneNonAutorizzataException
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DomandaTirocinio> visualizzaDomandeTirocinioInAttesaUfficio() throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il responsabile ufficio può visualizzare le domande di tirocinio in
		// attesa dell'ufficio
		if (!(utente instanceof ResponsabileUfficioTirocini)) {
			throw new OperazioneNonAutorizzataException();
		}

		List<DomandaTirocinio> domandeTirocinio = domandaTirocinioRepository.findAllByStato(DomandaTirocinio.ACCETTATA);

		return domandeTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalita' di visualizzazione delle domande di
	 * tirocinio valutate dall'ufficio
	 * 
	 * @return Lista di {@link DomandaTirocinio} che rappresenta la lista delle
	 *         domande di tirocinio <b>Può essere vuota</b> se nel database non sono
	 *         presenti domande di tirocinio valutate dall'ufficio
	 * @throws OperazioneNonAutorizzataException        
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DomandaTirocinio> visualizzaDomandeTirocinioValutateUfficio() throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il responsabile ufficio può visualizzare le domande di tirocinio
		// valutate dall'ufficio
		if (!(utente instanceof ResponsabileUfficioTirocini)) {
			throw new OperazioneNonAutorizzataException();
		}

		List<DomandaTirocinio> domandeTirocinio = new ArrayList<DomandaTirocinio>();
		domandeTirocinio.addAll(domandaTirocinioRepository.findAllByStato(DomandaTirocinio.APPROVATA));
		domandeTirocinio.addAll(domandaTirocinioRepository.findAllByStato(DomandaTirocinio.RESPINTA));

		return domandeTirocinio;
	}

	/**
	 * Il metodo fornisce la funzionalita' di visualizzazione dei tirocini in corso
	 * ufficio
	 * 
	 * @return Lista di {@link DomandaTirocinio} che rappresenta la lista dei
	 *         tirocini in corso <b>Può essere vuota</b> se nel database non sono
	 *         presenti tirocini in corso
	 *         
	 * @throws OperazioneNonAutorizzataException
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DomandaTirocinio> visualizzaTirociniInCorsoUfficio() throws OperazioneNonAutorizzataException {

		Utente utente = utenzaService.getUtenteAutenticato();

		// Solo il responsabile ufficio può visualizzare i tirocini in corso
		if (!(utente instanceof ResponsabileUfficioTirocini)) {
			throw new OperazioneNonAutorizzataException();
		}

		List<DomandaTirocinio> domandeTirocinio = domandaTirocinioRepository.findAllByStato(DomandaTirocinio.APPROVATA);
		List<DomandaTirocinio> tirociniInCorso = new ArrayList<DomandaTirocinio>();

		for (DomandaTirocinio d : domandeTirocinio) {
			if (d.getDataInizio().isBefore(LocalDate.now().plusDays(1L)) && d.getDataFine().isAfter(LocalDate.now())) {
				tirociniInCorso.add(d);
			}
		}
		return tirociniInCorso;
	}

	/**
	 * Il metodo fornisce i controlli di validazione del parametro conoscenze
	 * 
	 * @param conoscenze
	 * @return conoscenze
	 * @throws DomandaTirocinioNonValidaException
	 */
	public String validaConoscenze(String conoscenze) throws DomandaTirocinioNonValidaException {
		if (conoscenze == null)
			throw new DomandaTirocinioNonValidaException("ConoscenzeError",
					"Il campo conoscenze non può essere nullo.");

		if (conoscenze.length() < DomandaTirocinio.MIN_LUNGHEZZA_CONOSCENZE)
			throw new DomandaTirocinioNonValidaException("ConoscenzeError",
					"il campo Conoscenze Tecniche deve contenere almeno un carattere.");

		if (conoscenze.length() > DomandaTirocinio.MAX_LUNGHEZZA_CONOSCENZE)
			throw new DomandaTirocinioNonValidaException("ConoscenzeError",
					"il campo Conoscenze Tecniche deve contenere al massimo 200 caratteri.");
		return conoscenze;
	}

	/**
	 * Il metodo fornisce i controlli di validazione del parametro motivazioni
	 * 
	 * @param motivazioni
	 * @return motivazioni
	 * @throws DomandaTirocinioNonValidaException
	 */
	public String validaMotivazioni(String motivazioni) throws DomandaTirocinioNonValidaException {
		if (motivazioni == null)
			throw new DomandaTirocinioNonValidaException("MotivazioniError",
					"Il campo motivazioni non può essere nullo.");

		if (motivazioni.length() < DomandaTirocinio.MIN_LUNGHEZZA_MOTIVAZIONI)
			throw new DomandaTirocinioNonValidaException("MotivazioniError",
					"il campo Motivazioni deve contenere almeno un carattere.");

		if (motivazioni.length() > DomandaTirocinio.MAX_LUNGHEZZA_MOTIVAZIONI)
			throw new DomandaTirocinioNonValidaException("MotivazioniError",
					"il campo Motivazioni deve contenere al massimo 300 caratteri.");
		return motivazioni;
	}

	/**
	 * Il metodo fornisce i controlli di validazione del parametro dataInizio
	 * 
	 * @param dataInizio
	 * @return dataInizio
	 * @throws DomandaTirocinioNonValidaException
	 */
	public LocalDate validaDataInizio(LocalDate dataInizio) throws DomandaTirocinioNonValidaException {
		if (dataInizio == null)
			throw new DomandaTirocinioNonValidaException("dataInizioError",
					"Il campo data inizio non può essere nullo.");

		if (!dataInizio.isAfter(DomandaTirocinio.MIN_DATE_INIZIO))
			throw new DomandaTirocinioNonValidaException("dataInizioError",
					"Il campo Data Inizio Tirocinio deve contenere una data maggiore della data corrente.");
		return dataInizio;
	}

	/**
	 * Il metodo fornisce i controlli di validazione del parametro dataFine
	 * 
	 * @param dataInizio
	 * @param dataFine
	 * @return dataFine
	 * @throws DomandaTirocinioNonValidaException
	 */
	public LocalDate validaDataFine(LocalDate dataInizio, LocalDate dataFine)
			throws DomandaTirocinioNonValidaException {
		System.out.println(dataInizio + "" + dataFine);
		if (dataFine == null)
			throw new DomandaTirocinioNonValidaException("dataFineError", "Il campo data fine non può essere nullo.");

		if (!dataFine.isAfter(dataInizio))
			throw new DomandaTirocinioNonValidaException("dataFineError",
					"il campo Data Fine Tirocinio deve contenere una data di fine maggiore della data di inizio del tirocinio.");
		return dataFine;
	}

	/**
	 * Il metodo fornisce i controlli di validazione del parametro cfu
	 * 
	 * @param cfu
	 * @return cfu
	 * @throws DomandaTirocinioNonValidaException
	 */
	public String cfu(String cfu) throws DomandaTirocinioNonValidaException {
		if (cfu == null || cfu.equals(""))
			throw new DomandaTirocinioNonValidaException("cfuError", "Il campo cfu non può essere nullo.");

		if (Integer.parseInt(cfu) < DomandaTirocinio.MIN_CFU || Integer.parseInt(cfu) > DomandaTirocinio.MAX_CFU)
			throw new DomandaTirocinioNonValidaException("cfuError",
					"Il campo Numero CFU Associati deve contenere valori che vadano da 6 a 12.");
		return cfu;
	}

	/**
	 * Il metodo fornisce i controlli di validazione del parametro cfu
	 * 
	 * @param condizioni
	 * @return
	 * @throws DomandaTirocinioNonValidaException
	 * 
	 * @return condizioni
	 */
	public String validaCondizioni(String condizioni) throws DomandaTirocinioNonValidaException {
		if (condizioni == null)
			throw new DomandaTirocinioNonValidaException("CondizioniError",
					"È obbligatorio accettare le condizioni sulla privacy.");

		return condizioni;
	}

}
