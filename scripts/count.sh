j=0
for i in `find . -maxdepth 1 -type f -name '*.out.txt'`; do
	#echo $i
	#rm $i 
	j=$((j+1))
done
echo $j