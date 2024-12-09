package vn.hoidanit.jobhunter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.service.CompanyService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/companies")
    public ResponseEntity<Company> createNewCompanies(@Valid @RequestBody Company postCompany) {
        Company new_Company = this.companyService.handleCreateCompany(postCompany);
        return ResponseEntity.status(HttpStatus.CREATED).body(new_Company);

    }

    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getAllCompany() {
        return ResponseEntity.ok().body(this.companyService.fetchAllCompany());
    }

    @PutMapping("/companies")
    public ResponseEntity<Company> putUpdateCompany(@Valid @RequestBody Company company) {

        return ResponseEntity.ok().body(this.companyService.updateCompany(company));
    }

    // @DeleteMapping("/users/{id}")
    // public ResponseEntity<String> delateUser(@PathVariable("id") long id) {

    // this.userService.handleDeleteUser(id);
    // return ResponseEntity.status(HttpStatus.OK).body("delete user");
    // }
    @DeleteMapping("/companies/{id}")
    public ResponseEntity<String> deleteCompany(@PathVariable("id") long id) {
        this.companyService.handleDeleteCompany(id);
        return ResponseEntity.ok("Delete Company success");
    }

}
