package main;

import main.repository.Repos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//TODO:
// Во время индексирования в статистике отображаются только уже проиндексированные.
// Возможно, стоит контент страницы выделить в отдельную таблицу,
// связанную с таблицей Page один-к-одному.
// Убрать  запрос /api/indexSite, он может быть выполнен в запросе /api/indexPage
// с пустым параметром

@SpringBootApplication
public class SearchEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SearchEngineApplication.class, args);
	}

	@Bean(initMethod="init")
	public Repos getBean() {
		return new Repos();
	}
}
