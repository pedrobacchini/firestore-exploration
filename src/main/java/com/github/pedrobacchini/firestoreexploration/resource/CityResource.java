package com.github.pedrobacchini.firestoreexploration.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pedrobacchini.firestoreexploration.domain.CityWeather;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/city")
public class CityResource {

    private final Firestore firestore;
    private final CollectionReference collectionReference;

    @Autowired
    public CityResource(Firestore firestore) {
        this.firestore = firestore;
        collectionReference = firestore.collection("cities-weather");
        DocumentReference docRef = collectionReference.document("goiania");
        ListenerRegistration registration = docRef.addSnapshotListener((snapshot, error) -> {
            if(error!=null) {
                System.out.println("Listen failed: " + error);
                return;
            }

            if(snapshot != null && snapshot.exists())
                System.out.println("Current data: " + snapshot.getData());
            else
                System.out.println("Current data: null");
        });
//        registration.remove();
    }

    @GetMapping("/{cityName}")
    public ResponseEntity getCity(@PathVariable String cityName) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> future = collectionReference.document(cityName).get();
        DocumentSnapshot document = future.get();
        if(document.exists())
            return ResponseEntity.ok(Objects.requireNonNull(document.toObject(CityWeather.class)));
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such document!");
    }

    @PostMapping("/{cityName}")
    public ResponseEntity saveCity(@PathVariable String cityName,
                                   @RequestBody CityWeather cityWeather) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = collectionReference.document(cityName);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        if(!future.get().exists())
            return ResponseEntity.ok(documentReference.set(cityWeather).get());
        else
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Document already exist!");
    }

    @DeleteMapping("/{cityName}")
    public ResponseEntity deleteCity(@PathVariable String cityName) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = collectionReference.document(cityName);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        if(future.get().exists())
            return ResponseEntity.ok(documentReference.delete().get());
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such document!");
    }

    @PutMapping("/{cityName}")
    public ResponseEntity updateCity(@PathVariable String cityName,
                                     @RequestBody CityWeather cityWeather) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = collectionReference.document(cityName);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        if(future.get().exists()){
            Map map = new ObjectMapper().convertValue(cityWeather, Map.class);
            return ResponseEntity.ok(documentReference.update(map).get());
        }
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such document!");
    }

    @GetMapping
    public ResponseEntity getAllCity() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = collectionReference.get();
        QuerySnapshot queryDocumentSnapshots = query.get();
        return ResponseEntity.ok(queryDocumentSnapshots.toObjects(CityWeather.class));
    }

    @GetMapping("/area/{arenaName}")
    public ResponseEntity getAllCityInArea(@PathVariable String arenaName) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection("area").document(arenaName);
        ApiFuture<DocumentSnapshot> snapshotApiFuture = documentReference.get();
        DocumentSnapshot documentSnapshot = snapshotApiFuture.get();
        Map<String, Boolean> cities = (Map<String, Boolean>) documentSnapshot.getData().get("cities");
        List<ApiFuture<DocumentSnapshot>> futures = new ArrayList<>();
        cities.forEach((city, aBoolean) -> futures.add(collectionReference.document(city).get()));
        List<DocumentSnapshot> documentSnapshotList = ApiFutures.allAsList(futures).get();
        List<CityWeather> results = new ArrayList<>();
        documentSnapshotList.forEach(snapshot -> results.add(snapshot.toObject(CityWeather.class)));
        return ResponseEntity.ok(results);
    }
}
