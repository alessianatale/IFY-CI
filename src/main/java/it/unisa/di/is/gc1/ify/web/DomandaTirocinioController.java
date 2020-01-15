package it.unisa.di.is.gc1.ify.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.unisa.di.is.gc1.ify.Studente.OperazioneNonAutorizzataException;
import it.unisa.di.is.gc1.ify.Studente.Studente;
import it.unisa.di.is.gc1.ify.convenzioni.DelegatoAziendale;
import it.unisa.di.is.gc1.ify.domandaTirocinio.DomandaTirocinio;
import it.unisa.di.is.gc1.ify.domandaTirocinio.DomandaTirocinioService;
import it.unisa.di.is.gc1.ify.progettoFormativo.ProgettoFormativo;
import it.unisa.di.is.gc1.ify.progettoFormativo.ProgettoFormativoService;
import it.unisa.di.is.gc1.ify.utenza.Utente;
import it.unisa.di.is.gc1.ify.utenza.UtenzaService;

/**
 * Controller per la gestione delle domande di tirocinio
 * 
 * @author Geremia Cavezza Giusy Castaldo Carmine Ferrara
 */

@Controller
public class DomandaTirocinioController {
	@Autowired
	private DomandaTirocinioService domandaTirocinioService;
	@Autowired
	private ProgettoFormativoService progettoFormativoService;

	@Autowired
	private UtenzaService utenzaService;

	@Autowired
	private DomandaTirocinioFormValidator domandaTirocinioFormValidator;

	/**
	 * Metodo per visualizzare il form modal di inserimento di una domanda di
	 * tirocinio
	 * 
	 * @param redirectAttribute
	 * @param idProgetto
	 * @return String stringa che rappresenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/nuovaDomandaTirocinio", method = RequestMethod.POST)
	public String visualizzaFormInserimentoDomandaTirocinio(RedirectAttributes redirectAttribute,
			@RequestParam("idProgettoFormativo") long idProgetto) {
		Utente utente = utenzaService.getUtenteAutenticato();

		if (utente instanceof Studente) {
			String s;
			try {
				s = domandaTirocinioService.controllaStatoStudente();
				if (s.equals("")) {

					ProgettoFormativo progettoFormativo = progettoFormativoService.cercaProgettoPerId(idProgetto);
					redirectAttribute.addFlashAttribute("progettoFormativo", progettoFormativo);
					return "redirect:/visualizzaAziendeConvenzionateStudente";
				} else {
					redirectAttribute.addFlashAttribute("message", s);
					return "redirect:/visualizzaAziendeConvenzionateStudente";
				}
			} catch (OperazioneNonAutorizzataException e) {
				return "redirect:/";
			}
		} else
			return "redirect:/";
	}

	/**
	 * Metodo per inviare una domanda di tirocinio
	 * 
	 * @param domandaTirocinioForm
	 * @param idProgettoFormativo
	 * @param result
	 * @param redirectAttribute
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/inserimentoDomandaTirocinio", method = RequestMethod.POST)
	public String invioDomandaTirocinio(
			@ModelAttribute("domandaTirocinioForm") DomandaTirocinioForm domandaTirocinioForm,
			@RequestParam("idProgettoFormativo") String idProgettoFormativo, BindingResult result,
			RedirectAttributes redirectAttribute, Model model) {

		domandaTirocinioFormValidator.validate(domandaTirocinioForm, result);

		if (result.hasErrors()) {
			// se ci sono errori il metodo controller setta tutti i parametri

			redirectAttribute.addFlashAttribute("domandaTirocinioForm", domandaTirocinioForm);
			redirectAttribute.addFlashAttribute("progettoFormativo",
					progettoFormativoService.cercaProgettoPerId(Long.parseLong(idProgettoFormativo)));

			for (ObjectError x : result.getGlobalErrors()) {
				redirectAttribute.addFlashAttribute(x.getCode(), x.getDefaultMessage());
			}

			return "redirect:/visualizzaAziendeConvenzionateStudente";
		}

		Studente studente;
		try {
			studente = (Studente) utenzaService.getUtenteAutenticato();
		} catch (Exception e) {
			return "redirect:/";
		}

		DomandaTirocinio domandaTirocinio = new DomandaTirocinio();
		ProgettoFormativo progettoFormativo = progettoFormativoService
				.cercaProgettoPerId(Long.parseLong(idProgettoFormativo));

		domandaTirocinio.setConoscenze(domandaTirocinioForm.getConoscenze());
		domandaTirocinio.setMotivazioni(domandaTirocinioForm.getMotivazioni());
		domandaTirocinio.setDataInizio(domandaTirocinioForm.getDataInizio());
		domandaTirocinio.setDataFine(domandaTirocinioForm.getDataFine());
		domandaTirocinio.setCfu(Integer.parseInt(domandaTirocinioForm.getNumeroCFU()));
		domandaTirocinio.setProgettoFormativo(progettoFormativo);
		domandaTirocinio.setAzienda(progettoFormativo.getAzienda());
		domandaTirocinio.setStudente(studente);
		domandaTirocinio.setStato(DomandaTirocinio.IN_ATTESA);

		try {
			domandaTirocinioService.salvaDomandaTirocinio(domandaTirocinio);
		} catch (Exception e) {
			return "redirect:/";
		}

		redirectAttribute.addFlashAttribute("successoInserimentoDomanda",
				"La sua domanda di tirocinio è stata inserita con successo.");
		return "redirect:/visualizzaAziendeConvenzionateStudente";
	}

	/**
	 * Metodo per accettare una domanda di tirocinio in attesa
	 * 
	 * @param model
	 * @param id
	 * @param redirectAttribute
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/accettaDomandaTirocinio", method = RequestMethod.POST)
	public String accettaDomandaTirocinio(@RequestParam("idDomanda") long id, Model model,
			RedirectAttributes redirectAttribute) {

		DomandaTirocinio domandaTirocinio;
		try {
			domandaTirocinio = domandaTirocinioService.accettaDomandaTirocinio(id);
			model.addAttribute("domandaTirocinioAccettata", domandaTirocinio);
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		redirectAttribute.addFlashAttribute("message",
				"La domanda di tirocinio dello studente " + domandaTirocinio.getStudente().getNome() + " "
						+ domandaTirocinio.getStudente().getCognome() + " è stata accettata con successo");
		return "redirect:/visualizzaDomandeTirocinioInAttesaAzienda";
	}

	/**
	 * Metodo per rifiutare una domanda di tirocinio in attesa
	 * 
	 * @param model
	 * @param id
	 * @param redirectAttribute
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/rifiutaDomandaTirocinio", method = RequestMethod.POST)
	public String rifiutaDomandaTirocinio(@RequestParam("idDomanda") long id, Model model,
			RedirectAttributes redirectAttribute) {

		DomandaTirocinio domandaTirocinio;
		try {
			domandaTirocinio = domandaTirocinioService.rifiutoDomandaTirocinio(id);
			model.addAttribute("domandaTirocinioRifiutata", domandaTirocinio);
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		redirectAttribute.addFlashAttribute("message",
				"La domanda di tirocinio dello studente " + domandaTirocinio.getStudente().getNome() + " "
						+ domandaTirocinio.getStudente().getCognome() + " è stata rifiutata con successo");
		return "redirect:/visualizzaDomandeTirocinioInAttesaAzienda";
	}

	/**
	 * Metodo per approvare una domanda di tirocinio accettata
	 * 
	 * @param model
	 * @param id
	 * @param redirectAttribute
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/approvazioneDomandaTirocinio", method = RequestMethod.POST)
	public String approvaDomandaTirocinio(@RequestParam("idDomanda") long id, Model model,
			RedirectAttributes redirectAttribute) {

		DomandaTirocinio domandaTirocinio;
		try {
			domandaTirocinio = domandaTirocinioService.approvazioneDomandaTirocinio(id);
			model.addAttribute("domandaTirocinioApprovata", domandaTirocinio);
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		redirectAttribute.addFlashAttribute("message",
				"La domanda di tirocinio dello studente " + domandaTirocinio.getStudente().getNome() + " "
						+ domandaTirocinio.getStudente().getCognome() + " è stata approvata con successo");
		return "redirect:/visualizzaDomandeTirocinioInAttesaUfficio";
	}

	/**
	 * Metodo per respingere una domanda di tirocinio accettata
	 * 
	 * @param model
	 * @param id
	 * @param redirectAttribute
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/respintaDomandaTirocinio", method = RequestMethod.POST)
	public String respingiDomandaTirocinio(@RequestParam("idDomanda") long id, Model model,
			RedirectAttributes redirectAttribute) {

		DomandaTirocinio domandaTirocinio;
		try {
			domandaTirocinio = domandaTirocinioService.respintaDomandaTirocinio(id);
			model.addAttribute("domandaTirocinioRespinta", domandaTirocinio);
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		redirectAttribute.addFlashAttribute("message",
				"La domanda di tirocinio dello studente " + domandaTirocinio.getStudente().getNome() + " "
						+ domandaTirocinio.getStudente().getCognome() + " è stata respinta con successo");
		return "redirect:/visualizzaDomandeTirocinioInAttesaUfficio";
	}

	/**
	 * Metodo per visualizzare la lista delle domande di tirocinio in attesa
	 * dell'azienda
	 * 
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/visualizzaDomandeTirocinioInAttesaAzienda", method = RequestMethod.GET)
	public String visualizzaDomandeTirocinioInAttesaAzienda(Model model) {
		DelegatoAziendale delegatoAziendale;
		try {
			delegatoAziendale = (DelegatoAziendale) utenzaService.getUtenteAutenticato();
		} catch (Exception e) {
			return "redirect:/";
		}

		List<DomandaTirocinio> domandeTirocinio;

		try {
			domandeTirocinio = domandaTirocinioService
					.visualizzaDomandeTirocinioInAttesaAzienda(delegatoAziendale.getAzienda().getpIva());
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		model.addAttribute("domandeTirocinio", domandeTirocinio);
		return "visualizzaDomandeTirocinioInAttesaAzienda";
	}

	/**
	 * Metodo per visualizzare la lista delle domande di tirocinio inoltrate
	 * dell'azienda all'ufficio con relativo stato
	 * 
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/visualizzaDomandeTirocinioInoltrateAzienda", method = RequestMethod.GET)
	public String visualizzaDomandeTirocinioInoltrateAzienda(Model model) {
		DelegatoAziendale delegatoAziendale;
		try {
			delegatoAziendale = (DelegatoAziendale) utenzaService.getUtenteAutenticato();
		} catch (Exception e) {
			return "redirect:/";
		}

		List<DomandaTirocinio> domandeTirocinio;

		try {
			domandeTirocinio = domandaTirocinioService
					.visualizzaDomandeTirocinioInoltrateAzienda(delegatoAziendale.getAzienda().getpIva());
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		model.addAttribute("domandeTirocinio", domandeTirocinio);
		return "visualizzaDomandeTirocinioInoltrateAzienda";
	}

	/**
	 * Metodo per visualizzare la lista delle domande di tirocinio inoltrate
	 * dell'azienda all'ufficio con relativo stato
	 * 
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/visualizzaTirociniInCorsoAzienda", method = RequestMethod.GET)
	public String visualizzaTirociniInCorsoAzienda(Model model) {
		DelegatoAziendale delegatoAziendale;
		try {
			delegatoAziendale = (DelegatoAziendale) utenzaService.getUtenteAutenticato();
		} catch (Exception e) {
			return "redirect:/";
		}

		List<DomandaTirocinio> tirocini;

		try {
			tirocini = domandaTirocinioService
					.visualizzaTirociniInCorsoAzienda(delegatoAziendale.getAzienda().getpIva());
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		model.addAttribute("tirociniInCorso", tirocini);
		return "visualizzaTirociniInCorsoAzienda";
	}

	/**
	 * Metodo per visualizzare la lista delle domande di tirocinio inoltrate dello
	 * studente all'azienda
	 * 
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/visualizzaDomandeTirocinioInoltrateStudente", method = RequestMethod.GET)
	public String visualizzaDomandeTirocinioInoltrateStudente(Model model) {
		Studente studente;
		try {
			studente = (Studente) utenzaService.getUtenteAutenticato();
		} catch (Exception e) {
			return "redirect:/";
		}

		List<DomandaTirocinio> domandeTirocinio;

		try {
			domandeTirocinio = domandaTirocinioService.visualizzaDomandeTirocinioInoltrateStudente(studente.getId());
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		model.addAttribute("domandeInoltrate", domandeTirocinio);
		return "visualizzaDomandeTirocinioInoltrateStudente";
	}

	/**
	 * Metodo per visualizzare la lista dei tirocini in corso dello studente
	 * 
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/visualizzaTirociniInCorsoStudente", method = RequestMethod.GET)
	public String visualizzaTirociniInCorsoStudente(Model model) {
		Studente studente;
		try {
			studente = (Studente) utenzaService.getUtenteAutenticato();
		} catch (Exception e) {
			return "redirect:/";
		}

		List<DomandaTirocinio> tirocini;

		try {
			tirocini = domandaTirocinioService.visualizzaTirociniInCorsoStudente(studente.getId());
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		model.addAttribute("tirociniInCorso", tirocini);
		return "visualizzaTirociniInCorsoStudente";
	}

	/**
	 * Metodo per visualizzare la lista delle domande di tirocinio perventute
	 * all'ufficio tirocini in attesa di valutazione
	 * 
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/visualizzaDomandeTirocinioInAttesaUfficio", method = RequestMethod.GET)
	public String visualizzaDomandeTirocinioInAttesaUfficio(Model model) {

		List<DomandaTirocinio> domandeTirocinio;

		try {
			domandeTirocinio = domandaTirocinioService.visualizzaDomandeTirocinioInAttesaUfficio();
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		model.addAttribute("domandeTirocinio", domandeTirocinio);
		return "visualizzaDomandeTirocinioInAttesaUfficio";
	}

	/**
	 * Metodo per visualizzare la lista delle domande di tirocinio valutate
	 * dall'ufficio tirocini
	 * 
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/visualizzaDomandeTirocinioValutateUfficio", method = RequestMethod.GET)
	public String visualizzaDomandeTirocinioValutateUfficio(Model model) {

		List<DomandaTirocinio> domandeTirocinio;

		try {
			domandeTirocinio = domandaTirocinioService.visualizzaDomandeTirocinioValutateUfficio();
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		model.addAttribute("domandeTirocinio", domandeTirocinio);
		return "visualizzaDomandeTirocinioValutateUfficio";
	}

	/**
	 * Metodo per visualizzare la lista dei tiroci in corso dell'ufficio tirocini
	 * 
	 * @param model
	 * @return String stringa che rapprestenta la pagina da visualizzare
	 */
	@RequestMapping(value = "/visualizzaTirociniInCorsoUfficio", method = RequestMethod.GET)
	public String visualizzaTirociniInCorsoUfficio(Model model) {

		List<DomandaTirocinio> tirocini;

		try {
			tirocini = domandaTirocinioService.visualizzaTirociniInCorsoUfficio();
		} catch (OperazioneNonAutorizzataException e) {
			return "redirect:/";
		}

		model.addAttribute("tirociniInCorso", tirocini);
		return "visualizzaTirociniInCorsoUfficio";
	}
}
