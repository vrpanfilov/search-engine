package main;

import main.repository.Repos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//TODO:
// После сохранения объекта Page в базу данных текстовые поля объекта нужно обнулить.
// Потом, при анализе содержимого страниц, объекты снова надо загружать из БД.
// Это сэкономит память, которой иначе не хватит.

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
