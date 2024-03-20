package io.burpabet.betting.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public String homePage(Model model) {
        return "index";
    }

    @GetMapping("/notice")
    public String noticePage(Model model) {
        return "notice";
    }

    @GetMapping("/bets-placed")
    public String betPlacedPage(Model model) {
        return "bet-placed";
    }

    @GetMapping("/bets-settled")
    public String betSettledPage(Model model) {
        return "bet-settled";
    }

    @GetMapping("/races")
    public String racesPage(Model model) {
        return "races";
    }
}
