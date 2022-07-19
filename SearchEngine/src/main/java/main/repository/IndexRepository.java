package main.repository;

import main.model.Index;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IndexRepository extends JpaRepository<Index, Integer> {
    @Query(value = "select i.* from `index` i, `page` p " +
//            @Query(value = "select i.id, i.rank, i.lemma_id, i.page_id from `index` i, `page` p " +
            "where p.site_id = ?1 and i.page_id = p.id",
            nativeQuery = true)
    List<Index> findAllBySiteId(Integer siteId);
}
