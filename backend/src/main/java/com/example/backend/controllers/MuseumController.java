package com.example.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backend.models.Artist;
import com.example.backend.models.Country;
import com.example.backend.models.Museum;
import com.example.backend.models.Painting;
import com.example.backend.repositories.ArtistRepository;
import com.example.backend.repositories.MuseumRepository;
import com.example.backend.repositories.PaintingRepository;
import com.example.backend.tools.DataValidationException;

import javax.validation.Valid;
import java.util.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1")
public class MuseumController {

    @Autowired
    MuseumRepository museumRepository;

    @GetMapping("/museums")
    public Page<Museum> getAllMuseums(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return museumRepository.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }

    @GetMapping("/museums/{id}")
    public ResponseEntity<Museum> getMuseum(@PathVariable(value = "id") Long museumId)
            throws DataValidationException {
        Museum museum = museumRepository.findById(museumId).orElseThrow(()->new DataValidationException("Музей с таким индексом не найден"));
        return ResponseEntity.ok(museum);
    }

    @GetMapping("/museums/{id}/paintings")
    public ResponseEntity<List<Painting>> getArtistPaintings(@PathVariable(value="id") Long museumId) {
        Optional<Museum> cc = museumRepository.findById(museumId);
        if (cc.isPresent()) {
            return ResponseEntity.ok(cc.get().paintings);
        }
        return ResponseEntity.ok(new ArrayList<Painting>());
    }

    @PostMapping("/museums")
    public ResponseEntity<Object> createMuseum(@RequestBody Museum museum) throws DataValidationException {
        try {
            Museum nc = museumRepository.save(museum);
            return new ResponseEntity<Object>(nc, HttpStatus.OK);
        }
        catch(Exception ex) {
            if (ex.getMessage().contains("museum.name_UNIQUE"))
                throw new DataValidationException("Этот музей уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @PutMapping("/museums/{id}")
    public ResponseEntity<Museum> updateMuseum(@PathVariable(value = "id") Long museumId, @Valid @RequestBody Museum museumDetails)  throws DataValidationException{
        try {
            Museum museum = museumRepository.findById(museumId).orElseThrow(() -> new DataValidationException("Музей с таким индексом не найден"));
            museum.name = museumDetails.name;
            museum.location = museumDetails.location;
            museumRepository.save(museum);
            return ResponseEntity.ok(museum);
        }
        catch (Exception ex) {
            if (ex.getMessage().contains("museum.name_UNIQUE"))
                throw new DataValidationException("Этот музей уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @PostMapping("/deletemuseums")
    public ResponseEntity<Object> deleteMuseums(@Valid @RequestBody List<Museum> museums) {
        museumRepository.deleteAll(museums);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}