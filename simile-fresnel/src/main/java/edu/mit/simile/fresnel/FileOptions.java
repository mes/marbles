package edu.mit.simile.fresnel;

public class FileOptions {
	private String _file;
	private String _format;

	protected FileOptions(String format, String file) {
		this._file = file;
		this._format = format;
	}

	public String getFormat() {
		return this._format;
	}

	public String getFile() {
		return this._file;
	}
}
