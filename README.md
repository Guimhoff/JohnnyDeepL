<img src="/images/icone.png" width="60" />

# Johnny DeepL


Projet de développement mobile ESIREM :

Réalisation d'une application Android utilisant l'API DeepL pour réaliser des traductions.

[Sujet réalisé par M. Meunier](https://www.lamarmotte.info/wp-content/uploads/2023/01/Projet-Android-2023.pdf)


## Fonctionnalités

**Vue principale (traduction)**
* Sélection de la langue de départ, y compris détection de la langue
* Sélection de la langue de traduction
* Traduction du texte
* Affichage de la langue détectée
* Coller le texte du presse papier dans l'encart du texte à traduire
* Copier le texte traduit dans le presse papier
* Ouverture automatique de la fenêtre de configuration en l'absence de clé

**Vue historique**
* Conservation en mémoire des dernières langues utilisées, même après la fermeture de l'application
* Historique des 10 dernières traductions (la limite est imposée par le sujet, et pourrait être très simplement retirée)
* Possibilité d'afficher les traductions de l'historique dans l'affichage principal (utile pour les longs textes)
* Suppression individuelle les traductions de l'historique
* Suppression de la totalité les traductions de l'historique

**Vue clé / configuration**
* Configuration de la clé DeepL utilisée pour les traductions
* Sauvegarde uniquement les clés fonctionnelles
* Possibilité de retirer la clé sauvegarder (en laissant simplement le champ vide)
* Affichage de la consommation de la clé DeepL (caractères utilisés et caractères disponibles)

## Visuels

Vue principale :

<img src="/images/vue_trad_1.jpg" width="200" />

Exemple de traduction avec détection de langue :

<img src="/images/vue_trad_2.jpg" width="200" />

Utilisation des touches de copier-coller avec langue imposée :

<img src="/images/vue_trad_3.jpg" width="200" />

Affichage de l'historique :

<img src="/images/vue_hist.jpg" width="200" />

Affichage d'une traduction sauvegardée dans la vue principale :

<img src="/images/vue_trad_charg.jpg" width="200" />

Vue de configuration quand la clé n'est pas entrée (attention la traduction est impossible sans clé) :

<img src="/images/vue_cle_1.jpg" width="200" />

Vue de configuration quand la clé a été rentrée, avec affichage de la consommation :

<img src="/images/vue_cle_2.jpg" width="200" />

**Mode sombre !**

Vue principale sombre :

<img src="/images/vue_trad_sombre.jpg" width="200" />

Vue historique sombre :

<img src="/images/vue_hist_sombre.jpg" width="200" />

Vue configuration sombre :

<img src="/images/vue_cle_sombre.jpg" width="200" />




