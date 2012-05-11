FixedFontOCR
============

FixedFontOCR recognizes the characters in a target image by matching pixel-for-pixel 
an area of the image to a set of glyphs from a specified font.   It is useful for parsing the text
from a screenshot for example.  

It does not work with aliased fonts since the pixels of such fonts are partially transparent.
For the time being, it cannot auto-detect the text areas in an image: the user must provide the
point in the image where the parsing should start.

It can make some "mistakes" since some characters in some fonts have the exact same glyph.
For example, upper case 'o' might be identical to zero and capital 'i', the letter 'l' and the 
vertical bar might also have the same glyph. 

You can see some usage examples in the test directory.