package com.geoquiz.repository;

import com.geoquiz.model.ConqueredCountry;
import com.geoquiz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ConqueredCountryRepository extends JpaRepository<ConqueredCountry, Long> {
    @Query("SELECT c FROM ConqueredCountry c WHERE c.user = :user AND (:gameMode IS NULL OR c.gameMode = :gameMode)")
    List<ConqueredCountry> findByUserAndGameMode(User user, String gameMode);
    
    Optional<ConqueredCountry> findByUserAndCountry(User user, com.geoquiz.model.Country country);
    
    @Query("SELECT COUNT(DISTINCT c.country.id) FROM ConqueredCountry c WHERE c.user = :user AND (:gameMode IS NULL OR c.gameMode = :gameMode)")
    long countUniqueCountriesByUserAndGameMode(User user, String gameMode);
    
    @Query("SELECT c.country.continent, COUNT(c) FROM ConqueredCountry c WHERE c.user = :user AND (:gameMode IS NULL OR c.gameMode = :gameMode) GROUP BY c.country.continent")
    List<Object[]> countByContinentForUserAndGameMode(User user, String gameMode);

    @Query("SELECT c.country.isoAlpha2 FROM ConqueredCountry c WHERE c.user = :user AND (:gameMode IS NULL OR c.gameMode = :gameMode)")
    List<String> findConqueredIsoCodesByUserAndGameMode(User user, String gameMode);
}
