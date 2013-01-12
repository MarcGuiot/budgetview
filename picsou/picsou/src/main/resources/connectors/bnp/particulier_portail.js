/**
* Permet de réaliser des changements de catégorie.
*/
function SwapCategorie(nom, image, typeClient) {
	if (categorieCourante != null
		&& imageCateCourante !=null
		&& document.getElementById(imageCateCourante)){
		document.getElementById(categorieCourante).style.display = "none";
		if(typeClient == "bpf"){
			document.getElementById(imageCateCourante).src = IMG_PLUS_BPF;
		}else{
			document.getElementById(imageCateCourante).src = IMG_PLUS;
		}
	}
	if (categorieCourante != nom) {
		document.getElementById(nom).style.display = "block";
		if(typeClient == "bpf"){
			document.getElementById(image).src = IMG_MOINS_BPF;
		}else{
			document.getElementById(image).src = IMG_MOINS;
		}
		categorieCourante = nom;
		imageCateCourante = image;
	} else {
		categorieCourante = null;
		imageCateCourante = null;
	}
}
function SwapDossier(nom, image) {
	if (dossierCourant != null){
	   	if (document.all){
			document.all[dossierCourant].style.display = "none";
			document.all[imageDossierCourant].src = IMG_PLUS;
		} else {
			document.getElementById(dossierCourant).style.display = "none";
			document.getElementById(imageDossierCourant).src = IMG_PLUS;
		}
		if (dossierCourant != nom) {
			if (document.all){
				document.all[nom].style.display = "block";
				document.all[image].src = IMG_MOINS;
			} else {
				document.getElementById(nom).style.display = "block";
				document.getElementById(image).src = IMG_MOINS;
			}
			dossierCourant = nom;
			imageDossierCourant = image;
		} else {
			dossierCourant = null;
			imageDossierCourant = null;
		}
	} else {
		if (document.all){
			document.all[nom].style.display = "block";
			document.all[image].src = IMG_MOINS;
		} else {
			document.getElementById(nom).style.display = "block";
			document.getElementById(image).src = IMG_MOINS;
		}
		dossierCourant = nom;
		imageDossierCourant = image;
	}
}
/* Permet d'activer les liens sociaux. */
function activateLiensSociaux(){
	var ligne = document.getElementById("impression");
	for( var i = 0; i < ligne.childNodes.length; i++ ){
		var fils = ligne.childNodes[i];
		if(fils.className != undefined){
			if("lien_social" == fils.className){
				fils.className = "";
			}
		}
	}
}
function mentionslegales() {
	top.open(urlMentionsLegales,"MentionLegales","width=790,height=400,toolbar=no,directories=no,resizable=no,scrollbars=yes,location=no,status=no,menubar=no");
}
function ValidRechForm(form) {
	form.motCle.value = CleanLeftRightSpace(form.motCle.value);
	if (form.motCle.value.length == 0 || form.motCle.value == "rechercher") {
		return false;
	} else {
		return true;
	}
}

function traiterFocusChampRecherche(champ) {
	if (champ.value == "Rechercher") champ.value = "";
}
function traiterBlurChampRecherche(champ) {
	if (champ.value == "") champ.value = "Rechercher";
}
function toggleButton(bt,state) {
	var reg = new RegExp("_on\.|_off\.", "g");
	bt.src = bt.src.replace(reg,'_'+state+'.');
}
function testSaisie() {
	var exp = new RegExp("^([0-9]{2}\\s*){5}$" );
	var tel1 = document.getElementById("idTel1").value;
	var tel2 = document.getElementById("idTel2").value;

	var ok1 = true;
	var ok2 = true;

	ok1 = tel1.match(exp);
	document.getElementById("erreurSaisie1").style.display = ok1 ? "none" : "block";

	ok2 = (tel1 == tel2);
	document.getElementById("erreurConfirm").style.display = ok2 ? "none" : "block";

	if (ok1 && ok2) document.forms['saisieTel'].submit();
}

Width=screen.availWidth;
Height=screen.availHeight;

var ns4 = (document.layers)? true:false; //NS 4
var ie4 = (document.all)? true:false; //IE 4
var dom = (document.getElementById)? true:false;	 //DOM
function getLeft(MyObject) {
	if (dom || ie4) {
		if (MyObject.offsetParent)
			return (MyObject.offsetLeft + getLeft(MyObject.offsetParent));
		else
			return (MyObject.offsetLeft);
	}
	if (ns4) return (MyObject.x);
}
function getTop(MyObject) {
	if (dom || ie4) {
		if (MyObject.offsetParent)
			return (MyObject.offsetTop + getTop(MyObject.offsetParent));
		else
			return (MyObject.offsetTop);
	}
	if (ns4) return (MyObject.y);
}

var currentLayer;
function showHideLayers(element,layerId,visible) {
	var obj = document.getElementById(layerId);
	obj.style.display = (visible?"block":"none");
	if (visible && currentLayer && (currentLayer!=obj)) currentLayer.style.display = 'none';
	currentLayer = obj;
}