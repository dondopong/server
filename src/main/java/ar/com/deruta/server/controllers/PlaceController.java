package ar.com.deruta.server.controllers;

import ar.com.deruta.server.models.Picture;
import ar.com.deruta.server.models.Place;
import ar.com.deruta.server.models.enums.Repository;
import ar.com.deruta.server.models.utils.Coordinates;
import ar.com.deruta.server.services.PlaceService;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/place")
public class PlaceController {

    @Autowired
    private PlaceService placeService;

    @GetMapping
    public List<Place> getAll() {
        return placeService.getAll();
    }

    @PostMapping
    public Place save(@RequestBody Place place) {
        return placeService.save(place);
    }

    //151020
    @GetMapping("/ioverlander/{to}")
    public void saveFromIoverlander (@PathVariable Long to) {
        ArrayList<Long> ids = new ArrayList<>();
        for (long i = 0L; i <= to; i++) {
            ids.add(i);
        }
        ArrayList<Long> errores = new ArrayList<>();
        AtomicInteger cant = new AtomicInteger();
        Long startTime = new Date().getTime();
        ids.parallelStream().forEach(id -> {
            try {
                long currentTime = new Date().getTime();
                int c;
                long millis = currentTime - startTime;
                System.out.println("Cantidad procesada: " + (c = cant.incrementAndGet()) +
                        ". Tiempo transcurrido: " + String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))) +
                        ". Tiempo estimado restante: " + String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(((to - c) * (currentTime - startTime) / c)),
                        TimeUnit.MILLISECONDS.toMinutes(((to - c) * (currentTime - startTime) / c)) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(((to - c) * (currentTime - startTime) / c))),
                        TimeUnit.MILLISECONDS.toSeconds(((to - c) * (currentTime - startTime) / c)) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(((to - c) * (currentTime - startTime) / c)))));
                Place place = new Place();

                Document doc = Jsoup.connect("https://www.ioverlander.com/places/" + id).get();

                place.setId(id);
                place.setRepository(Repository.IOVERLANDER);
                place.setName(doc.getElementsByTag("h1").get(0).text().split("\\|")[0].trim());
                place.setType(doc.getElementsByTag("h1").get(0).text().split("\\|")[1].trim());
                place.setCountry(doc.getElementById("place_nearby_div").text().trim());

                Elements details = doc.getElementsByClass("placeContent").get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
                place.setLastVisited(details.get(0).getElementsByTag("td").get(1).text());
                String[] coordinates = details.get(1).getElementsByTag("td").get(1).text().split(",");
                place.setCoordinates(new Coordinates(Double.parseDouble(coordinates[0].trim()), Double.parseDouble(coordinates[1].trim())));
                try {
                    place.setAltitude(Double.parseDouble(details.get(2).getElementsByTag("td").get(1).text().split(" ")[0]));
                } catch (NumberFormatException e) {
                    place.setAltitude(null);
                }
                place.setWebsite(details.size() > 3 ? details.get(3).getElementsByTag("td").get(1).text() : "");
                place.setPhone(details.size() > 4 ? details.get(4).getElementsByTag("td").get(1).text() : "");

                place.setDescription(doc.getElementsByClass("description").get(0).getElementsByTag("p").get(0).text());
                Elements pictures = doc.getElementsByClass("photosliderdiv");
                for (int i = 0; i < pictures.size(); i++) {
                    Picture picture = new Picture();
                    picture.setLink(pictures.get(i).getElementsByTag("img").get(0).attr("src"));
                    place.getPictures().add(picture);
                }
                placeService.save(place);
            } catch (HttpStatusException e) {

            } catch (Exception e) {
                System.out.println("Id " + id + " con error: " + e.getMessage());
                errores.add(id);
            }
        });
        System.out.println("Ids con error: " + errores.toString());
    }

    @GetMapping("/{id}")
    public void saveFromIoverlanderId (@PathVariable Long id) {
        try {
            System.out.println("Procesando id: " + id);
            Place place = new Place();

            Document doc = Jsoup.connect("https://www.ioverlander.com/places/" + id).get();

            place.setId(id);
            place.setRepository(Repository.IOVERLANDER);
            place.setName(doc.getElementsByTag("h1").get(0).text().split("\\|")[0].trim());
            place.setType(doc.getElementsByTag("h1").get(0).text().split("\\|")[1].trim());
            place.setCountry(doc.getElementById("place_nearby_div").text().trim());

            Elements details = doc.getElementsByClass("placeContent").get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
            place.setLastVisited(details.get(0).getElementsByTag("td").get(1).text());
            String[] coordinates = details.get(1).getElementsByTag("td").get(1).text().split(",");
            place.setCoordinates(new Coordinates(Double.parseDouble(coordinates[0].trim()), Double.parseDouble(coordinates[1].trim())));
            try {
                place.setAltitude(Double.parseDouble(details.get(2).getElementsByTag("td").get(1).text().split(" ")[0]));
            } catch (NumberFormatException e) {
                place.setAltitude(null);
            }
            place.setWebsite(details.size() > 3 ? details.get(3).getElementsByTag("td").get(1).text() : "");
            place.setPhone(details.size() > 4 ? details.get(4).getElementsByTag("td").get(1).text() : "");

            place.setDescription(doc.getElementsByClass("description").get(0).getElementsByTag("p").get(0).text());
            Elements pictures = doc.getElementsByClass("photosliderdiv");
            for (int i = 0; i < pictures.size(); i++) {
                Picture picture = new Picture();
                picture.setLink(pictures.get(i).getElementsByTag("img").get(0).attr("src"));
                place.getPictures().add(picture);
            }
            placeService.save(place);
            System.out.println("id " + id + " procesado correctamente");
        } catch (HttpStatusException e) {
            System.out.println("id " + id + " procesado con error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("id " + id + " procesado con error: " + e.getMessage());
        }
    }

}
