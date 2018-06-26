package com.example.searchengine.dto;

import java.util.List;
import lombok.Data;

@Data
public class ResultDto {
	List<Result> results;

	@Data
	public static class Result {
		String fileName;
		String score;
	}
}
