package com.example.searchengine.logic;

import com.example.searchengine.InvertedIndexBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Data
@RequiredArgsConstructor
public class BooleanSearchLogic {

	private final InvertedIndexBuilder builder;

	public SortedSet<Integer> mergeAlgorithm(SortedSet<Integer> postings1, SortedSet<Integer> postings2, String op) {
		switch (op) {
			case "&":
				return Sets.newTreeSet(setIntersection(postings1, postings2));
			case "|":
				return Sets.newTreeSet(setUnion(postings1, postings2));
			default:
				throw new IllegalArgumentException("Operator unknown: " + op);
		}
	}

	private Set<Integer> setIntersection(SortedSet<Integer> set1, SortedSet<Integer> set2) {
		return Sets.intersection(set1, set2);
	}

	private Set<Integer> setUnion(SortedSet<Integer> set1, SortedSet<Integer> set2) {
		return Sets.union(set1, set2);
	}

	private Set<Integer> setSymmetricDifference(SortedSet<Integer> set1, SortedSet<Integer> set2) {
		return Sets.difference(set1, set2);
	}

	public SortedSet<Integer> getPhrasePostings(String[] terms) {
		SortedMap<Integer, SortedSet<Integer>> result = getPostingAndPositionMap(terms[0]);
		for (int i = 1; i < terms.length; i++) {
			positionalIntersection(result, getPostingAndPositionMap(terms[i]), i);
		}
		return Sets.newTreeSet(result.keySet());
	}

	private SortedMap<Integer, SortedSet<Integer>> positionalIntersection(SortedMap<Integer, SortedSet<Integer>> p1,
			SortedMap<Integer, SortedSet<Integer>> p2, int k) {
		SortedMap<Integer, SortedSet<Integer>> result = new TreeMap<>();
		Set<Integer> intersection = setIntersection(Sets.newTreeSet(p1.keySet()), Sets.newTreeSet(p2.keySet()));
		for (Integer doc : intersection) {
			Integer[] positions1 = p1.get(doc).toArray(new Integer[0]);
			Integer[] positions2 = p2.get(doc).toArray(new Integer[0]);
			SortedSet<Integer> l = new TreeSet<>();
			int i = 0;
			while (i < positions1.length) {
				int j = 0;
				while (j < positions2.length) {
					if (Math.abs(positions1[i] - positions2[j]) <= k) {
						l.add(positions2[j]);
					} else if (positions2[j] > positions1[i]) {
						break;
					}
					j++;
				}
				while (!l.isEmpty() && (Math.abs(l.first() - positions1[i]) > k)) {
					l.remove(l.first());
				}
				for (Integer ps : l) {
					if (result.containsKey(doc)) {
						result.get(doc).add(ps);
					} else {
						SortedSet<Integer> temp = new TreeSet<>();
						temp.add(ps);
						result.put(doc, temp);
					}
				}

				i++;
			}
		}
		return result;
	}

	public SortedMap<Integer, SortedSet<Integer>> getPostingAndPositionMap(String term) {
		return builder.getInvertedIndex().row(term);
	}

	public SortedSet<Integer> getTermPostings(String term) {
		return Sets.newTreeSet(builder.getInvertedIndex().row(term).keySet());
	}

	public SortedSet<Integer> getNotTermPostings(String term) {
		return Sets.newTreeSet(Sets.difference(builder.getMapDocIdToName().keySet(),
				builder.getInvertedIndex().row(term).keySet()));
	}

	public Map<Integer, Double> rankDocuments(SortedSet<Integer> result, SortedSet<String> terms) {
		HashMap<Integer, Double> multimap = Maps.newHashMap();
		for (Integer doc : result) {
			multimap.put(doc, calculateScore(doc, terms));
		}
		return multimap;
	}


	private Double calculateScore(Integer document, SortedSet<String> terms) {
		Double result = 0.0;
		for (String term : terms) {
			int df = 0;
			int tf = 0;
			SortedMap<Integer, SortedSet<Integer>> row = builder.getInvertedIndex().row(term);
			df = row.size();
			SortedSet<Integer> positions = row.get(document);
			if (Objects.nonNull(positions)) {
				tf = positions.size();
			}
			result += tfIdf(tf, df);
		}
		return result;

	}

	private Double tfIdf(Integer termFrequency, Integer documentFrequency) {
		Integer n = builder.getTotalNumberOfDocuments();
		return termFrequency(termFrequency) * (Math.log10((Double.valueOf(n)) / documentFrequency));
	}

	private Double termFrequency(Integer termFrequency) {
		if (termFrequency == 0) {
			return 0.0;
		}
		return (1 + Math.log10(termFrequency));
	}


}
