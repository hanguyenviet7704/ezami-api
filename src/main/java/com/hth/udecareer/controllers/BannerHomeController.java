package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ApiPrefixController
public class BannerHomeController {

    @GetMapping("home/banner")
    public ResponseEntity<List<String>> getAllBanner() {
        List<String> banners = List.of(
                "https://img.freepik.com/free-photo/learning-education-ideas-insight-intelligence-study-concept_53876-120116.jpg?semt=ais_hybrid&w=740&q=80",
                "https://static.wixstatic.com/media/65246d_c7bd3ba476fb4191af59a11494ad027f~mv2.jpg/v1/fill/w_820,h_460,al_c,q_85/65246d_c7bd3ba476fb4191af59a11494ad027f~mv2.jpg",
                "https://media.istockphoto.com/id/1130023029/photo/light-bulb-ideas-creative-diagram-concept.jpg?s=612x612&w=0&k=20&c=e538x8i0JeMU9iJUVGKLjb3GXWj9jT7ntyyxyqLm8o0="
        );

        return ResponseEntity.ok(banners);
    }

}
