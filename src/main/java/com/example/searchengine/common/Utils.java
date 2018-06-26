package com.example.searchengine.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class Utils {

	public static List<Resource> listOfFiles() throws IOException {
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		return Arrays.asList(resourcePatternResolver.getResources("data/*.txt"));
	}

	public static Resource getfile(String fileName) {
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		return resourcePatternResolver.getResource("data/".concat(fileName));
	}

	public static String readResourceFile(Resource resource) throws IOException {
		return FileUtils.readFileToString(resource.getFile(), Charset.forName("UTF-8"));
	}

	public static String[] tokenize(String text) {
		return text.replaceAll("[^a-zA-Z ]", "")
				.toLowerCase().split("\\s+");
	}

}
