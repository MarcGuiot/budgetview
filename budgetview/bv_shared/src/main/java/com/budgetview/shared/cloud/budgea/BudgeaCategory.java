package com.budgetview.shared.cloud.budgea;

public enum BudgeaCategory {

  INDEFINI(9998, "Indéfini"),
  CHEQUE(112, 9998, "Chèque"),
  CHEQUE2(115, 9998, "Chèque"),
  E_COMMERCE(108, 9998, "E-commerce"),
  RETRAIT(1, 9998, "Retrait"),
  VIREMENT(88, 9998, "Virement"),

  AIDE_ALLOCATIONS(91, "Aide / Allocations"),

  ALIMENTATION(6, "Alimentation"),
  BOUCHER_POISSONNIER(40, 6, "Boucher / Poissonnier"),
  BOULANGER(41, 6, "Boulanger"),
  CANTINE(106, 6, "Cantine"),
  CAVISTE(45, 6, "Caviste"),
  EPICIER(44, 6, "Epicier"),
  MARAICHER(42, 6, "Maraîcher"),
  SUPERMARCHE(39, 6, "Supermarché"),
  TRAITEUR_CHOCOLATIER(43, 6, "Traiteur / Chocolatier"),

  BANQUE_ASSURANCE(13, "Banque / Assurance"),
  ASSURANCE_AUTO_HABITATION(81, 13, "Assurance auto/habitation"),
  ASSURANCE_DECES(82, 13, "Assurance décès"),
  ASSURANCE_DIVERS(119, 13, "Assurance divers"),
  COTISATION_RETRAITE(125, 13, "Cotisation retraite"),
  FRAIS_BANCAIRES(95, 13, "Frais bancaires"),
  REMBOURSEMENT_CREDIT(96, 13, "Remboursement crédit"),

  BEAUTE_SHOPPING(7, "Beauté / Shopping"),
  ACCESSOIRES_BIJOUX(114, 7, "Accessoires / Bijoux"),
  COIFFEUR(49, 7, "Coiffeur"),
  ESTHETIQUE(50, 7, "Esthétique"),
  PARFUMERIE(48, 7, "Parfumerie"),
  SHOPPING_VETEMENTS(46, 7, "Shopping / Vêtements"),

  CADEAUX(94, "Cadeaux"),

  CADEAUX_DONS(18, "Cadeaux / Dons"),
  CADEAUX2(97, 18, "Cadeaux"),
  CARITATIFS_HUMANITAIRES(17, 18, "Caritatifs / Humanitaires"),
  PRET_A_UN_TIERS(124, 18, "Prêt à un tiers"),

  COMMUNICATION(10, "Communication"),
  INTERNET_TV(61, 10, "Internet / TV"),
  POSTE(62, 10, "Poste"),
  TELEPHONE(60, 10, "Téléphone"),

  CREDIT_EMPRUNT(122, "Crédit / Emprunt"),

  FAMILLE_ANIMAUX(11, "Famille / Animaux"),
  ACCESSOIRES_BEBE(113, 11, "Accessoires bébé"),
  ANIMALERIE(79, 11, "Animalerie"),
  ARGENT_DE_POCHE(67, 11, "Argent de poche"),
  GARDE_ACTIVITE_ENFANTS(66, 11, "Garde / Activité enfants"),
  HABILLEMENT_ENFANT(64, 11, "Habillement enfant"),
  JOUETS(65, 11, "Jouets"),
  PENSION_ALIMENTAIRE(126, 11, "Pension alimentaire"),
  REMBOURSEMENT_PRET_FAMILIAL(100, 11, "Remboursement prêt familial"),
  VETERINAIRE(78, 11, "Vétérinaire"),

  FORMATION_EDUCATION(12, "Formation / Education"),
  CREDIT_PRET_ETUDIANT(63, 12, "Crédit / Prêt étudiant"),
  FORMATION(69, 12, "Formation"),
  FOURNITURES_SCOLAIRES(71, 12, "Fournitures scolaires"),
  SCOLARITE(68, 12, "Scolarité"),
  SOUTIEN_SCOLAIRE(70, 12, "Soutien scolaire"),

  FRAIS_PROFESSIONNELS(118, "Frais professionnels"),

  IMPOTS_ADMINISTRATIF(14, "Impôts / Administratif"),
  AVOCAT(75, 14, "Avocat"),
  CONSEIL_JURIDIQUE(76, 14, "Conseil juridique"),
  CONTRAVENTIONS(109, 14, "Contraventions"),
  DEFISCALISATION(77, 14, "Défiscalisation"),
  IMPOT_SUR_LE_REVENU(73, 14, "Impôt sur le revenu"),
  IMPOTS_FONCIERS(102, 14, "Impôts fonciers"),
  IMPOTS_LOCAUX(103, 14, "Impôts locaux"),
  ISF(72, 14, "ISF"),
  TAXE_D_HABITATION(74, 14, "Taxe d'habitation"),

  INTERETS(92, "Intérêts"),

  LOCATION(93, "Location"),

  LOGEMENT(3, "Logement"),
  BRICOLAGE_JARDINAGE(20, 3, "Bricolage / Jardinage"),
  CREDIT_PRET_IMMOBILIER(24, 3, "Crédit / Prêt immobilier"),
  CUISINE_ELECTROMENAGER(26, 3, "Cuisine / Électroménager"),
  DECORATION_MOBILIER(22, 3, "Décoration / Mobilier"),
  EAU(29, 3, "Eau"),
  ELECTRICITE(28, 3, "Electricité"),
  ENTRETIEN(21, 3, "Entretien"),
  GAZ(27, 3, "Gaz"),
  LOYER_CHARGES(23, 3, "Loyer / Charges"),
  TRAVAUX(25, 3, "Travaux"),

  LOISIRS(8, "Loisirs"),
  BARS_SORTIES(54, 8, "Bars / Sorties"),
  CINEMA_FILMS(53, 8, "Cinéma / Films"),
  CULTURE_INFOS(51, 8, "Culture / Infos"),
  HIGHTECH(121, 8, "Hightech"),
  LOISIRS_CREATIFS(116, 8, "Loisirs créatifs"),
  MULTIMEDIA_LIVRES(52, 8, "Multimédia / Livres"),
  MUSIQUE(56, 8, "Musique"),
  RESTAURANT(84, 8, "Restaurant"),
  SPORTS_ACTIVITES(55, 8, "Sports / Activités"),
  TABAC_JEUX_D_ARGENT(98, 8, "Tabac / Jeux d'argent"),

  RETRAITE(120, "Retraite"),

  SALAIRE(86, "Salaire"),

  SANTE(9, "Santé"),
  DENTAIRE(111, 9, "Dentaire"),
  MEDECIN_HOPITAL(57, 9, "Médecin / Hopital"),
  MUTUELLE(59, 9, "Mutuelle"),
  OPTIQUE(110, 9, "Optique"),
  PHARMACIE(58, 9, "Pharmacie"),
  SPECIALISTE(47, 9, "Spécialiste"),

  TRANSPORT_VOYAGES(5, "Transport / Voyages"),
  ENTRETIEN_GARAGE_VEHICULE(31, 5, "Entretien / Garage véhicule"),
  ESSENCE(30, 5, "Essence"),
  HOTEL(107, 5, "Hôtel"),
  LOCATION_ACHAT_VEHICULE(38, 5, "Location / Achat véhicule"),
  PARKING(32, 5, "Parking"),
  PEAGE(37, 5, "Péage"),
  TAXI(36, 5, "Taxi"),
  TRANSPORT_EN_COMMUN(33, 5, "Transport en commun"),
  VOYAGE_EN_TRAIN_AVION(34, 5, "Voyage en train/avion"),

  VACANCES(117, "Vacances"),

  VENTE(123, "Vente"),

  VENTE_ACTIONS_DIVIDENDES(90, "Vente actions / Dividendes"),

  VIREMENT_INTERNE(85, "Virement interne"),
  EPARGNE(104, 85, "Épargne");


  private final int id;
  private final int parentId;
  private final String name;

  BudgeaCategory(int id, String name) {
    this(id, -1, name);
  }

  BudgeaCategory(int id, int parentId, String name) {
    this.id = id;
    this.parentId = parentId;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
