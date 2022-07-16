package main.repository;

import main.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {
}
