package com.springbootlearning.learningspringboot.tacosapp.controller;

import com.springbootlearning.learningspringboot.tacosapp.model.Ingredient;
import com.springbootlearning.learningspringboot.tacosapp.model.Taco;
import com.springbootlearning.learningspringboot.tacosapp.model.TacoOrder;
import com.springbootlearning.learningspringboot.tacosapp.model.Type;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// @Slf4j (Lombok) : génère un champ `log` (Logger SLF4J) dans la classe.
// Sert à écrire des logs : log.info(...), log.warn(...), log.error(...)
// Avantage : pas besoin de déclarer manuellement le logger.
@Slf4j

// @Controller (Spring MVC) : indique que cette classe est un contrôleur web.
// Spring la détecte via le component scanning et l'utilise pour gérer des requêtes HTTP.
// Contrairement à @RestController, ici les méthodes retournent souvent un nom de vue (Thymeleaf).
@Controller

// @RequestMapping("/design") (Spring MVC) : définit un préfixe d'URL commun à toutes les routes de ce contrôleur.
// Exemple : si une méthode a @GetMapping, son URL sera /design + (chemin de la méthode).
@RequestMapping("/design")

// @SessionAttributes("tacoOrder") (Spring MVC) : demande à Spring de conserver dans la session HTTP
// un attribut de modèle nommé "tacoOrder" (souvent pour un formulaire multi-étapes / panier).
// Concrètement : si tu ajoutes "tacoOrder" au Model, Spring le stocke aussi en session et le réutilise
// entre plusieurs requêtes jusqu'à ce que tu le "complètes" / nettoies (souvent via SessionStatus).
@SessionAttributes("tacoOrder")
public class DesignTacoController {

    // @ModelAttribute (Spring MVC) sur une méthode :
    // -> cette méthode est appelée AVANT chaque handler (GET/POST) de ce contrôleur,
    //    et ses ajouts au Model sont disponibles pour la vue.
    // Ici : on remplit le Model avec des listes d’ingrédients par type (wrap, protein, etc.).
    @ModelAttribute
    public void addIngredientsToModel(Model model) {
        List<Ingredient> ingredients = Arrays.asList(
                new Ingredient("FLTO", "Flour Tortilla", Type.WRAP),
                new Ingredient("COTO", "Corn Tortilla", Type.WRAP),
                new Ingredient("GRBF", "Ground Beef", Type.PROTEIN),
                new Ingredient("CARN", "Carnitas", Type.PROTEIN),
                new Ingredient("TMTO", "Diced Tomatoes", Type.VEGGIES),
                new Ingredient("LETC", "Lettuce", Type.VEGGIES),
                new Ingredient("CHED", "Cheddar", Type.CHEESE),
                new Ingredient("JACK", "Monterrey Jack", Type.CHEESE),
                new Ingredient("SLSA", "Salsa", Type.SAUCE),
                new Ingredient("SRCR", "Sour Cream", Type.SAUCE)
        );

        Type[] types = Type.values();
        for (Type type : types) {
            model.addAttribute(type.toString().toLowerCase(), filterByType(ingredients, type));
        }
    }

    // @ModelAttribute(name = "tacoOrder") (Spring MVC) :
    // -> crée/ajoute un attribut de modèle nommé "tacoOrder".
    // Comme la classe est annotée @SessionAttributes("tacoOrder"), cet objet est aussi
    // conservé en session pour les prochaines requêtes.
    // Rôle : avoir une TacoOrder “persistante” pendant que l’utilisateur design plusieurs tacos.
    @ModelAttribute(name = "tacoOrder")
    public TacoOrder order() {
        return new TacoOrder();
    }

    // @ModelAttribute(name = "taco") :
    // -> ajoute au Model un objet Taco vide, souvent utilisé comme "form backing bean"
    //    (l’objet sur lequel le formulaire HTML va binder les champs).
    @ModelAttribute(name = "taco")
    public Taco taco() {
        return new Taco();
    }

    // @GetMapping (Spring MVC) :
    // -> mappe les requêtes HTTP GET sur le chemin du contrôleur.
    // Comme la classe a @RequestMapping("/design"), ici c’est GET /design
    // Rôle : afficher la page de formulaire de création de taco.
    @GetMapping
    public String showDesignForm() {
        return "design";
    }

    // @PostMapping (Spring MVC) :
    // -> mappe les requêtes HTTP POST sur le chemin du contrôleur.
    // Donc ici : POST /design
    // Rôle : recevoir les données du formulaire et traiter la soumission.
    @PostMapping
    public String processTaco(
            // @Valid (Jakarta Bean Validation) :
            // -> déclenche la validation de l’objet Taco à partir de ses contraintes
            //    (annotations comme @NotNull, @Size, etc. sur la classe Taco).
            // Si des contraintes échouent, les erreurs sont placées dans `Errors`.
            @Valid Taco taco,

            // Errors (Spring) :
            // -> contient les erreurs de binding/validation après le @Valid.
            // Important : il doit être juste après l’objet validé dans la signature.
            Errors errors,

            // @ModelAttribute (Spring MVC) sur un paramètre :
            // -> récupère l’attribut "tacoOrder" depuis le Model (ou depuis la session
            //    grâce à @SessionAttributes) et l’injecte ici.
            // Rôle : ajouter le taco courant dans la commande en cours.
            @ModelAttribute TacoOrder tacoOrder) {

        if (errors.hasErrors()) {
            return "design";
        }

        tacoOrder.addTaco(taco);
        log.info("Processing taco: {}", taco);

        // "redirect:" (Spring MVC) n’est pas une annotation, mais une convention :
        // -> demande à Spring de renvoyer une redirection HTTP vers /orders/current
        // au lieu de rendre une vue.
        return "redirect:/orders/current";
    }

    private Iterable<Ingredient> filterByType(List<Ingredient> ingredients, Type type) {
        return ingredients.stream()
                .filter(x -> x.getType().equals(type))
                .collect(Collectors.toList());
    }


}