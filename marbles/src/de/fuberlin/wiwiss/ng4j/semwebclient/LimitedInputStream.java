package de.fuberlin.wiwiss.ng4j.semwebclient;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream{
	private InputStream inputStream;
	private int limit;
	private long count=0;
	
	public LimitedInputStream(InputStream io,int limit){
		this.inputStream = io;
		this.limit = limit;
	}
	public int read() throws IOException{
		this.count++;
		if(count >= this.limit)
			throw new IOException();
		return this.inputStream.read();
	}
	public int available()throws IOException{
		return this.inputStream.available();
	}
	public void close()throws IOException{
		this.inputStream.close();
	}
	public void mark(int m){
		this.inputStream.mark(m);
	}
	public boolean 	markSupported() {
		return this.inputStream.markSupported();
	}

	public int read(byte[] b)throws IOException{
		return this.read(b);
	}
	
	public int read(byte[] b, int off, int len)throws IOException {
		return this.inputStream.read(b, off, len);
	}
	public void reset()throws IOException{
		this.inputStream.reset();
	}
	public long skip(long l)throws IOException{
		return this.inputStream.skip(l);
	}
	

}
