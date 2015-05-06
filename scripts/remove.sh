cd /Users/vajih/Documents/workspace-git/alloy/expr_output
for i in `find . -maxdepth 1 -type f -name '*.out.txt'`; do
	#echo $i
	rm $i 
done