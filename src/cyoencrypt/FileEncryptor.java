/*
[CyoEncrypt] FileEncryptor.java

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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class FileEncryptor {

	public FileEncryptor() {
		//todo
	}

	public void encryptFile(String pathname, String password) throws java.lang.Exception {
		final String encryptedSuffix = ".encrypted";
		String outputPathname = pathname;
		int opmode;
		int suffixPos = outputPathname.indexOf(encryptedSuffix);
		if (suffixPos >= 0) {
			// The file is already encrypted; decrypt it...
			opmode = Cipher.DECRYPT_MODE;
			outputPathname = outputPathname.substring(0, suffixPos); //remove the suffix
		}
		else {
			// The file is not encrypted; encrypt it...
			opmode = Cipher.ENCRYPT_MODE;
			outputPathname += encryptedSuffix; //append the suffix
		}

		char[] passwordChars = password.toCharArray();
		byte[] saltBytes = hashPassword(password, "SHA-256");
		byte[] ivBytes = hashPassword(password, "MD5");

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(passwordChars, saltBytes, 65536, 128);
		SecretKey tempKey = factory.generateSecret(spec);
		SecretKeySpec key = new SecretKeySpec(tempKey.getEncoded(), "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(opmode, key, new IvParameterSpec(ivBytes));

		final int MaxBytes = (16 * 1024); //16KiB
		byte[] buffer = new byte[MaxBytes];

		FileInputStream input = null;
		FileOutputStream output = null;
		try {
			input = new FileInputStream(pathname);
			output = new FileOutputStream(outputPathname);

			while (true) {
				int bytesRead = input.read(buffer);
				if (bytesRead <= 0)
					break;
				output.write(cipher.update(buffer, 0, bytesRead));
			}

			output.write(cipher.doFinal());

			input.close();
			input = null;

			System.out.println("Success");

			try {
				new java.io.File(pathname).delete();
			}
			catch (java.lang.Exception ex) {
				System.out.println("Unable to delete original file (" + ex.getMessage() + ")");
			}
		}
		finally {
			if (input != null)
				input.close();
			if (output != null)
				output.close();
		}
	}

	private byte[] hashPassword(String password, String algorithm) throws java.lang.Exception {
		byte[] bytes = password.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance(algorithm);
		md.update(bytes);
		return md.digest();
	}
}
