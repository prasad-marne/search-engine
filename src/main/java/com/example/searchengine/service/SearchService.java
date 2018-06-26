package com.example.searchengine.service;

import com.example.searchengine.InvertedIndexBuilder;
import com.example.searchengine.dto.ResultDto;
import com.example.searchengine.logic.BooleanExpressionEvaluate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
	private final InvertedIndexBuilder builder;
	private final BooleanExpressionEvaluate logic;

	public ResultDto searchBooleanQuery(String query) {
		Map<Integer, Double> multimap = logic.evaluateBooleanExpression(query);
		List<ResultDto.Result> results = new LinkedList<>();
		multimap.keySet().forEach(k -> {
			ResultDto.Result result = new ResultDto.Result();
			result.setScore(multimap.get(k).toString());
			result.setFileName(builder.getMapDocIdToName().get(k));
			results.add(result);
		});
		ResultDto dto = new ResultDto();
		results.sort(Collections.reverseOrder((Comparator.comparing(ResultDto.Result::getScore))));
		dto.setResults(results);
		return dto;
	}

	public void deleteIndex(@NotNull String document) {
		builder.deleteIndex(document);
	}

	public void updateIndex(@NotNull String document) throws Exception {
		builder.updateIndex(document);
	}
}
