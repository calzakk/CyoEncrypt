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
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class FileEncryptor {

    public void encryptFile(String pathname, String password) throws Exception {
        final String encryptedSuffix = ".encrypted";
        String outputPathname = pathname;
        int suffixPos = outputPathname.indexOf(encryptedSuffix);
        boolean isEncrypted = (suffixPos >= 0);
        if (isEncrypted) {
            // The file is already encrypted; decrypt it...
            outputPathname = outputPathname.substring(0, suffixPos); // remove the suffix
        } else {
            // The file is not encrypted; encrypt it...
            outputPathname += encryptedSuffix; // append the suffix
        }

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(pathname);
            output = new FileOutputStream(outputPathname);

            encryptStream(input, output, password, isEncrypted);

            output.close();
            output = null;

            input.close();
            input = null;

            System.out.println("Success");

            try {
                new java.io.File(pathname).delete();
            } catch (java.lang.Exception ex) {
                System.out.println("Unable to delete original file (" + ex.getMessage() + ")");
            }
        } finally {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
        }
    }

    private static final int VERSION_MAJOR = 2;
    private static final int VERSION_MINOR = 0;
    private static final int VERSION = (VERSION_MAJOR << 16) | VERSION_MINOR;

    private static final int PREAMBLE = 0x43594F00;
    private static final int TRAILER = 0x5A5A5A5A;

    private static final int SALT_LENGTH = 32;
    private static final int IV_LENGTH = 16;

    private static final String RANDOM_ALGORITHM = "SHA1PRNG";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String SECRET_KEY_ALGORITHM = "AES";

    private final class HeaderReader {
        public HeaderReader(byte[] header) {
            buffer_ = ByteBuffer.wrap(header);
        }

        public int getInt() {
            int val = buffer_.getInt(offset_);
            offset_ += Integer.BYTES;
            return val;
        }

        public long getLong() {
            long val = buffer_.getLong(offset_);
            offset_ += Long.BYTES;
            return val;
        }

        public void getBytes(byte[] bytes) {
            for (int i = 0; i < bytes.length; ++i) {
                bytes[i] = buffer_.get(offset_++);
            }
        }

        private ByteBuffer buffer_;
        private int offset_ = 0;
    }

    private final class HeaderWriter {
        public void create(byte[] header, byte[] salt, byte[] iv) {
            offset_ = 0;
            buffer_ = ByteBuffer.wrap(header);
            addInt(PREAMBLE);
            addInt(VERSION);
            addLong(0); // reserved
            addInt(salt.length);
            addBytes(salt);
            addInt(iv.length);
            addBytes(iv);
            addInt(calculateChecksum(salt, iv));
            addInt(TRAILER);
        }

        private ByteBuffer buffer_;
        private int offset_ = 0;

        private void addInt(int value) {
            buffer_.putInt(offset_, value);
            offset_ += Integer.BYTES;
        }

        private void addLong(long value) {
            buffer_.putLong(offset_, value);
            offset_ += Long.BYTES;
        }

        private void addBytes(byte[] bytes) {
            for (byte b : bytes) {
                buffer_.put(offset_++, b);
            }
        }
    }

    private void encryptStream(FileInputStream input, FileOutputStream output, String password, boolean isEncrypted)
            throws Exception {
        char[] passwordChars = password.toCharArray();

        final int MaxBytes = (16 * 1024); // 16KiB
        byte[] buffer = new byte[MaxBytes];

        byte[] saltBytes = new byte[SALT_LENGTH];
        byte[] ivBytes = new byte[IV_LENGTH];
        int headerSize = (Integer.BYTES * 6 + Long.BYTES + saltBytes.length + ivBytes.length);
        assert headerSize == 80;
        byte[] headerBytes = new byte[headerSize];
        boolean isVersion1 = true;

        if (isEncrypted) {
            // Decrypting; attempt to read header...
            int bytesRead = input.read(headerBytes);
            if (bytesRead == headerBytes.length) {
                HeaderReader headerReader = new HeaderReader(headerBytes);
                int preamble = headerReader.getInt();
                int version = headerReader.getInt();
                long reserved = headerReader.getLong();
                int saltLength = headerReader.getInt();
                headerReader.getBytes(saltBytes);
                int ivLength = headerReader.getInt();
                headerReader.getBytes(ivBytes);
                int checksum = headerReader.getInt();
                int trailer = headerReader.getInt();
                if ((preamble == PREAMBLE) && versionIsAtLeast(version, 2, 0) && (reserved == 0)
                        && (saltLength == SALT_LENGTH) && (ivLength == IV_LENGTH)
                        && (checksum == calculateChecksum(saltBytes, ivBytes)) && (trailer == TRAILER)) {
                    isVersion1 = false;
                }
            }
            if (isVersion1) {
                // Insecure old-style encryption...
                saltBytes = hashPassword(password, "SHA-256");
                ivBytes = hashPassword(password, "MD5");
            }
        } else {
            // Encrypting; generate new header...
            SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
            random.nextBytes(saltBytes);
            random.nextBytes(ivBytes);
            HeaderWriter headerWriter = new HeaderWriter();
            headerWriter.create(headerBytes, saltBytes, ivBytes);
            output.write(headerBytes);
        }

        // Create the encryption or decryption key...
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(passwordChars, saltBytes, 65536, 128);
        SecretKey tempKey = factory.generateSecret(spec);
        SecretKeySpec key = new SecretKeySpec(tempKey.getEncoded(), SECRET_KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        int opmode = (isEncrypted ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE);
        cipher.init(opmode, key, new IvParameterSpec(ivBytes));

        // Encrypt or decrypt the data...
        if (isEncrypted && isVersion1) {
            // Decrypt the data that wasn't actually a version 2+ header...
            output.write(cipher.update(headerBytes, 0, headerBytes.length));
        }
        while (true) {
            int bytesRead = input.read(buffer);
            if (bytesRead <= 0)
                break;
            output.write(cipher.update(buffer, 0, bytesRead));
        }
        output.write(cipher.doFinal());
    }

    private byte[] hashPassword(String password, String algorithm) throws Exception {
        byte[] bytes = password.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(bytes);
        return md.digest();
    }

    private int calculateChecksum(final byte[] saltBytes, final byte[] ivBytes) {
        int checksum = 0;

        for (byte b : saltBytes) {
            checksum = (checksum << 8) | (checksum >> 24); // rotate left 8 bits
            checksum ^= (int) b;
        }

        for (byte b : ivBytes) {
            checksum = (checksum >> 4) | (checksum << 28); // rotate right 4 bits
            checksum ^= (int) b;
        }

        return checksum;
    }

    private boolean versionIsAtLeast(int version, int major, int minor) {
        int versionMajor = (version >> 16);
        if (versionMajor > major)
            return true;
        if (versionMajor < major)
            return false;
        assert versionMajor == major;

        int versionMinor = (version & 0xffff);
        if (versionMinor < minor)
            return false;
        assert versionMinor >= minor;
        return true;
    }
}
