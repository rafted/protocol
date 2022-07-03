# Codegen
This module is a program that generates code for packing / unpacking packets.

# Philosophy
This has a lot of unsafe code (as in, code that might cause null pointer exceptions, and they are not handled), the
reasons for this are:
- this program will be ran under supervision
- this program *will* terminate when it's finished; it's not a server
- if there's an issue, it's probably a bad idea to continue as we want *everything* to work