package com.example.searchengine;

import com.example.searchengine.common.Utils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeBasedTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.IntStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class InvertedIndexBuilder implements CommandLineRunner {

	private TreeBasedTable<String, Integer, SortedSet<Integer>> invertedIndex;
	private HashMap<Integer, String> mapDocIdToName;
	private Integer totalNumberOfDocuments;

	@Override
	public void run(String... args) throws Exception {
		int docId = 0;
		invertedIndex = TreeBasedTable.create();
		mapDocIdToName = Maps.newHashMap();
		for (Resource resource : Utils.listOfFiles()) {
			log.info("reading file: {}", resource.getFilename());
			mapDocIdToName.put(docId, resource.getFilename());
			buildIndex(resource, docId);
			docId++;
		}
		totalNumberOfDocuments = docId;
		totalNumberOfDocuments++;
	}

	private synchronized void buildIndex(Resource resource, Integer documentId) throws Exception {
		String[] tokens = Utils.tokenize(Utils.readResourceFile(resource));
		IntStream.range(0, tokens.length).forEach(i -> {
			SortedMap<Integer, SortedSet<Integer>> posting = invertedIndex.row(tokens[i]);
			if (Objects.isNull(posting)) {
				posting = new TreeMap<>();
			}
			SortedSet<Integer> positions = posting.get(documentId);
			if (Objects.isNull(positions)) {
				positions = new TreeSet<>();
			}
			positions.add(i);
			posting.put(documentId, positions);
		});
	}

	public synchronized void deleteIndex(String document) {
		Integer key = new Integer(0);
		String value = "";
		Boolean found = false;
		for (Map.Entry doc : mapDocIdToName.entrySet()) {
			if (doc.getValue().equals(document)) {
				SortedSet<String> temp = Sets.newTreeSet(invertedIndex.rowKeySet());
				for (String row: temp) {
					invertedIndex.remove(row, doc.getKey());
				}
				key = (Integer) doc.getKey();
				value = (String) doc.getValue();
				found = true;
				totalNumberOfDocuments--;
				break;
			}
		}
		if (found) {
			mapDocIdToName.remove(key, value);
		}

	}

	public synchronized void updateIndex(String fileName) throws Exception {
		totalNumberOfDocuments++;
		buildIndex(Utils.getfile(fileName), totalNumberOfDocuments);
	}


}
