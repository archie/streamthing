package eu.emdc.streamthing;

/**
 * Implemented as a circular buffer with messages. 
 */
public class VideoBuffer<T> {
	
	private T buffer[];
	private int head;
	private int tail;
	private int current_size;
	private int max_size;
	
	@SuppressWarnings("unchecked")
	public VideoBuffer(int size) {
		buffer = (T[]) new Object[size];
		tail = 0;
		head = 0;
		current_size = 0;
		max_size = size;
	}
	
	/**
	 * Add element to buffer. 
	 * @param object
	 * @return false if buffer is full
	 */
	public boolean add(T object) {
		if (!isFull()) {
			current_size++;
			tail = (tail + 1) % max_size;
			buffer[tail] = object;
			return true;
		} else 
			return false;
	}
	
	/**
	 * Retrieve an element from the buffer
	 * @return null if buffer is empty
	 */
	public T get() {
		if (!isEmpty()) {
			current_size--;
			head = (head+1) % max_size;
			return buffer[head];
		} else 
			return null;
	}
	
	public boolean isEmpty() {
		return current_size == 0;
	}
	
	public boolean isFull() {
		return current_size == max_size;
	}
	
	public int size() {
		return current_size;
	}

}
