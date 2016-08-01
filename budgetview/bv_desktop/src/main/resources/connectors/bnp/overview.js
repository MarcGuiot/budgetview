var chart;

function genererGrapheBloc(idBloc, dataArray, msgInfo) {
	var gTitle = null;
	if(dataArray==null || dataArray.length==0){
		gTitle = msgInfo;
	}
	chart = new Highcharts.Chart( {
		chart : {
			renderTo : idBloc,
			defaultSeriesType : 'column',
			backgroundColor : null,
			marginTop : 30
		},
		credits : {
			enabled : false
		},
		title : {
			text : gTitle
		},
		xAxis : {
			categories : [ ' ' ]
		},
		yAxis : {
			title : {
				text : null
			},
			labels : {
				formatter : function() {
					return Highcharts.numberFormat(this.value, 0) +' '+String.fromCharCode(8364);
				}
			}
		},
		tooltip : {
			formatter : function() {
				return this.series.name + '<br/><b>'
						+ Highcharts.numberFormat(this.y, 2) + '</b> '+String.fromCharCode(8364);
			}
		},
		plotOptions : {
			column : {
				pointPadding : 0.2,
				pointWidth: 35,
				borderWidth : 0
			}
		},
		series : dataArray
	});

}

function preparerGraphe(idBlocCredits, idBlocDebits) {
	Highcharts.setOptions({
		lang:{
			thousandsSep : ".",
			decimalPoint : ","
		}
	});

	jQuery('#containerDebits').hide();
	jQuery('.choixGraphe input[name="choixGraphe"]').change(function() {
		if (jQuery(this).val() == "credits") {
			jQuery('#'+idBlocDebits).hide();
			jQuery('#'+idBlocCredits).show();
		} else {
			jQuery('#'+idBlocDebits).show();
			jQuery('#'+idBlocCredits).hide();
		}
	});
}


//Submit a form and set the eventId
// needed for executing the webflow action
function doSubmit(eventId,contractId) {
	document.showContractsOverview._eventId.value=eventId;
	document.showContractsOverview.contractId.value=contractId;
	document.showContractsOverview.groupId.value=document.getElementById("groupSelected").value;
	document.showContractsOverview.submit();
}

function doSubmitTdb(eventId,contractId) {
	document.showContractsOverview._eventId.value=eventId;
	document.showContractsOverview.contractId.value=contractId;
	document.showContractsOverview.groupId.value='-1';
	document.showContractsOverview.submit();
}

function goToStatements(contractId,pastOrPendingOperations,ficheId) {
	document.goToApplication.externalIAId.value='IAStatements';
	document.goToApplication.cboFlowName.value='flow/iastatement';
	document.goToApplication.contractId.value=contractId;
	document.goToApplication.pastOrPendingOperations.value=pastOrPendingOperations;
	document.goToApplication.groupId.value=document.getElementById("groupSelected").value;
	document.goToApplication.step.value='STAMENTS';
	document.goToApplication.pageId.value=ficheId;
	document.goToApplication.submit();
}

function goToStatementsGraph(ficheId) {
	document.goToApplication.externalIAId.value='IAStatements';
	document.goToApplication.cboFlowName.value='flow/iastatement';
	document.goToApplication.groupId.value=document.getElementById("groupSelected").value;
	document.goToApplication.step.value='GRAPH';
	document.goToApplication.pageId.value=ficheId;
	document.goToApplication.submit();
}
function goToPreferences() {
	document.goToApplication.externalIAId.value='IAPreferences';
	document.goToApplication.cboFlowName.value='flow/iapreference';
	document.goToApplication.groupId.value=document.getElementById("groupSelected").value;
	document.goToApplication.submit();
}

function doSubmitHomeContract(eventId,contractId) {
	document.showContractsOverview._eventId.value=eventId;
	document.showContractsOverview.contractId.value=contractId;
	document.showContractsOverview.groupId.value='-2';
	document.showContractsOverview.submit();
}

function doSubmitOthers(eventId,contractId) {
	document.showContractsOverview._eventId.value=eventId;
	document.showContractsOverview.contractId.value=contractId;
	document.showContractsOverview.groupId.value='-3';
	document.showContractsOverview.submit();
}
function doSubmitWithGroupId(eventId,contractId,groupId) {
	document.showContractsOverview._eventId.value=eventId;
	document.showContractsOverview.contractId.value=contractId;
	document.showContractsOverview.groupId.value=groupId;
	document.showContractsOverview.submit();
}

function doSubmitCustomizedLongLabel(eventId,contractId) {

	var newLibelle = document.getElementById("newLibellePerso").value;
	var Expression = new RegExp("((.*)((<)|(</))(ARTICLE|AUDIO|COMMAND|SECTION|SOURCE|VIDEO|ISINDEX|STYLE|BODY|IMG|FORM|JAVASCRIPT|VBSCRIPT|EXPRESSION|APPLET|META|XML|BLINK|LINK|STYLE|SCRIPT|EMBED|OBJECT|IFRAME|FRAME|FRAMESET|ILAYER|LAYER|BGSOUND|TITLE|BASE|BACKGROUND)(.*))|((.*)(ARTICLE|AUDIO|COMMAND|SECTION|SOURCE|VIDEO|ISINDEX|STYLE|BODY|IMG|FORM|JAVASCRIPT|VBSCRIPT|EXPRESSION|APPLET|META|XML|BLINK|LINK|STYLE|SCRIPT|EMBED|OBJECT|IFRAME|FRAME|FRAMESET|ILAYER|LAYER|BGSOUND|TITLE|BASE)( *)(>))|((.*)(CITE|ICON|ONFORMINPUT|ONINPUT|ONINVALID|ONCANPLAY|ONCANPLAYTHROUGH|ONDURATIONCHANGE|ONEMPTIED|ONENDED|ONLOADEDDATA|ONLOADEDMETADATA|ONLOADSTART|ONPAUSE|ONPLAY|ONPLAYING|ONPROGRESS|ONRATECHANGE|ONSEEKED|ONSEEKING|ONSTALLED|ONSUSPEND|ONTIMEUPDATE|ONVOLUMECHANGE|ONWAITING|ONMESSAGE|ONSHOW|ACTION|HREF|STYLE|ONABORT|ONACTIVATE|ONAFTERPRINT|ONAFTERUPDATE|ONBEFOREACTIVATE|ONBEFORECOPY|ONBEFORECUT|ONBEFOREDEACTIVATE|ONBEFOREEDITFOCUS|ONBEFOREPASTE|ONBEFOREPRINT|ONBEFOREUNLOAD|ONBEFOREUPDATE|ONBLUR|ONBOUNCE|ONCELLCHANGE|ONCHANGE|ONCLICK|ONCONTEXTMENU|ONCONTROLSELECT|ONCOPY|ONCUT|ONDATAAVAILABLE|ONDATASETCHANGED|ONDATASETCOMPLETE|ONDBLCLICK|ONDEACTIVATE|ONDRAG|ONDRAGEND|ONDRAGENTER|ONDRAGLEAVE|ONDRAGOVER|ONDRAGSTART|ONDROP|ONERROR|ONERRORUPDATE|ONFILTERCHANGE|ONFINISH|ONFOCUS|ONFOCUSIN|ONFOCUSOUT|ONHELP|ONKEYDOWN|ONKEYPRESS|ONKEYUP|ONLAYOUTCOMPLETE|ONLOAD|ONLOSECAPTURE|ONMOUSEDOWN|ONMOUSEENTER|ONMOUSELEAVE|ONMOUSEMOVE|ONMOUSEOUT|ONMOUSEOVER|ONMOUSEUP|ONMOUSEWHEEL|ONMOVE|ONMOVEEND|ONMOVESTART|ONPASTE|ONPROPERTYCHANGE|ONREADYSTATECHANGE|ONRESET|ONRESIZE|ONRESIZEEND|ONRESIZESTART|ONROWENTER|ONROWEXIT|ONROWSDELETE|ONROWSINSERTED|ONSCROLL|ONSELECT|ONSELECTIONCHANGE|ONSELECTSTART|ONSTART|ONSTOP|ONSUBMIT|ONUNLOAD)(.*)=(.*))|((.*)(URL\\s*)=(.*))", "i");
	if (Expression.test(newLibelle)) {
		alert("Votre saisie contient des caract\u00e8res non valides.");
		return false;
	} else {
		document.showContractsOverview._eventId.value=eventId;
		document.showContractsOverview.contractId.value=contractId;
		document.showContractsOverview.customizedLongLabel.value=newLibelle;
		document.showContractsOverview.submit();
	}
}

/***********************
 * Page dashboardIntro *
 ***********************/
/**
 * Redirige vers une page du tableau de bord
 * @param eventId
 */

var PAGE_MOUVEMENT = "Mouvement";
var PAGE_CATEGORIE = "Categorie";

var TRANSITION_MOUVEMENT = "TYPE";
var TRANSITION_CATEGORIE = "CATEGORY";

function gotoDashboard(eventId) {


	var pageType = (eventId == "byCategory") ? PAGE_CATEGORIE : PAGE_MOUVEMENT;

	var cookieDisponible = false;

	var identifiant = getCookie("identifiant");
	if (identifiant != null) {
		var listeParams = getCookie("dashboard"+pageType+identifiant);
		if (listeParams != null) {
			cookieDisponible = true;

			var params = listeParams.split("-");
			var idGraphe = params[0];

			// Affichage des graphes
			if (idGraphe < 1 && idGraphe > 4) {
				idGraphe = (pageType == PAGE_CATEGORIE) ? 3 : 1;
			}
			remplirChampsStyle(idGraphe);

			remplirChampFormulaire(3, params, "BeginDate", idGraphe);
			remplirChampFormulaire(4, params, "EndDate", idGraphe);
			remplirChampFormulaire(7, params, "Contracts", idGraphe);

			// Graphes sur les modes de paiement
			if (pageType == PAGE_MOUVEMENT) {
				remplirChampFormulaire(5, params, "OpTypes", idGraphe);
			}
			// Graphes sur les categories
			else if (pageType == PAGE_CATEGORIE) {
				if (document.getElementById("g"+idGraphe+"Type") != null) {
					document.getElementById("g"+idGraphe+"Type").value = params[2];
				}
				remplirChampFormulaire(6, params, "Categs", idGraphe);
			}
		}
	}

	// Si pas de cookie, on affiche le 1er graphe de la page
	if (!cookieDisponible) {
		if (pageType == PAGE_CATEGORIE) {
			remplirChampsStyle(3);
		} else {
			remplirChampsStyle(1);
		}
	}

	var transition = (eventId == "byCategory") ? TRANSITION_CATEGORIE : TRANSITION_MOUVEMENT;


	document.goToApplication.externalIAId.value='IAStatements';
	document.goToApplication.cboFlowName.value='flow/iastatement';
	document.goToApplication.groupId.value=document.getElementById("groupSelected").value;
	document.goToApplication.step.value='GRAPH';
	document.goToApplication.pageId.value='tdb';
	document.goToApplication.entryDashboard.value=transition;
	document.goToApplication.submit();



}

/**
 * Remplir un champ caché du formulaire pour accéder au TDB
 * @param position La position du champ dans le cookie
 * @param params Le tableau des paramètres stocké dans le cookie
 * @param suffixe Le suffixe du nom du paramètres attendu par la couche EE
 * @param idGraphe Le numéro du graphe auquel correspond le champ
 */
function remplirChampFormulaire(position, params, suffixe, idGraphe) {
	var fieldType = document.getElementById(suffixe);
	if (fieldType != null) {
		fieldType.name = "g"+idGraphe+suffixe;
		fieldType.value = params[position];
	}
}

/**
 * Remplir les champs cachés du formulaire pour l'affichage/masquage des graphes
 * @param numeroGraphe Le numéro du graphe qui doit être affiché
 */
function remplirChampsStyle(numeroGraphe) {
	document.getElementById("g1Style").value = (numeroGraphe == 1) ? "expand" : "collapse";
	document.getElementById("g2Style").value = (numeroGraphe == 2) ? "expand" : "collapse";
	document.getElementById("g3Style").value = (numeroGraphe == 3) ? "expand" : "collapse";
	document.getElementById("g4Style").value = (numeroGraphe == 4) ? "expand" : "collapse";
}