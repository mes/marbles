package de.fuberlin.wiwiss.marbles.loading;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.fuberlin.wiwiss.ng4j.semwebclient.DereferencingTask;

/**
 * The DereferencingTaskQueue is a thread which observes the
 * DereferencerThreads. It starts all DereferencerThreads tries to 
 * assign new tasks to free DereferencerThreads and interrupts them
 * if the timeout is reached.
 * 
 * Adapted to the Apache HTTP Client by Christian Becker
 */
public class DereferencingTaskQueue extends Thread {
    private static int MAX_PENDING_TASKS = 5;
	private int maxthreads;
	private List<DereferencerThread> threads = new ArrayList<DereferencerThread>();
	private boolean stopped = false;
	private LinkedList<DereferencingTask> tasks = new LinkedList<DereferencingTask>();
	private Log log = LogFactory.getLog(DereferencingTaskQueue.class);
	private int maxfilesize;
    private HttpClient httpClient;
    private SpongerProvider spongerProvider;

    public DereferencingTaskQueue(HttpClient httpClient, SpongerProvider spongerProvider, int maxThreads, int maxfilesize) {
		this.maxthreads = maxThreads;
		this.maxfilesize = maxfilesize;
		this.httpClient = httpClient;
		this.spongerProvider = spongerProvider;
		setName("Queue");
		start();
	}
	
	public synchronized boolean addTask(DereferencingTask task) {
		if (tasks.size() > MAX_PENDING_TASKS)
			return false;
		
		/* prefer step 0 tasks as these halt processing */
		if (task.getStep() == 0)
			this.tasks.addFirst(task);
		else
			this.tasks.addLast(task);
		this.log.debug("Enqueue: <" + task.getURI() + ">@" + task.getStep() + 
				" (n = " + this.tasks.size() + ")");
		this.notify();
		return true;
	}

	public void run() {
		initThreadPool(this.maxthreads);
		while (!this.stopped) {
			checkForTasksAndWait();
		}
	}

	public synchronized void close() {
		Iterator<DereferencerThread> it = this.threads.iterator();
		while (it.hasNext()) {
			DereferencerThread thread = (DereferencerThread) it.next();
			thread.stopThread();
			thread.interrupt();
			thread = null;
		}
		this.stopped = true;
		notify();
	}
	
	private void checkForTasksAndWait() {
		while (!this.tasks.isEmpty()) {
			DereferencingTask task = (DereferencingTask) this.tasks.getFirst();
			if (tryAssignTask(task)) {
				this.tasks.removeFirst();
				this.log.debug("Dequeue: <" + task.getURI() + ">@" + task.getStep() + 
						" (n = " + this.tasks.size() + ")");
			} else {
				break;
			}
		}
		try {
			// TODO Wake up when a worker thread is finished
			synchronized (this) {
				wait(100);
			}
		} catch (InterruptedException ex) {
			// Don't know when this happens
			throw new RuntimeException(ex);
		}
	}

	private boolean tryAssignTask(DereferencingTask task) {
		Iterator<DereferencerThread> it = this.threads.iterator();
		while (it.hasNext()) {
			DereferencerThread thread = it.next();
			if (thread.startDereferencingIfAvailable(task)) {
				return true;
			}
		}
		return false;
	}

	private void initThreadPool(int numThreads) {
		for (int i = 0; i < numThreads; i++) {
			DereferencerThread thread = new DereferencerThread(httpClient, spongerProvider);
			thread.setName("DerefThread"+i);
			thread.setMaxfilesize(this.maxfilesize);
			thread.start();
			this.threads.add(thread);
		}
	}
}
