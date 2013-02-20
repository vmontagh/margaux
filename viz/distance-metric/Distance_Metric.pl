#!/usr/local/bin/perl
use strict;
use warnings;
use Text::CSV;
use FileHandle;

my $file = 'Complete_log.csv';
my $csv = Text::CSV->new();
my $CSV = new FileHandle;
my $count= 0;
my @first_col;
my @distance_vals;
my $val;

open ($CSV, "<", $file) or die $!;

while (my $cols_aref = $csv->getline($CSV)){
unless($count==0){
	 $first_col[$count] = "@{$cols_aref}[0]";
	 $distance_vals[$count] = Distance_Metric(@{$cols_aref});
	 #print $first_col[$count]."\t="."\t$distance_vals[$count]\n";
}
	 $count++;
}
close $CSV;

open FILE, ">", "HTML_DM.html" or die $!;
HTML_Header();
HTML_Table(\@first_col,\@distance_vals);
HTML_Footer();
close FILE;


sub Distance_Metric{
	$val = 0;
	unless ($_[1] eq ""){$val += 8};
	unless ($_[2] eq ""){$val += 6};
	unless ($_[3] eq ""){$val += 2};
	unless ($_[4] eq ""){$val += 5};
	unless ($_[5] eq ""){$val += 4};
	unless ($_[6] eq ""){$val += 4};
	unless ($_[7] eq ""){$val += 5};
	unless ($_[8] eq ""){$val += 5};
	unless ($_[9] eq ""){$val += 4};
	unless ($_[10] eq ""){$val += 4};
	unless ($_[11] eq ""){$val += 6};
	unless ($_[12] eq ""){$val += 6};
	unless ($_[13] eq ""){$val += 5};
	unless ($_[14] eq ""){$val += 4};
	unless ($_[15] eq ""){$val += 3};
	return $val;
}

sub HTML_Header{
	print FILE "<html>\n<head>\n<title>Distance Metric Results</title>\n</head>\n<body>\n<table border=\"1\">\n<tr>\n<th>Model</th>\n<th>Distance</th>\n</tr>\n";
}

sub HTML_Table{
my $i = 0;
my @local_col2 = @{$_[1]};
my @local_col1 = @{$_[0]};
my $temp;
	foreach $temp (@local_col1){
		print FILE "<tr>\n<td>".$temp. "<td>\n<td>".$local_col2[$i]."<td>\n<tr>\n";
		$i++;
	}
}

sub HTML_Footer{
print FILE "</body>\n</html>\n";
}
