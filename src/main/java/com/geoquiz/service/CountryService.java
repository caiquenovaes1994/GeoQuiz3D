package com.geoquiz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geoquiz.model.Country;
import com.geoquiz.repository.CountryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryService {

    private final CountryRepository countryRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // Se tiver poucos países, tenta completar a importação
        if (countryRepository.count() < 200) {
            importCountries();
        }
    }

    public void importCountries() {
        log.info("Iniciando importação de países do arquivo countries.json...");
        try {
            File file = new File("countries.json");
            if (!file.exists()) {
                log.error("Arquivo countries.json não encontrado na raiz do projeto!");
                return;
            }

            JsonNode root = objectMapper.readTree(file);
            JsonNode features = root.get("features");

            if (features != null && features.isArray()) {
                for (JsonNode feature : features) {
                    try {
                        JsonNode props = feature.get("properties");
                        String name = props.has("name") ? props.get("name").asText() : "Desconhecido";
                        String iso2 = props.has("ISO3166-1-Alpha-2") ? props.get("ISO3166-1-Alpha-2").asText() : null;
                        String iso3 = props.has("ISO3166-1-Alpha-3") ? props.get("ISO3166-1-Alpha-3").asText() : null;

                        // Buscar se já existe
                        Optional<Country> existing = countryRepository.findByIsoAlpha2(iso2);
                        Country country;
                        
                        if (existing.isEmpty()) {
                            country = new Country();
                            country.setName(name);
                            country.setIsoAlpha2(iso2);
                            country.setIsoAlpha3(iso3);
                        } else {
                            country = existing.get();
                        }

                        // Se o país for novo ou estiver sem coordenadas, tenta calcular
                        if (country.getLatitude() == null || country.getLongitude() == null) {
                            JsonNode geometry = feature.get("geometry");
                            double[] centroid = calculateCentroid(geometry);
                            if (centroid != null) {
                                country.setLongitude(centroid[0]);
                                country.setLatitude(centroid[1]);
                                countryRepository.save(country);
                                log.debug("Coordenadas atualizadas para: {}", name);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Erro ao importar um país específico, pulando... Erro: {}", e.getMessage());
                    }
                }
            }
            log.info("Importação concluída. Total no banco: {}", countryRepository.count());
        } catch (IOException e) {
            log.error("Erro fatal ao ler o arquivo countries.json", e);
        }
    }

    private double[] calculateCentroid(JsonNode geometry) {
        if (geometry == null || !geometry.has("coordinates")) return null;
        JsonNode coordinates = geometry.get("coordinates");
        String type = geometry.get("type").asText();

        return processGeometry(type, coordinates);
    }

    private double[] processGeometry(String type, JsonNode coordinates) {
        double sumLon = 0;
        double sumLat = 0;
        long count = 0;

        if ("Point".equals(type)) {
            return new double[]{coordinates.get(0).asDouble(), coordinates.get(1).asDouble()};
        } else if ("Polygon".equals(type)) {
            for (JsonNode ring : coordinates) {
                for (JsonNode point : ring) {
                    sumLon += point.get(0).asDouble();
                    sumLat += point.get(1).asDouble();
                    count++;
                }
            }
        } else if ("MultiPolygon".equals(type)) {
            for (JsonNode polygon : coordinates) {
                for (JsonNode ring : polygon) {
                    for (JsonNode point : ring) {
                        sumLon += point.get(0).asDouble();
                        sumLat += point.get(1).asDouble();
                        count++;
                    }
                }
            }
        }

        if (count > 0) {
            return new double[]{sumLon / count, sumLat / count};
        }
        return null;
    }

    /**
     * Gera a URL da bandeira usando o serviço Flagpedia/FlagCDN.
     * @param isoAlpha2 Código ISO 3166-1 Alpha-2 do país.
     * @return URL da imagem da bandeira.
     */
    public String getFlagUrl(String isoAlpha2) {
        if (isoAlpha2 == null || isoAlpha2.isEmpty()) return null;
        // Flagpedia costuma usar códigos em minúsculo
        return "https://flagcdn.com/w640/" + isoAlpha2.toLowerCase() + ".png";
    }
}
