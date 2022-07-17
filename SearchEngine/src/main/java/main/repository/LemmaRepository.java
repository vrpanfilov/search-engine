package main.repository;

import main.model.Lemma;
import main.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Transactional
    void deleteAllInBatchBySite(Site site);
}
