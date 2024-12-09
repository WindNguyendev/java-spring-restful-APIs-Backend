package vn.hoidanit.jobhunter.service;

import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.CompanyRepository;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company handleCreateCompany(Company company) {
        return this.companyRepository.save(company);
    }

    public List<Company> fetchAllCompany() {

        return this.companyRepository.findAll();

    }

    // public User updateCo(User user) {
    // User user_new = new User();
    // try {
    // user_new = this.fetchUserById(user.getId());
    // if (user_new != null) {
    // user_new.setName(user.getName());
    // user_new.setEmail(user.getEmail());
    // user_new.setPassword(user.getPassword());
    // this.userRepository.save(user_new);

    // } else {
    // return null;
    // }
    // } catch (Exception e) {
    // // TODO: handle exception
    // }
    // return user;

    public Company updateCompany(Company company) {

        Optional<Company> c = this.companyRepository.findById(company.getId());
        if (c.isPresent()) {
            Company company_update = c.get();
            company_update.setName(company.getName());
            company_update.setDescription(company.getDescription());
            company_update.setLogo(company.getLogo());
            return this.companyRepository.save(company_update);
        }
        return null;
    }

    public void handleDeleteCompany(long id) {
        Optional<Company> c = this.companyRepository.findById(id);
        if (c.isPresent()) {
            this.companyRepository.delete(c.get());
        }
    }

}
