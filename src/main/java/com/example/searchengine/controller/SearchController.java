package com.example.searchengine.controller;

import com.example.searchengine.dto.QueryDto;
import com.example.searchengine.dto.ResultDto;
import com.example.searchengine.service.SearchService;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService service;

	@PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResultDto search(@RequestBody @NotNull QueryDto dto) {
		return service.searchBooleanQuery(dto.getQuery());
	}

	@DeleteMapping(value = "/index")
	public void deleteIndex(@RequestParam @NotNull String documentName) {
		service.deleteIndex(documentName.toLowerCase());
	}

	@PutMapping(value = "/index")
	public void updateIndex(@RequestParam @NotNull String documentName) throws Exception {
		service.updateIndex(documentName.toLowerCase());
	}

}
