package com.plewa.irregular_verbs.controllers;

import com.plewa.irregular_verbs.entity.IrregularVerb;
import com.plewa.irregular_verbs.service.IrregularVerbService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/irregularverbs")
public class IrregularVerbsController {

    static final Integer MAX_AMOUNT_VERBS = 69;
    private IrregularVerbService irregularVerbService;

    public IrregularVerbsController(IrregularVerbService irregularVerbService) {
        this.irregularVerbService = irregularVerbService;
    }

    @GetMapping("/home")
    public String getIrregularVerbsHome(HttpSession session) {
        session.invalidate();
        return "irregular_verbs/home";
    }

    @GetMapping("/allIrregularVerbs")
    public String printAllIrregularVerbs(Model model) {
        model.addAttribute("irregularVerbs", irregularVerbService.findAll());
        return "irregular_verbs/allverbs";
    }

    @GetMapping("/setLimit")
    public String getLimitPage() {
        return "irregular_verbs/limitdecide";
    }

    @PostMapping("/fill2and3verbPOST")
    public String fill2And3VerbPOST(@RequestParam int limitVerbs, HttpSession session) {

        if (limitVerbs <= 0 || limitVerbs > MAX_AMOUNT_VERBS) {
            return "irregular_verbs/limitdecide";
        }

        List<IrregularVerb> uniqueIrregularVerbs = irregularVerbService.getUniqueVerbsList(limitVerbs);
        IrregularVerb randomIrregularVerb = irregularVerbService.getOneRandomIrregularVerbFromList(uniqueIrregularVerbs);

        session.setAttribute("uniqueIrregularVerbs", uniqueIrregularVerbs);
        session.setAttribute("progress", 0);
        session.setAttribute("limit", limitVerbs);
        session.setAttribute("randomIrregularVerb", randomIrregularVerb);


        return "redirect:/irregularverbs/fill2and3verb";
    }

    @GetMapping("/fill2and3verb")
    public String fill2And3VerbGET(@RequestParam(defaultValue = "empty") String answer1, @RequestParam(defaultValue = "empty") String answer2, HttpSession session) {
        List<IrregularVerb> uniqueIrregularVerbs = (List<IrregularVerb>) session.getAttribute("uniqueIrregularVerbs");
        IrregularVerb irregularVerb = (IrregularVerb) session.getAttribute("randomIrregularVerb");
        int progress = (int) session.getAttribute("progress");

        if (irregularVerb.getVerb2().equals(answer1) && irregularVerb.getVerb3().equals(answer2)) {
            uniqueIrregularVerbs.remove(irregularVerb);
            progress++;

            if (uniqueIrregularVerbs.isEmpty()) {
                return "irregular_verbs/congratulations";
            }

            session.setAttribute("uniqueIrregularVerbs", uniqueIrregularVerbs);
            session.setAttribute("progress", progress);
        }

        IrregularVerb randomIrregularVerb = irregularVerbService.getOneRandomIrregularVerbFromList(uniqueIrregularVerbs);
        session.setAttribute("randomIrregularVerb", randomIrregularVerb);

        return "irregular_verbs/fillverbs_2and3_limit";
    }


    @GetMapping("/polish-translate-english")
    public String polishTranslateEnglish(HttpSession session) {

        if (session.getAttribute("correctAnswers") == null || session.getAttribute("incorrectAnswers") == null) {
            session.setAttribute("correctAnswers", 0);
            session.setAttribute("incorrectAnswers", 0);
        }

        irregularVerbService.saveInSessionOneRandomIrregularVerb(session); // attribute name: "randomVerb"

        return "irregular_verbs/polish_translate_english";
    }


    @PostMapping("/polish-translate-english/check-answer")
    public String checkIfAnswerIsCorrectPolishTranslateEnglish(RedirectAttributes redirectAttributes, HttpSession session, @RequestParam(defaultValue = "empty") String answer) {
        IrregularVerb randomIrregularVerb = (IrregularVerb) session.getAttribute("randomVerb");

        if (irregularVerbService.checkIfAnswerWasCorrect(randomIrregularVerb.getVerb1(), answer)) {
            irregularVerbService.increaseCorrectAnswersInSession(session); // attribute name: "correctAnswers"
            irregularVerbService.redirectJsonInModelWithCorrectOutput(redirectAttributes); // attribute name: "previousAnswer"
        } else {
            irregularVerbService.increaseIncorrectAnswersInSession(session); // attribute name: "incorrectAnswers"
            irregularVerbService.redirectJsonInModelWithIncorrectOutput(redirectAttributes); // attribute name: "previousAnswer"
        }

        return "redirect:/irregularverbs/polish-translate-english";
    }
}
