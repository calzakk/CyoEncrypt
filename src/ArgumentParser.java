/*
[CyoEncrypt] ArgumentParser.java

The MIT License (MIT)

Copyright (c) 2013-2016 Graham Bull

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package cyoencrypt;

public class ArgumentParser {

	public ArgumentParser(String[] args) {
		args_ = args;
	}

	public boolean help() { return help_; }
	public String getPathname() { return pathname_; }
	public String getPassword() { return password_; }
	public boolean getRecurse() { return recurse_; }
	public boolean getConfirm() { return confirm_; }

	public boolean parse() {
		if (args_.length == 0) {
			help_ = true;
			return true;
		}
		for (String arg : args_) {
			if (arg.equals("-?") || arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")
					|| arg.equals("/?") || arg.equalsIgnoreCase("/h") || arg.equalsIgnoreCase("/help")) {
				help_ = true;
				return true;
			}
			else if (arg.equalsIgnoreCase("-r") || arg.equalsIgnoreCase("--recurse")
					|| arg.equalsIgnoreCase("/r") || arg.equalsIgnoreCase("/recurse")) {
				recurse_ = true;
			}
			else if (arg.equalsIgnoreCase("--noconfirm")
					|| arg.equalsIgnoreCase("/noconfirm")) {
				confirm_ = false;
			}
			else if (pathname_ == null) {
				pathname_ = arg;
			}
			else if (password_ == null) {
				password_ = arg;
			}
			else {
				System.err.println("Invalid argument: " + arg);
				return false;
			}
		}
		return true;
	}

	private String[] args_;
	private boolean help_ = false;
	private String pathname_ = null;
	private String password_ = null;
	private boolean recurse_ = false;
	private boolean confirm_ = true;
}
