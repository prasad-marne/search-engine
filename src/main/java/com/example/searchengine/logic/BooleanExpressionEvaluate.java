package com.example.searchengine.logic;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BooleanExpressionEvaluate {
	private final BooleanSearchLogic logic;

	public Map<Integer, Double> evaluateBooleanExpression(String query) {
		Deque<String> operator = new ArrayDeque<>();
		SortedSet<String> terms = new TreeSet<>();
		Deque<SortedSet<Integer>> operand = new ArrayDeque<>();
		int i = 0;
		while (i < query.length()) {
			char c = query.charAt(i);
			switch (c) {
				case '|':
				case '&':
					while (isNotSmallPrecedence(operator, String.valueOf(c))) {
						calculate(operator, operand);
					}
					operator.push(String.valueOf(c));
					i++;
					break;
				case '!':
					int m = i + 1;
					while (m < query.length() && query.charAt(m) != ' ') {
						m++;
					}
					String notTerm = query.substring(i+1, m);
					log.info("not term: {}", notTerm);
					i = m;
					terms.add(notTerm);
					operand.push(logic.getNotTermPostings(notTerm.toLowerCase()));
					break;
				case '(':
					operator.push("(");
					i++;
					break;
				case ')':
					while (!operator.peek().equals("(")) {
						calculate(operator, operand);
					}
					operator.pop();
					i++;
					break;
				case '\"':
					int j = i + 1;
					while (j < query.length() && query.charAt(j) != '\"') {
						j++;
					}
					String phrase = query.substring(i+1, j);
					log.info("phrase: {}", phrase);
					i = j+1;
					String[] arr = phrase.toLowerCase().split(" ");
					terms.addAll(Arrays.asList(arr));
					operand.push(logic.getPhrasePostings(arr));
					break;
				case ' ':
					i++;
					break;
				default:
					if ((Character.isLetter(c))) {
						int k = i + 1;
						while (k < query.length() && Character.isLetter(query.charAt(k))) {
							k++;
						}
						String term = query.substring(i, k);
						log.info("term: {}", term);
						terms.add(term);
						operand.push(logic.getTermPostings(term.toLowerCase()));
						i = k;
					}
			}
		}

		while (!operator.isEmpty()) {
			calculate(operator, operand);
		}

		return logic.rankDocuments(operand.peek(), terms);
	}

	private void calculate(Deque<String> operator, Deque<SortedSet<Integer>> operand) {
		String op = operator.pop();
		SortedSet<Integer> operand1 = operand.pop();
		SortedSet<Integer> operand2 = operand.pop();
		SortedSet<Integer> result = logic.mergeAlgorithm(operand1, operand2, op);
		operand.push(result);
	}

	private boolean isNotSmallPrecedence(Deque<String> stack, String operator) {
		return !(stack.isEmpty() || (precedenceLevel(stack.peek()) < precedenceLevel(operator)));
	}

	private int precedenceLevel(String op) {
		switch (op) {
			case "(":
				return -1;
			case "|":
				return 0;
			case "&":
				return 1;
			case "!":
				return 2;
			case ")":
				return 3;
			default:
				throw new IllegalArgumentException("Operator unknown: " + op);
		}
	}
}
