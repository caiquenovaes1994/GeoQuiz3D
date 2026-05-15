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

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryService {

    private final CountryRepository countryRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        if (countryRepository.count() == 0) {
            importCountries();
        }
    }

    public void importCountries() {
        log.info("Iniciando importação de países de countries.json...");
        try {
            File jsonFile = new File("countries.json");
            if (!jsonFile.exists()) {
                log.error("Arquivo countries.json não encontrado na raiz do projeto!");
                return;
            }

            JsonNode root = objectMapper.readTree(jsonFile);
            JsonNode features = root.get("features");

            if (features != null && features.isArray()) {
                for (JsonNode feature : features) {
                    JsonNode props = feature.get("properties");
                    if (props != null) {
                        String name = props.get("name").asText();
                        String iso2 = props.has("ISO3166-1-Alpha-2") ? props.get("ISO3166-1-Alpha-2").asText() : null;
                        String iso3 = props.has("ISO3166-1-Alpha-3") ? props.get("ISO3166-1-Alpha-3").asText() : null;

                        if (iso2 != null && !iso2.isEmpty()) {
                            Country country = new Country();
                            country.setName(name);
                            country.setIsoAlpha2(iso2);
                            country.setIsoAlpha3(iso3);

                            // Extração e cálculo básico de centroide
                            JsonNode geometry = feature.get("geometry");
                            double[] centroid = calculateCentroid(geometry);
                            if (centroid != null) {
                                country.setLongitude(centroid[0]);
                                country.setLatitude(centroid[1]);
                            }

                            countryRepository.save(country);
                        }
                    }
                }
            }
            log.info("Importação concluída. Total de países: {}", countryRepository.count());
        } catch (IOException e) {
            log.error("Erro ao ler o arquivo countries.json", e);
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

        if ("Polygon".equals(type)) {
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
