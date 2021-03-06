package com.aol.simple.react.stream.traits;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactory;

public interface EagerToQueue<U> extends ToQueue<U> {

	abstract QueueFactory<U> getQueueFactory();

	abstract <T, R> SimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn);
	

	abstract <R> SimpleReactStream<R> thenSync(final Function<U, R> fn);
	
	

	
	/**
	 * Convert the current Stream to a SimpleReact Queue
	 * 
	 * @return Queue populated asynchrnously by this Stream
	 */
	default Queue<U> toQueue() {
		Queue<U> queue = this.getQueueFactory().build();

		 thenSync(it -> queue.offer(it)).allOf(it ->queue.close());

		return queue;
	}
	 default Queue<U> toQueue(Function<Queue,Queue> modifier){
		  Queue<U> queue = modifier.apply(this.getQueueFactory().build());
		  thenSync(it -> queue.offer(it)).allOf(it ->queue.close());

			return queue;
	}

	default <K> void toQueue(Map<K, Queue<U>> shards, Function<U, K> sharder) {

		thenSync(
				it -> shards.get(sharder.apply(it)).offer(it)).allOf(
				data -> {
					shards.values().forEach(it -> it.close());
					return true;
				});

	}
}
