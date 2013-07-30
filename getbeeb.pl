#!/usr/bin/perl

#
#   getbeeb.pl   
#  
#   This file is part of getbeeb
#
#   getbeeb is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   getbeeb is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with getbeeb.  If not, see <http://www.gnu.org/licenses/>.
 	    
use strict;

use LWP::Simple;
use CGI qw(:cgi);
use XML::Parser;

my $link="";
my $page="";
my $href='';
my $href2='';
my $f=0;
my $f2=0;

if (param()) {
    $link = param('mslink');
} 

if ($link !~ /^http:\/\/www\.bbc\.co\.uk\//) {
    exit_error("script error - invalid hostname");	
} else {
    #$page="$'";
    $page=$link;
}
if ($page !~ /[A-Za-z0-9\/]/) {
    exit_error("script error - invalid mslink");
}

# for testing
#$page = 'http://localhost/projects/getbeeb/b00s2x98';
#$page = 'http://localhost/projects/getbeeb/test.txt';

# initialize the parser
my $parser = XML::Parser->new( Handlers => 
    {
        Start=>\&handle_start
        # End=>\&handle_end,
    });

my $xmldata = get($page);

$parser->parse($xmldata);

print "Content-type: text/html\r\n\r\n";
print "<html>";
print "<head>";	
print "</head>";
print "<body>";
if ($href) {
    print "Click on the link to play the selection<br><br>";
    print "<a href=\"$href\">$href</a> (real audio format)<br><br>";
} elsif ($href2) {
    print "Click on the link to play the selection<br><br>";
    print "<a href=\"$href2\">$href2</a> (wma format)";
} else {
    print "Selection is Unavailable";
}

#
# process a start-of-element event: print message about element
#
sub handle_start {
    my( $expat, $element, %attrs ) = @_;    
  
    # get real audio href
    if ($element eq 'media') {
        if (($attrs{'kind'} eq 'audio') and ($attrs{'encoding'} eq 'real')) {
           $f=1;
        } else {
           $f=0;
        }	
    } elsif ($element eq 'connection') {
        if ($f and !$href) {
          $href=$attrs{'href'};
        }      
    }

    # get wma href2
    if ($element eq 'media') {
        if (($attrs{'kind'} eq 'audio') and ($attrs{'encoding'} eq 'wma9')) {
           $f2=1;
        } else {
           $f2=0;
        }	
    } elsif ($element eq 'connection') {
        if ($f2 and !$href2) {
          $href2=$attrs{'href'};
        }      
    }

}

sub exit_error {	
    print "Content-type: text/plain\r\n\r\n";   
    print shift;    
    exit;
}
