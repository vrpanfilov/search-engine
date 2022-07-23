package main.repository;

import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IndexRepository extends JpaRepository<Index, Integer> {
    @Query(value = "select i.* from `index` i, `page` p " +
            "where p.site_id = ?1 and i.page_id = p.id",
            nativeQuery = true)
    List<Index> findAllBySiteId(Integer siteId);

    List<Index> findAllByLemma(Lemma lemma);
}
