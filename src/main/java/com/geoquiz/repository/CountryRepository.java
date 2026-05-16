package com.geoquiz.repository;

import com.geoquiz.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByIsoAlpha2(String isoAlpha2);
    boolean existsByGeometryJsonIsNull();
}
