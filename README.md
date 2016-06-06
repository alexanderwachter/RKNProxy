# RKN2016Proxy
MITM Proxy for Computer and Communication Networks written in Java

The `assignment.png` shows an overview and is used as a simple archive of https://teaching.iaik.tugraz.at/rkn/assignments/task2

## Features:
- JS Keylogger
- SOP deactivation
- HTTP and HTTPS proxying possible
- HTTP headers/cookies can be dumped
- HTTP content can be replaced (currently GIF, Text and JPEG are supported)
- Chunked Encoding supported
- Header changer externally configurable
- Content type downgraded from gzip

## Setup:
- The `RKNProxy` includes an Eclipse project used to build and manage the code.
- In the `js` folder you can find the used scripts.
- The `config` folder contains an example configuration of the server.

## TODO:
- SOP is not really bypassed, it's just deactivated.
- SSLStrip is not implemented
- JavaScript sources and Plugins needs to be updated

## General Information:
- This project is just for educational purpose, it's far from perfect
- There is no active development
- We do not gurantee that JS files will stay @ http://xn--4ca0b.eu/ forever.
- Patches are welcome!
