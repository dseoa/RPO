package com.example.backend.models;

import javax.persistence.*;

@Entity
@Table(name = "countries")
@Access(AccessType.FIELD)
public class Country {

    public Country() {
    }

    public Country(Long id) {
        this.id = id;
    }

    @Column(name = "name", nullable = false, unique = true)
    public String name;


    @PostMapping("/countries")
    public ResponseEntity<Object> createCountry(@RequestBody Country country)
            throws Exception {
        try {
            Country nc = countryRepository.save(country);
            return new ResponseEntity<Object>(nc, HttpStatus.OK);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("countries.name_UNIQUE"))
                error = "countyalreadyexists";
            else
                error = "undefinederror";
            Map<String, String>
                    map = new HashMap<>();
            map.put("error", error);
            return ResponseEntity.ok(map);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public long id;

    @Column(name = "name")
    public String name;
}
