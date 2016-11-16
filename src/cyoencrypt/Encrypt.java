/*
[CyoEncrypt] Encrypt.java

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

import java.io.File;

public class Encrypt {

	public static void main(String[] args) {
		try {
			ArgumentParser argparser = new ArgumentParser(args);
			if (!argparser.parse())
				System.exit(1);

			if (argparser.help()) {
				System.err.println("Usage:\n"
					+ "  ENCRYPT <pathname> <password>\n"
					+ "  ENCRYPT <path> <password> [-r|--recurse]");
				System.exit(1);
			}

			String pathname = argparser.getPathname();
			String password = argparser.getPassword();
			boolean recurse = argparser.getRecurse();
			boolean confirm = argparser.getConfirm();

			if (pathname == null) {
				System.err.println("Pathname not specified!");
				System.exit(1);
			}

			if (password == null) {
				System.out.print("Password: ");
				password = System.console().readLine();
				if (password.isEmpty()) {
					System.err.println("Password not specified!");
					System.exit(1);
				}
			}

			if (confirm) {
				System.out.print("Confirm: ");
				String password2 = System.console().readLine();
				if (password2 == null) {
					System.out.println();
					System.exit(1);
				}
				if (!password2.equals(password)) {
					System.err.println("Passwords do not match!");
					System.exit(1);
				}
			}
			
			File file = new File(pathname);
			if (!file.exists())
				throw new java.lang.Exception(recurse ? "Folder not found" : "File or folder not found");
			if (file.isFile())
				EncryptFile(pathname, password);
			else if (file.isDirectory())
				EncryptFolder(pathname, password, recurse);
			else
				throw new java.lang.Exception("Not a file or folder");
		}
		catch (java.lang.Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void EncryptFile(String pathname, String password) throws java.lang.Exception {
		FileEncryptor encryptor = new FileEncryptor();
		encryptor.encryptFile(pathname, password);
	}

	private static void EncryptFolder(String pathname, String password, boolean recurse) throws java.lang.Exception {
		throw new java.lang.Exception("Not yet implemented");
	}
}
