package com.literalura.literalura.principal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.literalura.literalura.model.Autor;
import com.literalura.literalura.model.Libro;
import com.literalura.literalura.repository.AutorRepository;
import com.literalura.literalura.repository.LibroRepository;
import com.literalura.literalura.service.ConsumoApi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class Principal {

    private final ConsumoApi consumoApi;
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.consumoApi = new ConsumoApi();
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraMenu() {
        Scanner scanner = new Scanner(System.in);
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("------------");
            System.out.println("Elija la opción a través de su número:");
            System.out.println("1- Buscar libro por título");
            System.out.println("2- Listar libros registrados");
            System.out.println("3- Listar autores registrados");
            System.out.println("4- Listar autores vivos en un determinado año");
            System.out.println("5- Listar libros por idioma");
            System.out.println("0- Salir");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1 -> buscarLibroPorTitulo(scanner);
                    case 2 -> listarLibrosRegistrados();
                    case 3 -> listarAutoresRegistrados();
                    case 4 -> listarAutoresVivos(scanner);
                    case 5 -> listarLibrosPorIdioma(scanner);
                    case 0 -> System.out.println("Saliendo...");
                    default -> System.out.println("Opción no válida");
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Ingresa un número válido.");
            }
        }
    }

    private void buscarLibroPorTitulo(Scanner scanner) {
        System.out.print("🔍 Ingresa el título del libro: ");
        String titulo = scanner.nextLine();

        try {
            String json = consumoApi.obtenerDatos("https://gutendex.com/books?search=" + titulo.replace(" ", "%20"));
            JsonNode root = objectMapper.readTree(json);
            JsonNode resultados = root.path("results");

            if (resultados.isArray() && resultados.size() > 0) {
                JsonNode libroJson = resultados.get(0);

                String tituloLibro = libroJson.path("title").asText();
                JsonNode autoresJson = libroJson.path("authors");
                String nombreAutor = autoresJson.get(0).path("name").asText();

                Integer anioNacimiento = autoresJson.get(0).path("birth_year").isInt()
                        ? autoresJson.get(0).path("birth_year").asInt()
                        : null;

                Integer anioMuerte = autoresJson.get(0).path("death_year").isInt()
                        ? autoresJson.get(0).path("death_year").asInt()
                        : null;

                String idioma = libroJson.path("languages").get(0).asText();
                Integer numeroDescargas = libroJson.path("download_count").asInt();


                Autor autor = autorRepository.findByNombre(nombreAutor)
                        .orElseGet(() -> autorRepository.save(new Autor(nombreAutor, anioNacimiento, anioMuerte)));


                Libro libro = new Libro(tituloLibro, idioma, numeroDescargas, autor);
                libroRepository.save(libro);

                System.out.println("📚 Libro guardado: " + libro);
            } else {
                System.out.println("❌ No se encontraron resultados para el título: " + titulo);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error al buscar el libro: " + e.getMessage());
        }
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("📖 No hay libros registrados.");
        } else {
            libros.forEach(System.out::println);
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("✍️ No hay autores registrados.");
        } else {
            autores.forEach(System.out::println);
        }
    }

    private void listarAutoresVivos(Scanner scanner) {
        System.out.print("📅 Ingresa el año: ");
        try {
            int anio = Integer.parseInt(scanner.nextLine());
            List<Autor> autoresVivos = autorRepository
                    .findByAnioNacimientoLessThanEqualAndAnioMuerteGreaterThanEqual(anio, anio);

            if (autoresVivos.isEmpty()) {
                System.out.println("❌ No se encontraron autores vivos en el año " + anio);
            } else {
                autoresVivos.forEach(System.out::println);
            }

        } catch (NumberFormatException e) {
            System.out.println("⚠️ Ingresa un año válido.");
        }
    }

    private void listarLibrosPorIdioma(Scanner scanner) {
        System.out.print("🌐 Ingresa el código del idioma (ej: 'en', 'es'): ");
        String idioma = scanner.nextLine();

        List<Libro> libros = libroRepository.findByIdioma(idioma);

        if (libros.isEmpty()) {
            System.out.println("❌ No se encontraron libros en el idioma: " + idioma);
        } else {
            libros.forEach(System.out::println);
        }
    }
}
