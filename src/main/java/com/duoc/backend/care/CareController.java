package com.duoc.backend.care;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cares")
public class CareController {

    @Autowired
    private CareService careService;

    @GetMapping
    public List<Care> getAllCares() {
        return careService.getAllCares();
    }

    @GetMapping("/{id}")
    public Care getCareById(@PathVariable Long id) {
        return careService.getCareById(id);
    }

    @PostMapping
    public Care saveCare(@RequestBody Care care) {
        return careService.saveCare(care);
    }

    @DeleteMapping("/{id}")
    public void deleteCare(@PathVariable Long id) {
        careService.deleteCare(id);
    }
}
