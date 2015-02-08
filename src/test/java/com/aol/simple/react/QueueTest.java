package com.aol.simple.react;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.async.Queue;

public class QueueTest {

	@Before
	public void setup(){
		found =0;
	}
	int found =0;
	public synchronized void incrementFound(){
		found++;
	}
	@Test(expected=Queue.ClosedQueueException.class)
	public void queueTestBlock(){
		
		try{
			Queue q = new Queue(new LinkedBlockingQueue());
			
			
			new SimpleReact().react(() -> q.add(1), ()-> q.add(2),()-> {sleep(200); return q.add(4); }, ()-> { sleep(400); q.setOpen(false); return 1;});
			
			
			
			new SimpleReact(false).fromStream(q.dequeueForSimpleReact())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.block();
			
			
				
		}finally{
			assertThat(found,is(3));
		}
		
		
	}
	@Test
	public void queueTestRun(){
		try{
			Queue q = new Queue(new LinkedBlockingQueue());
			
			
			new SimpleReact().react(() -> q.add(1), ()-> q.add(2),()-> {sleep(200); return q.add(4); }, ()-> { sleep(400); q.setOpen(false); return 1;});
			
			
			
			new SimpleReact(false).fromStream(q.dequeueForSimpleReact())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.run();
			
			
				
		}finally{
			assertThat(found,is(3));
		}
		
		
	}

	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
