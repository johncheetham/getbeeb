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

use IO::Socket::INET;
use CGI qw/:standard/;
use XML::Parser;

my $link="";
my $page="";
my $href='';
my $f=0;

if (param()) {
       $link      = param('mslink');
} 

if ($link !~ /^http:\/\/www\.bbc\.co\.uk\//) {
	exit_error("script error - invalid hostname");	
} else {
	$page="$'";
}
if ($page !~ /[A-Za-z0-9\/]/) {
	exit_error("script error - invalid mslink");
}

# initialize the parser
my $parser = XML::Parser->new( Handlers => 
                                     {
                                      Start=>\&handle_start
                                     # End=>\&handle_end,
                                     });

my $sock = IO::Socket::INET->new(PeerAddr => "www.bbc.co.uk",
                                 PeerPort => 'http(80)',
                                 Proto    => 'tcp')
			or exit_error("Can't connect $@\n");
	
print $sock "GET /$page HTTP/1.0\r\n\r\n" or exit_error("Can't send");       

my $xmldata;
my $prt=0;
my $lc=0;
while (my $line = <$sock>) {		
	if (!$prt and $line =~ /^<\?xml/) {
		$prt=1;
	}
	if ($prt) {
		$lc++;		
		$xmldata.=$line;
	}
}
close $sock;

$parser->parse($xmldata);

if ($href) {
	#
	# Check for Pocket PC
	#
	##if ($ENV{'HTTP_USER_AGENT'} =~ /MSIE 4.01; Windows CE; PPC;/) {
		print "Content-type: text/html\r\n\r\n";
		print "<html>";
		print "<head>";	
		print "</head>";
		print "<body>";
		print "click on link<br><br>";
		print "<a href=\"$href\">$href</a>";
		print "</body>";
		print "</html>";
	##} else {			
		#print "Content-type: audio/x-pn-realaudio\r\n\r\n";
		#print $href;
		
		#
		#	print "Content-type: text/html\r\n\r\n";
		#       print "<html>";
		#	print "<head>";
		#	print "<meta content='1;url=$href/' http-equiv='refresh'/>";
		#	print "</head>";
		#	print "<body>";
		#	print "redirecting";
		#	print "</body>";
		#	print "</html>";
		#
	##}

} else {
	print "Content-type: text/html\r\n\r\n";
	print "unavailable";
}
#
# process a start-of-element event: print message about element
#
sub handle_start {
    my( $expat, $element, %attrs ) = @_;    
  
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

}

sub exit_error {	
	print "Content-type: text/html\r\n\r\n";
	print shift;
	exit;
}
