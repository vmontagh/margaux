 grep '^-1,' "$1"  | sed 's/-1,-1,-1,-1,-1,-1,-1,//' | sed 'p;s/$/.als.out.txt.timedout/' | sed 's/_$/_.als.out.txt/' | xargs -n2 mv
