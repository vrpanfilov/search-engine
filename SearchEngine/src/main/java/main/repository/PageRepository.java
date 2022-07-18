package main.repository;

import main.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PageRepository extends JpaRepository<Page, Integer> {
    @Query(value = "select count(*) from page p " +
            "left join site s on s.id = p.site_id and s.type = 'INDEXED'",
            nativeQuery = true)
    Integer findPageCount();

    @Query(value = "select count(*) from page p " +
            "where p.site_id = ?1",
            nativeQuery = true)
    Integer findPageCountInSite(Integer siteId);
}
