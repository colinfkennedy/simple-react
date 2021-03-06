package com.aol.simple.react.simple;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.simple.react.stream.simple.SimpleReact;

public class ResultCollectionTest {
	/**
	@Test
	public void testBlockThen() throws InterruptedException, ExecutionException {

		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.collectResults()
				.block()
				.proceed()
				.then(it -> it +"*").block();

		assertThat(strings.size(), is(3));
		assertThat(strings.get(0), endsWith("*"));
		assertThat(strings.get(0), startsWith("*"));

	}
	
	@Test
	public void testBlockThenResultResetAndCorrect() throws InterruptedException, ExecutionException {

		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.collectResults()
				.block()
				.proceed()
				.then(it -> it +"*")
				.block();

		assertThat(strings.size(), is(3));
		assertThat(strings.get(0), endsWith("*"));
		assertThat(strings.get(0), startsWith("*"));

	}
	**/
	
	
	@Test
	public void testBlockStreamsSameForkJoinPool() throws InterruptedException,
			ExecutionException {
		Set<String> threadGroup = Collections.synchronizedSet(new TreeSet());

		Integer result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> { threadGroup.add(Thread.currentThread().getThreadGroup().getName()); return it * 200;})
				.collectResults().block().<Integer>submit( 
						it -> it.parallelStream()
								.filter(f -> f > 300)
								.map(m ->{ threadGroup.add(Thread.currentThread().getThreadGroup().getName());return m - 5; })
								.reduce(0, (acc, next) -> acc + next));

		

		assertThat(result, is(990));
		assertThat(threadGroup.size(), is(1));
	}
	@Test
	public void testBlockStreamsSameForkJoinPoolImplicit() throws InterruptedException,
	
			ExecutionException {
		Set<String> threadGroup = Collections.synchronizedSet(new TreeSet());

		Integer result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> { threadGroup.add(Thread.currentThread().getThreadGroup().getName()); return it * 200;})
				.submitAndBlock( 
						it -> it.parallelStream()
								.filter(f -> f > 300)
								.map(m ->{ threadGroup.add(Thread.currentThread().getThreadGroup().getName());return m - 5; })
								.reduce(0, (acc, next) -> acc + next));

		

		assertThat(result, is(990));
		assertThat(threadGroup.size(), is(1));
	}

	
	@Test
	public void testBlock() throws InterruptedException, ExecutionException {

		
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.collectResults()
				.block()
				.getResults();

		assertThat(strings.size(), is(3));

	}
	@Test
	public void testBlockToSet() throws InterruptedException, ExecutionException {

		Set<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 1, () -> 3)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.collectResults()
				.<Set<String>>block(Collectors.toSet())
				.getResults();

		assertThat(strings.size(), is(2));

	}
	@Test
	public void testBreakout() throws InterruptedException, ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it)
				.collectResults()
				.block(status -> status.getCompleted() > 1)
				.getResults();

		assertThat(strings.size(), greaterThan(1));

	}
	@Test
	public void testBreakoutToSet() throws InterruptedException, ExecutionException {
		Throwable[] error = { null };
		Set<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it)
				.collectResults()
				.<Set<String>>block(Collectors.toSet(),status -> status.getCompleted() > 1)
				.getResults();

		assertThat(strings.size(), is(2));

	}

	@Test
	public void testBreakoutException() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.<String>then(it -> {

					throw new RuntimeException("boo!");

				}).capture(e -> error[0] = e)
				.collectResults()
				.block(status -> status.getCompleted() >= 1)
				.getResults();

		assertThat(strings.size(), is(0));
		assertThat(error[0], instanceOf(RuntimeException.class));
	}
	volatile int count =0;
	@Test
	public void testBreakoutExceptionTimes() throws InterruptedException,
			ExecutionException {
		count =0;
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.<String>then(it -> {

					throw new RuntimeException("boo!");

				}).capture(e -> count++)
				.collectResults()
				.block(status -> status.getCompleted() >= 1)
				.getResults();

		assertThat(strings.size(), is(0));
		assertThat(count, is(3));
	}
	@Test
	public void testBreakoutAllCompleted() throws InterruptedException,
			ExecutionException {
		count =0;
		List<Integer> results = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if(it==100)
						throw new RuntimeException("boo!");
					else
						sleep(it);
					return it;

				}).capture(e -> count++)
				.block(status -> status.getAllCompleted() >0);

		assertThat(results.size(), is(0));
		assertThat(count, is(1));
	}
	@Test
	public void testBreakoutAllCompletedAndTime() throws InterruptedException,
			ExecutionException {
		count =0;
		List<Integer> results = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					sleep(it);
					return it;

				}).capture(e -> count++)
				.block(status -> status.getAllCompleted() >1 && status.getElapsedMillis()>200);

		assertThat(results.size(), greaterThan(1));
		assertThat(count, is(0));
	}
	

	@Test
	public void testBreakoutInEffective() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1)
				.then(it -> "*" + it)
				.block(status -> status.getCompleted() > 5);

		assertThat(strings.size(), is(3));

	}
	
	private Object sleep(Integer it) {
		try {
			Thread.sleep(it);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		return it;
	}
}
