The Kindle DX Collection Generator
===================================
 
On 3 December 2013, the source repository was migrated from
[SourceForge](http://kdxgen.sourceforge.net) to GitHub.


## Summary

1. _Introduction_ - What does the program do?
2. _Features_ - What are its features?
3. _Usage_ - How do you use it?
4. _Example_ - Illustration with an example scenario.
5. _Todo_ - Any room for further improvement?
6. _Source_ - How does the program work?
7. _Revisions_ - How has the software evolved?
  
  
## Introduction

The program kdxgen is a command line (CLI) and graphical user interface (GUI)
tool for generating Kindle book collections from a directory tree containing
e-books. The collection names reflect multi-level organisation, as they are
generated according to the directory tree. This program has only been tested
using GNU/Linux, and Mac OSX.

I started writing this program for personal use. I have seen GUI based
applications, such as [Calibre](http://calibre-ebook.com/) and [Kindle
Collection Manager](http://www.colegate.net/KindleCollectionManager/),
however I could not use them. The later is only available for Windows
systems; whereas, the former is more general purpose (and less suitable
for my needs). I organise my e-books using a directory tree, and hence,
I prefer a command line tool. Since January 2011, I have added GUI facility
for general use.

I hope that you will also enjoy using the tool, and find it useful.

     ______________________________________________________________________
    |                                                                      |
    |  DISCLAIMER    DISCLAIMER    DISCLAIMER    DISCLAIMER    DISCLAIMER  |   
    |----------------------------------------------------------------------|
    | Please note that this is not a commercial product. This program is   |
    | distributed in the hope that it will be useful, but WITHOUT ANY      |
    | WARRANTY; without even the implied warranty of MERCHANTABILITY or    |
    | FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License |
    | for more details.                                                    |
    |______________________________________________________________________|
 

## Features

* Generate collections automatically from the directory tree.
* Multi-level directory tree for better document organisation.
* No need to maintain collections directly from device.


## Usage

     $ java -jar kdxgen.jar [OPTIONS]
 
      -c         By default graphical user interface is assumed. Use this switch
                 to request command line interface. All of the following switches
                 are ignored in graphical mode.
             
      -d <arg>   Path to Kindle device root. This must point to the root directory
                 of the Kindle device.
            
      -l <arg>   The maximum number of characters allowed as collection names.
                 If the generated collection name is longer than the permitted,
                 it will be shortened to fit within the specified length. By
                 default, this value is set to 48 characters.
            
      -o <arg>   Send result to output file. If unspecified, result will be
                 sent to standard output (stdout).
            
      -v         Display log information on console. By default, log
                 information is directed to '/tmp/kdxgen.log' file only.


## Example

To illustrate usage, consider this directory structure at KDX mount point

     /mnt/kdx
        |____audible
        |____music
        |____system
        |____documents
                 |____Alpha
                 :       |____Fruits
                 :              |____apple.pdf
                 :              |____mango.azw
                 :              |____orange.azw
                 |____Beta
                 :       |____Animals
                 :              |____zebra.pdf
                 :              |____rhino.pdf
                 |____Gamma
                         |____Interesting sea creatures
                                |____Fish
                                      |____Big
                                      :     |____dolphin.azw
                                      :     |____whale.pdf
                                      |____Small
                                            |____lobster.azw
                                            |____seahorse.pdf


Running kdxgen, as shown below will produce a collections.json.

     $ cd /mnt/kdx/system
     $ mv collections.json collections.json.old
     $ java -jar kdxgen.jar -d /mnt/kdx -o collections.json

       ______________________________________________________________________
      |                                                                      |
      | WARNING     WARNING     WARNING     WARNING     WARNING     WARNING  |   
      |----------------------------------------------------------------------|
      | Please save your existing collections.json before running kdxgen as  |
      | shown above; otherwise, the current collections will be lost.        |
      |______________________________________________________________________|
 
 
The collections.json file should go inside the 'system' directory (see above).
Once loaded, restart the KDX (see Kindle* DX documentation). This could take
a while, depending on the size of the collection. Once everything is loaded,
the collections at the KDX 'home' should look like this:

     Alpha/Fruits
         apple.pdf    \
         mango.azw     } Collection contents; not displayed at KDX home. 
         orange.azw   /
     Beta/Animals
         zebra.pdf
         rhino.pdf
     Gamma/Interesting sea creatures/Fish/Big
         dolphin.azw
         whale.pdf
     Gamma/Interesting sea creatures/Fish/Small      
         lobster.azw
         seahorse.pdf


If the collection name is too long (default value is 48 characters), the
collection name will be shortened. For example, if we assume that the
maximum length is 35 characters, the following collection will be produced
instead of the previous.

     Alpha/Fruits
         apple.pdf
         mango.azw
         orange.azw
     Beta/Animals
         zebra.pdf
         rhino.pdf
     Gamma/Interesting sea creatures...      
         dolphin.azw
         whale.pdf
         lobster.azw
         seahorse.pdf

Note here that, when collections cannot be differentiated from one another,
all of the ebooks in the child subtrees are grouped together under the same
collection, as one would expect. In other words, the following erroneous
collection will not be generated. 

     Alpha/Fruits
         apple.pdf
         mango.azw
         orange.azw
     Beta/Animals
         zebra.pdf
         rhino.pdf
     Gamma/Interesting sea creatures...      
         dolphin.azw
         whale.pdf
     Gamma/Interesting sea creatures...
         lobster.azw
         seahorse.pdf

## Todo

The current version retrieves ebook meta-data by parsing the filename. Because
of this, some .azw or .azw1 files, such as

     Treasure-Island.azw

will not be added to a collection. If you use the verbose switch, `-v`, while
running the program, the files that have been skipped will be displayed. The
same is visible in the log file `/tmp/kdxgen.log`.

For an ebook to be added to a collection, it should either be a PDF file, or
if `.azw` or `.azw1` file, then it must have a name in the following format.

     The Adventures of Sherlock Holme-asin_B000JQU1VS-type_EBOK-v_0.azw

Future updates will extract the meta-data directly from the ebook.


## Source

Please download the source codes from GitHub

     $ git clone https://github.com/gyaikhom/kdxgen.git

or SourceForge.net subversion repository.

     $ svn co https://kdxgen.svn.sourceforge.net/svnroot/kdxgen kdxgen

This project uses [Apache Commons CLI library](http://commons.apache.org/cli/)
for parsing input arguments. All of the icons used, except for the kdxgen icon,
are part of the [Oxygen Icon theme](http://www.oxygen-icons.org/).


## Revisions

* Release 1.1.0
     - Graphical user interface.
     - Save collections directly to Kindle device.
* Release 1.0.1
     - Sort collections.
     - Convert timestamp to seconds.

***

* Kindle DX is a trademark of Amazon.com Inc.
  This project is not in any way affiliated with Amazon.com Inc.
* Windows is a trademark of Microsoft Inc.
* Mac OSX is a trademark of Apple Inc.
