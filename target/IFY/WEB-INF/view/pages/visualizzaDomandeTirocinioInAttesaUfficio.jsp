<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html>
	<head>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
		<meta name="description" content="">
		<meta name="author" content="">
		<title>Domande tirocinio in attesa</title>
		<link rel="stylesheet" href="webjars/bootstrap/4.4.1/css/bootstrap.min.css">
		<link href="webjars/font-awesome/5.12.0/css/all.css" rel="stylesheet" />
		<link href="./resources/css/bootstrap-table.css" rel="stylesheet" />
		<!-- Custom styles for this template -->
		<link rel="stylesheet" href="./resources/css/sidebar.css">
		<link href="./resources/css/dashboard.css" rel="stylesheet">
	</head>
	<body>
		<div class="container-fluid">			
		<%@ include file="header.jsp"%>
			<div class="row">
				<div class="wrapper d-flex align-items-stretch">
					<nav id="sidebar">
						<div class="custom-menu">
							<button type="button" id="sidebarCollapse" class="btn btn-primary">
								<i class="fa fa-bars"></i> <span class="sr-only">Toggle Menu</span>
							</button>
						</div>
						<div class="p-4 pt-5">							
							<ul class="list-unstyled components mb-5">							
								<li><a href="./">Dashboard</a></li>
								<li><a href="#homeSubmenuRichieste" data-toggle="collapse" aria-expanded="true" class="dropdown-toggle">Richieste</a>
									<ul class="collapse list-unstyled" id="homeSubmenuRichieste">
										<li><a href="./visualizzaRichiesteIscrizione">Richieste di iscrizione</a></li>
										<li><a href="./visualizzaRichiesteConvenzionamento">Richieste di convenzionamento</a></li>	
									</ul>
								</li>
								<li><a href="#homeSubmenuDomande" data-toggle="collapse" aria-expanded="true" class="dropdown-toggle active">Domande di tirocinio</a>
									<ul class="collapse list-unstyled" id="homeSubmenuDomande">
										<li><a href="./visualizzaDomandeTirocinioInAttesaUfficio" class="active">Domande in attesa</a></li>
										<li><a href="./visualizzaDomandeTirocinioValutateUfficio">Domande valutate</a></li>	
									</ul>
									<li><a href="./visualizzaTirociniInCorsoUfficio">Tirocini in corso</a></li>
								</li>
							</ul>
						</div>
					</nav>
					
				<!-- Page Content  -->
					<div id="content" class="p-4 p-md-5 pt-5">
						<div class="container">	
							<h4>
								<span class="my-4 header">Domande di tirocinio in attesa</span>
							</h4>
							<input class="form-control" id="filter" type="text"
								placeholder="Filtra Domande">
							<table id="parentTable" data-toggle="table" data-sortable="true"
								data-detail-view="true" data-pagination="true" data-page-size="5">
								<thead>
									<tr>
										<th class="d-none">Hidden nested details table</th>
										<th class="detail"></th>
										<th class="detail"></th>
										<th class="detail titolo" data-sortable="true">ID Domanda</th>
										<th data-sortable="true" class="titolo">Studente</th>
										<th data-sortable="true" class="titolo">Matricola</th>
										<th data-sortable="true" class="titolo">Azienda</th>
										<th data-sortable="true" class="titolo">Progetto</th>
									</tr>	
								</thead>
								<tbody>								
									<c:forEach items="${domandeTirocinio}" var="current" varStatus="loop">
										<tr>
											<td>
												<dl>
													<dt>CFU:</dt>
													<dd>${current.cfu}</dd>
													<br>
													<dt>Data inizio:</dt>
													<dd><fmt:parseDate  value="${current.dataInizio}"  type="date" pattern="yyyy-MM-dd" var="parsedDate" /><fmt:formatDate value="${parsedDate}" pattern = "dd-MM-yyyy"   type="date" var="stdDatum" /><c:out value="${stdDatum}"></c:out></dd>
													<br>
													<dt>Data fine:</dt>
													<dd><fmt:parseDate  value="${current.dataFine}"  type="date" pattern="yyyy-MM-dd" var="parsedDate" /><fmt:formatDate value="${parsedDate}" pattern = "dd-MM-yyyy"   type="date" var="stdDatum" /><c:out value="${stdDatum}"></c:out></dd>
													<br>
													<dt>Sede azienda:</dt>
													<dd>${current.azienda.sede}</dd>
													<br>
													<dt>Settore azienda:</dt>
													<dd>${current.azienda.settore}</dd>
													<br>
													<dt>Attività del tirocinio:</dt>
													<dd>${current.progettoFormativo.attivita}</dd>
													<br>
													<dt>Conoscenze richieste:</dt>
													<dd>${current.progettoFormativo.conoscenze}</dd>
													<br>
													<dt>Conoscenze Studente:</dt>
													<dd>${current.conoscenze}</dd>
													<br>
													<dt>Motivazioni Studente:</dt>
													<dd>${current.motivazioni}</dd>
													<br>	
												</dl>
											</td>
											<td class="valuta testo-tabella">
												<form name="approvaForm" method="POST"
													action="./approvazioneDomandaTirocinio">
													<input type="hidden" name="idDomanda"
														value="${current.id}">
													<button class="btn btn-success tir"  data-toggle="tooltip" title="Approva domanda">
														<i class="fa fa-check"></i>
													</button>
												</form>
											<td class="valuta testo-tabella">
												<form name="respingiForm" method="POST"
													action="./respintaDomandaTirocinio">
													<input type="hidden" name="idDomanda"
														value="${current.id}">
													<button class="btn btn-danger tir"  data-toggle="tooltip" title="Respingi domanda">
														<i class="fa fa-times"></i>
													</button>
												</form>
											</td>
											<td class="testo-tabella">Domanda ${current.id}</td>
											<td class="testo-tabella">${current.studente.nome} ${current.studente.cognome}</td>
											<td class="testo-tabella">${current.studente.matricola}</td>
											<td class="testo-tabella">${current.azienda.ragioneSociale}</td>
											<td class="testo-tabella">${current.progettoFormativo.nome}</td>											
										</tr>											
									</c:forEach>
								</tbody>
							</table>
						</div>
					</div>	
				</div>
			</div>
		</div>
	<%@ include file="footer.jsp"%>
	
	<script src="webjars/jquery/3.3.1/jquery.min.js"></script>
	<script src="webjars/bootstrap/4.4.1/js/bootstrap.min.js"></script>
	<script src="./resources/js/bootstrap-table.min.js"></script>
	<script src="./resources/js/sidebar.js"></script>
	<c:if test="${message != null}">
		<%@ include file="modalNotifica.jsp"%>
	</c:if>

	<script>
		// Load detail view
		$('#parentTable').on('expand-row.bs.table',
				function(e, index, row, $detail) {

					// Get subtable from first cell
					var $rowDetails = $(row[0]);

					// Give new id to avoid conflict with first cell    
					var id = $rowDetails.attr("id");
					$rowDetails.attr("id", id + "-Show");

					// Write rowDetail to detail
					$detail.html($rowDetails);

					return;

				})

		/*filtraggio campi*/
		$(document)
				.ready(
						function() {
							$("#filter")
									.on(
											"keyup",
											function() {
												var value = $(this).val()
														.toLowerCase();
												$("#parentTable tbody tr")
														.filter(
																function() {
																	$(this)
																			.toggle(
																					$(
																							this)
																							.text()
																							.toLowerCase()
																							.indexOf(
																									value) > -1)
																});
											});
						});

		//show modal
	</script>
</body>
</html>