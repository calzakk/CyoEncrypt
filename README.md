# CyoEncrypt

CyoEncrypt is an easy-to-use command-line file encryptor.

## Features

* Written in Java.
* CyoEncrypt can encrypt (and decrypt) a single file.
* TODO: Recursive encryption (and decryption) of folders.
* Files are encrypted using AES.
* Encrypted files are identified with an extension of ".encrypted". 

## Usage

### Windows

Build:

    buildjar.bat

Encrypt a file:

    cyoencrypt.bat filename password

Decrypt an encrypted file:

    cyoencrypt.bat filename.encrypted password

Options:

    /noconfirm  Skip password confirmation

### Linux

Build:

    ./buildjar.sh

Encrypt a file:

    ./cyoencrypt.sh filename password

Decrypt an encrypted file:

    ./cyoencrypt.sh filename.encrypted password

Options:

    --noconfirm  Skip password confirmation

## Platforms

CyoEncrypt is known to work on Windows (using Oracle Java) and Linux (using OpenJDK).

## License

### The MIT License (MIT)

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
