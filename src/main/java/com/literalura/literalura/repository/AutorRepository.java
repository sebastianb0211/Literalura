package com.literalura.literalura.repository;

import com.literalura.literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNombre(String nombre);

    List<Autor> findByAnioNacimientoLessThanEqualAndAnioMuerteGreaterThanEqual(int nacimiento, int muerte);
}
