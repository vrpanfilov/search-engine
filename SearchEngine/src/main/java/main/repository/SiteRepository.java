package main.repository;

import main.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Integer> {
    @Transactional
    Optional<Site> findByUrl(String url);

    @Transactional
    List<Site> findAllByUrl(String url);

    Optional<Site> findByNameAndType(String name, String type);

    @Query(value = "select count(*) from site s where s.type = 'INDEXED'",
            nativeQuery = true)
    Integer findSiteCount();
}
