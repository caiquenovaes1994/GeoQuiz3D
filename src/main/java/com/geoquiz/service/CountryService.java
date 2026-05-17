package com.geoquiz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geoquiz.model.ConqueredCountry;
import com.geoquiz.model.Country;
import com.geoquiz.repository.ConqueredCountryRepository;
import com.geoquiz.repository.CountryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryService {

    private final CountryRepository countryRepository;
    private final ConqueredCountryRepository conqueredCountryRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // Se tiver poucos países ou se faltarem geometrias, tenta completar a importação
        if (countryRepository.count() < 200 || countryRepository.existsByGeometryJsonIsNull()) {
            importCountries();
        }
        fixTaiwanEntries();
        fixUnknownContinents();
        healSpecificCountries();
    }

    private void fixTaiwanEntries() {
        countryRepository.findAll().stream()
            .filter(c -> c.getName().toLowerCase().contains("taiwan"))
            .forEach(c -> {
                if (c.getIsoAlpha2() == null || !c.getIsoAlpha2().equals("TW")) {
                    c.setIsoAlpha2("TW");
                    countryRepository.save(c);
                    log.info("Corrigido código ISO para Taiwan.");
                }
            });
    }

    private void fixUnknownContinents() {
        long count = countryRepository.findAll().stream()
            .filter(c -> c.getContinent() == null || c.getContinent().equals("Desconhecido"))
            .peek(c -> {
                String newContinent = getContinentByIso(c.getIsoAlpha2());
                c.setContinent(newContinent);
                countryRepository.save(c);
            })
            .count();
        if (count > 0) {
            log.info("Corrigidos {} continentes que estavam como Desconhecido no banco.", count);
        }
    }

    private String getContinentByIso(String iso2) {
        if (iso2 == null) return "Desconhecido";
        iso2 = iso2.toUpperCase();
        
        // Europa (Europe)
        if (List.of("AD", "AL", "AM", "AT", "AZ", "BA", "BE", "BG", "BY", "CH", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GE", "GR", "HR", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MD", "ME", "MK", "MT", "NL", "NO", "PL", "PT", "RO", "RS", "RU", "SE", "SI", "SK", "SM", "UA", "GB", "VA", "XK", "UK", "GB-ENG", "GB-WLS", "GB-SCT", "GB-NIR").contains(iso2)) {
            return "Europe";
        }
        
        // América do Sul (South America)
        if (List.of("AR", "BO", "BR", "CL", "CO", "EC", "FK", "GF", "GY", "PE", "PY", "SR", "UY", "VE").contains(iso2)) {
            return "South America";
        }
        
        // América do Norte / Central (North America)
        if (List.of("AG", "BS", "BB", "BZ", "CA", "CR", "CU", "DM", "DO", "SV", "GD", "GT", "HT", "HN", "JM", "MX", "NI", "PA", "KN", "LC", "VC", "TT", "US", "PR", "GL", "GP", "MQ", "MS", "TC", "VI", "VG", "KY", "BM", "PM", "SX", "MF", "BL", "AW", "CW", "BQ").contains(iso2)) {
            return "North America";
        }
        
        // Ásia (Asia)
        if (List.of("AF", "BH", "BD", "BT", "BN", "KH", "CN", "IN", "ID", "IR", "IQ", "IL", "JP", "JO", "KZ", "KP", "KR", "KW", "KG", "LA", "LB", "MY", "MV", "MN", "MM", "NP", "OM", "PK", "PS", "PH", "QA", "SA", "SG", "LK", "SY", "TW", "TJ", "TH", "TL", "TR", "TM", "AE", "UZ", "VN", "YE").contains(iso2)) {
            return "Asia";
        }
        
        // Oceania
        if (List.of("AU", "FJ", "KI", "MH", "FM", "NR", "NZ", "PW", "PG", "WS", "SB", "TO", "TV", "VU", "NC", "PF", "AS", "GU", "MP", "CK", "NU", "WF", "TK").contains(iso2)) {
            return "Oceania";
        }
        
        // África (Africa)
        if (List.of("AO", "BJ", "BW", "BF", "BI", "CV", "CM", "CF", "TD", "KM", "CD", "CG", "CI", "DJ", "EG", "GQ", "ER", "SZ", "ET", "GA", "GM", "GH", "GN", "GW", "KE", "LS", "LR", "LY", "MG", "MW", "ML", "MR", "MU", "MA", "MZ", "NA", "NE", "NG", "RW", "ST", "SN", "SC", "SL", "SO", "ZA", "SS", "SD", "TZ", "TG", "TN", "UG", "ZM", "ZW", "YT", "RE", "SH", "EH").contains(iso2)) {
            return "Africa";
        }
        
        // Antártida (Antarctica)
        if (List.of("AQ", "TF", "BV", "HM", "GS").contains(iso2)) {
            return "Antarctica";
        }
        
        return "Desconhecido";
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
                        String continent = props.has("CONTINENT") ? props.get("CONTINENT").asText() : "Desconhecido";

                        // Normalização para Taiwan
                        if (name.toLowerCase().contains("taiwan") && (iso2 == null || iso2.isEmpty())) {
                            iso2 = "TW";
                            continent = "Asia";
                        }

                        if ("Desconhecido".equals(continent) && iso2 != null) {
                            continent = getContinentByIso(iso2);
                        }

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
                        
                        country.setContinent(continent);

                        // Se o país for novo ou estiver sem coordenadas/geometria, tenta calcular e salvar
                        if (country.getLatitude() == null || country.getLongitude() == null || country.getGeometryJson() == null) {
                            JsonNode geometry = feature.get("geometry");
                            
                            // Salva a geometria como string para o Worldle
                            if (geometry != null) {
                                country.setGeometryJson(geometry.toString());
                            }

                            double[] centroid = calculateCentroid(geometry);
                            if (centroid != null) {
                                country.setLongitude(centroid[0]);
                                country.setLatitude(centroid[1]);
                                log.debug("Coordenadas e Geometria atualizadas para: {}", name);
                            }
                        }
                        countryRepository.save(country);
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
        
        String code = isoAlpha2.toLowerCase();
        
        // Correção para Taiwan (comumente vindo como TW ou códigos alternativos em alguns datasets)
        if (code.equals("tw") || code.contains("taiwan")) {
            return "https://flagcdn.com/w640/tw.png";
        }
        
        return "https://flagcdn.com/w640/" + code + ".png";
    }

    private void healSpecificCountries() {
        log.info("Iniciando rotina de cura e calibragem de países específicos...");

        // 1. Remover países indesejados (e suas respectivas conquistas)
        removeCountryAndConquests("Dhekelia Sovereign Base Area");
        removeCountryAndConquests("United States Minor Outlying Islands");
        removeCountryAndConquestsByIso2("UM");

        // 2. Atualizar coordenadas errôneas para posições geográficas reais
        updateCountryCoordinates("EC", -1.8312, -78.1834);   // Ecuador
        updateCountryCoordinates("FJ", -17.7134, 178.0650);  // Fiji
        updateCountryCoordinates("KI", 1.8369, -157.3768);   // Kiribati
        updateCountryCoordinates("NZ", -40.9006, 174.8860);  // New Zealand
        updateCountryCoordinates("PT", 39.3999, -8.2245);    // Portugal
        updateCountryCoordinates("US", 37.0902, -95.7129);   // United States

        // 3. Garantir a existência e geometrias de France, Norway e French Guyana
        ensureCountryExists("France", "FR", "FRA", "Europe", 46.2276, 2.2137);
        ensureCountryExists("Norway", "NO", "NOR", "Europe", 60.4720, 8.4689);
        ensureCountryExists("French Guyana", "GF", "GUF", "South America", 4.0, -53.0);
        
        log.info("Rotina de cura e calibragem concluída com sucesso!");
    }

    private void removeCountryAndConquests(String name) {
        countryRepository.findAll().stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst()
            .ifPresent(this::deleteCountryAndConquestsHelper);
    }

    private void removeCountryAndConquestsByIso2(String iso2) {
        countryRepository.findByIsoAlpha2(iso2)
            .ifPresent(this::deleteCountryAndConquestsHelper);
    }

    private void deleteCountryAndConquestsHelper(Country country) {
        try {
            List<ConqueredCountry> conquered = conqueredCountryRepository.findAll().stream()
                .filter(cc -> cc.getCountry().getId().equals(country.getId()))
                .toList();
            if (!conquered.isEmpty()) {
                conqueredCountryRepository.deleteAll(conquered);
                log.info("Removidas {} conquistas históricas do país '{}' no auto-healing.", conquered.size(), country.getName());
            }
            countryRepository.delete(country);
            log.info("País removido com sucesso no auto-healing: '{}'", country.getName());
        } catch (Exception e) {
            log.error("Erro ao remover país '" + country.getName() + "' no auto-healing", e);
        }
    }

    private void updateCountryCoordinates(String iso2, double lat, double lon) {
        countryRepository.findByIsoAlpha2(iso2).ifPresent(c -> {
            c.setLatitude(lat);
            c.setLongitude(lon);
            countryRepository.save(c);
            log.info("Calibrado: {}: Lat={}, Lon={}", c.getName(), lat, lon);
        });
    }

    private void ensureCountryExists(String name, String iso2, String iso3, String continent, double lat, double lon) {
        Optional<Country> existing = countryRepository.findByIsoAlpha2(iso2);
        if (existing.isPresent()) {
            Country c = existing.get();
            c.setLatitude(lat);
            c.setLongitude(lon);
            c.setContinent(continent);
            countryRepository.save(c);
            log.info("País '{}' recalibrado com sucesso.", name);
            return;
        }

        log.info("País '{}' ausente. Criando e tentando extrair geometria de countries.json...", name);
        Country newCountry = new Country();
        newCountry.setName(name);
        newCountry.setIsoAlpha2(iso2);
        newCountry.setIsoAlpha3(iso3);
        newCountry.setContinent(continent);
        newCountry.setLatitude(lat);
        newCountry.setLongitude(lon);

        boolean foundInJson = false;
        try {
            File file = new File("countries.json");
            if (file.exists()) {
                JsonNode root = objectMapper.readTree(file);
                JsonNode features = root.get("features");
                if (features != null && features.isArray()) {
                    for (JsonNode feature : features) {
                        JsonNode props = feature.get("properties");
                        String pName = props.has("name") ? props.get("name").asText() : "";
                        String pIso2 = props.has("ISO3166-1-Alpha-2") ? props.get("ISO3166-1-Alpha-2").asText() : "";
                        String pIso3 = props.has("ISO3166-1-Alpha-3") ? props.get("ISO3166-1-Alpha-3").asText() : "";

                        if (pIso2.equalsIgnoreCase(iso2) || pIso3.equalsIgnoreCase(iso3) || 
                            pName.equalsIgnoreCase(name) || pName.toLowerCase().contains(name.toLowerCase())) {
                            
                            JsonNode geometry = feature.get("geometry");
                            if (geometry != null) {
                                newCountry.setGeometryJson(geometry.toString());
                                if (!pName.isEmpty()) {
                                    newCountry.setName(pName);
                                }
                                foundInJson = true;
                                log.info("Geometria extraída do JSON com sucesso para '{}'", newCountry.getName());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao ler geometria de '{}' do countries.json: {}", name, e.getMessage());
        }

        if (!foundInJson) {
            double offset = 0.5;
            String fallbackGeometry = String.format(
                "{\"type\":\"Polygon\",\"coordinates\":[[[%f,%f],[%f,%f],[%f,%f],[%f,%f],[%f,%f]]]}",
                lon - offset, lat - offset,
                lon + offset, lat - offset,
                lon + offset, lat + offset,
                lon - offset, lat + offset,
                lon - offset, lat - offset
            );
            newCountry.setGeometryJson(fallbackGeometry);
            log.info("Geometria não encontrada no GeoJSON para '{}'. Fallback simplificado gerado.", name);
        }

        countryRepository.save(newCountry);
        log.info("País '{}' inserido e curado com sucesso!", newCountry.getName());
    }
}
