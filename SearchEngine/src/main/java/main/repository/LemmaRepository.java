package main.repository;

import main.model.Lemma;
import main.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Transactional
    void deleteAllInBatchBySite(Site site);

    @Query(
            value = "select count(*) from lemma l " +
                    "left join site s on s.id = l.site_id and s.type = 'INDEXED'",
            nativeQuery = true)
    Integer findLemmaCount();

    @Query(value = "select count(*) from lemma l " +
            "where l.site_id = ?1",
            nativeQuery = true)
    Integer findLemmaCountInSite(Integer siteId);
}
