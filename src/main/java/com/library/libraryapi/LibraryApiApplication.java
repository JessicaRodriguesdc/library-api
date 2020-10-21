package com.library.libraryapi;

import com.library.libraryapi.service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class LibraryApiApplication extends SpringBootServletInitializer {

//	@Autowired
//	private EmailService emailService;

	//cria uma instancia para servir a toda a aplicacao
	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}

//	@Bean
//	public CommandLineRunner runner(){
//		return args -> {
//			List<String> emails = Arrays.asList("library-api-c26b8d@inbox.mailtrap.io");
//			emailService.sendMails("Testando serviço de emails.",emails);
//			System.out.println("EMAILS ENVIADOS");
//		};
//	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}
}
