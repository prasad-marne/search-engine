package com.example.searchengine.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueryDto {
	@NotNull
	String query;
}
